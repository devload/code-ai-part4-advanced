package com.aiprocess.step25;

import java.util.*;

/**
 * 간단한 벡터 저장소 (교육용)
 *
 * 실제로는 Pinecone, Weaviate, Chroma 등을 사용
 */
public class SimpleVectorStore {

    private final SimpleEmbedding embedding;
    private final List<Document> documents = new ArrayList<>();

    public SimpleVectorStore(SimpleEmbedding embedding) {
        this.embedding = embedding;
    }

    /**
     * 문서 저장
     */
    public void addDocument(String id, String content, Map<String, String> metadata) {
        float[] vector = embedding.embed(content);
        documents.add(new Document(id, content, vector, metadata));
    }

    /**
     * 여러 문서 저장
     */
    public void addDocuments(List<String> contents) {
        for (int i = 0; i < contents.size(); i++) {
            addDocument("doc_" + i, contents.get(i), new HashMap<>());
        }
    }

    /**
     * 유사한 문서 검색
     */
    public List<SearchResult> search(String query, int topK) {
        float[] queryVector = embedding.embed(query);

        List<SearchResult> results = new ArrayList<>();

        for (Document doc : documents) {
            float similarity = SimpleEmbedding.cosineSimilarity(queryVector, doc.vector);
            results.add(new SearchResult(doc, similarity));
        }

        // 유사도 순으로 정렬
        results.sort((a, b) -> Float.compare(b.similarity, a.similarity));

        return results.subList(0, Math.min(topK, results.size()));
    }

    /**
     * 검색 결과 시각화
     */
    public void visualizeSearch(String query, int topK) {
        System.out.println("\n검색 쿼리: \"" + query + "\"");
        System.out.println("-".repeat(50));

        List<SearchResult> results = search(query, topK);

        for (int i = 0; i < results.size(); i++) {
            SearchResult result = results.get(i);
            String preview = result.document.content.length() > 60 ?
                result.document.content.substring(0, 60) + "..." :
                result.document.content;

            System.out.printf("%d. [%.3f] %s%n",
                i + 1, result.similarity, preview.replace("\n", " "));
        }
    }

    public int size() {
        return documents.size();
    }

    /**
     * 문서 클래스
     */
    public static class Document {
        public final String id;
        public final String content;
        public final float[] vector;
        public final Map<String, String> metadata;

        public Document(String id, String content, float[] vector, Map<String, String> metadata) {
            this.id = id;
            this.content = content;
            this.vector = vector;
            this.metadata = metadata;
        }
    }

    /**
     * 검색 결과 클래스
     */
    public static class SearchResult {
        public final Document document;
        public final float similarity;

        public SearchResult(Document document, float similarity) {
            this.document = document;
            this.similarity = similarity;
        }
    }
}
