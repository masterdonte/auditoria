package br.gov.ma.emap.auditoria.repository;

import br.gov.ma.emap.auditoria.dto.enums.TipoPerfil;
import br.gov.ma.emap.auditoria.model.Parametrizacao;
import br.gov.ma.emap.auditoria.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLogin(String login);
    List<Usuario> findDistinctByAtivoTrueAndPerfisNome(TipoPerfil perfil);

}