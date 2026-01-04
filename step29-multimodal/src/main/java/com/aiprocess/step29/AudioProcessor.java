package com.aiprocess.step29;

import java.util.*;

/**
 * 오디오 처리 시뮬레이션
 *
 * STT (Speech-to-Text), TTS (Text-to-Speech) 시뮬레이션
 */
public class AudioProcessor {

    // STT 시뮬레이션용 데이터
    private final Map<String, String> sttDatabase = new HashMap<>();

    // TTS 설정
    private String voice = "alloy";
    private float speed = 1.0f;

    public AudioProcessor() {
        // 샘플 오디오 파일 → 텍스트 매핑
        sttDatabase.put("greeting.mp3", "안녕하세요, 반갑습니다.");
        sttDatabase.put("question.mp3", "오늘 날씨가 어때요?");
        sttDatabase.put("command.mp3", "알람을 오전 7시로 설정해줘.");
        sttDatabase.put("conversation.mp3", "네, 알겠습니다. 다른 도움이 필요하신가요?");
    }

    /**
     * Speech-to-Text (시뮬레이션)
     */
    public STTResult transcribe(String audioPath) {
        String text = sttDatabase.get(audioPath);

        if (text == null) {
            return new STTResult(audioPath, "오디오를 인식할 수 없습니다.", "unknown", 0.5f);
        }

        // 언어 감지 (간단한 규칙 기반)
        String language = detectLanguage(text);

        return new STTResult(audioPath, text, language, 0.92f);
    }

    /**
     * Text-to-Speech (시뮬레이션)
     */
    public TTSResult synthesize(String text) {
        // 예상 오디오 길이 계산 (대략 1글자당 0.1초)
        float duration = text.length() * 0.1f / speed;

        // 가상의 오디오 파일 경로
        String outputPath = "output_" + System.currentTimeMillis() + ".mp3";

        return new TTSResult(outputPath, text, voice, duration);
    }

    /**
     * 음성 설정
     */
    public void setVoice(String voice) {
        this.voice = voice;
    }

    public void setSpeed(float speed) {
        this.speed = Math.max(0.5f, Math.min(2.0f, speed));
    }

    /**
     * 사용 가능한 음성 목록
     */
    public List<String> getAvailableVoices() {
        return Arrays.asList("alloy", "echo", "fable", "onyx", "nova", "shimmer");
    }

    private String detectLanguage(String text) {
        // 한글이 포함되어 있으면 한국어
        for (char c : text.toCharArray()) {
            if (c >= 0xAC00 && c <= 0xD7A3) {
                return "ko";
            }
        }
        return "en";
    }

    /**
     * STT 결과 클래스
     */
    public static class STTResult {
        public final String audioPath;
        public final String text;
        public final String language;
        public final float confidence;

        public STTResult(String audioPath, String text, String language, float confidence) {
            this.audioPath = audioPath;
            this.text = text;
            this.language = language;
            this.confidence = confidence;
        }

        public void print() {
            System.out.println("오디오: " + audioPath);
            System.out.println("변환 텍스트: " + text);
            System.out.println("언어: " + language);
            System.out.println("신뢰도: " + (confidence * 100) + "%");
        }
    }

    /**
     * TTS 결과 클래스
     */
    public static class TTSResult {
        public final String outputPath;
        public final String text;
        public final String voice;
        public final float duration;

        public TTSResult(String outputPath, String text, String voice, float duration) {
            this.outputPath = outputPath;
            this.text = text;
            this.voice = voice;
            this.duration = duration;
        }

        public void print() {
            System.out.println("출력 파일: " + outputPath);
            System.out.println("텍스트: " + text);
            System.out.println("음성: " + voice);
            System.out.printf("예상 길이: %.1f초%n", duration);
        }
    }
}
