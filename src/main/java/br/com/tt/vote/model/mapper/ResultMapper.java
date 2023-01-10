package br.com.tt.vote.model.mapper;

import br.com.tt.vote.model.Agenda;
import br.com.tt.vote.model.Result;
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

    @Mapping(source = "result.finalResult.label", target = "finalResult")
    QuestionResultDTO map(Result result);
    List<QuestionResultDTO> mapToQuestionsResult(List<Result> result);

    default ResultDTO map(List<Result> results) {
        Agenda agenda = results.stream().findFirst().orElseThrow().getQuestion().getAgenda();
        AgendaResultDTO agendaResultDTO = this.map(agenda);

        List<QuestionResultDTO> questionResultDTOS = this.mapToQuestionsResult(results);

        return new ResultDTO().agenda(agendaResultDTO).questions(questionResultDTOS);
    }
}
