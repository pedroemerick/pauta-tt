package br.com.tt.vote.repository;

import br.com.tt.vote.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {

}
