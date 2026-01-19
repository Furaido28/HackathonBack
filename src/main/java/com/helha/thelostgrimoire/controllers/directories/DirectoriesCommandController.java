
package com.helha.thelostgrimoire.controllers.directories;

import com.helha.thelostgrimoire.application.directories.command.DirectoriesCommandProcessor;
import com.helha.thelostgrimoire.application.directories.command.create.CreateDirectoriesInput;
import com.helha.thelostgrimoire.application.directories.command.create.CreateDirectoriesOutput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// (Optionnel) Si tu veux des annotations Swagger/OpenAPI, décommente celles-ci
// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.media.Content;
// import io.swagger.v3.oas.annotations.media.Schema;
// import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/directories")
public class DirectoriesCommandController {

    private final DirectoriesCommandProcessor processor;

    public DirectoriesCommandController(DirectoriesCommandProcessor processor) {
        this.processor = processor;
    }

    // @Operation(
    //     summary = "Créer un dossier pour un utilisateur",
    //     description = "Crée un dossier (éventuellement sous un parent) pour l'utilisateur dont l'id est passé en path",
    //     responses = {
    //         @ApiResponse(responseCode = "201", description = "Dossier créé",
    //             content = @Content(schema = @Schema(implementation = CreateDirectoryOutput.class))),
    //         @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content),
    //     }
    // )
    @PostMapping("/{userId}")
    public ResponseEntity<CreateDirectoriesOutput> createDirectory(
            @PathVariable Long userId,
            @RequestBody CreateDirectoriesInput input
    ) {
        // On force le userId depuis l'URL (source de vérité)
        input.userId = userId;

        CreateDirectoriesOutput output = (CreateDirectoriesOutput) processor.process(input);
        return new ResponseEntity<>(output, HttpStatus.CREATED);
    }
}
