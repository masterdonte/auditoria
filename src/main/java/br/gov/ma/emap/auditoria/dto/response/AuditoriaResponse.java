package br.gov.ma.emap.auditoria.dto.response;

import br.gov.ma.emap.auditoria.dto.enums.CriterioAmostra;
import br.gov.ma.emap.auditoria.dto.enums.StatusAuditoria;
import br.gov.ma.emap.auditoria.model.Auditoria;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record AuditoriaResponse(
        Long id,
        Long campanhaId,
        Long checklistId,
        String processo,
        String contrato,
        String objeto,
        String fornecedor,
        String classificacao,
        String departamento,
        String diretoria,
        BigDecimal valor,
        LocalDate pagamento,
        CriterioAmostra criterio,
        StatusAuditoria status,
        OffsetDateTime inicio,
        OffsetDateTime conclusao
) {

    public static AuditoriaResponse from(Auditoria auditoria) {
        return new AuditoriaResponse(
                auditoria.getId(),
                auditoria.getCampanha().getId(),
                auditoria.getChecklistId(),
                auditoria.getProcesso(),
                auditoria.getContrato(),
                auditoria.getObjeto(),
                auditoria.getFornecedor(),
                auditoria.getClassificacao(),
                auditoria.getDepartamento(),
                auditoria.getDiretoria(),
                auditoria.getValor(),
                auditoria.getPagamento(),
                auditoria.getCriterio(),
                auditoria.getStatus(),
                auditoria.getInicio(),
                auditoria.getConclusao()
        );
    }
}