# Transaction Isolation Level

- Transaction에는 Isolation Level(격리 수준)을 정할 수 있다.  
  이를 알기 전에, ACID 원칙을 보고 가자.

## ACID

- Transaction에서의 ACID원칙은 다음 규칙을 의미한다.

  - A(Atomicity): 트랜잭션과 관련된 작업들은 부분적으로 실행되다가 중단되지 않음을 보장해야 한다.
  - C(Consistency): 트랜잭션이 성공적으로 실행을 완료하면 언제나 일관성 있는 데이터베이스 상태로 변경되어야 한다.
  - I(Isolation): 트랜잭션 수행 중 다른 트랜잭션의 연산 작업에 의해 영향을 받으면 안된다.
  - D(Durability): 성공적으로 완료된 트랜잭션의 결과는 DB의 일관성이 깨지더라도 영구적으로 반영되어야 한다.

<hr/>

## Isolation Level

- Transaction의 격리 수준은 크게 4가지로 나눌 수 있다.

  - `READ UNCOMMITED`: 트랜잭션이 수행되는 내부에서 변경 내용이 `COMMIT` 또는 `ROLLBACK`되는 것과 관계없이  
    다른 트랜잭션에서의 값을 읽을 수 있다. 이런 현상을 Dirty Read라 하는데, 이는 정합성에 많은 문제를 일으키는 격리 수준이다.

  - `READ COMMITED`: RDB에서 대부분 기본적으로 사용되고 있는 격리 수준이며, `COMMIT`이 완료된 데이터만  
    읽을 수 있다. `READ UNCOMMITED`에서 발생하는 Dirty Read가 발생하지 않으며, 이 격리 수준의 단점은  
    하나의 트랜잭션 내에서 동일한 SELECT문을 수행하면 각 결과가 동일하다는 것이 보장되지 않는다는 것이다.  
    이러한 단점이 있는 이유는 다른 트랜잭션에서 `COMMIT`이 수행되었다면 그 `COMMIT`된 데이터를 반환해주는게  
    `READ COMMITED`의 특징이기 때문이다.

  - `REPEATABLE READ`: 이 격리 수준은 한 트랜잭션 내에서 여러 번 동일한 SELECT문을 수행하더라도  
    결과가 동일함을 보장한다. 이 격리 수준은 처음으로 SELECT를 수행한 시간을 기록한 뒤, 그 이후에는 모든  
    SELECT마다 해당 시점을 기준으로 Consistent Read를 수행해준다.  
    따라서 트랜잭션 도중 다른 트랜잭션에서 `COMMIT`이 일어나도 변경된 데이터가 반영되지 않는다.  
    첫 SELECT시에 생성된 snapshot을 활용하기 때문이다.

    > Consistent Read: SELECT 연산을 수행할 때 현재 DB의 실시간 값이 아닌, 특정 시점의 DB에 대한  
    > Snapshot을 읽어오는 것이다. 이 Snapshot은 Commit된 변화만이 적용된 상태를 의미한다.

  - `SERIALIZABLE`: 모든 작업을 하나의 트랜잭션에서 처리하는 것과 같은 격리 수준이다.  
    `READ COMMITED`, `REPEATABLE READ`의 공통적인 문제점은 Phantom Read가 발생할 수 있다는 점이다.

    > Phantom Read: 하나의 트랜잭션에서 UPDATE문이 유실되거나 덮어써질 수 있는, 즉 UPDATE 후  
    > Commit하고 다시 조회를 했을 때 예상과는 다른 값이 보이거나 데이터가 유실되는 경우

    - 아래 2개의 트랜잭션이 서로를 업데이트하는 상황을 보자.

    ```sql
    # (A - 1)
    SELECT phone FROM users WHERE id = 1;
    # (B - 1)
    SELECT phone FROM users WHERE id = 1;
    # (B - 2)
    UPDATE users SET phone = '010-1111-2222' WHERE id = 1;
    # (B - 3)
    COMMIT;
    # (A - 2)
    UPDATE users SET phone = '010-3333-4444' WHERE id = 1;
    # (A - 3)
    COMMIT;
    ```

    - (A-1): `SELECT`문이 `SELECT ... FOR SHARE`로 바뀌며 id = 1인 row에 S Lock이 걸린다.
    - (B-1): 역시 id = 1인 row에 S Lock을 건다.
    - (B-2, A-2): `UPDATE`를 수행하려 하면 해당 row에 X Lock을 걸려고 시도한다.(`DELETE`도 마찬가지)  
      이때, 이미 해당 row에는 S Lock이 걸려있으므로 `DEADLOCK` 상황에 빠진다.
    - 두 트랜잭션 모두 `DEADLOCK`으로 인한 timeout으로 실패하여 결론적으로 데이터는 변경되지 않는다.

    > S Lock(Shared Lock, 공유 잠금): 읽기 잠금(Read Lock)이라고도 하며, 리소스를 다른 트랜잭션에서  
    > 동시에 읽을 수는 있게 하되, 수정하는 것은 불가하게 하는 것이다. 이미 S Lock이 걸린 리소스에 대해서는  
    > X Lock을 걸 수 없다.

    > X Lock(Exclusive lock, 배타적 잠금): 쓰기 잠금(Write Lock)이라고도 하며, 어떤 트랜잭션에서  
    > 데이터를 변경하고자 할 때 해당 트랜잭션이 완료될 때까지 다른 트랜잭션에서 읽거나 쓰지 못하게 lock을 하고  
    > 트랜잭션을 진행시키는 것이다. 마찬가지로 X lock에 걸린 리소스는 S Lock을 걸 수 없으며, 이미 X lock이  
    > 걸린 리소스에 대해 다른 트랜잭션에서 X Lock을 걸 수도 없다.

    - 이렇게 `SERIALIZABLE`는 데이터를 안전하게 보호할 수는 있지만, `DEADLOCK`에 걸리기가 쉽다.

<hr/>
