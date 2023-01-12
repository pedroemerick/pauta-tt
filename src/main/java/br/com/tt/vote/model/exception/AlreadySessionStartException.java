package br.com.tt.vote.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AlreadySessionStartException extends ResponseStatusException {

    public AlreadySessionStartException(LocalDateTime endSession) {
        super(HttpStatus.CONFLICT,
                String.format("A sessão desta pauta já foi iniciada e se encerra no dia %s às %s.",
                        endSession.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        endSession.format(DateTimeFormatter.ofPattern("hh:mm:ss"))));
    }
}
