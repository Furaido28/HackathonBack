package com.helha.thelostgrimoire.application.repositories.users.command.login;

import com.helha.thelostgrimoire.application.repositories.users.UserMapper;
import com.helha.thelostgrimoire.application.repositories.utils.ICommandHandler;
import com.helha.thelostgrimoire.domain.models.Users;
import com.helha.thelostgrimoire.infrastructure.users.DbUsers;
import com.helha.thelostgrimoire.infrastructure.users.IUsersRepository;
import com.helha.thelostgrimoire.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LoginHandler implements ICommandHandler<LoginInput, String> {
    private final IUsersRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginHandler(IUsersRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public String handle(LoginInput input) {
        // 1. User lookup
        // Attempt to find the user by their email address.
        // Throw 401 Unauthorized if not found, using a generic message for security (obfuscation).
        DbUsers user = userRepository
                .findByEmailAddress(input.email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect"
                ));

        // 2. Password validation
        // Compare the raw password from the input with the hashed password stored in the database.
        boolean passwordOk = passwordEncoder.matches(
                input.password,
                user.hashPassword
        );

        if (!passwordOk) {
            // 3. Security failure
            // If the password hash does not match, throw 401 Unauthorized.
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect"
            );
        }

        // 4. Token generation
        // Map the database entity to the domain model and generate a signed JWT for the user session.
        Users u = UserMapper.toDomain(user);
        return jwtService.generateToken(u);
    }
}