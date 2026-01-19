package com.helha.thelostgrimoire.application.users;

import com.helha.thelostgrimoire.application.users.command.login.LoginHandler;
import org.springframework.stereotype.Service;

@Service
public class UsersCommandProcessor {
    public final LoginHandler loginHandler;

    public UsersCommandProcessor(LoginHandler loginHandler) {
        this.loginHandler = loginHandler;
    }
}