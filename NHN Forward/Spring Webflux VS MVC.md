<h1>내가 만든 Spring Webflux가 느린 이유</h1>

<h2>Spring MVC와 Spring Webflux의 Thread Model</h2>

<h3>Spring MVC에 대한 고찰</h3>

* Spring MVC는 Thread per Request Model로 구현되어 있다.   
  Servlet Spec 3.1 이상으로 구성되어 있는  Servlet Container, 즉 WAS로 구동된다.   
  이 Servlet Container는 Thread Per Request로 서비스를 진행한다.   
  지금 브라우저가 서버로 Request를 보내면, 서버는 해당 Request를 처리하기 위해서   
  Thread Pool에 있는 Thread 하나를 뽑아서 할당한다. 할당된 Thread는 요청을 받고 응답할 때까지   
  모든 처리를 담당하게 된다. 그리고 클라이언트에게 응답을 맘치면 할당된 Thread는 다시 Thread Pool로   
  반환된다. 참고로 Spring Boot에서 제공하는 Tomcat 내장 서버의 경우에는 ThreadPool의   
  Thread개수가 200개이다.

* 분산 시스템으로 설계된 서비는 API 서버가 아닌 다른 API 서버의 REST API를 호출하여   
  데이터를 통합하는 경우가 매우 흔하다. Spring MVC는 위에서 말한 바와 같이 Thread Per Request Model을   
  사용하고 있으므로 Thread하나가 모든 일을 처리한다. 이 말은 곧 다른 서버의 API 호출에 대한 응답이 오기   
  전까지 해당 Thread는 Blocking되어 있다는 것이다. 이때 만약 타 서버의 데이터 처리 시간이 길어지면   
  우리 Thread의 blocking시간 또한 길어지게 되는 것이다. Blocking 시간이 길어지면 해당 Request를   
  처리하는 Thread 는 Network I/O가 끝나기 전까지는 Waiting 상태가 된다.   
  그 후 Network I/O가 끝나면 추후 작업을 처리하기 위해 Runnable 상태로 변경된다.

* 위의 과정들은 시스템 부하가 적은 환경에서는 큰 문제가 되지 않는다. 하지만 시스템 부하가 높은 환경에서   
  Thread 상태가 Runnable에서 Waiting으로 바뀌는 것, 즉 Context Switching이 되고 Thread의 데이터가   
  계속해서 로딩하는 오버헤드가 문제가 된다. 또한 Tomcat의 200개 Thread가 얼마 되지 않는 CPU Core를   
  점유하기 위해서 Thread들이 경합하는 현상도 발생한다. 일반적으로 CPU Core는 2, 4, 8개 중 하나인데,   
  200개의 Thread가 8개의 Core를 점유하기 위해서 경합한다면 이또한 큰 부하가 될 것이다.

<h3>Spring WebFlux</h3>

* Spring WebFlux는 Event Loop Model로 동작한다.   
  사용자들의 요청이나 애플리케이션 내부에서 처리해야되는 작업들은 모두 Event라는 단위로 관리되고,   
  Event Queue에적재되어 순서대로 처리되는 구조이다.   
  그리고 Event로 처리하는 ThreadPool이 존재한다. 이 ThreadPool은 순차적으로 Event를   
  처리한다고 해서 Event Loop라고 부르기도 한다. Event Loop는 Event Queue에서 Event를 뽑아서   
  하나씩 처리한다.

* Spring WebFlux는 리액터 라이브러리와 Netty를 기반으로 동작한다.   
  Netty는 Tomcat과 달리 ThreadPool의 Thread개수가 머신 Core의 두 배이다.   
  Spring WebFlux는 Spring MVC처럼 Thread가 Block되어 Network I/O가 끝날 때까지 Thread가 Waiting하는 대신,   
  I/O가 시작되기 전 작업도 Event I/O가 종료되면 처리할 작업도 Event로 만들어져 Queue에 들어간다.   
  NIO를 이용하여 I/O 작업 처리를 하기 때문에 Thread 상태가 Block되지 않는다.   
  Thread Per Request Model 방식보다 Context Switch 오버헤드가 줄어들고 Thread 숫자도 작기 때문에   
  Core를 차지하기 위한 경합도 줄어든다. 높은 처리량이 가능한 이유는 이 Event Loop와 Non-blocking I/o를   
  사용하기 때문이다. Event Loop의 Thread를 일하는데만 집중하여 성능을 쥐어짜기 때문에 처리량이 높게 나온다.   
  이는 다른 일을 할 수 있는 프리한 Thread가 많기 때문이다.

* 만약 Non-blocking I/O 대신 Blocing I/O를 사용하면 어떻게 될까?   
  WebFlux는 상대적으로 적은 ThreadPool을 유지하기 때문에 CPU 사용량이 높은 작업이 많거나   
  Blocking I/O를 이용하여 프로그래밍을 한다면 Event Loop가 빨리 빨리 Event Queue에 있는 Event를   
  처리할 수 없다. Runnable 상태의 Thread들이 CPU를 점유하고 있기 때문이다. 그래서 전반적인 성능 하락이 발생한다.
<hr/>
