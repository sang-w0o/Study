# Webflux에서 Spring Security로 JWT 인증 구현하기

> MVC에서 Spring Security로 구현하는 방법은 <a href="https://github.com/sang-w0o/Study/blob/master/Backend%20Frameworks/Spring%20%EB%BF%8C%EC%85%94%EB%B3%B4%EA%B8%B0/Spring%20Security%EB%A1%9C%20JWT%20%EC%9D%B8%EC%A6%9D%20%EA%B5%AC%ED%98%84%ED%95%98%EA%B8%B0.md">여기</a>에 있다.

- Bearer Scheme를 사용하여 JWT 인증을 구현해보자.  
  Bearer Scheme를 사용한다 함은, 클라이언트에서 인증이 필요한 요청을 보낼 때 헤더에  
  `Authorization`이 key이며 `Bearer token` 의 value를 보낸다는 것을 의미한다.  
  token 자리에는 서버에서 발급한 access token이 들어가며, 서버는 이를 가져와 인증 정보를 검증한다.

- MVC에서 했던 것과 마찬가지로 Spring Security의 SecurityFilterChain에 직접 정의한  
  `Filter`를 넣어줄 것이다. 마찬가지로 `UsernamePasswordAuthenticationFilter` 이전에  
  넣어 작동하도록 해보자.

- Webflux에서 `Filter`를 사용해 인증을 구현하기 위해서는 `ReactiveAuthenticationManager`의 구현체와  
  `ServerAuthenticationConverter`의 구현체가 필요하다.

- 기본적인 인증 과정은 아래와 같다.

  - (1) 정의한 `Filter`의 `doFilter()`가 호출된다.
  - (2) `ServerAuthenticationConverter` 구현체가 요청 헤더에서 토큰을 가져와  
    `Authentication` 객체에 담아 반환한다.
  - (3) `ReactiveAuthenticationManager` 구현체가 `(2)`에서 받은 `Authentication` 의  
    정보를 가져와 검증을 수행한다.

> 참고로 이 예시에서는 JWT 관련된 모든 역할(토큰 생성, 검증, 파싱 등)들의 책임을 가진  
> `JwtTokenUtil`을 사용할 것이다. 즉, `(3)` 내에서 실제 검증은 `JwtTokenUtil`이 담당한다.

## (1) ServerAuthenticationConverter 구현하기

- 인증 정보를 적절한 `Authentication`에 담아 반환할 구현체를 만들어보자.

```kt
@Component
class JwtServerAuthenticationConverter : ServerAuthenticationConverter {
    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        return Mono.just(exchange)
            .flatMap { Mono.justOrEmpty(extractTokenFromHeader(it)) }
            .map {
                UsernamePasswordAuthenticationToken(it, "", mutableListOf())
            }
    }

    private fun extractTokenFromHeader(exchange: ServerWebExchange): String {
        val authorizationHeader = exchange.request.headers["Authorization"]
        if (authorizationHeader.isNullOrEmpty()) {
            throw AuthenticateException("JWT Header is missing.")
        } else {
            if (authorizationHeader[0].contains("Bearer")) {
                return authorizationHeader[0].replace("Bearer ", "")
            } else {
                throw AuthenticateException("Invalid Authorization header scheme.")
            }
        }
    }
}
```

- `extractTokenFromHeader()` 메소드는 요청 헤더에서 `Authorization` 부분을 가져와  
  Bearer Scheme에 알맞게 토큰을 가져온다. 그 과정에서 Scheme가 맞지 않거나, 헤더 값이 없는 등의  
  문제가 발생하면 예외를 던진다.

- `convert()`는 `ServerAuthenticationConverter`의 메소드를 구현한 것으로,  
  `Mono<UsernamePasswordAuthenticationToken>`을 반환한다. `UsernamePasswordAuthenticationToken`은  
  `Authentication`의 구현체이다. 인스턴스를 만들 때 첫 번째 인자로 토큰을 지정했는데, 이는 나중에  
  `Authentication.principal`로 접근 가능하다.

<hr/>

## (2) ReactiveAuthenticationManager 구현하기

- 이제 실제 인증 요청 정보(Access Token)를 갖고 인증을 수행하는 책임을 가진 `ReactiveAuthenticationManager`의  
  구현체를 만들어보자. 이전에 실제 토큰에 대한 작업을 수행하는 `JwtTokenUtil` 클래스를 보자.  
  MVC 버전의 `JwtTokenUtil`과 비슷하지만, `Mono`로 감싸진 객체를 반환하는 메소드가 있다.

```kt
@Component
class JwtTokenUtil(
    private val jwtProperties: JwtProperties,
    private val userRepository: UserRepository
) {
    private fun getUserId(claim: Claims): String {
        try {
            return claim.get("userId", String::class.javaObjectType)
        } catch (e: Exception) {
            throw AuthenticateException("JWT Claim에 userId가 없습니다.")
        }
    }

    private fun extractExp(token: String): Date {
        return extractClaim(token, Claims::getExpiration)
    }

    private fun extractAllClaims(token: String): Claims {
        try {
            return Jwts.parser().setSigningKey(jwtProperties.secret).parseClaimsJws(token).body
        } catch (expiredJwtException: ExpiredJwtException) {
            throw AuthenticateException("Jwt 토큰이 만료되었습니다.")
        } catch (unsupportedJwtException: UnsupportedJwtException) {
            throw AuthenticateException("지원되지 않는 Jwt 토큰입니다.")
        } catch (malformedJwtException: MalformedJwtException) {
            throw AuthenticateException("잘못된 형식의 Jwt 토큰입니다.")
        } catch (signatureException: SignatureException) {
            throw AuthenticateException("Jwt Signature이 잘못된 값입니다.")
        } catch (illegalArgumentException: IllegalArgumentException) {
            throw AuthenticateException("Jwt 값이 잘못되었습니다.")
        }
    }

    private fun createToken(claims: Map<String, Any>, exp: Int): String {
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + exp))
            .signWith(SignatureAlgorithm.HS256, jwtProperties.secret)
            .compact()
    }

    private fun <T> extractClaim(token: String, claimResolver: Function<Claims, T>): T {
        return claimResolver.apply(extractAllClaims(token))
    }

    fun extractUserId(token: String): String {
        return extractClaim(token, this::getUserId)
    }

    fun isTokenExpired(token: String): Boolean {
        return extractExp(token).before(Date())
    }

    fun verify(token: String): Mono<Authentication> {
        extractAllClaims(token)
        val userId = extractUserId(token)
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(AuthenticateException("Invalid userId")))
            .map {
                UsernamePasswordAuthenticationToken(it, "", mutableListOf())
            }
    }

    fun generateAccessToken(userId: String): String {
        val claims: MutableMap<String, Any> = mutableMapOf()
        claims["userId"] = userId
        return createToken(claims, jwtProperties.accessTokenExp)
    }

    fun generateRefreshToken(userId: String): String {
        val claims: MutableMap<String, Any> = mutableMapOf()
        claims["userId"] = userId
        return createToken(claims, jwtProperties.refreshTokenExp)
    }

    fun getAuthentication(authentication: Authentication): Mono<Authentication> {
        return Mono.just(authentication)
            .flatMap { auth ->
                verify(auth.principal.toString())
                    .map {
                        it
                    }
            }
    }
}
```

- 위 코드 중 `verify()`는 토큰을 해석해 올바른 토큰인지 검증하는 메소드이다.  
  `ReactiveAuthenticationManager`의 `authenticate()` 메소드는 `Mono<Authentication>`를 반환하므로,  
  이를 맞춰주기 위한 `getAuthentication()`을 `JwtTokenUtil`에 만들어줬다.

- 이제 구현체를 보자.

```kt
@Component
class JwtAuthenticationManager(
    private val jwtTokenUtil: JwtTokenUtil
) : ReactiveAuthenticationManager {
    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        return Mono.just(authentication)
            .flatMap { auth ->
                jwtTokenUtil.getAuthentication(auth).map {
                    it
                }
            }
    }
}
```

- 위에서 만든 `JwtTokenUtil#getAuthentication()`를 `authenticate()`에서 사용했다.

## (3) 필터 설정하기

- 이제 `Filter`가 수행할 작업들을 구현한 구현체들을 모두 만들었으므로, `Filter`를 만들고  
  SecurityFilterChain에 등록해보자.

```kt
@EnableWebFluxSecurity
class SecurityConfig(
    private val jwtAuthenticationConverter: ServerAuthenticationConverter,
    private val jwtAuthenticationManager: ReactiveAuthenticationManager,
) {
    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        val jwtAuthenticationWebFilter = AuthenticationWebFilter(jwtAuthenticationManager)
        jwtAuthenticationWebFilter.setServerAuthenticationConverter(jwtAuthenticationConverter)
        return http {

	    //..

	    addFilterAt(jwtAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
        }
    }
}
```

- `AuthenticationWebFilter`의 인스턴스를 만들고, 위에서 만든 manager와 converter를  
  각각 setter, 생성자를 통해 지정해주었다.

<hr/>

## 더 보기: 어노테이션으로 사용자 정보 가져오기

- 이번에는 webflux에서 어노테이션을 만들어서 컨트롤러 메소드의 파라미터로 정보를  
  가져올 수 있게 해보자. 참고로 MVC에서는 이를 <a href="https://github.com/sang-w0o/Study/blob/master/Backend%20Frameworks/Spring%20%EB%BF%8C%EC%85%94%EB%B3%B4%EA%B8%B0/Java%20%EC%96%B4%EB%85%B8%ED%85%8C%EC%9D%B4%EC%85%98%20%ED%8C%8C%ED%97%A4%EC%B9%98%EA%B8%B0.md#%EC%A0%81%EC%9A%A9%ED%95%B4%EB%B3%B4%EA%B8%B0---%EC%82%AC%EC%9A%A9%EC%9E%90-%EC%A0%95%EB%B3%B4-%EA%B0%80%EC%A0%B8%EC%98%A4%EA%B8%B0">여기</a>에서 설명하는 것처럼  
  쉽게 구현할 수 있다. webflux도 조금만 다를 뿐이지, 마찬가지다.

- 우선 동일하게 어노테이션을 먼저 정의하자.

```kt
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class LoggedInUser()
```

- 다음으로 실제 파라미터 바인딩을 수행해줄 부분을 작성해야 한다. MVC에서와 마찬가지로 `HandlerMethodArgumentResolver`의  
  구현체를 만들면 된다.

```kt
@Component
class UserInfoArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.getParameterAnnotation(LoggedInUser::class.java) != null && parameter.parameterType == User::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        bindingContext: BindingContext,
        exchange: ServerWebExchange
    ): Mono<Any> {
        return ReactiveSecurityContextHolder.getContext().map {
            it.authentication.principal
        }.filter {
            it is User
        }.switchIfEmpty(Mono.error(RuntimeException("Authentication.principle is not type of User")))
            .map {
                it as User
            }
    }
}
```

- MVC가 `SecurityContext`를 Thread별로 관리하지만, Webflux에서는 모든 요청을 최소한의(1개) 스레드로  
  처리하려 하기 때문에 더 이상 Thread별로 관리하도록 만들어진 `SecurityContextHolder`를 사용할 수 없다.  
  대신 `ReactiveSecurityContextHolder`를 사용해야 한다.

- 하지만 인증 정보를 검증하는 로직에서 `ReactiveSecurityContextHolder`에 접근해 `Authentication`  
  구현체를 명시적으로 넣어주는 부분이 없는데, 이는 아마도 `ReactiveAuthenticationManager#authenticate()`가  
  검증을 수행한 후 반환할 때 자동으로 저장되는 것 같다.

- 이제 아래처럼 컨트롤러 메소드 단에서 편리하게 사용자 정보를 가져와 사용할 수 있다.

```kt
@RestController
@RequestMapping("/v1/user")
class UserApiController {

    @GetMapping("/info")
    fun getUserInfo(@LoggedInUser user: User) = userService.getInfo(Mono.just(user))

    //..
}
```

<hr/>
