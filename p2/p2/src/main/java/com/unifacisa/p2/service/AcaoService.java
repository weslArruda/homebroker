package com.unifacisa.p2.service;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import com.unifacisa.p2.entity.Acao;
import com.unifacisa.p2.repository.AcaoRepository;

@Service
public class AcaoService {
      @Autowired
      
    private AcaoRepository acaoRepository;
    //private static final Logger log = (Logger) LoggerFactory.getLogger(SchedulerConfig.class);

    public JSONArray buscarAcoesDoSite() {
        try {
            // Crie um JSONArray para armazenar as opções
            JSONArray jsonArray = new JSONArray();

            // Parte 1: Obtenha a lista de ações de "https://opcoes.net.br/opcoes2/bovespa"
            String bovespaUrl = "https://opcoes.net.br/opcoes2/bovespa";
            Document bovespaDocument = Jsoup.connect(bovespaUrl).get();

            // Encontre o elemento <select> com o atributo name="IdAcao"
            Element selectElement = bovespaDocument.select("select[name=IdAcao]").first();

            // Verifique se o elemento foi encontrado
            if (selectElement != null) {
                // Obtenha todas as opções dentro do elemento <select>
                Elements options = selectElement.select("option");

                for (Element option : options) {
                    String acaoNome = option.val(); // Nome da ação

                    // Crie um objeto JSON para cada ação
                    JSONObject jsonOption = new JSONObject();
                    jsonOption.put("acao", acaoNome);

                    // Adicione o objeto JSON ao JSONArray
                    jsonArray.add(jsonOption);
                }

                // Parte 2: Obtenha as cotações de todas as ações construindo a URL para cada ação
                String cotacaoBaseUrl = "https://opcoes.net.br/cotacoes?ativos=";

                // Use o Apache HttpClient para fazer a solicitação HTTP para cada ação
                try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                    JSONArray novoJsonArray = new JSONArray(); // Criar um novo JSONArray

                    for (Object acaoObj : jsonArray) {
                        JSONObject acaoJSON = (JSONObject) acaoObj;
                        String acaoNome = (String) acaoJSON.get("acao");

                        // Construa a URL da cotação para a ação específica
                        String cotacaoUrl = cotacaoBaseUrl + acaoNome;

                        // Faça a solicitação HTTP para obter a página da web
                        HttpGet httpGet = new HttpGet(cotacaoUrl);
                        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                            // Analise o JSON retornado
                            JSONParser parser = new JSONParser();
                            JSONObject jsonObject = (JSONObject) parser.parse(EntityUtils.toString(response.getEntity()));

                            // Extraia as informações da página da web
                            JSONObject data = (JSONObject) jsonObject.get("data");
                            if (data.containsKey(acaoNome)) {
                                JSONObject acaoData = (JSONObject) data.get(acaoNome);

                                if (acaoData.containsKey("ULT")) {
                                    Double valor = (Double) acaoData.get("ULT");

                                    // Crie um único objeto JSON com o valor atualizado
                                    JSONObject novoAcaoJSON = new JSONObject();
                                    novoAcaoJSON.put("acao", acaoNome);
                                    novoAcaoJSON.put("valor", valor);

                                    // Adicione o objeto JSON atualizado ao novo JSONArray
                                    novoJsonArray.add(novoAcaoJSON);
                                }
                            }

                        } catch (Exception e) {
                            System.out.println("Erro ao obter cotação da ação: " + acaoNome);
                            e.printStackTrace();
                        }
                    }
                    // Atribuir o novo JSONArray ao jsonArray original
                    jsonArray = novoJsonArray;
                } catch (Exception e) {
                    System.out.println("Erro ao obter cotações de ações");
                    e.printStackTrace();
                }
            } else {
                System.out.println("Elemento <select> com name='IdAcao' não encontrado.");
            }

            return jsonArray;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Acao> salvarAcao() {
        JSONArray jsonArray = buscarAcoesDoSite(); // Chama o método para buscar as informações
        // Agora você tem o JSONArray com os dados das ações

        List<Acao> acoesSalvas = new ArrayList<>();

        // Itera sobre o JSONArray e salva no banco
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject acaoJSON = (JSONObject) jsonArray.get(i);
            String acaoNome = (String) acaoJSON.get("acao");
            Double valor = (Double) acaoJSON.get("valor");

            // Crie um novo objeto Acoes para cada ação
            Acao acao = new Acao();
            acao.setNomeAcao(acaoNome);
            acao.setValor(valor);

            // Salve as informações da ação no banco de dados
            acaoRepository.save(acao);

            // Adicione a ação salva à lista de ações salvas
            acoesSalvas.add(acao);
        }

        return acoesSalvas;
    }


    public List<Acao> listarAcoes() {
        return acaoRepository.findAll();
    }
}
