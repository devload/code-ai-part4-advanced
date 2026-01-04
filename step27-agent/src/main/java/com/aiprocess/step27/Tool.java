package com.aiprocess.step27;

import java.util.*;

/**
 * 도구 인터페이스: AI Agent가 사용할 수 있는 도구
 */
public interface Tool {

    /**
     * 도구 이름
     */
    String getName();

    /**
     * 도구 설명
     */
    String getDescription();

    /**
     * 파라미터 정보
     */
    Map<String, String> getParameters();

    /**
     * 도구 실행
     */
    String execute(Map<String, Object> params);
}
