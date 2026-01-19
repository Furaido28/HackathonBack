package com.helha.thelostgrimoire.controllers.users;

import com.helha.thelostgrimoire.application.users.UsersCommandProcessor;
import com.helha.thelostgrimoire.application.users.command.login.LoginHandler;
import com.helha.thelostgrimoire.application.users.command.login.LoginInput;
import com.helha.thelostgrimoire.application.users.command.register.RegisterInput;
import com.helha.thelostgrimoire.domain.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
public class UserCommandController {

    private final UsersCommandProcessor usersCommandProcessor;

    public UserCommandController(UsersCommandProcessor usersCommandProcessor) {
        this.usersCommandProcessor = usersCommandProcessor;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginInput input) {

       usersCommandProcessor.loginHandler.handle(input);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterInput input) {
        usersCommandProcessor.registerHandler.handle(input);

        return ResponseEntity.ok().build();
    }
}

