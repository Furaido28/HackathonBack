package com.helha.thelostgrimoire.application.notes.query.getById;

import java.time.LocalDateTime;

public class GetNoteByIdOutput {
    public Long id;
    public Long userId;
    public Long directoryId;
    public String name;
    public String content;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
