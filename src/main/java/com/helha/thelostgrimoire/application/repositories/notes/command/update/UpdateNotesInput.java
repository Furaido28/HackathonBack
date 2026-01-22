package com.helha.thelostgrimoire.application.repositories.notes.command.update;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class UpdateNotesInput {
    @JsonIgnore
    public Long id;
    @JsonIgnore
    public Long userId;
    public String name;
    public String content;
}
