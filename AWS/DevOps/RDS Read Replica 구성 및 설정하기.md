# RDS Read Replica 구성 및 설정하기

## Read Replica 생성

- RDS가 이미 있다면, AWS Management Console에서 쉽게 read replica를 생성할 수 있다.

![picture 15](/images/AWS_RDS_RL_1.png)

- Read replica를 구성하는 이유는 데이터를 변경하는 Create, Update, Delete 요청은 primary가 처리하고, read 연산은 read replica가  
  처리하게 함으로써 Primary의 부하를 줄이기 위함이다.

- 이때 RDS는 create, update, delete는 primary가 처리하도록 하고, read 연산은 read replica가 처리하도록 하는 단일 endpoint를  
  지원하지 않는다. 따라서 데이터를 변경하는 작업은 primary로, 조회하는 작업은 read replica로 전달하는 로직은 애플리케이션단에서 직접 설정해야 한다.

- Read replica를 설정하고 나면 primary database에 생기는 데이터의 변경사항들이 각 read replica들에게 비동기적으로 전달되고, 반영된다.

---

## Read Replica Load Balancing

- Read Replica가 2개 이상 있을 때, 이들에 대해 load balancing을 수행하고 싶은 경우가 있다.  
  Aurora의 경우에는 Reader Endpoint를 제공해 애플리케이션에서 이 endpoint를 사용하면 내부적으로 read replica들에 대해 load balancing을  
  수행해 주지만, RDS의 경우 이런 기능을 제공하지 않는다.

- 따라서 RDS Read Replica들에 대해 load balancing을 수행하려면 현재로써는 크게 두 가지 방법이 떠오르는데,  
  하나는 **애플리케이션에 load balancing 로직을 직접 구현하하는 것** 이고, 다른 하나는 **Aurora Reader Endpoint를 흉내내는 것** 이다.

- 여기서는 두 번째 방식을 선택할 것인데, 그 이유는 아래와 같다.

  - (1) 애플리케이션에 load balancing 로직을 추가하면 구현 및 검증하는 데 시간이 꽤나 소모될 것 같다.
  - (2) Read Replica가 추가되면 그만큼 많은 endpoint들을 애플리케이션이 처리하도록 관리해줘야 한다.

- 결정적으로 두 번째 방식을 택한 이유는 [Route53의 weighted routing policy](https://docs.aws.amazon.com/Route53/latest/DeveloperGuide/routing-policy-weighted.html)로 쉽게 구현할 수 있기 때문이다.

> - 실제로 이 방식은 [AWS가 제시하는 best practice 중 하나](https://aws.amazon.com/premiumsupport/knowledge-center/requests-rds-read-replicas/?nc1=h_ls)이기도 하다.

- 우선 Route53에 Hosted Zone이 있다는 가정 하에, 아래처럼 weighted routing policy를 사용해 쉽게 단일 endpoint를 제공할 수 있다.

![picture 16](/images/AWS_RDS_RL_2.png)

- 위처럼 동일한 record name을 가진 CNAME Record를 생성하고, 각 value에는 read replica의 endpoint들을 넣어주면 설정이 완료된다.  
  2개의 record에 주어진 weight가 모두 100이므로, `100 / (100 + 100)`, 즉 `1/2`의 확률로 실제로 연결할 read replica를 선택하게 될 것이다.

---

> Spring boot application에서 read replica를 사용하도록 설정하는 방법은 [여기](https://github.com/sang-w0o/Study/blob/master/Backend%20Frameworks/Spring%20%EB%BF%8C%EC%85%94%EB%B3%B4%EA%B8%B0/RDBMS%20Read%20Replica%20%EC%84%A4%EC%A0%95%ED%95%98%EA%B8%B0.md)에 있다.
