package br.com.tt.vote.service;

import br.com.tt.vote.config.GsonLocalDateTimeSerializer;
import br.com.tt.vote.model.Agenda;
import br.com.tt.vote.model.FinalResultEnum;
import br.com.tt.vote.model.Question;
import br.com.tt.vote.model.Vote;
import br.com.tt.vote.model.exception.*;
import br.com.tt.vote.model.mapper.ResultMapper;
import br.com.tt.vote.repository.AgendaRepository;
import br.com.tt.vote.repository.QuestionRepository;
import br.com.tt.vote.repository.VoteRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AgendaService {

    private static final int DEFAULT_SESSION_DURATION = 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(AgendaService.class);

    private AgendaRepository agendaRepository;
    private VoteRepository voteRepository;
    private QuestionRepository questionRepository;
    private TaskScheduler taskScheduler;
    private RedisTemplate<Long, String> redisTemplate;
    private Gson gson;

    @Value("${spring.kafka.topic-name}")
    private String kafkaTopicName;
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public AgendaService(AgendaRepository agendaRepository,
                         VoteRepository voteRepository,
                         KafkaTemplate<String, String> kafkaTemplate,
                         QuestionRepository questionRepository,
                         TaskScheduler taskScheduler,
                         @Qualifier("redisTemplate") RedisTemplate<Long, String> redisTemplate) {
        this.agendaRepository = agendaRepository;
        this.voteRepository = voteRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.questionRepository = questionRepository;
        this.taskScheduler = taskScheduler;
        this.redisTemplate = redisTemplate;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new GsonLocalDateTimeSerializer())
                .excludeFieldsWithoutExposeAnnotation()
                .create();
    }

    public Agenda create(Agenda agenda) {
        agenda.setAccountedResult(false);
        return this.agendaRepository.save(agenda);
    }

    public Agenda findById(Long agendaId) {
        return this.agendaRepository.findById(agendaId).orElseThrow(() ->
                new AgendaNotFoundException(agendaId)
        );
    }

    public void startSession(Long agendaId, Long duration) {
        Agenda agenda = this.findById(agendaId);

        if (Objects.nonNull(agenda.getStartSessionIn()) && Objects.nonNull(agenda.getEndOfSessionIn())) {
            throw new AlreadySessionStartException(agenda.getEndOfSessionIn());
        }

        LocalDateTime startSession = LocalDateTime.now();
        LocalDateTime endOfSession = startSession.plusMinutes(
                Objects.nonNull(duration)? duration : DEFAULT_SESSION_DURATION);

        agenda.setStartSessionIn(startSession);
        agenda.setEndOfSessionIn(endOfSession);

        this.agendaRepository.save(agenda);

        taskScheduler.schedule(() -> {
            LOGGER.info(String.format("Iniciando cálculo dos resultados da pauta %d", agendaId));
            getVoteResults(agendaId);
            LOGGER.info(String.format("Finalizado cálculo dos resultados da pauta %d", agendaId));
        }, Date.from(endOfSession.atZone(ZoneId.systemDefault()).toInstant()));

        try {
            this.redisTemplate.opsForValue().set(agendaId, gson.toJson(agenda));
            this.redisTemplate.expireAt(agendaId, Date.from(endOfSession.atZone(ZoneId.systemDefault()).toInstant()));
        } catch (Exception e) {
            LOGGER.error("Erro ao se comunicar com o Redis.");
        }
    }

    public void vote(Long agendaId, List<Vote> votes) {
        Agenda agenda = null;
        try {
            agenda = gson.fromJson(redisTemplate.opsForValue().get(agendaId), Agenda.class);
        } catch (Exception e) {
            LOGGER.error("Erro ao se comunicar com o Redis.");
        }

        if (Objects.isNull(agenda)) {
            agenda = this.findById(agendaId);
        }

        if (Objects.isNull(agenda.getStartSessionIn()) || Objects.isNull(agenda.getEndOfSessionIn())) {
            throw new NotStartedSessionException();
        }

        LocalDateTime localDateTime = LocalDateTime.now();
        if (localDateTime.isAfter(agenda.getEndOfSessionIn())) {
            throw new ClosedSessionException();
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

            Question question = questions.stream()
                    .filter(qq -> qq.getNumber().equals(vote.getQuestion().getNumber()))
                    .findFirst()
                    .get();

            if(Objects.isNull(question.getVotes())) {
                question.setVotes(new ArrayList<>(){{add(vote);}});
            } else {
                question.getVotes().add(vote);
            }
        }

        this.voteRepository.saveAll(votes);

        try {
            this.redisTemplate.opsForValue().set(agendaId, gson.toJson(agenda));
        } catch (Exception e) {
            LOGGER.error("Erro ao se comunicar com o Redis.");
        }
    }

    private void checkIfReceiveVotesForAllQuestions(Set<Long> numQuestionsOfAgenda, Set<Long> numQuestionsOfVote) {
        if (!numQuestionsOfVote.containsAll(numQuestionsOfAgenda)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O associado não votou em todas as questões da pauta.");
        }
    }

    private void checkIfExistQuestionsOfVotes(Set<Long> numQuestionsOfAgenda, Set<Long> numQuestionsOfVote) {
        if (!numQuestionsOfAgenda.containsAll(numQuestionsOfVote)) {
            throw new QuestionNotOnTheAgendaException();
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
            throw new VoteTwiceQuestionException(numberOfQuestion);
        }
    }

    public List<Question> getVoteResults(Long agendaId) {
        Agenda agenda = this.findById(agendaId);
        agenda.setQuestions(this.questionRepository.findByAgendaId(agendaId));

        if (Objects.isNull(agenda.getStartSessionIn()) || Objects.isNull(agenda.getEndOfSessionIn())) {
            throw new NotStartedSessionException();
        }

        LocalDateTime localDateTime = LocalDateTime.now();
        if (localDateTime.isBefore(agenda.getEndOfSessionIn())) {
            throw new SessionInProgressException();
        }

        if (Objects.nonNull(agenda.getAccountedResult()) && agenda.getAccountedResult()) {
            return agenda.getQuestions();
        }

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

            question.setQntVotesInFavor(qntVotesInFavor);
            question.setQntVotesAgainst(qntVotesAgainst);
            question.setFinalResult(finalResult);
        }

        agenda.setAccountedResult(true);
        this.agendaRepository.save(agenda);

        try {
            this.kafkaTemplate.send(this.kafkaTopicName, ResultMapper.INSTANCE.map(agenda.getQuestions()).toString());
        } catch (KafkaException e) {
            LOGGER.error(String.format("Erro ao enviar resultado da pauta '%s' ao tópico do Kafka.", agendaId));
        }

        return agenda.getQuestions();
    }
}
