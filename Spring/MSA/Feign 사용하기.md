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

<hr/>
