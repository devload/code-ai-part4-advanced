package com.aiprocess.step30;

import com.aiprocess.step25.*;
import com.aiprocess.step26.*;
import com.aiprocess.step27.*;
import com.aiprocess.step29.*;
import java.util.*;

/**
 * 스마트 어시스턴트: RAG + Agent + Multimodal 통합
 *
 * 실제 AI 어시스턴트의 구조를 시뮬레이션
 */
public class SmartAssistant {

    private final RAGPipeline rag;
    private final SimpleAgent agent;
    private final ImageProcessor vision;
    private final AudioProcessor audio;
    private final List<Message> conversationHistory = new ArrayList<>();

    public SmartAssistant() {
        // RAG 초기화
        this.rag = new RAGPipeline(200, 64, 3);

        // Agent 초기화
        this.agent = new SimpleAgent(5);
        agent.registerTool(new CalculatorTool());
        agent.registerTool(new WeatherTool());
        agent.registerTool(new SearchTool());

        // Multimodal 프로세서 초기화
        this.vision = new ImageProcessor();
        this.audio = new AudioProcessor();
    }

    /**
     * 지식 베이스에 문서 추가
     */
    public void addKnowledge(String source, String content) {
        rag.indexDocument(content, source);
    }

    /**
     * 사용자 입력 처리 (텍스트)
     */
    public AssistantResponse processText(String userInput) {
        conversationHistory.add(new Message("user", userInput));

        // 의도 분석
        Intent intent = analyzeIntent(userInput);
        System.out.println("📋 감지된 의도: " + intent.type);

        AssistantResponse response;

        switch (intent.type) {
            case "tool_use":
                // Agent로 도구 사용
                String agentResult = agent.run(userInput);
                response = new AssistantResponse("agent", agentResult, intent);
                break;

            case "knowledge_query":
                // RAG로 지식 검색
                RAGPipeline.RAGResponse ragResult = rag.query(userInput);
                response = new AssistantResponse("rag", ragResult.answer, intent);
                break;

            case "conversation":
            default:
                // 일반 대화
                String chatResponse = generateChatResponse(userInput);
                response = new AssistantResponse("chat", chatResponse, intent);
                break;
        }

        conversationHistory.add(new Message("assistant", response.content));
        return response;
    }

    /**
     * 이미지 처리
     */
    public AssistantResponse processImage(String imagePath, String question) {
        conversationHistory.add(new Message("user", "[이미지: " + imagePath + "] " + question));

        String answer = vision.answerQuestion(imagePath, question);
        AssistantResponse response = new AssistantResponse("vision", answer,
            new Intent("image_analysis", 0.95f));

        conversationHistory.add(new Message("assistant", response.content));
        return response;
    }

    /**
     * 음성 처리
     */
    public AssistantResponse processAudio(String audioPath) {
        AudioProcessor.STTResult sttResult = audio.transcribe(audioPath);
        conversationHistory.add(new Message("user", "[음성: " + audioPath + "] " + sttResult.text));

        // 변환된 텍스트로 처리
        return processText(sttResult.text);
    }

    /**
     * 텍스트를 음성으로 변환
     */
    public AudioProcessor.TTSResult textToSpeech(String text) {
        return audio.synthesize(text);
    }

    /**
     * 의도 분석
     */
    private Intent analyzeIntent(String input) {
        String lower = input.toLowerCase();

        // 도구 사용 의도
        if (lower.contains("날씨") || lower.contains("계산") ||
            lower.contains("검색") || lower.contains("+") || lower.contains("-")) {
            return new Intent("tool_use", 0.9f);
        }

        // 지식 쿼리 의도
        if (lower.contains("뭐야") || lower.contains("무엇") ||
            lower.contains("설명") || lower.contains("알려줘") ||
            lower.contains("어떻게")) {
            return new Intent("knowledge_query", 0.85f);
        }

        // 일반 대화
        return new Intent("conversation", 0.7f);
    }

    /**
     * 일반 대화 응답 생성
     */
    private String generateChatResponse(String input) {
        if (input.contains("안녕") || input.contains("hello")) {
            return "안녕하세요! 무엇을 도와드릴까요?";
        }
        if (input.contains("고마워") || input.contains("감사")) {
            return "도움이 되었다니 기쁩니다! 다른 질문이 있으시면 말씀해주세요.";
        }
        if (input.contains("안녕히") || input.contains("bye")) {
            return "안녕히 가세요! 좋은 하루 되세요.";
        }

        return "네, 말씀해주신 내용을 잘 들었습니다. 더 자세히 알려주시면 도움을 드릴 수 있습니다.";
    }

    /**
     * 대화 히스토리 반환
     */
    public List<Message> getConversationHistory() {
        return Collections.unmodifiableList(conversationHistory);
    }

    /**
     * 대화 히스토리 초기화
     */
    public void clearHistory() {
        conversationHistory.clear();
    }

    /**
     * 메시지 클래스
     */
    public static class Message {
        public final String role;
        public final String content;
        public final long timestamp;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
            this.timestamp = System.currentTimeMillis();
        }
    }

    /**
     * 의도 클래스
     */
    public static class Intent {
        public final String type;
        public final float confidence;

        public Intent(String type, float confidence) {
            this.type = type;
            this.confidence = confidence;
        }
    }

    /**
     * 어시스턴트 응답 클래스
     */
    public static class AssistantResponse {
        public final String source;    // chat, rag, agent, vision
        public final String content;
        public final Intent intent;

        public AssistantResponse(String source, String content, Intent intent) {
            this.source = source;
            this.content = content;
            this.intent = intent;
        }

        public void print() {
            System.out.println("━".repeat(40));
            System.out.println("🤖 어시스턴트 (" + source + ")");
            System.out.println("━".repeat(40));
            System.out.println(content);
        }
    }
}
