package com.helha.thelostgrimoire.application.notes.query.getById;
import com.helha.thelostgrimoire.application.utils.CurrentUserContext;
import com.helha.thelostgrimoire.application.utils.IQueryHandlerIO;
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

        Long currentUserId = CurrentUserContext.getUserId();

        DbNotes entity = notesRepository.findById(request.id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Notes not found"
                ));

        if (!entity.userId.equals(currentUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED
            );
        }

        return modelMapper.map(entity, GetNoteByIdOutput.class);
    }
}

