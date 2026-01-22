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
import org.springframework.web.server.ResponseStatusException; // <--- Import ajouté

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
        // 1. Chercher l'utilisateur (ou échouer avec 401)
        DbUsers user = userRepository
                .findByEmailAddress(input.email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect"
                ));

        // 2. Vérifier le mot de passe
        boolean passwordOk = passwordEncoder.matches(
                input.password,
                user.hashPassword
        );

        if (!passwordOk) {
            // 3. Si échec, lancer une 401 explicite
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect"
            );
        }

        // 4. Générer le token
        Users u = UserMapper.toDomain(user);
        return jwtService.generateToken(u);
    }
}