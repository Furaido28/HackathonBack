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
        DbDirectories directory = directoriesRepository.findById(input.id)
                .orElseThrow(() -> new DirectoriesNotFound(input.id));

        if (!directory.userId.equals(input.userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to update this directory");
        }

        if (input.name != null && !input.name.isBlank()) {
            // --- NOUVELLE VÉRIFICATION DE DOUBLON ---
            // On vérifie si un dossier avec ce nom existe déjà au même endroit
            // MAIS on exclut le dossier actuel (d'où le "id != input.id")
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

        directoriesRepository.save(directory);
    }
}