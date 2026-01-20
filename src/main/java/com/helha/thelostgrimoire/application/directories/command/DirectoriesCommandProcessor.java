package com.helha.thelostgrimoire.application.directories.command;

import com.helha.thelostgrimoire.application.directories.command.create.CreateDirectoriesHandler;
import com.helha.thelostgrimoire.application.directories.command.create.CreateDirectoriesInput;
import com.helha.thelostgrimoire.application.directories.command.delete.DeleteDirectoriesHandler;
import org.springframework.stereotype.Service;

@Service
public class DirectoriesCommandProcessor {
    public final CreateDirectoriesHandler createDirectoriesHandler;
    public final DeleteDirectoriesHandler deleteDirectoriesHandler;

    public DirectoriesCommandProcessor(CreateDirectoriesHandler createDirectoriesHandler, DeleteDirectoriesHandler deleteDirectoriesHandler) {
        this.createDirectoriesHandler = createDirectoriesHandler;
        this.deleteDirectoriesHandler = deleteDirectoriesHandler;
    }


}