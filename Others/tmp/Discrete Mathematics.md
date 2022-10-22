# Discrete Mathematics

## 1.1 명제 논리

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

- $p \rightarrow q$에서 아래의 조건문을 만들어낼 수 있다.

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

## 1.2 명제 논리의 응용

### 문장을 논리로 변환하기

- 문장을 논리 표현으로 바꾸는 과정:

  - 복잡한 문장을 기본 문장들로 나누고, 명제 변수로 적절하게 표현한다.
  - 그리고 적절한 논리 연산자를 사용해 결합한다.

  - ex) "If I go to Harry's or to the country, I will not go shopping."

    - $p$: I go to Harry's.
    - $q$: I go to the country.
    - $r$: I go shopping.
    - 논리 표현: $(p \lor q) \rightarrow \lnot r$

  - ex) "You can access the internet from campus only if you are a computer science major or you are not a freshman."
    - $p$: You can access the internet from campus.
    - $q$: You are a computer science major.
    - $r$ : You are a freshman.
    - 논리 표현: $p \rightarrow (q \lor \lnot r)$

---

## 1.3 명제의 동치(propositional equivalences)

### 항진명제, 모순, 불확정명제

- 항진명제(tautology): 항상 T인 명제
  - ex) $p \lor \lnot p$
- 모순(contradiction): 항상 F인 명제
  - ex) $p \land \lnot p$
- 불확정 명제(contingency): T 또는 F인 명제

  | $p$ | $\lnot p$ | $p \lor \lnot p$ | $p \land \lnot p$ |
  | --- | --------- | ---------------- | ----------------- |
  | T   | F         | T                | F                 |
  | F   | T         | T                | F                 |

### 논리적 동치(logically equivalent)

- 두 복합명제 $p, q$에 대해 $p \leftrightarrow q$가 항진 명제이면,  
  $p$와 $q$는 **논리적 동치** 이며 $p \equiv q$로 표기한다.

- 두 명제가 동치임을 판정하는 방법 중 하나는 **진리표**를 이용하는 것이다.  
  아래의 진리표로 $\lnot p \lor q$와 $p \rightarrow q$가 동치임을 보여보자.

  | $p$ | $q$ | $\lnot p$ | $\lnot p \lor q$ | $p \rightarrow q$ |
  | --- | --- | --------- | ---------------- | ----------------- |
  | T   | T   | F         | T                | T                 |
  | T   | F   | F         | F                | F                 |
  | F   | T   | T         | T                | T                 |
  | F   | F   | T         | T                | T                 |

### 드 모르간의 법칙

- $\lnot(p \land q) \equiv \lnot p \lor \lnot q$
- $\lnot(p \lor q) \equiv \lnot p \land \lnot q$

  | $p$ | $q$ | $\lnot p$ | $\lnot q$ | $p \lor q$ | $\lnot(p \lor q)$ | $\lnot p \land \lnot q$ |
  | --- | --- | --------- | --------- | ---------- | ----------------- | ----------------------- |
  | T   | T   | F         | F         | T          | F                 | F                       |
  | T   | F   | F         | T         | T          | F                 | F                       |
  | F   | T   | T         | F         | T          | F                 | F                       |
  | F   | F   | T         | T         | F          | T                 | T                       |

### 논리적 동치

- Identity laws: $p \land T \equiv p$, $p \lor F \equiv p$
- Domination laws: $p \lor T \equiv T$, $p \land F \equiv F$
- Double negation law: $\lnot(\lnot p) \equiv p$
- Negation laws: $p \lor \lnot p \equiv T$, $p \land \lnot p \equiv F$

- Commutative laws(교환): $p \land q \equiv q \land p$, $p \lor q \equiv q \lor p$
- Associative laws(결합): $(p \land q) \land r \equiv p \land (q \land r)$, $(p \lor q) \lor r \equiv p \lor (q \lor r)$
- Distributive laws(분배): $p \land (q \lor r) \equiv (p \land q) \lor (p \land r)$, $p \lor (q \land r) \equiv (p \lor q) \land (p \lor r)$
- Absorption laws(흡수): $p \land (p \lor q) \equiv p$, $p \lor (p \land q) \equiv p$

### 조건문을 포함한 논리적 동치(중요!)

- $p \rightarrow q \equiv \lnot p \lor q$
- $p \rightarrow q \equiv \lnot q \rightarrow \lnot p$
- $p \leftrightarrow q \equiv (p \rightarrow q) \land (q \rightarrow p)$

### 새로운 논리적 동치 만들기

- ex) $\lnot(p \rightarrow q) \equiv p \land \lnot q$ 임을 보여라!

  - $\lnot(p \rightarrow q)$ = $\lnot(\lnot p \lor q)$ = 드모르간 = $\lnot(\lnot p) \land \lnot q$ = $p \land \lnot q$

- ex) $\lnot(p \lor (\lnot p \land q))$ 와 $\lnot p \land \lnot q$와 논리적 동치임을 보여라!

  - $\lnot(p \lor (\lnot p \land q))$ = $\lnot((p \lor \lnot p) \land (p \lor q))$ = $\lnot(T \land (p \lor q))$ = $\lnot(p \lor q)$ = $\lnot p \land \lnot q$

- ex) $(p \land q) \rightarrow (p \lor q)$ 가 tautology(항진 명제)임을 보여라.

  - $(p \land q) \rightarrow (p \lor q)$ = $\lnot(p \land q) \lor (p \lor q)$ = $(\lnot p \lor \lnot q) \lor (p \lor q)$ = $(\lnot p \lor p) \lor (\lnot q \lor q)$ = $T \lor T$ = $T$

---
