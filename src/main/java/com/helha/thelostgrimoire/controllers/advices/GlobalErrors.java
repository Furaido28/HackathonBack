package com.helha.thelostgrimoire.controllers.advices;

import org.springframework.beans.factory.parsing.Problem;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalErrors {
    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail badRequest(Exception exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Bad request");
        problemDetail.setDetail(exception.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail badRequest(MethodArgumentNotValidException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Bad request");
        Map errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage(),
                        (oldName, newName) -> oldName
                ));
        problemDetail.setProperty("errors",errors);
        return problemDetail;
    }

    //permet de gérer tous les cas non traité auparavant
    @ExceptionHandler(Exception.class)
    ProblemDetail internalServerError(Exception exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setDetail(exception.getMessage());
        return problemDetail;
    }
}
