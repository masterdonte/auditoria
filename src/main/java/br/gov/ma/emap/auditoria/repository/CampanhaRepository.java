package br.gov.ma.emap.auditoria.repository;

import br.gov.ma.emap.auditoria.model.Campanha;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampanhaRepository extends JpaRepository<Campanha, Long> {

    Optional<Campanha> findByCompetencia(Integer competencia);
    List<Campanha> findByCompetenciaBetween(Integer competenciaInicio, Integer competenciaFim);
    Optional<Campanha> findByIdAndAprovacaoIsNotNull(Long campanhaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT c FROM Campanha c WHERE c.id = :id AND c.aprovacao IS NOT NULL
    """)//Produz => SELECT * FROM campanha WHERE id = ? AND aprovacao IS NOT NULL FOR UPDATE; //fique bloqueada até o COMMIT ou ROLLBACK da transação.
    Optional<Campanha> findAprovadaForUpdate(@Param("id") Long id);
}