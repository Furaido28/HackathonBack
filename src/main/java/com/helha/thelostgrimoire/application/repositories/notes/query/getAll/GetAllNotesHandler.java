package com.helha.thelostgrimoire.application.repositories.notes.query.getAll;

import com.helha.thelostgrimoire.application.repositories.utils.CurrentUserContext;
import com.helha.thelostgrimoire.application.repositories.utils.IQueryHandler;
import com.helha.thelostgrimoire.infrastructure.notes.DbNotes;
import com.helha.thelostgrimoire.infrastructure.notes.INotesRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllNotesHandler implements IQueryHandler<GetAllNotesOutput> {
    private final INotesRepository notesRepository;
    private final ModelMapper modelMapper;

    public GetAllNotesHandler(INotesRepository notesRepository, ModelMapper modelMapper) {
        this.notesRepository = notesRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public GetAllNotesOutput handle() {
        // Retrieve the unique identifier of the currently authenticated user from the security context.
        Long userId = CurrentUserContext.getUserId();

        // Fetch all notes belonging to the user, sorted alphabetically by name in ascending order.
        List<DbNotes> entities = notesRepository.findAllByUserIdOrderByNameAsc(userId);

        // Initialize the response DTO container.
        GetAllNotesOutput output = new GetAllNotesOutput();

        // Map each persistence entity to the output DTO structure and collect them into the result list.
        for (DbNotes entity : entities) {
            output.notes.add(modelMapper.map(entity, GetAllNotesOutput.Note.class));
        }

        return output;
    }
}