
package com.helha.thelostgrimoire.application.directories.command.create;

import com.helha.thelostgrimoire.application.utils.ICommandHandler;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.directories.IDirectoriesRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CreateDirectoriesHandler implements ICommandHandler<CreateDirectoriesInput, CreateDirectoriesOutput> {

    private final IDirectoriesRepository repository;

    public CreateDirectoriesHandler(IDirectoriesRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public CreateDirectoriesOutput handle(CreateDirectoriesInput input) {

        validate(input);

        // Créer l'entité JPA
        DbDirectories dir = new DbDirectories();
        dir.name = input.name;
        dir.userId = input.userId;                         // imposé depuis l'URL dans le controller
        dir.parentDirectoryId = input.parentDirectoryId;   // peut être null (dossier racine)
        dir.createdAt = LocalDateTime.now();

        // Persistence
        DbDirectories saved = repository.save(dir);

        CreateDirectoriesOutput output = new CreateDirectoriesOutput();
        output.id = saved.id;
        output.name = saved.name;

        return output;
    }

    private void validate(CreateDirectoriesInput input) {
        if (input == null) throw new IllegalArgumentException("Input is required");

        if (isBlank(input.name)) throw new IllegalArgumentException("Directory name is required");
        if (input.userId == null) throw new IllegalArgumentException("UserId is required");




        // parentDirectoryId peut rester null (pas de validation ici)
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
