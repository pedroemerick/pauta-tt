package br.com.tt.vote;

import br.com.tt.vote.model.Agenda;
import br.com.tt.vote.model.FinalResultEnum;
import br.com.tt.vote.model.Question;
import br.com.tt.vote.model.Vote;

import java.util.List;

public class TestUtils {

    public static Agenda getAgendaMock() {
        Agenda agenda = TestUtils.getOnlyAgendaMock();
        agenda.setQuestions(List.of(TestUtils.getQuestionMock()));

        return agenda;
    }

    public static Question getQuestionMock() {
        Question question = new Question();
        question.setNumber(1L);
        question.setTitle("anyTitle1");
        Agenda agenda = new Agenda();
        agenda.setId(1L);
        question.setAgenda(agenda);

        return question;
    }

    public static Question getQuestionCompleteMock() {
        Question question = new Question();
        question.setNumber(1L);
        question.setTitle("anyTitle1");
        question.setQntVotesInFavor(2L);
        question.setQntVotesAgainst(1L);
        question.setFinalResult(FinalResultEnum.APPROVED);
        question.setAgenda(TestUtils.getOnlyAgendaMock());

        return question;
    }

    public static Agenda getOnlyAgendaMock() {
        Agenda agenda = new Agenda();
        agenda.setId(1L);
        agenda.setTitle("anyTitle");
        agenda.setNotes("anyNotes");

        return agenda;
    }

    public static Vote getVote() {
        Vote vote = new Vote();
        vote.setQuestion(TestUtils.getQuestionMock());
        vote.setAssociateId(123456L);
        vote.setInFavor(false);

        return vote;
    }
}
