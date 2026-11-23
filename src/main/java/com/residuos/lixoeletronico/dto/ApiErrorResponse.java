package com.residuos.lixoeletronico.dto;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
    int status,
    String message,
    List<String> errors,
    Instant timestamp
) {
    public ApiErrorResponse(int status, String message, List<String> errors) {
        this(status, message, errors != null ? errors : List.of(), Instant.now());
    }
}
