package com.helha.thelostgrimoire.controllers.users;

import com.helha.thelostgrimoire.application.users.command.UsersCommandProcessor;
import com.helha.thelostgrimoire.application.users.command.login.LoginInput;
import com.helha.thelostgrimoire.application.users.command.register.RegisterInput;
import com.helha.thelostgrimoire.application.users.command.register.RegisterOutput;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    headers = @Header(
                            name = "User Register",
                            description = "User created"
                    )
            ),
            @ApiResponse(responseCode = "400",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<RegisterOutput> register(@Valid @RequestBody RegisterInput input) {
        RegisterOutput output = usersCommandProcessor.registerHandler.handle(input);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(output.id)
                .toUri();

        return ResponseEntity
                .created(location)
                .body(output);
    }
}

