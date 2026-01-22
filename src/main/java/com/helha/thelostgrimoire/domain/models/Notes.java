package com.helha.thelostgrimoire.domain.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Notes {
    private int id;                     // Note identification
    private String name;                // Name of the note
    private String content;             // Content of the note
    private LocalDateTime created_at;   // Note creation date
    private LocalDateTime updated_at;   // Date of last update of the note
}
