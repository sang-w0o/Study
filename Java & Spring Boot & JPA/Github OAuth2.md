<h1>Github OAuth2 Login 구현</h1>

- 어떤 프로젝트에서 Github OAuth2 로그인 연동을 구현해야한 적이 있다.  
  인터넷에 나와있는 대부분의 코드들은 Thymeleaf 등의 STE를 사용하는 예제들이 많았는데,  
  아래 설명은 프론트엔드 코드는 ReactJS로 따로 있고, 백엔드 코드를 Spring으로 작성한 예시이다.

- 또한 ReactJS의 코드는 `localhost:3000`에서, 그리고 백엔드 코드는 `localhost:8080`에서 구동된다고 가정한다.

<h2>Spring Security 도입하기</h2>

- Spring Security는 Google, Facebook, Github 등에 대한 OAuth2 지원을 기본적으로 해준다.
- 우선 Spring Security를 통해 OAuth를 사용하기 위한 의존성은 아래와 같다.

```gradle
dependencies {
    // Other dependencies..
    implementation('org.springframework.boot:spring-boot-starter-oauth2-client')
}
```

<hr/>

<h2>Spring Security Configuration</h2>
<h2>Service</h2>
<h2>Handlers</h2>
<h2>Exception handling</h2>
