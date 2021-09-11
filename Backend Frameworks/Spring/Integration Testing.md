# Spring Integration Test

<h2>Spring Integration Test</h2>

- 웹 애플리케이션에는 테스트의 종류가 크게 단위 테스트, 통합 테스트, 기능 테스트로 나뉜다.  
  여기서 다룰 테스트 종류는 **API 통합 테스트** 이다.  
  API 통합 테스트란, 각 API에 대해 여러 가지 상황을 만들고, 실제로 그 API를 호출해보며  
  각 상황이 예상한대로 동작하는지를 테스트하는 것이다.

<h2>MockMvc vs RestTemplate</h2>

- `RestTemplate`은 HTTP 요청을 처리하기 위한 동기 클라이언트이다.  
  이 클래스를 사용하여 외부 API와 동기 HTTP 요청을 수행할 수 있다.  
  테스트 코드에서 사용하는 `TestRestTemplate`는 통합 테스트에 적합하도록 만들어진  
  `RestTemplate`의 대용 클래스이다.

- `MockMvc`는 Server-Side REST API 테스트 목적으로 만들어진 클래스이다.  
  Server-Side REST API 라 했는데, 그럼 Client-Side REST API도 있을 것이다.  
  이 둘의 차이점을 `MockMvc`와 `RestTemplate`의 차이점과 함께 알아보자.

- 우선 먼저 Spring MVC의 Servlet Container에 대해 알아야 한다.  
  Spring Application은 실행 시 Servlet Container에 Servlet들을 실행하는데,  
  이렇게 실행된 후 요청이 오면 `DispatcherServlet`이 일단 모든 요청을 받아서 알맞은  
  컨트롤러에 요청을 위임해준다.

- `RestTemplate`은 HTTP 동기 요청 클라이언트이다. `TestRestTemplate`을 사용하더라도  
  이 클래스는 **실제 HTTP 요청**을 보낸다. 따라서 테스트 시 실제 Spring Application이  
  로컬에서 실행되고, `loclahost:포트번호`에 요청을 보내는 식으로 테스트가 수행된다.

- 반면, `MockMvc`는 실제 HTTP 요청을 보내지 않고, `DispatcherServlet`을 mocking하여  
  일종의 가짜 `DispatcherServlet`만 실행시키고, 서버의 동작을 확인할 때 사용한다.  
  즉 전체 애플리케이션을 실행하지 않고, `DispatcherServlet`만 실행시키는 것이다.

<hr/>

<h2>JUnit4 vs JUnit5</h2>

- JUnit4와 JUnit5의 차이점을 알아보자.

  - JUnit4의 모든 프레임워크는 하나의 jar 라이브러리로 제공되었다. 따라서 특정 기능만  
    필요했어도 전체 라이브러리를 사용해야 했다. JUnit5에서는 자율성이 더 부여되어  
    필요한 부분만 가져다 쓸 수 있다.

  - JUnit4에서는 Test Runner가 한번에 1개만 실행될 수 있었는데, JUnit5에서는 여러 개의  
    Test Runner들을 동시에 수행할 수 있게 되었다.

  - JUnit4는 Java7이후로 업데이트가 되지 않아, 현재 가장 많이 쓰이는 Java8의 기능을 사용할 수  
    없다. 반면 JUnit5는 이러한 기능들을 모두 사용할 수 있다.

- 그 외에 JUnit4에서 `@Test` 어노테이션에 지정할 수 있던 속성들이 JUnit5에서는 모두 별도의  
  Assertion 메소드로 빠져나왔다는 점, *Assumtion class*가 생겼다는 점, 테스트를 그룹핑할 때  
  JUnit4에서는 `@Category`를 사용했지만 JUnit5에서는 `@Tag`를 사용하는 점 등 여러 가지  
  차이점들이 있다. 예시 코드와 함께 <a href="https://www.baeldung.com/junit-5-migration">여기</a>에서 볼 수 있다.

<hr/>

<h2>코드 바꿔가기</h2>

<h3>기존 코드</h3>

<h3>변경 후</h3>

<hr/>

<h2>결론</h2>

https://www.baeldung.com/kotlin/mockmvc-kotlin-dsl
https://stackoverflow.com/questions/25901985/difference-between-mockmvc-and-resttemplate-in-integration-tests
https://www.baeldung.com/junit-5-migration
