package com.helha.thelostgrimoire.application.utils;

public interface ICommandHandler<I, O> {
    O handle(I input);
}
