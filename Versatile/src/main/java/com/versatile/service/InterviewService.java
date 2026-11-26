package com.versatile.service;

import com.versatile.entity.InterviewSessionEntity;
import com.versatile.entity.TurnEntity;
import com.versatile.dto.*;
import com.versatile.repository.InterviewSessionRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class InterviewService {

    private final ChatClient chatClient;
    private final ContextBuilder contextBuilder;
    private final InterviewSessionRepository sessionRepository;

    public InterviewService(
            ChatClient.Builder builder,
            ContextBuilder contextBuilder,
            InterviewSessionRepository sessionRepository) {
        this.chatClient = builder
                .defaultSystem("You are an expert interviewer. Ask one question at a time. Be constructive and concise.")
                .build();
        this.contextBuilder = contextBuilder;
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public InterviewTurnView startSession(StartInterviewRequest request) {
        InterviewSessionEntity session = new InterviewSessionEntity(
                request.interviewType(),
                request.topicOrContext(),
                request.maxQuestions()
        );

        String prompt = contextBuilder.buildFirstQuestionPrompt(session);
        String firstQuestion = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        session.addTurn(new TurnEntity(1, firstQuestion));
        sessionRepository.save(session);

        return new InterviewTurnView(
                session.getSessionId(), 1, session.getMaxQuestions(),
                false, null, null, firstQuestion
        );
    }

    @Transactional
    public InterviewTurnView submitAnswer(String sessionId, SubmitAnswerRequest request) {
        InterviewSessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview session not found"));

        if (session.isCompleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Interview is already completed");
        }

        // 1. Mutate the current managed TurnEntity directly
        int lastIndex = session.getHistory().size() - 1;
        TurnEntity currentTurn = session.getHistory().get(lastIndex);
        currentTurn.setCandidateAnswer(request.answer());

        // 2. Ask LLM for evaluation + next question as structured JSON
        String prompt = contextBuilder.buildEvaluationAndNextPrompt(session);
        AiTurnResponse aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .entity(AiTurnResponse.class);

        // 3. Save evaluation & score on currentTurn
        currentTurn.setEvaluation(aiResponse.evaluation());
        currentTurn.setScore(aiResponse.score());

        // 4. Check if we reached maxQuestions
        if (currentTurn.getTurnNumber() >= session.getMaxQuestions()) {
            session.setCompleted(true);
            sessionRepository.save(session);
            return new InterviewTurnView(
                    session.getSessionId(), currentTurn.getTurnNumber(), session.getMaxQuestions(),
                    true, aiResponse.evaluation(), aiResponse.score(), null
            );
        }

        // 5. Otherwise add the next turn & persist
        int nextTurnNumber = currentTurn.getTurnNumber() + 1;
        session.addTurn(new TurnEntity(nextTurnNumber, aiResponse.nextQuestion()));
        sessionRepository.save(session);

        return new InterviewTurnView(
                session.getSessionId(), nextTurnNumber, session.getMaxQuestions(),
                false, aiResponse.evaluation(), aiResponse.score(), aiResponse.nextQuestion()
        );
    }

    @Transactional(readOnly = true)
    public InterviewReportView generateReport(String sessionId){
        InterviewSessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview session not found"));

        List<TurnEntity> answeredTurns = session.getHistory().stream().filter(t->t.getCandidateAnswer() !=null).toList();

        if (answeredTurns.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot generate report before answering at least one question");
        }

        double averageScore = answeredTurns.stream().mapToInt(TurnEntity::getScore).average().orElse(0.0);

        double roundedAvg = Math.round(averageScore*10.0)/10.0;
        String prompt = contextBuilder.buildFinalReportPrompt(session);
        AiReportFeedback feedback = chatClient.prompt().user(prompt).call()
                .entity(AiReportFeedback.class);

        return new InterviewReportView(
                session.getSessionId(),
                session.getInterviewType(),
                answeredTurns.size(),
                roundedAvg,
                feedback.overallSummary(),
                feedback.strengths(),
                feedback.areasForImprovement(),
                feedback.recommendedStudyTopics()
        );
    }
}
