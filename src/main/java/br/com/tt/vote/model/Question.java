package br.com.tt.vote.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "question")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "number")
    private Long number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agenda_id", nullable = false)
    private Agenda agenda;

    @Column(name = "title")
    private String title;

    @OneToMany(mappedBy = "question", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Vote> votes;

    @Column(name = "qnt_votes_favor")
    private Long qntVotesInFavor;

    @Column(name = "qnt_votes_against")
    private Long qntVotesAgainst;

    @Column(name = "final_result")
    private FinalResultEnum finalResult;
}
