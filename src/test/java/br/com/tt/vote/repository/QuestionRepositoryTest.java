package br.com.tt.vote.repository;

import br.com.tt.vote.TestUtils;
import br.com.tt.vote.model.Agenda;
import br.com.tt.vote.model.Question;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
public class QuestionRepositoryTest {

    private TestEntityManager entityManager;
    private QuestionRepository questionRepository;

    @Autowired
    public QuestionRepositoryTest(TestEntityManager entityManager, QuestionRepository questionRepository) {
        this.entityManager = entityManager;
        this.questionRepository = questionRepository;
    }

    @Test
    void shouldReturnQuestionWhenFindByAgendaId() {
        Agenda agendaMock = TestUtils.getAgendaMockWithoutIds();
        this.entityManager.persist(agendaMock);
        this.entityManager.flush();

        List<Question> questions = this.questionRepository.findByAgendaId(agendaMock.getId());

        assertThat(questions.size(), is(equalTo(agendaMock.getQuestions().size())));
        questions.forEach(question -> assertThat(question.getAgenda().getId(), is(equalTo(agendaMock.getId()))));
    }

    @Test
    void shouldNotReturnWhenFindByAgendaIdWithNonExistentId() {
        List<Question> questions = this.questionRepository.findByAgendaId(1L);

        assertThat(questions.isEmpty(), is(equalTo(true)));
    }
}
