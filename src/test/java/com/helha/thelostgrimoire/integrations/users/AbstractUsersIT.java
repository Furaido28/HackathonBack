package com.helha.thelostgrimoire.integrations.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helha.thelostgrimoire.TestcontainersConfiguration;
import com.helha.thelostgrimoire.application.repositories.users.UserMapper;
import com.helha.thelostgrimoire.domain.models.Users;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.directories.IDirectoriesRepository;
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
                "spring.jpa.show-sql=false"
        }
)
@AutoConfigureMockMvc
public abstract class AbstractUsersIT {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;

    @Autowired protected IUsersRepository usersRepository;
    @Autowired protected IDirectoriesRepository directoriesRepository;
    @Autowired protected JwtService jwtService;

    @BeforeEach
    void cleanDb() {
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
        DbUsers savedUser = usersRepository.save(user);

        DbDirectories root = new DbDirectories();
        root.userId = savedUser.id;
        root.name = "root";
        root.isRoot = true;
        root.parentDirectoryId = null;
        root.createdAt = LocalDateTime.now();
        directoriesRepository.save(root);

        return savedUser;
    }

    protected Cookie jwtCookieFor(DbUsers savedUser) {
        Users domainUser = UserMapper.toDomain(savedUser);
        String token = jwtService.generateToken(domainUser);
        return new Cookie("jwt", token);
    }
}