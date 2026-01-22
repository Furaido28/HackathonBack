package com.helha.thelostgrimoire.unitaires.exports;

import com.helha.thelostgrimoire.application.services.ExportPdfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests Unitaires Étendus - Export PDF")
class ExportPdfTest {

    private ExportPdfService exportPdfService;

    @BeforeEach
    void setUp() {
        exportPdfService = new ExportPdfService();
    }

    @Test
    @DisplayName("Vérification de la croissance de taille")
    void testFileSizeGrowth() {
        byte[] smallPdf = exportPdfService.markdownToPdfBytes("# Titre");
        byte[] largePdf = exportPdfService.markdownToPdfBytes("# Titre\n" + "Texte long... ".repeat(100));

        assertTrue(largePdf.length > smallPdf.length,
                "Un contenu plus long devrait générer un fichier plus lourd");
    }

    @ParameterizedTest(name = "Markdown avec {0}")
    @ValueSource(strings = {
            "**Gras** et *Italique*",
            "### Liste\n- Item 1\n- Item 2",
            "```java\nSystem.out.println();\n```",
            "> Citation importante"
    })

    @DisplayName("Rendu des éléments Markdown standards")
    void testMarkdownElements(String markdown) {
        byte[] pdfBytes = exportPdfService.markdownToPdfBytes(markdown);

        assertNotNull(pdfBytes);
        assertTrue(isPdf(pdfBytes));
    }

    @Test
    @DisplayName("Sécurité - Caractères HTML injectés")
    void testHtmlInjection() {
        // On vérifie que des balises HTML brutes dans le markdown ne cassent pas le moteur PDF
        String markdown = "Normal <script>alert('hack')</script> **Gras**";

        assertDoesNotThrow(() -> {
            byte[] pdfBytes = exportPdfService.markdownToPdfBytes(markdown);
            assertNotNull(pdfBytes);
            assertTrue(isPdf(pdfBytes));
        });
    }

    @Test
    @DisplayName("Performance - Document très volumineux")
    void testLargeDocumentPerformance() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 1000; i++) {
            sb.append("Ligne numéro ").append(i).append(" avec du contenu.\n\n");
        }

        // On s'assure que même un gros document est généré en moins de 2 secondes
        assertTimeout(java.time.Duration.ofSeconds(2), () -> {
            byte[] pdfBytes = exportPdfService.markdownToPdfBytes(sb.toString());
            assertTrue(pdfBytes.length > 10000);
        });
    }

    private boolean isPdf(byte[] data) {
        if (data == null || data.length < 4) return false;
        // Signature %PDF-
        return data[0] == 0x25 && data[1] == 0x50 && data[2] == 0x44 && data[3] == 0x46;
    }
}