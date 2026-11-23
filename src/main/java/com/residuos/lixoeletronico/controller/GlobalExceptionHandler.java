package com.residuos.lixoeletronico.controller;

import com.residuos.lixoeletronico.dto.ApiErrorResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex
    ) {
        List<String> erros = ex
            .getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error ->
                error.getDefaultMessage() != null
                    ? error.getDefaultMessage()
                    : error.getField() + " inválido"
            )
            .distinct()
            .toList();

        String mensagem = erros.isEmpty()
            ? "Dados inválidos na requisição."
            : String.join("; ", erros);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), mensagem, erros)
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
        ResponseStatusException ex
    ) {
        int status = ex.getStatusCode().value();
        String mensagem = ex.getReason() != null && !ex.getReason().isBlank()
            ? ex.getReason()
            : "Erro na operação solicitada.";

        return ResponseEntity.status(ex.getStatusCode()).body(
            new ApiErrorResponse(status, mensagem, List.of())
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatchException(
        MethodArgumentTypeMismatchException ex
    ) {
        String mensagem =
            "Parâmetro inválido: " + ex.getName() + ".";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), mensagem, List.of())
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBodyException(
        HttpMessageNotReadableException ex
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Corpo da requisição inválido ou malformado.",
                List.of()
            )
        );
    }
}
