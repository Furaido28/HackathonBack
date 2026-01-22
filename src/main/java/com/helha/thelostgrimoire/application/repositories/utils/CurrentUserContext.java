package com.helha.thelostgrimoire.application.repositories.utils;

public final class CurrentUserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    private CurrentUserContext() { }

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        Long id = USER_ID.get();

        if (id == null) {
            throw new RuntimeException("No authenticated user in context");
        }
        return id;
    }

    public static void clear() {
        USER_ID.remove();
    }
}
