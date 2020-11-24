<h1>AWS Servless, CI/CD Immersion Day</h1>

<h2>Modern Application Architecture</h2>

* 예전에는 큰 하나의 application에 많은 팀이 붙어서 개발을 하는 식이었다면, 2002년도를 기준으로는   
  각각의 서비스를 마이크로 서비스 단위로 분할하고, 각 서비스를 2 Pizza Teams로 분리해서 개발을 하게 된다.
  * 개발 및 운영에 있어서 보다 빠른 도입, 혁신을 위해 구조를 변경했다고 알려져 있다.

* 민첩한 개발과 운영 환경을 위한 `Two Pizza Team`
  * 두 판의 피자를 먹을 사람의 수만으로 팀을 구성한다.
  * 갖고 있는 서비스도 그만큼 작게 모듈화한다.
  * 한 팀이 맡은 서비스를 모듈로 취급해 개발, 운영하는데에 전념한다.
  * 혁신에 집중한다.

* 전통적인 모놀리틱 아키텍쳐
  * 개발 관점에서의 어려움
  * 운영적인 측면에서의 어려움
  * 유연하지 못한 리소스 관리
  * 비용 관리에 있어서의 비효율성 존재
  * 한 모듈에 변화가 생겼을 때, 배포를 아예 다시 해야하는 경우가 발생할 수도 있다.

* 마이크로서비스 아키텍쳐(MSA, Micro Service Architecture)
  * 모놀리틱 아키텍쳐를 각각의 서비스 모듈로 분리하고, 각 서비스별로 책임을 부여하며 API로 통신을 하게 된다.
  * 이렇게 각 모듈들 간의 decoupling을 하여 의존성을 최소화할 수 있다.
  * 각 서비스별로 올라가는 인프라의 관리가 수월해진다.
  * 로그 담당 서버, App서버(IO 잦음)를 분리함으로써 리소스 관리, CI/CD에 있어서는 각 모듈만 배포하면 되는 효율성이 있다.

* MSA 기반의 Modern Architecture Pattern
  * 각 서비스들이 모듈 단위로 decoupling되어 있고, 서로 서로는 API를 통해 통신한다.
  * 3계층으로 이루어진다. --> `Presentation` --> `Business Logic` --> `Data`
  * 모놀리틱 아키텍쳐와의 차이는 각 계층이 __API로 통신__ 한다는 것이다.

* MSA 도입 시에 고려할 점 ==> __관리__
  * 서비스 모듈들이 쪼개지다 보니, 인프라도 각각 쪼개지며, 각 인프라에 대한 사이클 관리가 별도로 돌아가면서 관리 또한 별도로 해야 한다.

<hr/>

<h2>Why Serverless</h2>

* 서버 관리의 필요성이 없다.
* 사용한 만큼만 비용을 지불하면 된다. ==> Request에 비례하여 비용 발생
* 유연하고 자동화된 스케일링이 가능하다.
* 고가용성!

* __비즈니스 로직에 좀 더 집중하고 마이크로서비스 아키텍쳐에 최적화할 수 있다.__

<h3>AWS Lambda</h3>

* Amazon EC2 : 사이즈 조절이 가능한 Cloud 상의 가상 서버
* Amazon ECS : EC2에서 실행하는 Docker를 실행하는 Container 관리 서비스
* AWS Lambda : Serverless Computing, event에 대한 응답으로 code 실행

* AWS Lambda - Overview
  * 서버 인프라가 아닌 코드 기반으로 쉽게 비즈니스 로직을 처리할 수 있는 서비스를 생성
  * 대부분의 Use-Case를 Serverless 기반으로 Modernize한다.
  * Lambda 하나가 서비스 하나라고 생각해도 무방하다.

* Lambda 사용 시의 이점

  1. 관리 대상 서버가 없다 : Lambda는 서버를 별도로 프로비저닝하거나 관리할 필요 없이, 코드를 자동으로 실행한다.   
     단지 코드를 작성하고 Lambda에 업로드하면 된다.
    
  2. 연속적인 확장성 : Lambda는 각 trigger에 대한 응답으로 코드를 실행하며, app을 자동으로 확장한다.   
     코드는 병렬로 실행되며 각 trigger를 개별적으로 처리하여 작업 부하의 크기를 정확하게 조정한다.

  3. 비용 효율적인 컴퓨팅 : Lambda 코드가 실행되는 단위 시간(100ms) 및 Lambda 코드가 실행된 횟수에 대해 과금된다.   
     코드가 실행되지 않을 때에는 비용을 지불하지 않아도 된다.

* AWS Lambda의 동작 방식

  1. Bring your own code and libraries.

  2. Simple resource model
    * 128MB ~ 3008MB까지 64MB 단위로 메모리 선택
    * RAM에 비례하여 CPU 및 네트워크 할당
    * 효율적으로 리소스 사용 가능

  3. 유연한 호출 경로
    * Request-Response 또는 Event 호출 옵션
    * 예를 들어, 10개의 request가 한번에 들어오면, thread 단위가 아니라, 각 request를 위한 lambda 함수가 호출된다.
    * 다양한 AWS 서비스와 통합

  4. 세분화된 권한
    * IAM 역할을 이용한 Lambda 실행 권한 제어
    * AWS 이벤트 소스에 대한 리소스 정책 사용

* Lambda 함수 호출 방식

  * 비동기(Event) : Amazon API Gateway ==> Client의 호출 ==> Lambda Function 실행
  * 동기(Push) : Amazon SNS & S3 ==> 호출 ==> Lambda Function 실행
  * 스트림(Poll-based) : Amazon DynamoDB & Amazon Kinesis ==> 변경 발생 ==> AWS Lambda Service ==> Lambda Function 실행

* 비용 효율적인 리소스 사용
  * Free Tier : 월별 100만건의 무료 요청 및 400,000GB 초의 컴퓨팅 시간 포함
  * 유휴 시간에 대한 비용이 청구되지 않는다.
  * 사용한 만큼만 비용이 청구된다.
  * 함수 요청 수와 함수 시간 당 사용한 메모리에 대해 요금이 청구된다.

* Use cases
  * Data Processing : 데이터 변경, 시스템 상태 변경 또는 사용자 작업에 대한 응답으로 코드 실행
  * Backends : 백엔드 로직을 실행하여 웹, 모바일, IoT 및 3rd API에 대한 요청 처리
  * Control Systems : AWS 내으 상태 및 데이터 변경에 대한 응답 및 Workflow 처리

<h3>Amazon API Gateway</h3>

* "API는 MSA를 위한 효과적인 진입점" 이다.
* MSA의 decoupling된 모듈(서비스)들 사이의 통신을 위해 사용한다.

* API Gateway 사용에 있어서의 Pain Point
  * API의 여러 버전 및 stage 관리가 어렵다.
  * 3rd party 개발자의 액세스 모니터링에는 많은 시간이 필요하다.
  * 액세스 권한 부여는 어렵다.
  * 트래픽 급증으로 운영 부담이 발생한다.
  * 관리할 서버가 없어진다면?

* Amazon API Gateway 사용 이점
  1. 자동으로 스케일링 되고, 사용한만큼 비용을 지불하는 완전 관리형 서비스이다.
  2. Restful API와 WebSocket API를 지원한다.
  3. HTTP Endpoint 및 Lambda 함수와 연결 구성이 가능하다.
  4. VPC에서만 접근 가능한 Private 연결이 가능하다.
  5. 보안 요건에 따른 규정 준수 및 Security Features 제공
  6. Swagger 지원 및 Canary 배포 지원

* Amazon API Gateway Features
  * API의 여러 버전 및 단계를 호스팅할 수 있다.
  * 3rd party 개발자에게 API Key 생성 및 배포 기능 지원
  * 백엔드 보호를 위한 요청 조절(Throttle) 및 모니터링 지원
  * 백엔드로 Lambda 호출 가능
  * AWS WAF와 연동하여 악성 웹 트래픽에 대한 보호 가능
  * Caching도 가능

<hr/>

<h2>Application in Serverless Architecture</h2>

<h3>Serverless Databases</h3>

* Amazon Aurora Serverless
  * App의 필요에 따라 관계형 데이터베이스 Capacity를 제공하는 서비스
  * DB 운영 및 관리의 부담 없이 필요에 따라 용량 조절이 가능하다.
  * Aurora의 고가용 스토리지를 똑같이 이용한다.
  * 가변성이 높고 예측 불가능한 워크로드에 효율적이다.
  * 사용한 DB 리소스에 대해서만 요금이 청구된다.

* Amazon RDS Proxy
  * 관계형 DB의 연결을 효과적으로 관리하는 Database Proxy
  * RDS 및 Aurora에 적합한 완전 관리형의 고가용 DB Proxy
  * Connection Pool을 활용한 데이터베이스 연결 관리
  * 장애 조치 시간을 최대 79%까지 단축 가능(Aurora)
  * Serverless 환경에서의 RDB 연결을 효과적으로 관리

* Amazon DynamoDB
  * 어떤 규모에서도 10ms 미만의 성능을 제공하는 Key-Value 데이터베이스
  * 서버리스 완전관리형 NoSQL DB를 제공
  * 자체적으로 뛰어난 가용성과 내결함성 제공
  * 글로벌 테이블을 통한 데이터 Replication 제공
<hr/>

<h2>Serverless Orchestration</h2>

* Lambda 함수가 실제 서비스를 구성하게 되면 굉장히 많아질 수 있다. 즉, 관리가 어려워질 수도 있다.

* AWS Step Functions
  * 자동화된 Serverless Workflow 구축을 지원하는 Serverless Orchestrator
  * 그래픽 콘솔을 통한 application 구성 요소 시각화
  * 상태 머신을 시각적으로 확인하고 실행의 모니터링이 가능하다.
  * 여러 AWS 서비스와 연동하여 자동화된 Workflow 구축이 가능하다.
  * 여러 Serverless 함수들과 혼합하여 Serverless Workflow 생성이 가능하다.

  * 시각화된 서비스 Workflow 구성
    1. Define in JSON
    2. Visualize in the Console
    3. Monitor Executions

* Amazon SQS
  * Application Integration에 뛰어난 버퍼 기반의 완전 관리형 Message Queue
  * 뛰어난 확장성과 QoS를 제공하는 완전 관리형 Message 서비스
  * 메시지를 안정적으로 전달하며, 데이터의 암호화를 제공한다.
  * 분산형 시스템에서 데이터 전달을 위한 Queue로 활용이 가능하다.
  * 사용한 만큼만 요금을 지불하는 Serverless 기반의 비용 모델
  * 비동기 요청에 많이 사용된다.

<hr/>

<h2>Serverless Event Driven</h2>

* Amazon EventBridge
  * 자체 App, SaaS, AWS 서비스의 데이터를 연결하는 Serverless Event Bus
  * 코드 작성 없이 데이터 변경 사항에 대해 실시간 Access를 제공하는 Serverless Event Bus
  * 다양한 이벤트 소스에 대해 중앙집중식 이벤트 라우팅을 제공할 수 있다.
  * 상태에 대해 지속적으로 체크하는 대신 이벤트 기반의 아키텍쳐 구성이 가능하다.
  * 이벤트 개수에 대해 자동으로 확장하며, 내결함성 및 가용성 제공

<hr/>

<h2>Starting with Serverless</h2>

* Serverless Application Model
  * Serverless Application Build를 위한 오픈소스 프레임워크
  * Serverless Infra 생성 자동화를 위한 템플릿
  * 기존 CloudFormation Template와 연동
  * 단일 스택 상에서 연관된 컴포넌트 및 리소스 구성을 쉽게 제공할 수 있다.
  * YAML 또는 JSON 포맷으로 쉽게 생성할 수 있다.

* AWS Cloud9
  * 브라우저 만으로 코드를 작성, 실행 및 디버깅할 수 있는 클라우드 기반의 IDE
  * AWS 서비스 연동과 현업을 손쉽게 구성할 수 있다.
  * Local IDE 구성 없이도 풍부한 개발 환경 구축이 가능하다.
  * 손쉽게 Serverless Application 구성이 가능하다.
  * 빠르게 일관된 형태의 개발 환경 구축이 가능하다.
  * Serverless 황경의 간편한 디버깅 및 AWS 서비스들과의 연동이 가능하다.

* Serverless Application Repository
  * Serverless Application을 빠르게 찾고 게시할 수 있는 저장소
  * 미리 만들어진 다양한 Serverless Application을 찾고 배포할 수 있다.
  * 공개/비공개로 Serverless Application의 공유가 가능하다.
  * 쉽게 Serverless Application을 시작하고, 생태계를 확장시킬 수 있다.
  * 찾거나 배포하고 게시하는데에 비용이 들지 않는다.


* Serverless Architecture Summary
  * 운영 : 운영 및 유지 관리에 대한 비용 최소화
  * 안정성 : 안정적인 관리형 Serverless 기반 서비스들의 사용
  * 보안 : 관리형 서비스의 보안 제공
  * 성능 : 인프라의 효과적인 사용을 통한 성능 최적화 가능
  * 비용 : 비용 효율적인 리소스 구성을 통한 아키텍쳐 개선

<hr/>

<h2>Hands On Lab</h2>

<hr/>