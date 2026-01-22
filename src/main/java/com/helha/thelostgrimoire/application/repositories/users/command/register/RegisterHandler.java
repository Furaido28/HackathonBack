package com.helha.thelostgrimoire.application.repositories.users.command.register;

import com.helha.thelostgrimoire.application.repositories.utils.ICommandHandler;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.directories.IDirectoriesRepository;
import com.helha.thelostgrimoire.infrastructure.users.DbUsers;
import com.helha.thelostgrimoire.infrastructure.users.IUsersRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RegisterHandler implements ICommandHandler<RegisterInput, RegisterOutput> {

    private final IUsersRepository repository;
    private final IDirectoriesRepository directoriesRepository;
    private final PasswordEncoder encoder;

    public RegisterHandler(IUsersRepository repository,
                           IDirectoriesRepository directoriesRepository,
                           PasswordEncoder encoder) {
        this.repository = repository;
        this.directoriesRepository = directoriesRepository;
        this.encoder = encoder;
    }

    @Override
    @Transactional // Ensures atomicity: if directory creation fails, the user creation is rolled back.
    public RegisterOutput handle(RegisterInput input) {

        // Perform initial syntax and presence checks on input data.
        validate(input);

        // Conflict check: Ensure the email address is not already registered in the system.
        if (repository.findByEmailAddress(input.email).isPresent()) {
            throw new IllegalStateException("Email already in use");
        }

        // 1. User Creation
        // Initialize the new user entity with a hashed password and normalized email address.
        DbUsers newUser = new DbUsers();
        newUser.name = input.name;
        newUser.firstname = input.firstName;
        newUser.emailAddress = input.email.toLowerCase();
        newUser.hashPassword = encoder.encode(input.password);
        newUser.createdAt = LocalDateTime.now();

        // Save the user to generate the primary key (ID) needed for the directory relationship.
        DbUsers savedUser = repository.save(newUser);

        // 2. AUTOMATIC ROOT DIRECTORY CREATION
        // Every new user must have a root directory to store their personal notes.
        DbDirectories rootDir = new DbDirectories();
        rootDir.name = "root";
        rootDir.userId = savedUser.id;
        rootDir.parentDirectoryId = null;  // Root directory has no parent.
        rootDir.isRoot = true;             // Mark as the entry point for the user's file system.
        rootDir.createdAt = LocalDateTime.now();

        directoriesRepository.save(rootDir);

        // 3. Prepare the response DTO
        RegisterOutput output = new RegisterOutput();
        output.id = savedUser.id;
        output.email = savedUser.emailAddress;

        return output;
    }

    /**
     * Internal validation logic to ensure data integrity before persistence.
     */
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