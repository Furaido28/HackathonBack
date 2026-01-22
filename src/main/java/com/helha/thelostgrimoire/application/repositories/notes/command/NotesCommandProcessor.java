package com.helha.thelostgrimoire.application.repositories.notes.command;

import com.helha.thelostgrimoire.application.repositories.notes.command.create.CreateNotesHandler;
import com.helha.thelostgrimoire.application.repositories.notes.command.update.UpdateNotesHandler;
import com.helha.thelostgrimoire.application.repositories.notes.command.delete.DeleteNotesHandler;
import org.springframework.stereotype.Service;

@Service
public class NotesCommandProcessor {
    public final CreateNotesHandler createNotesHandler;
    public final DeleteNotesHandler deleteNotesHandler;
    public final UpdateNotesHandler updateNotesHandler;

    public NotesCommandProcessor(CreateNotesHandler createNotesHandler, DeleteNotesHandler deleteNotesHandler, UpdateNotesHandler updateNotesHandler) {
        this.createNotesHandler = createNotesHandler;
        this.deleteNotesHandler = deleteNotesHandler;
        this.updateNotesHandler = updateNotesHandler;
    }
}
