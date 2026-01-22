package com.helha.thelostgrimoire.integrations.exports;

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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.show-sql=true"
        }
)
@AutoConfigureMockMvc
public abstract class AbstractExportIT {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;

    @Autowired protected IUsersRepository usersRepository;
    @Autowired protected IDirectoriesRepository directoriesRepository;
    @Autowired protected INotesRepository notesRepository;

    @Autowired protected JwtService jwtService;

    @BeforeEach
    void cleanDb() {
        notesRepository.deleteAll();
        directoriesRepository.deleteAll();
        usersRepository.deleteAll();
    }

    protected DbUsers persistUserRawForJwt(String email) {
        DbUsers user = new DbUsers();
        user.name = "Doe";
        user.firstname = "John";
        user.emailAddress = email;
        user.hashPassword = "password123";
        user.createdAt = LocalDateTime.now();
        return usersRepository.save(user);
    }

    protected Cookie jwtCookieFor(DbUsers savedUser) {
        Users domainUser = UserMapper.toDomain(savedUser);
        String token = jwtService.generateToken(domainUser);
        return new Cookie("jwt", token);
    }

    protected DbDirectories persistRootDirectory(Long userId) {
        DbDirectories root = new DbDirectories();
        root.userId = userId;
        root.isRoot = true;
        root.name = "root";
        root.createdAt = LocalDateTime.now();
        return directoriesRepository.save(root);
    }

    protected DbDirectories persistDirectory(Long userId, String name, Long parentDirectoryId) {
        DbDirectories dir = new DbDirectories();
        dir.userId = userId;
        dir.isRoot = false;
        dir.name = name;
        dir.parentDirectoryId = parentDirectoryId; // peut être null si autorisé
        dir.createdAt = LocalDateTime.now();
        return directoriesRepository.save(dir);
    }

    protected DbNotes persistNote(Long userId, Long directoryId, String name, String content) {
        DbNotes note = new DbNotes();
        note.userId = userId;
        note.directoryId = directoryId;
        note.name = name;
        note.content = content;
        note.createdAt = LocalDateTime.now();
        note.updatedAt = LocalDateTime.now();
        return notesRepository.save(note);
    }

    protected ResultActions performAndMaybeAsyncDispatch(MvcResult mvcResult) throws Exception {
        if (mvcResult.getRequest().isAsyncStarted()) {
            return mockMvc.perform(asyncDispatch(mvcResult));
        }
        return null;
    }
}