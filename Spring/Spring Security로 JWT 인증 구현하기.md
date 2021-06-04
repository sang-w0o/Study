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
- `Filter`, `FilterChain`에 대한 간략한 설명은 <a href="https://github.com/sang-w0o/Study/blob/master/Spring%20Security/2-0.%20Servlet%20Security.md">여기</a>에서 볼 수 있다.

- 우선, Access Token을 검증하는 작업은 컨트롤러가 요청을 받아 서비스의 알맞은 비즈니스 로직을  
  수행하기 전에 수행되어야 한다. **따라서 Authorization Header의 검증은 Filter로 동작** 해야 한다.

  > Authorization Header를 검증하는 Filter 만들기

- 그 다음으로는 위에서 만든 Filter를 언제 적용 시켜야 할지를 정해야 한다.  
  Spring Security가 기본적으로 FilterChain에 등록하여 실행하는 여러 개의 Filter들 중,  
  위에서 만든 커스텀 필터를 언제 적용할지 생각해야 한다.  
  이 예제에서는 **실질적인 인증이 수행되는 `UsernamePasswordAuthenticationFilter`** 의 이전 단계에  
  위에서 만든 커스텀 필터를 적용하도록 하겠다.

  > `UsernamePasswordAuthenticationFilter` 이전에 커스텀 필터 적용하기

- 위에서 언급했지만, 인증이 수행되는 시점은 컨트롤러에 요청이 매핑되기 이전이다.  
  따라서 컨트롤러가 요청을 받은 순간부터 발생하는 예외를 처리하는 `@RestControllerAdvice`가 적용된  
  Global Exception Handler는 인증과정에서 발생하는 Spring Security의 `AuthenticationException`을  
  처리할 수 없다. 이를 해결하기 위해 Spring Security에서는 인증 과정이 실패하는 과정을 처리할 수 있게끔  
  `AuthenticationEntryPoint`라는 인터페이스를 제공한다.

  > `AuthenticationEntryPoint`를 구현하는 인증 예외 처리 핸들러 만들기

- 마지막으로 위의 모든 작업을 설정해주는 Spring Security Configuration 클래스가 필요하다.
  > Spring Security Configuration Class 만들기

<hr/>

<h3>1. 인증 예외 처리 핸들러 만들기</h3>

- Spring Security의 Authentication 과정에서 발생하는 예외를 핸들링 하기 위해서는  
  `org.springframework.security.web.AuthenticationEntryPoint` 인터페이스의 구현체를 만들어야 한다.

```kotlin
@Component
class JWTAuthenticationEntryPoint : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        exception: AuthenticationException?
    ) {
        request!!
        val errorResponseDto = ErrorResponseDto(
            DateConverter.convertDateWithTime(LocalDateTime.now()), HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.reasonPhrase, exception?.message!!, request.requestURI, request.remoteAddr)
        response?.status = HttpStatus.UNAUTHORIZED.value()
        response?.contentType = MediaType.APPLICATION_JSON_VALUE
        response?.characterEncoding = "UTF-8"
        response?.writer?.println(convertObjectToJson(errorResponseDto))
    }

    private fun convertObjectToJson(obj: Any): String? {
        if (obj == null) {
            return null;
        }
        val mapper = ObjectMapper()
        return mapper.writeValueAsString(obj);
    }
}
```

- 위 코드처럼 `AuthenticationEntryPoint`의 `commence()` 메소드에서 예외 상황을 처리할 수 있다.  
  `ErrorResponseDto`는 예외 상황을 처리하기 위해 만들어진 data class이며, `convertObjectToJson()`  
  메소드는 JSON Object 형식으로 된 String을 만들어주는 메소드이다.  
  `commence()`에서는 인증 과정에서 `AuthenticationException`이 발생하면 401(UNAUTHORIZED)의  
  HTTP Status Code와 함께 다른 값들을 클라이언트에게 전달해주도록 설정해주었다.

<hr/>
