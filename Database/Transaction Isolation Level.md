# Transaction Isolation Level

<h2>Transaction</h2>

- Database Transaction은 데이터베이스에 대해 특정 연산을 수행하는 단위라고도 할 수 있으며,  
  시스템에서 데이터베이스와 상호작용하기 위한 단위라고도 할 수 있다.

<hr/>

<h2>ACID</h2>

- 트랜잭션은 ACID 원칙을 따라야 한다. 여기서 ACID는 아래와 같다.

- A(Atomicity): 트랜잭션과 관련된 작업들은 부분적으로 실행되다가 중단되지 않음을 보장해야 한다.  
  이는 말 그대로 트랜잭션의 작업이 부분적으로 실행되면 안된다는 이야기이다. 정상적인 실행을 마쳤으면 COMMIT을 하고,  
  도중에 정의하지 않은 에러 상황을 만나게 된다면 전체 연산이 ROLLBACK 처리 되어야 한다.  
  이는 특히 데이터의 정합성과 일관성 유지를 위해 매우 중요한 원칙이다.

- C(Consistency): 트랜잭션이 성공적으로 실행을 완료하면 언제나 일관성 있는 데이터베이스 상태로 변경되어야 한다.

- I(Isolation): 트랜잭션 수행 중 다른 트랜잭션의 연산 작업에 의해 영향을 받으면 안된다.

- D(Durability): 성공적으로 완료된 트랜잭션의 결과는 DB의 일관성이 깨지더라도 영구적으로 반영되어야 한다.

- 위 ACID 원칙 중, I(Isolation)에 대해 다뤄보자.

<hr/>

<h2>Isolation Level</h2>

- Isolation Level은 트랜잭션의 격리 수준을 의미하며, 이를 이용하여 하나의 트랜잭션이 다른 트랜잭션의 영향을  
  받게 할 수도, 못 받게 할 수도 있다.

- Isolation Level을 테스트하기 위한 테스트용 테이블은 아래와 같다.

```sql
CREATE TABLE users(
	user_id INTEGER AUTO_INCREMENT PRIMARY KEY,
	name VARCHAR(255) NOT NULL
);
```

- 참고로 아래에 있는 예시 쿼리문들은 모두 MySQL, MariaDB를 기준으로 작성되었다.

<h3>READ UNCOMMITED</h3>

- `READ UNCOMMITED`는 트랜잭션이 수행되는 내부에서 변경 사항이 COMMIT 또는 ROLLBACK되는 것과 관계없이  
  다른 트랜잭션에서 변경 사항을 읽을 수 있다. 이러한 현상을 Dirty Read라 하는데, 이는 사실 정합성에 많은 문제를  
  일으킬 수 있는 격리 수준이다.

- 우선 작동을 확인하기 위해 아래 쿼리문으로 트랜잭션 격리 수준을 변경한 후, INSERT를 수행해보자.  
  2개의 세션을 실행하기 위해 DataGrip의 콘솔을 2개로 분리했다.  
  우선, 하나의 세션(A)에서 아래의 쿼리를 순차적으로 실행하여 데이터를 삽입해보자.

```sql
# Session A
SET TRANSACTION ISOLATION LEVEL READ UNCOMMITED;
BEGIN;
INSERT INTO users VALUES(DEFAULT, 'name1');
```

- 다음으로 다른 세션(B)에서도 동일하게 READ UNCOMMITED로 트랜잭션을 실행 한 후,  
  여기서는 SELECT문을 해보자. 진행한다면 Session B의 SELECT 결과에는 아직 Session A의  
  트랜잭션 결과가 COMMIT되기 전인데도 Session A에서 INSERT한 데이터가 조회된다.  
  이때, Session A에서 ROLLBACK처리를 해주면, Session B의 SELECT 결과에는 아무런 값도 나오지 않는다.

<h3>READ COMMITED</h3>

- `READ COMMITED`는 RDB에서 기본값으로 많이 사용되는 격리 수준이다. 이 격리 수준에서는 COMMIT이 완료된 데이터만  
  조회할 수 있다. `READ UNCOMMITED`에서 발생하는 Dirty Read가 발생하지 않지만, 이 격리 수준의 단점은  
  하나의 트랜잭션 내에서 동일한 SELECT문을 여러번 수행하면 각 결과가 동일하다는 것이 보장되지 않는다는 것이다.

- 바로 실제 쿼리문으로 확인해보자.  
  우선 Session A에서는 `READ COMMITED`로 격리 수준을 맞추고, SELECT문만 실행해보자.

```sql
# Session A
SET TRANSACTION ISOLATION LEVEL READ COMMITED;
BEGIN;
SELECT * FROM users;

# Session B
INSERT INTO users VALUES(DEFAULT, 'name');
```

- 그리고 Session B에서는 계속해서 INSERT문을 수행해보자.  
  Session B의 INSERT문을 하고, 바로 Session A에서 SELECT문을 해보면,  
  Session A에서는 하나의 트랜잭션에서 동일한 SELECT문을 계속 질의하지만 결과는 달라지는 것이 확인된다.  
  이것이 `READ COMMITED` 격리 수준의 단점이다.

<h3>REPEATABLE READ</h3>

- `REPEATABLE READ`는 하나의 트랜잭션 내에서 동일한 SELECT문을 여러 번 질의하더라도 항상 결과가 동일함을 보장한다.  
  이 격리 수준에서는 처음으로 SELECT 쿼리를 수행한 시점을 저장한 후, 이후에는 모두 이 시점을 기준으로 Consistent Read를 수행한다.  
  따라서 트랜잭션 도중 다른 트랜잭션에서 COMMIT으로 인해 데이터가 수정되거나 추가되더라도 변경된 사항이 이 트랜잭션에는  
  반영되지 않는다.

  > Consistent Read: SELECT 연산을 수행할 때 실제 DB의 실시간 값이 아닌, 특정 시점의 DB에 대한  
  > Snapshot을 읽어오는 것이다. 이 Snapshot은 COMMIT된 변화만이 적용된 상태이다.

- Session A에서 격리 수준을 `REPEATABLE READ`로 맞추고, SELECT문을 실행해보자.  
  이때, Session A에서는 SELECT를, Session B에서는 INSERT를 한 번씩 번갈아가며 수행해보자.

```sql
# Session A
SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;
BEGIN;
SELECT * FROM users;

# Session B
INSERT INTO users VALUES(DEFAULT, 'name');
```

- Session B에서 값을 추가, 수정, 삭제하더라도 Session A는 이미 처음 SELECT를 질의한 시점에 Snapshot을 만들고,  
  트랜잭션 내에서는 그 Snapshot에 대해서만 질의를 하기 때문에 Session B에서 만든 변경 사항이 Session A에는 반영되지 않는다.

<h3>SERIALIZABLE</h3>

- `SERIALIZABLE`는 모든 작업을 하나의 트랜잭션에서 처리하는 것과 같은 격리 수준이다.  
  `READ COMMITED`, `REPEATABLE READ`의 공통적인 문제점은 Phantom Read가 발생할 수 있다는 점이다.

  > Phantom Read: 하나의 트랜잭션에서 UPDATE문이 유실되거나 덮어써질 수 있는, 즉 UPDATE 후  
  > COMMIT하고 다시 조회를 했을 때 예상과는 다른 값이 보이거나 데이터가 유실되는 경우

- Session A와 Session B가 서로를 업데이트하는 상황을 보자.  
  이 상황에는 user_id가 1인 데이터가 존재한다.

```sql
# Session A
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
BEGIN;
SELECT * FROM users WHERE user_id = 1;

# Session B
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
BEGIN;
UPDATE users SET user_name = 'new_name' WHERE user_id = 1;
```

- 위 경우에, Session B의 UPDATE문은 동작하지 않는다. 이유를 알기 위해서는 S Lock과  
  X Lock에 대해 알아야 한다.

> S Lock(Shared Lock, 공유 잠금): 읽기 잠금(Read Lock)이라고도 하며, 리소스를 다른 트랜잭션에서  
>  동시에 읽을 수는 있게 하되, 수정하는 것은 불가하게 하는 것이다. 이미 S Lock에 걸린 리소스에 대해서는  
>  X Lock을 걸 수 없다.

> X Lock(Exclusive Lock, 전체 잠금): 쓰기 잠금(Write Lock)이라고도 하며, 어떤 트랜잭션에서  
>  데이터를 변경하고자 할 때 해당 트랜잭션이 완료될 때 까지 다른 트랜잭션에서 읽거나 쓰지 못하게 lock을 하고  
>  트랜잭션을 진행시키는 것이다. X Lock에 걸린 리소스에 대해서 S Lock을 걸 수도 없고, 이미 X Lock에 걸린  
>  리소스에 대해서 다른 트랜잭션에서 또 X Lock을 걸 수도 없다.

- 우선 위 쿼리문에서 SELECT문이 실행되면, `SERIALIZABLE` 격리 수준에서는 `SELECT ... FOR SHARE`로  
  쿼리가 변경되어 user_id가 1인 행에 S Lock을 걸게 된다.

- 이후에 Session B의 UPDATE문이 실행되면, S Lock에 걸려 있는 행에 대해 X Lock을 걸려고 하기에  
  에러가 발생한다. 결국 UPDATE 문은 아래의 에러를 출력하며 작업에 실패한다.

```
Lock wait timeout exceeded;
```

- 마찬가지로 만약 Session A에서 `SELECT * FROM users;`를 실행하여 users 테이블의  
  모든 데이터에 대해 S Lock이 걸리게 되면, Session B에서는 INSERT가 수행되지 않는다.

<hr/>
