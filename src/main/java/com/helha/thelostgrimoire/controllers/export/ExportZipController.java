package com.helha.thelostgrimoire.controllers.export;

import com.helha.thelostgrimoire.application.services.ExportZipService;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.directories.IDirectoriesRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.zip.ZipOutputStream;

@Tag(name = "Export", description = "Exporting endpoints")
@RestController
@RequestMapping("/api/export")
public class ExportZipController {

    private final ExportZipService exportZipService;
    private final IDirectoriesRepository directoriesRepository;
    public ExportZipController(ExportZipService exportZipService, IDirectoriesRepository directoriesRepository) {
        this.exportZipService = exportZipService;
        this.directoriesRepository = directoriesRepository;
    }

    @GetMapping(value = "/zip/me", produces = "application/zip")
    public ResponseEntity<StreamingResponseBody> exportAllNotes() {

        Long userId = Long.parseLong(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        DbDirectories rootDir = directoriesRepository.findByUserIdAndIsRootTrue(userId)
                .orElseThrow(() -> new RuntimeException("Root directory not found"));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"grimoire_export.zip\"")
                .body(out -> {
                    try (ZipOutputStream zos = new ZipOutputStream(out)) {
                        exportZipService.writeDirectoryToZip(userId, rootDir.id, "", zos);
                    }
                });
    }
}