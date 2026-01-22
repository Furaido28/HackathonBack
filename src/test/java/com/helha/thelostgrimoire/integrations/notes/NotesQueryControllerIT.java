package com.helha.thelostgrimoire.integrations.notes;

import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.notes.DbNotes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("🔍 Tests d'Intégration - Notes Queries (GET)")
class NotesQueryControllerIT extends AbstractNotesIT {

    @Nested
    @DisplayName("READ (GET)")
    class ReadOperations {

        @Test
        @DisplayName("200 - Get Me (Filtre User)")
        void shouldGetOnlyMyNotes() throws Exception {
            createNoteInDb("Mine 1", "A");
            createNoteInDb("Mine 2", "B");
            createHackerNote(); // Note d'un autre user

            mockMvc.perform(get("/api/notes/me").cookie(jwtCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.notes", hasSize(2))) // Ignore celle du hacker
                    .andExpect(jsonPath("$.notes[0].name", is("Mine 1")));
        }

        @Test
        @DisplayName("200 - Get Me (Vérification du tri Alphabétique)")
        void shouldGetNotesSortedByName() throws Exception {
            // GIVEN : On insère des notes dans le désordre (Z, A, B)
            createNoteInDb("Zebra Note", "Content");
            createNoteInDb("Alpha Note", "Content");
            createNoteInDb("Beta Note", "Content");

            // WHEN : On récupère la liste
            mockMvc.perform(get("/api/notes/me").cookie(jwtCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.notes", hasSize(3)))

                    // THEN : On vérifie que c'est bien trié A -> B -> Z
                    .andExpect(jsonPath("$.notes[0].name", is("Alpha Note")))
                    .andExpect(jsonPath("$.notes[1].name", is("Beta Note")))
                    .andExpect(jsonPath("$.notes[2].name", is("Zebra Note")));
        }

        @Test
        @DisplayName("200 - Get Me (Liste vide)")
        void shouldGetEmptyList_WhenNoNotes() throws Exception {
            // Aucune note créée
            mockMvc.perform(get("/api/notes/me").cookie(jwtCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.notes", hasSize(0)));
        }

        @Test
        @DisplayName("200 - Get By ID")
        void shouldGetNoteById() throws Exception {
            DbNotes savedNote = createNoteInDb("Target", "Content");

            mockMvc.perform(get("/api/notes/{id}", savedNote.id).cookie(jwtCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Target")));
        }

        @Test
        @DisplayName("404 - Get By ID (Inexistant)")
        void shouldFail_WhenIdNotFound() throws Exception {
            mockMvc.perform(get("/api/notes/{id}", 99999L).cookie(jwtCookie))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("403 - Get By ID (Interdit)")
        void shouldFail_WhenIdForbidden() throws Exception {
            DbNotes hackerNote = createHackerNote();
            mockMvc.perform(get("/api/notes/{id}", hackerNote.id).cookie(jwtCookie))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("200 - Get By Directory")
        void shouldGetNotesByDirectory() throws Exception {
            createNoteInDb("In Dir", "C");
            mockMvc.perform(get("/api/notes/directory/{id}", savedDirectory.id).cookie(jwtCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.notes", hasSize(1)));
        }

        @Test
        @DisplayName("200 - Get By Directory (Dossier vide)")
        void shouldGetEmptyList_WhenDirEmpty() throws Exception {
            DbDirectories emptyDir = new DbDirectories();
            emptyDir.name = "Empty";
            emptyDir.userId = savedUser.id;
            emptyDir.parentDirectoryId = savedRootDirectory.id;
            emptyDir.createdAt = LocalDateTime.now();
            emptyDir = directoriesRepository.save(emptyDir);

            mockMvc.perform(get("/api/notes/directory/{id}", emptyDir.id).cookie(jwtCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.notes", hasSize(0)));
        }

        @Test
        @DisplayName("404 - Get By Directory (Dir inexistant)")
        void shouldFail_WhenDirNotFound() throws Exception {
            mockMvc.perform(get("/api/notes/directory/{id}", 99999L).cookie(jwtCookie))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("403 - Get By Directory (Dir Interdit)")
        void shouldFail_WhenDirForbidden() throws Exception {
            DbNotes hackerNote = createHackerNote(); // Crée dossier hacker
            mockMvc.perform(get("/api/notes/directory/{id}", hackerNote.directoryId).cookie(jwtCookie))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Metadata & Stats")
    class MetadataTests {

        @Test
        @DisplayName("200 - Metadata (Check DTO léger)")
        void shouldReturnMetadata_WithoutContent() throws Exception {
            DbNotes savedNote = createNoteInDb("Stats", "Some long content");

            mockMvc.perform(get("/api/notes/{id}/metadata", savedNote.id).cookie(jwtCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(savedNote.id.intValue())))
                    .andExpect(jsonPath("$.wordCount", is(3)))
                    .andExpect(jsonPath("$.content").doesNotExist()); // Vérifie l'absence du champ
        }

        @Test
        @DisplayName("200 - Metadata (Note vide)")
        void shouldReturnZeroStats() throws Exception {
            DbNotes savedNote = createNoteInDb("Empty", "");

            mockMvc.perform(get("/api/notes/{id}/metadata", savedNote.id).cookie(jwtCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.wordCount", is(0)))
                    .andExpect(jsonPath("$.byteSize", is(0)));
        }

        @Test
        @DisplayName("404 - Metadata (Note inexistante)")
        void shouldFail_MetaNotFound() throws Exception {
            mockMvc.perform(get("/api/notes/{id}/metadata", 99999L).cookie(jwtCookie))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("403 - Metadata (Note interdite)")
        void shouldFail_MetaForbidden() throws Exception {
            DbNotes hackerNote = createHackerNote();
            mockMvc.perform(get("/api/notes/{id}/metadata", hackerNote.id).cookie(jwtCookie))
                    .andExpect(status().isForbidden());
        }
    }
}