package com.gpis.marketplace_link.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MinimalValidationAdvice {
    private static final boolean FIRST_ONLY = true;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleDTOValidation(MethodArgumentNotValidException ex) {
        Map<String, List<String>> grouped = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        LinkedHashMap::new,
                        Collectors.mapping(DefaultMessageSourceResolvable::getDefaultMessage, Collectors.toList())
                ));

        return buildBody(HttpStatus.BAD_REQUEST, "Error de validación", grouped);
    }

    @SuppressWarnings("removal")
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> handleMethodValidation(HandlerMethodValidationException ex) {
        Map<String, List<String>> errors = new LinkedHashMap<>();
        ex.getAllValidationResults().forEach(pvr -> {
            String name = Optional.ofNullable(pvr.getMethodParameter().getParameterName())
                    .orElse("arg" + pvr.getMethodParameter().getParameterIndex());
            pvr.getResolvableErrors().forEach(err -> {
                String msg = Optional.ofNullable(err.getDefaultMessage()).orElse(err.toString());
                errors.computeIfAbsent(name, k -> new ArrayList<>()).add(msg);
            });
        });
        return buildBody(HttpStatus.BAD_REQUEST, "Parámetros inválidos en la solicitud", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, List<String>> errors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(cv -> {
            String path = cv.getPropertyPath().toString();
            errors.computeIfAbsent(path, k -> new ArrayList<>()).add(cv.getMessage());
        });
        return buildBody(HttpStatus.BAD_REQUEST, "Parámetros inválidos en la solicitud", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJsonInvalid(HttpMessageNotReadableException ex) {
        Map<String, List<String>> errors = new LinkedHashMap<>();
        if (ex.getCause() instanceof JsonMappingException jme) {
            String path = jme.getPath().stream()
                    .map(ref -> Objects.toString(ref.getFieldName(), String.valueOf(ref.getIndex())))
                    .collect(Collectors.joining("."));
            if (path.isBlank()) path = "$";
            String msg = Optional.ofNullable(jme.getOriginalMessage())
                    .orElse("Cuerpo JSON inválido o no legible");
            errors.put(path, List.of(msg));
        } else {
            errors.put("$", List.of(Optional.ofNullable(ex.getMostSpecificCause())
                    .map(Throwable::getMessage)
                    .orElse("Cuerpo JSON inválido o no legible")));
        }
        return buildBody(HttpStatus.BAD_REQUEST, "JSON inválido o no legible", errors);
    }

    // ---------- helpers ----------

    private ResponseEntity<Map<String, Object>> buildBody(HttpStatus status, String message, Map<String, List<String>> grouped) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("message", message);
        if (FIRST_ONLY) {
            Map<String, String> compact = new LinkedHashMap<>();
            grouped.forEach((k, v) -> compact.put(k, v.isEmpty() ? "" : v.get(0)));
            body.put("errors", compact);
        } else {
            body.put("errors", grouped);
        }
        return ResponseEntity.status(status).body(body);
    }
}
