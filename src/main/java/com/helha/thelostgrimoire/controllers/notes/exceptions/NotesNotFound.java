package com.helha.thelostgrimoire.controllers.notes.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class NotesNotFound extends ErrorResponseException {
    public NotesNotFound(long id) {
        super(
                HttpStatus.NOT_FOUND,
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_FOUND,
                        "Note with %d is not found".formatted(id)
                ),
                null
        );
    }
}
