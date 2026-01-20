package com.helha.thelostgrimoire.application.utils;

public interface IQueryHandlerIO<I, O> {
    O handle(I request);
}
