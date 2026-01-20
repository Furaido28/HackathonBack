package com.helha.thelostgrimoire.application.notes.query;

import com.helha.thelostgrimoire.application.notes.query.getAll.GetAllNotesHandler;
import com.helha.thelostgrimoire.application.notes.query.getById.GetNoteByIdHandler;
import org.springframework.stereotype.Service;

@Service
public class NotesQueryProcessor {
    public final GetAllNotesHandler getAllNotesHandler;
    public final GetNoteByIdHandler getNoteByIdHandler;

    public NotesQueryProcessor(GetAllNotesHandler getAllNotesHandler,
                               GetNoteByIdHandler getNoteByIdHandler) {
        this.getAllNotesHandler = getAllNotesHandler;
        this.getNoteByIdHandler = getNoteByIdHandler;
    }
}
