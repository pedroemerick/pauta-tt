package br.com.tt.vote.repository;

import br.com.tt.vote.model.Result;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultRepository extends JpaRepository<Result, Long> {

}
