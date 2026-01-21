package com.helha.thelostgrimoire.controllers.export;

import com.helha.thelostgrimoire.application.services.ExportPdfService;
import com.helha.thelostgrimoire.controllers.notes.exceptions.NotesNotFound;
import com.helha.thelostgrimoire.infrastructure.notes.DbNotes;
import com.helha.thelostgrimoire.infrastructure.notes.INotesRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Export", description = "Exporting endpoints")
@RestController
@RequestMapping("/api/export")
public class ExportPdfController {

    private final INotesRepository notesRepository;
    private final ExportPdfService exportPdfService;

    public ExportPdfController(
            INotesRepository notesRepository,
            ExportPdfService notesPdfService
    ) {
        this.notesRepository = notesRepository;
        this.exportPdfService = notesPdfService;
    }

    /**
     * Téléchargement PDF d'une note
     * URL : GET /api/notes/{id}/pdf
     */
    @GetMapping("/pdf/{noteId}")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long noteId) {

        DbNotes note = notesRepository.findById(noteId)
                .orElseThrow(() -> new NotesNotFound(noteId));

        byte[] pdf = exportPdfService.markdownToPdfBytes(note.content);

        String filename = (note.name == null ? "note" : note.name)
                .replaceAll("[^a-zA-Z0-9._-]", "_") + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }
}
