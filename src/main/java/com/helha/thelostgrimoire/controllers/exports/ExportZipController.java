package com.helha.thelostgrimoire.controllers.exports;

import com.helha.thelostgrimoire.application.services.ExportZipService;
import com.helha.thelostgrimoire.controllers.directories.exceptions.DirectoriesNotFound;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.directories.IDirectoriesRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.zip.ZipOutputStream;

@Tag(name = "Export", description = "Exporting endpoints")
@RestController
@RequestMapping("/api/export")
public class ExportZipController {

    /**
     * Services for ZIP generation and repository for directory data access
     */
    private final ExportZipService exportZipService;
    private final IDirectoriesRepository directoriesRepository;
    public ExportZipController(ExportZipService exportZipService, IDirectoriesRepository directoriesRepository) {
        this.exportZipService = exportZipService;
        this.directoriesRepository = directoriesRepository;
    }

    @GetMapping(value = "/zip/me", produces = "application/zip")
    public ResponseEntity<StreamingResponseBody> exportAllNotes() {
        /**
         * Extract the user ID from the security context within the main thread
         * to avoid context loss during asynchronous streaming
         */
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        final Long userId = Long.parseLong(name);

        /**
         * Locate the root directory for the current user
         */
        DbDirectories rootDir = directoriesRepository.findByUserIdAndIsRootTrue(userId)
                .orElseThrow(() -> new RuntimeException("Root directory not found"));

        /**
         * Return a streaming response to write the ZIP content directly to the output stream
         */
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"archive.zip\"")
                .body(out -> {
                    try (ZipOutputStream zos = new ZipOutputStream(out)) {
                        exportZipService.writeDirectoryToZip(userId, rootDir.id, "", zos);
                    }
                });
    }

    @GetMapping(value = "/zip/{directoryId}", produces = "application/zip")
    public ResponseEntity<StreamingResponseBody> exportDirectoryNotes(@PathVariable Long directoryId) {
        /**
         * Get the authenticated user ID for ownership validation
         */
        final Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        /**
         * Verify directory existence before starting the stream
         */
        DbDirectories rootDir = directoriesRepository.findById(directoryId)
                .orElseThrow(() -> new DirectoriesNotFound(directoryId));

        /**
         * Stream the specific directory content as a ZIP file
         */
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=archive_" + rootDir.name + ".zip")
                .body(out -> {
                    try (ZipOutputStream zos = new ZipOutputStream(out)) {
                        exportZipService.writeDirectoryToZip(userId, rootDir.id, "", zos);
                    }
                });
    }
}