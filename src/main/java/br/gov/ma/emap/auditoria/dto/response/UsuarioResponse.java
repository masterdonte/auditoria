package br.gov.ma.emap.auditoria.dto.response;

import br.gov.ma.emap.auditoria.dto.enums.TipoPerfil;
import br.gov.ma.emap.auditoria.model.Perfil;
import br.gov.ma.emap.auditoria.model.Usuario;

import java.time.OffsetDateTime;
import java.util.List;

public record UsuarioResponse(
        Long id,
        String login,
        String nome,
        String email,
        Boolean ativo,
        List<TipoPerfil> perfis,
        OffsetDateTime updatedAt
) {

    public static UsuarioResponse from(Usuario usuario) {
        List<TipoPerfil> perfis = usuario.getPerfis().stream().map(Perfil::getNome).toList();
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getLogin(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getAtivo(),
                perfis,
                usuario.getUpdatedAt()
        );

    }

}