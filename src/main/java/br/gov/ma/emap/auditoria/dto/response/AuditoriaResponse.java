package br.gov.ma.emap.auditoria.dto.response;

import br.gov.ma.emap.auditoria.dto.enums.CriterioAmostra;
import br.gov.ma.emap.auditoria.dto.enums.StatusAuditoria;
import br.gov.ma.emap.auditoria.model.Auditoria;
import br.gov.ma.emap.auditoria.model.AuditoriaAuditor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

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
        OffsetDateTime conclusao,
        List<AuditorResumoResponse> auditores
) {

    public static AuditoriaResponse from(Auditoria auditoria, List<AuditoriaAuditor> vinculos) {

        List<AuditorResumoResponse> auditores = vinculos.stream()
                .map(vinculo -> new AuditorResumoResponse(vinculo.getAuditor().getId(),vinculo.getAuditor().getNome()))
                .toList();

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
                auditoria.getConclusao(),
                auditores
        );
    }
}