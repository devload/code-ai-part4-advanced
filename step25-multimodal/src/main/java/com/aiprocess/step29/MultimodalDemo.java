package com.aiprocess.step29;

import java.util.*;

/**
 * Multimodal AI 데모: 다중 모달리티 처리
 */
public class MultimodalDemo {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("STEP 29: Multimodal AI");
        System.out.println("========================================\n");

        // 1. 멀티모달 개념
        System.out.println("[ 1. Multimodal AI란? ]");
        System.out.println("-".repeat(40));
        System.out.println();
        System.out.println("Multimodal = 여러 종류의 데이터를 함께 처리");
        System.out.println();
        System.out.println("┌─────────────────────────────────────────┐");
        System.out.println("│  텍스트 ─┐                              │");
        System.out.println("│  이미지 ─┼─→ Multimodal Model ─→ 출력   │");
        System.out.println("│  오디오 ─┘                              │");
        System.out.println("└─────────────────────────────────────────┘");
        System.out.println();
        System.out.println("예시:");
        System.out.println("  - GPT-4V: 이미지 + 텍스트 이해");
        System.out.println("  - Claude Vision: 문서/차트/코드 분석");
        System.out.println("  - Gemini: 텍스트, 이미지, 오디오, 비디오");

        // 2. Vision (이미지 처리)
        System.out.println("\n[ 2. Vision - 이미지 처리 ]");
        System.out.println("-".repeat(40));

        ImageProcessor vision = new ImageProcessor();

        String[] images = {"cat.jpg", "diagram.png", "code.png"};

        for (String image : images) {
            System.out.println("\n" + "─".repeat(30));
            ImageProcessor.ImageAnalysisResult result = vision.analyze(image);
            result.print();
        }

        // 이미지 질의응답
        System.out.println("\n이미지 기반 질의응답:");
        System.out.println("-".repeat(30));

        String imagePath = "cat.jpg";
        String[] questions = {
            "이 이미지에서 무엇을 볼 수 있나요?",
            "주요 특징을 설명해주세요"
        };

        for (String question : questions) {
            System.out.println("\nQ: " + question);
            String answer = vision.answerQuestion(imagePath, question);
            System.out.println("A: " + answer);
        }

        // 3. Audio (음성 처리)
        System.out.println("\n[ 3. Audio - 음성 처리 ]");
        System.out.println("-".repeat(40));

        AudioProcessor audio = new AudioProcessor();

        // STT 데모
        System.out.println("\n[STT: Speech-to-Text]");
        String[] audioFiles = {"greeting.mp3", "question.mp3", "command.mp3"};

        for (String audioFile : audioFiles) {
            System.out.println("\n" + "─".repeat(30));
            AudioProcessor.STTResult sttResult = audio.transcribe(audioFile);
            sttResult.print();
        }

        // TTS 데모
        System.out.println("\n[TTS: Text-to-Speech]");
        System.out.println("-".repeat(30));
        System.out.println("\n사용 가능한 음성: " + audio.getAvailableVoices());

        String[] textsToSpeak = {
            "안녕하세요, 저는 AI 어시스턴트입니다.",
            "오늘의 날씨는 맑고 기온은 15도입니다."
        };

        for (String text : textsToSpeak) {
            System.out.println("\n" + "─".repeat(30));
            AudioProcessor.TTSResult ttsResult = audio.synthesize(text);
            ttsResult.print();
        }

        // 4. API 사용 예시
        System.out.println("\n[ 4. Vision API 사용 예시 ]");
        System.out.println("-".repeat(40));

        String visionApiExample = """

            // OpenAI GPT-4 Vision
            response = client.chat.completions.create(
                model="gpt-4-vision-preview",
                messages=[{
                    "role": "user",
                    "content": [
                        {"type": "text", "text": "이 이미지를 설명해줘"},
                        {"type": "image_url", "image_url": {"url": "data:image/jpeg;base64,..."}}
                    ]
                }]
            )

            // Claude Vision
            response = anthropic.messages.create(
                model="claude-3-opus-20240229",
                messages=[{
                    "role": "user",
                    "content": [
                        {"type": "image", "source": {"type": "base64", "media_type": "image/png", "data": "..."}},
                        {"type": "text", "text": "이 다이어그램을 분석해줘"}
                    ]
                }]
            )
            """;
        System.out.println(visionApiExample);

        // 5. Audio API 예시
        System.out.println("\n[ 5. Audio API 사용 예시 ]");
        System.out.println("-".repeat(40));

        String audioApiExample = """

            // OpenAI Whisper (STT)
            transcript = client.audio.transcriptions.create(
                model="whisper-1",
                file=open("audio.mp3", "rb")
            )

            // OpenAI TTS
            response = client.audio.speech.create(
                model="tts-1",
                voice="alloy",
                input="안녕하세요, 반갑습니다."
            )
            response.stream_to_file("output.mp3")

            // Google Cloud STT
            response = speech_client.recognize(
                config={"language_code": "ko-KR"},
                audio={"content": audio_content}
            )
            """;
        System.out.println(audioApiExample);

        // 6. 멀티모달 활용 사례
        System.out.println("\n[ 6. 멀티모달 활용 사례 ]");
        System.out.println("-".repeat(40));
        System.out.println();
        System.out.println("1. 문서 분석");
        System.out.println("   - PDF, 이미지에서 텍스트 추출");
        System.out.println("   - 차트/그래프 데이터 해석");
        System.out.println("   - 영수증, 계약서 처리");
        System.out.println();
        System.out.println("2. 접근성");
        System.out.println("   - 시각 장애인용 이미지 설명");
        System.out.println("   - 음성 인터페이스");
        System.out.println("   - 실시간 자막 생성");
        System.out.println();
        System.out.println("3. 콘텐츠 생성");
        System.out.println("   - 이미지 기반 글쓰기");
        System.out.println("   - 팟캐스트 → 블로그 변환");
        System.out.println("   - 동영상 요약");
        System.out.println();
        System.out.println("4. 교육");
        System.out.println("   - 문제 사진 → 풀이 설명");
        System.out.println("   - 발음 교정");
        System.out.println("   - 인터랙티브 학습");

        // 7. 모달리티별 모델
        System.out.println("\n[ 7. 주요 모델 ]");
        System.out.println("-".repeat(40));
        System.out.println();
        System.out.println("┌────────────┬─────────────────────────────────┐");
        System.out.println("│ 모달리티   │ 모델                            │");
        System.out.println("├────────────┼─────────────────────────────────┤");
        System.out.println("│ Vision     │ GPT-4V, Claude Vision, Gemini   │");
        System.out.println("│ STT        │ Whisper, Google STT, Clova STT  │");
        System.out.println("│ TTS        │ OpenAI TTS, ElevenLabs, Coqui   │");
        System.out.println("│ Video      │ Gemini, Sora (생성)             │");
        System.out.println("│ 이미지 생성│ DALL-E, Midjourney, Stable Diff │");
        System.out.println("└────────────┴─────────────────────────────────┘");

        // 요약
        System.out.println("\n========================================");
        System.out.println("핵심 정리");
        System.out.println("========================================");
        System.out.println("1. Multimodal: 텍스트, 이미지, 오디오 통합 처리");
        System.out.println("2. Vision: 이미지 분석, 문서 처리, VQA");
        System.out.println("3. STT/TTS: 음성 ↔ 텍스트 변환");
        System.out.println("4. 활용: 문서 분석, 접근성, 콘텐츠 생성");
        System.out.println("5. API: OpenAI, Google Cloud, Claude 등 제공");
    }
}
