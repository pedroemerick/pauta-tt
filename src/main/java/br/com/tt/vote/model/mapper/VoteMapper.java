package br.com.tt.vote.model.mapper;

import br.com.tt.vote.model.Question;
import br.com.tt.vote.model.Vote;
import br.com.tt.vote.model.openapi.VoteEntryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface VoteMapper {

    VoteMapper INSTANCE = Mappers.getMapper(VoteMapper.class);

    Vote map(Long associateId, Boolean inFavor, Question question);

    default List<Vote> map(VoteEntryDTO voteEntryDTO) {
        return voteEntryDTO.getVotes().stream().map(voteDTO -> {
            Question question = new Question();
            question.setNumber(voteDTO.getQuestionNumber());

            return map(voteEntryDTO.getAssociateId(), voteDTO.getInFavor(), question);
        }).toList();
    }
}
