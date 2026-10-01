package br.gov.ma.emap.auditoria.repository;

import br.gov.ma.emap.auditoria.model.Campanha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampanhaRepository extends JpaRepository<Campanha, Long> {

    Optional<Campanha> findByCompetencia(Integer competencia);
    List<Campanha> findByCompetenciaBetween(Integer competenciaInicio, Integer competenciaFim);
}