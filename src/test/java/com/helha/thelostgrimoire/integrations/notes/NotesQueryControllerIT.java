package com.helha.thelostgrimoire.integrations.notes;

import com.helha.thelostgrimoire.infrastructure.notes.DbNotes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("🔍 Tests d'Intégration - Notes Queries (GET)")
class NotesQueryControllerIT extends AbstractNotesIT {

    @Nested
    @DisplayName("📖 READ (GET)")
    class ReadOperations {

        @Test
        @DisplayName("200 - Récupérer MES notes uniquement (filtrage par user)")
        void shouldGetOnlyMyNotes() throws Exception {
            // GIVEN : 2 notes à moi
            createNoteInDb("My Note 1", "C1");
            createNoteInDb("My Note 2", "C2");

            // ET : 1 note d'un autre utilisateur (le "Hacker")
            createHackerNote();

            // WHEN : J'appelle /api/notes/me
            mockMvc.perform(get("/api/notes/me").cookie(jwtCookie))
                    .andExpect(status().isOk())
                    // THEN : Je ne dois recevoir que mes 2 notes, pas celle du hacker (total 3 en base, mais 2 reçues)
                    .andExpect(jsonPath("$.notes", hasSize(2)))
                    .andExpect(jsonPath("$.notes[0].name", is("My Note 1")))
                    .andExpect(jsonPath("$.notes[1].name", is("My Note 2")));
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
            // On vérifie que la note est bien trouvée dans le dossier
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

    @Nested
    @DisplayName("Metadata & Stats")
    class MetadataTests {

        @Test
        @DisplayName("200 - [Legacy] Vérification métadonnées via GET /id")
        void shouldReturnCorrectMetadata_OnStandardGet() throws Exception {
            String content = "Hello World\nTest"; // 3 mots, 2 lignes, 16 chars
            DbNotes savedNote = createNoteInDb("Meta Note", content);

            mockMvc.perform(get("/api/notes/{id}", savedNote.id)
                            .cookie(jwtCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.wordCount", is(3)))
                    .andExpect(jsonPath("$.lineCount", is(2)))
                    .andExpect(jsonPath("$.byteSize", is(16)));
        }

        @Test
        @DisplayName("200 - [New Endpoint] Récupérer métadonnées via /metadata (Léger)")
        void shouldReturnMetadata_OnDedicatedEndpoint() throws Exception {
            // GIVEN
            String content = "Lorem Ipsum"; // 2 mots, 1 ligne, 11 chars
            DbNotes savedNote = createNoteInDb("Lightweight Note", content);

            // WHEN : Appel de la route dédiée /metadata
            mockMvc.perform(get("/api/notes/{id}/metadata", savedNote.id)
                            .cookie(jwtCookie))
                    .andExpect(status().isOk())
                    // THEN : Les stats sont là
                    .andExpect(jsonPath("$.id", is(savedNote.id.intValue())))
                    .andExpect(jsonPath("$.name", is("Lightweight Note")))
                    .andExpect(jsonPath("$.wordCount", is(2)))
                    .andExpect(jsonPath("$.characterCount", is(11)))
                    // ET SURTOUT : Le contenu n'est PAS là (ou null) car c'est un DTO léger
                    .andExpect(jsonPath("$.content").doesNotExist());
        }

        @Test
        @DisplayName("200 - Vérification métadonnées sur contenu vide")
        void shouldReturnZeroMetadata_WhenContentIsEmpty() throws Exception {
            DbNotes savedNote = createNoteInDb("Empty Note", "");

            mockMvc.perform(get("/api/notes/{id}/metadata", savedNote.id)
                            .cookie(jwtCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.wordCount", is(0)))
                    .andExpect(jsonPath("$.byteSize", is(0)));
        }
    }
}