package com.helha.thelostgrimoire.application.notes.query;

import com.helha.thelostgrimoire.application.notes.query.GetAllByDirectoriesByUserId.GetAllByDirectoriesByUserIdHandler;
import com.helha.thelostgrimoire.application.notes.query.GetAllByDirectoriesByUserId.GetAllByDirectoriesByUserIdInput;
import com.helha.thelostgrimoire.application.notes.query.getAll.GetAllNotesHandler;
import com.helha.thelostgrimoire.application.notes.query.getById.GetNoteByIdHandler;
import org.springframework.stereotype.Service;

@Service
public class NotesQueryProcessor {
    public final GetAllNotesHandler getAllNotesHandler;
    public final GetAllByDirectoriesByUserIdHandler getAllByDirectoriesByUserIdHandler;
    public final GetNoteByIdHandler getNoteByIdHandler;

    public NotesQueryProcessor(GetAllNotesHandler getAllNotesHandler, 
                               GetAllByDirectoriesByUserIdHandler getAllByDirectoriesByUserIdHandler,
                               GetNoteByIdHandler getNoteByIdHandler) {
        this.getAllNotesHandler = getAllNotesHandler;
        this.getAllByDirectoriesByUserIdHandler = getAllByDirectoriesByUserIdHandler;
        this.getNoteByIdHandler = getNoteByIdHandler;

    }
}
