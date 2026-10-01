package br.gov.ma.emap.auditoria.dto.request;

import br.gov.ma.emap.auditoria.model.Parametrizacao;
import br.gov.ma.emap.auditoria.model.Usuario;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ParametrizacaoRequest(

        @NotBlank(message = "A versão é obrigatória")
        String versao,

        @NotNull(message = "O percentual por diretoria é obrigatório")
        BigDecimal percDiretoria,

        @NotNull(message = "O percentual de materialidade é obrigatório")
        BigDecimal percMaterial,

        @NotNull(message = "O percentual aleatório é obrigatório")
        BigDecimal percAleatorio,

        @NotNull(message = "O percentual temporal é obrigatório")
        BigDecimal percTemporal,

        @NotNull(message = "A recorrência de materialidade é obrigatória")
        Integer recorrenciaMaterial,

        @NotNull(message = "O tempo sem auditoria é obrigatório")
        Integer tempoSemAuditoria,

        String observacao
) {
    public Parametrizacao toEntity(Usuario usuario) {
        return Parametrizacao.builder()
                .versao(versao)
                .percDiretoria(percDiretoria)
                .percMaterial(percMaterial)
                .percAleatorio(percAleatorio)
                .percTemporal(percTemporal)
                .recorrenciaMaterial(recorrenciaMaterial)
                .tempoSemAuditoria(tempoSemAuditoria)
                .observacao(observacao)
                .ativo(true)
                .createdBy(usuario)
                .updatedBy(usuario)
                .build();
    }

    @AssertTrue(message = "A soma dos percentuais de materialidade, aleatoriedade e temporalidade deve ser 100")
    public boolean isPercentuaisValidos() {
        // A validação de nulidade é responsabilidade do @NotNull.
        // Aqui validamos apenas a soma quando todos os percentuais estão presentes.
        if (percMaterial == null || percAleatorio == null || percTemporal == null) {
            return true;
        }

        return percMaterial.add(percAleatorio).add(percTemporal).compareTo(BigDecimal.valueOf(100)) == 0;
    }

}