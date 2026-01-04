package com.aiprocess.step27;

import java.util.*;

/**
 * 간단한 AI Agent 구현
 *
 * ReAct (Reason + Act) 패턴 기반
 */
public class SimpleAgent {

    private final List<Tool> tools = new ArrayList<>();
    private final List<AgentStep> history = new ArrayList<>();
    private final int maxSteps;

    public SimpleAgent(int maxSteps) {
        this.maxSteps = maxSteps;
    }

    /**
     * 도구 등록
     */
    public void registerTool(Tool tool) {
        tools.add(tool);
    }

    /**
     * 등록된 도구 목록
     */
    public List<Tool> getTools() {
        return Collections.unmodifiableList(tools);
    }

    /**
     * 에이전트 실행 (ReAct 패턴)
     */
    public String run(String task) {
        history.clear();

        System.out.println("\n" + "=".repeat(50));
        System.out.println("Agent 실행 시작");
        System.out.println("=".repeat(50));
        System.out.println("태스크: " + task);
        System.out.println();

        String currentThought = task;

        for (int step = 0; step < maxSteps; step++) {
            System.out.println("─".repeat(50));
            System.out.printf("Step %d%n", step + 1);
            System.out.println("─".repeat(50));

            // 1. Thought (생각)
            String thought = think(currentThought, step);
            System.out.println("💭 Thought: " + thought);

            // 2. Action (행동 결정) - currentThought에 이전 관찰 결과가 있음
            ActionDecision action = decideAction(task, currentThought, step);

            if (action.toolName.equals("finish")) {
                System.out.println("✅ Action: 완료");
                System.out.println("📝 Final Answer: " + action.params.get("answer"));

                history.add(new AgentStep(step, thought, "finish",
                    action.params, (String) action.params.get("answer")));

                return (String) action.params.get("answer");
            }

            System.out.println("🔧 Action: " + action.toolName);
            System.out.println("📋 Params: " + action.params);

            // 3. Observation (관찰)
            String observation = executeAction(action);
            System.out.println("👁️ Observation: " + observation);

            history.add(new AgentStep(step, thought, action.toolName,
                action.params, observation));

            currentThought = observation;
        }

        return "최대 단계 수에 도달했습니다.";
    }

    /**
     * 생각 단계 (시뮬레이션)
     */
    private String think(String context, int step) {
        if (step == 0) {
            return "사용자의 요청을 분석합니다. 적절한 도구를 선택해야 합니다.";
        }
        return "이전 결과를 바탕으로 다음 행동을 결정합니다.";
    }

    /**
     * 행동 결정 (키워드 기반 시뮬레이션)
     *
     * @param task 원래 태스크
     * @param context 현재 컨텍스트 (이전 관찰 결과 포함)
     * @param step 현재 단계 (0부터 시작)
     */
    private ActionDecision decideAction(String task, String context, int step) {
        String lowerTask = task.toLowerCase();

        // step > 0이면 이전 도구 실행 결과가 context에 있음
        // 결과를 얻었으면 완료 처리
        if (step > 0) {
            if (context.contains("현재 날씨:") || context.contains("계산 결과:") ||
                context.contains("검색 결과")) {
                Map<String, Object> params = new HashMap<>();
                params.put("answer", context);
                return new ActionDecision("finish", params);
            }
        }

        // 키워드에 따라 도구 선택
        if (lowerTask.contains("날씨") || lowerTask.contains("기온")) {
            Map<String, Object> params = new HashMap<>();
            String city = extractCity(task);
            params.put("city", city);
            return new ActionDecision("weather", params);
        }

        if (lowerTask.contains("계산") || lowerTask.contains("+") ||
            lowerTask.contains("-") || lowerTask.contains("*") ||
            lowerTask.contains("/") || lowerTask.contains("더하") ||
            lowerTask.contains("빼") || lowerTask.contains("곱")) {
            Map<String, Object> params = new HashMap<>();
            params.put("expression", extractExpression(task));
            return new ActionDecision("calculator", params);
        }

        if (lowerTask.contains("검색") || lowerTask.contains("찾아") ||
            lowerTask.contains("알려줘") || lowerTask.contains("뭐야")) {
            Map<String, Object> params = new HashMap<>();
            params.put("query", extractSearchQuery(task));
            return new ActionDecision("search", params);
        }

        // 기본: 완료
        Map<String, Object> params = new HashMap<>();
        params.put("answer", "요청을 처리할 수 없습니다. 다른 방식으로 질문해주세요.");
        return new ActionDecision("finish", params);
    }

    private String extractCity(String text) {
        String[] cities = {"서울", "부산", "제주", "대전", "광주", "인천", "대구"};
        for (String city : cities) {
            if (text.contains(city)) return city;
        }
        return "서울";
    }

    private String extractExpression(String text) {
        // 숫자와 연산자 추출
        StringBuilder expr = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (Character.isDigit(c) || "+-*/. ".indexOf(c) >= 0) {
                expr.append(c);
            }
        }
        String result = expr.toString().trim();
        if (result.isEmpty()) result = "1+1";
        return result;
    }

    private String extractSearchQuery(String text) {
        // 간단한 쿼리 추출
        text = text.replace("검색", "").replace("찾아줘", "")
                   .replace("알려줘", "").replace("뭐야", "").trim();
        return text.isEmpty() ? "인공지능" : text;
    }

    /**
     * 행동 실행
     */
    private String executeAction(ActionDecision action) {
        for (Tool tool : tools) {
            if (tool.getName().equals(action.toolName)) {
                return tool.execute(action.params);
            }
        }
        return "도구를 찾을 수 없습니다: " + action.toolName;
    }

    /**
     * 실행 히스토리 출력
     */
    public void printHistory() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("Agent 실행 히스토리");
        System.out.println("=".repeat(50));

        for (AgentStep step : history) {
            System.out.printf("Step %d: %s → %s%n",
                step.stepNumber + 1, step.action, step.observation);
        }
    }

    /**
     * 행동 결정 클래스
     */
    public static class ActionDecision {
        public final String toolName;
        public final Map<String, Object> params;

        public ActionDecision(String toolName, Map<String, Object> params) {
            this.toolName = toolName;
            this.params = params;
        }
    }

    /**
     * 에이전트 단계 기록
     */
    public static class AgentStep {
        public final int stepNumber;
        public final String thought;
        public final String action;
        public final Map<String, Object> params;
        public final String observation;

        public AgentStep(int stepNumber, String thought, String action,
                        Map<String, Object> params, String observation) {
            this.stepNumber = stepNumber;
            this.thought = thought;
            this.action = action;
            this.params = params;
            this.observation = observation;
        }
    }
}
