package com.helha.thelostgrimoire.application.repositories.notes.query.getById;

import com.helha.thelostgrimoire.application.repositories.utils.CurrentUserContext;
import com.helha.thelostgrimoire.application.repositories.utils.IQueryHandlerIO;
import com.helha.thelostgrimoire.infrastructure.notes.DbNotes;
import com.helha.thelostgrimoire.infrastructure.notes.INotesRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GetNoteByIdHandler
        implements IQueryHandlerIO<GetNoteByIdInput, GetNoteByIdOutput> {

    private final INotesRepository notesRepository;
    private final ModelMapper modelMapper;

    public GetNoteByIdHandler(INotesRepository notesRepository,
                              ModelMapper modelMapper) {
        this.notesRepository = notesRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public GetNoteByIdOutput handle(GetNoteByIdInput request) {

        // Retrieve the current user ID from the context for authorization purposes.
        Long currentUserId = CurrentUserContext.getUserId();

        // Attempt to find the specific note by ID. Throw a 404 Not Found exception if it does not exist.
        DbNotes entity = notesRepository.findById(request.id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Notes not found"
                ));

        // Ownership check: ensure the note belongs to the user attempting to access it.
        // Returns 403 Forbidden if the user is not the owner.
        if (!entity.userId.equals(currentUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to access this note"
            );
        }

        // Map the database entity to the output DTO.
        return modelMapper.map(entity, GetNoteByIdOutput.class);
    }
}