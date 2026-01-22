package com.helha.thelostgrimoire.application.repositories.notes.query;

import com.helha.thelostgrimoire.application.repositories.notes.query.GetAllByDirectoriesByUserId.GetAllByDirectoriesByUserIdHandler;
import com.helha.thelostgrimoire.application.repositories.notes.query.getAll.GetAllNotesHandler;
import com.helha.thelostgrimoire.application.repositories.notes.query.getById.GetNoteByIdHandler;
import com.helha.thelostgrimoire.application.repositories.notes.query.getMetaData.GetNoteMetadataHandler;
import org.springframework.stereotype.Service;

@Service
public class NotesQueryProcessor {
    public final GetAllNotesHandler getAllNotesHandler;
    public final GetAllByDirectoriesByUserIdHandler getAllByDirectoriesByUserIdHandler;
    public final GetNoteByIdHandler getNoteByIdHandler;
    public final GetNoteMetadataHandler getNoteMetadataHandler;

    public NotesQueryProcessor(GetAllNotesHandler getAllNotesHandler,
                               GetAllByDirectoriesByUserIdHandler getAllByDirectoriesByUserIdHandler,
                               GetNoteByIdHandler getNoteByIdHandler, GetNoteMetadataHandler getNoteMetadataHandler) {
        this.getAllNotesHandler = getAllNotesHandler;
        this.getAllByDirectoriesByUserIdHandler = getAllByDirectoriesByUserIdHandler;
        this.getNoteByIdHandler = getNoteByIdHandler;
        this.getNoteMetadataHandler = getNoteMetadataHandler;
    }
}
