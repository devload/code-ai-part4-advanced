# STEP 25: RAG 개념

AI 모델에게 최신 정보를 제공하는 방법, RAG를 알아봅니다.

---

## AI의 한계

ChatGPT에게 "오늘 날씨 어때?"라고 물으면?

```
죄송합니다. 저는 실시간 정보에 접근할 수 없습니다.
제 학습 데이터는 2023년까지입니다.
```

AI 모델은 학습된 시점까지의 정보만 알고 있습니다. 이걸 **지식 컷오프(Knowledge Cutoff)**라고 합니다.

그럼 어떻게 최신 정보를 제공할까요?

---

## RAG란?

**RAG = Retrieval Augmented Generation**

한국어로 "검색 증강 생성"입니다.

원리는 간단합니다:

```
1. 사용자 질문을 받는다
2. 관련 문서를 검색한다
3. 검색 결과 + 질문을 AI에게 전달한다
4. AI가 답변을 생성한다
```

```
[사용자 질문]
     ↓
[검색] → 관련 문서 찾기
     ↓
[AI 모델] ← 질문 + 검색 결과
     ↓
[답변]
```

---

## 왜 RAG가 필요한가?

### 1. 최신 정보

모델을 다시 학습시키지 않아도 최신 문서를 제공할 수 있습니다.

### 2. 전문 지식

회사 내부 문서, 제품 매뉴얼 등 AI가 모르는 정보를 제공합니다.

### 3. Hallucination 감소

AI가 모르는 걸 지어내는 대신, 실제 문서를 기반으로 답변합니다.

### 4. 비용 효율

Fine-tuning보다 저렴하고 빠르게 적용할 수 있습니다.

---

## RAG의 구성 요소

### 1. 문서 저장소

검색할 문서들을 저장하는 곳입니다.

```
- 회사 위키
- 제품 문서
- FAQ
- 코드 저장소
```

### 2. 임베딩 (Embedding)

문서를 벡터(숫자 배열)로 변환합니다.

```
"오늘 날씨가 좋다" → [0.1, 0.3, -0.2, 0.8, ...]
```

비슷한 의미의 문장은 비슷한 벡터가 됩니다.

### 3. 벡터 데이터베이스

벡터를 저장하고 검색하는 특수한 데이터베이스입니다.

```
- Pinecone
- Weaviate
- Chroma
- pgvector (PostgreSQL)
```

### 4. 검색 (Retrieval)

질문과 가장 유사한 문서를 찾습니다.

```java
// 의사 코드
Vector queryVector = embed("사용자 질문");
List<Document> results = vectorDB.search(queryVector, topK=5);
```

### 5. 생성 (Generation)

검색된 문서와 함께 AI에게 질문합니다.

```
시스템: 아래 문서를 참고해서 질문에 답하세요.

[문서 1]: ...
[문서 2]: ...

사용자: 질문 내용
```

---

## 간단한 RAG 흐름

```java
public class SimpleRAG {

    public String answer(String question) {
        // 1. 질문을 벡터로 변환
        float[] questionVector = embeddingModel.embed(question);

        // 2. 유사한 문서 검색
        List<Document> relevantDocs = vectorDB.search(questionVector, 5);

        // 3. 프롬프트 구성
        String context = relevantDocs.stream()
            .map(Document::getContent)
            .collect(Collectors.joining("\n\n"));

        String prompt = String.format("""
            다음 문서를 참고해서 질문에 답하세요.

            [참고 문서]
            %s

            [질문]
            %s
            """, context, question);

        // 4. AI에게 질문
        return llm.generate(prompt);
    }
}
```

---

## RAG vs Fine-tuning

| 구분 | RAG | Fine-tuning |
|------|-----|-------------|
| 비용 | 저렴 | 비쌈 |
| 속도 | 즉시 적용 | 학습 시간 필요 |
| 최신성 | 문서 업데이트로 해결 | 재학습 필요 |
| 정확도 | 검색 품질에 의존 | 모델에 내재화 |
| 적합한 경우 | 문서 기반 Q&A | 스타일/행동 변경 |

대부분의 경우 RAG로 시작하는 것이 좋습니다.

---

## 핵심 정리

```
RAG = 검색 + 생성

1. 질문 → 벡터 변환 (Embedding)
2. 벡터 → 유사 문서 검색 (Retrieval)
3. 문서 + 질문 → AI 생성 (Generation)
```

**RAG의 장점:**
- 최신 정보 제공
- 전문 지식 활용
- Hallucination 감소
- 비용 효율적

---

## 다음 단계

다음 STEP에서는 RAG를 실제로 구현해봅니다. Embedding, Chunking, Vector DB를 직접 다뤄봅니다.

---

## 참고 자료

- [LangChain RAG 가이드](https://python.langchain.com/docs/tutorials/rag/)
- [OpenAI Embeddings](https://platform.openai.com/docs/guides/embeddings)
