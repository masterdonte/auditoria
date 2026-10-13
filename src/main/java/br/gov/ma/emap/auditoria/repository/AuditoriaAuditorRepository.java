package br.gov.ma.emap.auditoria.repository;

import br.gov.ma.emap.auditoria.model.Auditoria;
import br.gov.ma.emap.auditoria.model.AuditoriaAuditor;
import br.gov.ma.emap.auditoria.model.Campanha;
import br.gov.ma.emap.auditoria.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuditoriaAuditorRepository extends JpaRepository<AuditoriaAuditor, Long> {

    boolean existsByAuditoriaAndAuditorAndFimIsNull(Auditoria auditoria, Usuario auditor);
    List<AuditoriaAuditor> findByAuditoriaAndFimIsNull(Auditoria auditoria);
    Optional<AuditoriaAuditor> findByAuditoriaAndAuditorAndFimIsNull(Auditoria auditoria, Usuario auditor);
    boolean existsByAuditoriaCampanha(Campanha campanha);

}