package br.gov.ma.emap.auditoria.repository;

import br.gov.ma.emap.auditoria.model.AuditoriaLegado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AuditoriaLegadoRepository extends JpaRepository<AuditoriaLegado, Long> {

    List<AuditoriaLegado> findByReferenciaGreaterThanEqual(Integer referencia);
    List<AuditoriaLegado> findByReferenciaIn(Collection<Integer> referencias);

}