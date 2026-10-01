package br.gov.ma.emap.auditoria.dto.request;

import jakarta.validation.constraints.NotNull;

public record CampanhaRequest(

        @NotNull(message = "A competência é obrigatória")
        Integer competencia

) {
}