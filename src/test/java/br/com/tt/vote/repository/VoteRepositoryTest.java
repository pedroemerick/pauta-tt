package br.com.tt.vote.repository;

import br.com.tt.vote.TestUtils;
import br.com.tt.vote.model.Agenda;
import br.com.tt.vote.model.Question;
import br.com.tt.vote.model.Vote;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
public class VoteRepositoryTest {

    private TestEntityManager entityManager;
    private VoteRepository voteRepository;

    @Autowired
    public VoteRepositoryTest(TestEntityManager entityManager, VoteRepository voteRepository) {
        this.entityManager = entityManager;
        this.voteRepository = voteRepository;
    }

    @Test
    void shouldPersistWhenSaveAll() {
        Agenda agendaMock = TestUtils.getAgendaMockWithoutIds();
        this.entityManager.persist(agendaMock);
        this.entityManager.flush();

        Question questionMock = agendaMock.getQuestions().get(0);

        Vote voteMockFirst = new Vote();
        voteMockFirst.setAssociateId(123L);
        voteMockFirst.setQuestion(questionMock);
        voteMockFirst.setInFavor(true);

        Vote voteMockSecond = new Vote();
        voteMockSecond.setAssociateId(1234L);
        voteMockSecond.setQuestion(questionMock);
        voteMockSecond.setInFavor(false);

        List<Vote> votes = this.voteRepository.saveAll(List.of(voteMockFirst, voteMockSecond));

        assertThat(votes.size(), is(equalTo(2)));
        assertThat(votes, containsInAnyOrder(
                hasProperty("id", is(notNullValue())),
                hasProperty("id", is(notNullValue())))
        );
        assertThat(votes, containsInRelativeOrder(
                hasProperty("associateId", is(equalTo(voteMockFirst.getAssociateId()))),
                hasProperty("associateId", is(equalTo(voteMockSecond.getAssociateId()))))
        );
        assertThat(votes, containsInRelativeOrder(
                hasProperty("inFavor", is(equalTo(voteMockFirst.getInFavor()))),
                hasProperty("inFavor", is(equalTo(voteMockSecond.getInFavor()))))
        );
    }
}
