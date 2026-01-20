package com.helha.thelostgrimoire.application.notes.command;

import com.helha.thelostgrimoire.application.notes.command.create.CreateNotesHandler;
import org.springframework.stereotype.Service;

@Service
public class NotesCommandProcessor {
    public final CreateNotesHandler createNotesHandler;

    public NotesCommandProcessor(CreateNotesHandler createNotesHandler) {
        this.createNotesHandler = createNotesHandler;
    }
}
