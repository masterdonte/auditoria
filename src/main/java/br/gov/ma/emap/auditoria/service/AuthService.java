package br.gov.ma.emap.auditoria.service;

import br.gov.ma.emap.auditoria.config.JwtService;
import br.gov.ma.emap.auditoria.dto.request.AuthRequest;
import br.gov.ma.emap.auditoria.dto.response.LoginResponse;
import br.gov.ma.emap.auditoria.exception.BusinessException;
import br.gov.ma.emap.auditoria.model.Usuario;
import br.gov.ma.emap.auditoria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public LoginResponse login(AuthRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.usuario(), request.senha())
        );

        Usuario usuario = usuarioRepository.findByLogin(authentication.getName()).orElseThrow(() ->
                new BusinessException("Usuário não possui acesso ao sistema")
        );

        if (!usuario.isEnabled()) {
            throw new DisabledException("Usuário desativado");
        }

        String token = jwtService.gerarToken(usuario);

        return LoginResponse.from(usuario, token);
    }

}