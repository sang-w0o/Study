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

- 따라서 Server-Side REST API를 테스트 하기에는 `MockMvc`가 적절하고, Client-Side REST API를  
  테스트하기에는 `RestTemplate` 또는 `TestRestTemplate`가 적절하다.

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

- 가장 간단한 테스트 상황을 보자. Spring Actuator가 기본적으로 제공해주는 `/actuator/health`가  
  잘 작동하는지를 테스트하는 코드이다. 이 API에 GET 요청을 보내면, 아래 응답이 와야 한다.

```json
{ "status": "UP" }
```

<h3>기존 코드</h3>

- 기존에는 `RestTemplate`의 통합 테스트에 특화된 버전인 `TestRestTemplate`을 사용해서 테스트했다.  
  이 클래스는 `RequestEntity`로 요청 객체를 만들어 전달해야 하기에 아래처럼 코드를 작성했다.

```kt
// 모든 테스트 클래스가 상속받는 부모 클래스인 ApiIntegrationTest.kt
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class ApiIntegrationTest {
    //..

    @Autowired
    protected lateinit var restTemplate: TestRestTemplate

    //..
}

// Actuator 테스트 클래스 ActuatorTest.kt

class ActuatorTest : ApiIntegrationTest() {

    @Test
    fun healthCheckApiIsOpen() {
	val requestEntity = RequestEntity.get(URI.create("/actuator/health"))
	    .build()
	val responseEntity = restTemplate.exchange(requestEntity, String::class.java)
	val response = responseEntity.body
	assertEquals(HttpStatus.OK, responseEntity.statusCode)
	assertTrue(response.contains("status"))
	assertTreu(resonse.contains("UP))
    }
}
```

- 위 코드에서는 `restTemplate.exchange`의 두 번째 인자로 `String::class.java`를 주어  
  ResponseBody를 String으로 주었으나, 제대로 하려면 DTO class를 직접 만들어서 해당 클래스를  
  인자로 전달해줘도 된다.

<h3>변경 후</h3>

- 우선 `MockMvc`를 사용할 것이기에 `ApiIntegrationTest`에 `MockMvc`를 추가해줘야 한다.

```kt
// ApiIntegrationTest.kt
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
abstract class ApiIntegrationTest {

    //..

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper
}
```

- `ObjectMapper`를 추가해준 이유는 `MockMvc`에서 요청을 보낼 때 Request Body를 쓰거나,  
  Response Body를 읽을 때 필요하기 때문이다.

- 그럼 이제 바뀐 테스트 코드를 보자.

```kt
class ActuatorTest : ApiIntegrationTest() {

    @Test
    fun healthCheckApiIsOpen() {
	val test = mockMvc.get(URI.create("/actuator/health"))
	val result = test.andExpect {
	    status { isOk }
	    jsonPath("status") { value("UP")}
	}
    }

    // JSON Response Body에서 "status"의 key 값을 읽어올 때 아래처럼 해도 된다.
    // 아래 처럼 가져오면 추가적인 로직으로 검증할 수 있다.
    @Test
    fun healthCheckApiIsOpen() {
        val test = mockMvc.get(URI.create("/actuator/health"))
        val result = test.andExpect {
            status { isOk() }
            jsonPath("status") { exists() }
        }.andReturn()
        val statusResult = JsonPath.read<String>(result.response.contentAsString, "$.status")
        assertEquals("UP", statusResult)
    }
}
```

<hr/>

<h2>결론</h2>

- `RestTemplate`을 사용하면 `RequestEntity`, `ResponseEntity`도 함께 사용해야 해서  
  테스트 코드가 길어진다. 하지만 `MockMvc`를 사용하면 코드를 조금 더 함수형으로 짤 수 있게 되며  
  테스트 코드 또한 훨씬 짧아진다. 그리고 실제 Application을 구동하지 않고 `DispatcherServlet`만  
  mocking하여 테스트하기 때문에 속도도 향상된다.

- 참고 링크

- JUnit4에서 JUnit5로 바꾸는 위 코드 예시들의 PR: <a href="https://github.com/sang-w0o/spring-boot-kotlin-template/pull/8/files">link</a>
- JUnit4에서 JUnit5 차이점 및 마이그레이션: <a href="https://www.baeldung.com/junit-5-migration">link</a>
- `MockMvc` 사용 시 중복 코드를 리팩토링할 때 유용한 글: <a href="https://www.baeldung.com/kotlin/mockmvc-kotlin-dsl">link</a>
- `MockMvc` vs `RestTemplate`: <a href="https://stackoverflow.com/questions/25901985/difference-between-mockmvc-and-resttemplate-in-integration-tests">link</a>

<hr/>
