package com.aiprocess.step27;

import java.util.*;

/**
 * 검색 도구 (시뮬레이션)
 */
public class SearchTool implements Tool {

    private final Map<String, List<String>> searchDatabase = new HashMap<>();

    public SearchTool() {
        // 시뮬레이션용 검색 데이터
        searchDatabase.put("자바", Arrays.asList(
            "Java는 객체지향 프로그래밍 언어입니다.",
            "Java 21 LTS가 2023년 9월 출시되었습니다.",
            "Spring Boot는 Java 기반 웹 프레임워크입니다."
        ));
        searchDatabase.put("파이썬", Arrays.asList(
            "Python은 데이터 과학에 널리 사용됩니다.",
            "Python 3.12가 2023년 10월 출시되었습니다.",
            "Django와 FastAPI는 Python 웹 프레임워크입니다."
        ));
        searchDatabase.put("인공지능", Arrays.asList(
            "ChatGPT는 OpenAI가 개발한 대화형 AI입니다.",
            "Claude는 Anthropic이 개발한 AI 어시스턴트입니다.",
            "GPT-4는 멀티모달 기능을 지원합니다."
        ));
    }

    @Override
    public String getName() {
        return "search";
    }

    @Override
    public String getDescription() {
        return "웹에서 정보를 검색합니다.";
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("query", "검색할 키워드");
        return params;
    }

    @Override
    public String execute(Map<String, Object> params) {
        String query = (String) params.get("query");
        if (query == null || query.isEmpty()) {
            return "오류: 검색어를 입력해주세요.";
        }

        StringBuilder result = new StringBuilder();
        result.append("검색 결과 (\"").append(query).append("\"):\n");

        boolean found = false;
        for (Map.Entry<String, List<String>> entry : searchDatabase.entrySet()) {
            if (query.toLowerCase().contains(entry.getKey().toLowerCase()) ||
                entry.getKey().toLowerCase().contains(query.toLowerCase())) {
                for (String item : entry.getValue()) {
                    result.append("  - ").append(item).append("\n");
                    found = true;
                }
            }
        }

        if (!found) {
            result.append("  관련 결과를 찾을 수 없습니다.");
        }

        return result.toString();
    }
}
