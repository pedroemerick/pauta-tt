package br.com.tt.vote.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "agenda")
public class Agenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    private String title;

    @OneToMany(mappedBy = "agenda", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Question> questions;

    @Column(name = "notes")
    private String notes;

    @Column(name = "start_session")
    private LocalDateTime startSessionIn;

    @Column(name = "end_of_session")
    private LocalDateTime endOfSessionIn;

    @Column(name = "accounted_result")
    private Boolean accountedResult;
}
