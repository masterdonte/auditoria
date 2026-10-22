package br.gov.ma.emap.auditoria.dto.request;

import java.util.List;

public record DistribuicaoRequest(
        Long auditoriaId,
        List<Long> auditoresIds
) {
}
