# Spring Security로 JWT 인증 구현하기

- Spring Security가 무엇인지에 대해서는 <a href="https://github.com/sang-w0o/Study/blob/master/Spring%20Security/1.%20%20Spring%20Boot%EC%99%80%20Security.md">여기</a>에서 볼 수 있다.

- 우선 가장 먼저 Spring Security를 추가하자. Spring Boot와 함께 쓰기에 아래 의존성을 추가한다.

```gradle
// Other dependencies..

implementation("org.springframework.boot:spring-boot-starter-security")
testImplementation("org.springframework.security:spring-security-test")
```

<hr/>

<h2>Spring Security 작동 방식</h2>

- Spring Security의 인증 진행 방식은 간단하게 말하면 여러 개의 `Filter`들과,  
  그 `Filter`들을 연결해주는 `FilterChain`들의 연쇄 작용으로 진행된다.
