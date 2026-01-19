
package com.helha.thelostgrimoire.application.directories.command.create;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CreateDirectoriesInput {
    @JsonIgnore
    public Long userId;
    public String name;
    public Long parentDirectoryId;
}
