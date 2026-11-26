package com.versatile.dto;

public record AiTurnResponse (
    String evaluation,
    int score,
    String nextQuestion
){}
