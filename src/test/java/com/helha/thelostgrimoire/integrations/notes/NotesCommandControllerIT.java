package com.helha.thelostgrimoire.integrations.notes;

import com.helha.thelostgrimoire.application.repositories.notes.command.create.CreateNotesInput;
import com.helha.thelostgrimoire.application.repositories.notes.command.update.UpdateNotesInput;
import com.helha.thelostgrimoire.infrastructure.notes.DbNotes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("🔥 Tests d'Intégration - Notes Commands (CUD)")
class NotesCommandControllerIT extends AbstractNotesIT {

    // ===================================================================================
    // 📝 CREATE (POST)
    // ===================================================================================
    @Nested
    @DisplayName("📝 CREATE (POST)")
    class CreateOperations {

        @Test
        @DisplayName("201 - Création standard (Nom uniquement)")
        void shouldCreateNote() throws Exception {
            CreateNotesInput input = new CreateNotesInput();
            input.name = "My New Note";
            input.directoryId = savedDirectory.id;

            mockMvc.perform(post("/api/notes")
                            .cookie(jwtCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is("My New Note")));
        }

        @Test
        @DisplayName("201 - Création avec caractères spéciaux/Emojis")
        void shouldCreateNote_WithSpecialChars() throws Exception {
            String specialName = "Note 🚀 (Kanji: 漢字)";
            CreateNotesInput input = new CreateNotesInput();
            input.name = specialName;
            input.directoryId = savedDirectory.id;

            mockMvc.perform(post("/api/notes")
                            .cookie(jwtCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is(specialName)));
        }

        @Test
        @DisplayName("201 - Création limite max (100 chars)")
        void shouldCreateNote_MaxChars() throws Exception {
            String maxName = "a".repeat(100);
            CreateNotesInput input = new CreateNotesInput();
            input.name = maxName;
            input.directoryId = savedDirectory.id;

            mockMvc.perform(post("/api/notes")
                            .cookie(jwtCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("400 - Erreur si nom vide")
        void shouldReturnBadRequest_WhenNameIsEmpty() throws Exception {
            CreateNotesInput input = new CreateNotesInput();
            input.name = "";
            input.directoryId = savedDirectory.id;

            mockMvc.perform(post("/api/notes")
                            .cookie(jwtCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("403 - Erreur si création dans dossier d'autrui")
        void shouldReturnForbidden_WhenCreatingInOtherDirectory() throws Exception {
            DbNotes hackerNote = createHackerNote();
            CreateNotesInput input = new CreateNotesInput();
            input.name = "Intrusion";
            input.directoryId = hackerNote.directoryId;

            mockMvc.perform(post("/api/notes")
                            .cookie(jwtCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isForbidden());
        }
    }

    // ===================================================================================
    // 💾 UPDATE (PUT)
    // ===================================================================================
    @Nested
    @DisplayName("💾 UPDATE (PUT)")
    class UpdateOperations {

        @Test
        @DisplayName("204 - Mise à jour standard (Contenu)")
        void shouldUpdateNoteContent() throws Exception {
            DbNotes savedNote = createNoteInDb("Draft", "");
            UpdateNotesInput input = new UpdateNotesInput();
            input.name = "Final Title";
            input.content = "New Content";

            mockMvc.perform(put("/api/notes/{id}", savedNote.id)
                            .cookie(jwtCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isNoContent());

            DbNotes updated = notesRepository.findById(savedNote.id).orElseThrow();
            assert updated.content.equals("New Content");
        }

        @Test
        @DisplayName("204 - Mise à jour avec contenu géant (MEDIUMTEXT)")
        void shouldUpdateNote_WithLargeContent() throws Exception {
            DbNotes savedNote = createNoteInDb("Empty", "");
            String largeContent = "a".repeat(100000); // 100kb+

            UpdateNotesInput input = new UpdateNotesInput();
            input.name = "Large Note";
            input.content = largeContent;

            mockMvc.perform(put("/api/notes/{id}", savedNote.id)
                            .cookie(jwtCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isNoContent());

            DbNotes updated = notesRepository.findById(savedNote.id).orElseThrow();
            assert updated.content.length() == largeContent.length();
        }

        @Test
        @DisplayName("404 - Erreur si mise à jour note inexistante")
        void shouldReturnNotFound_WhenUpdatingNonExistent() throws Exception {
            UpdateNotesInput input = new UpdateNotesInput();
            input.name = "Ghost";

            mockMvc.perform(put("/api/notes/{id}", 999999L)
                            .cookie(jwtCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("403 - Erreur si mise à jour note d'autrui")
        void shouldReturnForbidden_WhenUpdatingOtherUserNote() throws Exception {
            DbNotes hackerNote = createHackerNote();
            UpdateNotesInput input = new UpdateNotesInput();
            input.name = "Hacked";

            mockMvc.perform(put("/api/notes/{id}", hackerNote.id)
                            .cookie(jwtCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isForbidden());
        }
    }

    // ===================================================================================
    // 🗑️ DELETE (DELETE)
    // ===================================================================================
    @Nested
    @DisplayName("🗑️ DELETE (DELETE)")
    class DeleteOperations {

        @Test
        @DisplayName("204 - Suppression standard")
        void shouldDeleteNote() throws Exception {
            DbNotes savedNote = createNoteInDb("To Delete", "");

            mockMvc.perform(delete("/api/notes/{id}", savedNote.id).cookie(jwtCookie))
                    .andExpect(status().isNoContent());

            assert notesRepository.findById(savedNote.id).isEmpty();
        }

        @Test
        @DisplayName("403 - Erreur si suppression note d'autrui")
        void shouldReturnForbidden_WhenDeletingOtherUserNote() throws Exception {
            DbNotes hackerNote = createHackerNote();
            mockMvc.perform(delete("/api/notes/{id}", hackerNote.id).cookie(jwtCookie))
                    .andExpect(status().isForbidden());
        }
    }
}