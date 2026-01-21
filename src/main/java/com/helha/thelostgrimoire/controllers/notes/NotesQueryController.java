package com.helha.thelostgrimoire.controllers.notes;

import com.helha.thelostgrimoire.application.notes.query.GetAllByDirectoriesByUserId.GetAllByDirectoriesByUserIdInput;
import com.helha.thelostgrimoire.application.notes.query.GetAllByDirectoriesByUserId.GetAllByDirectoriesByUserIdOutput;
import com.helha.thelostgrimoire.application.notes.query.NotesQueryProcessor;
import com.helha.thelostgrimoire.application.notes.query.getAll.GetAllNotesOutput;

import com.helha.thelostgrimoire.application.notes.query.getById.GetNoteByIdInput;
import com.helha.thelostgrimoire.application.notes.query.getById.GetNoteByIdOutput;

import com.helha.thelostgrimoire.application.notes.query.getMetaData.GetNoteMetadataInput;
import com.helha.thelostgrimoire.application.notes.query.getMetaData.GetNoteMetadataOutput;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notes", description = "Notes endpoints")
@RestController
@RequestMapping("/api/notes")
public class NotesQueryController {
    private final NotesQueryProcessor processor;

    public NotesQueryController(NotesQueryProcessor processor) {
        this.processor = processor;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    headers = @Header(name = "All notes", description = "Get all notes")),
            @ApiResponse(responseCode = "404",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<GetAllNotesOutput> getAllByUserId() {
        GetAllNotesOutput output = processor.getAllNotesHandler.handle();
        return ResponseEntity.ok(output);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notes retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Directory does not belong to you",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Directory not found",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    @GetMapping("/directory/{directoryId}")
    public ResponseEntity<GetAllByDirectoriesByUserIdOutput> getNotesByDirectory(@PathVariable Long directoryId) {
        Long authUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        GetAllByDirectoriesByUserIdInput input = new GetAllByDirectoriesByUserIdInput();
        input.directoryId = directoryId;
        input.userId = authUserId;

        GetAllByDirectoriesByUserIdOutput output = processor.getAllByDirectoriesByUserIdHandler.handle(input);

        return ResponseEntity.ok(output);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    headers = @Header(
                            name = "Note informations",
                            description = "Get note informations"
                    )
            ),
            @ApiResponse(responseCode = "404",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    @GetMapping("/{noteId}")
    public ResponseEntity<GetNoteByIdOutput> getById(@PathVariable Long noteId) {
        GetNoteByIdInput input = new GetNoteByIdInput();
        input.id = noteId;

        GetNoteByIdOutput output = processor.getNoteByIdHandler.handle(input);

        return ResponseEntity.ok(output);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metadata retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Note not found")
    })
    @GetMapping("/{noteId}/metadata")
    public ResponseEntity<GetNoteMetadataOutput> getMetadata(@PathVariable Long noteId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        GetNoteMetadataInput input = new GetNoteMetadataInput();
        input.noteId = noteId;
        input.userId = userId;

        GetNoteMetadataOutput output = processor.getNoteMetadataHandler.handle(input);

        return ResponseEntity.ok(output);
    }


}
