package com.helha.thelostgrimoire.application.repositories.users.query;

import com.helha.thelostgrimoire.application.repositories.users.query.getMe.GetMeHandler;
import org.springframework.stereotype.Service;

@Service
public class UsersQueryProcessor {
    public GetMeHandler getMeHandler;

    public UsersQueryProcessor(GetMeHandler getMeHandler) {
        this.getMeHandler = getMeHandler;
    }
}
