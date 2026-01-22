package com.helha.thelostgrimoire.application.repositories.notes.query.getMetaData;

import com.helha.thelostgrimoire.application.repositories.utils.IQueryHandlerIO;
import com.helha.thelostgrimoire.infrastructure.notes.DbNotes;
import com.helha.thelostgrimoire.infrastructure.notes.INotesRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GetNoteMetadataHandler implements IQueryHandlerIO<GetNoteMetadataInput, GetNoteMetadataOutput> {

    private final INotesRepository notesRepository;
    private final ModelMapper modelMapper;

    public GetNoteMetadataHandler(INotesRepository notesRepository, ModelMapper modelMapper) {
        this.notesRepository = notesRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public GetNoteMetadataOutput handle(GetNoteMetadataInput input) {
        // Fetch the note entity by its ID. Throw 404 Not Found if the note does not exist in the database.
        DbNotes note = notesRepository.findById(input.noteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));

        // Authorization check: Validate that the note belongs to the user requesting the metadata.
        // This prevents users from accessing timestamps or other metadata of notes they do not own.
        if (!note.userId.equals(input.userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        // Map the existing database entity to the metadata output DTO.
        return modelMapper.map(note, GetNoteMetadataOutput.class);
    }
}