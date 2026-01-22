package com.helha.thelostgrimoire.application.repositories.notes.query.GetAllByDirectoriesByUserId;

import com.helha.thelostgrimoire.application.repositories.utils.IQueryHandlerIO;
import com.helha.thelostgrimoire.controllers.directories.exceptions.DirectoriesNotFound;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.directories.IDirectoriesRepository;
import com.helha.thelostgrimoire.infrastructure.notes.DbNotes;
import com.helha.thelostgrimoire.infrastructure.notes.INotesRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class GetAllByDirectoriesByUserIdHandler implements IQueryHandlerIO<GetAllByDirectoriesByUserIdInput, GetAllByDirectoriesByUserIdOutput> {

    private final INotesRepository notesRepository;
    private final IDirectoriesRepository directoriesRepository;
    private final ModelMapper modelMapper;

    public GetAllByDirectoriesByUserIdHandler(INotesRepository notesRepository, IDirectoriesRepository directoriesRepository, ModelMapper modelMapper) {
        this.notesRepository = notesRepository;
        this.directoriesRepository = directoriesRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public GetAllByDirectoriesByUserIdOutput handle(GetAllByDirectoriesByUserIdInput input) {
        // Retrieve the directory by ID; throw a custom 404 exception if it does not exist.
        DbDirectories directory = directoriesRepository.findById(input.directoryId)
                .orElseThrow(() -> new DirectoriesNotFound(input.directoryId));

        // Security check: ensure the target directory actually belongs to the user making the request.
        if (!directory.userId.equals(input.userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
        }

        // Fetch all notes contained in the specified directory, sorted alphabetically by name.
        List<DbNotes> entities = notesRepository.findAllByDirectoryIdOrderByNameAsc(input.directoryId);

        // Initialize output container and map database entities to the DTO list.
        GetAllByDirectoriesByUserIdOutput output = new GetAllByDirectoriesByUserIdOutput();
        for (DbNotes entity : entities) {
            output.notes.add(modelMapper.map(entity, GetAllByDirectoriesByUserIdOutput.NoteDto.class));
        }
        return output;
    }
}