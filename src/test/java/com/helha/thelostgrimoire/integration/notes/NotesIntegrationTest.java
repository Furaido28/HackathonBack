package com.helha.thelostgrimoire.integration.notes;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.show-sql=true"
        }
)
@AutoConfigureMockMvc
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

        // 1. User
        DbUsers user = new DbUsers();
        user.name = "Doe";
        user.firstname = "John";
        user.emailAddress = "john.doe@example.com";
        user.hashPassword = "password123";
        user.createdAt = LocalDateTime.now();
        savedUser = usersRepository.save(user);

        // 2. Directory
        DbDirectories dir = new DbDirectories();
        dir.name = "My Directory";
        dir.userId = savedUser.id;
        dir.createdAt = LocalDateTime.now();
        savedDirectory = directoriesRepository.save(dir);

        // 3. JWT
        Users domainUser = UserMapper.toDomain(savedUser);
        String token = jwtService.generateToken(domainUser);
        jwtCookie = new Cookie("jwt", token);
    }

    // --- NOUVEAU TEST 1 : Récupérer toutes les notes ---
    @Test
    void shouldGetAllNotes() throws Exception {
        // Given : 2 notes en base
        createNoteInDb("Note 1");
        createNoteInDb("Note 2");

        // When/Then
        mockMvc.perform(get("/api/notes")
                        .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes", hasSize(2))) // Vérifie qu'on a bien 2 notes
                .andExpect(jsonPath("$.notes[0].name", is("Note 1")));
    }

    // --- NOUVEAU TEST 2 : Récupérer les notes par dossier ---
    @Test
    void shouldGetNotesByDirectory() throws Exception {
        // Given : 2 notes dans le "savedDirectory"
        createNoteInDb("DirNote A");
        createNoteInDb("DirNote B");

        // When/Then
        mockMvc.perform(get("/api/notes/directory/{directoryId}", savedDirectory.id)
                        .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes", hasSize(2)))
                .andExpect(jsonPath("$.notes[0].name", is("DirNote A")));
    }

    @Test
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
    void shouldGetNoteById() throws Exception {
        DbNotes savedNote = createNoteInDb("Existing Note");

        mockMvc.perform(get("/api/notes/{id}", savedNote.id)
                        .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Existing Note")));
    }

    @Test
    void shouldUpdateNote() throws Exception {
        DbNotes savedNote = createNoteInDb("Old Name");

        UpdateNotesInput updateInput = new UpdateNotesInput();
        updateInput.name = "New Name";
        updateInput.content = "New Content";

        mockMvc.perform(put("/api/notes/{id}", savedNote.id)
                        .cookie(jwtCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateInput)))
                .andExpect(status().isNoContent());

        DbNotes updated = notesRepository.findById(savedNote.id).orElseThrow();
        assert updated.name.equals("New Name");
    }

    @Test
    void shouldDeleteNote() throws Exception {
        DbNotes savedNote = createNoteInDb("To Delete");

        mockMvc.perform(delete("/api/notes/{id}", savedNote.id)
                        .cookie(jwtCookie))
                .andExpect(status().isNoContent());

        assert notesRepository.findById(savedNote.id).isEmpty();
    }

    // Helper pour éviter de dupliquer le code de création de note
    private DbNotes createNoteInDb(String name) {
        DbNotes note = new DbNotes();
        note.name = name;
        note.content = "Content";
        note.directoryId = savedDirectory.id;
        note.userId = savedUser.id;
        note.createdAt = LocalDateTime.now();
        note.updatedAt = LocalDateTime.now();
        return notesRepository.save(note);
    }
}