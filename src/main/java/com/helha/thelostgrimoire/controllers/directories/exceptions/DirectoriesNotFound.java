package com.helha.thelostgrimoire.controllers.directories.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class DirectoriesNotFound extends ErrorResponseException {
    public DirectoriesNotFound(long id) {
        super(
                HttpStatus.NOT_FOUND,
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_FOUND,
                        "Directory with %d is not found".formatted(id)
                ),
                null
        );
    }
}