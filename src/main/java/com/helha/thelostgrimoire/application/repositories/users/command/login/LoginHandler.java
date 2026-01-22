package com.helha.thelostgrimoire.application.repositories.users.command.login;

import com.helha.thelostgrimoire.application.repositories.users.UserMapper;
import com.helha.thelostgrimoire.application.repositories.utils.ICommandHandler;
import com.helha.thelostgrimoire.domain.models.Users;
import com.helha.thelostgrimoire.infrastructure.users.DbUsers;
import com.helha.thelostgrimoire.infrastructure.users.IUsersRepository;
import com.helha.thelostgrimoire.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginHandler implements ICommandHandler<LoginInput, String> {
    private final IUsersRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService  jwtService;

    public LoginHandler(IUsersRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public String handle(LoginInput input) {
        DbUsers user = userRepository
                .findByEmailAddress(input.email)
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));

        boolean passwordOk = passwordEncoder.matches(
                input.password,
                user.hashPassword
        );

        if (!passwordOk) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        Users u = UserMapper.toDomain(user);

        return jwtService.generateToken(u);
    }
}
