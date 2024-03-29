# Multi-tenant 시스템에서의 형평성

> - [Amazon Builder's Library - Fairness in multi-tenant systems](https://aws.amazon.com/builders-library/fairness-in-multi-tenant-systems/)

## Introduction

- 이 글은 Amazon이 rate limiting(처리율 제한, throttling, admission control)의 오버헤드를 유발하지  
  않으면서 시스템의 API 요청을 관리하는 방법에 대한 몇 가지 접근법을 소개한다. 이러한 보호 체계가 있지 않다면  
  시스템이 처리 가능한 수치보다 더 많은 트래픽이 몰렸을 때 부하가 발생한다. API rate limiting은 클라이언트  
  workload에 우선순위를 매기거나 예측이 불가한 처리량 급증에 배압을 적용하는 등의 방식으로 구현할 수 있다.  
  여기서는 상용 시스템에서 안전하게 처리량을 관리할 수 있는 다양한 방법을 살펴본다. 특히 Amazon이 API들을  
  _공평하게_ 제공함으로써 예측 가능한 성능과 가용성을 유지하는 비결에 대해서도 소개한다.

- Amazon이 rate limiting을 도입하기 전, 왜 rate 기반의 quota가 중요한지, 그리고 이를 개선하는 것이  
  어떻게 multi-tenant system의 가용성은ㄹ 높이는지 살펴보자.

---

## The case for multitenancy

- SoA(Service-oriented Architecture)는 amazon의 강한 오너십, 그리고 팀과 시스템의 느슨한 결합을 위한  
  필수 요소이다. 이 아키텍쳐는 또다른 중요한 장점도 가지는데, 바로 리소스 공유를 통해 하드웨어를 더욱 효율적으로  
  사용할 수 있다는 것이다. 만약 이미 존재하는 서비스를 다른 애플리케이션이 사용하고 싶다면, 서비스 관리자는 새로운  
  tenant에 대해 그렇게 해야 할 일이 많지 않다. 사용 사례와 보안 검사를 진행한 후 서비스 관리자는 새로운 client  
  system에게 특정 API를 호출하거나, 특정 데이터에 접근할 수 있는 권한을 부여한다. 이후에 동일한 서비스를 더  
  추가해야한다면, 단지 복사만 하면 된다.

- Multi-tenant system에서의 리소스 공유는 매우 중요한 장점이다. Multi-tenant 시스템은 여러 고객의 요청을  
  동시에 처리하는 등 여러 개의 workload를 동시에 처리한다. 또한 workload에 우선순위를 매겨 더 우선순위가  
  높거나 빠르게 처리되어야 하는 workload를 먼저 처리할 수도 있다. 이에 반해 single-tenant 시스템은  
  단일 고객만의 workload를 받아 처리한다.

- 리소스 공유는 SoA와 클라우드 아키텍쳐의 핵심적인 개념이며, 이를 활용하면 인프라 비용 감소 및 인력 또한  
  감소시킬 수 있다. 추가적으로 자원을 더 잘 활용하기 때문에 동일한 workload를 처리하기 위해 필요한 서버 개수가  
  줄어들게 되며, 이는 곧 인프라를 관리하기 위한 에너지(전기, 냉각수 등)의 비용도 줄어들기에 환경에 대한 부담도  
  줄어들게 된다.

### The evolution of multi-tenant databases

- 저자는 single-tenant와 multi-tenant 시스템을 비교할 때, 데이터베이스를 자주 떠올린다고 한다.  
  사실 대부분의 시스템에서는 상태를 관리하고 저장하기 위해 데이터베이스를 사용한다. 그리고 일부 시스템은  
  처리해야하는 트래픽이 적은 반면, 어떤 시스템은 굉장히 중요한 역할을 수행함과 동시에 처리해야 할 트래픽도 많을 수 있다.

- 저자가 Amazon에서 근무한지 얼마 안되었을 때, `Amazon.com`의 web server fleet의 운영 방식을 자동화하는  
  팀에서 일했다고 한다. 저자는 팀원들과 함께 시간대별로 provisioning해야 할 web server의 개수를 예측하는 시스템,  
  각 서버의 health check 모니터링, 그리고 장애가 난 서버들의 자동화된 제거 및 교체 작업을 구현했다.  
  또한 다른 팀들이 소프트웨어를 더욱 편리하게 배포할 수 있도록 해주는 시스템도 구현했다고 한다.

- 이러한 도구들의 상태를 관리하기 위해서는 데이터베이스가 필요했다. 지금의 Amazon이라면 이런 경우를 위해 기본적으로  
  관리의 편이성, 확장성, 예측 가능한 성능, 그리고 가용성을 제공하는 NoSQL 데이터베이스를 사용하겠지만,  
  당시에는 NoSQL의 개념이 널리 확장되지 않은 시절이라 MySQL을 사용하고, 고가용성을 확보하기 위해 replica node들을  
  추가해 사용했다고 한다. 그리고 여기에 더해 SPOF를 없애기 위해 장애에 대한 테스트, 백업, 그리고 복구도 구현했다고 한다.

- 시스템이 하나둘 늘어날 때마다 저자의 팀은 새로운 데이터베이스를 추가하는 대신, 이미 존재하는 데이터베이스에 table을  
  추가하는 방식이 더 끌렸다고 한다. 왜 이 방식이 더 끌렸을까? 가장 먼저, 데이터베이스 서버 사용량을 봤을 때 서버의  
  부하가 거의 없었으며, 새로운 데이터베이스를 설정하고, 모니터링하고, 관리하는 등의 운영 비용이 꽤나 높았기 때문이다.

- 아래 그림은 수년 전 `Amazon.com` 의 web server fleet을 관리하기 위한 시스템의 아키텍쳐이다.  
  그림에 나오다시피 deployment service와 periodic fleet operator가 동일한 데이터베이스를 사용하고 있다.

  ![picture 1](/images/AWS_BL_MTSF_1.png)

- 하지만 저자의 팀은 실제로 여러 개의 애플리케이션들이 동일한 데이터베이스를 사용하도록 한 순간, 후회하게 되었다고 한다.  
  심지어 저자는 배포 도구의 성능 저하로 인해 on-call 을 받았던 적도 있다고 한다. 문제는 매일 밤마다 상태를 갱신하기 위해  
  수행되는 cron job 때문이었다. 이 batch job이 공유된 데이터베이스에 엄청난 부하를 일으켰다고 한다.

- 이렇게 데이터베이스를 공유하는 것(인프라적, 관리적 적은 비용)과 별도의 데이터베이스를 사용하는 것(workload의 격리)  
  을 고민하다가, 결국 MySQL이 single tenant에서 사용되도록 의도된 것임에 따라 별도의 데이터베이스를 사용하기로 결정했다고 한다.

- Amazon RDS가 공개된 이후, 저자의 팀은 운영 비용을 상당히 자동화할 수 있어서 매우 편리했다고 한다.  
  그리고 동일한 데이터베이스를 공유하는 것과 거의 비슷한 정도로 편리하게 애플리케이션마다의 데이터베이스를 구축하고 운영할 수  
  있었다고 한다. 하지만 그럼에도 workload마다 다루는 크기가 달랐기 때문에 각 데이터베이스마다의 활용도를 지속적으로  
  모니터링해 최적의 스펙을 선택해야 했다고 한다.

- 이후 저자는 새로운 데이터베이스를 만드는 팀에 합류하게 되었는데, 이 데이터베이스의 목표는 확장성이 뛰어나고, 가용성이 높고,  
  latency가 낮고, 완벽히 관리형이어야 한다는 것이었다.(DynamoDB)

- DynamoDB는 multitenancy를 적극적으로 활용해 뛰어난 확장성, 안정성, 그리고 가용성을 제공한다.  
  Amazon RDS를 사용할 때와 달리 DynamoDB 리소스를 생성하려면 EC2 instance를 띄울 필요조차 없다.  
  데이터베이스와는 API를 통해 소통하며, DynamoDB는 workload를 처리하기 위한 최적의 방법을 내부적으로 알아내 처리한다.  
  사용자의 workload가 많아지고 적어지는 것을 내부적으로 처리해, 최소한의 필요한 서버를 사용하도록 한다.

- 데이터베이스의 경우와 마찬가지로 일반적인 목적을 위한 compute server들도 multitenancy를 활용한다.  
  AWS Lambda의 경우, 리소스 공유를 위해 경량화된 가상화 기술인 Firecracker를 사용함으로써 컴퓨팅 리소스 공유가  
  수 ms 단에 일어나며, Amazon API Gateway의 경우에는 API reqeust 레벨에서 자원 공유가 이뤄진다고 한다.  
  이러한 서비스를 사용하는 고객들은 뛰어난 확장성, 그리고 사용의 편리성을 누릴 수 있게 된다. 하지만 내부적으로는  
  multitenancy를 사용함으로써 발생하는 문제점들을 처리하기 위해 굉장히 복잡한 시스템이 돌아가고 있다.  
  이러한 문제점들 중 하나는 _형평성_ 이 있다.

---

## Fairness in multi-tenant systems

- Multitenency를 사용하는 서비스들은 형평성을 보장하기 위해 시스템과 상호작용한다.  
  여기서 말하는 _형평성_ 이란, multi-tenant system 내의 모든 클라이언트들에게 마치 single-tenant system을  
  사용하는 것과 같은 경험을 제공하는 것을 의미한다. 이렇게 형평성을 유지하기 위해 시스템은 아래의 작업들을 수행한다.

  - 새로운 workload가 fleet 내에 배치될 위치를 결정하는 알고리즘을 수행한다.
  - Workload들을 분배하기 위해 각 workload와 서버의 활용도를 모니터링한다.
  - 전반적인 fleet의 활용도를 모니터링하면서, fleet의 크기를 조절한다.
  - 기반 시스템이 모두 활용되지 않으면 할당된 범위를 넘어서 workload가 사용하도록 하고, 반대로 시스템이 모두  
    활용되고 있다면 workload가 지정된 할당량을 지키도록 한다.

- 고도화된 형평성 제공 시스템들은 위의 기술들은 흥미로운 방법으로 조합한다. 예를 들어, 어떤 형평성 시스템은 각  
  workload의 활용도를 모니터링하면서 함께 사용하기 좋은 workload들의 집합을 만들어 동일한 서버에 배치시킨다.  
  그리고 하나의 workload가 자신에게 할당된 리소스를 모두 활용하지 않는 경우, 동일한 서버에 위치한 다른  
  workload가 리소스를 빌려 사용할 수도 있게 한다.

- 이러한 자원 공유가 동작하도록 하기 위해 "자원을 빌리는 행위"는 workload는 모른 채 진행되어야 한다.  
  만약 특정 workload가 자신에게 할당된 모든 리소스를 사용해야 할 경우가 온다면, 다른 workload가 빌려서  
  사용하고 있는 자원을 반납하는 행위는 즉시 일어나야 한다. 추가적으로 workload들은 매우 빠르게 다른 서버로  
  이동되어 실행될 수 있어야 한다. 만약 A라는 workload가 자신에게 할당된 리소스를 넘어서 사용하기 위해 B라는  
  workload의 여유 리소스를 사용하고 있는 경우, B가 자신에게 할당된 리소스를 모두 사용하게 되면 A는 재빠르게  
  다른 서버로 이동되어 실행되어야 한다.

### Load shedding + fairness

- 시스템의 부하가 증가하면 자동으로 확장할 수 있어야 한다. 이를 가장 쉽게 구현하는 방법은 더 많은 리소스를 할당하고,  
  수평적으로 확장하는 것이다. AWS Lambda 등의 serverless 아키텍쳐를 사용하는 서비스들의 경우 수평적 확장은  
  거의 즉시 일어나는 반면, non-serverless 서비스들은 확장하는 데에 더 많은 시간이 소요된다.

- 일반적으로는 수 분 내에 확장하는 것으로 충분할 수 있다. 하지만 만약 auto scaling이 수행되는 시간보다 더 빨리  
  트래픽량이 급증하면 어떻게 해야할까? 이 경우에는 두 가지 선택지가 있는데, 하나는 일시적으로 시스템이 부하에 걸려  
  모든 요청의 처리 속도가 느려지게 하는 것이고, 또다른 방식으로는 특정 수치 이상의 요청을 무시하는 shedding 방식을  
  사용할 수 있다. Amazon의 경우, 일관적인 성능 제공을 더욱 선호하기 때문에 두 번째 방식을 선택했다고 한다.  
  시스템이 과부하되었을 때 latency를 증가시키는 것은 분산 시스템인 경우 다른 시스템의 처리 속도도 느리게 할 수 있기  
  때문에 수많은 부수 효과들을 일으킬 수 있다. 이렇게 빠르게 실패하는 fail-fast strategy는 과부화된 시스템들이  
  지속적으로 동작하고, 다른 시스템들의 성능을 저하시키는 것을 방지할 수 있다.

- 여기서 과부하가 발생한 경우 load shedding이 굉장히 효과적이다.  
  Load shedding은 특정 수치 이상의 요청이 리소스를 사용하도록 하는 대신, 빠르게 요청을 거부하는 방식을 말한다.  
  HTTP 서비스의 경우, 클라이언트는 요청을 보내자마자 HTTP 503 상태 코드와 함께 에러 메시지를 반환받는다.  
  이렇게 하는 것은 auto scaling이 동작해 추가적인 리소스를 확보하기 위한 시간을 벌어줄 수 있다.  
  그리고 빠르게 에러와 함께 요청을 거부하는 것은 리소스를 사용해 요청을 처리하는 것보다 훨씬 더 비용적으로 저렴하기  
  때문에 서버는 지속적으로 일관된 성능을 제공할 수 있게 된다.

- 이렇게 Amazon에서는 대부분의 경우 서버의 성능을 최상으로 끌어올리기 위해 load shedding을 사용하지만,  
  특정 경우에는 일부러 latency를 늘리는 경우가 있다. 예를 들어, 만약 load balancer가  
  LOR(Least Outstanding Requests) 알고리즘을 사용하는 경우, 성공적으로 처리되는 요청들의 latency에  
  맞게 에러 메시지를 반환하도록 함으로써 load balancer가 이미 과부하된 서버에 추가적인 트래픽을 전달하는 것을 방지한다.

> LOR(Least Outstanding Requests) 알고리즘: 특정 서버가 시간이 오래 걸리는 요청을 처리하거나 요청을  
> 처리하는 데에 시간이 오래 걸리는 경우, 그렇지 않은 대상에게 트래픽을 전달하는 알고리즘
>
> 즉, LOR 알고리즘을 사용하면 상대적으로 처리 시간이 짧은 부하가 걸린 대상(빠른 에러 응답)에게
> 되려 요청을 전달함으로써 과부하된 대상이 트래픽을 더 받는 현상이 발생할 수 있기 때문에
> 경우에 따라 에러 메시지도 성공적으로 처리되는 요청들의 latency에 맞춰 반환한다는 것이다.

- 하지만 multi-tenant 서비스의 경우, load shedding만을 사용한다고 사용자들에게 마치 single-tenant  
  서비스를 사용하는 것과 같은 경험을 줄 수 없다. 일반적으로 여러 개의 tenant가 가지는 부하는 각자 다르다.  
  그렇기에 만약 서비스의 전반적인 부하가 증가하면, 이는 대부분 하나의 tenant에 의한 영향일 것이다.  
  진정한 형평성을 제공하기 위해서는 하나의 tenant의 부하 증가로 인해 다른 tenant가 요청을 처리하는 데에 실패하면 안된다는  
  것을 감안해야 한다.

- Amazon에서는 multi-tenant 시스템에 형평성을 구현하기 위해 트래픽의 급증에 대처하기 위해 rate limiting을  
  사용하는 동시에 tenant, 그리고 workload 단위의 quota(최대 리소스 할당량 등)을 부여한다.  
  이러한 방식을 통해 multi-tenant 서비스가 일시적으로 많은 량의 트래픽을 받더라도, 해당 workload의 요청만 빠르게  
  거절하지, 다른 workload들은 일반적인 상태와 마찬가지로 예측 가능한 성능을 제공할 수 있다.

- 하지만 quota를 사용하는 것은 역설적이지만, 서비스의 가용성을 높이는 동시에 내린다.  
  Tenant 내 하나의 workload가 자신의 quota를 넘어서면, 추가적인 요청들이 실패하게 될 것이고, 이는 곧 가용성이  
  낮아짐을 의미한다. 하지만 실세계에서 서비스는 해당 요청들을 처리하기 위한 여유 리소스가 충분히 있을 수 있다.  
  API rate limiting은 서비스의 가용성을 지키기 위해 유용한 기술이지만, 여기에 더해 호출자들이 자신의 quota를  
  불필요하게 넘어서지 않도록 하기 위해 노력해야 한다.

- Load shedding과 마찬가지로 rate-based quota를 강요하는 것은 요청을 처리하기 전 훨씬 빠르고 효율적으로  
  에러 메시지를 반환하도록 할 수 있다. 하지만 이 응답은 "클라이언트가" 자신의 quota를 초과했다는 것을 의미하는  
  HTTP 429 상태 코드를 가진다.

> 일반적으로 500번대 HTTP 상태 코드는 모종의 이유로 서버가 요청을 처리하지 못했음을 의미한다.

### Quota visibility and flexibility

- 서비스 관리자들은 종종 클라이언트마다의 quota를 설정하곤 한다. 예를 들어 AWS의 경우 클라이언트는 일반적으로  
  AWS 계정일 것이다. 그리고 경우에 따라 클라이언트 단위가 아닌, 더 세부적인 quota가 설정되는 경우가 있다.  
  예를 들어, DynamoDB table의 연산을 호출할 수 있는 quota가 있다.  
  서비스 관리자들은 각 클라이언트들에게 기본적인 quota를 부여하고, 만약 클라이언트들이 일반적인 경우를 넘어서  
  요청할 필요가 있다면, quota를 늘려줄 것을 요청한다.

- Quota에는 다양한 종류가 있고, 각 종류마다의 단위가 있다.  
  그들 중 하나는 "클라이언트가 동시에 실행할 수 있는 리소스의 개수" 가 있다. 예를 들어, Amazon EC2는  
  각 AWS 계정마다 동시에 실행시킬 수 있는 EC2 instance의 개수에 대한 quota를 가진다.  
  또다른 quota로는 rate-based quota가 있다. Rate-based quota는 일반적으로 "초당 요청 개수"와 같이 측정된다.

- 아래 그래프는 quota를 사용하는 예시를 나타낸다. 서비스는 유한된 자원을 가지며(y축), 3개의 클라이언트  
  (blue, orange, gray)를 가진다. 그리고 서비스는 이 3개 클라이언트에게 각각 자신의 자원을 1/3씩 hard-allocate 했다.  
  그림에서 Blue는 자신에게 hard-allocate된 quota를 초과하려 하지만, 그러지 못한다.

  ![picture 2](/images/AWS_BL_MTSF_2.png)

- 위와 같은 quota가 확장될 수 있다는 점을 감안해, 서비스는 클라이언트에게 해당 클라이언트의 quota 정보와 현재  
  사용량과 같은 정보를 노출시킨다. 그리고 이후 클라이언트가 자신의 quota를 초과하면, 에러 응답을 반환한다.  
  이를 더 수월하게 하기 위해 서비스는 quota와 관련된 메트릭을 제공하고, 클라이언트가 quota에 가깝게 사용량이  
  많아지면 경보를 발생시킨다. Amazon DynamoDB의 경우, table에 할당된 처리량을 나타내기 위한 Amazon  
  CloudWatch 메트릭을 제공하고, 특정 시간 동안의 사용량이 특정 임계값을 넘어서면 경보를 발생시킨다.

- 일부 API는 다른 API에 비해 처리량이 많이 소모될 수 있다. 이를 위해 서비스들은 값비싼 API들에 대해서는  
  각 클라이언트에게 더 적은 quota를 부여할 수 있다. 그리고 특정 연산을 처리하기 위한 비용을 미리 알 수 없는  
  경우도 있다. 예를 들어, 1KB의 단일 row를 반환하는 query는 1MB의 row를 반환하는 query보다 훨씬 더 적은  
  처리량을 소모할 것이다. 이를 방지하기 위해 pagination을 사용할 수 있지만, page size를 적절히 조절하는  
  것은 매우 어려운 문제이다. 이를 더욱 쉽게 해결하기 위해 일부 서비스들은 응답의 크기가 큰 요청을 여러 개의  
  요청을 수행한 것과 동일하게 계산한다. 한 가지 구현 방식으로, 처음에는 모든 요청을 가장 값싼 요청으로 계산하고,  
  API call이 완료되었을 때 실제로 사용된 처리량을 계산하도록 할 수 있다.

- Quota를 구현할 때 확장성도 고려해야 한다. 한 가지 예시로, client A가 1000TPS의 quota를 부여받았지만,  
  서비스가 10000 TPS를 감당할 수 있을 정도로 확장했고, 실시간 클라이언트별 TPS는 5000인 상황이라 해보자.  
  이때 만약 client A가 500TPS에서 3000TPS로 급증한다면 어떻게 해야할까? 확장성을 고려하지 않았다면  
  기존에 부여한대로 1000TPS만 허용하고, 나머지 2000TPS는 거부해야 할 것이다. 하지만 이렇게 하는 대신  
  트래픽을 허용하도록 할 수 있을 것이다. 그리고 만약 다른 클라이언트들이 자신의 quota를 많이 사용하게 되면,  
  client A의 "over quota" 요청들을 그제서야 거절하도록 할 수 있다.

- 이 상황을 나타내는 그림을 보자. 이전 그림과는 다르게 서비스는 클라이언트들에게 quota를 hard-allocate하는  
  대신, 허용량을 stacking한다. 이렇게 stacking하는 방식을 사용하면 특정 클라이언트가 다른 클라이언트가  
  활용하지 않는 capacity를 활용할 수 있다. 아래 그림에서는 orange, gray가 자신의 capacity를 모두  
  활용하고 있지 않기 때문에 blue가 soft-allocate된 capacity를 넘어 활용할 수 있다.  
  만약 이후 orange 또는 gray가 자신의 capacity 활용도를 높이게 된다면 이 트래픽이 먼저 처리되어  
  blue의 capacity는 감소하게 될 것이다.

  ![picture 3](/images/AWS_BL_MTSF_3.png)

- 이를 활용하는 사례를 보자. AWS에서는 사용 사례에 따른 트래픽 패턴을 고려해 quota의 유연성을 확보한다.  
  예를 들어, EC2 인스턴스와 해당 인스턴스에 연결된 EBS volume들은 인스턴스가 실행되는 초기 단계에 더  
  많은 처리량을 필요로 한다. 이러한 패턴을 고려해 EC2 인스턴스에 대한 quota를 인스턴스가 실행되는 초기 단계에는  
  더 많이 부여하도록 했다. 이렇게 함으로써 boot time을 줄이는 것과 동시에 서로 다른 workload에 장기적으로  
  형평성있게 리소스를 할당할 수 있게 되었다.

- 시간 또는 비즈니스가 성장함에 따라 증가하는 트래픽량에 따라 quota를 유연하게 가져갈 수도 있어야 한다.  
  예를 들어, 일부 서비스들은 성장과 비례해 quota를 증가시킨다. 하지만 일부 경우에는 클라이언트들이 고정된  
  quota를 요구할 수도 있는데, 예를 들어 비용 조절을 위한 quota 등이 있다. 이러한 경우, quota는 보호 메커니즘이  
  아니라 서비스의 기능으로써 노출되는 경우가 많다.

---

## Implementing layers of admission control

- 트래픽을 관리하고, load shedding과 rate-based quota를 구현한 시스템을 _admission control system_ 이라 한다.

- Amazon이 제공하는 많은 서비스들은 거절되는 수많은 요청들로부터 보호하기 위해 수십개의 admission control layer로  
  구성되어 있다. 일반적으로는 서비스 앞단에 Amazon API Gateway를 둬서, quota와 rate limiting의 일부를  
  위임한다. API Gateway만으로 수많은 트래픽을 처리할지 말지를 결정할 수 있기 때문에, 뒷단에 있는 서비스 server들은  
  영향을 받지 않은 채 실 트래픽을 처리할 수 있다. 여기에 더해 ALB, CloudFront, 그리고 WAF까지 활용해  
  admission control의 역할을 더욱 위임한다.

- Amazon은 이러한 admission control layer들을 수십년간 고도화해왔고, 여기서는 그 중 일부를 소개한다.

### Local admission control

- Admission control을 구현하는 일반적인 방법 중 하나는 token bucket 알고리즘을 활용하는 것이다.  
  Token bucket은 token들을 가지며, 요청이 수신될 때마다 하나의 token을 bucket에서 사용한다.  
  더 이상 token이 남아있지 않다면 요청은 거절되고, bucket은 빈 상태로 계속 남아있다.  
  설정된 rate에 따라 token이 bucket에 추가로 전달되고, 지정된 최대 용량만큼 쌓이게 된다.  
  이 최대 용량은 burst traffic에 의해 즉시 소모될 수 있기 때문에 burst capacity라고도 불린다.

- 이렇게 token이 즉시 모두 소모되는 것은 양날의 검이다. 균등하지 못한 트래픽에 어느정도 대처가 가능하지만  
  만약 traffic burst volume이 너무 크다면 rate limiting의 동작을 못하게 되기 때문이다.

- 이에 대한 대안으로 서로 다른 token bucket들이 함께 구성되도록 할 수 있다. 하나의 token bucket은  
  상대적으로 낮은 rate를 가지고 높은 burst capacity를 가지도록 하고, 다른 bucket은 rate는 높지만  
  burst capacity는 낮게 가지도록 하는 것이다. 첫 번째 bucket부터 확인하고 그 다음 bucket을  
  확인하는 식으로 구현하면 큰 burst까지 대처할 수 있다.

- Serverless architecture를 사용하지 않는 전통적인 서비스들에서는 특정 사용자로부터 발생한 요청이  
  얼마나 균등하게 서버들에게 전달되는지도 고려해야 한다. 만약 요청들이 균등하지 않다면 bursting capacity를  
  더 넉넉하게 잡고, 분산 admission control 기술들을 사용한다.

### Distributed admission control

- Local admission control은 로컬 리소스를 보호하기 위해 유용하지만, quota를 강요하는 것과 형평성을  
  지키기 위해서는 수평적으로 확장된 fleet 전반에 대한 admission control이 필요하다.

#### Computing rates locally and dividing the quota by the number of servers

- 이 접근법을 사용하면 서버들은 자신들이 받는 트래픽의 rate를 기반으로 admission control을 수행하지만,  
  각 key 별 quota를 트래픽을 감당하는 서버의 개수로 나눈다. 이 접근법은 트래픽이 서버들로 상대적으로 균등하게  
  배분된다는 가정 하에 진행된다. Load balancer가 round robin 방식으로 동작한다면, 이 전제는 대부분 참이다.

- 아래 그림은 하나의 load balancer를 사용해 트래픽이 서버들로 균등하게 배분하는 경우를 나타낸다.

  ![picture 4](/images/AWS_BL_MTSF_4.png)

- 트래픽의 고른 분산이라는 전제는 특정 fleet의 경우 참이 아닐 수 있다. 예를 들어 만약 load balancer가 요청을 분산하는 것이  
  아니라 connection을 분산하는 방식을 사용한다면, 적은 수의 connection을 가지는 클라이언트들은 동일한 서버와만 연결할  
  것이다. 이 경우, key별 quota가 충분히 크다면 문제가되지 않는다. 또한 load balancer를 여러 개 사용하는 굉장히 큰  
  fleet의 경우도 고려해야 한다. 이 경우, 클라이언트는 여러 개의 load balancer와 연결을 수립하고, 이 클라이언트의 요청을  
  처리하는 서버 인스턴스도 여러 개가 될 수 있다. 이 경우도 이전과 마찬가지로 quota가 충분히 크거나 클라이언트들이 자신에게  
  할당된 quota를 최대한 사용하는 경우가 없으면 문제가 되지 않는다.

- 아래 그림은 DNS caching 때문에 여러 개의 load balancer가 있어도 특정 클라이언트로부터의 요청이 고르게 분산되지  
  않는 경우의 모습을 나타낸다. 이 문제는 규모가 커지고 시간이 지남에 따라 클라이언트들이 connection을 열고 닫음에 따라  
  문제가 될 여지가 적다.

  ![picture 5](/images/AWS_BL_MTSF_5.png)

#### Using consistent hashing for distributed admission control

- 특정 서비스 관리자들은 Amazon ElastiCache for Redis fleet과 같이 별도의 fleet을 관리한다.  
  이때, 특정 throttle key에 안정 해시를 수행해 특정 rate tracker server로 트래픽을 배치하고, 해당 rate tracker가  
  local하게 admission control을 수행하게 할 수 있다. 이 접근법은 key의 cardinality가 높은 경우, 즉 key의 수가  
  많은 경우에도 잘 동작하는데, 이는 각 rate tracker는 자신이 처리할 key들에 대해서만 알아도 되기 때문이다.  
  하지만 기본 구현 방식을 사용하면 특정 throttle key를 사용한 요청량이 커지면 cache fleet에 대해 "hotspot" 현상을  
  발생시킬 수 있기 때문에, 특정 key의 요청량이 증가했을 때도 원활히 처리할 수 있도록 local admission control 시스템을  
  고도화해야 한다.

- 아래 그림은 data store에 대해 안정 해시를 사용하는 모습을 나타낸다. 트래픽이 균등하지 않더라도 cache와 같은 데이터베이스로  
  전달되는 트래픽을 안정 해시를 사용해 계산하는 것은 분산 admission control의 문제를 해결할 수 있다.  
  하지만 이 접근법은 시스템 규모가 커지면 문제가 발생할 수 있다.

  ![picture 6](/images/AWS_BL_MTSF_6.png)

#### Taking other approaches

- Amazon에서는 사용 사례에 따라 overhead, 정확도를 고려해 distributed admission control 시스템이 구현된다.  
  그리고 이들은 대부분 각 throttle key들의 관측된 rate를 fleet 내의 서버들이 공유하는 과정을 포함한다.  
  이 접근법에는 확장성, 정확성, 그리고 운영의 단순함 측면에서 많은 tradeoff가 발생한다.

- 아래 그림은 균등하지 못한 트래픽에 대응하기 위해 서버들이 비동기적으로 정보를 공유하는 모습을 나타낸다.  
  물론 이 방식도 나름의 확장성, 정확성 문제가 발생한다.

  ![picture 7](/images/AWS_BL_MTSF_7.png)

### Reactive admission control

- 예측할 수 없는 traffic spike에 대응하기 위한 수단으로 quota는 분명히 중요하지만, 서비스 또한 예측할 수 없는 workload를  
  원활히 처리할 수 있어야 한다. 예를 들어 클라이언트가 의도적으로 잘못된 요청을 보내거나, 예상한 것보다 처리하는 데 훨씬 더 오래  
  걸리는 workload를 보낼 수 있을 것이다. 이런 경우에 대응하고 유연성을 확보하기 위해 user-agent HTTP header, URI,  
  source IP 주소 등 요청의 다양한 부분을 확인하는 admission control system을 별도로 둘 수 있다.

- 추가적으로 rate limit 규칙은 빠르게 조절하고, 변경될 수 있어야 한다. 하나의 방법으로 process 시작 시 규칙 설정 파일을  
  메모리에서 불러오는 것을 생각할 수도 있지만, 이렇게 하면 규칙의 변경 사항을 빠르게 배포할 수 없다. 즉, 안정성을 고려하면서  
  동적으로 설정할 수 있는 방식을 사용해야 한다.

### Admission control of high cardinality dimensions

- 지금까지 살펴본 quota의 종류들 중 대부분의 경우, admission control system은 관측된 rate와 함께 quota 값들을  
  지속적으로 추적해야 한다. 예를 들어, 만약 한 서비스가 10개의 다른 애플리케이션에 의해 호출되었다면, admission control  
  system은 10개의 서로 다른 rate와 quota 값을 추적해야 한다. 이렇기에 요청의 cardinality가 높아질수록 admission  
  control 과정이 복잡해지게 된다. 예를 들어, 시스템은 전세계의 모든 IPv6 주소 각각에 대해 rate-based quota를  
  추적해야 하거나, DynamoDB table 내의 각 row에 대해 추적하거나, 아니면 S3 bucket 내의 각 object에 대한 quota를  
  추적해야 할 수 있다.

### Reacting to rate-exceeded responses

- 클라이언트가 rate 초과와 관련된 에러를 만난다면, 재시도를 하거나, 그냥 에러를 반환할 수 있다.  
  Amazon의 경우, 이 문제에 대해 동기 시스템과 비동기 시스템의 경우를 나누어 다르게 처리한다.

- 동기 시스템은 응답을 기다리는 무언가가 있기 때문에 빠르게 처리할 수 있어야 한다. 요청을 재시도해 성공할 수도 있겠지만,  
  rate 초과의 경우에는 재시도하는 것은 이미 과부하된 시스템에 추가적인 부하를 발생시키고 응답을 더 늦출 수 있게 된다.  
  일례로 AWS SDK는 `STANDARD` retry mode를 지정하면 에러가 자주 반환될 경우 자동으로 재시도를 중단한다.

- 많은 비동기 시스템은 이를 더 쉽게 처리할 수 있다. Rate 초과 관련 에러 응답을 받으면, 단순히 배압을 적용해  
  요청이 성공적으로 올 때까지 특정 시간 동안 처리 시간을 늦추기만 하면 된다. 일부 비동기 시스템은 주기적으로 동작하며  
  작업을 처리하는 데에 오랜 시간이 소요된다. 이러한 시스템들에 대해서는 최대한 빨리 실행하고, 일부 의존성을 가지는  
  시스템에서 병목 현상이 일어나면 배압을 적용해야 한다.

### Evaluating admission control accuracy

- 서비스를 보호하기 위해 사용하는 admission control 알고리즘이 무엇이든, 해당 알고리즘의 정확성은 꼭 측정해야 한다.  
  이를 위한 한 가지 방법은 throttle key와 해당 key의 rate 관련 로그를 request마다 쌓고, 로그 분석을 통해  
  fleet 단위에서의 각 throttle key의 RPS를 측정하는 것이다. 그리고 이 RPS를 설정한 rate limit과 비교한다.  
  이때 "true positve rate"(올바르게 거절된 요청들)과 "true negative rate"(올바르게 허용된 요청들),  
  "false positive rate"(잘못 거절된 요청들)과 "false negative rate"(잘못 허용된 요청들)을 측정한다.

---

## Architectural approaches to avoid quotas

- 이렇게 서버단의 가용성을 확보하고, 고객들을 보호하기 위해 admission control을 적용하는 것은 쉬워보일 수 있다.  
  하지만 어떻게 보면 quota라는 것이 고객들에게 불편하게 작용할 수 있다. Quota가 있다면 고객이 자신이 원하는 일을 수행하는 시간이  
  느려지게 된다. 형평성 메커니즘과 함께 고객들이 작업을 빠르게 처리할 수 있는 방법을 알아보자.

- AWS의 경우, 클라이언트가 자신의 rate-based quota를 넘어서지 않도록 방지하는 방식은 API가 control plane API인지,  
  data plane API인지에 따라 달라진다. Data plane API의 경우, 상대적으로 더 많이 호출되고, rate도 높게 가져간다.  
  예를 들어 S3 GetObject, DynamoDB GetItem, SQS ReceiveMessage API 등이 data plane API에 포함된다.  
  반면 control plane API는 data plane API의 사용량에 관계없이 때때로 호출되고, 호출량도 적은 API들을 의미한다.  
  예를 들어, S3 CreateBucket, DynamoDB DescribeTable, EC2 DescribeInstances API 등이 있다.

### Capacity management approaches to avoid exceeding quotas

- Data plane workload들은 유동적으로 변하기 때문에, service 도 마찬가지로 유동성을 가져야 한다.  
  서비스가 유동성을 가지기 위해서는 고객의 workload에 따라 기반 인프라도 함께 확장할 수 있어야 한다.  
  거기에 더해 고객들이 quota를 관리할 때도 유연성을 가질 수 있게 해야 한다. AWS의 경우, 아래의 다양한 기술들을 적용해  
  고객들이 quota를 관리하고 유연성을 확보할 수 있도록 지원한다.

  - Provison된 공간에 비해 활용도가 적은 부분이 있다면, 다른 부분이 해당 여유 공간을 사용하도록 한다.
  - Auto scaling을 구현해 비즈니스의 성장과 호출자의 rate limit을 증가시킨다.
  - 고객들이 자신의 limit에 얼마나 가까운지 알 수 있게 하고, 특정 임계치를 넘으면 알림을 받을 수 있게 한다.
  - 호출자들이 자신의 limit에 가까워지는 경우에 대한 모니터링을 집중적으로 한다.

### API design approaches to avoid exceeding quotas

- 위에서 말한 기술들은 control plane들에 대해서는 적용하기 어려울 수 있다. Control plane은 애초에 비교적 덜 많이 호출될  
  것임을 가정으로 하고 설계된 반면, data plane은 요청이 굉장히 많은 것을 가정하고 설계된다. 하지만 control plane의 경우에도  
  만약 고객이 수많은 리소스를 생성하고, 이들에 대해 관리, 감사, 기타 작업들을 수행해야 한다면 자신의 quota를 모두 사용하고  
  API rate limit을 마주할 수 있게 될 수도 있다. AWS는 이런 사례를 위해 rate-based quota를 모두 사용하지 않는 아래의  
  대안들을 제시한다.

#### Supporting a change stream

- 예를 들어, 일부 사용자들은 Amazon EC2 DescribeInstances API를 주기적으로 호출해 자신들이 관리하는 EC2 인스턴스들의  
  목록을 가져온다. 그리고 최근에 생성되거나 삭제된 인스턴스들을 식별한다. 이러한 고객들의 EC2 fleet이 커지면, API call은  
  더 횟수가 많아지고, 비싸지게 되고, 결과적으로 rate-based quota를 초과하게 된다. 이와 유사한 사용 사례의 경우,  
  AWS는 AWS CloudTrail이라는 서비스를 제공해 특정 연산의 change log를 제공함으로써 고객들이 주기적으로 API를 호출하지  
  않아도 되게 한다.

#### Exporting data to another place that supports higher call volumes

- 이 방식을 활용한 사례로는 S3 Inventory API가 있다. 일부 고객들은 S3 bucket 내에 무수히 많은 object들이 담겨 있으며,  
  이 bucket 내에서 특정 object들을 찾아내고 싶어한다. 이들은 처음에는 ListObjects API를 사용했다. 하지만 이렇게 하지  
  않고 더 높은 처리량을 달성하도록 하기 위해 S3는 특정 bucket 내의 object들의 목록을 JSON 형태로 직렬화해 다른 S3
  object로 비동기적으로 export 해주는 Inventory API를 만들었다. 즉, control plane API 대신 data plane API를  
  활용해 동일한 목적을 달성할 수 있게 한 것이다.

#### Adding a bulk API to support high volumes of writes

- 일부 고객들은 control plane이 관리하는 entity를 매우 많이 생성하거나 갱신하는 API를 호출하고 싶어한다.  
  일부 고객들은 API가 제공하는 rate limit을 준수할 의향이 있었지만, 이를 준수하기 위해 특정 작업을 하는 데에 드는 시간이  
  길어지는 것은 원하지 않았다. AWS IoT의 경우, 이 문제를 API 설계를 바꿈으로써 해결했다. 비동기적으로 동작하는  
  Bulk Provisioning API들을 제공함으로써, 고객들은 자신이 원하는 변경 사항들을 담은 파일들을 업로드하고, 서비스가 변경  
  사항들을 반영하고 나서 결과를 담은 파일들을 반환하게 했다. 이렇게 함으로써 고객들은 거대한 batch 작업들을 더욱 편리하게  
  수행할 수 있게 되었고, 부분 실패, 재시도 등에 대해 고민하지 않아도 되게 되었다.

#### Projecting control plane data into places where it needs to be commonly referenced

- EC2 DescribeInstances control plane API는 각 인스턴스의 network interface부터 block mapping 정보까지 거의 모든  
  metadata를 반환한다. 하지만 일부 metadata는 인스턴스에서 실행되는 코드에 의존적이다. 만약 인스턴스 개수가 많다면, DescribeInstances  
  API에 의해 각 인스턴스가 반환하는 정보도 거대해질 것이다. 만약 API가 rate 초과, 혹은 모종의 이유로 실패한다면 인스턴스에서  
  실행되고 있는 고객의 애플리케이션에도 문제를 끼치게 된다. 이를 해결하기 위해 EC2는 각 인스턴스에 대해 특정 인스턴스의 metadata를  
  조회할 수 있는 기능을 제공한다. 이렇게 control plane이 관리하는 데이터를 인스턴스단에서도 볼 수 있게 함으로써, 고객들의  
  애플리케이션들은 API rate limit에 다가가는 것을 피할 수 있게 된다.

### Admission control as a feature

- 일부 경우에는 무한한 유연성 대신 admission control을 더 선호하게 되는데, 주로 비용을 조절할 수 있기 때문이다.  
  일반적으로 서비스들은 거절된 요청들에 대해서는 과금하지 않는데, 요청이 거절되는 것은 드물게 발생하고 처리 비용이 싸기 때문이다.  
  예를 들어, AWS Lambda의 고객들은 lambda 함수의 동시 호출 가능 횟수를 제한시켜 비용을 조절하는 기능을 원했다.  
  고객들이 이런 기능을 원할 때는 limit을 손쉽게 API call로 바꾸고, 충분한 가시성과 알림 기능을 제공해야 한다.

---

## Conclusion

- Multi-tenant 서비스들은 리소스를 공유함으로써 인프라 비용을 낮추고, 운영 효율성을 높이게 해준다.  
  Amazon에서는 multi-tenant 시스템에 형평성을 보장함으로써 고객들에게 안정적인 성능과 가용성을 제공한다.

- 형평성을 구현하기 위해 service quota가 유용하게 사용될 수 있다. Rate-based quota는 예측 불가한 workload의 증가로  
  다른 서비스까지 영향을 끼치는 것을 막음으로써 웹 서비스를 더욱 안정적으로 제공할 수 있게 해준다. 하지만, rate-based quota는  
  고객의 사용 경험에 해를 끼칠 수 있다는 점에 유의해야 한다. 이를 위해 quota와 관련된 가시성 제공, burst sharing, 그리고  
  다른 방법들을 활용해 고객들이 자신의 quota를 넘지 않도록 하는 방법을 제공해야 한다.

- 분산 시스템에서 admission control을 구현하는 것은 꽤나 까다롭다. AWS의 경우 API Gateway는 throttling을 위한  
  여러 가지 방법들을 제공하고, WAF는 서비스 보호를 위한 layer를 제공하며 API Gateway와 Load Balancer와 연계할 수 있다.  
  DynamoDB는 각 index 레벨에서의 provisioned throughput을 제공함으로써 고객들이 서로 다른 workload들에 대해  
  처리량 요구사항을 격리할 수 있도록 한다. 이와 유사하게 AWS Lambda는 함수마다의 동시 호출 횟수를 제한할 수 있게 함으로써  
  서로 다른 workload들이 격리될 수 있게 한다.

- Amazon은 quota를 사용한 admission control을 안정적이고 예측 가능한 성능을 제공하는 시스템을 만들기 위한 중요한  
  요소로 생각한다. 하지만 admission control만으로는 불충분하다. 거기에 더해 auto scaling 등을 제공해 만약  
  의도하지 못한 load shedding이 발생하는 경우, 시스템이 auto scaling을 통해 자동으로 확장할 수 있게 한다.

- 표면적으로 봤을 때 비용과 workload의 격리 측면에서 서비스를 single-tenant하게 노출시키는 것과 multi-tenant하게  
  노출시키는 것에 tradeoff 관계가 있는 것처럼 보일 수 있다. 하지만 multi-tenant system에 형평성을 보장할 수 있다면,  
  고객들은 마치 single-tenant system을 사용하는 것처럼 느낄 수 있다.

---
