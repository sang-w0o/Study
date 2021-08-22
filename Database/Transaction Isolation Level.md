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
