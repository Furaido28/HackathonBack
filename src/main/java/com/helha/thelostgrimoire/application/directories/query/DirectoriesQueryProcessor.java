package com.helha.thelostgrimoire.application.directories.query;

import com.helha.thelostgrimoire.application.directories.query.getAll.GetAllHandler;
import com.helha.thelostgrimoire.application.directories.query.getAllByUserId.GetAllByUserIdHandler;
import org.springframework.stereotype.Service;

@Service
public class DirectoriesQueryProcessor {
    public final GetAllHandler getAllHandler;
    public final GetAllByUserIdHandler getAllByUserIdHandler;

    public DirectoriesQueryProcessor(GetAllHandler getAllHandler,
                                     GetAllByUserIdHandler getAllByUserIdHandler) {
        this.getAllHandler = getAllHandler;
        this.getAllByUserIdHandler = getAllByUserIdHandler;
    }
}
