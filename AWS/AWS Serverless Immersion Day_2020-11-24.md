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

* 

<hr/>

<h2>Application in Serverless Architecture</h2>

<hr/>

<h2>Starting with Serverless</h2>

<hr/>

<h2>Hands On Lab</h2>

<hr/>