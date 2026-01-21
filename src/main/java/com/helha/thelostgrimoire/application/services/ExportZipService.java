package com.helha.thelostgrimoire.application.services;

import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.directories.IDirectoriesRepository;
import com.helha.thelostgrimoire.infrastructure.notes.DbNotes;
import com.helha.thelostgrimoire.infrastructure.notes.INotesRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ExportZipService {

    private final INotesRepository notesRepository;
    private final IDirectoriesRepository directoriesRepository;

    public ExportZipService(INotesRepository notesRepository, IDirectoriesRepository directoriesRepository) {
        this.notesRepository = notesRepository;
        this.directoriesRepository = directoriesRepository;
    }

    public void writeDirectoryToZip(Long userId, Long parentId, String currentPath, ZipOutputStream zos) throws IOException {
        // 1. Trouver les dossiers enfants
        List<DbDirectories> subDirs = directoriesRepository.findByUserIdAndParentDirectoryId(userId, parentId);

        // 2. Trouver les notes dans ce dossier
        List<DbNotes> notes = notesRepository.findByUserIdAndDirectoryId(userId, parentId);

        // Ajouter les notes du dossier actuel
        for (DbNotes note : notes) {
            String fileName = currentPath + sanitize(note.name) + ".md";
            ZipEntry entry = new ZipEntry(fileName);
            zos.putNextEntry(entry);
            byte[] content = (note.content != null ? note.content : "").getBytes(StandardCharsets.UTF_8);
            zos.write(content);
            zos.closeEntry();
        }

        // Parcourir les sous-dossiers (Récursion)
        for (DbDirectories dir : subDirs) {
            String newPath = currentPath + sanitize(dir.name) + "/";
            // On ajoute une entrée pour le dossier lui-même (optionnel mais propre)
            zos.putNextEntry(new ZipEntry(newPath));
            zos.closeEntry();

            // Appel récursif pour descendre dans l'arborescence
            writeDirectoryToZip(userId, dir.id, newPath, zos);
        }
    }

    private String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}