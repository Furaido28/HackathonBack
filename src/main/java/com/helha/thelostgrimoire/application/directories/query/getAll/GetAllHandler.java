package com.helha.thelostgrimoire.application.directories.query.getAll;

import com.helha.thelostgrimoire.application.utils.IQueryHandler;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.directories.IDirectoriesRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllHandler implements IQueryHandler<GetAllOutput> {
    private final IDirectoriesRepository directoriesRepository;
    private final ModelMapper modelMapper;

    public GetAllHandler(IDirectoriesRepository directoriesRepository, ModelMapper modelMapper) {
        this.directoriesRepository = directoriesRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public GetAllOutput handle() {
        List<DbDirectories> entities = directoriesRepository.findAll();

        GetAllOutput output = new GetAllOutput();

        for (DbDirectories entity : entities) {
            output.directories.add(modelMapper.map(entity, GetAllOutput.Directory.class));
        }

        return output;
    }
}
