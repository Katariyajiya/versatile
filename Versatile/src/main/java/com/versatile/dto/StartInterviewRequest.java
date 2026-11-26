package com.versatile.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record StartInterviewRequest (
    @NotBlank
    String interviewType,
    @NotBlank
    String topicOrContext,
    @Min(1) @Max(15)
    int maxQuestions)
{}
