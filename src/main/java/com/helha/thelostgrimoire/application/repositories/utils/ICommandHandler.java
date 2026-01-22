package com.helha.thelostgrimoire.application.repositories.utils;

public interface ICommandHandler<I, O> {
    O handle(I input);
}
