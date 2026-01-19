
package com.helha.thelostgrimoire.application.users.command.register;

import com.helha.thelostgrimoire.application.utils.ICommandHandler;
import com.helha.thelostgrimoire.infrastructure.users.DbUsers;
import com.helha.thelostgrimoire.infrastructure.users.IUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class RegisterHandler implements ICommandHandler<RegisterInput, Long> {

    private final IUserRepository repository;
    private final PasswordEncoder encoder;

    public RegisterHandler(IUserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public Long handle(RegisterInput input) {

        validate(input);

        // Vérifier que l'email n'existe pas déjà
        if (repository.findByEmailAddress(input.email).isPresent()) {
            throw new IllegalStateException("Email already in use");
        }

        // Créer l'entité JPA
        DbUsers newUser = new DbUsers();
        newUser.name = input.name;
        newUser.firstname = input.firstName;
        newUser.emailAddress = input.email.toLowerCase();
        newUser.hashPassword = encoder.encode(input.password);
        newUser.createdAt = LocalDateTime.now();

        // Save → retourne un DbUsers avec id généré
        DbUsers saved = repository.save(newUser);

        return saved.id;
    }

    private void validate(RegisterInput input) {
        if (input == null) throw new IllegalArgumentException("Input is required");

        if (isBlank(input.name)) throw new IllegalArgumentException("Name is required");
        if (isBlank(input.firstName)) throw new IllegalArgumentException("First name is required");

        if (isBlank(input.email)) throw new IllegalArgumentException("Email is required");
        if (!input.email.contains("@")) throw new IllegalArgumentException("Invalid email");

        if (isBlank(input.password)) throw new IllegalArgumentException("Password is required");
        if (input.password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
