package com.aiprocess.step27;

import java.util.*;

/**
 * AI Agent 데모: 도구 사용과 ReAct 패턴
 */
public class AgentDemo {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("STEP 27: AI Agent");
        System.out.println("========================================\n");

        // 1. Agent 개념 소개
        System.out.println("[ 1. AI Agent란? ]");
        System.out.println("-".repeat(40));
        System.out.println("AI Agent = LLM + 도구 사용 + 자율 실행\n");
        System.out.println("일반 LLM:");
        System.out.println("  사용자 → 질문 → LLM → 답변\n");
        System.out.println("AI Agent:");
        System.out.println("  사용자 → 태스크 → Agent → [생각→행동→관찰]* → 결과\n");
        System.out.println("핵심 능력:");
        System.out.println("  1. 도구 사용 (Function Calling)");
        System.out.println("  2. 계획 수립 및 실행");
        System.out.println("  3. 자기 반성 및 오류 수정");

        // 2. 도구 정의
        System.out.println("\n[ 2. 도구 (Tools) 정의 ]");
        System.out.println("-".repeat(40));

        List<Tool> tools = Arrays.asList(
            new CalculatorTool(),
            new WeatherTool(),
            new SearchTool()
        );

        System.out.println("등록된 도구:");
        for (Tool tool : tools) {
            System.out.printf("\n📦 %s%n", tool.getName());
            System.out.println("   설명: " + tool.getDescription());
            System.out.println("   파라미터:");
            for (Map.Entry<String, String> param : tool.getParameters().entrySet()) {
                System.out.printf("     - %s: %s%n", param.getKey(), param.getValue());
            }
        }

        // 3. ReAct 패턴
        System.out.println("\n[ 3. ReAct 패턴 ]");
        System.out.println("-".repeat(40));
        System.out.println("ReAct = Reason (추론) + Act (행동)\n");
        System.out.println("┌─────────────────────────────────────────┐");
        System.out.println("│  Thought: 무엇을 해야 할지 생각         │");
        System.out.println("│     ↓                                   │");
        System.out.println("│  Action: 도구 선택 및 실행              │");
        System.out.println("│     ↓                                   │");
        System.out.println("│  Observation: 결과 관찰                 │");
        System.out.println("│     ↓                                   │");
        System.out.println("│  (반복 또는 종료)                       │");
        System.out.println("└─────────────────────────────────────────┘");

        // 4. Agent 실행
        System.out.println("\n[ 4. Agent 실행 데모 ]");
        System.out.println("-".repeat(40));

        SimpleAgent agent = new SimpleAgent(5);
        for (Tool tool : tools) {
            agent.registerTool(tool);
        }

        // 다양한 태스크 실행
        String[] tasks = {
            "서울 날씨 알려줘",
            "25 + 37 계산해줘",
            "파이썬에 대해 검색해줘"
        };

        for (String task : tasks) {
            agent.run(task);
            System.out.println();
        }

        // 5. Function Calling 형식
        System.out.println("\n[ 5. Function Calling (OpenAI 스타일) ]");
        System.out.println("-".repeat(40));
        System.out.println("실제 API에서는 JSON 형식으로 도구를 정의합니다:\n");

        String functionDef = """
            {
              "name": "weather",
              "description": "특정 도시의 현재 날씨 정보를 조회합니다.",
              "parameters": {
                "type": "object",
                "properties": {
                  "city": {
                    "type": "string",
                    "description": "날씨를 조회할 도시 이름"
                  }
                },
                "required": ["city"]
              }
            }
            """;
        System.out.println(functionDef);

        System.out.println("LLM 응답 예시:");
        String toolCall = """
            {
              "tool_calls": [{
                "function": {
                  "name": "weather",
                  "arguments": "{\\"city\\": \\"서울\\"}"
                }
              }]
            }
            """;
        System.out.println(toolCall);

        // 6. Agent 아키텍처
        System.out.println("\n[ 6. Agent 아키텍처 패턴 ]");
        System.out.println("-".repeat(40));
        System.out.println();
        System.out.println("1. ReAct Agent");
        System.out.println("   - Thought → Action → Observation 반복");
        System.out.println("   - 가장 기본적인 패턴");
        System.out.println();
        System.out.println("2. Plan-and-Execute");
        System.out.println("   - 먼저 전체 계획 수립");
        System.out.println("   - 계획에 따라 순차 실행");
        System.out.println();
        System.out.println("3. Multi-Agent");
        System.out.println("   - 여러 Agent가 협력");
        System.out.println("   - 각 Agent가 전문 역할 담당");
        System.out.println();
        System.out.println("4. Reflexion");
        System.out.println("   - 실행 결과를 반성");
        System.out.println("   - 오류 시 개선된 방법으로 재시도");

        // 7. 실제 Agent 프레임워크
        System.out.println("\n[ 7. Agent 프레임워크 ]");
        System.out.println("-".repeat(40));
        System.out.println();
        System.out.println("┌────────────────────────────────────────┐");
        System.out.println("│  프레임워크         │  특징             │");
        System.out.println("├────────────────────────────────────────┤");
        System.out.println("│  LangChain Agents   │ 풍부한 도구 지원  │");
        System.out.println("│  AutoGPT            │ 자율 실행         │");
        System.out.println("│  CrewAI             │ 멀티 에이전트     │");
        System.out.println("│  Claude MCP         │ 도구 프로토콜     │");
        System.out.println("│  OpenAI Assistants  │ 통합 플랫폼       │");
        System.out.println("└────────────────────────────────────────┘");

        // 요약
        System.out.println("\n========================================");
        System.out.println("핵심 정리");
        System.out.println("========================================");
        System.out.println("1. Agent = LLM + Tools + 자율 실행");
        System.out.println("2. ReAct: Thought → Action → Observation");
        System.out.println("3. Function Calling: 도구를 JSON으로 정의");
        System.out.println("4. 다양한 아키텍처: ReAct, Plan-Execute, Multi-Agent");
        System.out.println("5. 프레임워크: LangChain, CrewAI, Claude MCP 등");
    }
}
