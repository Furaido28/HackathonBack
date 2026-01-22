package com.helha.thelostgrimoire.application.repositories.users.query.getMe;

import com.helha.thelostgrimoire.application.repositories.utils.CurrentUserContext;
import com.helha.thelostgrimoire.application.repositories.utils.IQueryHandler;
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

        // Retrieve the authenticated user's ID from the security context.
        Long userId = CurrentUserContext.getUserId();

        // Fetch the user's detailed profile from the database using the session ID.
        // Throw an exception if the user record no longer exists.
        DbUsers user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Users not found"));

        // Map the database entity fields to the GetMeOutput DTO for the frontend.
        GetMeOutput output = new GetMeOutput();
        output.id = user.id;
        output.name = user.name;
        output.firstname = user.firstname;
        output.email = user.emailAddress;
        output.createdAt = user.createdAt;

        return output;
    }
}