package com.helha.thelostgrimoire.controllers.users.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class UserNotFound extends ErrorResponseException {
    public UserNotFound(long id) {
        super(
                HttpStatus.NOT_FOUND,
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_FOUND,
                        "User with %d is not found".formatted(id)
                ),
                null
        );
    }
}
