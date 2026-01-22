package com.helha.thelostgrimoire.integrations.users;

import com.helha.thelostgrimoire.application.repositories.users.command.login.LoginInput;
import com.helha.thelostgrimoire.application.repositories.users.command.register.RegisterInput;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.users.DbUsers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    @Nested
    @DisplayName("REGISTER (POST)")
    class RegisterOperations {

        @Test
        @DisplayName("201 - Inscription réussie + Création Racine")
        void shouldRegisterSuccessfully() throws Exception {
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

            // Vérification side-effect (Dossier Root)
            DbUsers createdUser = usersRepository.findByEmailAddress("john.doe@example.com").orElseThrow();
            Optional<DbDirectories> rootDir = directoriesRepository.findByUserIdAndIsRootTrue(createdUser.id);
            assertTrue(rootDir.isPresent());
            assertTrue(rootDir.get().isRoot);
        }

        @Test
        @DisplayName("400/409 - Échec si l'email existe déjà")
        void shouldFail_WhenEmailAlreadyExists() throws Exception {
            // GIVEN : Un utilisateur existe déjà
            persistUserRawForJwt("duplicate@test.com");

            // WHEN : On essaie de s'inscrire avec le même email
            RegisterInput input = new RegisterInput();
            input.name = "Another";
            input.firstName = "User";
            input.email = "duplicate@test.com";
            input.password = "pass";

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    // THEN : On attend une erreur (400 Bad Request ou 409 Conflict selon ton implémentation)
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("400 - Échec si champs manquants (Validation)")
        void shouldFail_WhenFieldsAreMissing() throws Exception {
            RegisterInput input = new RegisterInput();
            // Nom et Password manquants
            input.email = "invalid";

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("LOGIN (POST)")
    class LoginOperations {

        @Test
        @DisplayName("200 - Login réussi + Cookie JWT")
        void shouldLoginSuccessfully() throws Exception {
            // On enregistre via l'API pour être sûr que le hash du mot de passe est correct
            RegisterInput reg = new RegisterInput();
            reg.name = "Doe";
            reg.firstName = "John";
            reg.email = "login@test.com";
            reg.password = "secret123";

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reg)))
                    .andExpect(status().isCreated());

            // Login
            LoginInput login = new LoginInput("login@test.com", "secret123");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("jwt=")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")));
        }

        @Test
        @DisplayName("401/403 - Échec si mauvais mot de passe")
        void shouldFail_WhenPasswordIsWrong() throws Exception {
            // Inscription
            RegisterInput reg = new RegisterInput();
            reg.name = "Doe";
            reg.firstName = "John";
            reg.email = "wrongpass@test.com";
            reg.password = "goodpass";
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reg)));

            // Tentative avec mauvais MDP
            LoginInput login = new LoginInput("wrongpass@test.com", "badpass");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().is4xxClientError()); // Souvent 401 ou 403
        }

        @Test
        @DisplayName("404/401 - Échec si email inconnu")
        void shouldFail_WhenUserNotFound() throws Exception {
            LoginInput login = new LoginInput("ghost@test.com", "any");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("LOGOUT (DELETE)")
    class LogoutOperations {
        @Test
        @DisplayName("204 - Logout expire le cookie")
        void logout_shouldExpireCookie() throws Exception {
            mockMvc.perform(delete("/api/auth/logout"))
                    .andExpect(status().isNoContent())
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("jwt=")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
        }
    }
}