package br.com.tt.vote.service;

import br.com.tt.vote.model.Agenda;
import br.com.tt.vote.repository.AgendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgendaService {

    @Autowired
    private AgendaRepository agendaRepository;

    public Agenda create(Agenda agenda) {
        return this.agendaRepository.save(agenda);
    }

    public Agenda findById(Long id) {
        return this.agendaRepository.findById(id).orElse(null);
    }
}
