package br.com.tt.vote.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class QuestionNotOnTheAgendaException extends ResponseStatusException {

    public QuestionNotOnTheAgendaException() {
        super(HttpStatus.CONFLICT,
                "Existem votos em questões não definidas na pauta.");
    }
}
