# NoSQL Design

- Amazon DynamoDB와 같은 NoSQL은 데이터를 다루기 위해 key-value pair, 또는 document 저장소와 같은 모델을 사용한다.  
  RDBMS에서 NoSQL로 바꿀 때에는 이 둘의 차이점을 파악하고, 디자인 원칙을 아는 것이 중요하다.

## Differences Between Relational Data Design and NoSQL

- RDBMS와 NoSQL은 각자 장점과 단점이 있다.

  - RDBMS에서는 데이터를 유연하게 쿼리할 수 있지만, 쿼리 수행 비용이 상대적으로 비싸며 트래픽이 많은 상황에 쉽게 scale할 수 없다.
  - NoSQL에서는 데이터를 효율적이지만 제한된 방식으로 질의할 수 있다.

- 위와 같은 차이점으로 인해 데이터베이스를 설계할 때도 차이점이 생긴다.

  - RDBMS에서는 유연성을 최우선으로 두고 설계하게 된다. 따라서 상세한 구현 내용이나 성능에 대해 덜 신경쓰게 된다.  
    Query 최적화는 스키마 디자인에 영향을 주지 않는다.

  - NoSQL에서는 가장 자주 사용되고, 중요한 query들을 최대한 적은 비용으로 하기 위해 스키마를 설계해야 한다.  
    데이터의 구조는 비즈니스 요구사항에 따라 다르다.

---

## Two Key Concepts for NoSQL Design

- NoSQL의 설계는 RDBMS를 설계할 때와는 조금 다른 시각이 필요하다. RDBMS를 설계할 때는 접근 패턴에 대해서는 신경쓰지 않고  
  정형화된 데이터 모델을 만들 수 있다. 그리고 이후에 새로운 문제나 요구사항이 생길 때 확장할 수 있다.

### How NoSQL is different

- RDBMS와 반대로 NoSQL은 요구사항을 파악하기 전까지 스키마를 설계하지 않는다. NoSQL 설계 시 사전에 비즈니스 요구사항과  
  애플리케이션에서 다룰 방식을 파악하는 작업을 필수적이다.

- NoSQL 에서는 최대한 적은 개수의 table을 사용하도록 해야 한다.

### Approaching NoSQL Design

- 최우선적으로 시스템의 요구사항을 만족시키는 query 패턴을 파악하는 것이 중요하다.

- 조금 더 자세히 말하자면, 설계에 들어가기 전에 애플리케이션이 접근할 방식을 파악하기 위해 아래 3가지 요소를 알아야 한다.

  - Data size: 한 번에 얼마만큼의 데이터가 저장되고 요청될지 파악하는 것은 데이터를 가장 효율적으로 partition하기 위해 큰 도움이 된다.
  - Data shape: RDBMS와 같이 query가 수행될 때 데이터를 가공하는 것과 반대로, NoSQL 데이터베이스는 query가 예상하는 데이터 형식 그대로를  
    스키마에서 사용하게 된다. 이는 특히 속도 향상과 가용성 향상을 위해 중요하다.
  - Data velocity: DynamoDB는 query를 처리할 수 있는 물리적 partition들을 증가시킴으로써 scale 한다. 따라서 가장 많이 사용될  
    query를 파악해 데이터를 어떻게 partition할 것인지를 파악하는 것이 중요하다.

- 위의 query 요구사항을 파악했다면, 다음으로는 데이터를 일반적인 규약에 따라 구조화해야 한다.

  - Keep relational data together: 20년 전 수행된 routing-table 최적화에 따르면, 관계를 가지는 데이터들을 한 곳에 모아두는 것이  
    속도 향상에 가장 중요한 factor였다고 한다. 이 연구결과는 NoSQL에도 그대로 적용된다. 즉 **관계를 갖는 데이터들을 한 곳에 모아두는 것이**  
    **query 비용과 성능에 큰 영향을 미치게 된다.** 이는 곧 NoSQL Database에서는 가능한 한 table 개수가 적도록 설계하라는 것을 의미한다.

  - User sort order: 관계된 item들은 만약 key가 그들을 정렬되게끔 설정되어 있다면 query, grouping 모두 효율적으로 할 수 있다.

  - Distribute queries: 많은 양의 query가 데이터베이스의 한 뿌분에 집중적으로 몰리게 하지 않는 것도 중요하다.  
    대신, 트래픽을 partition에 따라 적절히 분배시킬 수 있도록 data key를 정의해야 한다.

  - Use global secondary indexes: Global Secondary Index를 생성해 table이 기본적으로 지원하는 query보다 더 다양한 query를  
    빠르고 저비용으로 처리할 수 있다.

---
