package com.helha.thelostgrimoire.application.directories.query.getAllByUserId;

import com.helha.thelostgrimoire.application.utils.IQueryHandlerIO;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.directories.IDirectoriesRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllByUserIdHandler implements IQueryHandlerIO<GetAllByUserIdInput, GetAllByUserIdOutput> {
    private final IDirectoriesRepository directoriesRepository;
    private final ModelMapper modelMapper;

    public GetAllByUserIdHandler(IDirectoriesRepository directoriesRepository, ModelMapper modelMapper) {
        this.directoriesRepository = directoriesRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public GetAllByUserIdOutput handle(GetAllByUserIdInput request) {
        List<DbDirectories> entities = directoriesRepository
                .findAllByUserId(request.userId);

        GetAllByUserIdOutput output = new GetAllByUserIdOutput();

        for (DbDirectories entity : entities) {
            output.directories.add(
                    modelMapper.map(entity, GetAllByUserIdOutput.Directory.class)
            );
        }

        return output;
    }
}
