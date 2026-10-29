package br.gov.ma.emap.auditoria.controller;

import br.gov.ma.emap.auditoria.dto.ProcessoDTO;
import br.gov.ma.emap.auditoria.dto.request.AuthRequest;
import br.gov.ma.emap.auditoria.dto.response.LoginResponse;
import br.gov.ma.emap.auditoria.service.AuthService;
import br.gov.ma.emap.auditoria.service.ClickHouseService;
import br.gov.ma.emap.auditoria.service.EcmService;
import br.gov.ma.emap.auditoria.service.LdapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class AuthController {

    private final AuthService service;
    private final ClickHouseService clickService;

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid AuthRequest credential) {
        return ResponseEntity.ok(service.login(credential));
    }

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        return ResponseEntity.ok(clickService.version());
    }

    /*
    @GetMapping("/version")
    public ResponseEntity<?> version() {
        return ResponseEntity.ok(clickService.version());
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String login) {
        return ResponseEntity.ok(ldapService.search(login));
    }

    @GetMapping("/cubo")
    public ResponseEntity<List<ProcessoDTO>> cubo(@RequestParam YearMonth ref) {
        List<ProcessoDTO> processos = clickService.buscarDadosCubo(ref);
        return ResponseEntity.ok(processos);
    }

    @GetMapping("/ecmUser")
    public ResponseEntity<?> buscarEcmUserByLogin(@RequestParam String login) {
        return ResponseEntity.ok(clickService.buscarEcmUserId(login));
    }

    @GetMapping("/teste")
    public ResponseEntity<?> buscaAuditoriaEcm(@RequestParam String titulo) {
        return ResponseEntity.ok(ecmService.buscarAuditoria(titulo));
    }
    */

}
