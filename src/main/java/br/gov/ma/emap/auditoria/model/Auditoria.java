package br.gov.ma.emap.auditoria.model;

import br.gov.ma.emap.auditoria.dto.enums.CriterioAmostra;
import br.gov.ma.emap.auditoria.dto.enums.StatusAuditoria;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private Usuario createdBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "updated_by", nullable = false)
    private Usuario updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campanha_id", nullable = false)
    private Campanha campanha;

    @Column(name = "checklist_id")
    private Long checklistId;

    @Column(nullable = false, length = 100)
    private String processo;

    @Column(length = 100)
    private String contrato;

    @Column(length = 255)
    private String fornecedor;

    @Column(length = 1000)
    private String objeto;

    @Column(length = 100)
    private String classificacao;

    @Column(length = 100)
    private String departamento;

    @Column(length = 100)
    private String diretoria;

    @Column(precision = 15, scale = 2)
    private BigDecimal valor;

    private LocalDate pagamento;

    @Column(length = 100)
    @Enumerated(EnumType.STRING)
    private CriterioAmostra criterio;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private StatusAuditoria status;

    private OffsetDateTime inicio;

    private OffsetDateTime conclusao;
}