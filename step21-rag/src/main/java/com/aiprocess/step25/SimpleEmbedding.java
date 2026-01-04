package com.aiprocess.step25;

import java.util.*;

/**
 * 간단한 임베딩 구현 (교육용)
 *
 * 실제로는 OpenAI Embedding API나 Sentence-BERT 등을 사용
 */
public class SimpleEmbedding {

    private final int dimension;
    private final Random random;
    private final Map<String, float[]> cache = new HashMap<>();

    public SimpleEmbedding(int dimension) {
        this.dimension = dimension;
        this.random = new Random(42); // 재현성을 위한 시드
    }

    /**
     * 텍스트를 벡터로 변환
     */
    public float[] embed(String text) {
        if (cache.containsKey(text)) {
            return cache.get(text);
        }

        // 단어들의 임베딩을 평균
        String[] words = text.toLowerCase().split("\\s+");
        float[] result = new float[dimension];

        for (String word : words) {
            float[] wordVec = getWordVector(word);
            for (int i = 0; i < dimension; i++) {
                result[i] += wordVec[i];
            }
        }

        // 평균 및 정규화
        float norm = 0;
        for (int i = 0; i < dimension; i++) {
            result[i] /= words.length;
            norm += result[i] * result[i];
        }
        norm = (float) Math.sqrt(norm);

        if (norm > 0) {
            for (int i = 0; i < dimension; i++) {
                result[i] /= norm;
            }
        }

        cache.put(text, result);
        return result;
    }

    /**
     * 단어의 벡터 (해시 기반 의사 랜덤)
     */
    private float[] getWordVector(String word) {
        float[] vec = new float[dimension];
        Random wordRandom = new Random(word.hashCode());

        for (int i = 0; i < dimension; i++) {
            vec[i] = (wordRandom.nextFloat() - 0.5f) * 2;
        }

        return vec;
    }

    /**
     * 코사인 유사도 계산
     */
    public static float cosineSimilarity(float[] a, float[] b) {
        float dot = 0, normA = 0, normB = 0;

        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        return dot / (float) (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
