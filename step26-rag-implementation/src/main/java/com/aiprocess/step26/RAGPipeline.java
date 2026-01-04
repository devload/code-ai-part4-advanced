package com.aiprocess.step26;

import com.aiprocess.step25.*;
import java.util.*;

/**
 * RAG 파이프라인: 전체 RAG 워크플로우 구현
 */
public class RAGPipeline {

    private final DocumentChunker chunker;
    private final SimpleEmbedding embedding;
    private final SimpleVectorStore vectorStore;
    private final int topK;

    public RAGPipeline(int chunkSize, int embeddingDim, int topK) {
        this.chunker = new DocumentChunker(chunkSize, chunkSize / 5);
        this.embedding = new SimpleEmbedding(embeddingDim);
        this.vectorStore = new SimpleVectorStore(embedding);
        this.topK = topK;
    }

    /**
     * 문서 인덱싱
     */
    public void indexDocument(String document, String source) {
        List<String> chunks = chunker.chunkByParagraph(document);

        for (int i = 0; i < chunks.size(); i++) {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("source", source);
            metadata.put("chunk_index", String.valueOf(i));

            vectorStore.addDocument(source + "_" + i, chunks.get(i), metadata);
        }
    }

    /**
     * 여러 문서 인덱싱
     */
    public void indexDocuments(Map<String, String> documents) {
        for (Map.Entry<String, String> entry : documents.entrySet()) {
            indexDocument(entry.getValue(), entry.getKey());
        }
    }

    /**
     * RAG 쿼리 실행
     */
    public RAGResponse query(String question) {
        // 1. 검색
        List<SimpleVectorStore.SearchResult> searchResults =
            vectorStore.search(question, topK);

        // 2. 컨텍스트 구성
        StringBuilder context = new StringBuilder();
        List<String> sources = new ArrayList<>();

        for (SimpleVectorStore.SearchResult result : searchResults) {
            context.append(result.document.content).append("\n\n");
            String source = result.document.metadata.get("source");
            if (source != null && !sources.contains(source)) {
                sources.add(source);
            }
        }

        // 3. 프롬프트 생성
        String prompt = buildPrompt(context.toString(), question);

        // 4. 응답 생성 (시뮬레이션)
        String answer = simulateLLMResponse(question, searchResults);

        return new RAGResponse(question, answer, sources, searchResults);
    }

    /**
     * RAG 프롬프트 생성
     */
    private String buildPrompt(String context, String question) {
        return String.format("""
            You are a helpful assistant. Answer the question based on the provided context.
            If the answer cannot be found in the context, say "I don't have enough information."

            ### Context:
            %s

            ### Question:
            %s

            ### Answer:
            """, context, question);
    }

    /**
     * LLM 응답 시뮬레이션 (실제로는 API 호출)
     */
    private String simulateLLMResponse(String question,
                                        List<SimpleVectorStore.SearchResult> results) {
        if (results.isEmpty()) {
            return "해당 질문에 대한 정보를 찾을 수 없습니다.";
        }

        // 가장 관련성 높은 문서 기반으로 응답 생성
        String topContent = results.get(0).document.content;

        // 간단한 응답 생성 (실제로는 LLM이 처리)
        if (question.contains("무엇") || question.contains("뭐")) {
            return "문서에 따르면: " + truncate(topContent, 150);
        } else if (question.contains("어떻게")) {
            return "다음과 같은 방법이 있습니다: " + truncate(topContent, 150);
        } else {
            return "관련 정보: " + truncate(topContent, 150);
        }
    }

    private String truncate(String text, int maxLen) {
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen) + "...";
    }

    public int getDocumentCount() {
        return vectorStore.size();
    }

    /**
     * RAG 응답 클래스
     */
    public static class RAGResponse {
        public final String question;
        public final String answer;
        public final List<String> sources;
        public final List<SimpleVectorStore.SearchResult> searchResults;

        public RAGResponse(String question, String answer, List<String> sources,
                          List<SimpleVectorStore.SearchResult> searchResults) {
            this.question = question;
            this.answer = answer;
            this.sources = sources;
            this.searchResults = searchResults;
        }

        public void print() {
            System.out.println("\n질문: " + question);
            System.out.println("─".repeat(40));
            System.out.println("답변: " + answer);
            System.out.println("\n참고 문서:");
            for (int i = 0; i < searchResults.size(); i++) {
                SimpleVectorStore.SearchResult r = searchResults.get(i);
                System.out.printf("  [%d] 유사도: %.3f%n", i + 1, r.similarity);
            }
            if (!sources.isEmpty()) {
                System.out.println("출처: " + String.join(", ", sources));
            }
        }
    }
}
