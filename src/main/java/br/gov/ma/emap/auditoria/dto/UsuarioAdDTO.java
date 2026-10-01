package br.gov.ma.emap.auditoria.dto;

public record UsuarioAdDTO(
        String login,
        String nome,
        String email
) {}