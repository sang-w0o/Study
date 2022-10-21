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

---

## (3)

### 메시지 인증

- Source authentication: 진짜 발신자로 표기된 그 사람이 보냈는가?
- Message integritiy: 그 사람이 보낸 메시지 내용이 변조되지 않았는가?

- 대칭 암호를 이용한 메시지 인증

  - Source authentication

    - 송수신자가 동일한 key를 **둘 사이에서만** 공유하고 있다는 전제
    - 오직 진짜 송신자만이 수신자에게 보내는 메시지를 성공적으로 암호화할 수 있다.

    ![picture 9](../../images/TMP_NS_7.png)

  - Message integrity
    - 암호문 변조 시 제대로된 복호화 불가
    - ECB를 사용하면 1개 block만 교체할 수도 있기에 CBC mode 등을 사용해야 한다.
      ![picture 10](../../images/TMP_NS_8.png)

### MAC(Message Authentication Code) - 메시지 인증 코드

- 무결성을 위해 사용
- $MAC_m = F(K_{AB}, M)$

  - $M$: 메시지
  - $F$: 인증 코드 생성 함수
  - $K_{AB}$: A와 B의 공유 키
  - ![picture 11](../../images/TMP_NS_9.png)

- 일방향 hash 함수
  - hash value = $H(m)$
    - $H()$ : hash function
    - $m$: 메시지
  - **key가 없으므로 source authentication, message integrity 검증 불가**

#### 일방향 hash function으로 MAC 구현하기

- 대칭 암호 사용

  - Hash value를 암호화해 전송

    ![picture 12](../../images/TMP_NS_10.png)

- 공개 키 암호 사용

  ![picture 13](../../images/TMP_NS_11.png)

- 비밀값 사용

  ![picture 14](../../images/TMP_NS_12.png)

### 안전한 hash function

- 안전한 hash function의 요건

  - (1) 임의 크기의 데이터 블록에 적용 가능
  - (2) 일정한 길이의 출력
  - (3) 계산의 용이성 및 구현 가능성
  - (4) **일방항 성질**
    - $h$에 대해 $H(x) = h$가 성립되는 $x$를 찾아내는 것이 계산적으로 불가능해야 한다.
    - $H()$가 일방향이 아니라면 공격자는 secret key를 찾아낼 수 있다.
  - (5) **약한 충돌 저항성**
    - 주어진 블록 $x$에 대해 $H(x) = H(y)$를 만족하는 $y \ne x$를 찾아 내는 것이 계산적으로 불가능해야 한다.
    - 즉 주어진 메시지의 hash value와 동일한 hash value를 갖는 다른 메시지를 찾을 수 없어야 한다.
  - (6) **강한 충돌 저항성**
    - $H(x) = H(y)$를 만족하는 쌍 (x, y)를 찾는 것이 계산적으로 불가능해야 한다.
    - 즉 동일한 hash value를 내는 서로 다른 2개의 값을 찾을 수 없어야 한다.
    - Collision을 이용한 공격: X, Y를 찾아냈다면 일종의 서명 문서 위조 가능
      ![picture 15](../../images/TMP_NS_13.png)

- $n$ bit hash code에 대한 공격 난이도

  - 일방향 성질: $2^n$
  - 약한 충돌 저항성: $2^n$
  - 강한 충돌 저항성: $2^{n/2}$

### SHA: Secure Hash Algorithm

- SHA-256: 입력은 512 bit, 출력은 256 bit
- SHA-512: 입력은 1024 bit, 출력은 512 bit

  ![picture 16](../../images/TMP_NS_14.png)

### HMAC

- 암호적 hash code를 이용한 MAC
- $MAC = HMAC(Key, Message)$
- HMAC의 특징(대칭 key 사용 MAC 대비)
  - 빠른 속도
  - 코드 획득의 용이성(암호적 hash function에 대한 코드를 쉽게 구현 가능)
  - 수출 규제 X

---

## (4)

### 공개 키 암호 개념

- plaintext, 암호 알고리즘, public key, private key, 암호문, 그리고 복호 알고리즘으로 구성
- 공개 키로 암호화하는 모습

  ![picture 17](../../images/TMP_NS_15.png)

- 개인 키로 암호화하는 모습(누구나 복호화 가능)

  ![picture 18](../../images/TMP_NS_16.png)

  - 암호화는 자신의 private key로, 복호화는 상대의 public key로 수행

- **암호화** : 수신자의 public key
- **복호화** : 수신자의 private key
  - 발신자의 public key로 암호화하면 누구나 public key로 복호화할 수 있다.
- 수신자의 public key로 복호화가 성공적으로 수행되면 그 사람의 private key로 암호화했음을 알 수 있다.
- **키 교환** : 암호화에 사용될 (대칭)키를 교환하는 과정
  - 키 일치(key agreement): DH(Diffie-Hellman) 알고리즘

### 공개 키 암호 요건

- 사용성

  - (1) B가 한 쌍의 key($PU_B$, $PR_B$)를 생성하는 것이 계산적으로 쉬워야 한다.
  - (2) Public key와 평문 $M$을 알고 있는 송신자 A는 암호문을 계산적으로 쉽게 구할 수 있어야 한다.  
    $C = E(PU_B, M)$
  - (3) 수신자 B가 암호문을 자신의 private key를 사용해 복호화하는 것이 계산적으로 쉬워야 한다.  
    $M = D(PR_B, C)$

- 보안성

  - (4) Public key $PU_B$를 알고 있는 공격자가 private key $PR_B$를 계산적으로 구하는 것이 불가능해야 한다.
  - (5) Public key $PU_B$와 암호문 $C$를 알고 있는 공격자가 원문 $M$을 계산적으로 구하는 것이 불가능해야 한다.

- RSA만 만족하는 요건: 2개의 key 중 어느 하나를 암호화에 사용하면, 다른 하나는 복호화에 사용할 수 있다.

  - $M = D[PU_b, E(PR_b, M)] = D[PR_b, E(PU_b, M)]$

### 공개 키 암호 알고리즘

#### RSA 알고리즘

- Public key($PU$): `{e, n}`
- Private key($PR$): `{d, n}`
- 공개 키로 암호화한다.
  - $C = M^e \mod n$
  - $M = C^d \mod n = (M^e)^d \mod n = M^{ed} \mod n$
- 알고리즘 요건

  - (1) $n$보다 작은 모든 정수 $M$에 대해 $M = M^{ed} \mod n$을 만족하는 값 $e, d, n$을 구할 수 있어야 한다.
  - (2) $n$보다 작은 모든 정수 $M$에 대해 $M^e$와 $C^d$를 구하는 것이 쉬워야 한다.
  - (3) $e, n$이 주어졌을 때 $d$를 구하는 것이 불가능해야 한다.

- 위 `e`, `n`, `d`를 구하기 위해 key 생성 알고리즘을 사용한다.

  ![picture 19](../../images/TMP_NS_17.png)

  - example:
    - (1) $p = 17, q = 11$이라 해보자.
    - (2) $n = pq = 17 \times 11 = 187$
    - (3) $\phi(n) = (p-1)(q-1) = 16 \times 10 = 160$
    - (4) $\phi(n)$과 서로소인 수 e를 선택. $e = 7$이라 해보자.
    - (5) $d < 160$이면서 $de \mod 160 = 1$인 수 $d$ 결정 -> $d = 23$이라 해보자.
    - (6) Public key: `{7, 187}`, Private key: `{23, 187}`

- 공격 방법

  - 수학적 공격

    - $n$은 공개되므로 $n$으로부터 $p, q$를 얻으면 공개된 $e$를 이용해 $d$를 쉽게 계산해낼 수 있다.
    - 방어 방법: n을 2048bit 이상의 큰 길이를 갖도록 한다.

  - 타이밍 공격

    - 복호화 알고리즘의 실행 시간, 전력 소모량 등을 관측해 $d$의 길이(값) 추정 => **부채널 공격**
    - 방어 방법: 랜덤으로 지체 시간을 넣는다.
    - **암호 알고리즘은 구현 상의 보안성도 중요하다!**

  - 선택 암호문 공격
    - 데이터 블록을 선택한 후 RSA의 약점을 이용한다.
    - 방어 방법: 평문에 padding 추가 (**최적 비대칭 암호화 padding(OAEP) 활용**)

#### Diffie-Hellman 키 교환

- 키 교환: 두 사용자가 메시지를 암호화하기 위한 대칭 key를 안전하게 교환하는 과정

- 이산대수 문제: $b = a^i \mod p, 0 \le i \le p - 1$에서 $b,a,p$가 주어졌을 때 $i$를 계산적으로 구할 수 없다.

- Diffie-Hellman 과정

  - 두 사용자 A, B가 있다고 하자. 그리고 $q, \alpha$가 주어진다.
  - (1) A는 $X_A < q$를 임의로 선택하고 $Y_A = \alpha^{X_A} \mod q$를 계산한다.
  - (2) B는 $X_B < q$를 임의로 선택하고 $Y_B = \alpha^{X_B} \mod q$를 계산한다.
  - (3) 양 측은 X를 private key로 보관하고 Y를 public key로 공개한다.

  ![picture 20](../../images/TMP_NS_18.png)

- 이후 A는 $K = (Y_B)^{X_A} \mod q$를 이용해 key를 계산하고, B는 $K = (Y_A)^{X_B} \mod q$를 이용해 key를 계산한다.

  ![picture 21](../../images/TMP_NS_19.png)

- 예시:

  ![picture 22](../../images/TMP_NS_20.png)

- 중간자 공격(MITM: Man In The Middle)

  ![picture 23](../../images/TMP_NS_21.png)
  ![picture 24](../../images/TMP_NS_22.png)

### 디지털 서명

- MAC은 메시지의 무결성, 소스 인증을 위해 사용한다.
- 부인 방지: 송신자가 메시지를 보낸 것을 부인하지 못하도록 하는 메커니즘
  - Secret key를 사용한 MAC(대칭 키 기반)은 송수신자가 이 key를 공유하기에 부인 방지가 불가하다.
- 전자서명: **공개 키 암호를 사용한 MAC**

  - 메시지의 무결성, 소스 인증, 부인 방지 모두 가능

  ![picture 25](../../images/TMP_NS_23.png)

---
