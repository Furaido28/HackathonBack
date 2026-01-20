package com.helha.thelostgrimoire.application.directories.command.create;

import com.helha.thelostgrimoire.application.utils.ICommandHandler;
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

        if (input.parentDirectoryId != null) {
            DbDirectories parent = repository.findById(input.parentDirectoryId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Parent directory not found"
                    ));

            if (!parent.userId.equals(input.userId)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not allowed to create a directory in this parent"
                );
            }
        }

        DbDirectories entity = modelMapper.map(input, DbDirectories.class);
        entity.id = null;
        entity.createdAt = now;

        DbDirectories savedEntity = repository.save(entity);
        return modelMapper.map(savedEntity, CreateDirectoriesOutput.class);
    }
}
