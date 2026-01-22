package com.helha.thelostgrimoire.integrations.directories;

import com.helha.thelostgrimoire.application.repositories.users.UserMapper;
import com.helha.thelostgrimoire.domain.models.Users;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.users.DbUsers;
import com.helha.thelostgrimoire.security.JwtService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Directories - Command Controller IT")
public class DirectoriesCommandControllerIT extends AbstractDirectoriesIT {

    @Autowired private JwtService jwtService;

    private Cookie jwtCookie;
    private DbUsers savedUser;
    private DbDirectories savedRoot;

    @BeforeEach
    void setUpJwt() {
        // cleanup
        directoriesRepository.deleteAll();
        usersRepository.deleteAll();

        DbUsers user = new DbUsers();
        user.name = "Doe";
        user.firstname = "John";
        user.emailAddress = "john.doe@example.com";
        user.hashPassword = "password123";
        user.createdAt = LocalDateTime.now();
        savedUser = usersRepository.save(user);

        savedRoot = persistRoot(savedUser.id);

        Users domainUser = UserMapper.toDomain(savedUser);
        String token = jwtService.generateToken(domainUser);
        jwtCookie = new Cookie("jwt", token);
    }

    @Nested
    @DisplayName("POST /api/directories")
    class CreateDirectory {

        @Test
        @DisplayName("201 - répertoire créé avec succès")
        void createDirectory_shouldReturnCreated() throws Exception {
            String jsonRequest = """
                {
                    "name": "Mes documents",
                    "parentDirectoryId": null
                }
                """;

            mockMvc.perform(post("/api/directories")
                            .cookie(jwtCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Mes documents"));

            DbDirectories created = directoriesRepository.findAll().stream()
                    .filter(d -> d.name.equals("Mes documents"))
                    .findFirst().orElseThrow();

            assertEquals(savedRoot.id, created.parentDirectoryId);
        }

        @Test
        @DisplayName("201 - répertoire créé avec parent")
        void createDirectory_withParent_shouldReturnCreated() throws Exception {
            DbDirectories parent = persist(savedUser.id, "Parent", null);

            String jsonRequest = """
                {
                    "name": "Sous-dossier",
                    "parentDirectoryId": %d
                }
                """.formatted(parent.id);

            mockMvc.perform(post("/api/directories")
                            .cookie(jwtCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Sous-dossier"));
        }

        @Test
        @DisplayName("400 - nom vide")
        void createDirectory_withEmptyName_shouldReturnBadRequest() throws Exception {
            String jsonRequest = """
                {
                    "name": "",
                    "parentDirectoryId": null
                }
                """;

            mockMvc.perform(post("/api/directories")
                            .cookie(jwtCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("403 - tentative d'utilisation d'un parent d'un autre utilisateur")
        void createDirectory_withOtherUserParent_shouldReturnForbidden() throws Exception {
            DbDirectories otherUserParent = persist(99L, "Autre utilisateur", null);

            String jsonRequest = """
                {
                    "name": "Tentative accès non autorisé",
                    "parentDirectoryId": %d
                }
                """.formatted(otherUserParent.id);

            mockMvc.perform(post("/api/directories")
                            .cookie(jwtCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/directories")
    class UpdateDirectory {

        @Test
        @DisplayName("204 - répertoire mis à jour")
        void updateDirectory_shouldReturnNoContent() throws Exception {
            DbDirectories directory = persist(savedUser.id, "Ancien nom", null);

            String jsonRequest = """
                {
                    "id": %d,
                    "name": "Nouveau nom"
                }
                """.formatted(directory.id);

            mockMvc.perform(put("/api/directories")
                            .cookie(jwtCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("DELETE /api/directories/{directoryId}")
    class DeleteDirectory {

        @Test
        @DisplayName("204 - répertoire standard supprimé")
        void deleteDirectory_shouldReturnNoContent() throws Exception {
            DbDirectories directory = persist(savedUser.id, "À supprimer", savedRoot.id);

            mockMvc.perform(delete("/api/directories/{directoryId}", directory.id)
                            .cookie(jwtCookie))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("400 - Tentative de suppression du dossier RACINE")
        void deleteRoot_shouldReturnBadRequest() throws Exception {
            mockMvc.perform(delete("/api/directories/{directoryId}", savedRoot.id)
                            .cookie(jwtCookie))
                    .andExpect(status().isBadRequest()); // Ou 403 selon ton implémentation exacte, mais tu avais mis BAD_REQUEST
        }
    }
}