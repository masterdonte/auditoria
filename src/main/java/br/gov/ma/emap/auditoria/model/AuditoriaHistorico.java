package br.gov.ma.emap.auditoria.model;

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
@Table(name = "auditoria_historico")
public class AuditoriaHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private Usuario createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    private Long auditoriaOriId;

    private Long auditoriaDesId;

    @Column(length = 100)
    private String processo;

    @Column(length = 100)
    private String contrato;

    @Column(length = 255)
    private String fornecedor;

    @Column(length = 100)
    private String classificacao;

    @Column(length = 100)
    private String diretoria;

    @Column(precision = 15, scale = 2)
    private BigDecimal valor;

    private LocalDate pagamento;

    @Column(name = "motivo", columnDefinition = "TEXT", nullable = false)
    private String motivo;
}