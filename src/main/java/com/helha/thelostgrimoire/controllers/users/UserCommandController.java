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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Duration;

@Tag(name = "Auth", description = "Authentication endpoints")
@RestController
@RequestMapping("/api/auth")
public class UserCommandController {

    private final UsersCommandProcessor usersCommandProcessor;

    public UserCommandController(UsersCommandProcessor usersCommandProcessor) {
        this.usersCommandProcessor = usersCommandProcessor;
    }

    @PostMapping("/login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    headers = @Header(
                            name = "Users Login",
                            description = "Users connected"
                    )
            ),
            @ApiResponse(responseCode = "400",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<Void> login(@Valid @RequestBody LoginInput input,
                                      HttpServletResponse response) {
       String token = usersCommandProcessor.loginHandler.handle(input);

        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofDays(1))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

       return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    headers = @Header(
                            name = "Users Register",
                            description = "Users created"
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


    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    headers = @Header(
                            name = "Users Logout",
                            description = "User logged out"
                    )
            ),
            @ApiResponse(responseCode = "404",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {

        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.noContent().build();
    }
}

