# Best Practices for Modeling Relational Data in DynamoDB

- 전통적인 RDBMS에서는 데이터를 관계를 갖도록 설계해 저장한다. 이 방식은 데이터가 계층적으로 구성되지 않는 대신,  
  여러 개의 table에 나누어 저장한다. 아래의 스키마는 일반적인 주문(order)과 직원 정보(HR)를 포함한 RDB 스키마이다.

![picture 19](/images/DYNAMODB_TRADITIONAL_RDB_SCHEME.png)

- RDBMS에서는 SQL을 사용해 애플리케이션에서 데이터를 원하는 대로 조회 혹은 조합할 수 있도록 해준다.  
  예를 들어, 주문된 order item들을 각 item을 배송할 수 있는 warehouse이 가진 재고 수량에 따라 정렬하기 위해 아래의 query를 수행할 수 있다.

```sql
SELECT * FROM Orders
  INNER JOIN Order_Items ON Orders.Order_ID = Order_Items.Order_ID
  INNER JOIN Products ON Products.Product_ID = Order_Items.Product_ID
  INNER JOIN Inventories ON Products.Product_ID = Inventories.Product_ID
  ORDER BY Quantity_on_Hand DESC
```

- 이렇게 원하는 결과를 위해 query를 작성하고 한 번에 요청하는 방식은 데이터 접근에는 엄청난 유연성을 더해주지만,  
  처리 비용이 상당하기 마련이다. 또한 다수의 table에서 데이터를 질의해 애플리케이션에서 사용하기 위해 조합해주어야 하는 경우도 많다.  
  위의 query문만 봐도 총 3개의 table에서 데이터를 읽어와서 정렬하고, 읽은 데이터들을 모두 모아 반환받게 된다.

- RDBMS 시스템의 성능을 저하시킬 수 있는 또다른 요소는 ACID 원칙을 만족시키는 transaction framework를 지원해야 한다는 점이다.  
  RDB 스키마는 계층적인 데이터를 여러 개의 table로 분해하기에 하나의 계층적인 데이터가 RDBMS에 저장되기 위해서는 여러 table에 접근해야 한다.  
  따라서 ACID 원칙을 만족시키는 transaction framework를 지원하기 위해면 write 연산이 수행중인 object에 대해 동시에 read가 수행되는  
  경우와 같은 race condition을 방지해야 한다. 이런 현상은 write 연산의 오버헤드를 증가시킨다.

> Race Condition: 두 개 이상의 프로세스가 공통 자원을 병행적으로(concurrently) 읽거나 쓰는 동작을 할 때,  
> 공용 데이터에 대한 접근이 어떤 순서에 따라 이루어졌는지에 따라 그 실행 결과가 같지 않고 달라지는 상황

- 위에서 말한 두 가지(한 번의 query가 여러 table에 접근하고 취합하는 것, race condition의 방지) 요소는 전통적인  
  RDBMS 플랫폼이 scale을 지원하기 어렵게 하는 대표적인 장벽이다.

- 따라서 만약 비즈니스 요구사항에 의해 엄청난 양의 traffic을 감당하는 동시에 low-latency 응답이 보장되어야 하는 경우,  
  NoSQL을 사용하는 것이 좋다.

- Scaling의 관점에서 다시 RDBMS와 NoSQL의 차이를 알아보자.

- RDBMS는 아래의 이유들 때문에 적절하게 scale할 수 없다.

  - 데이터를 계층화하지 않고, 여러 table에 나누어 저장시킨다.
  - ACID 원칙을 준수하는 transaction framework를 지원하기 위해 성능이 감소된다.
  - Query 횟수를 최소화하기 위해 처리 비용이 상당히 높은 join을 많이 사용한다.

- 반면, DynamoDB는 아래의 이유들 덕분에 더 잘 scale할 수 있다.

  - 스키마의 자율성 덕분에 하나의 item에 계층적인 데이터를 모두 넣을 수 있다.
  - Composite Key를 사용하면 같은 table내의 관련된 item들을 서로 가까이 저장할 수 있다.

- 데이터베이스에 대한 query 또한 매우 간결해지는데, 주로 아래의 형식으로 모두 해결된다.

```sql
SELECT * FROM Table_X WHERE Attribute_Y = "somevalue"
```

---
