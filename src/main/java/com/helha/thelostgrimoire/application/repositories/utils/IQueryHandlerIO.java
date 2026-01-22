package com.helha.thelostgrimoire.application.repositories.utils;

public interface IQueryHandlerIO<I, O> {
    O handle(I request);
}
