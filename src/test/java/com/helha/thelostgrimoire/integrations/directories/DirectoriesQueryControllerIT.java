package com.helha.thelostgrimoire.integrations.directories;

import com.helha.thelostgrimoire.application.repositories.users.UserMapper;
import com.helha.thelostgrimoire.domain.models.Users;
import com.helha.thelostgrimoire.infrastructure.users.DbUsers;
import com.helha.thelostgrimoire.infrastructure.users.IUsersRepository;
import com.helha.thelostgrimoire.security.JwtService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

// Vérifie bien ces imports statiques, ce sont eux qui permettent d'écrire du code propre
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Directories - Query Controller IT")
public class DirectoriesQueryControllerIT extends AbstractDirectoriesIT {

    @Autowired private IUsersRepository usersRepository;
    @Autowired private JwtService jwtService;

    private Cookie jwtCookie;
    private DbUsers savedUser;

    @BeforeEach
    void setUp() {
        directoriesRepository.deleteAll();
        usersRepository.deleteAll();

        DbUsers user = new DbUsers();
        user.name = "Doe";
        user.firstname = "John";
        user.emailAddress = "john.doe@example.com";
        user.hashPassword = "password123";
        user.createdAt = LocalDateTime.now();
        savedUser = usersRepository.save(user);

        Users domainUser = UserMapper.toDomain(savedUser);
        String token = jwtService.generateToken(domainUser);
        jwtCookie = new Cookie("jwt", token);
    }

    @Nested
    @DisplayName("GET /api/directories")
    class GetAll {

        @Test
        @DisplayName("200 - doit retourner tous les répertoires de la DB")
        void shouldReturnGlobalDatabaseContent() throws Exception {
            persist(savedUser.id, "Dossier John", null);
            persist(99L, "Dossier Autre", null);

            mockMvc.perform(get("/api/directories").cookie(jwtCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.directories", hasSize(2)));
        }

        @Test
        @DisplayName("200 - structure JSON conforme au DTO")
        void shouldHaveCorrectStructure() throws Exception {
            var dir = persist(savedUser.id, "Docs", null);

            mockMvc.perform(get("/api/directories").cookie(jwtCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.directories[0].id").value(dir.id.intValue()))
                    .andExpect(jsonPath("$.directories[0].name").value("Docs"))
                    .andExpect(jsonPath("$.directories[0].parentDirectoryId").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/directories/me")
    class GetMyDirectories {

        @Test
        @DisplayName("200 - isolation : John ne voit pas les dossiers des autres")
        void shouldFilterByAuthenticatedUser() throws Exception {
            persist(savedUser.id, "Mon Dossier", null);
            persist(999L, "Dossier Hacker", null);

            mockMvc.perform(get("/api/directories/me").cookie(jwtCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.directories", hasSize(1)))
                    .andExpect(jsonPath("$.directories[0].name").value("Mon Dossier"))
                    // On vérifie que le dossier du hacker n'est PAS présent
                    .andExpect(jsonPath("$.directories[?(@.name == 'Dossier Hacker')]").doesNotExist());
        }

        @Test
        @DisplayName("200 - vérifie la hiérarchie parentale")
        void shouldShowCorrectParentIds() throws Exception {
            var root = persist(savedUser.id, "ROOT", null);
            persist(savedUser.id, "SOUS-DOSSIER", root.id);

            mockMvc.perform(get("/api/directories/me").cookie(jwtCookie))
                    .andExpect(status().isOk())
                    // Utilisation de filter pour isoler le sous-dossier et vérifier son parent
                    .andExpect(jsonPath("$.directories[?(@.name == 'SOUS-DOSSIER')].parentDirectoryId")
                            .value(contains(root.id.intValue())));
        }

        @Test
        @DisplayName("403 - échec sans authentification")
        void shouldFailIfUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/directories/me"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("200 - liste vide si l'utilisateur n'a rien")
        void shouldReturnEmptyForUserWithNoData() throws Exception {
            mockMvc.perform(get("/api/directories/me").cookie(jwtCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.directories").isEmpty());
        }

        @Test
        @DisplayName("200 - gère les noms avec caractères spéciaux et espaces")
        void shouldHandleSpecialCharactersInNames() throws Exception {
            persist(savedUser.id, "Dossier @ ! #", null);
            persist(savedUser.id, "   Espaces   ", null);

            mockMvc.perform(get("/api/directories").cookie(jwtCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.directories", hasSize(2)))
                    .andExpect(jsonPath("$.directories[*].name", containsInAnyOrder("Dossier @ ! #", "   Espaces   ")));
        }

        @Test
        @DisplayName("200 - retourne une liste cohérente avec énormément de dossier")
        void shouldReturnManyDirectories() throws Exception {
            for (int i = 0; i < 50; i++) {
                persist(savedUser.id, "Folder " + i, null);
            }

            mockMvc.perform(get("/api/directories").cookie(jwtCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.directories", hasSize(50)));
        }
    }

    @Nested
    @DisplayName("GET /api/directories - Tests de Tri")
    class SortingTests {

        @Test
        @DisplayName("200 - doit retourner les répertoires triés par nom (A-Z)")
        void shouldReturnDirectoriesSortedByName() throws Exception {
            // On persiste dans le désordre
            persist(savedUser.id, "Zèbre", null);
            persist(savedUser.id, "Avion", null);
            persist(savedUser.id, "Maison", null);

            mockMvc.perform(get("/api/directories").cookie(jwtCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.directories", hasSize(3)))
                    // Vérification de l'ordre strict
                    .andExpect(jsonPath("$.directories[0].name").value("Avion"))
                    .andExpect(jsonPath("$.directories[1].name").value("Maison"))
                    .andExpect(jsonPath("$.directories[2].name").value("Zèbre"));
        }

        @Test
        @DisplayName("200 - le tri doit être insensible à la casse (optionnel selon config DB)")
        void shouldHandleCaseInsensitiveSort() throws Exception {
            persist(savedUser.id, "b", null);
            persist(savedUser.id, "A", null);
            persist(savedUser.id, "c", null);

            // Si ta DB est en case-insensitive (souvent le cas en MySQL), A sera avant b
            mockMvc.perform(get("/api/directories").cookie(jwtCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.directories[0].name", equalToIgnoringCase("a")))
                    .andExpect(jsonPath("$.directories[1].name", equalToIgnoringCase("b")))
                    .andExpect(jsonPath("$.directories[2].name", equalToIgnoringCase("c")));
        }
    }
}