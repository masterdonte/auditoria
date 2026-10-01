package br.gov.ma.emap.auditoria.dto.request;

public record UsuarioRequest(
        String login,
        Boolean ativo
) {}