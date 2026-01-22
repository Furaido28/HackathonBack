package com.helha.thelostgrimoire.application.repositories.directories.command.delete;

import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.directories.IDirectoriesRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class DeleteDirectoriesHandler {
    private final IDirectoriesRepository directoriesRepository;

    public DeleteDirectoriesHandler(IDirectoriesRepository directoriesRepository) {
        this.directoriesRepository = directoriesRepository;
    }

    @Transactional
    public void handle(Long directoryId, Long authenticatedUserId) {

        DbDirectories directory = directoriesRepository.findById(directoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Directory not found"));

        if (!directory.userId.equals(authenticatedUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this directory");
        }

        if (directory.isRoot) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot delete the root directory");
        }

        directoriesRepository.delete(directory);
    }
}