package com.helha.thelostgrimoire.application.services;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.vladsch.flexmark.html.HtmlRenderer;
import org.springframework.stereotype.Service;

import com.vladsch.flexmark.parser.Parser;
import java.io.ByteArrayOutputStream;

@Service
public class NotesPdfService {

    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer renderer = HtmlRenderer.builder().build();

    public byte[] markdownToPdfBytes(String markdown) {
        if (markdown == null) markdown = "";

        // 1) Markdown -> HTML (h1, h2, etc.)
        String htmlBody = renderer.render(parser.parse(markdown));

        // 2) On enveloppe dans un HTML complet + CSS
        String html = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8" />
          <style>
            body { font-family: Arial, sans-serif; font-size: 12pt; line-height: 1.4; }
            h1 { font-size: 24pt; margin: 16px 0 10px; }
            h2 { font-size: 18pt; margin: 14px 0 8px; }
            h3 { font-size: 14pt; margin: 12px 0 6px; }
            p { margin: 6px 0; }
            code { font-family: monospace; background: #f4f4f4; padding: 2px 4px; border-radius: 3px; }
            pre { background: #f4f4f4; padding: 10px; border-radius: 6px; overflow-wrap: break-word; white-space: pre-wrap; }
            blockquote { border-left: 4px solid #ccc; padding-left: 10px; color: #555; margin: 10px 0; }
            ul, ol { margin: 6px 0 6px 20px; }
            a { color: #0645ad; text-decoration: none; }
            table { border-collapse: collapse; width: 100%; margin: 10px 0; }
            th, td { border: 1px solid #ddd; padding: 6px; }
            th { background: #f0f0f0; }
          </style>
        </head>
        <body>
        %s
        </body>
        </html>
        """.formatted(htmlBody);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null); // baseUri null (ok si pas d'images externes)
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }
}
