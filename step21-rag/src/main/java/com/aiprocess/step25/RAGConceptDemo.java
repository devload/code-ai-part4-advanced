package com.aiprocess.step25;

import java.util.*;

/**
 * RAG 개념 데모: 검색 증강 생성의 기초
 */
public class RAGConceptDemo {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("STEP 25: RAG (Retrieval-Augmented Generation)");
        System.out.println("========================================\n");

        // 1. RAG 개념 소개
        System.out.println("[ 1. RAG란? ]");
        System.out.println("-".repeat(40));
        System.out.println("RAG = 검색 (Retrieval) + 생성 (Generation)");
        System.out.println();
        System.out.println("문제: LLM은 학습 데이터만 알고 있음");
        System.out.println("  - 최신 정보 모름");
        System.out.println("  - 회사 내부 문서 모름");
        System.out.println("  - 할루시네이션 발생 가능");
        System.out.println();
        System.out.println("해결: 필요한 정보를 검색해서 함께 제공!");
        System.out.println();
        System.out.println("┌─────────────────────────────────────────┐");
        System.out.println("│  질문 → [검색] → 관련문서 → [LLM] → 답변  │");
        System.out.println("└─────────────────────────────────────────┘");

        // 2. 문서 청킹 데모
        System.out.println("\n[ 2. 문서 청킹 (Chunking) ]");
        System.out.println("-".repeat(40));
        System.out.println("큰 문서를 작은 조각으로 나누는 과정\n");

        String sampleDocument = """
            인공지능(AI)은 인간의 학습, 추론, 자기계발 등을 컴퓨터가 할 수 있도록 하는 방법을 연구하는 분야입니다.

            머신러닝은 AI의 한 분야로, 데이터를 통해 학습하는 알고리즘을 개발합니다.
            지도학습, 비지도학습, 강화학습 등의 방법이 있습니다.

            딥러닝은 머신러닝의 한 종류로, 인공신경망을 사용합니다.
            특히 많은 층(deep layers)을 가진 신경망을 사용하여 복잡한 패턴을 학습합니다.

            자연어 처리(NLP)는 AI가 인간의 언어를 이해하고 생성하는 기술입니다.
            GPT, BERT, Claude 등의 대형 언어 모델이 대표적입니다.
            """;

        DocumentChunker chunker = new DocumentChunker(150, 30);
        List<String> chunks = chunker.chunkByParagraph(sampleDocument);
        chunker.visualizeChunks(chunks);

        // 3. 임베딩과 벡터 저장
        System.out.println("\n[ 3. 임베딩 & 벡터 저장소 ]");
        System.out.println("-".repeat(40));
        System.out.println("텍스트를 벡터로 변환하여 저장\n");

        SimpleEmbedding embedding = new SimpleEmbedding(64);
        SimpleVectorStore vectorStore = new SimpleVectorStore(embedding);

        // 청크들을 벡터 저장소에 추가
        vectorStore.addDocuments(chunks);

        System.out.printf("저장된 청크 수: %d개%n", vectorStore.size());

        // 임베딩 시각화
        System.out.println("\n임베딩 예시:");
        float[] vec = embedding.embed("딥러닝");
        System.out.print("'딥러닝' → [");
        for (int i = 0; i < 5; i++) {
            System.out.printf("%.3f", vec[i]);
            if (i < 4) System.out.print(", ");
        }
        System.out.println(", ...]");

        // 4. 검색 데모
        System.out.println("\n[ 4. 유사도 검색 ]");
        System.out.println("-".repeat(40));
        System.out.println("질문과 관련된 문서 청크 찾기\n");

        String[] queries = {
            "딥러닝이 뭐야?",
            "GPT는 어떤 기술이야?",
            "AI 학습 방법"
        };

        for (String query : queries) {
            vectorStore.visualizeSearch(query, 2);
        }

        // 5. RAG 프롬프트 생성
        System.out.println("\n[ 5. RAG 프롬프트 구성 ]");
        System.out.println("-".repeat(40));
        System.out.println("검색 결과를 프롬프트에 포함\n");

        String userQuestion = "딥러닝에 대해 설명해줘";
        List<SimpleVectorStore.SearchResult> searchResults =
            vectorStore.search(userQuestion, 2);

        StringBuilder ragPrompt = new StringBuilder();
        ragPrompt.append("다음 문서를 참고하여 질문에 답변하세요.\n\n");
        ragPrompt.append("### 참고 문서:\n");

        for (int i = 0; i < searchResults.size(); i++) {
            ragPrompt.append(String.format("[문서 %d]\n%s\n\n",
                i + 1, searchResults.get(i).document.content));
        }

        ragPrompt.append("### 질문:\n");
        ragPrompt.append(userQuestion);
        ragPrompt.append("\n\n### 답변:\n");

        System.out.println("생성된 RAG 프롬프트:");
        System.out.println("─".repeat(40));
        System.out.println(ragPrompt.toString());

        // 6. RAG vs 일반 LLM 비교
        System.out.println("\n[ 6. RAG의 장점 ]");
        System.out.println("-".repeat(40));
        System.out.println();
        System.out.println("┌────────────────┬────────────────────┐");
        System.out.println("│   일반 LLM     │       RAG          │");
        System.out.println("├────────────────┼────────────────────┤");
        System.out.println("│ 학습 데이터만  │ + 외부 지식 활용    │");
        System.out.println("│ 최신 정보 없음 │ + 실시간 정보 가능  │");
        System.out.println("│ 출처 불분명    │ + 출처 명시 가능    │");
        System.out.println("│ 할루시네이션   │ + 사실 기반 답변    │");
        System.out.println("└────────────────┴────────────────────┘");

        // 요약
        System.out.println("\n========================================");
        System.out.println("핵심 정리");
        System.out.println("========================================");
        System.out.println("1. 청킹: 문서를 적절한 크기로 분할");
        System.out.println("2. 임베딩: 텍스트를 벡터로 변환");
        System.out.println("3. 저장: 벡터 DB에 인덱싱");
        System.out.println("4. 검색: 쿼리와 유사한 문서 찾기");
        System.out.println("5. 생성: 검색 결과 + 질문 → LLM → 답변");
        System.out.println("\n→ STEP 26에서 실제 구현을 다룹니다!");
    }
}
