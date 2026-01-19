package com.helha.thelostgrimoire.application.directories.command;

import com.helha.thelostgrimoire.application.directories.command.create.CreateDirectoriesHandler;
import com.helha.thelostgrimoire.application.directories.command.create.CreateDirectoriesInput;
import org.springframework.stereotype.Service;

@Service
public class DirectoriesCommandProcessor {
    public final CreateDirectoriesHandler createDirectoriesHandler;

    public DirectoriesCommandProcessor(CreateDirectoriesHandler createDirectoriesHandler) {
        this.createDirectoriesHandler = createDirectoriesHandler;
    }


}
