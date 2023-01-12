package br.com.tt.vote.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class NotStartedSessionException extends ResponseStatusException {

    public NotStartedSessionException() {
        super(HttpStatus.CONFLICT,
                "A sessão de votação desta pauta ainda não foi aberta.");
    }
}
