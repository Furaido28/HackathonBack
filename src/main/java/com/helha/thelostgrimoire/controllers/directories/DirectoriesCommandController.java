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

    @PostMapping("/{userId}")
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

        CreateDirectoriesOutput output = directoriesCommandProcessor.createDirectoriesHandler.handle(input);

        // Location: /api/directories/{userId}/{idCreated}
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
