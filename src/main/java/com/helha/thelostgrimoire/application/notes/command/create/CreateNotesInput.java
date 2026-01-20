package com.helha.thelostgrimoire.application.notes.command.create;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

public class CreateNotesInput {
    @JsonIgnore
    public Long userId;
    public Long directoryId;
    public String name;
    @JsonIgnore
    public LocalDateTime createdAt;
    @JsonIgnore
    public LocalDateTime updatedAt;
}
