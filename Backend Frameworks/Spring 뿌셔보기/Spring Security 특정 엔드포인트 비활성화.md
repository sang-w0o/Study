# Spring Security 특정 엔드포인트 비활성화

- Spring Security를 사용하여 JWT 인증 방식을 구현할 때, 나는 직접 `Filter`를 만들어서  
  Spring Security의 SecurityFilterChain에 만든 `Filter`를 넣는 식으로 처리했다.

- SecurityFilterChain에 직접 만든 `Filter`를 등록하면, 기본적으로 모든 API Endpoint에 들어오는  
  요청에 대해 해당 `Filter`가 실행된다. 하지만 JWT 인증 기능을 제외해야 하는 엔드포인트도 있다.  
  예를 들어 보자.

  - 사용자 회원 가입 API
  - 사용자 로그인 API
  - 기타 인증이 필요 없는 API들

## MVC

- 우선 Spring MVC에서 JWT 인증을 위한 `Filter` 및 추가적인 설정 클래스들을 만드는 내용은  
  <a href="https://github.com/sang-w0o/Study/blob/master/Backend%20Frameworks/Spring%20%EB%BF%8C%EC%85%94%EB%B3%B4%EA%B8%B0/Spring%20Security%EB%A1%9C%20JWT%20%EC%9D%B8%EC%A6%9D%20%EA%B5%AC%ED%98%84%ED%95%98%EA%B8%B0.md#jwt-%EC%9D%B8%EC%A6%9D%EC%9D%84-%ED%95%98%EB%8A%94-filter-%EB%A7%8C%EB%93%A4%EA%B8%B0">여기</a>에서 확인할 수 있다.

- 우선 `WebSecurityConfigurerAdapter`를 상속하는 구현 클래스를 만들어 설정을 오버라이딩해야 한다.

```kt
@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtTokenUtil: JwtTokenUtil,
    private val authenticationEntryPoint: AuthenticationEntryPoint,
): WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http
            .httpBasic().disable()
            .headers().frameOptions().disable()
            .and()
            .csrf().disable()
            .cors().configurationSource(corsConfigurationSource())
            .and()
            .logout().disable()
            .formLogin().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers("/v1/**")
            .authenticated()
            .and()
            .addFilterBefore(JWTRequestFilter(jwtTokenUtil, authenticationEntryPoint), UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
    }

    override fun configure(web: WebSecurity) {
        web.ignoring()
	    .mvcMatchers(HttpMethod.POST, "/v1/user/signup")
	    .mvcMatchers(HttpMethod.POST, "/v1/user/login")
	    .mvcMatchers(HttpMethod.GET, "/actuator/health)
    }
}
```

- 위 설정을 보면 직접 만든 `JWTRequestFilter`가 기존에 SecurityFilterChain에 있는  
  `UsernamePasswordAuthenticationFilter` 이전에 들어가 작동하도록 했다. (`addFilterBefore()`)

- `configure(HttpSecurity)` 메소드를 오버라이딩하여 `permitAll()`을 해주는 것으로는 인증 절차를 없앨 수 없다.  
  왜냐하면 `permitAll()`은 _모든 사용자_ 의 접근을 허용해준다는 뜻인데, 일단 그 _사용자_ 를 알아야 하기 때문이다.  
  그렇기 때문에 직접 만든 `Filter`의 `doFilter()`가 호출되어 버린다.

- 실질적으로 인증 과정을 제거하는 방법은 `configure(WebSecurity)`를 오버라이딩하는 것이다.

```kt
@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtTokenUtil: JwtTokenUtil,
    private val authenticationEntryPoint: AuthenticationEntryPoint,
): WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        //..
    }

    override fun configure(web: WebSecurity?) {
        web?.ignoring()
            .mvcMatchers(HttpMethod.POST, "/v1/user/signup") // 회원 가입 API
	    .mvcMatchers(HttpMethod.POST, "/v1/user/login") // 로그인 API
	    .mvcMatchers(HttpMethod.GET, "/actuator/health) // Actuator API
    }
}
```

> `configure(HttpSecurity)`와 `configure(WebSecurity)`의 차이점은
> <a href="https://github.com/sang-w0o/Study/blob/master/Backend%20Frameworks/Spring%20%EB%BF%8C%EC%85%94%EB%B3%B4%EA%B8%B0/Spring%20Security%20config%20%EB%A9%94%EC%86%8C%EB%93%9C.md">여기</a>에서 확인할 수 있다.

<hr/>

## Webflux

- Spring Webflux에서도 MVC와 동일하게 직접 `Filter`를 만들고, `UsernamePasswordAuthenticationFilter`가  
  동작하기 직전에 작동하게끔 할 수 있다.

- 아래는 위와 동일한 설정을 Webflux에서 한 것인데, 특이점으로는 `SecurityConfig`가 MVC가 제공하는  
  `WebSecurityConfigurerAdapter` 등 다른 클래스를 구현하지 않는다는 점이다.

- 또한 MVC 버전의 코드 예시와는 달리, Kotlin Spring Security DSL을 적용했다.

```kt
@EnableWebFluxSecurity
class SecurityConfig(
    private val jwtAuthenticationConverter: ServerAuthenticationConverter,
    private val jwtAuthenticationManager: ReactiveAuthenticationManager,
    private val serverAccessDeniedHandler: ServerAccessDeniedHandler
) {
    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {

	// Filter 생성
        val jwtAuthenticationWebFilter = AuthenticationWebFilter(jwtAuthenticationManager)
        jwtAuthenticationWebFilter.setServerAuthenticationConverter(jwtAuthenticationConverter)

	// Security 관련 설정
        return http {
            securityMatcher(
                NegatedServerWebExchangeMatcher(ServerWebExchangeMatchers.pathMatchers("/actuator/**", "/v1/user/signup", "/v1/user/login"))
            )
            authorizeExchange { authorize(anyExchange, permitAll) }
            httpBasic { disable() }
            formLogin { disable() }
            csrf { disable() }
            logout { disable() }
            authorizeExchange {
                authorize("/v1/**", authenticated)
            }
	    // 생성한 Filter 추가
            addFilterAt(jwtAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            exceptionHandling {
                accessDeniedHandler = serverAccessDeniedHandler
            }
        }
    }
}
```

- Webflux의 `SecurityConfig`는 어떤 클래스를 상속하지도, 인터페이스를 구현하지도 않기 때문에  
  MVC 처럼 오버라이드할 메소드가 없다. 그래서 `configure(WebSecurity)`와 같은 작동을 하기 위해서는  
  위의 `securityMatcher()` 부분을 보면 된다.

- 참고로 아직까지는 특정 API Path에 대해 Http Method까지 지정할 수 없고, 단지 String 가변 인자를 받는  
  `ServerWebExchangeMatchers.pathMatchers()`에 Security의 작동을 무시할 API Path를  
  지정하는 방법 밖에 없는 것 같다.

<hr/>
