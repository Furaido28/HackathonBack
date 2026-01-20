package com.helha.thelostgrimoire.integrations.directories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.directories.IDirectoriesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class DirectoriesControllerIT {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mySQLContainer =
            new MySQLContainer<>("mysql:8.0");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private IDirectoriesRepository directoriesRepository;

    @BeforeEach
    public void setup() {
        directoriesRepository.deleteAll();
    }

    DbDirectories persist(Long userId, String name, Long parentDirectoryId, boolean isRoot) {
        DbDirectories directory = new DbDirectories();
        directory.userId = userId;
        directory.name = name;
        directory.parentDirectoryId = parentDirectoryId;
        directory.createdAt = LocalDateTime.now();
        return directoriesRepository.save(directory);
    }

    @Nested
    @DisplayName("POST /api/directories")
    class CreateDirectory {

        @Test
        @WithMockUser(username = "42") // Simule un utilisateur avec ID 42
        @DisplayName("201 - répertoire créé avec succès")
        void createDirectory_shouldReturnCreated() throws Exception {
            // Arrange
            String jsonRequest = """
                {
                    "name": "Mes documents",
                    "parentDirectoryId": null
                }
                """;

            // Act & Assert
            mockMvc.perform(post("/api/directories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest)
                    )
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.name").value("Mes documents"));
        }

        @Test
        @WithMockUser(username = "42")
        @DisplayName("201 - répertoire créé avec parent")
        void createDirectory_withParent_shouldReturnCreated() throws Exception {
            // Arrange
            DbDirectories parent = persist(42L, "Parent", null, true);

            String jsonRequest = """
                {
                    "name": "Sous-dossier",
                    "parentDirectoryId": %d
                }
                """.formatted(parent.id);

            // Act & Assert
            mockMvc.perform(post("/api/directories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest)
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Sous-dossier"));
        }

        @Test
        @WithMockUser(username = "42")
        @DisplayName("400 - nom vide")
        void createDirectory_withEmptyName_shouldReturnBadRequest() throws Exception {
            // Arrange
            String jsonRequest = """
                {
                    "name": "",
                    "parentDirectoryId": null
                }
                """;

            // Act & Assert
            mockMvc.perform(post("/api/directories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest)
                    )
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "42")
        @DisplayName("403 - tentative d'utilisation d'un parent d'un autre utilisateur")
        void createDirectory_withOtherUserParent_shouldReturnForbidden() throws Exception {
            // Arrange - Crée un répertoire parent appartenant à un autre utilisateur
            DbDirectories otherUserParent = persist(99L, "Autre utilisateur", null, true);

            String jsonRequest = """
                {
                    "name": "Tentative accès non autorisé",
                    "parentDirectoryId": %d
                }
                """.formatted(otherUserParent.id);

            // Act & Assert
            mockMvc.perform(post("/api/directories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest)
                    )
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/directories")
    class GetDirectories {

        @Test
        @WithMockUser(username = "42")
        @DisplayName("200 - retourne une liste vide")
        void getDirectories_shouldReturnEmptyList() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/directories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @WithMockUser(username = "42")
        @DisplayName("200 - retourne les répertoires de l'utilisateur")
        void getDirectories_shouldReturnUserDirectories() throws Exception {
            // Arrange
            persist(42L, "Documents", null, true);
            persist(42L, "Images", null, true);
            persist(99L, "Autre utilisateur", null, true); // Ne devrait pas apparaître

            // Act & Assert
            mockMvc.perform(get("/api/directories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].name").isString())
                    .andExpect(jsonPath("$[1].name").isString());
        }

        @Test
        @WithMockUser(username = "42")
        @DisplayName("200 - retourne l'arborescence")
        void getDirectoriesTree_shouldReturnHierarchy() throws Exception {
            // Arrange
            DbDirectories root = persist(42L, "Root", null, true);
            DbDirectories child1 = persist(42L, "Child 1", root.id, false);
            DbDirectories child2 = persist(42L, "Child 2", root.id, false);
            persist(42L, "Grandchild", child1.id, false);

            // Act & Assert
            mockMvc.perform(get("/api/directories/tree"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1))) // Un seul root
                    .andExpect(jsonPath("$[0].children", hasSize(2))); // Deux enfants
        }
    }

    @Nested
    @DisplayName("GET /api/directories/{directoryId}")
    class GetDirectoryById {

        @Test
        @WithMockUser(username = "42")
        @DisplayName("200 - retourne le répertoire")
        void getDirectoryById_shouldReturnDirectory() throws Exception {
            // Arrange
            DbDirectories directory = persist(42L, "Mon dossier", null, true);

            // Act & Assert
            mockMvc.perform(get("/api/directories/{id}", directory.id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(directory.id))
                    .andExpect(jsonPath("$.name").value("Mon dossier"))
                    .andExpect(jsonPath("$.userId").value(42));
        }

        @Test
        @WithMockUser(username = "42")
        @DisplayName("404 - répertoire non trouvé")
        void getDirectoryById_notFound_shouldReturn404() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/directories/{id}", 999L))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "42")
        @DisplayName("403 - accès à un répertoire d'un autre utilisateur")
        void getDirectoryById_otherUser_shouldReturnForbidden() throws Exception {
            // Arrange
            DbDirectories otherUserDir = persist(99L, "Autre utilisateur", null, true);

            // Act & Assert
            mockMvc.perform(get("/api/directories/{id}", otherUserDir.id))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/directories")
    class UpdateDirectory {

        @Test
        @WithMockUser(username = "42")
        @DisplayName("204 - répertoire mis à jour")
        void updateDirectory_shouldReturnNoContent() throws Exception {
            // Arrange
            DbDirectories directory = persist(42L, "Ancien nom", null, true);

            String jsonRequest = """
                {
                    "id": %d,
                    "name": "Nouveau nom"
                }
                """.formatted(directory.id);

            // Act & Assert
            mockMvc.perform(put("/api/directories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest)
                    )
                    .andExpect(status().isNoContent());

            // Vérifier que le nom a été changé
            mockMvc.perform(get("/api/directories/{id}", directory.id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Nouveau nom"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/directories/{directoryId}")
    class DeleteDirectory {

        @Test
        @WithMockUser(username = "42")
        @DisplayName("204 - répertoire supprimé")
        void deleteDirectory_shouldReturnNoContent() throws Exception {
            // Arrange
            DbDirectories directory = persist(42L, "À supprimer", null, true);

            // Act & Assert
            mockMvc.perform(delete("/api/directories/{id}", directory.id))
                    .andExpect(status().isNoContent());

            // Vérifier que le répertoire n'existe plus
            mockMvc.perform(get("/api/directories/{id}", directory.id))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "42")
        @DisplayName("409 - tentative de suppression d'un répertoire avec des enfants")
        void deleteDirectory_withChildren_shouldReturnConflict() throws Exception {
            // Arrange
            DbDirectories parent = persist(42L, "Parent", null, true);
            persist(42L, "Enfant", parent.id, false);

            // Act & Assert
            mockMvc.perform(delete("/api/directories/{id}", parent.id))
                    .andExpect(status().isConflict());
        }
    }
}