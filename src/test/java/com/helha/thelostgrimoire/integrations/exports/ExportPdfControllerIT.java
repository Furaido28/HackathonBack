package com.helha.thelostgrimoire.integrations.exports;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Export PDF Controller IT")
public class ExportPdfControllerIT extends AbstractExportIT {

    @Test
    @DisplayName("GET /api/export/pdf/{noteId} - 200 - retourne un PDF")
    void exportPdf_shouldReturnPdf() throws Exception {
        var user = persistUserRawForJwt("john.doe@example.com");
        Cookie jwt = jwtCookieFor(user);

        var root = persistRootDirectory((Long) user.id);
        var note = persistNote((Long) user.id, (Long) root.id, "My Note", "# Title\n\nHello **world**!");

        MvcResult result = mockMvc.perform(get("/api/export/pdf/" + note.id)
                        .cookie(jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment; filename=\"")))
                .andReturn();

        byte[] pdf = result.getResponse().getContentAsByteArray();
        assertNotNull(pdf);
        assertTrue(pdf.length > 10);

        // signature PDF: "%PDF"
        assertEquals('%', (char) pdf[0]);
        assertEquals('P', (char) pdf[1]);
        assertEquals('D', (char) pdf[2]);
        assertEquals('F', (char) pdf[3]);
    }

    @Test
    @DisplayName("GET /api/export/pdf/{noteId} - 404 - note not found")
    void exportPdf_whenNotFound_shouldReturn404() throws Exception {
        var user = persistUserRawForJwt("john.doe@example.com");
        Cookie jwt = jwtCookieFor(user);

        mockMvc.perform(get("/api/export/pdf/999999")
                        .cookie(jwt))
                .andExpect(status().isNotFound());
    }
}
