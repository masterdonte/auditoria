package br.gov.ma.emap.auditoria.exception;

public class BusinessException extends RuntimeException{

    public BusinessException(String msg) {
        super(msg);
    }

    public BusinessException() {
        super("Erro na regra de negocio");
    }

}
