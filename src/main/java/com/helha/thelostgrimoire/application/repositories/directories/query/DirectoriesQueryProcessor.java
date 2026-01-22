package com.helha.thelostgrimoire.application.repositories.directories.query;

import com.helha.thelostgrimoire.application.repositories.directories.query.getAll.GetAllDirectoriesHandler;
import com.helha.thelostgrimoire.application.repositories.directories.query.getAllByUserId.GetAllDirectoriesByUserIdHandler;
import org.springframework.stereotype.Service;

@Service
public class DirectoriesQueryProcessor {
    public final GetAllDirectoriesHandler getAllHandler;
    public final GetAllDirectoriesByUserIdHandler getAllDirectoriesByUserIdHandler;

    public DirectoriesQueryProcessor(GetAllDirectoriesHandler getAllHandler,
                                     GetAllDirectoriesByUserIdHandler getAllDirectoriesByUserIdHandler) {
        this.getAllHandler = getAllHandler;
        this.getAllDirectoriesByUserIdHandler = getAllDirectoriesByUserIdHandler;
    }
}
