package com.helha.thelostgrimoire.application.notes.command.update;

import com.helha.thelostgrimoire.application.utils.IEffectCommandHandler;
// import com.helha.thelostgrimoire.controllers.notes.exceptions.NotesNotFound; // Plus besoin de ça ici
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
        // 1. Récupération de la note (CORRECTION ICI : On force le 404)
        DbNotes entity = notesRepository.findById(input.id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Note not found with id " + input.id
                ));

        // 2. Vérification du propriétaire (403)
        if (!entity.userId.equals(input.userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You are not allowed to update this note"
            );
        }

        // 3. Validation manuelle (Optionnelle si tu as @Valid dans le controller + annotations dans l'Input)
        // Mais tu peux la garder par sécurité.
        if (input.name == null || input.name.isBlank()
                || input.content == null || input.content.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "name and content are required"
            );
        }

        // 4. Mise à jour
        entity.name = input.name;
        entity.content = input.content;
        entity.updatedAt = LocalDateTime.now();

        notesRepository.save(entity);
    }
}