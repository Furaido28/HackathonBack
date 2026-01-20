package com.helha.thelostgrimoire.application.directories.query.getAll;

import com.helha.thelostgrimoire.application.utils.IQueryHandler;
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
        List<DbDirectories> entities = directoriesRepository.findAll();

        GetAllDirectorieOutput output = new GetAllDirectorieOutput();

        for (DbDirectories entity : entities) {
            output.directories.add(modelMapper.map(entity, GetAllDirectorieOutput.Directory.class));
        }

        return output;
    }
}
