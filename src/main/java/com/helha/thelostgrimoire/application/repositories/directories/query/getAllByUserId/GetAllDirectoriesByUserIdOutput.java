package com.helha.thelostgrimoire.application.repositories.directories.query.getAllByUserId;

import java.util.ArrayList;
import java.util.List;

public class GetAllDirectoriesByUserIdOutput {
    public List<GetAllDirectoriesByUserIdOutput.Directory> directories = new ArrayList<>();

    public static class Directory {
        public int id;
        public String name;
        public int parentDirectoryId;
    }
}
