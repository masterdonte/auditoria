package br.gov.ma.emap.auditoria.dto.enums;

public enum StatusAuditoria {

    ABERTA,
    EM_ANALISE,
    EM_REGULARIZACAO,
    CONFORME,
    NAO_CONFORME;

    public boolean transicao(StatusAuditoria novoStatus) {
        return switch (this) {
            case ABERTA -> novoStatus == EM_ANALISE;
            case EM_ANALISE -> novoStatus == CONFORME || novoStatus == EM_REGULARIZACAO;
            case EM_REGULARIZACAO -> novoStatus == NAO_CONFORME;
            case CONFORME, NAO_CONFORME -> false;
        };
    }
}