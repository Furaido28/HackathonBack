package com.helha.thelostgrimoire.application.services;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.vladsch.flexmark.html.HtmlRenderer;
import org.springframework.stereotype.Service;

import com.vladsch.flexmark.parser.Parser;
import java.io.ByteArrayOutputStream;

@Service
public class ExportPdfService {

    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer renderer = HtmlRenderer.builder().build();

    public byte[] markdownToPdfBytes(String markdown) {
        if (markdown == null) markdown = "";

        String htmlBody = renderer.render(parser.parse(markdown));

        String html = """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
              <meta charset="UTF-8" />
              <style>
                /* Vos styles ici */
                body { font-family: sans-serif; }
              </style>
            </head>
            <body>
              %s
            </body>
            </html>
        """.replace("%s", htmlBody);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, "/");
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur PDF: " + e.getMessage());
        }
    }
}
