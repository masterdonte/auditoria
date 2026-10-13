package br.gov.ma.emap.auditoria.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "auditoria_auditor")
public class AuditoriaAuditor {
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
    @JoinColumn(name = "auditoria_id", nullable = false, updatable = false)
    private Auditoria auditoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auditor_id", nullable = false, updatable = false)
    private Usuario auditor;

    private OffsetDateTime inicio;
    private OffsetDateTime fim;

    @Column(name = "motivo_encerramento", length = 255)
    private String motivoEncerramento;

    public static AuditoriaAuditor criar(Auditoria auditoria, Usuario auditor, Usuario logado) {
        return AuditoriaAuditor.builder()
                .auditoria(auditoria)
                .auditor(auditor)
                .inicio(OffsetDateTime.now())
                .createdBy(logado)
                .updatedBy(logado)
                .build();
    }
}
