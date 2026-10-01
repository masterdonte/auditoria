package br.gov.ma.emap.auditoria.controller;

import br.gov.ma.emap.auditoria.dto.request.CampanhaRequest;
import br.gov.ma.emap.auditoria.dto.response.AuditoriaResponse;
import br.gov.ma.emap.auditoria.dto.response.CampanhaResponse;
import br.gov.ma.emap.auditoria.service.CampanhaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/campanha")
@RequiredArgsConstructor
public class CampanhaController {

    private final CampanhaService service;

    @PostMapping
    public ResponseEntity<CampanhaResponse> salvar(@RequestBody @Valid CampanhaRequest request ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(request));
    }

    @GetMapping
    public ResponseEntity<Page<CampanhaResponse>> listar(Pageable pageable) {
        Page<CampanhaResponse> result = service.listar(pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampanhaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping("/{campanhaId}/motor")
    public ResponseEntity<Page<AuditoriaResponse>> executarMotor(@PathVariable Long campanhaId, Pageable pageable) {
        return ResponseEntity.ok(service.executarMotor(campanhaId, pageable));
    }

    @PostMapping("/{campanhaId}/aprovar")
    public ResponseEntity<CampanhaResponse> aprovar(@PathVariable Long campanhaId) {
        return ResponseEntity.ok(service.aprovar(campanhaId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{campanhaId}/auditorias/exportar")
    public ResponseEntity<byte[]> exportarAuditorias(@PathVariable Long campanhaId) {
        byte[] arquivo = service.exportarExcel(campanhaId);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=auditorias.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(arquivo);
    }
}
