package com.helha.thelostgrimoire.application.repositories.utils;

public interface IEffectCommandHandler<I> {
    void handle(I input);
}
