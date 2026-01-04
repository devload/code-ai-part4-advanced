# STEP 26: RAG 구현

RAG를 실제로 구현합니다. Chunking, Embedding, Vector DB를 다룹니다.

---

## RAG 파이프라인

```
[문서] → [Chunking] → [Embedding] → [Vector DB]
                                          ↓
[질문] → [Embedding] → [검색] → [생성] → [답변]
```

두 단계로 나뉩니다:
1. **인덱싱**: 문서를 저장하는 과정 (오프라인)
2. **검색/생성**: 질문에 답하는 과정 (온라인)

---

## 1단계: Chunking

문서가 길면 통째로 저장할 수 없습니다. 작은 조각으로 나눠야 합니다.

### 왜 Chunking이 필요한가?

```
문서 길이: 10,000 토큰
Embedding 모델 한계: 512 토큰
Context Window: 4,000 토큰
```

긴 문서를 그대로 쓰면 잘리거나 비용이 폭증합니다.

### Chunking 전략

**1. 고정 크기**

```java
public List<String> fixedSizeChunk(String text, int size, int overlap) {
    List<String> chunks = new ArrayList<>();
    int start = 0;

    while (start < text.length()) {
        int end = Math.min(start + size, text.length());
        chunks.add(text.substring(start, end));
        start += size - overlap;  // 겹치는 부분
    }

    return chunks;
}
```

**2. 문장/문단 기준**

```java
public List<String> sentenceChunk(String text, int maxTokens) {
    String[] sentences = text.split("(?<=[.!?])\\s+");
    List<String> chunks = new ArrayList<>();
    StringBuilder current = new StringBuilder();

    for (String sentence : sentences) {
        if (tokenCount(current + sentence) > maxTokens) {
            chunks.add(current.toString().trim());
            current = new StringBuilder();
        }
        current.append(sentence).append(" ");
    }

    if (!current.isEmpty()) {
        chunks.add(current.toString().trim());
    }

    return chunks;
}
```

**3. 코드는 특별 처리**

```java
// 함수/클래스 단위로 분할
public List<String> codeChunk(String code) {
    // AST를 사용해 함수 단위로 분리
    CompilationUnit cu = StaticJavaParser.parse(code);
    return cu.findAll(MethodDeclaration.class).stream()
        .map(MethodDeclaration::toString)
        .collect(Collectors.toList());
}
```

### Chunk 크기 가이드

| 용도 | 권장 크기 | 이유 |
|------|-----------|------|
| 일반 문서 | 500-1000자 | 충분한 맥락 유지 |
| 코드 | 함수 단위 | 논리적 단위 |
| FAQ | 질문+답변 단위 | 완결성 |

---

## 2단계: Embedding

텍스트를 벡터로 변환합니다.

### Embedding이란?

```
"고양이가 잔다" → [0.2, -0.1, 0.8, ...]
"강아지가 잔다" → [0.3, -0.1, 0.7, ...]  // 비슷한 벡터!
"주식이 오른다" → [-0.5, 0.9, 0.1, ...]  // 다른 벡터
```

의미가 비슷하면 벡터도 비슷합니다.

### Embedding API 호출

```java
public class EmbeddingClient {

    private final OpenAIClient client;

    public float[] embed(String text) {
        EmbeddingRequest request = EmbeddingRequest.builder()
            .model("text-embedding-3-small")
            .input(text)
            .build();

        EmbeddingResponse response = client.embeddings(request);
        return response.getData().get(0).getEmbedding();
    }

    public List<float[]> embedBatch(List<String> texts) {
        // 배치로 처리하면 더 효율적
        EmbeddingRequest request = EmbeddingRequest.builder()
            .model("text-embedding-3-small")
            .input(texts)
            .build();

        return client.embeddings(request).getData().stream()
            .map(Embedding::getEmbedding)
            .collect(Collectors.toList());
    }
}
```

### Embedding 모델 선택

| 모델 | 차원 | 비용 | 특징 |
|------|------|------|------|
| text-embedding-3-small | 1536 | 저렴 | 일반 용도 |
| text-embedding-3-large | 3072 | 보통 | 높은 정확도 |
| multilingual-e5 | 1024 | 무료 | 다국어 지원 |

---

## 3단계: Vector Database

벡터를 저장하고 검색합니다.

### 간단한 인메모리 구현

```java
public class SimpleVectorDB {

    private List<VectorEntry> entries = new ArrayList<>();

    public void insert(String id, String content, float[] vector) {
        entries.add(new VectorEntry(id, content, vector));
    }

    public List<SearchResult> search(float[] query, int topK) {
        return entries.stream()
            .map(e -> new SearchResult(e, cosineSimilarity(query, e.vector)))
            .sorted((a, b) -> Double.compare(b.score, a.score))
            .limit(topK)
            .collect(Collectors.toList());
    }

    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
```

### 실제 Vector DB 사용 (Chroma 예시)

```java
// Chroma 클라이언트 (의사 코드)
ChromaClient chroma = new ChromaClient("http://localhost:8000");
Collection collection = chroma.getOrCreateCollection("my_docs");

// 저장
collection.add(
    ids = List.of("doc1", "doc2"),
    embeddings = embeddings,
    documents = List.of("문서1 내용", "문서2 내용"),
    metadatas = List.of(Map.of("source", "wiki"), Map.of("source", "faq"))
);

// 검색
QueryResult results = collection.query(
    queryEmbeddings = queryVector,
    nResults = 5
);
```

---

## 4단계: 전체 통합

```java
public class RAGSystem {

    private final EmbeddingClient embedder;
    private final VectorDB vectorDB;
    private final LLMClient llm;

    // 인덱싱 (문서 저장)
    public void index(String documentId, String content) {
        // 1. Chunking
        List<String> chunks = chunk(content, 500);

        // 2. Embedding
        for (int i = 0; i < chunks.size(); i++) {
            String chunkId = documentId + "_" + i;
            float[] vector = embedder.embed(chunks.get(i));

            // 3. 저장
            vectorDB.insert(chunkId, chunks.get(i), vector);
        }
    }

    // 질문 답변
    public String query(String question) {
        // 1. 질문 Embedding
        float[] queryVector = embedder.embed(question);

        // 2. 검색
        List<SearchResult> results = vectorDB.search(queryVector, 5);

        // 3. 컨텍스트 구성
        String context = results.stream()
            .map(r -> r.content)
            .collect(Collectors.joining("\n\n---\n\n"));

        // 4. 프롬프트 생성
        String prompt = String.format("""
            아래 참고 문서를 바탕으로 질문에 답하세요.
            문서에 없는 내용은 "모르겠습니다"라고 답하세요.

            [참고 문서]
            %s

            [질문]
            %s

            [답변]
            """, context, question);

        // 5. 생성
        return llm.generate(prompt);
    }
}
```

---

## 검색 품질 향상 팁

### 1. 하이브리드 검색

벡터 검색 + 키워드 검색을 조합합니다.

```java
// 벡터 검색 결과
List<Result> vectorResults = vectorDB.search(queryVector, 10);

// 키워드 검색 결과 (BM25 등)
List<Result> keywordResults = keywordDB.search(question, 10);

// 점수 조합
List<Result> combined = mergeResults(vectorResults, keywordResults);
```

### 2. 메타데이터 필터링

```java
// 특정 소스에서만 검색
results = vectorDB.search(queryVector, 5,
    filter = Map.of("source", "official_docs"));
```

### 3. Reranking

검색 결과를 다시 정렬합니다.

```java
// Cross-encoder로 재정렬
List<Result> reranked = reranker.rerank(question, results);
```

---

## 핵심 정리

```
RAG 구현 4단계:

1. Chunking: 문서를 적절한 크기로 분할
2. Embedding: 텍스트를 벡터로 변환
3. Vector DB: 벡터 저장 및 검색
4. Generation: 검색 결과 + 질문 → AI 답변
```

**주의사항:**
- Chunk 크기가 너무 작으면 맥락 손실
- Chunk 크기가 너무 크면 검색 정확도 저하
- Embedding 모델과 LLM 모델은 별개

---

## 다음 단계

다음 STEP에서는 AI Agent를 다룹니다. AI가 스스로 도구를 사용하고 결정하는 방법을 배웁니다.
