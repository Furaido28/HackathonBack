package com.helha.thelostgrimoire.application.repositories.notes.query.getAll;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GetAllNotesOutput {
    public List<GetAllNotesOutput.Note> notes = new ArrayList<>();

    public static class Note {
        public int id;
        public Long userId;
        public Long directoryId;
        public String name;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
    }
}
