package com.helha.thelostgrimoire.controllers.users;

import com.helha.thelostgrimoire.application.repositories.users.command.UsersCommandProcessor;
import com.helha.thelostgrimoire.application.repositories.users.command.login.LoginInput;
import com.helha.thelostgrimoire.application.repositories.users.command.register.RegisterInput;
import com.helha.thelostgrimoire.application.repositories.users.command.register.RegisterOutput;
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

    /**
     * Dependency for user-related command processing (login, register, etc.)
     */
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
        /**
         * Validate credentials and generate a JWT token via the login handler
         */
        String token = usersCommandProcessor.loginHandler.handle(input);

        /**
         * Create a secure HTTP-only cookie to store the JWT, preventing XSS access
         */
        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(false) // Set to true in production with HTTPS
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
        /**
         * Persist a new user in the system using the register handler logic
         */
        RegisterOutput output = usersCommandProcessor.registerHandler.handle(input);

        /**
         * Generate the resource URI for the newly created user profile
         */
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

        /**
         * Clear the JWT cookie by returning an empty value with a maxAge of 0
         */
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