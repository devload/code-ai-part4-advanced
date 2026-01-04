package com.aiprocess.step26;

import com.aiprocess.step25.*;
import java.util.*;

/**
 * 하이브리드 검색: 키워드 + 시맨틱 검색 결합
 */
public class HybridSearch {

    private final SimpleVectorStore vectorStore;
    private final List<String> documents = new ArrayList<>();
    private final float semanticWeight;

    public HybridSearch(SimpleVectorStore vectorStore, float semanticWeight) {
        this.vectorStore = vectorStore;
        this.semanticWeight = semanticWeight; // 0.0 ~ 1.0
    }

    public void addDocument(String content) {
        documents.add(content);
    }

    /**
     * 키워드 검색 (BM25 간소화 버전)
     */
    public Map<Integer, Float> keywordSearch(String query) {
        Map<Integer, Float> scores = new HashMap<>();
        String[] queryTerms = query.toLowerCase().split("\\s+");

        for (int i = 0; i < documents.size(); i++) {
            String doc = documents.get(i).toLowerCase();
            float score = 0;

            for (String term : queryTerms) {
                if (doc.contains(term)) {
                    // 단순 TF (용어 빈도)
                    int count = countOccurrences(doc, term);
                    score += Math.log(1 + count);
                }
            }

            if (score > 0) {
                scores.put(i, score);
            }
        }

        return scores;
    }

    private int countOccurrences(String text, String term) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(term, index)) != -1) {
            count++;
            index += term.length();
        }
        return count;
    }

    /**
     * 하이브리드 검색
     */
    public List<HybridResult> search(String query, int topK) {
        // 키워드 점수
        Map<Integer, Float> keywordScores = keywordSearch(query);

        // 시맨틱 점수
        List<SimpleVectorStore.SearchResult> semanticResults =
            vectorStore.search(query, documents.size());

        // 점수 정규화 및 결합
        Map<Integer, Float> combinedScores = new HashMap<>();

        // 키워드 점수 정규화
        float maxKeyword = keywordScores.values().stream()
            .max(Float::compare).orElse(1f);

        for (Map.Entry<Integer, Float> entry : keywordScores.entrySet()) {
            float normalized = entry.getValue() / maxKeyword;
            combinedScores.put(entry.getKey(),
                normalized * (1 - semanticWeight));
        }

        // 시맨틱 점수 추가
        for (int i = 0; i < semanticResults.size(); i++) {
            SimpleVectorStore.SearchResult r = semanticResults.get(i);
            // 문서 인덱스 추출 (간단화)
            int docIndex = i;

            float semanticScore = r.similarity * semanticWeight;
            combinedScores.merge(docIndex, semanticScore, Float::sum);
        }

        // 결과 정렬
        List<HybridResult> results = new ArrayList<>();
        for (Map.Entry<Integer, Float> entry : combinedScores.entrySet()) {
            int idx = entry.getKey();
            if (idx < documents.size()) {
                results.add(new HybridResult(
                    documents.get(idx),
                    entry.getValue(),
                    keywordScores.getOrDefault(idx, 0f),
                    semanticResults.size() > idx ?
                        semanticResults.get(idx).similarity : 0f
                ));
            }
        }

        results.sort((a, b) -> Float.compare(b.combinedScore, a.combinedScore));
        return results.subList(0, Math.min(topK, results.size()));
    }

    /**
     * 하이브리드 검색 결과
     */
    public static class HybridResult {
        public final String content;
        public final float combinedScore;
        public final float keywordScore;
        public final float semanticScore;

        public HybridResult(String content, float combinedScore,
                           float keywordScore, float semanticScore) {
            this.content = content;
            this.combinedScore = combinedScore;
            this.keywordScore = keywordScore;
            this.semanticScore = semanticScore;
        }
    }
}
