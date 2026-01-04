package com.aiprocess.step28;

import java.util.*;

/**
 * Fine-tuning 데모: 모델 미세조정 개념과 기법
 */
public class FineTuningDemo {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("STEP 28: Fine-tuning");
        System.out.println("========================================\n");

        // 1. Fine-tuning 개념
        System.out.println("[ 1. Fine-tuning이란? ]");
        System.out.println("-".repeat(40));
        System.out.println();
        System.out.println("Pre-trained Model → Fine-tuning → 특화 Model");
        System.out.println();
        System.out.println("사전학습 (Pre-training):");
        System.out.println("  - 대량의 텍스트로 일반적인 언어 이해 학습");
        System.out.println("  - GPT: 수십억 개의 웹 문서");
        System.out.println();
        System.out.println("미세조정 (Fine-tuning):");
        System.out.println("  - 특정 작업/도메인에 맞게 추가 학습");
        System.out.println("  - 적은 데이터로 높은 성능 달성");

        // 2. 학습 데이터 준비
        System.out.println("\n[ 2. 학습 데이터 준비 ]");
        System.out.println("-".repeat(40));

        TrainingData data = new TrainingData();

        // 고객 서비스 챗봇용 데이터
        data.addConversation(Arrays.asList(
            new TrainingData.Message("system", "당신은 친절한 고객 서비스 담당자입니다."),
            new TrainingData.Message("user", "환불 어떻게 하나요?"),
            new TrainingData.Message("assistant", "환불 절차를 안내해 드리겠습니다. 주문번호를 알려주시면 바로 처리해 드릴게요.")
        ));

        data.addConversation(Arrays.asList(
            new TrainingData.Message("system", "당신은 친절한 고객 서비스 담당자입니다."),
            new TrainingData.Message("user", "배송 언제 오나요?"),
            new TrainingData.Message("assistant", "배송 조회를 도와드리겠습니다. 주문번호나 송장번호를 알려주시겠어요?")
        ));

        data.addConversation(Arrays.asList(
            new TrainingData.Message("system", "당신은 친절한 고객 서비스 담당자입니다."),
            new TrainingData.Message("user", "제품이 불량이에요"),
            new TrainingData.Message("assistant", "불편을 드려 정말 죄송합니다. 교환 또는 환불 처리를 도와드리겠습니다. 사진을 보내주시겠어요?")
        ));

        // 더 많은 샘플 추가 (시뮬레이션)
        for (int i = 0; i < 7; i++) {
            data.addSample("질문 " + i, "답변 " + i);
        }

        System.out.printf("준비된 학습 데이터: %d개%n", data.size());

        // JSONL 형식 미리보기
        System.out.println("\nJSONL 형식 (OpenAI Fine-tuning 형식):");
        System.out.println("-".repeat(40));
        String jsonl = data.toJsonl();
        String[] lines = jsonl.split("\n");
        for (int i = 0; i < Math.min(2, lines.length); i++) {
            String line = lines[i];
            if (line.length() > 80) {
                line = line.substring(0, 80) + "...";
            }
            System.out.println(line);
        }

        // 3. 데이터 분할
        System.out.println("\n[ 3. 학습/검증 데이터 분할 ]");
        System.out.println("-".repeat(40));

        TrainingData.TrainTestSplit split = data.split(0.8);
        System.out.printf("학습 데이터: %d개 (80%%)%n", split.trainData.size());
        System.out.printf("검증 데이터: %d개 (20%%)%n", split.testData.size());

        // 4. LoRA (Low-Rank Adaptation)
        System.out.println("\n[ 4. LoRA (Low-Rank Adaptation) ]");
        System.out.println("-".repeat(40));
        System.out.println();
        System.out.println("문제: 대형 모델의 전체 파라미터를 학습하면");
        System.out.println("  - 비용이 너무 많이 듦 (GPU 메모리)");
        System.out.println("  - 시간이 오래 걸림");
        System.out.println();
        System.out.println("해결: LoRA - 저랭크 행렬만 학습!");
        System.out.println();
        System.out.println("┌─────────────────────────────────────────┐");
        System.out.println("│  W' = W + ΔW = W + (B × A)             │");
        System.out.println("│                                        │");
        System.out.println("│  W: [d × d] 원본 가중치 (고정)           │");
        System.out.println("│  A: [d × r] 저랭크 행렬 (학습)           │");
        System.out.println("│  B: [r × d] 저랭크 행렬 (학습)           │");
        System.out.println("│  r << d (예: r=8, d=4096)              │");
        System.out.println("└─────────────────────────────────────────┘");

        // LoRA 시뮬레이션
        System.out.println("\nLoRA 시뮬레이션:");
        LoRASimulator lora = new LoRASimulator(512, 8, 16.0f);
        lora.showParameterComparison();

        System.out.println();
        lora.train(split.trainData, 3, 0.001f);

        // 5. QLoRA
        System.out.println("\n[ 5. QLoRA (Quantized LoRA) ]");
        System.out.println("-".repeat(40));
        System.out.println();
        System.out.println("QLoRA = LoRA + 4비트 양자화");
        System.out.println();
        System.out.println("메모리 사용량 비교 (LLaMA 7B 기준):");
        System.out.println("  - 전체 Fine-tuning: ~56GB (fp16)");
        System.out.println("  - LoRA: ~14GB (fp16)");
        System.out.println("  - QLoRA: ~4GB (4bit + LoRA)");
        System.out.println();
        System.out.println("→ 일반 GPU에서도 대형 모델 Fine-tuning 가능!");

        // 6. Fine-tuning 방법 비교
        System.out.println("\n[ 6. Fine-tuning 방법 비교 ]");
        System.out.println("-".repeat(40));
        System.out.println();
        System.out.println("┌──────────────┬────────────┬───────────┬───────────┐");
        System.out.println("│ 방법         │ 파라미터   │ 메모리    │ 성능      │");
        System.out.println("├──────────────┼────────────┼───────────┼───────────┤");
        System.out.println("│ Full FT      │ 100%       │ 매우 높음 │ 최고      │");
        System.out.println("│ LoRA         │ ~1%        │ 낮음      │ 좋음      │");
        System.out.println("│ QLoRA        │ ~1%        │ 매우 낮음 │ 좋음      │");
        System.out.println("│ Adapter      │ ~2-5%      │ 중간      │ 좋음      │");
        System.out.println("│ Prompt Tuning│ <0.1%      │ 매우 낮음 │ 보통      │");
        System.out.println("└──────────────┴────────────┴───────────┴───────────┘");

        // 7. OpenAI Fine-tuning API
        System.out.println("\n[ 7. OpenAI Fine-tuning API ]");
        System.out.println("-".repeat(40));
        System.out.println();
        System.out.println("# 1. 데이터 업로드");
        System.out.println("openai api files.create -f training_data.jsonl");
        System.out.println();
        System.out.println("# 2. Fine-tuning 작업 생성");
        System.out.println("openai api fine_tuning.jobs.create \\");
        System.out.println("  -m gpt-3.5-turbo \\");
        System.out.println("  -t file-xxx");
        System.out.println();
        System.out.println("# 3. 모델 사용");
        System.out.println("model = \"ft:gpt-3.5-turbo:org:custom-name\"");

        // 8. 권장 데이터 수
        System.out.println("\n[ 8. Fine-tuning 팁 ]");
        System.out.println("-".repeat(40));
        System.out.println();
        System.out.println("데이터 수 가이드:");
        System.out.println("  - 최소: 50~100개 (기본 성능)");
        System.out.println("  - 권장: 500~1000개 (좋은 성능)");
        System.out.println("  - 이상적: 10,000개+ (최적 성능)");
        System.out.println();
        System.out.println("데이터 품질 체크리스트:");
        System.out.println("  ✓ 일관된 형식");
        System.out.println("  ✓ 다양한 예시");
        System.out.println("  ✓ 정확한 레이블");
        System.out.println("  ✓ 대표적인 케이스");
        System.out.println();
        System.out.println("주의사항:");
        System.out.println("  - 과적합(Overfitting) 주의");
        System.out.println("  - 검증 데이터로 성능 확인");
        System.out.println("  - 기본 모델 능력 유지 확인");

        // 요약
        System.out.println("\n========================================");
        System.out.println("핵심 정리");
        System.out.println("========================================");
        System.out.println("1. Fine-tuning: 사전학습 모델을 특정 작업에 맞게 조정");
        System.out.println("2. LoRA: 저랭크 행렬만 학습 → 효율적");
        System.out.println("3. QLoRA: LoRA + 4비트 양자화 → 더 효율적");
        System.out.println("4. 데이터 품질 > 데이터 양");
        System.out.println("5. OpenAI, HuggingFace 등에서 API 제공");
    }
}
