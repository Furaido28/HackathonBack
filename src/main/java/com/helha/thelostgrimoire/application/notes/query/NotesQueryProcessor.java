package com.helha.thelostgrimoire.application.notes.query;

import com.helha.thelostgrimoire.application.notes.query.getAll.GetAllNotesHandler;
import org.springframework.stereotype.Service;

@Service
public class NotesQueryProcessor {
    public final GetAllNotesHandler getAllNotesHandler;

    public NotesQueryProcessor(GetAllNotesHandler getAllNotesHandler) {
        this.getAllNotesHandler = getAllNotesHandler;
    }
}
