package br.com.tt.vote.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class SessionInProgressException extends ResponseStatusException {

    public SessionInProgressException() {
        super(HttpStatus.CONFLICT,
                "A sessão de votação desta pauta ainda está aberta.");
    }
}