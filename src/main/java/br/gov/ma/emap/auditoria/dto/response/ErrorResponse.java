package br.gov.ma.emap.auditoria.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.List;

public record ErrorResponse(
        List<String> errors,
        int status,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        Date timestamp
) {

    public ErrorResponse(List<String> errors, int status) {
        this(errors, status, new Date());
    }

    public ErrorResponse(String error, int status) {
        this(List.of(error), status, new Date());
    }
}


