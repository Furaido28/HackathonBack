package com.helha.thelostgrimoire.application.directories.query.getAllByUserId;

import com.helha.thelostgrimoire.application.utils.IQueryHandlerIO;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.directories.IDirectoriesRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllDirectoriesByUserIdHandler implements IQueryHandlerIO<GetAllDirectoriesByUserIdInput, GetAllDirectoriesByUserIdOutput> {
    private final IDirectoriesRepository directoriesRepository;
    private final ModelMapper modelMapper;

    public GetAllDirectoriesByUserIdHandler(IDirectoriesRepository directoriesRepository, ModelMapper modelMapper) {
        this.directoriesRepository = directoriesRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public GetAllDirectoriesByUserIdOutput handle(GetAllDirectoriesByUserIdInput request) {
        List<DbDirectories> entities = directoriesRepository
                .findAllByUserId(request.userId);

        GetAllDirectoriesByUserIdOutput output = new GetAllDirectoriesByUserIdOutput();

        for (DbDirectories entity : entities) {
            output.directories.add(
                    modelMapper.map(entity, GetAllDirectoriesByUserIdOutput.Directory.class)
            );
        }

        return output;
    }
}
