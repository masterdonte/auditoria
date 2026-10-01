package br.gov.ma.emap.auditoria.controller;

import br.gov.ma.emap.auditoria.dto.request.AuthRequest;
import br.gov.ma.emap.auditoria.dto.response.LoginResponse;
import br.gov.ma.emap.auditoria.service.AuthService;
import br.gov.ma.emap.auditoria.service.ClickHouseService;
import br.gov.ma.emap.auditoria.service.LdapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;
    private final ClickHouseService clickService;
    private final LdapService ldapService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid AuthRequest credential) {
        return ResponseEntity.ok(service.login(credential));
    }

    @GetMapping("/version")
    public ResponseEntity<?> version() {
        return ResponseEntity.ok(clickService.version());
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String login) {
        return ResponseEntity.ok(ldapService.search(login));
    }

}
