package com.helha.thelostgrimoire.infrastructure.notes;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets; // <--- Import nécessaire
import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
public class DbNotes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull(message = "User id can't be null")
    @Column(name = "user_id")
    public Long userId;

    @NotNull(message = "Directory id can't be null")
    @Column(name = "directory_id")
    public Long directoryId;

    @NotBlank(message = "Name can't be empty")
    @Column(name = "name")
    public String name;

    // J'ajoute MEDIUMTEXT pour permettre les contenus > 255 caractères (sinon crash sur gros textes)
    @Column(name = "content", columnDefinition = "MEDIUMTEXT")
    public String content;

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    // =================================================================
    // MÉTADONNÉES CALCULÉES AUTOMATIQUEMENT (Virtual Getters)
    // =================================================================

    // Taille en octets (UTF-8)
    public long getByteSize() {
        if (content == null) return 0;
        return content.getBytes(StandardCharsets.UTF_8).length;
    }

    // Nombre de caractères
    public int getCharacterCount() {
        return content == null ? 0 : content.length();
    }

    // Nombre de mots (séparation par espace blanc)
    public int getWordCount() {
        if (content == null || content.isBlank()) return 0;
        return content.trim().split("\\s+").length;
    }

    // Nombre de lignes
    public int getLineCount() {
        if (content == null || content.isEmpty()) return 0;
        // On compte les sauts de ligne (\n, \r, ou \r\n)
        return content.split("\r\n|\r|\n", -1).length;
    }
}