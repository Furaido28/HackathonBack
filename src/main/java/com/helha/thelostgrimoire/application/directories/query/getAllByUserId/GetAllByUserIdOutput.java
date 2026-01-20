package com.helha.thelostgrimoire.application.directories.query.getAllByUserId;

import java.util.ArrayList;
import java.util.List;

public class GetAllByUserIdOutput {
    public List<GetAllByUserIdOutput.Directory> directories = new ArrayList<>();

    public static class Directory {
        public int id;
        public String name;
        public int parentDirectoryId;
    }
}
