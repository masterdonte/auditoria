package br.gov.ma.emap.auditoria.repository;

import br.gov.ma.emap.auditoria.model.Parametrizacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParametrizacaoRepository extends JpaRepository<Parametrizacao, Long> {

    Optional<Parametrizacao> findByAtivoTrue();
}