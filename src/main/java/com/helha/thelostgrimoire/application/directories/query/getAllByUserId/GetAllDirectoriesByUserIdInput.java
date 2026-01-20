package com.helha.thelostgrimoire.application.directories.query.getAllByUserId;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GetAllDirectoriesByUserIdInput {
    @JsonIgnore
    public Long userId;
    public String directoryId;
}
