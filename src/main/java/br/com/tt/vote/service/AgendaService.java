package br.com.tt.vote.service;

import br.com.tt.vote.model.Agenda;
import br.com.tt.vote.repository.AgendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
public class AgendaService {

    @Autowired
    private AgendaRepository agendaRepository;

    private static final int DEFAULT_SESSION_DURATION = 1;

    public Agenda create(Agenda agenda) {
        return this.agendaRepository.save(agenda);
    }

    public Agenda findById(Long id) {
        // TODO adicionar tratamento para não encontrado
        return this.agendaRepository.findById(id).orElse(null);
    }

    public void startSession(Long id, Long duration) {
        // TODO Criar exceção personalizada
        Agenda agenda = this.agendaRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pauta não encontrada.")
        );

        if (Objects.nonNull(agenda.getStartSessionIn()) && Objects.nonNull(agenda.getEndOfSessionIn())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("A sessão desta pauta já foi iniciada e se encerra no dia %s às %s.",
                            agenda.getEndOfSessionIn().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            agenda.getEndOfSessionIn().format(DateTimeFormatter.ofPattern("hh:mm:ss"))
                    ));
        }

        LocalDateTime startSession = LocalDateTime.now();
        LocalDateTime endOfSession = startSession.plusMinutes(
                Objects.nonNull(duration)? duration : DEFAULT_SESSION_DURATION);

        agenda.setStartSessionIn(startSession);
        agenda.setEndOfSessionIn(endOfSession);

        this.agendaRepository.save(agenda);
    }
}
