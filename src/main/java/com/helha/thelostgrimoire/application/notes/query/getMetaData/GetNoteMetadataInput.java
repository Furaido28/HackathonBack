package com.helha.thelostgrimoire.application.notes.query.getMetaData;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GetNoteMetadataInput {
    public Long noteId;
    @JsonIgnore
    public Long userId;
}