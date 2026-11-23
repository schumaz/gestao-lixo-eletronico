package com.residuos.lixoeletronico.client;

import java.util.List;

public class ApiClientException extends RuntimeException {

    private final int statusCode;
    private final List<String> details;

    public ApiClientException(int statusCode, String message) {
        this(statusCode, message, List.of(), null);
    }

    public ApiClientException(int statusCode, String message, Throwable cause) {
        this(statusCode, message, List.of(), cause);
    }

    public ApiClientException(
        int statusCode,
        String message,
        List<String> details,
        Throwable cause
    ) {
        super(message, cause);
        this.statusCode = statusCode;
        this.details = details != null ? List.copyOf(details) : List.of();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public List<String> getDetails() {
        return details;
    }
}
