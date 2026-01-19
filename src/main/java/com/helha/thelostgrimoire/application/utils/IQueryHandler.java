package com.helha.thelostgrimoire.application.utils;

public interface IQueryHandler<I, O> {
    O handle( I input);
}
