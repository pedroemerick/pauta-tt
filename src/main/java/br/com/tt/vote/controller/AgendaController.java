package br.com.tt.vote.controller;

import br.com.tt.vote.controller.openapi.AgendaApi;
import br.com.tt.vote.model.Agenda;
import br.com.tt.vote.model.Question;
import br.com.tt.vote.model.mapper.AgendaMapper;
import br.com.tt.vote.model.mapper.ResultMapper;
import br.com.tt.vote.model.mapper.VoteMapper;
import br.com.tt.vote.model.openapi.AgendaDTO;
import br.com.tt.vote.model.openapi.ResultDTO;
import br.com.tt.vote.model.openapi.VoteEntryDTO;
import br.com.tt.vote.service.AgendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AgendaController implements AgendaApi {

    @Autowired
    private AgendaService agendaService;

    @Override
    public ResponseEntity<AgendaDTO> createAgenda(AgendaDTO agendaDTO) {
        Agenda agenda = this.agendaService.create(AgendaMapper.INSTANCE.map(agendaDTO));

        return ResponseEntity.ok().body(AgendaMapper.INSTANCE.map(agenda));
    }

    @Override
    public ResponseEntity<AgendaDTO> findAgendaById(Long id) {
        Agenda agenda = this.agendaService.findById(id);

        return ResponseEntity.ok().body(AgendaMapper.INSTANCE.map(agenda));
    }

    @Override
    public ResponseEntity<Void> startSession(Long id, Long duration) {
        this.agendaService.startSession(id, duration);

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> vote(Long id, VoteEntryDTO voteEntryDTO) {
        this.agendaService.vote(id, VoteMapper.INSTANCE.map(voteEntryDTO));

        return ResponseEntity.accepted().build();
    }

    @Override
    public ResponseEntity<ResultDTO> getVoteResults(Long id) {
        List<Question> questions = this.agendaService.getVoteResults(id);

        return ResponseEntity.ok().body(ResultMapper.INSTANCE.map(questions));
    }
}
