package com.helha.thelostgrimoire.integrations.directories;

import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Directories - Command Controller IT")
public class DirectoriesCommandControllerIT extends AbstractDirectoriesIT {

    @Nested
    @DisplayName("POST /api/directories")
    class CreateDirectory {

        @Test
        @WithMockUser(username = "42")
        @DisplayName("201 - répertoire créé avec succès")
        void createDirectory_shouldReturnCreated() throws Exception {
            String jsonRequest = """
                {
                    "name": "Mes documents",
                    "parentDirectoryId": null
                }
                """;

            mockMvc.perform(post("/api/directories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.name").value("Mes documents"));
        }

        @Test
        @WithMockUser(username = "42")
        @DisplayName("201 - répertoire créé avec parent")
        void createDirectory_withParent_shouldReturnCreated() throws Exception {
            DbDirectories parent = persist(42L, "Parent", null);

            String jsonRequest = """
                {
                    "name": "Sous-dossier",
                    "parentDirectoryId": %d
                }
                """.formatted(parent.id);

            mockMvc.perform(post("/api/directories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Sous-dossier"));
        }

        @Test
        @WithMockUser(username = "42")
        @DisplayName("400 - nom vide")
        void createDirectory_withEmptyName_shouldReturnBadRequest() throws Exception {
            String jsonRequest = """
                {
                    "name": "",
                    "parentDirectoryId": null
                }
                """;

            mockMvc.perform(post("/api/directories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "42")
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
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
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
            DbDirectories directory = persist(42L, "Ancien nom", null);

            String jsonRequest = """
                {
                    "id": %d,
                    "name": "Nouveau nom"
                }
                """.formatted(directory.id);

            mockMvc.perform(put("/api/directories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("DELETE /api/directories/{directoryId}")
    class DeleteDirectory {

        @Test
        @WithMockUser(username = "42")
        @DisplayName("204 - répertoire supprimé")
        void deleteDirectory_shouldReturnNoContent() throws Exception {
            DbDirectories directory = persist(42L, "À supprimer", null);

            mockMvc.perform(delete("/api/directories/{directoryId}", directory.id))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(username = "42")
        @DisplayName("409 - tentative de suppression d'un répertoire avec des enfants")
        void deleteDirectory_withChildren_shouldReturnConflict() throws Exception {
            DbDirectories parent = persist(42L, "Parent", null);
            persist(42L, "Enfant", parent.id);

            mockMvc.perform(delete("/api/directories/{directoryId}", parent.id))
                    .andExpect(status().isConflict());
        }
    }
}
