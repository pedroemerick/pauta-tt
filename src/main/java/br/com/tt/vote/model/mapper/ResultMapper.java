package br.com.tt.vote.model.mapper;

import br.com.tt.vote.model.Agenda;
import br.com.tt.vote.model.Question;
import br.com.tt.vote.model.openapi.AgendaResultDTO;
import br.com.tt.vote.model.openapi.QuestionResultDTO;
import br.com.tt.vote.model.openapi.ResultDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ResultMapper {

    ResultMapper INSTANCE = Mappers.getMapper(ResultMapper.class);

    AgendaResultDTO map(Agenda agenda);

    @Mapping(source = "question.finalResult.label", target = "finalResult")
    QuestionResultDTO map(Question question);
    List<QuestionResultDTO> mapToQuestionsResult(List<Question> questions);

    default ResultDTO map(List<Question> questions) {
        Agenda agenda = questions.stream().findFirst().orElseThrow().getAgenda();
        AgendaResultDTO agendaResultDTO = this.map(agenda);

        List<QuestionResultDTO> questionResultDTOS = this.mapToQuestionsResult(questions);

        return new ResultDTO().agenda(agendaResultDTO).questions(questionResultDTOS);
    }
}
