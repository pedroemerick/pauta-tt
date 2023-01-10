package br.com.tt.vote.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "result")
public class Result {

    @Id
    @Column(name = "question_id")
    private Long id;

    @Column(name = "qnt_votes_favor")
    private Long qntVotesInFavor;

    @Column(name = "qnt_votes_against")
    private Long qntVotesAgainst;

    @Column(name = "final_result")
    private FinalResultEnum finalResult;

    @OneToOne
    @MapsId
    @JoinColumn(name = "question_id")
    private Question question;
}
