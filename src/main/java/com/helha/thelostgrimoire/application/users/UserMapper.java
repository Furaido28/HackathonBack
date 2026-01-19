package com.helha.thelostgrimoire.application.users;

import com.helha.thelostgrimoire.domain.Users;
import com.helha.thelostgrimoire.infrastructure.users.DbUsers;

public class UserMapper {
    public static Users toDomain (DbUsers dbUser){
        if(dbUser == null)return null;

        Users user = new Users();
        user.setId(dbUser.id);
        user.setName(dbUser.name);
        user.setFirstname(dbUser.firstname);
        user.setEmail_address(dbUser.emailAddress);
        user.setHash_password(dbUser.hashPassword);
        user.setCreated_at(dbUser.createdAt);
        return user;
    }

    public static DbUsers toEntity(Users user){
        if(user == null)return null;

        DbUsers dbUser = new DbUsers();
        dbUser.id = user.getId();
        dbUser.name = user.getName();
        dbUser.firstname = user.getFirstname();
        dbUser.emailAddress = user.getEmail_address();
        dbUser.hashPassword = user.getHash_password();
        dbUser.createdAt = user.getCreated_at();
        return dbUser;
    }
}
