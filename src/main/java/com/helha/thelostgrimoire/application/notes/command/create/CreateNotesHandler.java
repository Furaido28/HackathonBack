package com.helha.thelostgrimoire.application.notes.command.create;

import com.helha.thelostgrimoire.application.utils.CurrentUserContext;
import com.helha.thelostgrimoire.application.utils.ICommandHandler;
import com.helha.thelostgrimoire.infrastructure.directories.DbDirectories; // <--- Import ajouté
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

        // ------------------------------------------------------------------
        // LOGIQUE DE SÉLECTION DU DOSSIER (Racine vs Spécifique)
        // ------------------------------------------------------------------

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

            // On utilise findById plutôt que exists pour distinguer 404 (Not Found) et 403 (Forbidden)
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

        // ------------------------------------------------------------------
        // CRÉATION DE LA NOTE
        // ------------------------------------------------------------------

        DbNotes entity = new DbNotes();
        entity.userId = userId;
        entity.directoryId = targetDirectoryId; // On utilise l'ID calculé juste au-dessus
        entity.name = request.name;

        // On s'assure que le contenu n'est jamais null (prévention NullPointerException sur les calculs de métadonnées)
        entity.content = "";

        entity.createdAt = now;
        entity.updatedAt = now;

        DbNotes saved = notesRepository.save(entity);

        return modelMapper.map(saved, CreateNotesOutput.class);
    }
}