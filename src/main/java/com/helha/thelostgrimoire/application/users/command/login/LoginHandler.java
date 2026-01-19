package com.helha.thelostgrimoire.application.users.command.login;

import com.helha.thelostgrimoire.application.users.UserMapper;
import com.helha.thelostgrimoire.application.utils.ICommandHandler;
import com.helha.thelostgrimoire.application.utils.IEffectCommandHandler;
import com.helha.thelostgrimoire.domain.Users;
import com.helha.thelostgrimoire.infrastructure.users.DbUsers;
import com.helha.thelostgrimoire.infrastructure.users.IUserRepository;
import com.helha.thelostgrimoire.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LoginHandler implements ICommandHandler<LoginInput, String> {
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService  jwtService;

    public LoginHandler(IUserRepository userRepository,
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
