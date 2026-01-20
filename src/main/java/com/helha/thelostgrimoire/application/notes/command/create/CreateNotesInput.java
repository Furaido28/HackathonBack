package com.helha.thelostgrimoire.application.notes.command.create;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CreateNotesInput {
    @JsonIgnore
    public Long userId;
    public Long directoryId;
    public String name;
}
