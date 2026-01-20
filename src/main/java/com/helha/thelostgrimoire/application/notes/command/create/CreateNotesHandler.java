package com.helha.thelostgrimoire.application.notes.command.create;

import com.helha.thelostgrimoire.application.utils.CurrentUserContext;
import com.helha.thelostgrimoire.application.utils.ICommandHandler;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.directories.IDirectoriesRepository;
import com.helha.thelostgrimoire.infrastructure.notes.DbNotes;
import com.helha.thelostgrimoire.infrastructure.notes.INotesRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CreateNotesHandler implements ICommandHandler<CreateNotesInput, CreateNotesOutput> {
    private final INotesRepository notesRepository;
    private final IDirectoriesRepository directoriesRepository;
    private final ModelMapper modelMapper;

    public CreateNotesHandler(INotesRepository notesRepository, IDirectoriesRepository directoriesRepository, ModelMapper modelMapper) {
        this.notesRepository = notesRepository;
        this.directoriesRepository = directoriesRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public CreateNotesOutput handle(CreateNotesInput request) {

        Long userId = CurrentUserContext.getUserId();
        LocalDateTime now = LocalDateTime.now();

        boolean ownsDirectory = directoriesRepository
                .existsByIdAndUserId(request.directoryId, userId);

        if (!ownsDirectory) {
            throw new RuntimeException("You cannot create a note in this directory");
        }

        DbNotes entity = new DbNotes();
        entity.userId = userId;
        entity.directoryId = request.directoryId;
        entity.name = request.name;
        entity.content = "";
        entity.createdAt = now;
        entity.updatedAt = now;

        DbNotes saved = notesRepository.save(entity);

        return modelMapper.map(saved, CreateNotesOutput.class);
    }
}
