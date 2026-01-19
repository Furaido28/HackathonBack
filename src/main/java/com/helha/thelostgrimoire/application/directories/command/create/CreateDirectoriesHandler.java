
package com.helha.thelostgrimoire.application.directories.command.create;

import com.helha.thelostgrimoire.application.utils.ICommandHandler;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.directories.IDirectoriesRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class CreateDirectoriesHandler implements ICommandHandler<CreateDirectoriesInput, Long> {

    private final IDirectoriesRepository repository;

    public CreateDirectoriesHandler(IDirectoriesRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Long handle(CreateDirectoriesInput input) {

        validate(input);

        DbDirectories dir = new DbDirectories();
        dir.name = input.name;
        dir.userId = input.userId;
        dir.parentDirectoryId = input.parentDirectoryId;
        dir.createdAt = LocalDateTime.now();

        DbDirectories saved = repository.save(dir);
        return saved.id;
    }

    private void validate(CreateDirectoriesInput input) {
        if (input == null)
            throw new IllegalArgumentException("Input is required");

        if (isBlank(input.name))
            throw new IllegalArgumentException("Directory name is required");

        if (input.userId == null)
            throw new IllegalArgumentException("UserId is required");
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
