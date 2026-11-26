package com.versatile.service;

import com.versatile.entity.InterviewSessionEntity;
import com.versatile.entity.TurnEntity;
import org.springframework.stereotype.Component;

@Component
public class ContextBuilder {

    public String buildFirstQuestionPrompt(InterviewSessionEntity session) {
        return """
                Interview Type: %s
                Topic / Source Material: %s
                
                Ask the candidate the first interview question. Keep it focused, clear, and one question only.
                """.formatted(session.getInterviewType(), session.getTopicOrContext());
    }

    public String buildEvaluationAndNextPrompt(InterviewSessionEntity session) {
        StringBuilder sb = new StringBuilder();
        sb.append("Interview Type: ").append(session.getInterviewType()).append("\n");
        sb.append("Topic / Source Material: ").append(session.getTopicOrContext()).append("\n\n");
        sb.append("Conversation History:\n");

        for (TurnEntity turn : session.getHistory()) {
            sb.append("Turn ").append(turn.getTurnNumber()).append(":\n");
            sb.append("Question: ").append(turn.getQuestion()).append("\n");
            if (turn.getCandidateAnswer() != null) {
                sb.append("Candidate Answer: ").append(turn.getCandidateAnswer()).append("\n");
            }
            if (turn.getEvaluation() != null) {
                sb.append("Previous Evaluation: ").append(turn.getEvaluation()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("Evaluate the candidate's latest answer (score 1-10) and ask the next follow-up question. ");
        sb.append("If they missed something, probe deeper. If they answered well, increase difficulty.");
        return sb.toString();
    }

    public String buildFinalReportPrompt(InterviewSessionEntity session){
        StringBuilder sb = new StringBuilder();
        sb.append("Interview Type: ").append(session.getInterviewType()).append("\n");
        sb.append("Topic / Source Material: ").append(session.getTopicOrContext()).append("\n\n");
        sb.append("Full Interview Transcript:\n");

        for (TurnEntity turn : session.getHistory()) {
            if (turn.getCandidateAnswer() == null) continue; // Skip unanswered turns if ended early
            sb.append("Q").append(turn.getTurnNumber()).append(": ").append(turn.getQuestion()).append("\n");
            sb.append("Candidate Answer: ").append(turn.getCandidateAnswer()).append("\n");
            sb.append("Turn Evaluation (Score ").append(turn.getScore()).append("/10): ")
                    .append(turn.getEvaluation()).append("\n\n");
        }

        sb.append("Based on the entire interview transcript above, provide a constructive final performance report ");
        sb.append("highlighting key strengths, specific areas for improvement, and actionable topics to study next.");
        return sb.toString();

    }
}
