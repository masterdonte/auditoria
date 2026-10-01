package br.gov.ma.emap.auditoria.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "campanha")
public class Campanha {

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

    @Column(nullable = false)
    private Integer competencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parametrizacao_id", nullable = false)
    private Parametrizacao parametrizacao;

    @Column(name = "execucao_motor")
    private OffsetDateTime execucaoMotor;

    @Column(name = "qtd_universo")
    private Integer qtdUniverso;

    @Column(name = "qtd_amostra")
    private Integer qtdAmostra;

    private OffsetDateTime aprovacao;

    public void aprovar(Usuario usuario) {
        setAprovacao(OffsetDateTime.now(ZoneId.of("America/Fortaleza")));
        setUpdatedBy(usuario);
    }
}