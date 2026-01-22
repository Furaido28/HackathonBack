package com.helha.thelostgrimoire.controllers.users;

import com.helha.thelostgrimoire.application.repositories.users.query.UsersQueryProcessor;
import com.helha.thelostgrimoire.application.repositories.users.query.getMe.GetMeOutput;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "User endpoints")
@RestController
@RequestMapping("/api/users")
public class UserQueryController {
    private final UsersQueryProcessor usersQueryProcessor;

    public UserQueryController(UsersQueryProcessor usersQueryProcessor) {
        this.usersQueryProcessor = usersQueryProcessor;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    headers = @Header(
                            name = "Curent user",
                            description = "Information of curent user"
                    )
            ),
            @ApiResponse(responseCode = "404",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<GetMeOutput> getMe() {
        GetMeOutput output = usersQueryProcessor.getMeHandler.handle();

        return ResponseEntity.ok(output);
    }
}
