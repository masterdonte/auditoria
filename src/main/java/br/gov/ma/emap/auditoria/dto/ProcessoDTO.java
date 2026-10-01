package br.gov.ma.emap.auditoria.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProcessoDTO(
        Long lanId,
        Long ecmId,
        String fornecedor,
        String processo,
        String contrato,
        String objeto,
        BigDecimal valorOriginal,
        LocalDateTime dataBaixa,
        String classificacao,
        String departamento,
        String diretoria
) {
}