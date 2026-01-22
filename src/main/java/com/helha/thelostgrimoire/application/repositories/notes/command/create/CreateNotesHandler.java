package com.helha.thelostgrimoire.application.repositories.notes.command.create;

import com.helha.thelostgrimoire.application.repositories.utils.CurrentUserContext;
import com.helha.thelostgrimoire.application.repositories.utils.ICommandHandler;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories;
import com.helha.thelostgrimoire.infrastructure.directories.IDirectoriesRepository;
import com.helha.thelostgrimoire.infrastructure.notes.DbNotes;
import com.helha.thelostgrimoire.infrastructure.notes.INotesRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class CreateNotesHandler implements ICommandHandler<CreateNotesInput, CreateNotesOutput> {
    private final INotesRepository notesRepository;
    private final IDirectoriesRepository directoriesRepository;
    private final ModelMapper modelMapper;

    public CreateNotesHandler(INotesRepository notesRepository, IDirectoriesRepository directoriesRepository, ModelMapper modelMapper) {
        this.notesRepository = notesRepository;
        this.directoriesRepository = directoriesRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public CreateNotesOutput handle(CreateNotesInput request) {

        Long userId = CurrentUserContext.getUserId();
        LocalDateTime now = LocalDateTime.now();

        // Variable pour stocker l'ID final du dossier (soit celui demandé, soit la racine)
        Long targetDirectoryId = request.directoryId;

        // 1. Détermination du dossier cible
        if (targetDirectoryId == null || targetDirectoryId == 0) {
            // CAS 1 : Pas d'ID fourni -> On récupère le dossier RACINE de l'utilisateur
            DbDirectories rootDir = directoriesRepository.findByUserIdAndIsRootTrue(userId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Critical Error: User root directory not found"
                    ));
            targetDirectoryId = rootDir.id;

        } else {
            // CAS 2 : Un ID est fourni -> On vérifie qu'il existe et appartient au user
            DbDirectories directory = directoriesRepository.findById(targetDirectoryId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Directory not found"
                    ));

            if (!directory.userId.equals(userId)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "You cannot create a note in a directory that is not yours"
                );
            }
        }

        // --- NOUVELLE VÉRIFICATION : Unicité du nom dans le dossier ---
        boolean nameExists = notesRepository.existsByNameAndDirectoryId(request.name, targetDirectoryId);

        if (nameExists) {
            // On renvoie une erreur 409 Conflict
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A note with this name already exists in the destination directory"
            );
        }
        // --------------------------------------------------------------

        // 2. Création de l'entité
        DbNotes entity = new DbNotes();
        entity.userId = userId;
        entity.directoryId = targetDirectoryId;
        entity.name = request.name;
        entity.content = ""; // Contenu vide par défaut
        entity.createdAt = now;
        entity.updatedAt = now;

        // 3. Sauvegarde
        DbNotes saved = notesRepository.save(entity);

        return modelMapper.map(saved, CreateNotesOutput.class);
    }
}