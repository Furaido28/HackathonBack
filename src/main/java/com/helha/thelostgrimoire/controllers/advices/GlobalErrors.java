package com.helha.thelostgrimoire.controllers.advices;

import com.helha.thelostgrimoire.controllers.directories.exceptions.DirectoriesNotFound;
import com.helha.thelostgrimoire.controllers.notes.exceptions.NotesNotFound;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

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

        Map<String, String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage(),
                        (oldMsg, newMsg) -> oldMsg
                ));

        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }

    // laisse passer les statuts (403/404/409/...)
    @ExceptionHandler(ResponseStatusException.class)
    ProblemDetail responseStatus(ResponseStatusException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(exception.getStatusCode());
        problemDetail.setTitle(exception.getStatusCode().toString());
        problemDetail.setDetail(exception.getReason() != null ? exception.getReason() : exception.getMessage());
        return problemDetail;
    }

    // permet de gérer tous les cas non traité auparavant
    @ExceptionHandler(Exception.class)
    ProblemDetail internalServerError(Exception exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setDetail(exception.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(NotesNotFound.class)
    public ProblemDetail handleNotesNotFound(NotesNotFound ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("Not Found");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(DirectoriesNotFound.class)
    public ProblemDetail handleDirNotFound(DirectoriesNotFound ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("Not Found");
        pd.setDetail(ex.getMessage());
        return pd;
    }
}