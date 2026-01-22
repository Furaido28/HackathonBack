package com.helha.thelostgrimoire.integrations.users;

import com.helha.thelostgrimoire.application.repositories.users.command.login.LoginInput;
import com.helha.thelostgrimoire.application.repositories.users.command.register.RegisterInput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Auth - Command Controller IT")
public class UserCommandControllerIT extends AbstractUsersIT {

    @Test
    @DisplayName("POST /api/auth/register - 201 - crée un user + Location + body(id,email)")
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
    }

    @Test
    @DisplayName("POST /api/auth/login - 200 - renvoie un cookie jwt (HttpOnly)")
    void login_shouldReturnOk_andSetJwtCookie() throws Exception {
        // Arrange: register d'abord (comme ça pas besoin de connaître ton hashing interne)
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
        // Optionnel:
        // .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Strict")));
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