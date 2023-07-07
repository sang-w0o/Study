# Overload를 피하기 위해 load shedding 사용하기

> - [Amazon Builder's Library - Using load shedding to avoid overload](https://aws.amazon.com/builders-library/using-load-shedding-to-avoid-overload/)

## Introduction

- 저자는 Amazon의 Service Frameworks 팀에서 수년간 근무했다. 저자가 있던 팀은 Amazon Route 54, Elastic Load  
  Balancing 등의 팀들이 서비스를 더욱 빠르게 개발하고, 서비스 클라이언트가 다른 서비스를 더욱 쉽게 호출하도록 돕는 도구를  
  개발하고 있었다. 거기에 더해 다른 팀이 제공하는 수치 측정, 인증, 모니터링, 클라이언트 라이브러리 생성 등의 다양한 설정을  
  손쉽게 많은 팀들이 사용하도록 하는 기능도 개발했다.

- 이때 저자가 맞이한 한 가지 문제점은 성능과 가용성과 관련된 기본적인 수치를 어떻게 제공하느냐에 대한 것이었다.  
  예를 들어, 저자의 팀은 API call의 latency 특성을 알 수 없었기 때문에 클라이언트단의 기본 timeout을 마음대로  
  설정할 수 없었다.

- 이를 해결하는 도중, 각 서비스가 클라이언트들에게 동일한 시점에 맺을 수 있는 connection의 개수를 결정해야하는 문제점이  
  발생했다. 이 설정은 서버에 과부하가 걸리거나, overloading되는 것을 방지하기 위해 필요했다. 더 자세히 말하자면,  
  load balancer의 최대 connection 개수에 알맞게 서버의 max connection을 설정하고 싶어했다.  
  이 당시에는 Elastic Load Balancing이 출시되기 전이었기 때문에, 하드웨어단의 load balancing이 널리 사용되고 있었다.

- Max connection을 결정하는 것은 매우 어려운 문제였다. Max connection이 너무 낮게 설정되어 있으면 서비스는 충분히  
  처리할 수 있음에도 불구하고 load balancer 단에서 요청을 끊어버리게 되고, 반대로 너무 높게 설정되어 있으면 서버가  
  느려지고 응답 불가 상태가 되기 때문이다.

- 이에 대한 대안으로 저자의 팀은 load shedding 기법을 적용했고, 결과적으로 훌륭하게 문제를 해결할 수 있었다.

---

## The anatomy of overload

- Amazon은 시스템이 overloading되기 전 미리 확장할 수 있도록 시스템을 설계한다. 하지만 시스템을 보호한다는 것은 곧  
  protection layer들이 추가될 것임을 나타낸다. 이 과정은 자동 확장으로부터 시작해 우아하게 excess load shedding을  
  하고, 이 메커니즘을 모니터링하고, 지속적으로 테스트하는 과정을 포함한다.

- 부하 테스트를 하면 시스템 활용도에 비례해 latency가 높아지는 경우가 많다. 부하가 많은 상황에서는 thread 경쟁, context  
  switching, GC, 그리고 I/O 경쟁 등의 문제점들이 더욱 명확하게 드러난다. 결과적으로 서비스들은 성능이 더욱 가파르게  
  나빠지는 inflection point를 지나게 된다.

- 이러한 관측의 뒤에는 Universal Scalability Law라는 이론이 있다. 이 이론은 시스템의 처리량이 동시성을 활용하면  
  증가할 수 있지만, 결과적으로는 직렬화할 수 밖에 없는 작업들에 의해 한계가 있음을 나타낸다.

- 불행히도 처리량은 시스템의 자원 한계 뿐만 아니라 시스템의 부하에 의해 더 나빠질 수 있다. 즉, 만약 시스템에 주어진  
  자원이 처리할 수 있는 처리량보다 더 많은 작업을 해야 한다면, 느려진다. 컴퓨터는 과부하된 상태에도 작업을 지속할 수는 있지만,  
  상당 수의 시간을 context switching에 소모하게 되고, 결국 매우 느려지게 된다.

- 클라이언트가 서버와 상호작용하는 분산 시스템에서 일반적으로 클라이언트는 timeout을 두게 되는데, 즉 서버로부터 일정 시간이  
  지날 때까지 응답이 오지 않으면 서버로부터의 응답 대기를 중지하게 된다. 서버에 과부하가 발생해 latency가 클라이언트의  
  timeout보다 길어지게 되면, 요청들은 실패하게 된다. 아래 그래프는 TPS가 증가함에 따라 inflection point를 지나면  
  처리량이 급격히 떨어지면서 latency가 증가하는 모습을 나타낸다.

  ![picture 1](/images/AWS_BL_ULSO_1.png)

- 위 그래프에서 클라이언트의 timeout보다 응답 시간이 길어지면 상황이 나쁜 것임은 알 수 있지만, 정확히 어떻게 얼마나 나쁜지는  
  표현되어 있지 않다. 이를 표현하기 위해 클라이언트 측에서의 가용성과 latency를 함께 표현한 아래 그래프를 살펴보자.  
  일반적인 response time 단위 대신, median response time을 사용했다. Median response time은 전체 요청의  
  50%가 해당 시간 이내에 응답을 받았음을 의미한다. 만약 서버의 median latency가 클라이언트의 timeout과 동일하다면  
  절반 가량의 요청은 timeout이 발생했을 것이고, 클라이언트가 봤을 때의 가용성은 50%가 된다. 즉, latency의 증가가  
  가용성에도 영향을 끼칠 수 있음을 보여준다.

  ![picture 2](/images/AWS_BL_ULSO_2.png)

- 위 그래프는 해석하기 어려울 수 있다. 이를 더 명료하게 표현하기 위해 _처리량(throughput)_ 에서 _goodput_ 을 별도로  
  나타내보자. 처리량(throughput)은 서버에게 전달되는 초당 요청의 개수를 나타내고, goodput은 에러 없이, 적절한 latency  
  이내에 처리된 요청들을 나타낸다.

  ![picture 3](/images/AWS_BL_ULSO_3.png)

### Positive feedback loops

- 과부하된 상태에서 문제는 계속해서 악화된다. 먼저 클라이언트가 timeout을 맞았다는 것은 이미 서버 상태가 좋지 못하다는 것을  
  나타낸다. 거기에 더해 서버가 요청을 처리하기 위해 수행하던 작업들이 낭비된다.

- 문제를 더욱 악화하는 것은 클라이언트들이 종종 요청을 재시도한다는 것이다. 이는 시스템에 부하를 훨씬 더 많이 발생시키게 된다.  
  만약 SoA 구조에서 call graph가 굉장히 크고, 각 layer가 자신만의 재시도를 수행한다면, 가장 하단의 layer에서 발생한  
  부하는 재시도를 다른 layer들로 전이시켜 부하를 기하 급수적으로 증가시킨다.

### Preventing work from going to waste

- 표면적으로 봤을 때 load shedding은 매우 단순하다. 서버가 과부하 상태에 도달하게 되면, 추가적인 요청을 거절함으로써  
  처리하기로 한 요청들에 대해서만 집중할 수 있어야 한다. Load shedding의 목표는 서버가 처리하기로 결정한 요청들의  
  latency를 낮게 유지해 클라이언트 timeout이 발생하지 않도록 하는 것이다. 이 접근법을 사용하면 서버는 수락한 요청들에  
  대한 가용성을 높게 유지할 수 있고, 오직 초과된 요청들의 가용성만 낮아지게 된다.

  ![picture 4](/images/AWS_BL_ULSO_4.png)

- 과부하된 요청을 shedding하고 수락한 요청들의 latency를 낮게 유지하는 것은 시스템의 가용성을 높일 수 있다.  
  하지만 이 방식의 장점을 위 그래프만으로는 제대로 파악할 수 없다. Overall availability는 여전히 아래로 내려가고,  
  이는 나쁘게 보이기 때문이다. 하지만 요점은 서버가 수락한 요청들이 빠르게 처리되기 때문에 높은 가용성을 유지할 수 있다는 것이다.

- Load shedding은 goodput을 유지하고 처리할 수 있는 요청을 최대한 많이 처리할 수 있게 해주며, 이는 서버가 제공하는  
  기본 처리량이 증가하더라도 동일하다. 하지만 load shedding을 수행하는 것 자체도 비용이 들기 때문에, 결과적으로  
  load shedding을 사용하더라도 트래픽이 매우 많아지면 goodput도 함께 낮아진다.

  ![picture 5](/images/AWS_BL_ULSO_5.png)

---

## Testing

- 부하 테스트에는 상당한 시간을 써야 한다. 서비스가 장애가 날 정도로, 그리고 장애를 내고도 남을 정도로 테스트해야 한다.  
  그리고 이를 토대로 위에서 본 그래프와 같은 지표를 그리게 되면 과부하 상태의 성능과 이후 개선점들을 파악할 수 있게 된다.

- 부하 테스트에는 다양한 종류가 있다. 어떤 부하 테스트들은 부하가 증가함에 따라 fleet이 자동으로 확장함을 검증한다.  
  만약 부하 테스트 결과 트래픽이 증가함에 따라 서비스의 가용성이 0에 수렴하게 된다면, 추가적인 load shedding  
  메커니즘을 도입해볼 필요가 있다. 가장 이상적인 부하 테스트 결과는 서비스의 자원 활용도가 최대치에 도달하거나  
  트래픽이 급증해도 안정적으로 서비스가 동작하는 것이다.

- Chaos monkey와 같은 도구들은 서비스를 대상으로 카오스 엔지니어링 테스트를 하도록 도와준다.  
  예를 들어, CPU 과부하를 유발하거나 packet 손실을 유발하는 등 과부하 상황에서 발생할 수 있는 문제점들을 발생시킬  
  수 있다. 또다른 테스트 방식으로 점진적으로 트래픽을 증가시키는 대신, 트래픽은 일정하게 보내면서 서버 개수를 하나씩  
  줄이는 방식이 있다. 이렇게 하면 서버 인스턴스마다의 처리량이 증가하기 때문에, 실질적으로 하나의 서버 인스턴스가  
  처리할 수 있는 트래픽을 알아낼 수 있다. 마지막으로 완전한 end-to-end 부하 테스트는 요청을 처리하기 위해 서비스가  
  의존하는 데이터베이스 등의 다양한 컴포넌트들도 포함하기 때문에 병목 지점들을 알아낼 수 있게 한다.

- 테스트를 할 때는 서버단의 가용성과 latency에 더해 클라이언트 기준에서의 가용성과 latency도 함께 측정해야 한다.  
  클라이언트 측에서 바라본 서버의 가용성이 감소하면 부하를 더욱 증가시켜보자. 만약 load shedding이 동작하고 있다면  
  트래픽이 서비스가 제공하는 처리량보다 훨씬 더 많아지더라도 goodput은 안정적인 수치를 유지할 것이다.

- 과부하를 피할 수 있는 메커니즘을 알아보기 전 부하테스트가 먼저 선행되어야 한다. 먼저 테스트를 함으로써 병목 지점을  
  발견하고, 어떤 보호 메커니즘을 적용해야 하는지 알 수 있기 때문이다.

---

## Visibility

- AWS는 부하로부터 보호하기 위해 어떤 기술을 사용하는지와 무관하게 메트릭과 가시성을 충분히 확보하고, 관찰함으로써 어떤 기술이 적합한지를 지속적으로 검사한다.

- 만약 잘못된 보호 메커니즘이 요청을 거절하면, 거절된 요청들에 의해 서비스의 가용성이 저하된다.  
  만약 서비스가 처리할 수 있는 리소스가 충분히 있음에도 불구하고 max connection이 너무 낮게 설정되는 등의 이유로 요청을 거절하게 되면  
  false positive 수치가 증가하게 된다. 이 false positive 수치는 0이어야 한다. 만약 이 수치가 주기적으로 0이 아닌 값을  
  가지게 된다면, 서비스가 너무 민감하게 tuning되어 있지 않은지, 지속적으로 과부하되는 인스턴스가 있는지, 확장 또는 load balancing에  
  문제가 있는지 등을 확인해야 한다. 이와 같은 경우, 애플리케이션 성능 튜닝을 해야하거나 더 큰 인스턴스 타입으로 바꿔  
  부하를 더 우아하게 처리할 수 있도록 해야 한다.

- 가시성의 경우, 만약 load shedding이 발생해 요청들이 거절되면 어떤 클라이언트의 요청이었는지, 어떤 작업을 호출했는지, 그리고 이 외에  
  보호 메커니즘을 튜닝하는 데에 도움이 될만한 정보들을 항상 함께 확인할 수 있어야 한다. 또한 load shedding에 의해 거절된 트래픽의  
  양도 항상 모니터링해야 한다. 만약 문제가 있다면 우선적으로 capacity를 추가하고 현재 병목 지점을 해결해야 한다.

In terms of visibility, when load shedding rejects requests, we make sure that we have proper instrumentation to know who the client was, which operation they were calling, and any other information that will help us tune our protection measures. We also use alarms to detect whether the countermeasures are rejecting any significant volume of traffic. When there is a brownout, our priority is to add capacity and to address the current bottleneck.

- Load shedding에는 가시성과 관련해 미묘하지만, 중요한 고려 사항이 하나 더 있다. 실패한 요청들의 latency가 서비스가 실제로 처리하는  
  요청들의 latency 관련 메트릭에 영향을 미치지 않도록 해야한다. 요청을 거절하는 latency는 실제로 처리되는 요청들의 latency에 비해  
  현저히 낮다는 것은 자명하다. 예를 들어, 만약 서비스가 받는 트래픽의 60%가 load shedding되어 거절된다면, 서비스가 실제로  
  처리하는 요청들의 latency는 매우 높더라도 median latency는 꽤나 좋아보일 수도 있다.

### Load shedding effects on automatic scaling and AZ failure

- 설정이 잘못된 경우, load shedding에 의해 auto scaling이 동작하지 않게될 수 있다. 다음 예시를 살펴보자.

  - 서비스에 CPU 사용량 기반의 auto scaling 정책이 적용되어 있고, 그와 동시에 비슷한 CPU 사용량에 도달했을 때 트래픽을 거절하는  
    load shedding도 함께 설정되어 있다.

- 위의 경우 load shedding 시스템에 의해 요청이 거절되면서 CPU 부하는 낮게 유지될 것이고, 결과적으로 CPU 사용량 기반의  
  auto scaling 정책은 동작할 일이 없을 것이다.

- AZ 단위 장애에 대응하기 위한 auto scaling 정책을 세울 때도 load shedding 로직을 고려해야 한다.  
  서비스는 우리가 설정한 latency를 지키기 위해 AZ가 제공하는 capacity를 모두 채울만큼 확장할 수 있다.  
  AWS에서는 CPU와 같은 시스템 메트릭들을 살펴보면서 서비스가 capacity limit에 얼만큼 다가가는지를 지속적으로 모니터링한다.  
  하지만 load shedding이 있다면 시스템 메트릭은 충분히 낮은데 요청이 거절되는 상황이 발생할 수 있으며, 결국  
  AZ 장애를 위해 마련된 auto scaling 정책이 적용되지 않게될 수 있다. 따라서 load shedding을 사용하는 경우, fleet의  
  capacity와 여유 공간이 항상 어떤 상태인지를 알 수 있도록 테스트를 더욱 정확히 진행해야 한다.

- 사실 치명적이지 않고 순간적으로 발생하는 대용량 트래픽에 대응해 비용을 아끼기 위해 load shedding을 사용할 수도 있다.  
  예를 들어, `amazon.com`의 웹사이트 트래픽을 감당하는 fleet이 있다고 해보자. 이때, 크롤러에 의해 수행되는 검색 트래픽의 경우에는 확장을  
  할 때 고려할 요소가 아닐 가능성이 높다. 하지만 이런 결정을 할 때는 굉장히 조심해야 한다. 모든 요청을 처리하는 데 드는 비용이 절대 동일하지  
  않고, 사람이 만들어내는 트래픽과 크롤러가 발생시키는 트래픽을 함께 고려해 AZ 단위의 가용성을 확보하는 것은 고도화된 설계, 지속적인 테스트  
  등이 필요하다. 그리고 만약 서비스의 클라이언트들이 서비스가 이렇게 구성된 것을 모른다면, AZ 장애가 발생했을 때 load shedding이 발생하는  
  것이 아니라 AZ 장애에 대처하지 못한 것처럼 보여 가용성이 떨어진 것처럼 느끼게 될 수 있다.  
  이러한 이유들 때문에 SoA의 경우, 이런 설계를 최대한 빠르게 시스템에 녹여넣어야 한다.

> 잘못된 설계를 한다면 AZ 단위의 장애 감래는 성공했지만, load shedding에 의해 사용자의 요청이 실패한다는 말이다.  
> 그리고 이를 사용자는 AZ 단위 장애에 대한 대처를 하지 못한 것으로 인지하고, 가용성이 떨어진다고 생각한다는 것이다.

---
