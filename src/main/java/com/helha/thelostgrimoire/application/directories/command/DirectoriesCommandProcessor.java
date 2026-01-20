package com.helha.thelostgrimoire.application.directories.command;

import com.helha.thelostgrimoire.application.directories.command.create.CreateDirectoriesHandler;
import com.helha.thelostgrimoire.application.directories.command.create.CreateDirectoriesInput;
import com.helha.thelostgrimoire.application.directories.command.delete.DeleteDirectoriesHandler;
import com.helha.thelostgrimoire.application.directories.command.update.UpdateDirectoriesHandler;
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