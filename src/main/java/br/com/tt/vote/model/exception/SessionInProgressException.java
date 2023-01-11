package br.com.tt.vote.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class SessionInProgressException extends ResponseStatusException {

    public SessionInProgressException() {
        super(HttpStatus.BAD_REQUEST,
                "A sessão de votação desta pauta ainda está aberta.");
    }
}