package com.novacasino.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

/**
 * A Q&A turn with the AI about a simulation (readme §3.2.10). Append-only history: the question, the
 * answer, the model used (traceability) and when it was asked.
 */
@Entity
@Table(name = "simulation_explanations")
public class SimulationExplanationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "simulation_run_id", nullable = false)
    private Long simulationRunId;

    @Column(name = "asked_by_user_id", nullable = false)
    private Long askedByUserId;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false)
    private String answer;

    @Column(nullable = false, length = 50)
    private String model;

    @Column(name = "asked_at", insertable = false, updatable = false)
    private OffsetDateTime askedAt;

    protected SimulationExplanationEntity() { }

    public SimulationExplanationEntity(final Long simulationRunId, final Long askedByUserId,
                                       final String question, final String answer, final String model) {
        this.simulationRunId = simulationRunId;
        this.askedByUserId = askedByUserId;
        this.question = question;
        this.answer = answer;
        this.model = model;
    }

    public Long getId()              { return id; }
    public Long getSimulationRunId() { return simulationRunId; }
    public Long getAskedByUserId()   { return askedByUserId; }
    public String getQuestion()      { return question; }
    public String getAnswer()        { return answer; }
    public String getModel()         { return model; }
    public OffsetDateTime getAskedAt() { return askedAt; }
}
