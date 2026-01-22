package com.helha.thelostgrimoire.integrations.exports;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Export ZIP Controller IT")
public class ExportZipControllerIT extends AbstractExportIT {

    @Test
    @DisplayName("GET /api/export/zip/me - 200 - retourne un zip")
    void exportAllNotes_shouldReturnZip() throws Exception {
        // Setup
        var user = persistUserRawForJwt("john.doe@example.com");
        Cookie jwt = jwtCookieFor(user);
        persistRootDirectory(user.id);

        // 1. Appel initial : On vérifie que l'asynchrone démarre
        MvcResult mvcResult = mockMvc.perform(get("/api/export/zip/me").cookie(jwt))
                .andExpect(request().asyncStarted())
                .andReturn();

        // 2. Dispatch : On vérifie le statut 200 (Le Content-Type est parfois capricieux en IT asynchrone)
        MvcResult result = mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andReturn();

        // 3. Vérification manuelle du header et du contenu binaire
        String contentDisposition = result.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION);
        assertNotNull(contentDisposition);
        assertTrue(contentDisposition.contains("archive.zip"));

        byte[] zip = result.getResponse().getContentAsByteArray();
        assertTrue(zip.length > 0, "Le fichier ZIP ne doit pas être vide");
        assertEquals('P', (char) zip[0], "Signature ZIP invalide (P)");
        assertEquals('K', (char) zip[1], "Signature ZIP invalide (K)");
    }

    @Test
    @DisplayName("GET /api/export/zip/{directoryId} - 200 - retourne un zip")
    void exportDirectoryNotes_shouldReturnZip() throws Exception {
        var user = persistUserRawForJwt("john.doe@example.com");
        Cookie jwt = jwtCookieFor(user);
        var dir = persistDirectory(user.id, "MyDir", null);

        MvcResult mvcResult = mockMvc.perform(get("/api/export/zip/" + dir.id).cookie(jwt))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk());

        // Vérification du nom de fichier dans le header
        String contentDisp = mvcResult.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION);
        assertNotNull(contentDisp);
        assertTrue(contentDisp.contains("archive_MyDir.zip"));
    }

    @Test
    @DisplayName("GET /api/export/zip/{directoryId} - 404 - Not Found Exception")
    void exportDirectoryNotes_whenNotFound_shouldReturn404() throws Exception {
        var user = persistUserRawForJwt("john.doe@example.com");
        Cookie jwt = jwtCookieFor(user);

        MvcResult mvcResult = mockMvc.perform(get("/api/export/zip/999999").cookie(jwt))
                .andReturn();

        if (mvcResult.getRequest().isAsyncStarted()) {
            mockMvc.perform(asyncDispatch(mvcResult))
                    .andExpect(status().isNotFound());
        } else {
            assertEquals(404, mvcResult.getResponse().getStatus());
        }
    }
}