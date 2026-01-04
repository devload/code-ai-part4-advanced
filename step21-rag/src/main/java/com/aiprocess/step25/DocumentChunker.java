package com.aiprocess.step25;

import java.util.*;

/**
 * 문서 청킹: 큰 문서를 작은 조각으로 나누기
 *
 * RAG에서 중요한 전처리 단계
 */
public class DocumentChunker {

    private final int chunkSize;      // 청크 크기 (글자 수)
    private final int overlapSize;    // 오버랩 크기

    public DocumentChunker(int chunkSize, int overlapSize) {
        this.chunkSize = chunkSize;
        this.overlapSize = overlapSize;
    }

    /**
     * 문장 단위 청킹
     */
    public List<String> chunkBySentence(String document) {
        List<String> chunks = new ArrayList<>();

        // 문장 분리 (간단한 방식)
        String[] sentences = document.split("(?<=[.!?])\\s+");

        StringBuilder currentChunk = new StringBuilder();

        for (String sentence : sentences) {
            if (currentChunk.length() + sentence.length() > chunkSize && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString().trim());

                // 오버랩: 마지막 몇 문장 유지
                String overlap = getOverlapText(currentChunk.toString());
                currentChunk = new StringBuilder(overlap);
            }

            currentChunk.append(sentence).append(" ");
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    /**
     * 고정 크기 청킹
     */
    public List<String> chunkBySize(String document) {
        List<String> chunks = new ArrayList<>();

        int start = 0;
        while (start < document.length()) {
            int end = Math.min(start + chunkSize, document.length());

            // 단어 경계에서 자르기
            if (end < document.length()) {
                int lastSpace = document.lastIndexOf(' ', end);
                if (lastSpace > start) {
                    end = lastSpace;
                }
            }

            chunks.add(document.substring(start, end).trim());
            start = end - overlapSize;

            if (start < 0) start = end;
        }

        return chunks;
    }

    /**
     * 문단 단위 청킹
     */
    public List<String> chunkByParagraph(String document) {
        List<String> chunks = new ArrayList<>();

        String[] paragraphs = document.split("\n\n+");

        StringBuilder currentChunk = new StringBuilder();

        for (String para : paragraphs) {
            if (currentChunk.length() + para.length() > chunkSize && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString().trim());
                currentChunk = new StringBuilder();
            }

            currentChunk.append(para).append("\n\n");
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    private String getOverlapText(String text) {
        if (text.length() <= overlapSize) {
            return text;
        }

        int start = text.length() - overlapSize;
        int spacePos = text.indexOf(' ', start);

        if (spacePos > start) {
            return text.substring(spacePos + 1);
        }

        return text.substring(start);
    }

    /**
     * 청킹 결과 시각화
     */
    public void visualizeChunks(List<String> chunks) {
        System.out.println("\n청킹 결과:");
        System.out.println("=".repeat(50));

        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            String preview = chunk.length() > 80 ?
                chunk.substring(0, 80) + "..." : chunk;

            System.out.printf("청크 %d (%d자): %s%n",
                i + 1, chunk.length(), preview.replace("\n", " "));
        }

        System.out.println("=".repeat(50));
        System.out.printf("총 %d개 청크 생성%n", chunks.size());
    }
}
