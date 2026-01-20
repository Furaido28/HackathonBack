package com.helha.thelostgrimoire.controllers.notes;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
                            description = "Directory created successfully"
                    )
            ),
            @ApiResponse(responseCode = "400",
                    content = @Content(schema = @Schema(
                            implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<CreateNotesOutput> createDirectory(
            @Valid @RequestBody CreateNotesInput input) {

        Long authenticatedUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        input.userId = authenticatedUserId;

        CreateNotesOutput output = processor.createNotesHandler.handle(input);

        // Location: /api/directories/{idCreated}
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{directoryId}")
                .buildAndExpand(output.id)
                .toUri();

        return ResponseEntity
                .created(location)
                .body(output);
    }
}
