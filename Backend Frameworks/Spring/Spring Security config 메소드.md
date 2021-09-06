# Spring Security config() method

- 아래 `SecurityConfig` 클래스는 <a href="https://github.com/sang-w0o/spring-boot-kotlin-template">이 프로젝트</a>에서 사용하는  
  Spring Security에 대한 설정 정보를 담은 클래스이다.  
  JWT를 통한 인증을 구현한 부분인데, 자세한 설명은 <a href="https://github.com/sang-w0o/Study/blob/master/Backend%20Frameworks/Spring/Spring%20Security%EB%A1%9C%20JWT%20%EC%9D%B8%EC%A6%9D%20%EA%B5%AC%ED%98%84%ED%95%98%EA%B8%B0.md">링크</a>에서 볼 수 있다.

```kt
@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtTokenUtil: JwtTokenUtil,
    private val authenticationEntryPoint: AuthenticationEntryPoint
    ): WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity?) {
        // TODO: JWT 인증 절차를 추가할 End point 추가 및 수정
        http!!
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

    override fun configure(web: WebSecurity?) {
        // TODO: JWT 인증 절차를 제외할 End point 추가 및 수정
        web?.ignoring()
            ?.mvcMatchers(HttpMethod.POST, "/v1/auth/update-token")
            ?.mvcMatchers(HttpMethod.POST, "/v1/auth/login")
            ?.mvcMatchers(HttpMethod.GET, "/swagger-ui/**")
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.addAllowedOriginPattern("*")
        configuration.addAllowedHeader("*")
        configuration.addAllowedMethod("*")
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
```

- `SecurityConfig` 클래스를 보면 `WebSecurityConfigurerAdapter` 추상 클래스를 상속하는 것을  
  볼 수 있다. `WebSecurityConfigurerAdapter`의 특정 메소드를 오버라이딩하여 직접  
  Spring Security가 이 프로젝트에서 어떻게 작동했는지를 설정했는데, 오버라이딩한 메소드를 보면  
  `configure(HttpSecurity)`, `configure(WebSecurity)`가 있다.

- 이 두 `configure()` 메소드는 공통적으로 Spring Security의 인증 절차 중  
  제외시킬, 즉 인증 절차 없이 항상 접근 가능한 API Endpoint를 지정할 수 있다.  
  아래와 같이 `/test` API Endpoint를 만들어보고, 각 `configure()`에 인증이 필요 없는  
  API로 지정해보자. 그리고 이 둘의 차이점에 대해 알아보자.

- 이번에 사용할 테스트용 `[GET] /test` API 이다.

```kt
// SimpleMessageDto.kt

data class SimpleMessageDto(val message: String)

// TestApiController.kt
@RestController
class TestApiController {

    @GetMapping("/test")
    fun testGetApi(): SimpleMessageDto {
        return SimpleMessageDto("HELLO!")
    }
}
```

- 위 두 개를 만들고 실행하면 Spring Security Configuration에 의해  
  프로젝트에서 정의한 대로 아래의 응답이 온다.

```json
{
  "timestamp": "2021-9-6 16:48",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authorization Header is missing.",
  "path": "/test",
  "remote": "0:0:0:0:0:0:0:1"
}
```

- 이제 이 API를 인증 없이 사용 가능하도록 설정해보자.

<h2>configure(HttpSecurity httpSecurity)</h2>

- 우선 `configure(HttpSecurity httpSecurity)`에 이를 적용해보자.

```kt
class SecurityConfig {

    //..

    override fun configure(http: HttpSecurity?) {
    // TODO: JWT 인증 절차를 추가할 End point 추가 및 수정
    http!!
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
	// 아래가 추가된 부분이다.
        .antMatchers(HttpMethod.GET, "/test").permitAll()
        .antMatchers("/v1/**").authenticated()
        .anyRequest().permitAll()
        .and()
        .addFilterBefore(JWTRequestFilter(jwtTokenUtil, authenticationEntryPoint),UsernamePasswordAuthenticationFilter::class.java)
        .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
    }
}
```

- `[GET] /test`에 대해 `permitAll()`을 지정해 주었으니 인증이 필요 없는 API가  
  되었을 것이라 생각했다. 하지만 요청을 보내보면 처음 보낸 것과 같이 `401(UNAUTHORIZED)`가 온다.

- 이는 `configure(HttpSecurity)` 메소드 내에서 `HttpSecurity`의 여러 체이닝 메소드 중  
  `permitAll()`이 _모든 사용자에 대해 요청 허용_ 을 하기 때문이다.  
  즉 인증 절차가 적용되긴 하는 것이다. `anonymous()`도 마찬가지다.

<hr/>

<h2>configure(WebSecurity webSecurity)</h2>

- 그럼 이번에는 `configure(WebSecurity webSecurity)` 메소드에 설정을 해줘보자.

```kt
class SecurityConfig {

    //..

    override fun configure(web: WebSecurity?) {
    // TODO: JWT 인증 절차를 제외할 End point 추가 및 수정
    web?.ignoring()
        ?.mvcMatchers(HttpMethod.POST, "/v1/auth/update-token")
        ?.mvcMatchers(HttpMethod.POST, "/v1/auth/login")
        ?.mvcMatchers(HttpMethod.GET, "/swagger-ui/**")
        ?.mvcMatchers(HttpMethod.GET, "/test")
    }
}
```

- 이렇게 하니 원하던 대로 `[GET] /test`에 대해 인증 작업을 수행하지 않고,  
  아래와 같이 원하는 응답이 나타났다.

```json
{ "message": "HELLO!" }
```

<hr/>

<h2>이 두 메소드의 차이점</h2>

<hr/>
