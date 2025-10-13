package com.gpis.marketplace_link.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed for one or more fields");
        problem.setTitle("Validation Error");

        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        Map<String, String> mappedErrors = new HashMap<>();
        for (FieldError error : fieldErrors) {
            mappedErrors.put(error.getField(), error.getDefaultMessage());
        }
        problem.setProperty("errors", mappedErrors);
        return problem;
    }
}
