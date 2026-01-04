# STEP 29: 멀티모달

텍스트뿐 아니라 이미지, 음성, 비디오까지 처리합니다.

---

## 멀티모달이란?

**Multi-Modal = 여러 형태의 입출력**

```
기존 LLM: 텍스트 → 텍스트
멀티모달: 텍스트 + 이미지 + 음성 → 텍스트 + 이미지 + 음성
```

### 대표 모델

| 모델 | 입력 | 출력 |
|------|------|------|
| GPT-4o | 텍스트, 이미지, 음성 | 텍스트, 음성 |
| Claude 3.5 | 텍스트, 이미지 | 텍스트 |
| Gemini | 텍스트, 이미지, 비디오 | 텍스트, 이미지 |
| DALL-E 3 | 텍스트 | 이미지 |
| Whisper | 음성 | 텍스트 |

---

## Vision: 이미지 이해

AI가 이미지를 보고 이해합니다.

### 이미지 입력

```java
// Base64 인코딩
String base64Image = Base64.getEncoder().encodeToString(
    Files.readAllBytes(Path.of("image.png"))
);

// API 호출
ChatRequest request = ChatRequest.builder()
    .model("gpt-4o")
    .messages(List.of(
        Message.user(List.of(
            ContentPart.text("이 이미지에 무엇이 있나요?"),
            ContentPart.image(base64Image, "image/png")
        ))
    ))
    .build();
```

### URL로 전달

```java
Message.user(List.of(
    ContentPart.text("이 사진을 설명해주세요"),
    ContentPart.imageUrl("https://example.com/photo.jpg")
))
```

### 실용적인 활용

**1. 코드 스크린샷 분석**

```
[스크린샷 첨부]
"이 에러 메시지를 보고 해결 방법을 알려줘"
```

**2. 다이어그램 이해**

```
[아키텍처 다이어그램 첨부]
"이 시스템 구조를 설명해줘"
```

**3. UI/UX 피드백**

```
[앱 스크린샷 첨부]
"이 화면의 UX를 개선할 점을 알려줘"
```

**4. 문서 OCR**

```
[손글씨/문서 사진 첨부]
"이 내용을 텍스트로 변환해줘"
```

---

## Image Generation: 이미지 생성

텍스트로 이미지를 만듭니다.

### DALL-E 3

```java
ImageRequest request = ImageRequest.builder()
    .model("dall-e-3")
    .prompt("고양이가 우주복을 입고 달에 서 있는 사실적인 사진")
    .size("1024x1024")
    .quality("hd")
    .n(1)
    .build();

ImageResponse response = client.images().create(request);
String imageUrl = response.getData().get(0).getUrl();
```

### 프롬프트 팁

```
# 나쁜 예
"고양이 그림"

# 좋은 예
"귀여운 주황색 고양이가 창가에 앉아 밖을 바라보고 있다.
따뜻한 오후 햇살이 들어오고, 수채화 스타일로 그려짐.
부드러운 파스텔 색상."
```

### 구성 요소

```
1. 주체: 무엇을 (고양이, 사람, 풍경)
2. 행동: 무엇을 하는지 (앉아있다, 걷고 있다)
3. 환경: 어디서 (창가, 숲속, 도시)
4. 스타일: 어떻게 (사실적, 수채화, 3D렌더링)
5. 분위기: 느낌 (따뜻한, 신비로운, 밝은)
```

---

## Speech: 음성 처리

### Speech-to-Text (STT)

음성을 텍스트로 변환합니다.

```java
// Whisper API
TranscriptionRequest request = TranscriptionRequest.builder()
    .model("whisper-1")
    .file(audioFile)
    .language("ko")
    .build();

TranscriptionResponse response = client.audio().transcriptions(request);
String text = response.getText();
```

### Text-to-Speech (TTS)

텍스트를 음성으로 변환합니다.

```java
SpeechRequest request = SpeechRequest.builder()
    .model("tts-1-hd")
    .input("안녕하세요, 오늘 날씨가 좋네요.")
    .voice("nova")  // alloy, echo, fable, onyx, nova, shimmer
    .responseFormat("mp3")
    .build();

byte[] audioBytes = client.audio().speech(request);
Files.write(Path.of("output.mp3"), audioBytes);
```

### 실시간 음성 대화

```java
// GPT-4o Realtime API (의사 코드)
RealtimeSession session = client.realtime().connect();

// 마이크 입력 스트리밍
session.onAudioInput(audioChunk -> {
    session.sendAudio(audioChunk);
});

// AI 음성 출력 스트리밍
session.onAudioOutput(audioChunk -> {
    speaker.play(audioChunk);
});

session.onTranscript(text -> {
    System.out.println("AI: " + text);
});
```

---

## 멀티모달 파이프라인

### 예: 음성 코드 리뷰

```
[음성 입력] "이 코드 리뷰해줘"
     ↓
[STT] 음성 → 텍스트
     ↓
[Vision] 화면 캡처 → 코드 인식
     ↓
[LLM] 코드 분석 + 리뷰 생성
     ↓
[TTS] 리뷰 결과 → 음성
     ↓
[음성 출력] "이 함수에서 null 체크가 빠졌네요..."
```

### 구현

```java
public class VoiceCodeReviewer {

    public void review() {
        // 1. 음성 입력
        byte[] audio = microphone.record(5000);  // 5초 녹음

        // 2. STT
        String command = whisper.transcribe(audio);

        // 3. 화면 캡처
        BufferedImage screen = captureScreen();
        String base64 = encodeImage(screen);

        // 4. Vision + LLM
        String review = gpt4o.chat(List.of(
            Message.user(List.of(
                ContentPart.text(command),
                ContentPart.image(base64, "image/png")
            ))
        ));

        // 5. TTS
        byte[] speech = tts.speak(review);

        // 6. 재생
        speaker.play(speech);
    }
}
```

---

## 비용 고려사항

| 모달 | 비용 요소 | 최적화 팁 |
|------|-----------|-----------|
| 이미지 입력 | 이미지 크기 | 필요한 해상도로 리사이즈 |
| 이미지 생성 | 생성 횟수 | 프롬프트 신중히 작성 |
| STT | 오디오 길이 | 무음 구간 제거 |
| TTS | 텍스트 길이 | 필요한 부분만 음성화 |

### 이미지 크기 최적화

```java
// 분석용: 낮은 해상도로 충분
BufferedImage resized = resize(original, 512, 512);

// 세부 분석 필요: 높은 해상도
BufferedImage highRes = resize(original, 2048, 2048);
```

---

## 실용 예제: 멀티모달 챗봇

```java
public class MultimodalChatbot {

    public String chat(String text, byte[] image, byte[] audio) {
        List<ContentPart> parts = new ArrayList<>();

        // 텍스트
        if (text != null) {
            parts.add(ContentPart.text(text));
        }

        // 이미지
        if (image != null) {
            parts.add(ContentPart.image(
                Base64.getEncoder().encodeToString(image),
                "image/png"
            ));
        }

        // 음성 → 텍스트 변환 후 추가
        if (audio != null) {
            String transcribed = whisper.transcribe(audio);
            parts.add(ContentPart.text("[음성 입력]: " + transcribed));
        }

        // AI 호출
        ChatResponse response = client.chat(
            Message.user(parts)
        );

        return response.getContent();
    }
}
```

---

## 핵심 정리

```
멀티모달 = 텍스트 + 이미지 + 음성

Vision (이미지 이해)
- 이미지 분석, OCR, UI 피드백

Image Generation (이미지 생성)
- DALL-E, Midjourney 등
- 좋은 프롬프트가 핵심

Speech (음성)
- STT: 음성 → 텍스트 (Whisper)
- TTS: 텍스트 → 음성
```

**활용 예:**
- 음성 명령 AI 비서
- 이미지 기반 Q&A
- 접근성 향상 (시각/청각 장애인 지원)
- 교육 콘텐츠

---

## 다음 단계

다음 STEP에서는 지금까지 배운 모든 것을 통합한 종합 프로젝트를 진행합니다.
