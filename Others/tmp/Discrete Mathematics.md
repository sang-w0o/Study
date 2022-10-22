# Discrete Mathematics

## (1)

### 명제(proposition)

- 명제: 참 또는 거짓 중 하나를 나타내는 문장이나 식

  - ex) 대한민국의 수도는 서울이다.
  - ex) `1 + 0 = 3`
  - 명제가 아닌 예시: 몇 시 입니까?

- 복합 명제(compound proposition): 논리 연산자를 이용해 기존의 명제들로 만든다.

  - 부정(negation): $\neg$
  - 논리곱(conjunction): $\land$
  - 논리합(disjuntion): $\lor$
  - 조건문(implication): $\rightarrow$
  - 상호 조건문(biconditional): $\leftrightarrow$
  - 논리곱(XOR): $\oplus$
    - 논리곱은 p, q 중 단 1개일 때만 T이고, 나머지는 모두 F이다.

- 부정(negation): $p$의 부정은 $\neg p$로 표현되고 아래의 진리표를 가진다.

  | $p$ | $\neg p$ |
  | --- | -------- |
  | T   | F        |
  | F   | T        |

  - 예시: $p$가 "지구는 둥글다"일 때 $\neg p$는 "지구는 둥글지 않다." 이다.

- 논리곱(conjunction): $p$와 $q$의 논리 곱은 $p \land q$로 표현되고 아래의 진리표를 가진다.

  | $p$ | $q$ | $p \land q$ |
  | --- | --- | ----------- |
  | T   | T   | T           |
  | T   | F   | F           |
  | F   | T   | F           |
  | F   | F   | F           |

  - 논리곱은 "AND"이다.
  - 예시: $p$가 "나는 집에 있다.", $q$가 "비가 온다" 일 때 $p \land q$는 "나는 집에 있고 비가 온다." 이다.

- 논리합(disjunction): $p$와 $q$의 논리합은 $p \lor q$로 표현하며 아래의 진리표를 가진다.

  | $p$ | $q$ | $p \lor q$ |
  | --- | --- | ---------- |
  | T   | T   | T          |
  | T   | F   | T          |
  | F   | T   | T          |
  | F   | F   | F          |

  - 논리합은 "OR"이다.
  - 예시: $p$가 "나는 집에 있다.", $q$가 "비가 온다" 일 때 $p \lor q$는 "나는 집에 있거나 비가 온다." 이다.

- 배타적 논리합(XOR): $p \oplus q$는 $p$, $q$ 중 어느 하나만이 T일때 T가 된다.

  | $p$ | $q$ | $p \oplus q$ |
  | --- | --- | ------------ |
  | T   | T   | F            |
  | T   | F   | T            |
  | F   | T   | T            |
  | F   | F   | F            |

- 조건문(implication): $p, q$에 대해 $p \rightarrow q$는 _"if p then q"_ 이다.

  | $p$ | $q$ | $p \rightarrow q$ |
  | --- | --- | ----------------- |
  | T   | T   | T                 |
  | T   | F   | F                 |
  | F   | T   | T                 |
  | F   | F   | T                 |

  - $p$가 T일 때 $q$가 F인 경우만 F가 된다.

  - 조건문의 진리값에 대한 이해

    - "If the moon is made of green cheese, then I have more money than Bill Gates."
      - $p \rightarrow q$에서 p가 F이고 q도 F이므로 T이다.
    - "If 1 + 1 = 3, then 2 + 3 = 5."
      - $p \rightarrow q$에서 p가 F이고 q가 T이므로 T이다.
    - "If I am elected, then I will lower taxes."
      - $p \rightarrow q$에서 이는 $T \rightarrow T$ 또는 $F \rightarrow F$ 일 때는 T, $T \rightarrow F$ 일 때는 F이다.
    - "If you get 100% on final, then you will get an A."
      - $p \rightarrow q$에서 이는 $T \rightarrow T$ 또는 $F \rightarrow F$ 일 때는 T, $F \rightarrow F$ 일 때는 T이다.

- $p \rightarrow q$를 나타내는 여러 가지 표현

  - if $p$, then $q$
  - $p$ implies $q$
  - $p$ only if $q$
  - A neccessary condition for $p$ is $q$ (필요 조건)
  - A sufficient condition for $p$ is $q$ (충분 조건)

### 역(converse), 이(inverse), 대우(contrapositive)

- $p$ \rightarrow q$에서 아래의 조건문을 만들어낼 수 있다.

  - $q \rightarrow p$ (역): is the **converse** of $p \rightarrow q$.
  - $\lnot p \rightarrow \lnot q$ (이): is the **inverse** of $p \rightarrow q$.
  - $\lnot q \rightarrow \lnot p$ (대우): is the **contrapositive** of $p \rightarrow q$.

- ex) "If it is raining, then I do not go to town."
  - Converse: "If I do not go to town, then it is not raining."
  - Inverse: "If it is not raining, then I go to town."
  - Contrapositive: "If I go to town, then it is not raining."

### 상호 조건문(biconditional)

- $p, q$에 대해 $p \leftrightarrow q$는 _"p if and only if q"_ 이다.  
  그리고 $p \leftrightarrow q$는 $(p \rightarrow q) \land (q \rightarrow p)$와 동일하다.

  | $p$ | $q$ | $p \leftrightarrow q$ |
  | --- | --- | --------------------- |
  | T   | T   | T                     |
  | T   | F   | F                     |
  | F   | T   | F                     |
  | F   | F   | T                     |

  - $p \leftrightarrow q$는 $p \rightarrow q$와 $q \rightarrow p$의 논리곱이다.
  - 예시: $p$가 "I am at home"이고 $q$가 "It is raining"이면 $p \leftrightarrow q$는 "I am at home if and only if it is raining"이다.

### 복합 명제의 진리표

- $(p \lor q) \rightarrow \lnot r$의 진리표

  | $p$ | $q$ | $r$ | $\lnot r$ | $p \lor q$ | $(p \lor q) \rightarrow \lnot r$ |
  | --- | --- | --- | --------- | ---------- | -------------------------------- |
  | T   | T   | T   | F         | T          | F                                |
  | T   | T   | F   | T         | T          | T                                |
  | T   | F   | T   | F         | T          | F                                |
  | T   | F   | F   | T         | T          | T                                |
  | F   | T   | T   | F         | T          | F                                |
  | F   | T   | F   | T         | T          | T                                |
  | F   | F   | T   | F         | F          | T                                |
  | F   | F   | F   | T         | F          | T                                |

### 동치 명제(equivalent propositions)

- 항상 동일한 진리값을 가지는 두 명제를 동치 명제라 한다.

- ex) 조건문이 대우와 동치 관계임을 진리표를 사용해 보여라.

  | $p$ | $q$ | $\lnot p$ | $\lnot q$ | $p \rightarrow q$ | $\lnot q \rightarrow \lnot p$ |
  | --- | --- | --------- | --------- | ----------------- | ----------------------------- |
  | T   | T   | F         | F         | T                 | T                             |
  | T   | F   | F         | T         | F                 | F                             |
  | F   | T   | T         | F         | T                 | T                             |
  | F   | F   | T         | T         | T                 | T                             |

- ex) 진리표를 사용해 조건문의 역, 이가 모두 조건문과 동치 관계가 아님을 보여라.

  | $p$ | $q$ | $\lnot p$ | $\lnot q$ | $p \rightarrow q$ | $\lnot p \rightarrow \lnot q$ | $q \rightarrow p$ |
  | --- | --- | --------- | --------- | ----------------- | ----------------------------- | ----------------- |
  | T   | T   | F         | F         | T                 | T                             | T                 |
  | T   | F   | F         | T         | F                 | T                             | T                 |
  | F   | T   | T         | F         | T                 | F                             | F                 |
  | F   | F   | T         | T         | T                 | T                             | T                 |

---
