package br.com.tt.vote.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ClosedSessionException extends ResponseStatusException {

    public ClosedSessionException() {
        super(HttpStatus.BAD_REQUEST,
                "A sessão de votação desta pauta já foi encerrada.");
    }
}
