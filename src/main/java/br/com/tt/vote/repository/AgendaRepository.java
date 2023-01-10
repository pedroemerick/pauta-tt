package br.com.tt.vote.repository;

import br.com.tt.vote.model.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgendaRepository extends JpaRepository<Agenda, Long> {

}
