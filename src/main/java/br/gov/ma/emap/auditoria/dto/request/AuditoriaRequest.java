package br.gov.ma.emap.auditoria.dto.request;

import br.gov.ma.emap.auditoria.dto.enums.CriterioAmostra;
import br.gov.ma.emap.auditoria.dto.enums.StatusAuditoria;
import br.gov.ma.emap.auditoria.model.Campanha;
import br.gov.ma.emap.auditoria.model.Usuario;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record AuditoriaRequest(
        Long checklistId,
        String processo,
        String contrato,
        String fornecedor,
        String classificacao,
        String diretoria,
        BigDecimal valor,
        LocalDate pagamento,
        String observacao
) {
}
