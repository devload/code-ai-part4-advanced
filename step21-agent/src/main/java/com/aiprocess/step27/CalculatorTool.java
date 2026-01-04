package com.aiprocess.step27;

import java.util.*;

/**
 * 계산기 도구
 */
public class CalculatorTool implements Tool {

    @Override
    public String getName() {
        return "calculator";
    }

    @Override
    public String getDescription() {
        return "수학 계산을 수행합니다. 사칙연산과 간단한 수학 함수를 지원합니다.";
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("expression", "계산할 수학 표현식 (예: '2 + 3 * 4')");
        return params;
    }

    @Override
    public String execute(Map<String, Object> params) {
        String expression = (String) params.get("expression");
        if (expression == null || expression.isEmpty()) {
            return "오류: 계산할 표현식을 입력해주세요.";
        }

        try {
            double result = evaluate(expression);
            return String.format("계산 결과: %s = %.2f", expression, result);
        } catch (Exception e) {
            return "오류: 계산할 수 없는 표현식입니다 - " + e.getMessage();
        }
    }

    /**
     * 간단한 수식 계산기
     */
    private double evaluate(String expression) {
        expression = expression.replaceAll("\\s+", "");

        // 기본 연산자 처리
        if (expression.contains("+")) {
            String[] parts = expression.split("\\+", 2);
            return evaluate(parts[0]) + evaluate(parts[1]);
        }
        if (expression.contains("-") && expression.lastIndexOf("-") > 0) {
            int idx = expression.lastIndexOf("-");
            return evaluate(expression.substring(0, idx)) -
                   evaluate(expression.substring(idx + 1));
        }
        if (expression.contains("*")) {
            String[] parts = expression.split("\\*", 2);
            return evaluate(parts[0]) * evaluate(parts[1]);
        }
        if (expression.contains("/")) {
            String[] parts = expression.split("/", 2);
            return evaluate(parts[0]) / evaluate(parts[1]);
        }

        // 숫자 반환
        return Double.parseDouble(expression);
    }
}
