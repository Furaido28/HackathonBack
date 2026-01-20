package com.helha.thelostgrimoire.application.notes.command;

import com.helha.thelostgrimoire.application.notes.command.create.CreateNotesHandler;
import com.helha.thelostgrimoire.application.notes.query.GetAllByDirectoriesByUserId.GetAllByDirectoriesByUserIdHandler;
import org.springframework.stereotype.Service;

@Service
public class NotesCommandProcessor {
    public final CreateNotesHandler createNotesHandler;
    public final GetAllByDirectoriesByUserIdHandler getAllByDirectoriesByUserIdHandler;

    public NotesCommandProcessor(CreateNotesHandler createNotesHandler, GetAllByDirectoriesByUserIdHandler getAllByDirectoriesByUserIdHandler) {
        this.createNotesHandler = createNotesHandler;
        this.getAllByDirectoriesByUserIdHandler = getAllByDirectoriesByUserIdHandler;
    }
}
