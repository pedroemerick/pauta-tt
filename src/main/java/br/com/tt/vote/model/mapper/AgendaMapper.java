package br.com.tt.vote.model.mapper;

import br.com.tt.vote.model.Agenda;
import br.com.tt.vote.model.Question;
import br.com.tt.vote.model.openapi.AgendaDTO;
import br.com.tt.vote.model.openapi.AgendaResultDTO;
import br.com.tt.vote.model.openapi.QuestionDTO;
import br.com.tt.vote.model.openapi.QuestionResultDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Objects;

@Mapper
public interface AgendaMapper {

    AgendaMapper INSTANCE = Mappers.getMapper(AgendaMapper.class);

    AgendaDTO map(Agenda agenda);
    QuestionDTO map(Question question);

    @Mapping(source = "questionDTO.title", target = "title")
    @Mapping(source = "agenda", target = "agenda")
    Question map(QuestionDTO questionDTO, Agenda agenda);

    @Mapping(target = "questions", ignore = true)
    AgendaResultDTO mapToResult(Agenda agenda);

    @Mapping(source = "question.finalResult.label", target = "finalResult")
    QuestionResultDTO mapToResult(Question question);
    List<QuestionResultDTO> mapToQuestionsResult(List<Question> questions);

    default List<Question> map(List<QuestionDTO> questionDTOS, Agenda agenda) {
        return questionDTOS.stream().map(questionDTO -> this.map(questionDTO, agenda)).toList();
    }

    default Agenda map(AgendaDTO agendaDTO) {
        if (Objects.isNull(agendaDTO)) {
            return null;
        }

        Agenda agenda = new Agenda();
        agenda.setId(agendaDTO.getId());
        agenda.setTitle(agendaDTO.getTitle());
        agenda.setQuestions(this.map(agendaDTO.getQuestions(), agenda));
        agenda.setNotes(agendaDTO.getNotes());

        return agenda;
    }

    default AgendaResultDTO map(List<Question> questions) {
        List<QuestionResultDTO> questionResultDTOS = this.mapToQuestionsResult(questions);

        Agenda agenda = questions.stream().findFirst().orElseThrow().getAgenda();
        AgendaResultDTO agendaResultDTO = this.mapToResult(agenda);
        agendaResultDTO.setQuestions(questionResultDTOS);

        return agendaResultDTO;
    }
}
