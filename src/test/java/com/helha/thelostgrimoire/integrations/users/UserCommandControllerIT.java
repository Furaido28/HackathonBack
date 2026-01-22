package com.helha.thelostgrimoire.integrations.users;

import com.helha.thelostgrimoire.application.users.command.login.LoginInput;
import com.helha.thelostgrimoire.application.users.command.register.RegisterInput;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.users.DbUsers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Auth - Command Controller IT")
public class UserCommandControllerIT extends AbstractUsersIT {

    @Test
    @DisplayName("POST /api/auth/register - 201 - crée un user ET son dossier racine")
    void register_shouldReturnCreated() throws Exception {
        RegisterInput input = new RegisterInput();
        input.name = "Doe";
        input.firstName = "John";
        input.email = "john.doe@example.com";
        input.password = "password123";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email", is("john.doe@example.com")));

        // --- VERIFICATION SUPPLEMENTAIRE ---
        // On vérifie que le side-effect (création du dossier root) a bien eu lieu
        DbUsers createdUser = usersRepository.findByEmailAddress("john.doe@example.com").orElseThrow();
        Optional<DbDirectories> rootDir = directoriesRepository.findByUserIdAndIsRootTrue(createdUser.id);

        assertTrue(rootDir.isPresent(), "Le dossier racine aurait dû être créé automatiquement à l'inscription");
        assertTrue(rootDir.get().isRoot);
    }

    @Test
    @DisplayName("POST /api/auth/login - 200 - renvoie un cookie jwt (HttpOnly)")
    void login_shouldReturnOk_andSetJwtCookie() throws Exception {
        // Arrange: register d'abord
        RegisterInput reg = new RegisterInput();
        reg.name = "Doe";
        reg.firstName = "John";
        reg.email = "john.doe@example.com";
        reg.password = "password123";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginInput login = new LoginInput("john.doe@example.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("jwt=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")));
    }

    @Test
    @DisplayName("DELETE /api/auth/logout - 204 - expire le cookie jwt")
    void logout_shouldReturnNoContent_andExpireCookie() throws Exception {
        mockMvc.perform(delete("/api/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("jwt=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
    }
}