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

  - `SampleServer`: 요청을 받아서 응답을 반환하는 서버 애플리케이션
  - `SampleFeignClient`: 클라이언트로부터 요청을 받아 `SampleServer`에 요청을 보낸 후, 응답을 하는 서버 애플리케이션

<hr/>

<h2>SampleServer 구축하기</h2>

- `SampleServer`는 단순히 요청을 받아서 적절한 응답을 반환해주는 매우 간단한 서버 애플리케이션이다.

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

<h2>
