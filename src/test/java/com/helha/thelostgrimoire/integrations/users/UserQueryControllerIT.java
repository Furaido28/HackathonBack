package com.helha.thelostgrimoire.integrations.users;

import com.helha.thelostgrimoire.infrastructure.users.DbUsers;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Users - Query Controller IT")
public class UserQueryControllerIT extends AbstractUsersIT {

    @Test
    @DisplayName("200 - GET /me retourne le user connecté")
    void getMe_shouldReturnOk() throws Exception {
        var savedUser = persistUserRawForJwt("john.doe@example.com");
        Cookie jwt = jwtCookieFor(savedUser);

        mockMvc.perform(get("/api/users/me")
                        .cookie(jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((Long) savedUser.id))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")));
    }

    @Test
    @DisplayName("401 - GET /me sans JWT échoue")
    void getMe_withoutJwt_shouldFail() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().is4xxClientError()); // 401 ou 403
    }

    @Test
    @DisplayName("401 - GET /me avec JWT invalide (signature altérée)")
    void getMe_withTamperedJwt_shouldFail() throws Exception {
        var savedUser = persistUserRawForJwt("hacker@example.com");
        Cookie validJwt = jwtCookieFor(savedUser);

        // On modifie le token pour le rendre invalide
        Cookie tamperedJwt = new Cookie("jwt", validJwt.getValue() + "fake");

        mockMvc.perform(get("/api/users/me")
                        .cookie(tamperedJwt))
                .andExpect(status().isForbidden()); // Le filtre JWT devrait rejeter
    }
}