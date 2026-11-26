package com.versatile.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "interview_sessions")
public class InterviewSessionEntity {

    @Id
    @Column(name = "session_id", length = 36)
    private String sessionId;

    @Column(nullable = false)
    private String interviewType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String topicOrContext;

    private int maxQuestions;
    private boolean completed;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("turnNumber ASC")
    private List<TurnEntity> history = new ArrayList<>();

    protected InterviewSessionEntity() {
        // Required by JPA
    }

    public InterviewSessionEntity(String interviewType, String topicOrContext, int maxQuestions) {
        this.sessionId = UUID.randomUUID().toString();
        this.interviewType = interviewType;
        this.topicOrContext = topicOrContext;
        this.maxQuestions = maxQuestions;
        this.completed = false;
        this.createdAt = LocalDateTime.now();
    }

    // Keeps both sides of the bidirectional JPA relationship synchronized
    public void addTurn(TurnEntity turn) {
        this.history.add(turn);
        turn.setSession(this);
    }

    public String getSessionId() { return sessionId; }
    public String getInterviewType() { return interviewType; }
    public String getTopicOrContext() { return topicOrContext; }
    public int getMaxQuestions() { return maxQuestions; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<TurnEntity> getHistory() { return history; }
}
