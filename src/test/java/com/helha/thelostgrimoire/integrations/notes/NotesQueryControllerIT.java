package com.helha.thelostgrimoire.integrations.notes;

import com.helha.thelostgrimoire.infrastructure.notes.DbNotes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("🔍 Tests d'Intégration - Notes Queries (GET)")
class NotesQueryControllerIT extends AbstractNotesIT {

    @Nested
    @DisplayName("📖 READ (GET)")
    class ReadOperations {

        @Test
        @DisplayName("200 - Récupérer toutes les notes")
        void shouldGetAllNotes() throws Exception {
            createNoteInDb("Note 1", "C1");
            createNoteInDb("Note 2", "C2");

            mockMvc.perform(get("/api/notes").cookie(jwtCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.notes", hasSize(2)));
        }

        @Test
        @DisplayName("200 - Récupérer par ID")
        void shouldGetNoteById() throws Exception {
            DbNotes savedNote = createNoteInDb("Target Note", "Content");

            mockMvc.perform(get("/api/notes/{id}", savedNote.id).cookie(jwtCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Target Note")));
        }

        @Test
        @DisplayName("200 - Récupérer par Dossier")
        void shouldGetNotesByDirectory() throws Exception {
            createNoteInDb("In Dir", "Content");
            mockMvc.perform(get("/api/notes/directory/{id}", savedDirectory.id).cookie(jwtCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.notes", hasSize(1)));
        }

        @Test
        @DisplayName("404 - Erreur si note inexistante")
        void shouldReturnNotFound_WhenNoteDoesNotExist() throws Exception {
            mockMvc.perform(get("/api/notes/{id}", 999999L).cookie(jwtCookie))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("403 - Erreur si lecture note d'autrui")
        void shouldReturnForbidden_WhenReadingOtherUserNote() throws Exception {
            DbNotes hackerNote = createHackerNote();
            mockMvc.perform(get("/api/notes/{id}", hackerNote.id).cookie(jwtCookie))
                    .andExpect(status().isForbidden());
        }
    }
}