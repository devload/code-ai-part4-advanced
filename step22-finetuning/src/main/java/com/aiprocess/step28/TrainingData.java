package com.aiprocess.step28;

import java.util.*;

/**
 * Fine-tuning용 학습 데이터
 */
public class TrainingData {

    private final List<DataSample> samples = new ArrayList<>();

    /**
     * 학습 샘플 추가
     */
    public void addSample(String input, String output) {
        samples.add(new DataSample(input, output));
    }

    /**
     * 대화형 샘플 추가
     */
    public void addConversation(List<Message> messages) {
        samples.add(new DataSample(messages));
    }

    /**
     * 전체 샘플 반환
     */
    public List<DataSample> getSamples() {
        return Collections.unmodifiableList(samples);
    }

    /**
     * 학습/검증 데이터 분할
     */
    public TrainTestSplit split(double trainRatio) {
        List<DataSample> shuffled = new ArrayList<>(samples);
        Collections.shuffle(shuffled, new Random(42));

        int trainSize = (int) (shuffled.size() * trainRatio);

        List<DataSample> trainData = shuffled.subList(0, trainSize);
        List<DataSample> testData = shuffled.subList(trainSize, shuffled.size());

        return new TrainTestSplit(trainData, testData);
    }

    /**
     * JSONL 형식으로 변환 (OpenAI 형식)
     */
    public String toJsonl() {
        StringBuilder sb = new StringBuilder();

        for (DataSample sample : samples) {
            sb.append(sample.toJson()).append("\n");
        }

        return sb.toString();
    }

    public int size() {
        return samples.size();
    }

    /**
     * 학습 샘플 클래스
     */
    public static class DataSample {
        public final String input;
        public final String output;
        public final List<Message> messages;

        public DataSample(String input, String output) {
            this.input = input;
            this.output = output;
            this.messages = null;
        }

        public DataSample(List<Message> messages) {
            this.input = null;
            this.output = null;
            this.messages = messages;
        }

        public String toJson() {
            if (messages != null) {
                StringBuilder sb = new StringBuilder("{\"messages\": [");
                for (int i = 0; i < messages.size(); i++) {
                    Message m = messages.get(i);
                    sb.append(String.format("{\"role\": \"%s\", \"content\": \"%s\"}",
                        m.role, escapeJson(m.content)));
                    if (i < messages.size() - 1) sb.append(", ");
                }
                sb.append("]}");
                return sb.toString();
            } else {
                return String.format("{\"prompt\": \"%s\", \"completion\": \"%s\"}",
                    escapeJson(input), escapeJson(output));
            }
        }

        private String escapeJson(String s) {
            return s.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n");
        }
    }

    /**
     * 메시지 클래스
     */
    public static class Message {
        public final String role;  // system, user, assistant
        public final String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    /**
     * 학습/테스트 분할 결과
     */
    public static class TrainTestSplit {
        public final List<DataSample> trainData;
        public final List<DataSample> testData;

        public TrainTestSplit(List<DataSample> trainData, List<DataSample> testData) {
            this.trainData = trainData;
            this.testData = testData;
        }
    }
}
