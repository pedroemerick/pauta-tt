package br.com.tt.vote.repository;

import br.com.tt.vote.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByAgendaId(Long agendaId);
}
