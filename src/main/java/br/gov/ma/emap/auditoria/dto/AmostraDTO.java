package br.gov.ma.emap.auditoria.dto;

import br.gov.ma.emap.auditoria.dto.enums.CriterioAmostra;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AmostraDTO(
        String processo,
        String contrato,
        String objeto,
        String fornecedor,
        String departamento,
        String diretoria,
        BigDecimal valor,
        LocalDate pagamento,
        String classificacao,
        CriterioAmostra criterio
) {
    public static AmostraDTO from(ProcessoDTO p, CriterioAmostra criterio) {
        return new AmostraDTO(p.processo(), p.contrato(), p.objeto(), p.fornecedor(),
                p.departamento(), p.diretoria(), p.valorOriginal(),
                p.dataBaixa().toLocalDate(), p.classificacao(), criterio);
    }

    public static AmostraDTO materialidade(ProcessoDTO processo) {
        return from(processo, CriterioAmostra.MATERIALIDADE);
    }

    public static AmostraDTO temporalidade(ProcessoDTO processo) {
        return from(processo, CriterioAmostra.TEMPORALIDADE);
    }

    public static AmostraDTO aleatoriedade(ProcessoDTO processo) {
        return from(processo, CriterioAmostra.ALEATORIEDADE);
    }

}