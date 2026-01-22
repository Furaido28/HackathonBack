package com.helha.thelostgrimoire.application.repositories.notes.command.create;

import com.helha.thelostgrimoire.application.repositories.utils.CurrentUserContext;
import com.helha.thelostgrimoire.application.repositories.utils.ICommandHandler;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.directories.IDirectoriesRepository;
import com.helha.thelostgrimoire.infrastructure.notes.DbNotes;
import com.helha.thelostgrimoire.infrastructure.notes.INotesRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class CreateNotesHandler implements ICommandHandler<CreateNotesInput, CreateNotesOutput> {
    private final INotesRepository notesRepository;
    private final IDirectoriesRepository directoriesRepository;
    private final ModelMapper modelMapper;

    public CreateNotesHandler(INotesRepository notesRepository, IDirectoriesRepository directoriesRepository, ModelMapper modelMapper) {
        this.notesRepository = notesRepository;
        this.directoriesRepository = directoriesRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public CreateNotesOutput handle(CreateNotesInput request) {

        // Retrieve the current authenticated user's ID and current timestamp.
        Long userId = CurrentUserContext.getUserId();
        LocalDateTime now = LocalDateTime.now();

        // Variable to hold the final target directory ID (requested ID or root fallback).
        Long targetDirectoryId = request.directoryId;

        // 1. Target Directory Determination
        if (targetDirectoryId == null || targetDirectoryId == 0) {
            // CASE 1: No ID provided -> Automatically fetch the user's ROOT directory.
            DbDirectories rootDir = directoriesRepository.findByUserIdAndIsRootTrue(userId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Critical Error: User root directory not found"
                    ));
            targetDirectoryId = rootDir.id;

        } else {
            // CASE 2: ID provided -> Verify it exists and belongs to the current user.
            DbDirectories directory = directoriesRepository.findById(targetDirectoryId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Directory not found"
                    ));

            // Security check: prevent creating notes in directories owned by other users.
            if (!directory.userId.equals(userId)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "You cannot create a note in a directory that is not yours"
                );
            }
        }

        // --- Name Uniqueness Verification ---
        // Ensure no note with the same name already exists within the target directory.
        boolean nameExists = notesRepository.existsByNameAndDirectoryId(request.name, targetDirectoryId);

        if (nameExists) {
            // Return 409 Conflict if a duplicate name is found in the same folder.
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A note with this name already exists in the destination directory"
            );
        }
        // --------------------------------------------------------------

        // 2. Entity Initialization
        // Build the note entity with calculated IDs and default empty content.
        DbNotes entity = new DbNotes();
        entity.userId = userId;
        entity.directoryId = targetDirectoryId;
        entity.name = request.name;
        entity.content = ""; // Default empty content to avoid null issues during metadata calculation.
        entity.createdAt = now;
        entity.updatedAt = now;

        // 3. Persist to Database
        // Save the entity and map it to the response DTO.
        DbNotes saved = notesRepository.save(entity);

        return modelMapper.map(saved, CreateNotesOutput.class);
    }
}