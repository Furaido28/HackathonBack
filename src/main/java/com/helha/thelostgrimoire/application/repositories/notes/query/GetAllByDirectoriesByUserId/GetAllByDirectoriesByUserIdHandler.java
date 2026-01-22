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
        DbDirectories directory = directoriesRepository.findById(input.directoryId)
                .orElseThrow(() -> new DirectoriesNotFound(input.directoryId));

        if (!directory.userId.equals(input.userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: This directory does not belong to you.");
        }

        List<DbNotes> entities = notesRepository.findAllByDirectoryId(input.directoryId);


        GetAllByDirectoriesByUserIdOutput output = new GetAllByDirectoriesByUserIdOutput();
        for (DbNotes entity : entities) {
            output.notes.add(modelMapper.map(entity, GetAllByDirectoriesByUserIdOutput.NoteDto.class));
        }

        return output;
    }
}