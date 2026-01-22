package com.helha.thelostgrimoire.application.repositories.directories.query.getAllByUserId;

import com.helha.thelostgrimoire.application.repositories.utils.IQueryHandlerIO;
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
        // Query the repository to find all directory records associated with the specific User ID provided in the request.
        List<DbDirectories> entities = directoriesRepository
                .findAllByUserId(request.userId);

        // Initialize the output DTO which will hold the list of directory results.
        GetAllDirectoriesByUserIdOutput output = new GetAllDirectoriesByUserIdOutput();

        // Iterate through the database entities, mapping each one to the internal Directory DTO class before adding it to the result list.
        for (DbDirectories entity : entities) {
            output.directories.add(
                    modelMapper.map(entity, GetAllDirectoriesByUserIdOutput.Directory.class)
            );
        }

        // Return the populated output object containing the user's directory structure.
        return output;
    }
}