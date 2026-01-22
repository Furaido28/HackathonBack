package com.helha.thelostgrimoire.application.repositories.directories.command.update;

import com.helha.thelostgrimoire.application.repositories.utils.IEffectCommandHandler;
import com.helha.thelostgrimoire.controllers.directories.exceptions.DirectoriesNotFound;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.directories.IDirectoriesRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UpdateDirectoriesHandler implements IEffectCommandHandler<UpdateDirectoriesInput> {
    private final IDirectoriesRepository directoriesRepository;

    public UpdateDirectoriesHandler(IDirectoriesRepository directoriesRepository) {
        this.directoriesRepository = directoriesRepository;
    }

    @Override
    public void handle(UpdateDirectoriesInput input) {
        // Fetch the existing directory by ID; throw a custom exception if not found.
        DbDirectories directory = directoriesRepository.findById(input.id)
                .orElseThrow(() -> new DirectoriesNotFound(input.id));

        // Security check: Verify that the authenticated user owns the directory before allowing modifications.
        if (!directory.userId.equals(input.userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to update this directory");
        }

        // Validate and process name update.
        if (input.name != null && !input.name.isBlank()) {
            // Uniqueness check: Ensure the new name is not already taken by another directory in the same folder.
            // The 'IdNot' condition prevents the check from conflicting with the directory currently being updated.
            boolean duplicateExists = directoriesRepository.existsByNameAndParentDirectoryIdAndUserIdAndIdNot(
                    input.name,
                    directory.parentDirectoryId,
                    input.userId,
                    input.id
            );

            if (duplicateExists) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A directory with name '" + input.name + "' already exists in this folder."
                );
            }

            directory.name = input.name;
        }

        // Persist the updated directory entity back to the database.
        directoriesRepository.save(directory);
    }
}