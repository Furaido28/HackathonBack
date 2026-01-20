package com.helha.thelostgrimoire.controllers.directories;

import com.helha.thelostgrimoire.application.directories.command.DirectoriesCommandProcessor;
import com.helha.thelostgrimoire.application.directories.command.create.CreateDirectoriesInput;
import com.helha.thelostgrimoire.application.directories.command.create.CreateDirectoriesOutput;
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

@Tag(name = "Directories", description = "Directories endpoints")
@RestController
@RequestMapping("/api/directories")
public class DirectoriesCommandController {

    private final DirectoriesCommandProcessor directoriesCommandProcessor;

    public DirectoriesCommandController(DirectoriesCommandProcessor directoriesCommandProcessor) {
        this.directoriesCommandProcessor = directoriesCommandProcessor;
    }

    @PostMapping
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
    public ResponseEntity<CreateDirectoriesOutput> createDirectory(
            @Valid @RequestBody CreateDirectoriesInput input
    ) {

        Long authenticatedUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        input.userId = authenticatedUserId;

        CreateDirectoriesOutput output = directoriesCommandProcessor.createDirectoriesHandler.handle(input);

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

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (Not your directory)", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @DeleteMapping("{directoryId}")
    public ResponseEntity<Void> delete(@PathVariable Long directoryId) {

        // 1. Récupérer l'ID de l'utilisateur connecté (Sécurité)
        Long authUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 2. Passer l'ID du dossier ET l'ID de l'user au Handler
        directoriesCommandProcessor.deleteDirectoriesHandler.handle(directoryId, authUserId);

        return ResponseEntity.noContent().build();
    }
}