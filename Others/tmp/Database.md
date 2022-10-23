# Database

## 1. 트랜잭션 이론

### 1.1 트랜잭션 개념

- 트랜잭션: 하나의 논리적 작업을 수행하는 데이터베이스 연산의 순서(sequence)

- 트랜잭션의 ACID Property

  - Atomicity(원자성): 트랜잭션의 연산은 모두 성공하거나 모두 실패해야 한다.
  - Consistency(일관성): 단일 트랜잭션의 수행은 데이터의 무결성을 유지해야 한다.
  - Isolation(고립성): 동시에 트랜잭션들이 수행되더라도 각 트랜잭션은 다른 트랜잭션의 연산에 끼어들 수 없다.
  - Durability(지속성): 트랜잭션이 성공적으로 완료되면 그 결과는 시스템 장애가 발생하더라도 데이터베이스 상태에 반영되어야 한다.

### 1.2 직렬 가능(serializability)

- Schedule: 동시적으로 수행되는 다수의 트랜잭션에 속하는 연산이 수행된 시간적 순서

- Serial schedule

![picture 73](../../images/TMP_DB_1.png)

- 아래 schedule은 serial schedule은 아니지만 위의 schedule과 동일한 결과를 보인다.

![picture 74](../../images/TMP_DB_2.png)

- Serializable(직렬 가능) schedule: Schedule의 결과가 serial schedule의 결과와 동일한 것.

- Conflicting instructions(충돌 연산): 동일한 데이터에 대해 서로 다른 트랜잭션 중 하나라도 write를 하면 conflict하다고 한다.  
  Conflict한 연산은 순서를 바꾸면 결과가 달라지지만 conflict하지 않은 연산은 순서를 바꾸어도 결과가 같다.(ex. read-read)

- Conflict equivalent: Schedule S에 대해 non-conflicting instructions의 순서를 바꾸어도 결과가 같은 schedule S'가 있다면,  
  S와 S'가 conflict equivalent라고 한다.
- Conflict serializable schedule: Schedule S가 serial schedule과 conflict equivalent하다면, S는 conflict serializable schedule이다.

![picture 75](../../images/TMP_DB_3.png)

- 아래 schedule은 conflict serializable하지 않은 schedule이다.
  ![picture 76](../../images/TMP_DB_4.png)

- View Serializability: 2개 schedule S, S'가 있다고 해보자. 이 둘은 아래 조건들을 모두 만족해야 view serializable하다.

  - S가 item Q의 초기 값을 read하면 S'도 item Q의 초기 값을 read한다.
  - S가 item S에 대해 final write를 하면 S'도 item Q에 대해 final write를 한다.

- 아래 예시 schedule은 view serializable하지만 conflict serializable하지는 않다.

  ![picture 77](../../images/TMP_DB_5.png)

  - 위 schedule은 <$T_5$, $T_6$, $T_7$> 형태의 serial schedule과 view equivalent하다.
    - 둘 다 $T_5$가 initial value read, $T_7$가 final value write하기 때문.

- 아래 그림처럼 conflict serializable은 view serializable의 부분집합이다.

![picture 78](../../images/TMP_DB_6.png)

### 1.3 직렬 가능 시험

- Precedence graph:

  - Vertex가 transaction들인 direct graph.
  - Conflict 관계인 두 트랜잭션 $T_i$, $T_j$에 대해 먼저 연산을 수행하는 쪽에서 나머지 쪽으로 directed edge를 그린다.

  ![picture 79](../../images/TMP_DB_7.png)

- 이렇게 그려진 precedence graph가 **acyclic 하면 해당 schedule은 conflict serializable하다.**

### 1.4 회복 가능

- 동시에 수행되는 트랜잭션들 중 일부가 실패했을 때 회복할 수 있어야 한다.

- Recovarable schedule: $T_i$에 의해 write된 item을 $T_j$가 읽는다면, $T_i$의 commit이 $T_j$의 commit보다 먼저 일어나야 한다.  
  아래 표는 recovarable하지 않은 schedule이다.
  ![picture 80](../../images/TMP_DB_8.png)

- Cascading rollbacks: 트랜잭션 하나의 실패가 수많은 트랜잭션의 rollback을 일으키는 것.  
  아래 schedule에서 그 어떤 tx도 commit하지 않았는데, 만약 $T_10$이 실패하면 $T_11, T_12$도 rollback되어야 한다.
  ![picture 81](../../images/TMP_DB_9.png)

  - 참고로 위 schedule은 recoverable하다.

- DBMS는 schedule이 항상 recoverable하고, cascading rollback이 발생하지 않도록 해야 한다.  
  이것이 cascadeless schedule이다.

- Cascadeless schedule(ACA schedules: Avoid Cascading Aborts):

  - Cascading rollback이 발생하지 않는다.
  - $T_i$에 의해 write된 item을 $T_j$가 읽는다면, $T_i$의 commit이 $T_j$의 가 해당 item을 읽는 시점보다 먼저 일어나야 한다.

![picture 82](../../images/TMP_DB_10.png)

- RC: Recovarable
- ACA: Avoiding Cascading Aborts
- ST: Strict
- SR: Conflict Serializable

- 이렇게 schedule을 conflict serializable 또는 view serializable하게 하도록, 그리고 recoverable하면서  
  cascadeless 하도록 하기 위해 DBMS는 concurrency control protocol을 사용한다.
  > Concurrency control protocol은 precedence graph를 바로 만들어 cycle 여부를 관측하지 않는다.

---

## 2. 동시성 제어(Concurrency Control)

### 2.1 Lock-based protocols

- X lock: read, write 모두 가능
- S lock: read만 가능

- 여러 개의 트랜잭션이 S-lock은 동시에 획득할 수 있는 반면, X-lock은 단 하나의 트랜잭션만 획득할 수 있다.  
  다른 트랜잭션은 이미 X-lock을 획득한 tx가 해당 lock을 release해야만 X-lock을 획득할 수 있다.

- Lock을 획득하기 위해 트랜잭션은 필요한 경우 대기(wait)한다.  
  이러한 locking은 lock manager로 구현되는데, lock manager는 lock table이라는 자료구조를 관리해  
  부여한 lock과 lock을 획득하기 기다리는 tx들의 요청을 처리한다.

- 대략적인 lock 흐름

  - (1) lock(A)
  - (2) read(A)
  - (3) unlock(A)

- 2PL(Two-Phase Locking Protocol)

  - Phase 1: Growing phase

    - Lock 획득만 가능. release는 불가
    - S-lock을 X-lock으로 _upgrade_ 할 수 있다.

  - Phase 2: Shrinking phase

    - Lock release만 가능. 획득은 불가
    - X-lock을 S-lock으로 _downgrade_ 할 수 있다.

  - 2PL은 conflict serializable한 schedule만 생성한다.
  - Cascading rollback은 가능하다.

    - 이를 막기 위해 아래의 2개 2PL이 더 있다.
      - Strict 2PL: 트랜잭션이 commit 또는 abort할 때까지 X-lock을 release하지 못하게 한다.
      - Rigorous 2PL: X-lock 뿐만아니라 S-lock까지 트랜잭션이 commit 또는 abort할 때까지 release하지 못하게 한다.

  - 2PL을 써도 deadlock은 발생할 수 있다.

- Deadlock(피할 수 없다. 어떻게 해도 발생하긴 하지만 매우 낮은 확률로 발생한다.)

  ![picture 83](../../images/TMP_DB_11.png)

  - 위 schedule에서 $T_4$는 Lock-S(B)를 획득하기 위해 $T_3$를 기다리고, $T_3$는 Lock-X(B)를 획득하기 위해 $T_4$를 기다린다.  
    이렇게 되면 무한정 기다리게 되는데, 이를 deadlock이라고 한다.

- Starvation(기아 상태): 특정 tx가 lock을 획득하지 못하고 필요 이상으로 lock을 기다리게 되는 현상

- Graph-based Protocol

  - Lock을 걸려고 하는 데이터가 부분 순서가 있어야 한다는 가정이 필요하다.
  - Conflict serializable한 schedule만 생성하고, deadlock을 발생시키지 않음을 보장한다.
  - 2PL보다 unlock이 빠르게 발생할 수 있어서 대기 시간이 줄어들게 되고, 결과적으로 동시성이 향상된다.
  - Deadlock이 발생하지 않고 rollback도 필요 없다.

  - 단점:
    - Recoverable함과 cascading rollback이 없음을 보장하지 못한다.
    - Tx가 실제로 접근하지 않는 item에 대해서도 lock을 걸어야 할 수도 있다.

- Tree-based Protocol

  - Graph-based protocol의 하나.
  - X-lock만 허용한다.
  - $T_i$는 처음에 어떠한 item에도 X-lock을 걸 수 있다.
  - 이후에 $T_i$가 또 X-lock을 획득하려면, X-lock을 획득하려는 item의 부모 노드의 X-lock을 이미 $T_i$가 소유하고 있어야만 한다.
  - Unlock은 언제든지 가능하다.
  - $T_i$가 lock, unlock을 한 번 수행한 item에 대해 $T_i$는 다시 lock을 걸 수 없다.

  ![picture 84](../../images/TMP_DB_12.png)

- MGL(Multiple Granularity Locking)

  - 데이터의 계층 구조를 만들어 tree처럼 사용한다.
  - Tx가 하나의 node에 대해 lock을 걸면, 해당 node의 모든 자식 node들까지 암시적으로 동일한 lock mode의 lock이 걸린다.

  - Intention lock: S, X-lock에 더해 3개의 추가적인 lock mode가 존재한다.

    - IS(Intention-Shared): IS를 거는 node의 자식 node 중 하나를 read할 때
    - IX(Intension-Exclusive): IX를 거는 node의 자식 node 중 하나를 write할 때
    - SIX(Shared + Intension-Exclusive): SIX를 거는 node에 명시적으로 S-lock을 걸고 자식 node 중 하나를 write할 때

  - Intention lock의 호환성

    ![picture 85](../../images/TMP_DB_13.png)

    - X는 다 호환 안됨.
    - SIX는 IS이랑만 호환됨.
    - IX는 IS, IX이랑만 호환됨.

  - MGL의 locking 방식

    - tx는 이전에 unlock하지 않은 node일 때만 lock 획득 가능.
    - Lock의 획득: root-to-leaf order
    - Lock의 해제: leaf-to-root order

  - 예시

    ![picture 86](../../images/TMP_DB_14.png)
    ![picture 87](../../images/TMP_DB_15.png)
    ![picture 88](../../images/TMP_DB_16.png)

### 2.3 Deadlock

- Deadlock handling

  - Timeout-based scheme: Tx는 lock을 획득하기 위해 지정된 시간만큼 wait한다. 해당 시간이 timeout되면 tx가 rollback된다.  
    (no deadlocks)
  - Deadlock prevention protocol을 사용해 deadlock을 발생하지 않도록 한다.(ex. Graph-based protocol)
  - Tx의 timestamp를 활용하는 방식들
    - Wait-die:
      - Older tx는 younger tx가 lock을 release하기를 기다린다.
      - 반대로 younger tx는 older tx를 절대 기다리지 않고 바로 rollback된다.
    - Wound-wait:
      - Older tx는 younger tx를 기다리는 대신 younger tx를 강제로 rollback한다.
      - Younger tx는 older tx가 lock을 release하기를 기다린다.
    - Wait-die, Wound-wait 모두 rollback된 tx는 기존의 timestamp를 갖고 재시작된다.

- Deadlock detection

  - Wait-for graph를 사용한다.
    - G = (V, E)
      - V: Tx의 집합, $T_i$에서 $T_j$로의 화살표는 $T_i$가 lock 획득을 위해 $T_j$를 기다리고 있음을 의미한다.
  - 이 Wait-for graph가 cyclic하면 deadlock에 빠진 것이다.
  - 이렇게 deadlock이 발견되면 아래 2개 중 하나의 기준으로 tx를 rollback해야 한다.
    - rollback 비용이 가장 적은 tx를 rollback
    - total rollback: tx abort후 재시작
    - partial rollback: deadlock을 해결할 수 있을 만큼만 rollback

### 2.4 입력 및 삭제 연산

- Delete 연산: 삭제될 item에 대해 X-lock을 획득한 후 수행해야 한다.
- Insert 연산: 삽입한 item에 대해 tx가 X-lock을 획득하게 된다.
- Insert, delete 연산은 phantom 현상이 발생할 수 있다.

- Phantom 현상: tuple locking을 수행해 서로 다른 tx들이 tuple의 입력 또는 삭제 여부를 인식하지 못하기에 발생

  - 방지책:
    - Table locking: Phantom 현상을 방지할 수 있지만, 효율성이 떨어진다.(현저히 저하된 동시성)
    - Index locking

- Index에 2PL 적용하기

  - Read하는 tx: 접근하는 모든 node에 S-lock 획득해야함.
  - Insert,update,delete하는 tx: 접근하는 모든 node에 X-lock 획득해야함.
  - 동시성 지원이 어려움.
  - 2PL과 달리 internal node에 대한 lock을 더 빠르게 unlock할 수 없을까? => tree-based protocol

- Index Crabbing(Index에 tree-based protocol 적용한 것)

  - (1) Root node에 S-lock 걸기
  - (2) 필요한 자식 node에 S-lock 다 걸었으면 root node의 S-lock release
  - (3) Insert, Delete 중에는 leaf lock의 S-lock을 X-lock으로 upgrade

  - 문제: Deadlock이 많이 발생함

- 더 나은 방식:

  - Child node에 대한 lock 획득 전에 부모 node의 lock 해제 등

### 2.5 SQL 트랜잭션 고립

- Weak levels of consistency

  - 일부 애플리케이션은 serializable하지 않은 schedule도 허용한다.
  - 즉 정확성과 성능의 tradeoff 발생

- Degree-two consistency

  - S-Lock은 언제든지 release 가능, X-lock은 tx의 끝까지 release 불가
  - Lock은 언제든지 획득 가능
  - Serializability 보장 안됨.

- Cursor stability

  - Read의 경우 각 tuple은 lock => read => unlock이 즉각적으로 이뤄진다.
  - X-lock은 tx의 끝까지 유지
  - Degree-two consistency의 특별한 case이다.

- Transaction isolations in SQL

  - SERIALIZABLE: Conflict serializable schedule만 생성됨을 보장.
  - REPEATABLE READ: commit된 값만 read. Tx 내에서 read를 몇 번 해도 결과가 동일함.
  - READ COMMITTED: Degree-two consistency와 동일.
  - READ UNCOMMITTED: uncommit된 데이터도 read 가능.

  ![picture 89](../../images/TMP_DB_17.png)

### 2.6 스냅샷 고립(Snapshot Isolation)

- Multiversion schemes

  - Data item의 이전 버전들을 보관해 동시성을 향상시킨다.
  - 성공적으로 write한 item이 있다면 해당 item의 새로운 버전이 생긴다.
  - 특정 item에 read가 발생하면 tx의 timestamp에 기반해 적절한 버전을 선택해 반환한다.  
    (read operations never have to wait!)

- Multiversion timestamp ordering

  - 각 item $Q_k$는 아래 3개 정보를 가진다.

    - Content: Value of the version $Q_k$
    - W-timestamp: $Q_k$를 성공적으로 write한 가장 최근 tx의 timestamp
    - R-timestamp: $Q_k$를 성공적으로 read한 가장 최근 tx의 timestamp

  - R-timestamp는 $TS(T_j) \gt R-timestamp(Q_k)$ 일 때 갱신된다.

  - $T_i$가 item Q에 대해 read, write를 한다고 해보자. 그리고 $Q_k$는 $TS(T_i)$와 같거나 바로 직전의 timestamp를 가진 버전이다.

    - $T_i$가 read(Q)를 수행하면 $Q_k$ 가 반환된다.
    - $T_i$가 write(Q)를 수행하면?
      - $TS(T_i) \gt R-timestamp(Q_k)$ 이면 $T_i$는 rollback된다.
      - $TS(T_i) = W-timestamp(Q_k)$이면 $Q_k$의 content만 갱신된다.
      - 그 외의 경우: Q의 새로운 버전이 생성된다.

- Snapshot Isolation

  ![picture 90](../../images/TMP_DB_18.png)

  - 이렇게 서로 다른 tx가 다른 item에 대해 write하면 결과가 달라질 수 있다.
  - 즉 write, read를 snapshot에 대해서만 수행하기에 발생하는 문제가 있다.

  - 이를 해결하기 위해 first-committer-wins, first-updater-wins 전략이 존재한다.

  ![picture 91](../../images/TMP_DB_19.png)

  - first-committer-wins: $T_2$가 rollback된다.
  - first-updater-wins: $T_1$이 rollback된다.

  - 장점: read 연산이 절대 block될 일이 없다.
  - 단점: serializable하지 않은 schedule이 생성될 수 있다.

  - Write skew

    ![picture 92](../../images/TMP_DB_20.png)

---
