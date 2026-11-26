package com.versatile.dto;

import java.util.List;

public record AiReportFeedback(
        String overallSummary,
        List<String> strengths,
        List<String> areasForImprovement,
        List<String> recommendedStudyTopics
) {}
