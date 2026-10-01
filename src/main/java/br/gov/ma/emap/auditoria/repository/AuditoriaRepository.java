package br.gov.ma.emap.auditoria.repository;

import br.gov.ma.emap.auditoria.model.Auditoria;
import br.gov.ma.emap.auditoria.model.Campanha;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    List<Auditoria> findByCampanhaCompetenciaIn(Collection<Integer> competencias);
    void deleteByCampanhaId(Long campanhaId);
    Page<Auditoria> findByCampanhaId(Long campanhaId, Pageable pageable);
    List<Auditoria> findByCampanhaAndProcesso(Campanha campanha, String processo);
    List<Auditoria> findByCampanha(Campanha campanha);
}