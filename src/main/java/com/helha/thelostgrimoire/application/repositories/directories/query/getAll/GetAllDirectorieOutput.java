package com.helha.thelostgrimoire.application.repositories.directories.query.getAll;

import java.util.ArrayList;
import java.util.List;

public class GetAllDirectorieOutput {
    public List<Directory> directories = new ArrayList<>();

    public static class Directory {
        public int id;
        public String name;
        public int parentDirectoryId;
    }
}
