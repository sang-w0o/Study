# Feign 사용하기

<h2>Feign 개요 및 예시 상황</h2>

- `Feign`은 Spring 생태계에서 MSA의 환경을 구축한 Netflix에서 제작한  
  HTTP Client Binder이다. 웹 서비스 클라이언트를 매우 간단하게 작성할 수 있으며,  
  Feign을 사용하려면 적절한 interface와 어노테이션만 적용해주면 되기에 매우 편리하다.  
  (JPA와 비슷한 정도의 편리함..)

- Feign은 Netflix OSS를 사용하여 구축된 Spring 기반의 MSA 환경에서 주로  
  **마이크로서비스들 사이에서 동기 통신을 처리** 할 때 주로 사용된다.

- Eureka 등을 사용하여 Service Registry Pattern을 적용한 경우에 Feign의 모든 기능을  
  사용할 수 있지만, 이 예시는 단순히 다른 서버에 요청을 보내는 내용을 담는다.

- 우선, 이 예시에서는 아래의 명칭을 사용하겠다.

  - `SampleServer(:8082)`: 요청을 받아서 응답을 반환하는 서버 애플리케이션
  - `SampleFeignClient(:8081)`: 클라이언트로부터 요청을 받아 `SampleServer`에 요청을 보낸 후, 응답을 하는 서버 애플리케이션

<hr/>

<h2>SampleServer 구축하기</h2>

- `SampleServer`는 단순히 요청을 받아서 적절한 응답을 반환해주는 매우 간단한 서버 애플리케이션이다.  
  API로는 `POST` 방식으로 호출할 수 있는 `/v1/sample`이 있다.  
  이 API의 명세를 보자.

- 아래는 클라이언트가 보내는 요청의 Request Body 예시이다.

```json
{
  "firstMessage": "MESSAGE 1",
  "secondMessage": "MESSAGE 2",
  "thirdMessage": "MESSAGE 3"
}
```

- 이 요청을 받으면, `SampleServer`는 아래의 응답을 반환한다.

```json
{
  "requestMessages": ["MESSAGE 1", "MESSAGE 2", "MESSAGE 3"],
  "messageFromServer": "This message is created by a server that Feign has called."
}
```

- `requestMessages`는 Request Body에서 받은 3개의 value를 배열로 가지며, `messageFromServer`는  
  이 응답이 `SampleServer`를 통해 받은 응답임을 알리기 위한 일종의 메시지이다.

<hr/>

<h2>SampleFeignClient 구축하기</h2>

- 이 예시 상황은 클라이언트가 `SampleFeignClient`가 요청을 받으면 `SampleServer`에 요청을 보낸 후  
  그 응답을 클라이언트에게 그대로 반환해주는 상황이다.

- 우선 `SampleFeignClient`가 `/v1/send-feign-api-call`이라는 `POST` 방식으로 호출할 수 있는  
  REST API가 있다고 하자. 이 API를 처리하는 서비스 로직에서는 Feign을 사용해 `localhost:8082/v1/sample`로  
  요청을 보내야 한다.

- 이 상황에 사용하는 것을 `FeignClient`라고 한다. 아래 코드를 보자.

<h3>Feign Client 작성하기</h3>

```kt
// SampleClient.kt

@FeignClient(name = "sampleServer", configuration = [FeignConfig::class], url = "http://localhost:8082")
interface SampleClient {
    @PostMapping("/v1/sample")
    fun makeApiCall(@RequestBody dto: SampleRequestDto): SampleResponseDto
}
```

- `SampleClient`라는 인터페이스에 `@FeignClient` 어노테이션을 적용하여 이 인터페이스가 Feign을 사용해  
  동기 호출을 할 것 임을 선언했다. `@FeignClient`에는 name, configuration, url의 속성들이 지정되어 있는데  
  name은 이 Feign Client의 이름, configuration은 Feign 관련 설정 정보를 담는 클래스, 그리고 url은  
  이 Feign Client가 호출할 API의 base url을 담는다.

> 참고로 Eureka로 Service Registry Pattern을 구현하면 name으로 서비스를 찾아주기에  
> url을 지정하지 않아도 된다.

- `SampleClent`에 있는 `makeApiCall()` 메소드를 보자.  
  컨트롤러에 Api Endpoint를 생성할 때의 코드와 매우 유사한데, 간단하게 아래의 내용을 의미한다.

  - `http://localhost:8082/v1/sample`로 요청을 보내는데, 요청의 Request Body에는  
    `SampleRequestDto`가 들어갈 것이며, 이 요청의 Response Body를 `SampleResponseDto`에  
    매핑하여 담을 것이다.

<h3>Application 설정</h3>

```kt
// SampleFeignClientApplication.kt

@SpringBootApplication
@EnableFeignClients
open class SampleFeignClientApplication

fun main(args: Array<String>) {
    runApplication<SampleFeignClientApplication>(*args)
}
```

- `@EnableFeignClients` 어노테이션은 이 애플리케이션에서 사용되는 `@FeignClient` 어노테이션이 적용된  
  인터페이스들을 모두 찾아 컴포넌트로 등록시켜준다. 이 어노테이션이 없다면 애플리케이션은 실행조차 되지 않는다.

<hr/>

<h2>시연 해보기</h2>

- 정말로 `SameFeignClient`가 `SampleServer`에 Feign을 사용하여 요청을 보내는지 확인해보자.  
  우선 `SampleServer`가 `/v1/sample`을 통해 반환하는 Response body에는 아래의 필드가 있었다.

```json
"messageFromServer": "This message is created by a server that Feign has called."
```

- `SampleServer`가 아닌 `SampleFiegnClient`가 실행하는 `localhost:8081/v1/send-feign-api-call`로  
  요청을 보내면 아래의 응답이 온다.

```json
{
  "requestMessages": ["MESSAGE 1", "MESSAGE 2", "MESSAGE 3"],
  "messageFromServer": "This message is created by a server that Feign has called."
}
```

- 이로써 실제로 `FiegnClient`가 `SampleServer`로 요청을 보내는 것을 확인할 수 있다.  
  그렇다면 정상적이지 않은 경우를 테스트 해보자.

- (1) `SampleServer`로부터 받는 Response Body를 담는 dto가 잘못된 경우

  - 기존에 `FeignClient`의 `SampleResponseDto`는 아래처럼 작성되어 있었다.

  ```kt
  data class SampleResponseDto(
      val requestMessages: List<String>,
      val messageFromServer: String
  )
  ```

  - 만약 messageFromServer 필드명을 message로 바꾸면 어떻게 될까?

    - `feign.codec.DecodeException`이 발생하며 json 파싱에 실패했음을 알 수 있다.

- (2) `SampleFeignClient`가 요청을 보내는 `SampleServer`가 꺼져 있는 경우

  - 바로 `feign.RetryableException`이 발생하며, `http://localhost:8082/v1/sample`에 POST 요청을 보내는데에  
    실패했음을 알 수 있다.

<hr/>

- 모든 소스 코드는 <a href="https://github.com/Example-Collection/Spring-Feign-Example">GitHub</a>에서 볼 수 있다.
