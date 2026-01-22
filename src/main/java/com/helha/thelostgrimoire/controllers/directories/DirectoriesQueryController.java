package com.helha.thelostgrimoire.controllers.directories;

import com.helha.thelostgrimoire.application.repositories.directories.query.DirectoriesQueryProcessor;
import com.helha.thelostgrimoire.application.repositories.directories.query.getAll.GetAllDirectorieOutput;
import com.helha.thelostgrimoire.application.repositories.directories.query.getAllByUserId.GetAllDirectoriesByUserIdInput;
import com.helha.thelostgrimoire.application.repositories.directories.query.getAllByUserId.GetAllDirectoriesByUserIdOutput;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Directories", description = "Directories endpoints")
@RestController
@RequestMapping("/api/directories")
public class DirectoriesQueryController {

    /**
     * Dependency injection of the query processor to handle data retrieval
     */
    private final DirectoriesQueryProcessor processor;

    public DirectoriesQueryController(DirectoriesQueryProcessor processor) {
        this.processor = processor;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    headers = @Header(
                            name = "All directories",
                            description = "Get all directories"
                    )
            ),
            @ApiResponse(responseCode = "404",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    @GetMapping()
    public ResponseEntity<GetAllDirectorieOutput> getAll() {
        /**
         * Fetch all existing directories without specific filtering
         */
        GetAllDirectorieOutput output = processor.getAllHandler.handle();

        return ResponseEntity.ok(output);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    headers = @Header(
                            name = "All user directories",
                            description = "Get all directories of users"
                    )
            ),
            @ApiResponse(responseCode = "404",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<GetAllDirectoriesByUserIdOutput> getMyDirectories() {

        /**
         * Retrieve the ID of the currently authenticated user from the security context
         */
        long authenticatedUserId = Long.parseLong(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        /**
         * Initialize the query input with the authenticated user's ID to filter results
         */
        GetAllDirectoriesByUserIdInput input = new GetAllDirectoriesByUserIdInput();
        input.userId = authenticatedUserId;

        /**
         * Execute the query to retrieve only the directories belonging to the current user
         */
        GetAllDirectoriesByUserIdOutput output = processor.getAllDirectoriesByUserIdHandler.handle(input);
        return ResponseEntity.ok(output);
    }
}