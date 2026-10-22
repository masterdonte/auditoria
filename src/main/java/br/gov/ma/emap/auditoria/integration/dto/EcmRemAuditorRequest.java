package br.gov.ma.emap.auditoria.integration.dto;

import java.util.List;

public record EcmRemAuditorRequest(
        String oper,
        Long cod_audit,
        Long user_id,
        List<Long> cod_documento
) {
}
