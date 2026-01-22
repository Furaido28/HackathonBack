package com.helha.thelostgrimoire.application.repositories.directories.command;

import com.helha.thelostgrimoire.application.repositories.directories.command.create.CreateDirectoriesHandler;
import com.helha.thelostgrimoire.application.repositories.directories.command.delete.DeleteDirectoriesHandler;
import com.helha.thelostgrimoire.application.repositories.directories.command.update.UpdateDirectoriesHandler;
import org.springframework.stereotype.Service;

@Service
public class DirectoriesCommandProcessor {
    public final CreateDirectoriesHandler createDirectoriesHandler;
    public final DeleteDirectoriesHandler deleteDirectoriesHandler;
    public final UpdateDirectoriesHandler updateDirectoriesHandler;

    public DirectoriesCommandProcessor(CreateDirectoriesHandler createDirectoriesHandler, DeleteDirectoriesHandler deleteDirectoriesHandler, UpdateDirectoriesHandler updateDirectoriesHandler) {
        this.createDirectoriesHandler = createDirectoriesHandler;
        this.deleteDirectoriesHandler = deleteDirectoriesHandler;
        this.updateDirectoriesHandler = updateDirectoriesHandler;
    }


}