package com.helha.thelostgrimoire.controllers.directories;

import com.helha.thelostgrimoire.application.repositories.directories.command.DirectoriesCommandProcessor;
import com.helha.thelostgrimoire.application.repositories.directories.command.create.CreateDirectoriesInput;
import com.helha.thelostgrimoire.application.repositories.directories.command.create.CreateDirectoriesOutput;
import com.helha.thelostgrimoire.application.repositories.directories.command.update.UpdateDirectoriesInput;
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

    /**
     * Dependency injection of the command processor following the CQRS pattern
     */
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
            @Valid @RequestBody CreateDirectoriesInput input) {
        // Récupérer l'ID de l'utilisateur depuis l'authentication
        /**
         * Extract the authenticated user's unique identifier from the SecurityContext
         */
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        Long authenticatedUserId = Long.parseLong(principal);

        /**
         * Link the new directory to the currently logged-in user
         */
        input.userId = authenticatedUserId;

        CreateDirectoriesOutput output = directoriesCommandProcessor.createDirectoriesHandler.handle(input);

        /**
         * Generate the URI for the newly created resource to be returned in the Location header
         */
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

        /**
         * Retrieve security credentials to ensure the user can only delete their own resources
         */
        Long authUserId = Long.parseLong(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        /**
         * Delegate the deletion logic to the command handler with ownership verification
         */
        directoriesCommandProcessor.deleteDirectoriesHandler.handle(directoryId, authUserId);

        return ResponseEntity.noContent().build();
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Update successful"),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (Not your directory)"),
            @ApiResponse(responseCode = "404", description = "Directory not found")
    })
    @PutMapping
    public ResponseEntity<Void> update(@Valid @RequestBody UpdateDirectoriesInput input) {

        /**
         * Ensure data integrity by overriding the input user ID with the authenticated session ID
         */
        Long authenticatedUserId = Long.parseLong(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        input.userId = authenticatedUserId;

        /**
         * Execute the update business logic
         */
        directoriesCommandProcessor.updateDirectoriesHandler.handle(input);

        return ResponseEntity.noContent().build();
    }
}