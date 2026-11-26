package com.versatile.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "interview_turns")
public class TurnEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSessionEntity session;

    private int turnNumber;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(columnDefinition = "TEXT")
    private String candidateAnswer;

    @Column(columnDefinition = "TEXT")
    private String evaluation;

    private int score;

    protected TurnEntity() {
        // Required by JPA
    }

    public TurnEntity(int turnNumber, String question) {
        this.turnNumber = turnNumber;
        this.question = question;
    }

    public Long getId() { return id; }
    public InterviewSessionEntity getSession() { return session; }
    public void setSession(InterviewSessionEntity session) { this.session = session; }
    public int getTurnNumber() { return turnNumber; }
    public String getQuestion() { return question; }
    public String getCandidateAnswer() { return candidateAnswer; }
    public void setCandidateAnswer(String candidateAnswer) { this.candidateAnswer = candidateAnswer; }
    public String getEvaluation() { return evaluation; }
    public void setEvaluation(String evaluation) { this.evaluation = evaluation; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}
