package com.unifacisa.p2.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unifacisa.p2.entity.Acao;
import com.unifacisa.p2.service.AcaoService;


@RestController
@RequestMapping("/acoes")
public class AcaoController {
    @Autowired
    private AcaoService acaoService;

    @GetMapping
    public ResponseEntity<List<Acao>> listarAcoes() {
        List<Acao> acoes = acaoService.listarAcoes();
        return ResponseEntity.ok(acoes);
    }

    @PostMapping("/salvar")
    public ResponseEntity<List<Acao>> salvarAcoes() {
        try {
            List<Acao> acoesSalvas = acaoService.salvarAcao();

            return ResponseEntity.ok(acoesSalvas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
