package com.helha.thelostgrimoire.application.repositories.directories.query.getAll;

import com.helha.thelostgrimoire.application.repositories.utils.IQueryHandler;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.directories.IDirectoriesRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllDirectoriesHandler implements IQueryHandler<GetAllDirectorieOutput> {
    private final IDirectoriesRepository directoriesRepository;
    private final ModelMapper modelMapper;

    public GetAllDirectoriesHandler(IDirectoriesRepository directoriesRepository, ModelMapper modelMapper) {
        this.directoriesRepository = directoriesRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public GetAllDirectorieOutput handle() {
        // Retrieve all directory entities from the database, sorted alphabetically by name in ascending order.
        List<DbDirectories> entities = directoriesRepository.findAll(
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "name")
        );

        // Initialize the output container.
        GetAllDirectorieOutput output = new GetAllDirectorieOutput();

        // Map each database entity to the corresponding Directory DTO and add it to the output list.
        for (DbDirectories entity : entities) {
            output.directories.add(modelMapper.map(entity, GetAllDirectorieOutput.Directory.class));
        }

        return output;
    }
}