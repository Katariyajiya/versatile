package com.versatile.dto;

public record InterviewTurnView (
    String sessionId,
    int currentTurn,
    int maxQuestions,
    boolean completed,
    String previousEvaluation,
    Integer previousScore,
    String nextQuestion
){}
