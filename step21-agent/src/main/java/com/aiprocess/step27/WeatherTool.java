package com.aiprocess.step27;

import java.util.*;

/**
 * 날씨 조회 도구 (시뮬레이션)
 */
public class WeatherTool implements Tool {

    private final Map<String, String> weatherData = new HashMap<>();

    public WeatherTool() {
        // 시뮬레이션용 날씨 데이터
        weatherData.put("서울", "맑음, 15°C");
        weatherData.put("부산", "흐림, 18°C");
        weatherData.put("제주", "비, 20°C");
        weatherData.put("대전", "맑음, 14°C");
        weatherData.put("광주", "구름많음, 16°C");
    }

    @Override
    public String getName() {
        return "weather";
    }

    @Override
    public String getDescription() {
        return "특정 도시의 현재 날씨 정보를 조회합니다.";
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("city", "날씨를 조회할 도시 이름 (예: '서울')");
        return params;
    }

    @Override
    public String execute(Map<String, Object> params) {
        String city = (String) params.get("city");
        if (city == null || city.isEmpty()) {
            return "오류: 도시 이름을 입력해주세요.";
        }

        String weather = weatherData.get(city);
        if (weather != null) {
            return String.format("%s의 현재 날씨: %s", city, weather);
        } else {
            return String.format("%s의 날씨 정보를 찾을 수 없습니다. 지원 도시: %s",
                city, String.join(", ", weatherData.keySet()));
        }
    }
}
