# STEP 27: AI Agent

AI가 스스로 판단하고 도구를 사용하는 Agent를 만듭니다.

---

## Agent란?

지금까지 만든 AI 서비스:

```
사용자 → AI → 답변 (끝)
```

Agent는 다릅니다:

```
사용자 → AI → 생각 → 도구 사용 → 결과 확인 → 또 생각 → ... → 최종 답변
```

AI가 **스스로 판단**하고 **도구를 선택**해서 **반복 실행**합니다.

---

## 왜 Agent가 필요한가?

### 단순 AI의 한계

```
사용자: "서울 날씨 알려주고, 비 오면 우산 사라고 알림 보내줘"

단순 AI: "죄송합니다. 저는 날씨 정보를 가져오거나
         알림을 보내는 기능이 없습니다."
```

### Agent의 처리

```
사용자: "서울 날씨 알려주고, 비 오면 우산 사라고 알림 보내줘"

Agent 생각: "날씨를 확인해야 하네. 날씨 API를 호출하자."
Agent 행동: weather_api.get("서울") → "비, 15도"

Agent 생각: "비가 오네. 알림을 보내야겠다."
Agent 행동: notification.send("우산 챙기세요!")

Agent 답변: "서울은 비가 오고 15도입니다. 우산 알림을 보냈습니다."
```

---

## Function Calling

Agent의 핵심 기술입니다. AI가 함수를 호출할 수 있게 합니다.

### 도구 정의

```java
public class WeatherTool {

    @Tool(description = "특정 도시의 현재 날씨를 조회합니다")
    public String getWeather(
        @Parameter(description = "도시 이름") String city
    ) {
        // 실제 날씨 API 호출
        return weatherAPI.get(city);
    }
}
```

### AI에게 도구 알려주기

```java
List<ToolDefinition> tools = List.of(
    ToolDefinition.builder()
        .name("get_weather")
        .description("특정 도시의 현재 날씨를 조회합니다")
        .parameters(Map.of(
            "type", "object",
            "properties", Map.of(
                "city", Map.of(
                    "type", "string",
                    "description", "도시 이름"
                )
            ),
            "required", List.of("city")
        ))
        .build()
);

ChatRequest request = ChatRequest.builder()
    .model("claude-3-5-sonnet")
    .messages(messages)
    .tools(tools)  // 도구 목록 전달
    .build();
```

### AI 응답 처리

```java
ChatResponse response = client.chat(request);

if (response.getStopReason() == StopReason.TOOL_USE) {
    // AI가 도구 사용을 요청함
    ToolCall toolCall = response.getToolCalls().get(0);

    String toolName = toolCall.getName();      // "get_weather"
    String args = toolCall.getArguments();      // {"city": "서울"}

    // 실제 도구 실행
    String result = executeTool(toolName, args);

    // 결과를 AI에게 다시 전달
    messages.add(Message.toolResult(toolCall.getId(), result));

    // 계속 대화
    response = client.chat(request);
}
```

---

## Agent 루프

```java
public class SimpleAgent {

    private final LLMClient llm;
    private final Map<String, Tool> tools;

    public String run(String userMessage) {
        List<Message> messages = new ArrayList<>();
        messages.add(Message.user(userMessage));

        int maxIterations = 10;  // 무한 루프 방지

        for (int i = 0; i < maxIterations; i++) {
            // AI 호출
            ChatResponse response = llm.chat(messages, tools);

            // 도구 사용 요청인가?
            if (response.getStopReason() == StopReason.TOOL_USE) {
                // 도구 실행
                for (ToolCall call : response.getToolCalls()) {
                    String result = executeTool(call);
                    messages.add(Message.toolResult(call.getId(), result));
                }
                // 루프 계속
            } else {
                // 최종 답변
                return response.getContent();
            }
        }

        return "최대 시도 횟수를 초과했습니다.";
    }

    private String executeTool(ToolCall call) {
        Tool tool = tools.get(call.getName());
        return tool.execute(call.getArguments());
    }
}
```

---

## ReAct 패턴

**Re**asoning + **Act**ing

AI가 생각을 말로 표현하고, 그에 따라 행동합니다.

### 프롬프트

```
당신은 도구를 사용할 수 있는 AI 어시스턴트입니다.

다음 형식으로 응답하세요:

Thought: 현재 상황과 다음에 할 일을 생각합니다
Action: 사용할 도구 이름
Action Input: 도구에 전달할 입력
Observation: 도구 실행 결과 (시스템이 채움)
... (반복)
Thought: 최종 답변을 정리합니다
Final Answer: 사용자에게 전달할 최종 답변
```

### 예시 실행

```
User: 삼성전자 주가와 SK하이닉스 주가를 비교해줘

Thought: 두 회사의 주가를 각각 조회해야 한다. 먼저 삼성전자를 조회하자.
Action: get_stock_price
Action Input: {"symbol": "005930"}
Observation: 72,500원

Thought: 삼성전자 주가를 얻었다. 이제 SK하이닉스를 조회하자.
Action: get_stock_price
Action Input: {"symbol": "000660"}
Observation: 178,000원

Thought: 두 주가를 모두 얻었다. 비교 결과를 정리하자.
Final Answer: 삼성전자는 72,500원, SK하이닉스는 178,000원입니다.
              SK하이닉스가 약 2.5배 높습니다.
```

---

## 도구 설계 원칙

### 1. 명확한 설명

```java
// 나쁜 예
@Tool(description = "검색")
public String search(String query) { ... }

// 좋은 예
@Tool(description = "웹에서 최신 정보를 검색합니다. 뉴스, 날씨, 주가 등 실시간 정보가 필요할 때 사용하세요.")
public String webSearch(
    @Parameter(description = "검색할 키워드나 질문") String query
) { ... }
```

### 2. 적절한 입출력

```java
// 나쁜 예: 너무 많은 파라미터
public String complexTool(String a, String b, String c, int d, boolean e) { ... }

// 좋은 예: 필수만
public String simpleTool(String mainInput) { ... }
```

### 3. 에러 처리

```java
public String safeTool(String input) {
    try {
        return actualLogic(input);
    } catch (Exception e) {
        return "오류 발생: " + e.getMessage();  // AI가 이해할 수 있게
    }
}
```

---

## 실용적인 도구들

### 코드 실행

```java
@Tool(description = "Python 코드를 실행합니다")
public String runPython(String code) {
    // 샌드박스에서 실행
    return pythonSandbox.execute(code);
}
```

### 파일 읽기/쓰기

```java
@Tool(description = "파일 내용을 읽습니다")
public String readFile(String path) {
    return Files.readString(Path.of(path));
}

@Tool(description = "파일에 내용을 씁니다")
public String writeFile(String path, String content) {
    Files.writeString(Path.of(path), content);
    return "저장 완료: " + path;
}
```

### 데이터베이스 조회

```java
@Tool(description = "SQL 쿼리를 실행합니다. SELECT만 허용됩니다.")
public String queryDatabase(String sql) {
    if (!sql.trim().toUpperCase().startsWith("SELECT")) {
        return "오류: SELECT 쿼리만 허용됩니다";
    }
    return database.query(sql).toString();
}
```

---

## 안전장치

### 1. 권한 제한

```java
public class SecureAgent {
    private Set<String> allowedTools = Set.of("search", "calculate");

    private String executeTool(ToolCall call) {
        if (!allowedTools.contains(call.getName())) {
            return "이 도구는 사용할 수 없습니다: " + call.getName();
        }
        return tools.get(call.getName()).execute(call.getArguments());
    }
}
```

### 2. 승인 요청

```java
if (isDangerousTool(toolCall)) {
    boolean approved = askUserApproval(
        "다음 작업을 실행할까요?\n" + toolCall.toString()
    );
    if (!approved) {
        return "사용자가 작업을 취소했습니다.";
    }
}
```

### 3. 실행 제한

```java
// 최대 반복 횟수
int maxIterations = 10;

// 최대 비용
double maxCost = 1.0;  // $1

// 타임아웃
Duration timeout = Duration.ofMinutes(5);
```

---

## 핵심 정리

```
Agent = AI + 도구 + 루프

1. AI가 상황 판단
2. 필요한 도구 선택
3. 도구 실행
4. 결과 확인
5. 완료될 때까지 반복
```

**핵심 기술:**
- Function Calling: AI가 함수 호출
- ReAct: 생각 → 행동 → 관찰 패턴
- Tool Design: 명확한 도구 설계

**안전장치 필수:**
- 권한 제한
- 사용자 승인
- 실행 제한

---

## 다음 단계

다음 STEP에서는 Fine-tuning을 다룹니다. 모델을 직접 학습시켜 원하는 행동을 하게 만듭니다.
