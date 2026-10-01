package br.gov.ma.emap.auditoria.model;

import br.gov.ma.emap.auditoria.dto.request.ParametrizacaoRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "parametrizacao")
public class Parametrizacao {

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

    @Column(nullable = false, length = 50)
    private String versao;

    private BigDecimal percDiretoria;
    private BigDecimal percMaterial;
    private BigDecimal percAleatorio;
    private BigDecimal percTemporal;
    private Integer recorrenciaMaterial;
    private Integer tempoSemAuditoria;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Column(nullable = false)
    private Boolean ativo = true;
}