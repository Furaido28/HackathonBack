package com.helha.thelostgrimoire.application.repositories.notes.command.update;

import com.helha.thelostgrimoire.application.repositories.utils.IEffectCommandHandler;
import com.helha.thelostgrimoire.infrastructure.notes.DbNotes;
import com.helha.thelostgrimoire.infrastructure.notes.INotesRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class UpdateNotesHandler implements IEffectCommandHandler<UpdateNotesInput> {

    private final INotesRepository notesRepository;

    public UpdateNotesHandler(INotesRepository notesRepository) {
        this.notesRepository = notesRepository;
    }

    @Override
    public void handle(UpdateNotesInput input) {
        // Fetch the note from the database; throw 404 Not Found if the ID does not exist.
        DbNotes entity = notesRepository.findById(input.id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Note not found with id " + input.id
                ));

        // Ownership check: ensure the authenticated user owns this note before allowing updates (403 Forbidden).
        if (!entity.userId.equals(input.userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You are not allowed to update this note"
            );
        }

        // Manual validation: ensure mandatory fields are present and not empty (400 Bad Request).
        if (input.name == null || input.name.isBlank()
                || input.content == null || input.content.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "name and content are required"
            );
        }

        // Update the entity fields and refresh the timestamp.
        entity.name = input.name;
        entity.content = input.content;
        entity.updatedAt = LocalDateTime.now();

        // Persist changes to the database.
        notesRepository.save(entity);
    }
}