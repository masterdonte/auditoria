package br.gov.ma.emap.auditoria.controller;

import br.gov.ma.emap.auditoria.dto.request.ParametrizacaoRequest;
import br.gov.ma.emap.auditoria.dto.response.ParametrizacaoResponse;
import br.gov.ma.emap.auditoria.model.Parametrizacao;
import br.gov.ma.emap.auditoria.service.ParametrizacaoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/parametrizacao")
public class ParametrizacaoController {

    private final ParametrizacaoService service;

    public ParametrizacaoController(ParametrizacaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ParametrizacaoResponse> salvar(@RequestBody @Valid ParametrizacaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(request));
    }

    @GetMapping("/ativa")
    public ResponseEntity<ParametrizacaoResponse> buscarAtiva() {
        ParametrizacaoResponse parametrizacao = service.buscarAtiva();
        return ResponseEntity.ok(parametrizacao);
    }

    @GetMapping
    public ResponseEntity<Page<ParametrizacaoResponse>> listar(Pageable pageable) {
        Page<ParametrizacaoResponse> result = service.listar(pageable);
        return ResponseEntity.ok(result);
    }
}