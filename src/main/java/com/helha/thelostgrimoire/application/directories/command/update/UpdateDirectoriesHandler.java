package com.helha.thelostgrimoire.application.directories.command.update;

import com.helha.thelostgrimoire.application.utils.IEffectCommandHandler;
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
            directory.name = input.name;
        }

        directoriesRepository.save(directory);
    }
}