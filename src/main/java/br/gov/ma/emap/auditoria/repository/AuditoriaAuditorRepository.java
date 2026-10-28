package br.gov.ma.emap.auditoria.repository;

import br.gov.ma.emap.auditoria.model.Auditoria;
import br.gov.ma.emap.auditoria.model.AuditoriaAuditor;
import br.gov.ma.emap.auditoria.model.Campanha;
import br.gov.ma.emap.auditoria.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuditoriaAuditorRepository extends JpaRepository<AuditoriaAuditor, Long> {

    boolean existsByAuditoriaAndAuditorAndFimIsNull(Auditoria auditoria, Usuario auditor);
    List<AuditoriaAuditor> findByAuditoriaAndFimIsNull(Auditoria auditoria);
    Optional<AuditoriaAuditor> findByAuditoriaAndAuditorAndFimIsNull(Auditoria auditoria, Usuario auditor);
    boolean existsByAuditoriaCampanha(Campanha campanha);

    @Query("""
        SELECT aa FROM AuditoriaAuditor aa JOIN FETCH aa.auditor WHERE aa.auditoria.id IN :auditoriasIds AND aa.fim IS NULL
    """)
    List<AuditoriaAuditor> findAtivosByAuditoriasIds(@Param("auditoriasIds") List<Long> auditoriasIds);

    List<AuditoriaAuditor> findByAuditoriaCampanha(Campanha campanha);

    @Query("""
        SELECT aa FROM AuditoriaAuditor aa JOIN FETCH aa.auditor JOIN FETCH aa.auditoria
        WHERE aa.auditoria.campanha = :campanha
    """)
    List<AuditoriaAuditor> findByCampanhaComAuditorEAuditoria(@Param("campanha") Campanha campanha);

    @Query("""
        SELECT aa FROM AuditoriaAuditor aa JOIN FETCH aa.auditor WHERE aa.auditoria = :auditoria AND aa.fim IS NULL
    """)
    List<AuditoriaAuditor> findAtivosByAuditoria(@Param("auditoria") Auditoria auditoria);

}