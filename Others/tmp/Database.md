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
