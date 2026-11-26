package com.versatile.controller;

import com.versatile.dto.InterviewReportView;
import com.versatile.dto.InterviewTurnView;
import com.versatile.dto.StartInterviewRequest;
import com.versatile.dto.SubmitAnswerRequest;
import com.versatile.model.InterviewSession;
import com.versatile.service.AiTestService;
import com.versatile.service.InterviewService;
import com.versatile.service.PdfExtractionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/interview")
public class InterviewController {


    private final AiTestService testService;
    private final InterviewService interviewService;
    private final PdfExtractionService pdfExtractionService;

    public InterviewController(AiTestService testService, InterviewService interviewService, PdfExtractionService pdfExtractionService) {
        this.testService = testService;
        this.interviewService = interviewService;
        this.pdfExtractionService = pdfExtractionService;
    }

    @GetMapping("/test-question")
    public String aiTestController(@RequestParam String topic){
        String ans = testService.generateSampleQuestion(topic);
        return ans;
    }
    @PostMapping
    public InterviewTurnView startInterview(@Valid @RequestBody StartInterviewRequest request) {
        return interviewService.startSession(request);
    }

    @PostMapping("/{sessionId}/answer")
    public InterviewTurnView submitAnswer(
            @PathVariable String sessionId,
            @Valid @RequestBody SubmitAnswerRequest request) {
        return interviewService.submitAnswer(sessionId, request);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public InterviewTurnView startInterviewPdf(
            @RequestParam("file")MultipartFile file,
            @RequestParam("interviewType") @NotBlank String interviewType,
            @RequestParam(value="maxQuestion",defaultValue = "5") @Min(1)@Max(15) int maxQuestion
            ){
        String extractedText = pdfExtractionService.extractText(file);
        StartInterviewRequest request = new StartInterviewRequest(interviewType,extractedText,maxQuestion);
        return interviewService.startSession(request);
    }

    @GetMapping("/{sessionId}/report")
    public InterviewReportView getInterviewReport(@PathVariable String sessionId) {
        return interviewService.generateReport(sessionId);
    }


}
