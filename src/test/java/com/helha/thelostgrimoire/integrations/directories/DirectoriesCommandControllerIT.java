package com.helha.thelostgrimoire.integrations.directories;

import com.helha.thelostgrimoire.application.users.UserMapper;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Directories - Command Controller IT")
public class DirectoriesCommandControllerIT extends AbstractDirectoriesIT {

    @Autowired private JwtService jwtService;

    private Cookie jwtCookie;
    private DbUsers savedUser;

    @BeforeEach
    void setUpJwt() {
        // cleanup
        directoriesRepository.deleteAll();
        usersRepository.deleteAll();

        // 1) User en DB (id = utilisé comme "42" dans tes tests)
        DbUsers user = new DbUsers();
        user.name = "Doe";
        user.firstname = "John";
        user.emailAddress = "john.doe@example.com";
        user.hashPassword = "password123";
        user.createdAt = LocalDateTime.now();

        savedUser = usersRepository.save(user);

        // 2) JWT via via JWTService
        Users domainUser = UserMapper.toDomain(savedUser);
        String token = jwtService.generateToken(domainUser);

        // 3) Cookie jwt
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
                    .andExpect(header().exists("Location"))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.name").value("Mes documents"));
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
        @DisplayName("204 - répertoire supprimé")
        void deleteDirectory_shouldReturnNoContent() throws Exception {
            DbDirectories directory = persist(savedUser.id, "À supprimer", null);

            mockMvc.perform(delete("/api/directories/{directoryId}", directory.id)
                            .cookie(jwtCookie))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("204 - suppression cascade: supprimer parent supprime enfant")
        void deleteDirectory_withChildren_shouldCascadeDelete_andReturnNoContent() throws Exception {
            DbDirectories parent = persist(savedUser.id, "Parent", null);
            DbDirectories child = persist(savedUser.id, "Child", parent.id);

            mockMvc.perform(delete("/api/directories/{directoryId}", parent.id)
                            .cookie(jwtCookie))
                    .andExpect(status().isNoContent());

            assertFalse(directoriesRepository.existsById(parent.id));
            assertFalse(directoriesRepository.existsById(child.id));
        }
    }
}