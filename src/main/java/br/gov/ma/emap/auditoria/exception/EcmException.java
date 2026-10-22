package br.gov.ma.emap.auditoria.exception;

public class EcmException extends RuntimeException {

    public EcmException(String message) {
        super("ECM: " + message);
    }

    public EcmException(String message, Throwable cause) {
        super("ECM: " + message, cause);
    }
}