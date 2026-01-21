package com.helha.thelostgrimoire.integrations.directories;

import com.helha.thelostgrimoire.application.users.UserMapper;
import com.helha.thelostgrimoire.domain.models.Users;
import com.helha.thelostgrimoire.infrastructure.users.DbUsers;
import com.helha.thelostgrimoire.infrastructure.users.IUsersRepository;
import com.helha.thelostgrimoire.security.JwtService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Directories - Query Controller IT")
public class DirectoriesQueryControllerIT extends AbstractDirectoriesIT {

    @Autowired private IUsersRepository usersRepository;
    @Autowired private JwtService jwtService;

    private Cookie jwtCookie;
    private DbUsers savedUser;

    @BeforeEach
    void setUpJwt() {
        // Optionnel si AbstractDirectoriesIT nettoie déjà, mais safe
        directoriesRepository.deleteAll();
        usersRepository.deleteAll();

        // Crée l'user "connecté"
        DbUsers user = new DbUsers();
        user.name = "Doe";
        user.firstname = "John";
        user.emailAddress = "john.doe@example.com";
        user.hashPassword = "password123";
        user.createdAt = LocalDateTime.now();
        savedUser = usersRepository.save(user);

        // JWT cookie
        Users domainUser = UserMapper.toDomain(savedUser);
        String token = jwtService.generateToken(domainUser);
        jwtCookie = new Cookie("jwt", token);
    }

    @Test
    @DisplayName("GET /api/directories - 200 - retourne tous les répertoires (output wrapper)")
    void getAll_shouldReturnOk() throws Exception {
        persist(savedUser.id, "Docs", null);
        persist(99L, "Other", null);

        mockMvc.perform(get("/api/directories")
                        .cookie(jwtCookie))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/directories/me - 200 - retourne seulement les répertoires du user connecté")
    void getMyDirectories_shouldReturnOnlyMine() throws Exception {
        persist(savedUser.id, "Documents", null);
        persist(savedUser.id, "Images", null);
        persist(99L, "Autre utilisateur", null);

        mockMvc.perform(get("/api/directories/me")
                        .cookie(jwtCookie))
                .andExpect(status().isOk());
    }
}