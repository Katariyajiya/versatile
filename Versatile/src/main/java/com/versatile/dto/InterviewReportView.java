package com.versatile.dto;

import java.util.List;

public record InterviewReportView(
        String sessionId,
        String interviewType,
        int totalQuestionsAnswered,
        double averageScore,
        String overallSummary,
        List<String> strengths,
        List<String> areasForImprovement,
        List<String> recommendedStudyTopics
) {}
