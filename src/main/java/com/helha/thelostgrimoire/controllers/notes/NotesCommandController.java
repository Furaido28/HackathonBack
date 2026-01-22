package com.helha.thelostgrimoire.controllers.notes;

import com.helha.thelostgrimoire.application.repositories.notes.command.NotesCommandProcessor;
import com.helha.thelostgrimoire.application.repositories.notes.command.create.CreateNotesInput;
import com.helha.thelostgrimoire.application.repositories.notes.command.create.CreateNotesOutput;
import com.helha.thelostgrimoire.application.repositories.notes.command.update.UpdateNotesInput;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Notes", description = "Notes endpoints")
@RestController
@RequestMapping("/api/notes")
public class NotesCommandController {
    private final NotesCommandProcessor processor;

    public NotesCommandController(NotesCommandProcessor notesCommandProcessor) {
        this.processor = notesCommandProcessor;
    }

    @PostMapping()
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    headers = @Header(
                            name = "Directories Created",
                            description = "Directory created successfully")),
            @ApiResponse(responseCode = "400",
                    content = @Content(schema = @Schema(
                            implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<CreateNotesOutput> createDirectory(
            @Valid @RequestBody CreateNotesInput input) {

        Long authenticatedUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        input.userId = authenticatedUserId;

        CreateNotesOutput output = processor.createNotesHandler.handle(input);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{noteId}")
                .buildAndExpand(output.id)
                .toUri();

        return ResponseEntity
                .created(location)
                .body(output);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Update successful"),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (Not your note)"),
            @ApiResponse(responseCode = "404", description = "Note not found")
    })
    @PutMapping("/{noteId}")
    public ResponseEntity<Void> update(@PathVariable Long noteId, @Valid @RequestBody UpdateNotesInput input) {
        Long authenticatedUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        input.userId = authenticatedUserId;
        input.id = noteId;

        processor.updateNotesHandler.handle(input);

        return ResponseEntity.noContent().build();
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (Not your note)", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @DeleteMapping("{noteId}")
    public ResponseEntity<Void> delete(@PathVariable Long noteId) {
        Long authUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        processor.deleteNotesHandler.handle(noteId, authUserId);
        return ResponseEntity.noContent().build();
    }
}