package br.com.tt.vote.controller;

import br.com.tt.vote.controller.openapi.AgendaApi;
import br.com.tt.vote.model.Agenda;
import br.com.tt.vote.model.mapper.AgendaMapper;
import br.com.tt.vote.model.openapi.AgendaDTO;
import br.com.tt.vote.service.AgendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

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
}
