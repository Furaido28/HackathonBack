
package com.helha.thelostgrimoire.application.directories.command.create;

import com.helha.thelostgrimoire.application.utils.CurrentUserContext;
import com.helha.thelostgrimoire.application.utils.ICommandHandler;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.directories.IDirectoriesRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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



        DbDirectories entity = modelMapper.map(input, DbDirectories.class);
        entity.id = null;
        entity.createdAt = now;
        DbDirectories savedEntity = repository.save(entity);
        return modelMapper.map(savedEntity, CreateDirectoriesOutput.class);
    }
}
