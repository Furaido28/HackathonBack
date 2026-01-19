package com.helha.thelostgrimoire.controllers.directories;

import com.helha.thelostgrimoire.application.directories.command.DirectoriesCommandProcessor;
import com.helha.thelostgrimoire.application.directories.command.create.CreateDirectoriesInput;
import com.helha.thelostgrimoire.application.directories.command.create.CreateDirectoriesOutput;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/directories")
public class DirectoriesCommandController {

    private final DirectoriesCommandProcessor directoriesCommandProcessor;

    public DirectoriesCommandController(DirectoriesCommandProcessor directoriesCommandProcessor) {
        this.directoriesCommandProcessor = directoriesCommandProcessor;
    }

    // 1. On retire "/{userId}". La route est désormais juste POST sur /api/directories
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
        // 2. SÉCURITÉ : Récupération de l'ID depuis le Token (via le Context)
        // Ton filtre a mis un "Long" dans le principal, donc on cast en Long.
        Long authenticatedUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 3. FORCE L'ID : On écrase ce que l'utilisateur a pu envoyer dans le JSON
        // Comme tes champs sont publics dans CreateDirectoriesInput, on assigne directement.
        input.userId = authenticatedUserId;

        // Appel du handler avec l'input sécurisé
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
}