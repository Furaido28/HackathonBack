package com.helha.thelostgrimoire.application.repositories.users.command;

import com.helha.thelostgrimoire.application.repositories.users.command.login.LoginHandler;
import com.helha.thelostgrimoire.application.repositories.users.command.register.RegisterHandler;
import org.springframework.stereotype.Service;

@Service
public class UsersCommandProcessor {
    public final LoginHandler loginHandler;
    public final RegisterHandler registerHandler;

    public UsersCommandProcessor(LoginHandler loginHandler, RegisterHandler registerHandler) {
        this.loginHandler = loginHandler;
        this.registerHandler = registerHandler;
    }
}