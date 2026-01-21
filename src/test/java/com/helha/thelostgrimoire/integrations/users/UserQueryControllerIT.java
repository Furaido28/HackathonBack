package com.helha.thelostgrimoire.integrations.users;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Users - Query Controller IT")
public class UserQueryControllerIT extends AbstractUsersIT {

    @Test
    @DisplayName("GET /api/users/me - 200 - retourne le user connecté")
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
    @DisplayName("GET /api/users/me - 401 - sans JWT")
    void getMe_withoutJwt_shouldFail() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().is4xxClientError());
    }
}