package br.com.tt.vote.repository;

import br.com.tt.vote.TestUtils;
import br.com.tt.vote.model.Agenda;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
public class AgendaRepositoryTest {

    private TestEntityManager entityManager;
    private AgendaRepository agendaRepository;

    @Autowired
    public AgendaRepositoryTest(TestEntityManager entityManager, AgendaRepository agendaRepository) {
        this.entityManager = entityManager;
        this.agendaRepository = agendaRepository;
    }

    @Test
    void shouldPersistAgendaWhenSave() {
        Agenda agendaMock = TestUtils.getAgendaMockWithoutIds();

        Agenda agenda = this.agendaRepository.save(agendaMock);

        assertThat(agenda.getId(), is(notNullValue()));
        assertThat(agenda.getTitle(), is(equalTo(agenda.getTitle())));
        assertThat(agenda.getNotes(), is(equalTo(agenda.getNotes())));
        assertThat(agenda.getQuestions().size(), is(equalTo(1)));
        assertThat(agenda.getQuestions().get(0).getNumber(), is(notNullValue()));
        assertThat(agenda.getQuestions().get(0).getTitle(), is(equalTo(agendaMock.getQuestions().get(0).getTitle())));
    }

    @Test
    void shouldReturnAgendaWhenFindById() {
        Agenda agendaMock = TestUtils.getAgendaMockWithoutIds();
        this.entityManager.persist(agendaMock);
        this.entityManager.flush();

        Agenda agenda = this.agendaRepository.findById(agendaMock.getId()).get();

        assertThat(agenda.getId(), is(equalTo(agendaMock.getId())));
        assertThat(agenda, is(equalTo(agendaMock)));
    }

    @Test
    void shouldReturnNullWhenFindByIdWithNonExistentId() {
        Boolean isEmpty = this.agendaRepository.findById(1L).isEmpty();

        assertThat(isEmpty, is(equalTo(true)));
    }
}
