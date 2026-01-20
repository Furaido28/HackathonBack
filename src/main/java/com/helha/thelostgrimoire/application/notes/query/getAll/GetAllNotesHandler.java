package com.helha.thelostgrimoire.application.notes.query.getAll;

import com.helha.thelostgrimoire.application.utils.IQueryHandler;
import com.helha.thelostgrimoire.infrastructure.notes.DbNotes;
import com.helha.thelostgrimoire.infrastructure.notes.INotesRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllNotesHandler implements IQueryHandler<GetAllNotesOutput> {
    private final INotesRepository notesRepository;
    private final ModelMapper modelMapper;

    public GetAllNotesHandler(INotesRepository notesRepository, ModelMapper modelMapper) {
        this.notesRepository = notesRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public GetAllNotesOutput handle() {
        List<DbNotes> entities = notesRepository.findAll();

        GetAllNotesOutput output = new GetAllNotesOutput();

        for (DbNotes entity : entities) {
            output.notes.add(modelMapper.map(entity, GetAllNotesOutput.Note.class));
        }

        return output;
    }
}
