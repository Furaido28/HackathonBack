package com.helha.thelostgrimoire.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Notes {
    private int id;
    private String name;
    private String content;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
