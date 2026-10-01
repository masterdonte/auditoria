package br.gov.ma.emap.auditoria.dto.response;

import br.gov.ma.emap.auditoria.model.Parametrizacao;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ParametrizacaoResponse(
        Long id,
        String versao,
        BigDecimal percDiretoria,
        BigDecimal percMaterial,
        BigDecimal percAleatorio,
        BigDecimal percTemporal,
        Integer recorrenciaMaterial,
        Integer tempoSemAuditoria,
        String observacao,
        Boolean ativo,
        String createdBy,
        OffsetDateTime createdAt
) {

    public static ParametrizacaoResponse from(Parametrizacao parametrizacao) {
        return new ParametrizacaoResponse(
                parametrizacao.getId(),
                parametrizacao.getVersao(),
                parametrizacao.getPercDiretoria(),
                parametrizacao.getPercMaterial(),
                parametrizacao.getPercAleatorio(),
                parametrizacao.getPercTemporal(),
                parametrizacao.getRecorrenciaMaterial(),
                parametrizacao.getTempoSemAuditoria(),
                parametrizacao.getObservacao(),
                parametrizacao.getAtivo(),
                parametrizacao.getCreatedBy().getUsername(),
                parametrizacao.getCreatedAt()
        );
    }
}