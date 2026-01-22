package com.helha.thelostgrimoire.application.repositories.notes.command.create;

import java.time.LocalDateTime;

public class CreateNotesOutput {
    public Long id;
    public Long userId;
    public Long directoryId;
    public String name;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
