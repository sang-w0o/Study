# Best Practices for Designing and Using Partiton Keys Effectively

- DynamoDB에서 table 내의 item들의 식별자로 쓰이는 PK(Primary Key)는 단순 PK(Partition Key)일 수도 있고,  
  복합 PK(Partition Key + Sort Key)일 수도 있다.

## Using Burst Capacity Effectively

- DynamoDB는 _Burst Capacity_ 라는 기능으로 partition 별로 처리량을 유연하게 조절할 수 있도록 해준다.  
  Partition의 처리량을 모두 사용하고 있지 않으면, DynamoDB는 사용량이 폭발적으로 증가할 상황을 위해 partition 처리량의 일정량을 예약해 놓는다.

- 현재 DynamoDB는 read, write를 5분동안 처리할 수 있는 만큼을 예약한다. 그리고 만약 write, read 연산이 급증하는 순간이 발생하면  
  이 예약된 부분을 사용해 유연하게 대응한다.

- 추가적으로 백그라운드 작업 유지보수 또는 다른 task들을 위해 burst capacity를 예약할 수도 있다.

---

## Understanding DynamoDB Adaptive Capacity

- *Adaptive Capacity*는 DynamoDB를 불규칙적인 workload들에 대해 안정적으로 사용할 수 있도록 해주는 기능이다.  
  이 기능은 처리량이 급증하는 등 예외적인 상황이 발생했을 때 병목 현상을 최대한 줄여준다. 또한 사용자가 필요한 만큼의 공간만을  
  provision해줘서 비용 절감에도 유용하다.

- Adaptive Capacity는 자동으로 모든 DynamoDB table에 대해 활성화되며, 추가적인 비용이 들지 않는다.  
  따라서 사용자는 명시적으로 활성화 혹은 비활성화할 필요가 없다.

### Boost Throughput Capacity to High-Traffic Partitions

- Read, write 활동을 균일하게 분배하는 작업은 불가능하다. 만약 데이터 접근이 불균형적으로 일어난다면, 하나의 partition이 다른 partition들보다  
  더 많은 read, write 트래픽을 받게될 수도 있다. (이를 _Hot Partition_ 이라 한다.)

- 위와 같은 불규칙적인 access pattern을 수월하게 처리하기 위해 DynamoDB의 Adaptive Capacity는 Hot Partition들에 대해 병목 현상 없이  
  지속적으로 read, write 연산을 수행할 수 있게 해준다. 즉 트래픽을 더 많이 받는 partition들의 처리량을 순간적으로 증가시키는 것이다.

- 아래의 그림은 Adaptive Capacity의 작동 방식을 보여준다. 하나의 테이블이 사용하는 4개의 partition들이 있는데, 초기에는 각각 400 WCU를  
  4분의 1씩 균등하게 분배받은 상태로 provisioning된다. 이때 Partition 1, Partition 2, Partition 3는 초당 50WCU의 트래픽을 받고,  
  Partition 4가 초당 150WCU의 트래픽을 받는 상황이 발생했다고 하자.(Partition 4가 Hot Partition이다.)  
  Hot Partition은 사용 가능한 Burst Capacity가 있을 때까지 write 트래픽을 처리할 것이지만, 이 상황에서 결국 초당 100 WCU의 트래픽  
  이상을 받게 된다면 병목 현상이 발생할 것이다.

- 이때 DynamoDB의 Adaptive Capacity가 적용되는데, Partition 4의 처리량을 증가시켜 초당 150 WCU의 트래픽을 병목현상 없이  
  처리할 수 있게 해준다.

![picture 6](../../../../../images/DYNAMODB_ADAPTIVE_CAPACITY
.png)

### Isolate Frequently Accessed Items

- 만약 애플리케이션에서 하나 이상의 특정 item들만 훨씬 더 높은 트래픽이 접근한다는 패턴이 발견되면, Adaptive Capacity가 동작해  
  자주 접근되는 item들이 동일한 partition에 존재하지 않도록 partition을 rebalance해준다. 이는 곧 하나의 partition에 대해  
  트래픽이 몰려 병목 현상이 발생할 가능성을 줄여준다.

- 만약 단 하나의 item에 대해서 지속적으로 많은 양의 트래픽이 집중되면, Adaptive Capacity는 rebalance를 수행해 해당 item만을  
  갖는 partition이 생기도록 할 것이다.

---
