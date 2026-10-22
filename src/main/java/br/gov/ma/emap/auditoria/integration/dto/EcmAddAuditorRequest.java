package br.gov.ma.emap.auditoria.integration.dto;

import java.time.LocalDate;
import java.util.List;

public record EcmAddAuditorRequest(
        String oper,
        Long cod_audit,
        Long user_id,
        LocalDate dat_prazo,
        List<Long> cod_documento
) {
}