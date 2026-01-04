package com.aiprocess.step29;

import java.util.*;

/**
 * 이미지 처리 시뮬레이션
 *
 * 실제로는 Vision API (GPT-4V, Claude Vision 등)를 사용
 */
public class ImageProcessor {

    // 시뮬레이션용 이미지 특징 데이터베이스
    private final Map<String, ImageFeatures> imageDatabase = new HashMap<>();

    public ImageProcessor() {
        // 샘플 이미지 데이터
        imageDatabase.put("cat.jpg", new ImageFeatures(
            "고양이",
            Arrays.asList("동물", "털", "귀여운", "반려동물"),
            "주황색 줄무늬 고양이가 소파 위에 앉아있습니다."
        ));

        imageDatabase.put("diagram.png", new ImageFeatures(
            "다이어그램",
            Arrays.asList("차트", "화살표", "박스", "플로우차트"),
            "데이터 처리 과정을 보여주는 플로우차트입니다."
        ));

        imageDatabase.put("code.png", new ImageFeatures(
            "코드 스크린샷",
            Arrays.asList("프로그래밍", "텍스트", "IDE", "소프트웨어"),
            "Python 코드가 표시된 IDE 스크린샷입니다."
        ));

        imageDatabase.put("chart.png", new ImageFeatures(
            "그래프",
            Arrays.asList("데이터", "시각화", "차트", "통계"),
            "월별 매출을 보여주는 막대 그래프입니다."
        ));
    }

    /**
     * 이미지 분석 (시뮬레이션)
     */
    public ImageAnalysisResult analyze(String imagePath) {
        ImageFeatures features = imageDatabase.get(imagePath);

        if (features == null) {
            // 알 수 없는 이미지에 대한 기본 응답
            return new ImageAnalysisResult(
                imagePath,
                "알 수 없음",
                Arrays.asList("분석 불가"),
                "이미지를 분석할 수 없습니다.",
                0.5f
            );
        }

        return new ImageAnalysisResult(
            imagePath,
            features.category,
            features.tags,
            features.description,
            0.95f
        );
    }

    /**
     * 이미지 + 질문 처리 (시뮬레이션)
     */
    public String answerQuestion(String imagePath, String question) {
        ImageFeatures features = imageDatabase.get(imagePath);

        if (features == null) {
            return "이미지를 분석할 수 없습니다.";
        }

        String lowerQuestion = question.toLowerCase();

        if (lowerQuestion.contains("무엇") || lowerQuestion.contains("뭐")) {
            return "이 이미지는 " + features.category + "입니다. " + features.description;
        }
        if (lowerQuestion.contains("설명")) {
            return features.description;
        }
        if (lowerQuestion.contains("특징") || lowerQuestion.contains("요소")) {
            return "주요 특징: " + String.join(", ", features.tags);
        }

        return features.description;
    }

    /**
     * 이미지 특징 클래스
     */
    private static class ImageFeatures {
        String category;
        List<String> tags;
        String description;

        ImageFeatures(String category, List<String> tags, String description) {
            this.category = category;
            this.tags = tags;
            this.description = description;
        }
    }

    /**
     * 이미지 분석 결과 클래스
     */
    public static class ImageAnalysisResult {
        public final String imagePath;
        public final String category;
        public final List<String> tags;
        public final String description;
        public final float confidence;

        public ImageAnalysisResult(String imagePath, String category,
                                   List<String> tags, String description, float confidence) {
            this.imagePath = imagePath;
            this.category = category;
            this.tags = tags;
            this.description = description;
            this.confidence = confidence;
        }

        public void print() {
            System.out.println("이미지: " + imagePath);
            System.out.println("분류: " + category + " (신뢰도: " + (confidence * 100) + "%)");
            System.out.println("태그: " + String.join(", ", tags));
            System.out.println("설명: " + description);
        }
    }
}
