package com.versatile.model;

public record Turn(
        int turnNumber,
        String question,
        String candidateAnswer,
        String evaluation,
        int score
){}
