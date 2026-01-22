package com.helha.thelostgrimoire.application.repositories.users.command.login;

public class LoginInput {
    public String email;
    public String password;

    public LoginInput(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
