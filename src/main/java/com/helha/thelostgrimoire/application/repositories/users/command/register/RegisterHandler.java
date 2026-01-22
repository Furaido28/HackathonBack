package com.helha.thelostgrimoire.application.repositories.users.command.register;

import com.helha.thelostgrimoire.application.repositories.utils.ICommandHandler;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories; // <--- Import
import com.helha.thelostgrimoire.infrastructure.directories.IDirectoriesRepository; // <--- Import
import com.helha.thelostgrimoire.infrastructure.users.DbUsers;
import com.helha.thelostgrimoire.infrastructure.users.IUsersRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RegisterHandler implements ICommandHandler<RegisterInput, RegisterOutput> {

    private final IUsersRepository repository;
    private final IDirectoriesRepository directoriesRepository; // <--- Nouvelle dépendance
    private final PasswordEncoder encoder;

    // Mise à jour du constructeur pour injecter le repo des dossiers
    public RegisterHandler(IUsersRepository repository,
                           IDirectoriesRepository directoriesRepository,
                           PasswordEncoder encoder) {
        this.repository = repository;
        this.directoriesRepository = directoriesRepository;
        this.encoder = encoder;
    }

    @Override
    @Transactional // Très important ici : si la création du dossier échoue, le user est annulé (Rollback)
    public RegisterOutput handle(RegisterInput input) {

        validate(input);

        // Vérifier que l'email n'existe pas déjà
        if (repository.findByEmailAddress(input.email).isPresent()) {
            throw new IllegalStateException("Email already in use");
        }

        // 1. Créer l'utilisateur
        DbUsers newUser = new DbUsers();
        newUser.name = input.name;
        newUser.firstname = input.firstName;
        newUser.emailAddress = input.email.toLowerCase();
        newUser.hashPassword = encoder.encode(input.password);
        newUser.createdAt = LocalDateTime.now();

        // Save → retourne un DbUsers avec id généré
        DbUsers savedUser = repository.save(newUser);

        // 2. CRÉATION AUTOMATIQUE DU DOSSIER RACINE
        DbDirectories rootDir = new DbDirectories();
        rootDir.name = "root";    // Nom système, tu pourras afficher "Mes Dossiers" en front
        rootDir.userId = savedUser.id;
        rootDir.parentDirectoryId = null;  // La racine n'a pas de parent
        rootDir.isRoot = true;    // C'est le dossier racine !
        rootDir.createdAt = LocalDateTime.now();

        directoriesRepository.save(rootDir);

        // 3. Préparer la réponse
        RegisterOutput output = new RegisterOutput();
        output.id = savedUser.id;
        output.email = savedUser.emailAddress;

        return output;
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