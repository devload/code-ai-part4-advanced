package com.aiprocess.step26;

import com.aiprocess.step25.*;
import java.util.*;

/**
 * RAG 구현 데모: 실제 RAG 시스템 구축
 */
public class RAGImplementationDemo {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("STEP 26: RAG 구현");
        System.out.println("========================================\n");

        // 1. 샘플 문서 준비
        System.out.println("[ 1. 문서 준비 ]");
        System.out.println("-".repeat(40));

        Map<String, String> documents = new LinkedHashMap<>();

        documents.put("java_guide", """
            Java는 객체지향 프로그래밍 언어입니다.
            1995년 Sun Microsystems에서 개발되었으며, 현재는 Oracle이 관리합니다.

            Java의 주요 특징:
            - Write Once, Run Anywhere (WORA)
            - 가비지 컬렉션으로 메모리 자동 관리
            - 강력한 타입 시스템
            - 풍부한 표준 라이브러리

            Java는 엔터프라이즈 애플리케이션, Android 앱, 빅데이터 처리 등에 널리 사용됩니다.
            """);

        documents.put("python_guide", """
            Python은 간결하고 읽기 쉬운 프로그래밍 언어입니다.
            1991년 Guido van Rossum이 개발했습니다.

            Python의 주요 특징:
            - 간결한 문법과 높은 가독성
            - 동적 타이핑
            - 풍부한 라이브러리 생태계
            - 다양한 패러다임 지원 (객체지향, 함수형)

            Python은 데이터 과학, 머신러닝, 웹 개발, 자동화 스크립트에 많이 사용됩니다.
            """);

        documents.put("ai_intro", """
            인공지능(AI)은 인간의 지능을 모방하는 컴퓨터 시스템입니다.

            AI의 주요 분야:
            - 머신러닝: 데이터로부터 학습하는 알고리즘
            - 딥러닝: 심층 신경망을 사용한 학습
            - 자연어처리(NLP): 인간 언어 이해 및 생성
            - 컴퓨터 비전: 이미지/영상 인식

            최근 GPT, Claude 같은 대형 언어 모델(LLM)이 큰 주목을 받고 있습니다.
            이들은 Transformer 아키텍처를 기반으로 합니다.
            """);

        System.out.printf("준비된 문서: %d개%n", documents.size());
        for (String name : documents.keySet()) {
            System.out.println("  - " + name);
        }

        // 2. RAG 파이프라인 구성
        System.out.println("\n[ 2. RAG 파이프라인 구성 ]");
        System.out.println("-".repeat(40));

        RAGPipeline rag = new RAGPipeline(200, 64, 2);

        System.out.println("설정:");
        System.out.println("  - 청크 크기: 200자");
        System.out.println("  - 임베딩 차원: 64");
        System.out.println("  - Top-K: 2");

        // 3. 문서 인덱싱
        System.out.println("\n[ 3. 문서 인덱싱 ]");
        System.out.println("-".repeat(40));

        rag.indexDocuments(documents);
        System.out.printf("인덱싱 완료: %d개 청크%n", rag.getDocumentCount());

        // 4. RAG 쿼리 테스트
        System.out.println("\n[ 4. RAG 쿼리 테스트 ]");
        System.out.println("-".repeat(40));

        String[] questions = {
            "Java의 특징은 무엇인가요?",
            "Python은 어디에 사용되나요?",
            "GPT는 무엇을 기반으로 하나요?",
            "머신러닝이란?"
        };

        for (String question : questions) {
            RAGPipeline.RAGResponse response = rag.query(question);
            response.print();
        }

        // 5. 하이브리드 검색 데모
        System.out.println("\n[ 5. 하이브리드 검색 ]");
        System.out.println("-".repeat(40));
        System.out.println("키워드 검색 + 시맨틱 검색 결합\n");

        SimpleEmbedding embedding = new SimpleEmbedding(64);
        SimpleVectorStore vectorStore = new SimpleVectorStore(embedding);

        List<String> docs = new ArrayList<>();
        for (String content : documents.values()) {
            docs.add(content);
            vectorStore.addDocument("doc", content, new HashMap<>());
        }

        HybridSearch hybrid = new HybridSearch(vectorStore, 0.6f);
        for (String doc : docs) {
            hybrid.addDocument(doc);
        }

        System.out.println("시맨틱 가중치: 0.6 (키워드: 0.4)");

        String hybridQuery = "Java 프로그래밍";
        System.out.println("\n쿼리: \"" + hybridQuery + "\"");

        List<HybridSearch.HybridResult> hybridResults =
            hybrid.search(hybridQuery, 3);

        for (int i = 0; i < hybridResults.size(); i++) {
            HybridSearch.HybridResult r = hybridResults.get(i);
            System.out.printf("\n[%d] 종합 점수: %.3f%n", i + 1, r.combinedScore);
            System.out.printf("    키워드: %.3f, 시맨틱: %.3f%n",
                r.keywordScore, r.semanticScore);
            System.out.println("    " + truncate(r.content, 60));
        }

        // 6. RAG 최적화 팁
        System.out.println("\n[ 6. RAG 최적화 팁 ]");
        System.out.println("-".repeat(40));
        System.out.println();
        System.out.println("1. 청킹 전략");
        System.out.println("   - 너무 작으면: 문맥 손실");
        System.out.println("   - 너무 크면: 노이즈 증가");
        System.out.println("   - 권장: 256~512 토큰");
        System.out.println();
        System.out.println("2. 임베딩 모델 선택");
        System.out.println("   - OpenAI text-embedding-3-small");
        System.out.println("   - Cohere embed-v3");
        System.out.println("   - 한국어: KoSimCSE, KR-SBERT");
        System.out.println();
        System.out.println("3. 검색 전략");
        System.out.println("   - 하이브리드 검색 (키워드 + 시맨틱)");
        System.out.println("   - Re-ranking으로 정확도 향상");
        System.out.println("   - MMR로 다양성 확보");
        System.out.println();
        System.out.println("4. 프롬프트 엔지니어링");
        System.out.println("   - 명확한 지시문");
        System.out.println("   - 출처 표시 요청");
        System.out.println("   - \"모르면 모른다고 하라\" 지시");

        // 요약
        System.out.println("\n========================================");
        System.out.println("핵심 정리");
        System.out.println("========================================");
        System.out.println("1. RAG 파이프라인: 인덱싱 → 검색 → 생성");
        System.out.println("2. 하이브리드 검색: 키워드 + 시맨틱 결합");
        System.out.println("3. 청킹/임베딩/검색 각 단계 최적화 중요");
        System.out.println("4. 실제 구현 시 Pinecone, Weaviate 등 활용");
    }

    private static String truncate(String text, int maxLen) {
        text = text.replace("\n", " ").trim();
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen) + "...";
    }
}
