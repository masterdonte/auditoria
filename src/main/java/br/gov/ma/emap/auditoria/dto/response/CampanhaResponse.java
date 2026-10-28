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
        Long ecmCodAudit,
        OffsetDateTime ecmLastSync,
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
                campanha.getEcmCodAudit(),
                campanha.getEcmLastSync(),
                campanha.getAprovacao(),
                campanha.getCreatedAt(),
                campanha.getUpdatedAt()
        );
    }
}