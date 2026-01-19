package com.helha.thelostgrimoire.application.directories.query.getAll;

import com.helha.thelostgrimoire.domain.Directories;

import java.util.ArrayList;
import java.util.List;

public class  GetAllOutput {
    public List<Directory> directories = new ArrayList<>();

    public static class Directory {
        public int id;
        public String name;
    }
}
