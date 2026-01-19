package com.helha.thelostgrimoire.application.users.query.getMe;

import com.helha.thelostgrimoire.application.utils.CurrentUserContext;
import com.helha.thelostgrimoire.application.utils.IQueryHandler;
import com.helha.thelostgrimoire.infrastructure.users.IUsersRepository;
import com.helha.thelostgrimoire.infrastructure.users.DbUsers;
import org.springframework.stereotype.Service;

@Service
public class GetMeHandler implements IQueryHandler<GetMeOutput> {

    private final IUsersRepository userRepository;

    public GetMeHandler(IUsersRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public GetMeOutput handle() {

        Long userId = CurrentUserContext.getUserId();

        DbUsers user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Users not found"));

        GetMeOutput output = new GetMeOutput();
        output.id = user.id;
        output.name = user.name;
        output.firstname = user.firstname;
        output.email = user.emailAddress;
        output.createdAt = user.createdAt;

        return output;
    }
}