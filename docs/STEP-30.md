# STEP 30: 종합 프로젝트

지금까지 배운 모든 것을 통합합니다.

---

## 프로젝트 개요

**AI 기반 코드 어시스턴트**를 만듭니다.

```
기능:
1. 코드 분석 및 리뷰 (Part 2)
2. AI 기반 개선 제안 (Part 3)
3. RAG로 문서 검색 (Part 4)
4. 음성/이미지 입력 지원 (Part 4)
5. 자동 수정 Agent (Part 4)
```

---

## 전체 아키텍처

```
┌─────────────────────────────────────────────────────────┐
│                    사용자 인터페이스                      │
│  [CLI]  [Web]  [IDE Plugin]  [Voice]  [Screenshot]      │
└─────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────┐
│                    입력 처리 레이어                       │
│  [텍스트] [이미지→Vision] [음성→STT] [파일 업로드]         │
└─────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────┐
│                    분석 레이어 (Part 2)                  │
│  [파싱] → [AST] → [패턴 매칭] → [이슈 탐지] → [점수화]     │
└─────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────┐
│                    RAG 레이어 (Part 4)                   │
│  [문서 검색] → [컨텍스트 구성] → [관련 예제 찾기]           │
└─────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────┐
│                    AI 레이어 (Part 3)                    │
│  [프롬프트 구성] → [LLM 호출] → [응답 파싱]                │
└─────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────┐
│                    Agent 레이어 (Part 4)                 │
│  [도구 선택] → [실행] → [검증] → [피드백 루프]             │
└─────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────┐
│                    출력 레이어                           │
│  [리뷰 결과] [수정된 코드] [음성 출력] [알림]              │
└─────────────────────────────────────────────────────────┘
```

---

## 핵심 컴포넌트

### 1. 통합 입력 처리

```java
public class UnifiedInputProcessor {

    private final WhisperClient whisper;
    private final VisionClient vision;

    public ProcessedInput process(RawInput input) {
        StringBuilder context = new StringBuilder();

        // 텍스트 입력
        if (input.getText() != null) {
            context.append(input.getText());
        }

        // 음성 → 텍스트
        if (input.getAudio() != null) {
            String transcribed = whisper.transcribe(input.getAudio());
            context.append("\n[음성]: ").append(transcribed);
        }

        // 이미지 → 코드 추출 (스크린샷의 경우)
        if (input.getImage() != null) {
            String extracted = vision.extractCode(input.getImage());
            context.append("\n[이미지에서 추출]: ").append(extracted);
        }

        // 파일 읽기
        if (input.getFilePath() != null) {
            String code = Files.readString(input.getFilePath());
            context.append("\n[파일]: ").append(code);
        }

        return new ProcessedInput(context.toString(), input);
    }
}
```

### 2. 분석 + RAG 통합

```java
public class AnalysisWithRAG {

    private final CodeAnalyzer analyzer;
    private final RAGSystem rag;

    public EnrichedAnalysis analyze(String code) {
        // 1. 코드 분석
        AnalysisResult analysis = analyzer.analyze(code);

        // 2. 발견된 이슈에 대한 문서 검색
        List<Document> relevantDocs = new ArrayList<>();

        for (Issue issue : analysis.getIssues()) {
            // 이슈 유형에 맞는 문서 검색
            String query = issue.getType() + " " + issue.getDescription();
            relevantDocs.addAll(rag.search(query, 3));
        }

        // 3. 유사 코드 예제 검색
        List<Document> codeExamples = rag.searchCodeExamples(code, 5);

        return new EnrichedAnalysis(analysis, relevantDocs, codeExamples);
    }
}
```

### 3. AI Agent 통합

```java
public class CodeAssistantAgent {

    private final Map<String, Tool> tools = Map.of(
        "analyze_code", new AnalyzeCodeTool(),
        "search_docs", new SearchDocsTool(),
        "apply_fix", new ApplyFixTool(),
        "run_tests", new RunTestsTool(),
        "explain_code", new ExplainCodeTool()
    );

    public String assist(String userRequest, String code) {
        List<Message> messages = new ArrayList<>();
        messages.add(Message.system("""
            당신은 코드 리뷰 전문가입니다.
            다음 도구를 사용할 수 있습니다:
            - analyze_code: 코드 분석
            - search_docs: 관련 문서 검색
            - apply_fix: 코드 수정 적용
            - run_tests: 테스트 실행
            - explain_code: 코드 설명

            사용자 요청을 완료할 때까지 도구를 사용하세요.
            """));

        messages.add(Message.user(userRequest + "\n\n코드:\n" + code));

        return runAgentLoop(messages);
    }

    private String runAgentLoop(List<Message> messages) {
        for (int i = 0; i < 10; i++) {
            ChatResponse response = llm.chat(messages, tools);

            if (response.getStopReason() == StopReason.END_TURN) {
                return response.getContent();
            }

            // 도구 실행
            for (ToolCall call : response.getToolCalls()) {
                String result = tools.get(call.getName())
                    .execute(call.getArguments());
                messages.add(Message.toolResult(call.getId(), result));
            }
        }

        return "작업을 완료하지 못했습니다.";
    }
}
```

### 4. 피드백 루프

```java
public class FeedbackLoop {

    public FinalResult processWithFeedback(String code, String request) {
        int maxAttempts = 3;
        String currentCode = code;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            // 1. Agent로 수정
            AgentResult result = agent.assist(request, currentCode);

            if (result.getModifiedCode() != null) {
                currentCode = result.getModifiedCode();
            }

            // 2. 검증
            ValidationResult validation = validate(currentCode);

            if (validation.isSuccess()) {
                return FinalResult.success(currentCode, result.getExplanation());
            }

            // 3. 피드백 구성
            request = String.format("""
                이전 수정에서 문제가 발생했습니다:
                %s

                다시 수정해주세요.
                """, validation.getErrors());
        }

        return FinalResult.partialSuccess(currentCode,
            "완전히 해결하지 못했습니다. 수동 검토가 필요합니다.");
    }

    private ValidationResult validate(String code) {
        List<String> errors = new ArrayList<>();

        // 구문 검사
        if (!syntaxChecker.isValid(code)) {
            errors.add("구문 오류가 있습니다");
        }

        // 분석
        AnalysisResult analysis = analyzer.analyze(code);
        if (analysis.hasCriticalIssues()) {
            errors.add("심각한 이슈가 남아있습니다: " +
                analysis.getCriticalIssues());
        }

        // 테스트
        TestResult tests = testRunner.run(code);
        if (!tests.allPassed()) {
            errors.add("테스트 실패: " + tests.getFailures());
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }
}
```

---

## 사용 시나리오

### 시나리오 1: CLI 코드 리뷰

```bash
$ code-ai review src/main/java/UserService.java

분석 중...
[점수] 72/100 (B등급)

[발견된 이슈]
1. (HIGH) SQL Injection 위험 - line 45
2. (MEDIUM) 메서드가 너무 깁니다 - line 20-80
3. (LOW) 사용하지 않는 import - line 3

[AI 제안]
1번 이슈에 대해 PreparedStatement를 사용하세요.
예시 코드: ...

자동 수정을 적용할까요? (y/n)
```

### 시나리오 2: 음성 리뷰

```
사용자: "이 코드 리뷰해줘" [화면 캡처]

AI (음성): "코드를 분석했습니다.
전체적으로 괜찮지만 45번 라인에서 SQL Injection 위험이 있네요.
PreparedStatement를 사용하는 것을 권장합니다.
수정해드릴까요?"

사용자: "응, 수정해줘"

AI (음성): "수정을 적용했습니다. 테스트도 통과했네요."
```

### 시나리오 3: RAG 기반 질문

```
사용자: "우리 프로젝트에서 인증은 어떻게 처리해?"

AI: [내부 문서 검색 중...]

"프로젝트에서는 JWT 기반 인증을 사용합니다.

[AuthService.java]에서 토큰을 생성하고,
[JwtFilter.java]에서 검증합니다.

관련 문서:
- docs/authentication.md
- docs/api-security.md

더 자세한 설명이 필요하신가요?"
```

---

## 확장 아이디어

### 1. IDE 플러그인

```java
// IntelliJ Plugin
public class CodeAIAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        String selectedCode = editor.getSelectionModel().getSelectedText();

        // 백그라운드에서 분석
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            String review = codeAI.review(selectedCode);

            // UI 업데이트
            ApplicationManager.getApplication().invokeLater(() -> {
                showReviewPopup(review);
            });
        });
    }
}
```

### 2. GitHub Integration

```java
// PR 코멘트로 리뷰 결과 등록
public void reviewPullRequest(String owner, String repo, int prNumber) {
    List<PullRequestFile> files = github.getPRFiles(owner, repo, prNumber);

    for (PullRequestFile file : files) {
        if (file.getFilename().endsWith(".java")) {
            String review = codeAI.review(file.getPatch());

            github.createReviewComment(owner, repo, prNumber,
                file.getFilename(), review);
        }
    }
}
```

### 3. Slack Bot

```java
@SlackEventListener
public void onMessage(MessageEvent event) {
    if (event.getText().contains("@code-ai")) {
        String response = codeAI.chat(event.getText());
        slack.postMessage(event.getChannel(), response);
    }
}
```

---

## 프로젝트 체크리스트

```
✅ Part 1: AI 모델의 원리
   - [ ] 토큰화 이해
   - [ ] 생성 원리 이해
   - [ ] 비용 구조 이해

✅ Part 2: 도메인 데이터 분석
   - [ ] 코드 파싱
   - [ ] 패턴 매칭
   - [ ] 이슈 탐지
   - [ ] 점수화

✅ Part 3: AI 서비스 통합
   - [ ] API 호출
   - [ ] 프롬프트 설계
   - [ ] 응답 파싱
   - [ ] 피드백 루프

✅ Part 4: 고급 주제
   - [ ] RAG 구현
   - [ ] Agent 구현
   - [ ] 멀티모달 지원
   - [ ] 통합 테스트
```

---

## 마무리

이 강의를 통해 배운 것:

1. **AI 모델의 원리** - 토큰화, 학습, 생성의 핵심
2. **데이터 분석** - 구조화, 패턴 탐지, 정량화
3. **AI 서비스** - API, 프롬프트, 피드백 루프
4. **고급 기술** - RAG, Agent, Fine-tuning, 멀티모달

이 패턴들은 코드 리뷰뿐 아니라 **모든 AI 서비스**에 적용됩니다:
- 문서 분석 시스템
- 고객 지원 챗봇
- 자동화 도구
- 교육 플랫폼

**시작이 반입니다.** 이제 여러분만의 AI 서비스를 만들어보세요!

---

## 참고 자료

- [OpenAI API 문서](https://platform.openai.com/docs)
- [Anthropic Claude 문서](https://docs.anthropic.com)
- [LangChain 가이드](https://python.langchain.com/docs)
- [Ollama 문서](https://ollama.com/docs)
