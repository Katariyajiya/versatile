let sessionId = null;
let mode = "topic"; // "topic" or "pdf"
let recognition = null;
let isRecording = false;

const $ = (id) => document.getElementById(id);

// Tab switching
$("tab-topic").onclick = () => {
    mode = "topic";
    $("tab-topic").classList.add("active");
    $("tab-pdf").classList.remove("active");
    $("panel-topic").classList.remove("hidden");
    $("panel-pdf").classList.add("hidden");
};

$("tab-pdf").onclick = () => {
    mode = "pdf";
    $("tab-pdf").classList.add("active");
    $("tab-topic").classList.remove("active");
    $("panel-pdf").classList.remove("hidden");
    $("panel-topic").classList.add("hidden");
};

function setLoading(loading, text = "AI Interviewer is thinking...") {
    $("loaderText").textContent = text;
    $("loader").classList.toggle("hidden", !loading);
    $("startBtn").disabled = loading;
    $("submitBtn").disabled = loading;
    $("reportBtn").disabled = loading;
}

function showError(msg) {
    const el = $("errorBanner");
    el.textContent = msg;
    el.classList.remove("hidden");
}

function clearError() {
    $("errorBanner").classList.add("hidden");
}

// Start Interview
$("startBtn").onclick = async () => {
    clearError();
    const interviewType = $("interviewType").value;
    const maxQuestions = parseInt($("maxQuestions").value, 10) || 4;

    try {
        setLoading(true, "Preparing your interview & generating Question 1...");
        let res;

        if (mode === "topic") {
            const topicOrContext = $("topicInput").value.trim();
            if (!topicOrContext) return showError("Please enter a topic or study notes.");

            res = await fetch("/api/interview", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ interviewType, topicOrContext, maxQuestions })
            });
        } else {
            const file = $("pdfFile").files[0];
            if (!file) return showError("Please select a PDF file.");

            const formData = new FormData();
            formData.append("file", file);
            formData.append("interviewType", interviewType);
            formData.append("maxQuestions", maxQuestions);

            res = await fetch("/api/interview/upload", {
                method: "POST",
                body: formData // Browser sets multipart boundary automatically!
            });
        }

        if (!res.ok) throw new Error((await res.json()).message || "Failed to start interview");
        const view = await res.json();

        sessionId = view.sessionId;
        $("setup-card").classList.add("hidden");
        $("interview-card").classList.remove("hidden");
        renderTurn(view);
    } catch (err) {
        showError(err.message);
    } finally {
        setLoading(false);
    }
};

function renderTurn(view) {
    $("turnBadge").textContent = `Question ${view.currentTurn} of ${view.maxQuestions}`;

    if (view.previousEvaluation) {
        $("feedbackBox").classList.remove("hidden");
        $("scoreBadge").textContent = `${view.previousScore}/10`;
        $("feedbackText").textContent = view.previousEvaluation;
    } else {
        $("feedbackBox").classList.add("hidden");
    }

    if (view.completed) {
        $("questionBox").classList.add("hidden");
        $("answerSection").classList.add("hidden");
        $("completedActions").classList.remove("hidden");
    } else {
        $("questionBox").classList.remove("hidden");
        $("questionText").textContent = view.nextQuestion;
        $("answerInput").value = "";
    }
}

// Submit Answer
$("submitBtn").onclick = async () => {
    clearError();
    stopRecordingIfActive();
    const answer = $("answerInput").value.trim();
    if (!answer) return showError("Please type or speak your answer before submitting.");

    try {
        setLoading(true, "Evaluating your answer & preparing follow-up...");
        const res = await fetch(`/api/interview/${sessionId}/answer`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ answer })
        });

        if (!res.ok) throw new Error((await res.json()).message || "Failed to submit answer");
        renderTurn(await res.json());
    } catch (err) {
        showError(err.message);
    } finally {
        setLoading(false);
    }
};

// Generate Final Report
async function loadReport() {
    clearError();
    stopRecordingIfActive();
    try {
        setLoading(true, "Compiling your final performance report...");
        const res = await fetch(`/api/interview/${sessionId}/report`);
        if (!res.ok) throw new Error((await res.json()).message || "Failed to generate report");
        const report = await res.json();

        $("interview-card").classList.add("hidden");
        $("report-card").classList.remove("hidden");
        $("avgScoreBadge").textContent = `Avg: ${report.averageScore} / 10`;
        $("reportSummary").textContent = report.overallSummary;

        fillList("strengthsList", report.strengths);
        fillList("improvementsList", report.areasForImprovement);
        fillList("studyList", report.recommendedStudyTopics);
    } catch (err) {
        showError(err.message);
    } finally {
        setLoading(false);
    }
}

function fillList(id, items) {
    const ul = $(id);
    ul.innerHTML = "";
    (items || []).forEach((item) => {
        const li = document.createElement("li");
        li.textContent = item;
        ul.appendChild(li);
    });
}

$("reportBtn").onclick = loadReport;
$("endEarlyBtn").onclick = loadReport;
$("restartBtn").onclick = () => location.reload();

// Browser Web Speech API (Voice-to-Text)
const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
if (SpeechRecognition) {
    recognition = new SpeechRecognition();
    recognition.continuous = true;
    recognition.interimResults = false;
    recognition.lang = "en-US";

    recognition.onresult = (event) => {
        const transcript = Array.from(event.results)
            .slice(event.resultIndex)
            .map((r) => r[0].transcript)
            .join(" ");
        const box = $("answerInput");
        box.value = (box.value + " " + transcript).trim();
    };

    recognition.onend = () => {
        isRecording = false;
        $("micBtn").textContent = "🎙️ Start Speaking";
        $("micBtn").classList.remove("recording");
    };
} else {
    $("micBtn").disabled = true;
    $("micBtn").textContent = "🎙️ Voice Unavailable";
}

function stopRecordingIfActive() {
    if (recognition && isRecording) recognition.stop();
}

$("micBtn").onclick = () => {
    if (!recognition) return;
    if (isRecording) {
        recognition.stop();
    } else {
        isRecording = true;
        $("micBtn").textContent = "⏹️ Stop Speaking";
        $("micBtn").classList.add("recording");
        recognition.start();
    }
};
