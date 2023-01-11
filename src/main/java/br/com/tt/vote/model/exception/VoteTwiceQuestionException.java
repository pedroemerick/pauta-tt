package br.com.tt.vote.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class VoteTwiceQuestionException extends ResponseStatusException {

    public VoteTwiceQuestionException(Long numberOfQuestion) {
        super(HttpStatus.BAD_REQUEST,
                String.format("O associado já votou na questão %d.", numberOfQuestion));
    }
}