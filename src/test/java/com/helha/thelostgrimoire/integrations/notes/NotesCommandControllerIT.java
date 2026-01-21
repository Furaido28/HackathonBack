package com.helha.thelostgrimoire.integrations.notes;

import com.helha.thelostgrimoire.application.notes.command.create.CreateNotesInput;
import com.helha.thelostgrimoire.application.notes.command.update.UpdateNotesInput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Notes - Command Controller IT")
public class NotesCommandControllerIT extends AbstractNotesIT {

    @Test
    @DisplayName("POST /api/notes - 201 - Crée une nouvelle note avec succès")
    void shouldCreateNote() throws Exception {
        CreateNotesInput input = new CreateNotesInput();
        input.name = "My New Note";
        input.directoryId = savedDirectory.id;

        mockMvc.perform(post("/api/notes")
                        .cookie(jwtCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name", is("My New Note")));
    }

    @Test
    @DisplayName("PUT /api/notes/{id} - 204 - Met à jour une note existante")
    void shouldUpdateNote() throws Exception {
        var savedNote = createNoteInDb("Old Name");

        UpdateNotesInput updateInput = new UpdateNotesInput();
        updateInput.name = "New Name";
        updateInput.content = "New Content";

        mockMvc.perform(put("/api/notes/{id}", savedNote.id)
                        .cookie(jwtCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateInput)))
                .andExpect(status().isNoContent());

        var updated = notesRepository.findById(savedNote.id).orElseThrow();
        assert updated.name.equals("New Name");
    }

    @Test
    @DisplayName("DELETE /api/notes/{id} - 204 - Supprime une note")
    void shouldDeleteNote() throws Exception {
        var savedNote = createNoteInDb("To Delete");

        mockMvc.perform(delete("/api/notes/{id}", savedNote.id)
                        .cookie(jwtCookie))
                .andExpect(status().isNoContent());

        assert notesRepository.findById(savedNote.id).isEmpty();
    }
}
