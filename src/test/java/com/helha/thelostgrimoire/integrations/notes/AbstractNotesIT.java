package com.helha.thelostgrimoire.integrations.notes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helha.thelostgrimoire.TestcontainersConfiguration;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.show-sql=true"
        }
)
@AutoConfigureMockMvc
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

        // 1) User
        DbUsers user = new DbUsers();
        user.name = "Doe";
        user.firstname = "John";
        user.emailAddress = "john.doe@example.com";
        user.hashPassword = "password123";
        user.createdAt = LocalDateTime.now();
        savedUser = usersRepository.save(user);

        // 2) Directory
        DbDirectories dir = new DbDirectories();
        dir.name = "My Directory";
        dir.userId = savedUser.id;
        dir.createdAt = LocalDateTime.now();
        savedDirectory = directoriesRepository.save(dir);

        // 3) JWT cookie
        Users domainUser = UserMapper.toDomain(savedUser);
        String token = jwtService.generateToken(domainUser);
        jwtCookie = new Cookie("jwt", token);
    }

    protected DbNotes createNoteInDb(String name) {
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
