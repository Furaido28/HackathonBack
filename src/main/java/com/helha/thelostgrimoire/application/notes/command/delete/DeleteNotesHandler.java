package com.helha.thelostgrimoire.application.notes.command.delete;

import com.helha.thelostgrimoire.infrastructure.notes.DbNotes;
import com.helha.thelostgrimoire.infrastructure.notes.INotesRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DeleteNotesHandler {
    private final INotesRepository notesRepository;

    public DeleteNotesHandler(INotesRepository notesRepository) {
        this.notesRepository = notesRepository;
    }

    public void handle(Long notesId, Long authenticatedUserId) {
        DbNotes note = notesRepository.findById(notesId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));

        if (!note.userId.equals(authenticatedUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this note");
        }

        notesRepository.delete(note);
    }
}
