package br.com.tt.vote.service;

import br.com.tt.vote.model.*;
import br.com.tt.vote.repository.AgendaRepository;
import br.com.tt.vote.repository.ResultRepository;
import br.com.tt.vote.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AgendaService {

    private static final int DEFAULT_SESSION_DURATION = 1;

    private AgendaRepository agendaRepository;
    private VoteRepository voteRepository;
    private ResultRepository resultRepository;

    @Autowired
    public AgendaService(AgendaRepository agendaRepository,
                         VoteRepository voteRepository,
                         ResultRepository resultRepository) {
        this.agendaRepository = agendaRepository;
        this.voteRepository = voteRepository;
        this.resultRepository = resultRepository;
    }

    public Agenda create(Agenda agenda) {
        agenda.setAccountedResult(false);
        return this.agendaRepository.save(agenda);
    }

    public Agenda findById(Long agendaId) {
        // TODO adicionar tratamento para não encontrado
        return this.agendaRepository.findById(agendaId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pauta não encontrada.")
        );
    }

    public void startSession(Long agendaId, Long duration) {
        // TODO Criar exceção personalizada
        Agenda agenda = this.findById(agendaId);

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

    public void vote(Long agendaId, List<Vote> votes) {
        Agenda agenda = this.findById(agendaId);

        if (Objects.isNull(agenda.getStartSessionIn()) || Objects.isNull(agenda.getEndOfSessionIn())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A sessão de votação desta pauta não está aberta no momento.");
        }

        LocalDateTime localDateTime = LocalDateTime.now();
        if (localDateTime.isAfter(agenda.getEndOfSessionIn())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A sessão de votação desta pauta já foi encerrada.");
        }

        List<Question> questions = agenda.getQuestions();

        Set<Long> numQuestionsOfAgenda = questions.stream()
                .map(Question::getNumber)
                .collect(Collectors.toSet());

        Set<Long> numQuestionsOfVote = votes.stream()
                .map(vote -> vote.getQuestion().getNumber())
                .collect(Collectors.toSet());

//        Se necessário que o associado vote em todas as questões de uma só vez,
//        basta retirar o comentário da linha abaixo
//        this.checkIfReceiveVotesForAllQuestions(numQuestionsOfAgenda, numQuestionsOfVote);

        this.checkIfExistQuestionsOfVotes(numQuestionsOfAgenda, numQuestionsOfVote);

        for(Vote vote : votes) {
            this.checkIfAssociateAlreadyVotedInQuestion(questions, vote.getAssociateId(),
                    vote.getQuestion().getNumber());
        }

        this.voteRepository.saveAll(votes);
    }

    private void checkIfReceiveVotesForAllQuestions(Set<Long> numQuestionsOfAgenda, Set<Long> numQuestionsOfVote) {
        if (!numQuestionsOfVote.containsAll(numQuestionsOfAgenda)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O associado não votou em todas as questões da pauta.");
        }
    }

    private void checkIfExistQuestionsOfVotes(Set<Long> numQuestionsOfAgenda, Set<Long> numQuestionsOfVote) {
        if (!numQuestionsOfAgenda.containsAll(numQuestionsOfVote)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Existem votos em questões não definidas na pauta.");
        }
    }

    private void checkIfAssociateAlreadyVotedInQuestion(List<Question> questionsOfAgenda, Long associateId,
                                                        Long numberOfQuestion) {

        Set<Long> associateIds = questionsOfAgenda.stream()
                .filter(question -> question.getNumber().equals(numberOfQuestion))
                .map(Question::getVotes)
                .flatMap(List::stream)
                .map(Vote::getAssociateId)
                .collect(Collectors.toSet());

        if (associateIds.contains(associateId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("O associado já votou na questão %d.", numberOfQuestion));
        }
    }

    public List<Result> getVoteResults(Long agendaId) {
        Agenda agenda = this.findById(agendaId);

        if (Objects.isNull(agenda.getStartSessionIn()) || Objects.isNull(agenda.getEndOfSessionIn())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A sessão de votação desta pauta ainda não foi iniciada.");
        }

        LocalDateTime localDateTime = LocalDateTime.now();
        if (localDateTime.isBefore(agenda.getEndOfSessionIn())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A sessão de votação desta pauta ainda está aberta.");
        }

        if (Objects.nonNull(agenda.getAccountedResult()) && agenda.getAccountedResult()) {
            return agenda.getQuestions().stream()
                    .map(Question::getResult)
                    .toList();
        }

        List<Result> results = new ArrayList<>();
        for (Question question : agenda.getQuestions()) {
            List<Boolean> votes = question.getVotes().stream()
                    .map(Vote::getInFavor)
                    .toList();

            long qntVotesInFavor = votes.stream().filter(inFavor -> inFavor.equals(true)).count();
            long qntVotesAgainst = votes.stream().filter(inFavor -> inFavor.equals(false)).count();

            FinalResultEnum finalResult;
            if (qntVotesInFavor > qntVotesAgainst) {
                finalResult = FinalResultEnum.APPROVED;
            } else if (qntVotesAgainst > qntVotesInFavor) {
                finalResult = FinalResultEnum.DISAPPROVED;
            } else {
                finalResult = FinalResultEnum.INCONCLUSIVE;
            }

            Result result = new Result();
            result.setQuestion(question);
            result.setQntVotesInFavor(qntVotesInFavor);
            result.setQntVotesAgainst(qntVotesAgainst);
            result.setFinalResult(finalResult);

            results.add(result);
        }

        results = this.resultRepository.saveAll(results);

        agenda.setAccountedResult(true);
        this.agendaRepository.save(agenda);

        return results;
    }
}
