package br.gov.ma.emap.auditoria.dto.response;

import br.gov.ma.emap.auditoria.dto.enums.TipoPerfil;
import br.gov.ma.emap.auditoria.model.Perfil;
import br.gov.ma.emap.auditoria.model.Usuario;

import java.util.ArrayList;
import java.util.List;

public record LoginResponse(
        String token,
        String login,
        String nome,
        List<TipoPerfil> perfis
) {
    public static LoginResponse from(Usuario usuario, String token) {
        return new LoginResponse(
                token,
                usuario.getLogin(),
                usuario.getNome(),
                usuario.getPerfis().stream().map(Perfil::getNome).toList()
        );
    }
}
