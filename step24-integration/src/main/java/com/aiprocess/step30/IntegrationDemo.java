package com.aiprocess.step30;

import com.aiprocess.step29.AudioProcessor;
import java.util.*;

/**
 * 종합 프로젝트 데모: 모든 기능 통합
 */
public class IntegrationDemo {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   STEP 30: 종합 프로젝트             ║");
        System.out.println("║   Smart AI Assistant                  ║");
        System.out.println("╚══════════════════════════════════════╝\n");

        // 1. 시스템 아키텍처
        System.out.println("[ 1. 시스템 아키텍처 ]");
        System.out.println("-".repeat(40));
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────┐");
        System.out.println("│                Smart Assistant                   │");
        System.out.println("├─────────────────────────────────────────────────┤");
        System.out.println("│  ┌─────────┐  ┌─────────┐  ┌─────────┐          │");
        System.out.println("│  │   RAG   │  │  Agent  │  │Multimodal│         │");
        System.out.println("│  │ (지식)  │  │ (도구)  │  │(이미지/│          │");
        System.out.println("│  │         │  │         │  │ 음성)   │          │");
        System.out.println("│  └────┬────┘  └────┬────┘  └────┬────┘          │");
        System.out.println("│       └──────────┬─────────────┘                │");
        System.out.println("│                  │                              │");
        System.out.println("│          ┌──────┴──────┐                        │");
        System.out.println("│          │Intent Router│                        │");
        System.out.println("│          └──────┬──────┘                        │");
        System.out.println("│                 │                               │");
        System.out.println("│          ┌──────┴──────┐                        │");
        System.out.println("│          │  LLM Core   │                        │");
        System.out.println("│          └─────────────┘                        │");
        System.out.println("└─────────────────────────────────────────────────┘");

        // 2. 스마트 어시스턴트 초기화
        System.out.println("\n[ 2. 스마트 어시스턴트 초기화 ]");
        System.out.println("-".repeat(40));

        SmartAssistant assistant = new SmartAssistant();

        // 지식 베이스 구축
        System.out.println("\n지식 베이스 구축 중...");

        assistant.addKnowledge("company_policy", """
            회사 환불 정책:
            - 구매 후 30일 이내 환불 가능
            - 제품 훼손 시 환불 불가
            - 환불 처리는 영업일 기준 3-5일 소요
            """);

        assistant.addKnowledge("product_info", """
            제품 정보:
            - Pro 모델: 고성능, 전문가용, 가격 1,500,000원
            - Standard 모델: 일반용, 가격 800,000원
            - Lite 모델: 입문용, 가격 400,000원
            """);

        assistant.addKnowledge("faq", """
            자주 묻는 질문:
            Q: 배송 기간은 얼마나 걸리나요?
            A: 일반 배송 2-3일, 빠른 배송 1일 이내

            Q: 보증 기간은 얼마인가요?
            A: 모든 제품은 1년 무상 보증이 적용됩니다.
            """);

        System.out.println("✓ 지식 베이스 준비 완료");
        System.out.println("✓ Agent 도구 등록 완료 (계산기, 날씨, 검색)");
        System.out.println("✓ Multimodal 프로세서 준비 완료");

        // 3. 대화 시뮬레이션
        System.out.println("\n[ 3. 대화 시뮬레이션 ]");
        System.out.println("-".repeat(40));

        // 다양한 시나리오 테스트
        String[] userInputs = {
            "안녕하세요!",
            "환불 정책이 어떻게 되나요?",
            "서울 날씨 알려줘",
            "25 * 4 + 100 계산해줘",
            "Pro 모델 가격이 얼마야?",
            "감사합니다!"
        };

        for (String input : userInputs) {
            System.out.println("\n👤 사용자: " + input);
            SmartAssistant.AssistantResponse response = assistant.processText(input);
            response.print();
        }

        // 4. 이미지 처리 데모
        System.out.println("\n[ 4. 이미지 처리 데모 ]");
        System.out.println("-".repeat(40));

        System.out.println("\n👤 사용자: [이미지 첨부: diagram.png]");
        System.out.println("   이 다이어그램에서 무엇을 볼 수 있어?");

        SmartAssistant.AssistantResponse imageResponse =
            assistant.processImage("diagram.png", "이 다이어그램에서 무엇을 볼 수 있어?");
        imageResponse.print();

        // 5. 음성 처리 데모
        System.out.println("\n[ 5. 음성 처리 데모 ]");
        System.out.println("-".repeat(40));

        System.out.println("\n🎤 [음성 입력: question.mp3]");
        SmartAssistant.AssistantResponse audioResponse =
            assistant.processAudio("question.mp3");
        audioResponse.print();

        // TTS 응답
        System.out.println("\n🔊 음성 응답 생성:");
        AudioProcessor.TTSResult ttsResult = assistant.textToSpeech(audioResponse.content);
        ttsResult.print();

        // 6. 대화 히스토리
        System.out.println("\n[ 6. 대화 히스토리 ]");
        System.out.println("-".repeat(40));

        List<SmartAssistant.Message> history = assistant.getConversationHistory();
        System.out.printf("\n총 %d개의 메시지%n", history.size());
        System.out.println();

        int count = 0;
        for (SmartAssistant.Message msg : history) {
            String icon = msg.role.equals("user") ? "👤" : "🤖";
            String preview = msg.content.length() > 40 ?
                msg.content.substring(0, 40) + "..." : msg.content;
            System.out.printf("%s %s: %s%n", icon, msg.role, preview.replace("\n", " "));
            if (++count >= 6) {
                System.out.println("   ... 더 많은 메시지");
                break;
            }
        }

        // 7. 확장 아이디어
        System.out.println("\n[ 7. 확장 아이디어 ]");
        System.out.println("-".repeat(40));
        System.out.println();
        System.out.println("1. 실제 LLM 연동");
        System.out.println("   - OpenAI API / Claude API 연결");
        System.out.println("   - 스트리밍 응답 지원");
        System.out.println();
        System.out.println("2. 벡터 DB 연동");
        System.out.println("   - Pinecone, Weaviate, Chroma");
        System.out.println("   - 대용량 문서 처리");
        System.out.println();
        System.out.println("3. 웹 인터페이스");
        System.out.println("   - React / Next.js 프론트엔드");
        System.out.println("   - 실시간 대화 UI");
        System.out.println();
        System.out.println("4. 추가 도구");
        System.out.println("   - 데이터베이스 쿼리");
        System.out.println("   - 이메일 전송");
        System.out.println("   - 일정 관리");
        System.out.println();
        System.out.println("5. 멀티모달 강화");
        System.out.println("   - 실시간 STT/TTS");
        System.out.println("   - 화면 공유 분석");
        System.out.println("   - 비디오 처리");

        // 8. 학습 완료 메시지
        System.out.println("\n" + "═".repeat(50));
        System.out.println("🎉 축하합니다! AI 프로세스 학습을 완료했습니다!");
        System.out.println("═".repeat(50));
        System.out.println();
        System.out.println("학습한 내용:");
        System.out.println("  Part 1: AI 기초 프로세스 (STEP 1-8)");
        System.out.println("    - 토큰화, 컨텍스트, 확률, 샘플링");
        System.out.println("    - Embedding, Transformer");
        System.out.println();
        System.out.println("  Part 2: 분석/개선 (STEP 9-14)");
        System.out.println("    - 토큰 분석, 성능 측정");
        System.out.println("    - 스트리밍, 캐싱");
        System.out.println();
        System.out.println("  Part 3: 서비스 구축 (STEP 15-24)");
        System.out.println("    - REST API, 클라이언트");
        System.out.println("    - 프롬프트 엔지니어링");
        System.out.println();
        System.out.println("  Part 4: 고급 주제 (STEP 25-30)");
        System.out.println("    - RAG, Agent, Fine-tuning");
        System.out.println("    - Multimodal, 통합 프로젝트");
        System.out.println();
        System.out.println("다음 단계:");
        System.out.println("  → 실제 LLM API를 연동해보세요");
        System.out.println("  → 여러분만의 AI 서비스를 만들어보세요");
        System.out.println("  → 오픈소스 모델을 직접 실행해보세요");
        System.out.println();
        System.out.println("🚀 Happy Coding with AI!");
    }
}
