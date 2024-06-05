package com.unifacisa.p2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unifacisa.p2.entity.Acao;

import java.util.UUID;

@Repository
public interface AcaoRepository extends JpaRepository<Acao, UUID> {
}