package com.helha.thelostgrimoire.application.users.command.login;

import com.helha.thelostgrimoire.application.utils.ICommandHandler;
import com.helha.thelostgrimoire.application.utils.IEffectCommandHandler;
import com.helha.thelostgrimoire.domain.User;
import com.helha.thelostgrimoire.infrastructure.users.DbUsers;
import com.helha.thelostgrimoire.infrastructure.users.IUserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginHandler implements IEffectCommandHandler<LoginInput> {
    private IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginHandler(IUserRepository userRepository,
                        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void handle(LoginInput input) {
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

        // TODO JWT ici
    }
}
