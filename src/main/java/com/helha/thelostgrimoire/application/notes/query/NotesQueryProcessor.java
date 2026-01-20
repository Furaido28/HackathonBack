package com.helha.thelostgrimoire.application.notes.query;

import com.helha.thelostgrimoire.application.notes.query.GetAllByDirectoriesByUserId.GetAllByDirectoriesByUserIdHandler;
import com.helha.thelostgrimoire.application.notes.query.GetAllByDirectoriesByUserId.GetAllByDirectoriesByUserIdInput;
import com.helha.thelostgrimoire.application.notes.query.getAll.GetAllNotesHandler;
import org.springframework.stereotype.Service;

@Service
public class NotesQueryProcessor {
    public final GetAllNotesHandler getAllNotesHandler;
    public final GetAllByDirectoriesByUserIdHandler getAllByDirectoriesByUserIdHandler;

    public NotesQueryProcessor(GetAllNotesHandler getAllNotesHandler, GetAllByDirectoriesByUserIdHandler getAllByDirectoriesByUserIdHandler) {
        this.getAllNotesHandler = getAllNotesHandler;
        this.getAllByDirectoriesByUserIdHandler = getAllByDirectoriesByUserIdHandler;
    }
}
