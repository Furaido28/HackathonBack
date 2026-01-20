package com.helha.thelostgrimoire.infrastructure.notes;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
public class DbNotes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotBlank(message = "User id can't be empty")
    @Column(name = "user_id")
    public Long userId;

    @NotBlank(message = "Directory id can't be empty")
    @Column(name = "directory_id")
    public Long directoryId;

    @NotBlank(message = "Name can't be empty")
    @Column(name = "name")
    public String name;

    @Column(name = "content")
    public String content;

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
}
