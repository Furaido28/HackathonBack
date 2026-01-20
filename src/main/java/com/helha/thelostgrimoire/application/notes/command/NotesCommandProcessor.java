package com.helha.thelostgrimoire.application.notes.command;

import com.helha.thelostgrimoire.application.notes.command.create.CreateNotesHandler;
import com.helha.thelostgrimoire.application.notes.command.update.UpdateNotesHandler;
import com.helha.thelostgrimoire.application.notes.command.delete.DeleteNotesHandler;
import com.helha.thelostgrimoire.application.notes.query.GetAllByDirectoriesByUserId.GetAllByDirectoriesByUserIdHandler;
import org.springframework.stereotype.Service;

@Service
public class NotesCommandProcessor {
    public final CreateNotesHandler createNotesHandler;
    public final DeleteNotesHandler deleteNotesHandler;
    public final UpdateNotesHandler updateNotesHandler;

    public NotesCommandProcessor(CreateNotesHandler createNotesHandler, GetAllByDirectoriesByUserIdHandler getAllByDirectoriesByUserIdHandler) {
        this.createNotesHandler = createNotesHandler;
        this.deleteNotesHandler = deleteNotesHandler;
        this.updateNotesHandler = updateNotesHandler;
    }
}
