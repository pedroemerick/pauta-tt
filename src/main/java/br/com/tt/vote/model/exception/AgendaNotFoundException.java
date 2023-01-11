package br.com.tt.vote.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AgendaNotFoundException extends ResponseStatusException {

    public AgendaNotFoundException(Long agendaId) {
        super(HttpStatus.NOT_FOUND,
                String.format("Pauta de ID %d não encontrado.", agendaId));
    }
}
