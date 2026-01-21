package com.helha.thelostgrimoire.application.notes.command.create;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class CreateNotesInput {
    @JsonIgnore
    public Long userId;
    public Long directoryId;
    @NotBlank(message = "Name cannot be empty")
    @Size(max = 100, message = "Name is too long")
    public String name;
    @JsonIgnore
    public LocalDateTime createdAt;
    @JsonIgnore
    public LocalDateTime updatedAt;
}
