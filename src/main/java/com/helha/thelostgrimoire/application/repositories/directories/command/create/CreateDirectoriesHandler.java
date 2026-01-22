package com.helha.thelostgrimoire.application.repositories.directories.command.create;

import com.helha.thelostgrimoire.application.repositories.utils.ICommandHandler;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.directories.IDirectoriesRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class CreateDirectoriesHandler implements ICommandHandler<CreateDirectoriesInput, CreateDirectoriesOutput> {

    private final IDirectoriesRepository repository;
    private final ModelMapper modelMapper;

    public CreateDirectoriesHandler(IDirectoriesRepository repository, ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    @Override
    public CreateDirectoriesOutput handle(CreateDirectoriesInput input) {
        LocalDateTime now = LocalDateTime.now();
        Long finalParentId = input.parentDirectoryId;

        // 1. Gestion du Parent
        if (finalParentId != null) {
            DbDirectories parent = repository.findById(finalParentId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Parent directory not found"
                    ));

            if (!parent.userId.equals(input.userId)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not allowed to create a directory in this parent"
                );
            }
        } else {
            DbDirectories rootDir = repository.findByUserIdAndIsRootTrue(input.userId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Critical Error: User has no root directory."
                    ));
            finalParentId = rootDir.id;
        }

        // Vérification des noms
        boolean duplicateExists = repository.existsByNameAndParentDirectoryIdAndUserId(
                input.name, finalParentId, input.userId
        );

        if (duplicateExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A directory with name '" + input.name + "' already exists in this folder."
            );
        }

        DbDirectories entity = new DbDirectories();
        entity.userId = input.userId;
        entity.name = input.name;
        entity.parentDirectoryId = finalParentId;
        entity.isRoot = false;
        entity.createdAt = now;

        DbDirectories savedEntity = repository.save(entity);
        return modelMapper.map(savedEntity, CreateDirectoriesOutput.class);
    }
}