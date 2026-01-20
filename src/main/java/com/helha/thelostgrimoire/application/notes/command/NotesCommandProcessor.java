package com.helha.thelostgrimoire.application.notes.command;

import com.helha.thelostgrimoire.application.notes.command.create.CreateNotesHandler;
import com.helha.thelostgrimoire.application.notes.command.update.UpdateNotesHandler;
import org.springframework.stereotype.Service;

@Service
public class NotesCommandProcessor {
    public final CreateNotesHandler createNotesHandler;
    public final UpdateNotesHandler updateNotesHandler;

    public NotesCommandProcessor(CreateNotesHandler createNotesHandler,
                                 UpdateNotesHandler updateNotesHandler) {
        this.createNotesHandler = createNotesHandler;
        this.updateNotesHandler = updateNotesHandler;
    }
}
