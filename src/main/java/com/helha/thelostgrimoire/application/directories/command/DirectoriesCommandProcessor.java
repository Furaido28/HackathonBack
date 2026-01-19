package com.helha.thelostgrimoire.application.directories.command;

import com.helha.thelostgrimoire.application.directories.command.create.CreateDirectoriesHandler;

public class DirectoriesCommandProcessor {
    public final CreateDirectoriesHandler createDirectoriesHandler;

    public DirectoriesCommandProcessor(CreateDirectoriesHandler createDirectoriesHandler) {
        this.createDirectoriesHandler = createDirectoriesHandler;
    }
}
