package br.gov.ma.emap.auditoria.integration.dto;

import java.time.LocalDate;
import java.util.List;

public record EcmAddAuditoriaRequest(
            String oper,
            String audit_titulo,
            String audit_descricao,
            LocalDate dat_inicio,
            LocalDate dat_fim,
            List<EcmAuditorRequest> auditores
    ) {
}
