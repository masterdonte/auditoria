package br.gov.ma.emap.auditoria.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "auditoria_legado")
public class AuditoriaLegado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String processo;
    private String contrato;
    private String fornecedor;
    private String setor;
    private String diretoria;
    private Integer referencia; // Ex: 202601
    private OffsetDateTime createdAt;
}