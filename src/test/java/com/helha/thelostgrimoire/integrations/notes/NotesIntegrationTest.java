package com.helha.thelostgrimoire.integrations.notes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helha.thelostgrimoire.TestcontainersConfiguration;
import com.helha.thelostgrimoire.application.notes.command.create.CreateNotesInput;
import com.helha.thelostgrimoire.application.notes.command.update.UpdateNotesInput;
import com.helha.thelostgrimoire.application.users.UserMapper;
import com.helha.thelostgrimoire.domain.Users;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.directories.IDirectoriesRepository;
import com.helha.thelostgrimoire.infrastructure.notes.DbNotes;
import com.helha.thelostgrimoire.infrastructure.notes.INotesRepository;
import com.helha.thelostgrimoire.infrastructure.users.DbUsers;
import com.helha.thelostgrimoire.infrastructure.users.IUsersRepository;
import com.helha.thelostgrimoire.security.JwtService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("🧪 Tests d'Intégration - Notes (Trié par CRUD)")
@Import(TestcontainersConfiguration.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.show-sql=false"
        }
)
@AutoConfigureMockMvc
@Transactional
public class NotesIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private IUsersRepository usersRepository;
    @Autowired private IDirectoriesRepository directoriesRepository;
    @Autowired private INotesRepository notesRepository;
    @Autowired private JwtService jwtService;

    private Cookie jwtCookie;
    private DbUsers savedUser;
    private DbDirectories savedDirectory;

    @BeforeEach
    void setUp() {
        notesRepository.deleteAll();
        directoriesRepository.deleteAll();
        usersRepository.deleteAll();

        // 1. Création User (John)
        DbUsers user = new DbUsers();
        user.name = "Doe";
        user.firstname = "John";
        user.emailAddress = "john.doe@example.com";
        user.hashPassword = "password123";
        user.createdAt = LocalDateTime.now();
        savedUser = usersRepository.save(user);

        // 2. Création Directory
        DbDirectories dir = new DbDirectories();
        dir.name = "My Directory";
        dir.userId = savedUser.id;
        dir.createdAt = LocalDateTime.now();
        savedDirectory = directoriesRepository.save(dir);

        // 3. Token JWT
        Users domainUser = UserMapper.toDomain(savedUser);
        String token = jwtService.generateToken(domainUser);
        jwtCookie = new Cookie("jwt", token);
    }

    // ===================================================================================
    // 📝 CREATE OPERATIONS (POST)
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
            DbNotes hackerNote = createHackerNote(); // Pour avoir un dossier hacker
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
    // 📖 READ OPERATIONS (GET)
    // ===================================================================================
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

    // ===================================================================================
    // 💾 UPDATE OPERATIONS (PUT)
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
    // 🗑️ DELETE OPERATIONS (DELETE)
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

    // ==========================================
    // 🛠️ HELPERS
    // ==========================================
    private DbNotes createNoteInDb(String name, String content) {
        DbNotes note = new DbNotes();
        note.name = name;
        note.content = content;
        note.directoryId = savedDirectory.id;
        note.userId = savedUser.id;
        note.createdAt = LocalDateTime.now();
        note.updatedAt = LocalDateTime.now();
        return notesRepository.save(note);
    }

    private DbNotes createHackerNote() {
        DbUsers hacker = new DbUsers();
        hacker.name = "Hacker";
        hacker.firstname = "Bob";
        hacker.emailAddress = "hacker@test.com";
        hacker.hashPassword = "hash";
        hacker.createdAt = LocalDateTime.now();
        hacker = usersRepository.save(hacker);

        DbDirectories hackerDir = new DbDirectories();
        hackerDir.name = "Hacker Dir";
        hackerDir.userId = hacker.id;
        hackerDir.createdAt = LocalDateTime.now();
        hackerDir = directoriesRepository.save(hackerDir);

        DbNotes hackerNote = new DbNotes();
        hackerNote.name = "Secret Hacker Note";
        hackerNote.content = "Secret";
        hackerNote.userId = hacker.id;
        hackerNote.directoryId = hackerDir.id;
        hackerNote.createdAt = LocalDateTime.now();
        return notesRepository.save(hackerNote);
    }
}