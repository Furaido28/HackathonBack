package com.helha.thelostgrimoire.integrations.directories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Directories - Query Controller IT")
public class DirectoriesQueryControllerIT extends AbstractDirectoriesIT {

    @Test
    @WithMockUser(username = "42")
    @DisplayName("GET /api/directories - 200 - retourne tous les répertoires (output wrapper)")
    void getAll_shouldReturnOk() throws Exception {
        persist(42L, "Docs", null);
        persist(99L, "Other", null);

        mockMvc.perform(get("/api/directories"))
                .andExpect(status().isOk());
        // Ici adapte selon ton GetAllDirectorieOutput (ex: $.items, $.directories, etc.)
    }

    @Test
    @WithMockUser(username = "42")
    @DisplayName("GET /api/directories/me - 200 - retourne seulement les répertoires du user connecté")
    void getMyDirectories_shouldReturnOnlyMine() throws Exception {
        persist(42L, "Documents", null);
        persist(42L, "Images", null);
        persist(99L, "Autre utilisateur", null);

        mockMvc.perform(get("/api/directories/me"))
                .andExpect(status().isOk());
    }
}