package com.helha.thelostgrimoire.integrations.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helha.thelostgrimoire.TestcontainersConfiguration;
import com.helha.thelostgrimoire.application.users.UserMapper;
import com.helha.thelostgrimoire.domain.Users;
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
public abstract class AbstractUsersIT {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;

    @Autowired protected IUsersRepository usersRepository;
    @Autowired protected JwtService jwtService;

    @BeforeEach
    void cleanDb() {
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
}
