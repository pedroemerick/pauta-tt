package br.com.tt.vote.service;

import br.com.tt.vote.TestUtils;
import br.com.tt.vote.config.GsonLocalDateTimeSerializer;
import br.com.tt.vote.model.Agenda;
import br.com.tt.vote.model.FinalResultEnum;
import br.com.tt.vote.model.Question;
import br.com.tt.vote.model.Vote;
import br.com.tt.vote.model.exception.*;
import br.com.tt.vote.repository.AgendaRepository;
import br.com.tt.vote.repository.QuestionRepository;
import br.com.tt.vote.repository.VoteRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.util.concurrent.ScheduledFuture;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.TaskScheduler;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class AgendaServiceTest {

    private final AgendaService agendaService;
    private final AgendaRepository agendaRepository;
    private final VoteRepository voteRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final QuestionRepository questionRepository;
    private final TaskScheduler taskScheduler;
    private final RedisTemplate<Long, String> redisTemplate;
    private final Gson gson;

    @Autowired
    public AgendaServiceTest() {
        this.agendaRepository = mock(AgendaRepository.class);
        this.voteRepository = mock(VoteRepository.class);
        this.kafkaTemplate = mock(KafkaTemplate.class);
        this.questionRepository = mock(QuestionRepository.class);
        this.taskScheduler = mock(TaskScheduler.class);
        this.redisTemplate = mock(RedisTemplate.class);
        this.agendaService = new AgendaService(this.agendaRepository,
                this.voteRepository, this.kafkaTemplate, this.questionRepository,
                this.taskScheduler, this.redisTemplate);

        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new GsonLocalDateTimeSerializer())
                .excludeFieldsWithoutExposeAnnotation()
                .create();
    }

    @Test
    void shouldReturnAgendaWhenCreateAgenda() {
        Agenda agendaMock = TestUtils.getAgendaMock();

        when(this.agendaRepository.save(any(Agenda.class))).thenReturn(agendaMock);

        Agenda agenda = this.agendaService.create(agendaMock);

        assertThat(agenda.getId(), is(Matchers.notNullValue()));
        assertThat(agenda.getTitle(), is(equalTo(agendaMock.getTitle())));
        assertThat(agenda.getNotes(), is(equalTo(agendaMock.getNotes())));
        assertThat(agenda.getAccountedResult(), is(equalTo(false)));
        assertThat(agenda.getQuestions().size(), is(equalTo(agendaMock.getQuestions().size())));
        assertThat(agenda.getQuestions().get(0).getNumber(), is(notNullValue()));
        assertThat(agenda.getQuestions().get(0).getTitle(), is(equalTo(agendaMock.getQuestions().get(0).getTitle())));
    }

    @Test
    void shouldReturnAgendaWhenFindById() {
        Agenda agendaMock = TestUtils.getAgendaMock();

        when(this.agendaRepository.findById(anyLong())).thenReturn(Optional.of(agendaMock));

        Agenda agenda = this.agendaService.findById(agendaMock.getId());

        assertThat(agenda.getId(), is(notNullValue()));
        assertThat(agenda.getTitle(), is(equalTo(agendaMock.getTitle())));
        assertThat(agenda.getNotes(), is(equalTo(agendaMock.getNotes())));
        assertThat(agenda.getQuestions().size(), is(equalTo(agendaMock.getQuestions().size())));
        assertThat(agenda.getQuestions().get(0).getNumber(), is(notNullValue()));
        assertThat(agenda.getQuestions().get(0).getTitle(), is(equalTo(agendaMock.getQuestions().get(0).getTitle())));
    }

    @Test
    void shouldReturnExceptionWhenFindByIdWithInvalidId() {
        when(this.agendaRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(AgendaNotFoundException.class, () ->
                this.agendaService.findById(1L));
    }

    @Test
    void shouldSuccessWhenStartSession() {
        Agenda agendaMock = TestUtils.getAgendaMock();
        long duration = 2L;

        when(this.agendaRepository.findById(anyLong())).thenReturn(Optional.of(agendaMock));
        when(this.agendaRepository.save(any(Agenda.class))).thenReturn(agendaMock);
        when(this.taskScheduler.schedule(any(Runnable.class), any(Date.class)))
                .thenReturn(mock(ScheduledFuture.class));
        when(this.redisTemplate.opsForValue()).thenReturn(mock(ValueOperations.class));

        this.agendaService.startSession(agendaMock.getId(), duration);

        assertThat(agendaMock.getStartSessionIn(), is(notNullValue()));
        assertThat(agendaMock.getEndOfSessionIn(), is(notNullValue()));
        assertThat(ChronoUnit.MINUTES.between(agendaMock.getStartSessionIn(), agendaMock.getEndOfSessionIn()),
                is(equalTo(duration)));

        verify(this.taskScheduler, times(1)).schedule(any(Runnable.class), any(Date.class));
        verify(this.redisTemplate, times(1)).opsForValue();
        verify(this.redisTemplate.opsForValue(), times(1)).set(anyLong(), anyString());
        verify(this.redisTemplate, times(1)).expireAt(anyLong(), any(Date.class));
    }

    @Test
    void shouldReturnExceptionWhenStartSessionWithAlreadyRunSession() {
        long duration = 2L;
        Agenda agendaMock = TestUtils.getAgendaMock();
        agendaMock.setStartSessionIn(LocalDateTime.now());
        agendaMock.setEndOfSessionIn(LocalDateTime.now().plusMinutes(duration));

        when(this.agendaRepository.findById(anyLong())).thenReturn(Optional.of(agendaMock));

        assertThrows(AlreadySessionStartException.class, () ->
                this.agendaService.startSession(agendaMock.getId(), duration)
        );
    }

    @Test
    void shouldReturnSuccessWhenVote() {
        Agenda agendaMock = TestUtils.getAgendaMock();
        agendaMock.setStartSessionIn(LocalDateTime.now());
        agendaMock.setEndOfSessionIn(LocalDateTime.now().plusHours(1));

        Vote voteMock = TestUtils.getVote();

        when(this.redisTemplate.opsForValue()).thenReturn(mock(ValueOperations.class));
        when(this.redisTemplate.opsForValue().get(anyLong())).thenReturn(null);
        when(this.agendaRepository.findById(anyLong())).thenReturn(Optional.of(agendaMock));
        when(this.voteRepository.saveAll(anyList())).thenReturn(List.of(voteMock));

        this.agendaService.vote(agendaMock.getId(), List.of(voteMock));

        verify(this.voteRepository, times(1)).saveAll(List.of(voteMock));
        verify(this.redisTemplate.opsForValue(), times(1)).get(anyLong());
        verify(this.redisTemplate.opsForValue(), times(1)).set(anyLong(), anyString());
    }

    @Test
    void shouldReturnExceptionWhenVoteWithNotStartedSession() {
        Agenda agendaMock = TestUtils.getAgendaMock();

        Vote voteMock = TestUtils.getVote();

        when(this.redisTemplate.opsForValue()).thenReturn(mock(ValueOperations.class));
        when(this.redisTemplate.opsForValue().get(anyLong())).thenReturn(null);
        when(this.agendaRepository.findById(anyLong())).thenReturn(Optional.of(agendaMock));

        assertThrows(NotStartedSessionException.class, () ->
                this.agendaService.vote(agendaMock.getId(), List.of(voteMock))
        );
    }

    @Test
    void shouldReturnExceptionWhenVoteWithClosedSession() {
        Agenda agendaMock = TestUtils.getAgendaMock();
        agendaMock.setStartSessionIn(LocalDateTime.now().minusHours(2));
        agendaMock.setEndOfSessionIn(LocalDateTime.now().minusHours(1));

        Vote voteMock = TestUtils.getVote();

        when(this.redisTemplate.opsForValue()).thenReturn(mock(ValueOperations.class));
        when(this.redisTemplate.opsForValue().get(anyLong())).thenReturn(null);
        when(this.agendaRepository.findById(anyLong())).thenReturn(Optional.of(agendaMock));

        assertThrows(ClosedSessionException.class, () ->
                this.agendaService.vote(agendaMock.getId(), List.of(voteMock))
        );
    }

    @Test
    void shouldReturnExceptionWhenVoteInInvalidQuestion() {
        Vote voteMock = TestUtils.getVote();
        voteMock.getQuestion().setNumber(2L);

        Agenda agendaMock = TestUtils.getAgendaMock();
        agendaMock.setStartSessionIn(LocalDateTime.now());
        agendaMock.setEndOfSessionIn(LocalDateTime.now().plusHours(1));

        when(this.redisTemplate.opsForValue()).thenReturn(mock(ValueOperations.class));
        when(this.redisTemplate.opsForValue().get(anyLong())).thenReturn(gson.toJson(agendaMock));

        assertThrows(QuestionNotOnTheAgendaException.class, () ->
                this.agendaService.vote(agendaMock.getId(), List.of(voteMock))
        );
    }

    @Test
    void shouldReturnExceptionWhenVoteTwiceInTheSameQuestion() {
        Vote voteMock = TestUtils.getVote();

        Agenda agendaMock = TestUtils.getAgendaMock();
        agendaMock.setStartSessionIn(LocalDateTime.now());
        agendaMock.setEndOfSessionIn(LocalDateTime.now().plusHours(1));
        agendaMock.getQuestions().get(0).setVotes(List.of(voteMock));

        when(this.redisTemplate.opsForValue()).thenReturn(mock(ValueOperations.class));
        when(this.redisTemplate.opsForValue().get(anyLong())).thenReturn(gson.toJson(agendaMock));

        assertThrows(VoteTwiceQuestionException.class, () ->
                this.agendaService.vote(agendaMock.getId(), List.of(voteMock))
        );
    }

    @Test
    void shouldReturnResultWhenGetVoteResults() {
        Agenda agendaMock = TestUtils.getAgendaMock();
        agendaMock.setStartSessionIn(LocalDateTime.now().minusHours(2));
        agendaMock.setEndOfSessionIn(LocalDateTime.now().minusHours(1));
        agendaMock.setQuestions(null);
        agendaMock.setAccountedResult(false);

        Question questionApprovedMock = TestUtils.getQuestionMock();
        questionApprovedMock.setNumber(1L);

        Vote voteFavorMock = new Vote();
        voteFavorMock.setAssociateId(1L);
        voteFavorMock.setQuestion(questionApprovedMock);
        voteFavorMock.setInFavor(true);
        questionApprovedMock.setVotes(List.of(voteFavorMock));

        Question questionDisapprovedMock = TestUtils.getQuestionMock();
        questionApprovedMock.setNumber(2L);

        Vote voteAgainstMock = new Vote();
        voteAgainstMock.setAssociateId(1L);
        voteAgainstMock.setQuestion(questionDisapprovedMock);
        voteAgainstMock.setInFavor(false);
        questionDisapprovedMock.setVotes(List.of(voteAgainstMock));

        Question questionInconclusiveMock = TestUtils.getQuestionMock();
        questionApprovedMock.setNumber(3L);

        when(this.agendaRepository.findById(anyLong())).thenReturn(Optional.of(agendaMock));
        when(this.questionRepository.findByAgendaId(anyLong())).thenReturn(
                List.of(questionApprovedMock, questionDisapprovedMock, questionInconclusiveMock));
        when(this.agendaRepository.save(any(Agenda.class))).thenReturn(agendaMock);

        List<Question> questions = this.agendaService.getVoteResults(agendaMock.getId());

        assertThat(questions.size(), is(equalTo(3)));
        assertThat(questions.get(0).getAgenda().getId(), is(equalTo(agendaMock.getId())));
        assertThat(questions.get(0).getQntVotesInFavor(), is(equalTo(1L)));
        assertThat(questions.get(0).getQntVotesAgainst(), is(equalTo(0L)));
        assertThat(questions.get(0).getFinalResult(), is(equalTo(FinalResultEnum.APPROVED)));
        assertThat(questions.get(1).getQntVotesInFavor(), is(equalTo(0L)));
        assertThat(questions.get(1).getQntVotesAgainst(), is(equalTo(1L)));
        assertThat(questions.get(1).getFinalResult(), is(equalTo(FinalResultEnum.DISAPPROVED)));
        assertThat(questions.get(2).getQntVotesInFavor(), is(equalTo(0L)));
        assertThat(questions.get(2).getQntVotesAgainst(), is(equalTo(0L)));
        assertThat(questions.get(2).getFinalResult(), is(equalTo(FinalResultEnum.INCONCLUSIVE)));

        verify(this.kafkaTemplate, times(1)).send(any(), anyString(), anyString());
    }

    @Test
    void shouldReturnResultWhenGetVoteResultsInAgendaWithAccountedResults() {
        Agenda agendaMock = TestUtils.getAgendaMock();
        agendaMock.setStartSessionIn(LocalDateTime.now().minusHours(2));
        agendaMock.setEndOfSessionIn(LocalDateTime.now().minusHours(1));
        agendaMock.setQuestions(null);
        agendaMock.setAccountedResult(true);

        Question questionMock = TestUtils.getQuestionCompleteMock();

        when(this.agendaRepository.findById(anyLong())).thenReturn(Optional.of(agendaMock));
        when(this.questionRepository.findByAgendaId(anyLong())).thenReturn(List.of(questionMock));

        List<Question> questions = this.agendaService.getVoteResults(agendaMock.getId());

        assertThat(questions.size(), is(equalTo(1)));
        assertThat(questions.get(0).getAgenda().getId(), is(equalTo(agendaMock.getId())));
        assertThat(questions.get(0).getQntVotesInFavor(), is(equalTo(questionMock.getQntVotesInFavor())));
        assertThat(questions.get(0).getQntVotesAgainst(), is(equalTo(questionMock.getQntVotesAgainst())));
        assertThat(questions.get(0).getFinalResult(), is(equalTo(questionMock.getFinalResult())));

        verify(this.agendaRepository, times(0)).save(any(Agenda.class));
        verify(this.kafkaTemplate, times(0)).send(any(), anyString(), anyString());
    }

    @Test
    void shouldReturnExceptionWhenGetVoteResultsWithNotStartedSession() {
        Agenda agendaMock = TestUtils.getAgendaMock();
        agendaMock.setQuestions(null);
        agendaMock.setAccountedResult(false);

        Question questionMock = TestUtils.getQuestionMock();
        questionMock.setNumber(1L);

        when(this.agendaRepository.findById(anyLong())).thenReturn(Optional.of(agendaMock));
        when(this.questionRepository.findByAgendaId(anyLong())).thenReturn(
                List.of(questionMock));

        assertThrows(NotStartedSessionException.class, () ->
                this.agendaService.getVoteResults(agendaMock.getId())
        );
    }

    @Test
    void shouldReturnExceptionWhenGetVoteResultsWithSessionInProgress() {
        Agenda agendaMock = TestUtils.getAgendaMock();
        agendaMock.setStartSessionIn(LocalDateTime.now());
        agendaMock.setEndOfSessionIn(LocalDateTime.now().plusHours(1));
        agendaMock.setQuestions(null);
        agendaMock.setAccountedResult(false);

        Question questionMock = TestUtils.getQuestionMock();
        questionMock.setNumber(1L);

        when(this.agendaRepository.findById(anyLong())).thenReturn(Optional.of(agendaMock));
        when(this.questionRepository.findByAgendaId(anyLong())).thenReturn(
                List.of(questionMock));

        assertThrows(SessionInProgressException.class, () ->
                this.agendaService.getVoteResults(agendaMock.getId())
        );
    }
}
