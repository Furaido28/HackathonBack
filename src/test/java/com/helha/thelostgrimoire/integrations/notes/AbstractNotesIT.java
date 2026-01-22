package com.helha.thelostgrimoire.integrations.notes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helha.thelostgrimoire.TestcontainersConfiguration;
import com.helha.thelostgrimoire.application.repositories.users.UserMapper;
import com.helha.thelostgrimoire.domain.models.Users;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.directories.IDirectoriesRepository;
import com.helha.thelostgrimoire.infrastructure.notes.DbNotes;
import com.helha.thelostgrimoire.infrastructure.notes.INotesRepository;
import com.helha.thelostgrimoire.infrastructure.users.DbUsers;
import com.helha.thelostgrimoire.infrastructure.users.IUsersRepository;
import com.helha.thelostgrimoire.security.JwtService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
public abstract class AbstractNotesIT {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected IUsersRepository usersRepository;
    @Autowired protected IDirectoriesRepository directoriesRepository;
    @Autowired protected INotesRepository notesRepository;
    @Autowired protected JwtService jwtService;

    protected Cookie jwtCookie;
    protected DbUsers savedUser;
    protected DbDirectories savedDirectory;

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

    // ==========================================
    // 🛠️ HELPERS (Protected pour l'héritage)
    // ==========================================
    protected DbNotes createNoteInDb(String name, String content) {
        DbNotes note = new DbNotes();
        note.name = name;
        note.content = content;
        note.directoryId = savedDirectory.id;
        note.userId = savedUser.id;
        note.createdAt = LocalDateTime.now();
        note.updatedAt = LocalDateTime.now();
        return notesRepository.save(note);
    }

    protected DbNotes createHackerNote() {
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