package com.helha.thelostgrimoire.unitaires.exports;

import com.helha.thelostgrimoire.application.services.ExportZipService;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.directories.IDirectoriesRepository;
import com.helha.thelostgrimoire.infrastructure.notes.DbNotes;
import com.helha.thelostgrimoire.infrastructure.notes.INotesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Tests Unitaires - Export ZIP Service")
class ExportZipTest {

    private INotesRepository notesRepository;
    private IDirectoriesRepository directoriesRepository;
    private ExportZipService exportZipService;

    @BeforeEach
    void setUp() {
        notesRepository = mock(INotesRepository.class);
        directoriesRepository = mock(IDirectoriesRepository.class);
        exportZipService = new ExportZipService(notesRepository, directoriesRepository);
    }

    // --- TESTS DE STRUCTURE ET RÉCURSION ---

    @Test
    @DisplayName("Doit générer un ZIP avec arborescence correcte")
    void testRecursiveZipGeneration() throws IOException {
        Long userId = 1L;

        // Dossier Racine (ID: 10) -> contient une note et un sous-dossier
        DbNotes noteRacine = createNote("Note Racine", "Contenu Racine");
        DbDirectories subDir = createDirectory(20L, "Sous-Dossier");

        // Sous-dossier (ID: 20) -> contient une note enfant
        DbNotes noteEnfant = createNote("Note Enfant", "Contenu Enfant");

        when(directoriesRepository.findByUserIdAndParentDirectoryId(userId, 10L)).thenReturn(List.of(subDir));
        when(notesRepository.findByUserIdAndDirectoryId(userId, 10L)).thenReturn(List.of(noteRacine));
        when(directoriesRepository.findByUserIdAndParentDirectoryId(userId, 20L)).thenReturn(List.of());
        when(notesRepository.findByUserIdAndDirectoryId(userId, 20L)).thenReturn(List.of(noteEnfant));

        byte[] zipBytes = generateZip(userId, 10L);
        List<String> entryNames = listZipEntries(zipBytes);

        assertAll(
                () -> assertTrue(entryNames.contains("Note_Racine.md"), "Note racine manquante"),
                () -> assertTrue(entryNames.contains("Sous-Dossier/"), "Dossier manquant"),
                () -> assertTrue(entryNames.contains("Sous-Dossier/Note_Enfant.md"), "Note enfant mal placée"),
                () -> assertEquals(3, entryNames.size(), "Nombre total d'entrées incorrect")
        );
    }

    @Test
    @DisplayName("Doit reconstruire le chemin sur plusieurs niveaux")
    void testDeepRecursion() throws IOException {
        Long userId = 1L;
        DbDirectories dir1 = createDirectory(1L, "Niveau1");
        DbDirectories dir2 = createDirectory(2L, "Niveau2");
        DbNotes note = createNote("Fin", "Hello");

        when(directoriesRepository.findByUserIdAndParentDirectoryId(userId, 0L)).thenReturn(List.of(dir1));
        when(directoriesRepository.findByUserIdAndParentDirectoryId(userId, 1L)).thenReturn(List.of(dir2));
        when(notesRepository.findByUserIdAndDirectoryId(userId, 2L)).thenReturn(List.of(note));

        List<String> entries = listZipEntries(generateZip(userId, 0L));
        assertTrue(entries.contains("Niveau1/Niveau2/Fin.md"));
    }

    // --- TESTS DE SÉCURITÉ ET NOMMAGE ---

    @Test
    @DisplayName("Doit corriger les caractères spéciaux")
    void testSanitize() throws IOException {
        Long userId = 1L;
        DbNotes noteSpeciale = createNote("Ma Note @ Spéciale!", null);
        DbDirectories dirDanger = createDirectory(666L, "../../etc/passwd");

        when(notesRepository.findByUserIdAndDirectoryId(userId, 1L)).thenReturn(List.of(noteSpeciale));
        when(directoriesRepository.findByUserIdAndParentDirectoryId(userId, 1L)).thenReturn(List.of(dirDanger));

        List<String> entries = listZipEntries(generateZip(userId, 1L));

        assertAll(
                () -> assertTrue(entries.stream().anyMatch(e -> e.startsWith("Ma_Note___Sp_ciale_")), "La note n'est pas sanitizée"),
                () -> assertFalse(entries.stream().anyMatch(e -> e.contains("/etc/")), "Faille de sécurité : Path Traversal détecté")
        );
    }

    // --- TESTS DES CAS LIMITES (EDGE CASES) ---

    @Test
    @DisplayName("Un dossier sans contenu doit produire un ZIP vide")
    void testEmptyDirectory() throws IOException {
        when(directoriesRepository.findByUserIdAndParentDirectoryId(anyLong(), any())).thenReturn(List.of());
        when(notesRepository.findByUserIdAndDirectoryId(anyLong(), any())).thenReturn(List.of());

        List<String> entries = listZipEntries(generateZip(99L, 100L));
        assertTrue(entries.isEmpty());
    }

    @Test
    @DisplayName("Doit créer un fichier de 0 octet sans planter")
    void testNoteWithNullContent() throws IOException {
        DbNotes noteNull = createNote("Vide", null);
        when(notesRepository.findByUserIdAndDirectoryId(anyLong(), any())).thenReturn(List.of(noteNull));

        byte[] zipData = generateZip(1L, 1L);

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipData))) {
            ZipEntry entry = zis.getNextEntry();
            assertNotNull(entry);
            byte[] content = zis.readAllBytes();
            assertEquals(0, content.length, "Le contenu de la note devrait être vide");
        }
    }

    // --- MÉTHODES UTILITAIRES (HELPERS) ---

    private DbNotes createNote(String name, String content) {
        DbNotes note = new DbNotes();
        note.name = name;
        note.content = content;
        return note;
    }

    private DbDirectories createDirectory(Long id, String name) {
        DbDirectories dir = new DbDirectories();
        dir.id = id;
        dir.name = name;
        return dir;
    }

    private byte[] generateZip(Long userId, Long parentId) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            exportZipService.writeDirectoryToZip(userId, parentId, "", zos);
        }
        return baos.toByteArray();
    }

    private List<String> listZipEntries(byte[] zipData) throws IOException {
        List<String> names = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipData))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                names.add(entry.getName());
                zis.closeEntry();
            }
        }
        return names;
    }
}