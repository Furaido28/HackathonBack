package com.helha.thelostgrimoire.application.directories.query;

import com.helha.thelostgrimoire.application.directories.query.getAll.GetAllHandler;
import org.springframework.stereotype.Service;

@Service
public class DirectoriesQueryProcessor {
    public final GetAllHandler getAllHandler;

    public DirectoriesQueryProcessor(GetAllHandler getAllHandler) {
        this.getAllHandler = getAllHandler;
    }
}
