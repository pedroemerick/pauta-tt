package br.com.tt.vote.controller;

import br.com.tt.vote.TestUtils;
import br.com.tt.vote.model.Agenda;
import br.com.tt.vote.model.Question;
import br.com.tt.vote.model.exception.AgendaNotFoundException;
import br.com.tt.vote.service.AgendaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AgendaControllerTest {

    private final AgendaService agendaService;
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Value("classpath:request/createAgendaRequest.json")
    private Resource createAgendaRequestResource;

    @Value("classpath:request/createAgendaRequestWithoutTitle.json")
    private Resource createAgendaRequestWithoutTitleResource;

    @Value("classpath:request/voteRequest.json")
    private Resource voteRequestResource;

    @Value("classpath:request/voteRequestWithoutRequiredField.json")
    private Resource voteRequestWithoutRequiredFieldResource;

    @Autowired
    public AgendaControllerTest(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.agendaService = mock(AgendaService.class);

        AgendaController agendaController = new AgendaController(agendaService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(agendaController).build();
    }

    @Test
    void shouldReturnSuccessWhenCreateAgenda() throws Exception {
        final String request = StreamUtils.copyToString(
                this.createAgendaRequestResource.getInputStream(),
                StandardCharsets.UTF_8);

        final Agenda agendaMock = this.objectMapper.readValue(request, Agenda.class);

        when(this.agendaService.create(any(Agenda.class))).thenReturn(agendaMock);

        this.mockMvc.perform(
                post("/v1/agenda")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is(equalTo(agendaMock.getTitle()))))
                .andExpect(jsonPath("$.questions", hasSize(2)))
                .andExpect(jsonPath("$.questions[0].title",
                        is(equalTo(agendaMock.getQuestions().get(0).getTitle()))))
                .andExpect(jsonPath("$.notes", is(equalTo(agendaMock.getNotes()))));

        verify(this.agendaService, times(1)).create(any(Agenda.class));
    }

    @Test
    void shouldReturnBadRequestWhenCreateAgendaWithoutRequiredField() throws Exception {
        final String request = StreamUtils.copyToString(
                this.createAgendaRequestWithoutTitleResource.getInputStream(),
                StandardCharsets.UTF_8);

        this.mockMvc.perform(
                        post("/v1/agenda")
                                .content(request)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(this.agendaService, times(0)).create(any(Agenda.class));
    }

    @Test
    void shouldReturnSuccessWhenGetAgendaById() throws Exception {
        final Agenda agendaMock = TestUtils.getAgendaMock();

        when(this.agendaService.findById(anyLong())).thenReturn(agendaMock);

        this.mockMvc.perform(
                        get("/v1/agenda/{id}", agendaMock.getId())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.title", is(equalTo(agendaMock.getTitle()))))
                .andExpect(jsonPath("$.questions", hasSize(1)))
                .andExpect(jsonPath("$.questions[0].title",
                        is(equalTo(agendaMock.getQuestions().get(0).getTitle()))))
                .andExpect(jsonPath("$.notes", is(equalTo(agendaMock.getNotes()))));

        verify(this.agendaService, times(1)).findById(anyLong());
    }

    @Test
    void shouldReturnNotFoundWhenGetAgendaById() throws Exception {
        when(this.agendaService.findById(anyLong())).thenThrow(new AgendaNotFoundException(1L));

        this.mockMvc.perform(
                        get("/v1/agenda/{id}", 1L)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(this.agendaService, times(1)).findById(anyLong());
    }

    @Test
    void shouldReturnNoContentWhenStartSession() throws Exception {
        this.mockMvc.perform(
                        post("/v1/agenda/{id}/session/start", 1L)
                                .queryParam("duration","3"))
                .andExpect(status().isNoContent());

        verify(this.agendaService, times(1)).startSession(anyLong(), anyLong());
    }

    @Test
    void shouldReturnAcceptWhenVote() throws Exception {
        final String request = StreamUtils.copyToString(
                this.voteRequestResource.getInputStream(),
                StandardCharsets.UTF_8);

        this.mockMvc.perform(
                        post("/v1/agenda/{id}/vote", 1L)
                                .content(request)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        verify(this.agendaService, times(1)).vote(anyLong(), anyList());
    }

    @Test
    void shouldReturnBadRequestWhenVoteWithoutRequiredField() throws Exception {
        final String request = StreamUtils.copyToString(
                this.voteRequestWithoutRequiredFieldResource.getInputStream(),
                StandardCharsets.UTF_8);

        this.mockMvc.perform(
                        post("/v1/agenda/{id}/vote", 1L)
                                .content(request)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(this.agendaService, times(0)).vote(anyLong(), anyList());
    }

    @Test
    void shouldReturnSuccessWhenGetVoteResult() throws Exception {
        final Question questionMock = TestUtils.getQuestionCompleteMock();

        when(this.agendaService.getVoteResults(anyLong()))
                .thenReturn(List.of(questionMock));

        this.mockMvc.perform(
                        get("/v1/agenda/{id}/result", questionMock.getAgenda().getId())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id",
                        is(equalTo(questionMock.getAgenda().getId().intValue()))))
                .andExpect(jsonPath("$.title", is(equalTo(questionMock.getAgenda().getTitle()))))
                .andExpect(jsonPath("$.notes", is(equalTo(questionMock.getAgenda().getNotes()))))
                .andExpect(jsonPath("$.questions", hasSize(1)))
                .andExpect(jsonPath("$.questions[0].number",
                        is(equalTo(questionMock.getNumber().intValue()))))
                .andExpect(jsonPath("$.questions[0].title", is(equalTo(questionMock.getTitle()))))
                .andExpect(jsonPath("$.questions[0].qnt_votes_in_favor",
                        is(equalTo(questionMock.getQntVotesInFavor().intValue()))))
                .andExpect(jsonPath("$.questions[0].qnt_votes_against",
                        is(equalTo(questionMock.getQntVotesAgainst().intValue()))))
                .andExpect(jsonPath("$.questions[0].final_result",
                        is(equalTo(questionMock.getFinalResult().label))));

        verify(this.agendaService, times(1)).getVoteResults(anyLong());
    }
}
