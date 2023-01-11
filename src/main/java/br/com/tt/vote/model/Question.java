package br.com.tt.vote.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
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
    @Expose
    private Long number;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agenda_id", nullable = false)
    private Agenda agenda;

    @Column(name = "title")
    @Expose
    private String title;

    @OneToMany(mappedBy = "question", fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @Expose
    private List<Vote> votes;

    @Column(name = "qnt_votes_favor")
    @Expose
    private Long qntVotesInFavor;

    @Column(name = "qnt_votes_against")
    @Expose
    private Long qntVotesAgainst;

    @Column(name = "final_result")
    @Expose
    private FinalResultEnum finalResult;
}
