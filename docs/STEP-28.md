# STEP 28: Fine-tuning

모델을 직접 학습시켜 원하는 방식으로 동작하게 만듭니다.

---

## Fine-tuning이란?

기존 모델(Pre-trained Model)을 특정 목적에 맞게 추가 학습시키는 것입니다.

```
GPT-4 (범용) + 우리 데이터 → 우리 전용 모델
```

### 비유

```
기본 요리사 (Pre-trained)
    ↓ + 한식 레시피 학습
한식 전문 요리사 (Fine-tuned)
```

---

## 언제 Fine-tuning이 필요한가?

### RAG로 충분한 경우

- 최신 정보 제공
- 특정 문서 기반 Q&A
- 팩트 기반 답변

### Fine-tuning이 필요한 경우

- 특정 **스타일**로 답변 (말투, 형식)
- 특정 **행동** 학습 (항상 JSON으로 응답)
- **도메인 전문성** (의료, 법률 용어)
- **일관된 성격** (캐릭터 AI)

### 예시

```
# RAG로 충분
"이 계약서에서 위약금 조항을 찾아줘"

# Fine-tuning 필요
"모든 답변을 법률 문서 형식으로 작성해"
"항상 친근한 반말로 대화해"
```

---

## Fine-tuning 방식

### 1. Full Fine-tuning

모델 전체를 학습시킵니다.

```
장점: 최고 성능
단점: 비용 매우 높음, GPU 메모리 많이 필요
```

### 2. LoRA (Low-Rank Adaptation)

모델의 일부 파라미터만 학습시킵니다.

```
장점: 효율적, 적은 메모리
단점: Full보다 성능 약간 낮을 수 있음
```

### 3. QLoRA

LoRA + 양자화(Quantization)

```
장점: 더 적은 메모리 (일반 GPU에서 가능)
단점: 속도 약간 느림
```

대부분 **LoRA** 또는 **QLoRA**를 사용합니다.

---

## 데이터 준비

Fine-tuning의 핵심은 **좋은 데이터**입니다.

### 데이터 형식

```json
{"messages": [
    {"role": "system", "content": "당신은 친절한 고객 상담원입니다."},
    {"role": "user", "content": "배송이 언제 오나요?"},
    {"role": "assistant", "content": "주문번호를 알려주시면 확인해드릴게요! 😊"}
]}

{"messages": [
    {"role": "system", "content": "당신은 친절한 고객 상담원입니다."},
    {"role": "user", "content": "환불하고 싶어요"},
    {"role": "assistant", "content": "네, 환불 도와드릴게요. 주문번호와 사유를 말씀해주세요!"}
]}
```

### 데이터 양 가이드

| 목적 | 최소 데이터 | 권장 데이터 |
|------|-------------|-------------|
| 스타일 변경 | 50개 | 200-500개 |
| 새로운 작업 | 100개 | 500-1000개 |
| 도메인 전문성 | 500개 | 2000개+ |

### 데이터 품질 체크리스트

```
✅ 일관된 형식
✅ 정확한 정보
✅ 다양한 케이스
✅ 원하는 스타일 반영
❌ 중복 제거
❌ 오타/에러 수정
```

---

## OpenAI Fine-tuning

### 1. 데이터 업로드

```python
from openai import OpenAI
client = OpenAI()

# 파일 업로드
file = client.files.create(
    file=open("training_data.jsonl", "rb"),
    purpose="fine-tune"
)
```

### 2. Fine-tuning 작업 생성

```python
job = client.fine_tuning.jobs.create(
    training_file=file.id,
    model="gpt-4o-mini-2024-07-18",
    hyperparameters={
        "n_epochs": 3,
        "learning_rate_multiplier": 0.1
    }
)
```

### 3. 상태 확인

```python
# 진행 상황 확인
job = client.fine_tuning.jobs.retrieve(job.id)
print(job.status)  # "running", "succeeded", "failed"

# 이벤트 확인
events = client.fine_tuning.jobs.list_events(job.id)
for event in events:
    print(event.message)
```

### 4. 사용

```python
response = client.chat.completions.create(
    model="ft:gpt-4o-mini:my-org::abc123",  # Fine-tuned 모델 ID
    messages=[
        {"role": "user", "content": "안녕하세요"}
    ]
)
```

---

## LoRA로 직접 학습 (Ollama)

로컬에서 오픈소스 모델을 Fine-tuning합니다.

### 1. 환경 설정

```bash
# 필요 라이브러리
pip install transformers peft datasets accelerate bitsandbytes
```

### 2. 모델 로드

```python
from transformers import AutoModelForCausalLM, AutoTokenizer
from peft import LoraConfig, get_peft_model

# 베이스 모델 로드
model_name = "meta-llama/Llama-2-7b-hf"
model = AutoModelForCausalLM.from_pretrained(
    model_name,
    load_in_4bit=True,  # QLoRA
    device_map="auto"
)
tokenizer = AutoTokenizer.from_pretrained(model_name)
```

### 3. LoRA 설정

```python
lora_config = LoraConfig(
    r=16,               # LoRA rank
    lora_alpha=32,      # 스케일링
    target_modules=["q_proj", "v_proj"],  # 적용 레이어
    lora_dropout=0.05,
    bias="none",
    task_type="CAUSAL_LM"
)

model = get_peft_model(model, lora_config)
model.print_trainable_parameters()
# trainable params: 4,194,304 || all params: 6,742,609,920 || trainable%: 0.06%
```

### 4. 학습

```python
from transformers import TrainingArguments, Trainer

training_args = TrainingArguments(
    output_dir="./results",
    num_train_epochs=3,
    per_device_train_batch_size=4,
    learning_rate=2e-4,
    logging_steps=10,
    save_steps=100
)

trainer = Trainer(
    model=model,
    args=training_args,
    train_dataset=train_dataset
)

trainer.train()
```

### 5. 저장 및 사용

```python
# LoRA 어댑터 저장
model.save_pretrained("./my-lora-adapter")

# 나중에 로드
from peft import PeftModel
base_model = AutoModelForCausalLM.from_pretrained(model_name)
model = PeftModel.from_pretrained(base_model, "./my-lora-adapter")
```

---

## Fine-tuning 팁

### 1. 작게 시작

```
처음: 50개 데이터로 테스트
확인: 원하는 방향인지 검증
확장: 데이터 추가하며 개선
```

### 2. 평가 세트 분리

```python
# 80% 학습, 20% 평가
train_data, eval_data = dataset.train_test_split(test_size=0.2)
```

### 3. 과적합 주의

```
증상: 학습 데이터는 잘 맞지만 새 입력에 이상한 답변
해결: 에폭 줄이기, 데이터 다양화
```

### 4. 베이스 모델 선택

```
빠른 응답 필요 → 작은 모델 (7B)
높은 품질 필요 → 큰 모델 (70B)
다국어 → 다국어 지원 모델
```

---

## 비용 비교

| 방식 | 초기 비용 | 운영 비용 | 적합한 경우 |
|------|-----------|-----------|-------------|
| 프롬프트 엔지니어링 | 없음 | 토큰당 과금 | 빠른 실험 |
| RAG | Vector DB | 토큰당 과금 | 문서 기반 |
| OpenAI Fine-tuning | $15-100 | 토큰당 과금 (저렴) | 스타일 변경 |
| 자체 Fine-tuning | GPU 비용 | 호스팅 비용 | 완전 커스텀 |

---

## 핵심 정리

```
Fine-tuning = 기존 모델 + 추가 학습

언제 필요?
- 특정 스타일/형식
- 도메인 전문성
- 일관된 행동

어떻게?
- OpenAI: 쉬움, 비용 발생
- LoRA: 효율적, GPU 필요
- QLoRA: 메모리 절약
```

**핵심 포인트:**
- 좋은 데이터가 가장 중요
- 작게 시작해서 점진적 개선
- 프롬프트/RAG로 안 되면 Fine-tuning

---

## 다음 단계

다음 STEP에서는 멀티모달을 다룹니다. 텍스트뿐 아니라 이미지, 음성도 처리합니다.
