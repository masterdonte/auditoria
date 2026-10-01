package br.gov.ma.emap.auditoria.controller;

import br.gov.ma.emap.auditoria.dto.ProcessoDTO;
import br.gov.ma.emap.auditoria.dto.request.AuditoriaRequest;
import br.gov.ma.emap.auditoria.dto.response.AuditoriaResponse;
import br.gov.ma.emap.auditoria.model.AuditoriaLegado;
import br.gov.ma.emap.auditoria.service.AuditoriaService;
import br.gov.ma.emap.auditoria.service.ClickHouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final ClickHouseService clickHouseService;
    private final AuditoriaService service;

    @GetMapping("/campanha/{campanhaId}")
    public ResponseEntity<Page<AuditoriaResponse>> listar(@PathVariable Long campanhaId, Pageable pageable) {
        return ResponseEntity.ok(service.listar(campanhaId, pageable));
    }

    @GetMapping("/buscarProcesso")
    public ResponseEntity<List<ProcessoDTO>> buscarProcesso(@RequestParam String processo) {
        return ResponseEntity.ok(clickHouseService.buscarProcesso(processo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuditoriaResponse> substituir(@PathVariable Long id, @RequestBody @Valid AuditoriaRequest request) {
        return ResponseEntity.ok(service.substituir(id, request));
    }

    @GetMapping("/legados")
    public ResponseEntity<List<AuditoriaLegado>> legados(@RequestParam Integer meses) {
        return ResponseEntity.ok(service.legados(meses));
    }

    @GetMapping("/cubo")
    public ResponseEntity<List<ProcessoDTO>> cubo(@RequestParam YearMonth ref) {
        List<ProcessoDTO> processos = clickHouseService.buscarDadosCubo(ref);
        return ResponseEntity.ok(processos);
    }

}
