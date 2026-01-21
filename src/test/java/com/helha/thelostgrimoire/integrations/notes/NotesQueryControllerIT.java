package com.helha.thelostgrimoire.integrations.notes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Notes - Query Controller IT")
public class NotesQueryControllerIT extends AbstractNotesIT {

    @Test
    @DisplayName("GET /api/notes - 200 - Récupère toutes les notes")
    void shouldGetAllNotes() throws Exception {
        createNoteInDb("Note 1");
        createNoteInDb("Note 2");

        mockMvc.perform(get("/api/notes")
                        .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes", hasSize(2)))
                .andExpect(jsonPath("$.notes[0].name", is("Note 1")));
    }

    @Test
    @DisplayName("GET /api/notes/directory/{id} - 200 - Récupère les notes d'un dossier spécifique")
    void shouldGetNotesByDirectory() throws Exception {
        createNoteInDb("DirNote A");
        createNoteInDb("DirNote B");

        mockMvc.perform(get("/api/notes/directory/{directoryId}", savedDirectory.id)
                        .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes", hasSize(2)))
                .andExpect(jsonPath("$.notes[0].name", is("DirNote A")));
    }

    @Test
    @DisplayName("GET /api/notes/{id} - 200 - Récupère une note par son ID")
    void shouldGetNoteById() throws Exception {
        var savedNote = createNoteInDb("Existing Note");

        mockMvc.perform(get("/api/notes/{id}", savedNote.id)
                        .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Existing Note")));
    }
}
