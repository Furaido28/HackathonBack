package com.helha.thelostgrimoire.controllers.notes;

import com.helha.thelostgrimoire.application.services.NotesPdfService;
import com.helha.thelostgrimoire.controllers.notes.exceptions.NotesNotFound;
import com.helha.thelostgrimoire.infrastructure.notes.DbNotes;
import com.helha.thelostgrimoire.infrastructure.notes.INotesRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Exporting notes", description = "Notes exporting endpoints")
@RestController
@RequestMapping("/api/notes")
public class NotesPdfController {

    private final INotesRepository notesRepository;
    private final NotesPdfService notesPdfService;

    public NotesPdfController(
            INotesRepository notesRepository,
            NotesPdfService notesPdfService
    ) {
        this.notesRepository = notesRepository;
        this.notesPdfService = notesPdfService;
    }

    /**
     * Téléchargement PDF d'une note
     * URL : GET /api/notes/{id}/pdf
     */
    @GetMapping("/{noteId}/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long noteId) {

        DbNotes note = notesRepository.findById(noteId)
                .orElseThrow(() -> new NotesNotFound(noteId));

        byte[] pdf = notesPdfService.markdownToPdfBytes(note.content);

        String filename = (note.name == null ? "note" : note.name)
                .replaceAll("[^a-zA-Z0-9._-]", "_") + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }
}
