package br.gov.ma.emap.auditoria.controller;

import br.gov.ma.emap.auditoria.dto.enums.TipoPerfil;
import br.gov.ma.emap.auditoria.dto.request.UsuarioRequest;
import br.gov.ma.emap.auditoria.dto.response.UsuarioResponse;
import br.gov.ma.emap.auditoria.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService service;

    @GetMapping
    public ResponseEntity<Page<UsuarioResponse>> listar(@RequestParam String search, Pageable pageable) {
        return ResponseEntity.ok(service.listar(search, pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(@PathVariable Long id,@RequestBody UsuarioRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> salvar(@RequestBody @Valid UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(request));
    }

    @PostMapping("/{usuarioId}/perfil/{perfil}")
    public ResponseEntity<UsuarioResponse> adicionarPerfil(@PathVariable Long usuarioId, @PathVariable TipoPerfil perfil) {
        return ResponseEntity.ok(service.adicionarPerfil(usuarioId, perfil));
    }

    @DeleteMapping("/{usuarioId}/perfil/{perfil}")
    public ResponseEntity<Void> removerPerfil(@PathVariable Long usuarioId, @PathVariable TipoPerfil perfil) {
        service.removerPerfil(usuarioId, perfil);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/perfis")
    public ResponseEntity<TipoPerfil[]> listarPerfis() {
        return ResponseEntity.ok(TipoPerfil.values());
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<UsuarioResponse>> listarAtivosPorPerfil(@RequestParam TipoPerfil perfil) {
        return ResponseEntity.ok(service.listarAtivosPorPerfil(perfil));
    }

}
