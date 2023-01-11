package br.com.tt.vote.model;

import com.google.gson.annotations.Expose;
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
    @Expose
    private Long id;

    @Column(name = "title")
    @Expose
    private String title;

    @OneToMany(mappedBy = "agenda", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Expose
    private List<Question> questions;

    @Column(name = "notes")
    @Expose
    private String notes;

    @Column(name = "start_session")
    @Expose
    private LocalDateTime startSessionIn;

    @Column(name = "end_of_session")
    @Expose
    private LocalDateTime endOfSessionIn;

    @Column(name = "accounted_result")
    @Expose
    private Boolean accountedResult;
}
