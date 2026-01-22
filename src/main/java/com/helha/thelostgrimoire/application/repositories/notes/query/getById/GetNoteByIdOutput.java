package com.helha.thelostgrimoire.application.repositories.notes.query.getById;

import java.time.LocalDateTime;

public class GetNoteByIdOutput {
    public Long id;
    public Long userId;
    public Long directoryId;
    public String name;
    public String content;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    // Champs pour les métadonnées
    // ModelMapper fait le lien automatiquement avec les getters de DbNotes
    public Long byteSize;
    public Integer characterCount;
    public Integer wordCount;
    public Integer lineCount;
}