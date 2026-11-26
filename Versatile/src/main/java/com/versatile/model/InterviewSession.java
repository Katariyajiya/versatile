package com.versatile.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InterviewSession {

    private final String sessionId;
    private final String interviewType;
    private final String topicOrContext;
    private final int maxQuestions;
    private final List<Turn> history;
    private boolean completed;

    public InterviewSession(String interviewType, String topicOrContext, int maxQuestions) {
        this.sessionId = UUID.randomUUID().toString();
        this.interviewType = interviewType;
        this.topicOrContext = topicOrContext;
        this.maxQuestions = maxQuestions;
        this.history = new ArrayList<>();
        this.completed = false;
    }

    public String getSessionId() { return sessionId; }
    public String getInterviewType() { return interviewType; }
    public String getTopicOrContext() { return topicOrContext; }
    public int getMaxQuestions() { return maxQuestions; }
    public List<Turn> getHistory() { return history; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
