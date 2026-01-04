package com.aiprocess.step28;

import java.util.*;

/**
 * LoRA (Low-Rank Adaptation) 시뮬레이션
 *
 * 실제 LoRA는 대형 모델의 일부 가중치만 학습합니다.
 * 여기서는 개념적인 시뮬레이션만 제공합니다.
 */
public class LoRASimulator {

    private final int originalDim;  // 원본 모델 차원
    private final int loraRank;     // LoRA 랭크 (보통 4~64)
    private final float alpha;      // 스케일링 팩터

    // LoRA 행렬 (시뮬레이션)
    private float[][] loraA;  // [original_dim x rank]
    private float[][] loraB;  // [rank x original_dim]

    public LoRASimulator(int originalDim, int loraRank, float alpha) {
        this.originalDim = originalDim;
        this.loraRank = loraRank;
        this.alpha = alpha;

        initializeLoRA();
    }

    /**
     * LoRA 행렬 초기화
     */
    private void initializeLoRA() {
        Random rand = new Random(42);

        // A 행렬: 가우시안 초기화
        loraA = new float[originalDim][loraRank];
        for (int i = 0; i < originalDim; i++) {
            for (int j = 0; j < loraRank; j++) {
                loraA[i][j] = (float) rand.nextGaussian() * 0.01f;
            }
        }

        // B 행렬: 0으로 초기화 (학습 시작 시 원본 모델과 동일)
        loraB = new float[loraRank][originalDim];
    }

    /**
     * 학습 시뮬레이션
     */
    public void train(List<TrainingData.DataSample> samples, int epochs, float learningRate) {
        System.out.println("LoRA 학습 시작...");
        System.out.printf("  원본 차원: %d, LoRA 랭크: %d%n", originalDim, loraRank);
        System.out.printf("  학습 샘플: %d개, Epochs: %d%n", samples.size(), epochs);
        System.out.println();

        Random rand = new Random();

        for (int epoch = 0; epoch < epochs; epoch++) {
            float totalLoss = 0;

            for (TrainingData.DataSample sample : samples) {
                // 시뮬레이션: 랜덤 손실 감소
                float loss = 1.0f / (epoch + 1) + rand.nextFloat() * 0.1f;
                totalLoss += loss;

                // LoRA 가중치 업데이트 (시뮬레이션)
                for (int i = 0; i < loraRank; i++) {
                    for (int j = 0; j < originalDim; j++) {
                        loraB[i][j] += learningRate * rand.nextGaussian() * 0.001f;
                    }
                }
            }

            float avgLoss = totalLoss / samples.size();
            System.out.printf("Epoch %d/%d - Loss: %.4f%n", epoch + 1, epochs, avgLoss);
        }

        System.out.println("\n학습 완료!");
    }

    /**
     * 파라미터 수 비교
     */
    public void showParameterComparison() {
        long fullParams = (long) originalDim * originalDim;
        long loraParams = (long) originalDim * loraRank * 2;

        System.out.println("파라미터 수 비교:");
        System.out.printf("  전체 Fine-tuning: %,d 파라미터%n", fullParams);
        System.out.printf("  LoRA Fine-tuning: %,d 파라미터%n", loraParams);
        System.out.printf("  절감률: %.2f%%%n", (1 - (double) loraParams / fullParams) * 100);
    }

    /**
     * LoRA 적용 (시뮬레이션)
     */
    public float[] apply(float[] input) {
        // delta = (B * A) * input
        // output = original_output + alpha/rank * delta

        float[] intermediate = new float[loraRank];
        for (int i = 0; i < loraRank; i++) {
            for (int j = 0; j < originalDim; j++) {
                intermediate[i] += loraA[j][i] * input[j];
            }
        }

        float[] delta = new float[originalDim];
        for (int i = 0; i < originalDim; i++) {
            for (int j = 0; j < loraRank; j++) {
                delta[i] += loraB[j][i] * intermediate[j];
            }
            delta[i] *= alpha / loraRank;
        }

        // 원본 + delta
        float[] output = new float[originalDim];
        for (int i = 0; i < originalDim; i++) {
            output[i] = input[i] + delta[i];
        }

        return output;
    }

    public int getLoraRank() {
        return loraRank;
    }

    public int getOriginalDim() {
        return originalDim;
    }
}
