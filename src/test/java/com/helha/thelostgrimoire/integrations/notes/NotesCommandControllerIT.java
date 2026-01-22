package com.helha.thelostgrimoire.integrations.notes;

import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.application.repositories.notes.command.create.CreateNotesInput;
import com.helha.thelostgrimoire.application.repositories.notes.command.update.UpdateNotesInput;
import com.helha.thelostgrimoire.infrastructure.notes.DbNotes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Tests d'Intégration - Notes Commands (CUD)")
class NotesCommandControllerIT extends AbstractNotesIT {

    @Nested
    @DisplayName("CREATE (POST)")
    class CreateOperations {

        @Test
        @DisplayName("201 - Création standard")
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

        // --- NOUVEAU TEST AJOUTÉ ICI ---
        @Test
        @DisplayName("409 - Erreur si nom déjà pris dans le dossier")
        void shouldReturnConflict_WhenNameAlreadyExists() throws Exception {
            // GIVEN : Une note existe déjà avec ce nom
            createNoteInDb("Duplicate Note", "Original Content");

            // WHEN : On tente de créer une autre note avec le MÊME nom dans le MÊME dossier
            CreateNotesInput input = new CreateNotesInput();
            input.name = "Duplicate Note";
            input.directoryId = savedDirectory.id;

            mockMvc.perform(post("/api/notes")
                            .cookie(jwtCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    // THEN : On s'attend à un conflit (409)
                    .andExpect(status().isConflict());
        }
        // -------------------------------

        @Test
        @DisplayName("201 - Création à la racine (null parent)")
        void shouldCreateNoteInRoot() throws Exception {
            CreateNotesInput input = new CreateNotesInput();
            input.name = "Root Note";
            input.directoryId = null;

            mockMvc.perform(post("/api/notes")
                            .cookie(jwtCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isCreated());

            DbNotes created = notesRepository.findAll().stream()
                    .filter(n -> n.name.equals("Root Note")).findFirst().get();
            assertEquals(savedRootDirectory.id, created.directoryId);
        }

        @Test
        @DisplayName("201 - Création avec Emojis")
        void shouldCreateNote_WithEmojis() throws Exception {
            CreateNotesInput input = new CreateNotesInput();
            input.name = "Note 🎃👻";
            input.directoryId = savedDirectory.id;

            mockMvc.perform(post("/api/notes")
                            .cookie(jwtCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is("Note 🎃👻")));
        }

        @Test
        @DisplayName("400 - Nom vide")
        void shouldFail_WhenNameIsEmpty() throws Exception {
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
        @DisplayName("404 - Dossier parent inexistant")
        void shouldFail_WhenDirectoryNotFound() throws Exception {
            CreateNotesInput input = new CreateNotesInput();
            input.name = "Lost Note";
            input.directoryId = 999999L;

            mockMvc.perform(post("/api/notes")
                            .cookie(jwtCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("403 - Création dans le dossier d'un autre")
        void shouldFail_WhenDirectoryForbidden() throws Exception {
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

        @Test
        @DisplayName("500 - Body JSON malformé")
        void shouldFail_WhenJsonIsMalformed() throws Exception {
            String badJson = "{ \"name\": \"Test\", \"directoryId\": ";

            mockMvc.perform(post("/api/notes")
                            .cookie(jwtCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(badJson))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("UPDATE (PUT)")
    class UpdateOperations {

        @Test
        @DisplayName("204 - Mise à jour Contenu")
        void shouldUpdateContent() throws Exception {
            DbNotes savedNote = createNoteInDb("Draft", "Old");
            UpdateNotesInput input = new UpdateNotesInput();

            input.name = "Draft";
            input.content = "New";

            mockMvc.perform(put("/api/notes/{id}", savedNote.id)
                            .cookie(jwtCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isNoContent());

            DbNotes updated = notesRepository.findById(savedNote.id).get();
            assertEquals("New", updated.content);
        }

        @Test
        @DisplayName("204 - Mise à jour Nom")
        void shouldUpdateName() throws Exception {
            DbNotes savedNote = createNoteInDb("Draft", "Content");
            UpdateNotesInput input = new UpdateNotesInput();
            input.name = "Final";
            input.content = "Content";

            mockMvc.perform(put("/api/notes/{id}", savedNote.id)
                            .cookie(jwtCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isNoContent());

            DbNotes updated = notesRepository.findById(savedNote.id).get();
            assertEquals("Final", updated.name);
        }

        @Test
        @DisplayName("Sécurité - Impossible de changer le propriétaire (userId)")
        void shouldIgnoreUserIdChange() throws Exception {
            DbNotes savedNote = createNoteInDb("My Note", "Content");
            UpdateNotesInput input = new UpdateNotesInput();
            input.userId = 9999L;
            input.name = "Still Mine";
            input.content = "Content";

            mockMvc.perform(put("/api/notes/{id}", savedNote.id)
                            .cookie(jwtCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isNoContent());

            DbNotes updated = notesRepository.findById(savedNote.id).get();
            assertEquals(savedUser.id, updated.userId, "L'utilisateur ne doit pas changer");
        }

        @Test
        @DisplayName("Intégrité - Impossible de déplacer la note via Update (directoryId ignoré)")
        void shouldIgnoreDirectoryChange() throws Exception {
            // 1. On crée une note de base
            DbNotes savedNote = createNoteInDb("My Note", "Content");

            // 2. On crée un autre dossier
            DbDirectories otherDir = new DbDirectories();
            otherDir.name = "Other";
            otherDir.userId = savedUser.id;
            otherDir.parentDirectoryId = savedRootDirectory.id;
            otherDir.createdAt = java.time.LocalDateTime.now();
            otherDir = directoriesRepository.save(otherDir);

            // 3. JSON manuel avec 'content'
            String maliciousJson = """
                {
                    "name": "Moved?",
                    "content": "Content",
                    "directoryId": %d
                }
            """.formatted(otherDir.id);

            // 4. Envoi
            mockMvc.perform(put("/api/notes/{id}", savedNote.id)
                            .cookie(jwtCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(maliciousJson))
                    .andExpect(status().isNoContent());

            // 5. Vérif
            DbNotes updated = notesRepository.findById(savedNote.id).orElseThrow();
            assertEquals("Moved?", updated.name);
            assertEquals(savedDirectory.id, updated.directoryId, "La note ne doit pas avoir changé de dossier !");
        }

        @Test
        @DisplayName("404 - Update note inexistante")
        void shouldFail_WhenUpdateNonExistent() throws Exception {
            UpdateNotesInput input = new UpdateNotesInput();
            input.name = "Ghost";
            input.content = "Boo";

            mockMvc.perform(put("/api/notes/{id}", 999999L)
                            .cookie(jwtCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("403 - Update note d'autrui")
        void shouldFail_WhenUpdateForbidden() throws Exception {
            DbNotes hackerNote = createHackerNote();
            UpdateNotesInput input = new UpdateNotesInput();
            input.name = "Hacked";
            input.content = "I am here";

            mockMvc.perform(put("/api/notes/{id}", hackerNote.id)
                            .cookie(jwtCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE (DELETE)")
    class DeleteOperations {

        @Test
        @DisplayName("204 - Suppression standard")
        void shouldDeleteNote() throws Exception {
            DbNotes savedNote = createNoteInDb("Delete Me", "");

            mockMvc.perform(delete("/api/notes/{id}", savedNote.id).cookie(jwtCookie))
                    .andExpect(status().isNoContent());

            assertEquals(0, notesRepository.count());
        }

        @Test
        @DisplayName("404 - Suppression note inexistante")
        void shouldFail_DeleteNonExistent() throws Exception {
            mockMvc.perform(delete("/api/notes/{id}", 999999L).cookie(jwtCookie))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("403 - Suppression note d'autrui")
        void shouldFail_DeleteForbidden() throws Exception {
            DbNotes hackerNote = createHackerNote();
            mockMvc.perform(delete("/api/notes/{id}", hackerNote.id).cookie(jwtCookie))
                    .andExpect(status().isForbidden());
        }
    }
}