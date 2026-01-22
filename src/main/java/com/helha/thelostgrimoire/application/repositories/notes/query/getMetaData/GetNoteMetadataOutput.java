package com.helha.thelostgrimoire.application.repositories.notes.query.getMetaData;

import java.time.LocalDateTime;

public class GetNoteMetadataOutput {
    public Long id;
    public String name;

    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public Long byteSize;
    public Integer characterCount;
    public Integer wordCount;
    public Integer lineCount;
}