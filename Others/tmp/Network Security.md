# Network Security

## (1)

### 정보 보안 목표(CIA - Confidentiality, Integrity, Availability)

- Confidentiality(기밀성): 의도하지 않은 자원의 **노출** 방지
- Integrity(무결성): 의도하지 않은 자원의 **변경** 방지
- Availability(가용성): 원래 의도된 자원의 이용 범위 유지

  - 접근 권한이 없는 자의 접근 방지 등

- 인증(Authenticity):

  - 신원 인증(그 사람이 맞는지)
  - 메시지 인증(그 사람이 보낸 메시지인지)

- 책임(Accountability)

  - 부인 봉쇄
  - 법적 조치 등

- OSI 보안 구조

  - 보안 공격: 정보의 안전성 침해 행위
  - 보안 메커니즘: 보안 공격을 탐지, 예방, 침해 복구하는 기술 및 절차
  - 보안 서비스: 정보 전송과 데이터 처리 시스템의 보안 강화를 위한 처리 또는 통신 서비스

- 위협: 보안에 침해 및 위해를 가할 수 있는 환경, 능력, 행동 등의 event
- 공격: 시스템 보안에 대한 위협을 통한 침해

### 보안 공격

- Passive attack(소극적 공격)
  - 메시지 내용 갈취, 도청 등
  - 트래픽 분석
- Active attack(적극적 공격)
  - 전송 차단, 변조, 위조 등

### 보안 서비스

- 인증: 통신 개체가 그 당사자인지를 확인하는 것
  - 개체 인증, 데이터 출처 인증
- 접근 제어: 자원을 불법적으로 사용하지 못하도록 방지하는 것
- 데이터 무결성
  - 통신: 수신된 데이터와 보낸 것과의 일치성 확인
  - 저장: 저장된 데이터의 불법적 변경 여부 확인
- 부인 봉쇄: 송수신 사실의 부인 방지

---

## (2)

### 대칭키 암호 개요

![picture 3](../../images/TMP_NS_1.png)

- 위처럼 송수신자가 하나의 key를 공유해 사용한다.

- 대칭키 암호 필수 조건

  - 강한 암호화 알고리즘(해독도 어려워야 함)
  - 안전한 key 공유
    - Kerckhoff's principle: 알고리즘은 공개되어도 key가 없으면 해독이 불가해야 한다.
    - **key의 안전한 공유, 배분이 중요!**

- Brufe-force attack(전수 공격): 모든 key를 시도해서 key를 찾는 방법, key 길이가 길수록 소요 시간도 길다.
- Computational security(계산적 안전성)

  - 암호문을 깨는 데 드는 비용 \> 암호화된 정보의 가치
  - 암호문을 깨는 데 소요되는 시간 \> 해당 정보의 수명

- 암호 해독
  - 암호문만 알고 있는 공격
  - 알려진 평문 공격
  - 선택 평문 공격
  - 선택 암호문 공격
  - 선택문 공격

### 대칭 암호 알고리즘

- AES: 각 round에서 대체, 치환을 이용해 data block 전체 처리

  - Substitute bytes(바이드 대체)
  - Shift rows(행 이동)
  - Mix columns(열 섞기)
  - Add round key(round key 더하기)

- Block 크기는 **128bit로 고정**

  - Key길이(128bit, 192bit, 256bit)는 key scheduling에만 영향을 준다.

- 첫 번째 round에만 add round key 수행, 마지막 round에서는 mix columns 수행하지 않는다.

> XOR: XOR, `P`: Plaintext, `C`: Ciphertext, `K`: Key
>
> - `P` XOR `K` = `C`
> - `C` XOR `K` = `P`

### 암호 블록 운용 모드

- EBC(Electric Code Block)

  - 평문을 _b_ bit 크기의 block으로 분해
  - 각 block을 동일한 key로 암호화
  - 단점: 평문의 패턴이 암호문에 그대로 드러난다.

- CBC(Cipher Block Chaining)

  - 같은 평문에서 같은 암호문이 생성되지 않는다. -> 평문으로부터 암호문의 패턴을 분석할 수 없다.
  - Initial vector 사용
  - `P1` XOR `IV` XOR `K` = `C1`, `C1` XOR `P2` XOR `K` = `C2`, ...

  ![picture 4](../../images/TMP_NS_2.png)

- CFB(Cipher Feedback Mode)

  - 메시지의 크기를 block 크기(16bytes)에 맞추기 위해 padding을 붙여야하는 비효율성 보완
  - 암호화
    ![picture 5](../../images/TMP_NS_3.png)
  - 복호화
    ![picture 6](../../images/TMP_NS_4.png)
  - 전 단계의 결과물을 다음 단계에 사용하는 chaining으로 구성되어 있어 plaintext가 길다면 소요 시간이 길어짐.  
    이를 해결하기 위해 병렬로 수행할 수 있는 CTR 등이 등장하게 됨.

- CTR(Counter Mode)

  ![picture 7](../../images/TMP_NS_5.png)

### Stream 암호

- 계속 전송되는 평문의 bit stream과 key stream에서 key를 구해 암호문을 생성한다.

  - Key stream은 사전에 공유하고 있는 secret key로부터 생성된다. 즉 송수신자는 동일한 key stream을 사용한다.

  ![picture 8](../../images/TMP_NS_6.png)

### 난수, 의사 난수

- 난수(random number)

  - 다양한 암호화에 사용
  - key stream 생성 시 사용
  - 임시 session key 생성 등

- 난수의 특성
  - 무작위성: 균등 분포, 독립성
  - 예측 불가능성
