package br.gov.ma.emap.auditoria.service;

import br.gov.ma.emap.auditoria.dto.UsuarioAdDTO;
import br.gov.ma.emap.auditoria.dto.enums.TipoPerfil;
import br.gov.ma.emap.auditoria.dto.request.UsuarioRequest;
import br.gov.ma.emap.auditoria.dto.response.ParametrizacaoResponse;
import br.gov.ma.emap.auditoria.dto.response.UsuarioResponse;
import br.gov.ma.emap.auditoria.exception.BusinessException;
import br.gov.ma.emap.auditoria.model.Perfil;
import br.gov.ma.emap.auditoria.model.Usuario;
import br.gov.ma.emap.auditoria.repository.PerfilRepository;
import br.gov.ma.emap.auditoria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final LdapService ldapService;
    private final UsuarioRepository repository;
    private final PerfilRepository perfilRepository;
    private final ClickHouseService clickHouseService;

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(String search, Pageable pageable) {
        Usuario filtro = Usuario.builder().login(search).nome(search).build();
        Example<Usuario> example = Example.of(filtro, ExampleMatcher.matchingAny().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        return repository.findAll(example, pageable).map(UsuarioResponse::from);
    }

    @Transactional
    public UsuarioResponse salvar(UsuarioRequest request) {

        if (request.login() == null || request.login().isBlank()) {
            throw new BusinessException("É necessário informar o login");
        }

        UsuarioAdDTO usuarioAd = ldapService.buscarUsuario(request.login()).orElseThrow(() ->
            new BusinessException("Usuário não encontrado no Active Directory")
        );

        if(repository.findByLogin(request.login()).isPresent()) {
            throw new BusinessException("Usuário já cadastrado no sistema");
        }

        long ecmUserId = clickHouseService.buscarEcmUserId(request.login());

        Perfil perfilDefault = buscarPerfil(TipoPerfil.DEFAULT);

        Usuario usuario = Usuario.builder()
                .login(request.login())
                .nome(usuarioAd.nome())
                .email(usuarioAd.email())
                .ativo(true)
                .perfis(new HashSet<>(Set.of(perfilDefault)))
                .ecmUserId(ecmUserId)
                .build();

        return UsuarioResponse.from(repository.save(usuario));
    }

    @Transactional
    public UsuarioResponse atualizar(Long usuarioId, UsuarioRequest request) {
        if (request.ativo() == null) {
            throw new BusinessException("É necessário informar o status do usuário");
        }
        Usuario usuario = buscarUsuario(usuarioId);
        usuario.setAtivo(request.ativo());//O dirty checking vai gerar o UPDATE no commit.
        return UsuarioResponse.from(usuario);
    }

    @Transactional
    public UsuarioResponse adicionarPerfil(Long usuarioId, TipoPerfil tipoPerfil) {
        Usuario usuario = buscarUsuario(usuarioId);
        Perfil perfil =  buscarPerfil(tipoPerfil);

        boolean adicionou = usuario.getPerfis().add(perfil);

        if (!adicionou) {
            throw new BusinessException("Usuário já possui esse perfil");
        }

        return UsuarioResponse.from(usuario);
    }

    @Transactional
    public void removerPerfil(Long usuarioId, TipoPerfil tipoPerfil) {
        Usuario usuario = buscarUsuario(usuarioId);

        if (usuario.getPerfis().size() == 1) {
            throw new BusinessException("O usuário deve possuir pelo menos um perfil");
        }

        boolean removeu = usuario.getPerfis().removeIf(perfil -> perfil.getNome() == tipoPerfil);

        if (!removeu) {
            throw new BusinessException("Usuário não possui o perfil informado");
        }
    }

    private Usuario buscarUsuario(Long usuarioId){
        return repository.findById(usuarioId).orElseThrow(() -> new BusinessException("Usuário não encontrado com ID: " + usuarioId));
    }

    public Perfil buscarPerfil(TipoPerfil tipoPerfil){
        return perfilRepository.findByNome(tipoPerfil).orElseThrow(() -> new BusinessException("Perfil não encontrado"));
    }

    public Usuario getUsuarioLogado() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return repository.findByLogin(login).orElseThrow(() ->new BusinessException("Usuário autenticado não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarAtivosPorPerfil(TipoPerfil perfil) {
        //return repository.findDistinctByAtivoTrueAndPerfisNome(perfil).stream().map(UsuarioResponse::from).toList();
        return repository.findAtivosByPerfilComPerfis(perfil).stream().map(UsuarioResponse::from).toList();
    }
}

