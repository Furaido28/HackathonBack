package com.helha.thelostgrimoire.application.notes.query.GetAllByDirectoriesByUserId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GetAllByDirectoriesByUserIdOutput {
    public List<NoteDto> notes = new ArrayList<>();

    public static class NoteDto {
        public Long id;
        public String name;
        public String content;
        public LocalDateTime createdAt;
    }
}