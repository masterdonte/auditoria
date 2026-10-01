package br.gov.ma.emap.auditoria.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "É necessário informar o usuário")
        String usuario,
        @NotBlank(message = "É necessário informar a senha")
        String senha
) {
}