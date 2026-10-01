package br.gov.ma.emap.auditoria.dto.response;

import br.gov.ma.emap.auditoria.model.Campanha;

import java.time.OffsetDateTime;

public record CampanhaResponse(
        Long id,
        Integer competencia,
        String parametrizacao,
        OffsetDateTime execucaoMotor,
        Integer qtdUniverso,
        Integer qtdAmostra,
        OffsetDateTime aprovacao,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static CampanhaResponse from(Campanha campanha) {
        return new CampanhaResponse(
                campanha.getId(),
                campanha.getCompetencia(),
                campanha.getParametrizacao().getVersao(),
                campanha.getExecucaoMotor(),
                campanha.getQtdUniverso(),
                campanha.getQtdAmostra(),
                campanha.getAprovacao(),
                campanha.getCreatedAt(),
                campanha.getUpdatedAt()
        );
    }
}