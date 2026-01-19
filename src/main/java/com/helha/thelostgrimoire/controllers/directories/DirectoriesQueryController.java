package com.helha.thelostgrimoire.controllers.directories;

import com.helha.thelostgrimoire.application.directories.command.DirectoriesCommandProcessor;
import com.helha.thelostgrimoire.application.directories.query.DirectoriesQueryProcessor;
import com.helha.thelostgrimoire.application.directories.query.getAll.GetAllOutput;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/directories")
public class DirectoriesQueryController {
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
    public ResponseEntity<GetAllOutput> getAll() {
        GetAllOutput output = processor.getAllHandler.handle();

        return ResponseEntity.ok(output);
    }
}
