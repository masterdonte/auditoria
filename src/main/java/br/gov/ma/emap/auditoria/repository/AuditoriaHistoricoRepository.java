package br.gov.ma.emap.auditoria.repository;

import br.gov.ma.emap.auditoria.model.AuditoriaHistorico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaHistoricoRepository extends JpaRepository<AuditoriaHistorico, Long> {


}