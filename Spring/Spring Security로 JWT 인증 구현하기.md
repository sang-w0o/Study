# Spring Security로 JWT 인증 구현하기

- Spring Security가 무엇인지에 대해서는 <a href="https://github.com/sang-w0o/Study/blob/master/Spring%20Security/1.%20%20Spring%20Boot%EC%99%80%20Security.md">여기</a>에서 볼 수 있다.

- 우선 가장 먼저 Spring Security를 추가하자. Spring Boot와 함께 쓰기에 아래 의존성을 추가한다.

```gradle
// Other dependencies..

implementation("org.springframework.boot:spring-boot-starter-security")
testImplementation("org.springframework.security:spring-security-test")
```

- 그리고 JWT 형식으로 AccessToken을 발급하고, 검증하는 기능을 하는 유틸리티 클래스를 만들어주자.

```kotlin
@Component
class JwtTokenUtil {

    @Value("\${jwt.secret}")
    lateinit var secretKey: String

    companion object {
        private const val ACCESS_TOKEN_EXP: Int = 86400000 // 1 day
    }

    private fun getUserId(claim: Claims): Int {
        try {
            return claim.get("userId", Int::class.javaObjectType)
        } catch(e: Exception) {
            throw AuthenticateException("JWT Claim에 userId가 없습니다.")
        }
    }

    private fun extractExp(token: String): Date {
        return extractClaim(token, Claims::getExpiration)
    }

    private fun extractAllClaims(token: String) : Claims{
        try {
            return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).body
        } catch(expiredJwtException: ExpiredJwtException) {
            throw AuthenticateException("Jwt 토큰이 만료되었습니다.")
        } catch(unsupportedJwtException: UnsupportedJwtException) {
            throw AuthenticateException("지원되지 않는 Jwt 토큰입니다.")
        } catch(malformedJwtException: MalformedJwtException) {
            throw AuthenticateException("잘못된 형식의 Jwt 토큰입니다.")
        } catch(signatureException: SignatureException) {
            throw AuthenticateException("Jwt Signature이 잘못된 값입니다.")
        } catch(illegalArgumentException: IllegalArgumentException) {
            throw AuthenticateException("Jwt 헤더 값이 잘못되었습니다.")
        }
    }

    private fun createToken(claims: Map<String, Any>, exp: Int): String {
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + exp))
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact()
    }

    fun <T> extractClaim(token: String, claimResolver: Function<Claims, T>): T {
        return claimResolver.apply(extractAllClaims(token))
    }

    fun extractUserId(token: String): Int {
        return extractClaim(token, this::getUserId)
    }

    fun isTokenExpired(token: String): Boolean {
        return extractExp(token).before(Date())
    }

    fun generateAccessToken(userId: Int): String {
        val claims: MutableMap<String, Any> = mutableMapOf()
        claims["userId"] = userId
        return createToken(claims, ACCESS_TOKEN_EXP)
    }
}
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

<h3>인증 절차를 위한 클래스들 만들기</h3>

- Filter를 구현하기 전에, 인증 절차에 사용될 클래스들을 먼저 만들어야 한다.  
  우선 Spring Security에서는 `인증 성공 여부`를 개발자가 마음대로 정의할 수 있도록 하는데,  
  그러기 위해서는 사용자를 name(이름)으로 불러오는 작업을 하는 `UserDetailsService` 인터페이스의 구현체가  
  필요하다. 우리의 경우, accessToken 생성 시 userId를 넣어주기에 이 값을 name으로 취급하면 된다.

```kotlin
@Service
class JWTUserDetailsService(private val authorizeUser: AuthorizeUser) : UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails {
        val user = authorizeUser.apply(Integer.parseInt(username!!))
        return UserDetailsImpl(user.id!!)
    }

    fun getAuthorities() : Set<GrantedAuthority> {
        return mutableSetOf()
    }
}
```

- 참고로 위에서 사용된 `AuthorizeUser`는 함수형 인터페이스로, userId를 인자로 받아 해당 사용자(userId)가 실제로 존재하는지를  
  파악한 후, 없다면 Spring Security에서 기본적으로 제공적하는 예외 클래스 중 하나인 `AuthenticationException`을 상속하는`UsernameNotFoundException`를 발생시킨다.

```kotlin
@Component
class AuthorizeUser : Function<Int, User>{

    @Autowired
    private lateinit var userRepository: UserRepository

    override fun apply(userId: Int): User {
        return userRepository.findById(userId).orElseThrow { UsernameNotFoundException("잘못된 userId 값입니다.") }
    }
}
```

- 다음으로, 위의 `JWTUserDetailsService`에서 사용된 `UserDetailsImpl`을 보자.  
  Spring Security에서는 인증이 수행되면, 사용자의 정보를 `SecurityContextHolder`라는 객체의 `SecurityContext`내에  
  한 request가 끝날 때까지 저장한다. 이 때, `SecurityContext`에 저장되는 객체는 `Authentication` 객체이다.  
  `UserDetailsImpl`은 `Authentication`객체에 사용자 정보를 저장하기 위해 제공되는 `UserDetail` 인터페이스의 구현체이다.

```kotlin
class UserDetailsImpl(
    private val id: Int
): UserDetails {

    override fun isEnabled(): Boolean {
        return true
    }

    override fun getPassword(): String {
        return ""
    }

    override fun getUsername(): String {
        return id.toString()
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return mutableListOf()
    }
}
```

<h3>JWT 인증을 하는 Filter 만들기</h3>

- `Filter`를 만들 때에는 `org.springframework.web.filter` 패키지에 있는 클래스의 구현체를  
  만들면 된다. 여기서는 `OncePerRequestFilter`를 상속하는 Filter를 만들 것인데, 그 이유는 아래와 같다.

  - `OncePerRequestFilter`는 Request가 올 때마다 어떠한 Servlet Container이든  
    **무조건 1번 실행됨을 보장** 하기 위해 만들어진 추상 클래스이기 때문이다. 또한, `HttpServletRequest`,  
    `HttpServletResponse`, `FilterChain`를 매개변수로 하는 `doFilterInternal()` 메소드를 제공하여  
    해당 필터가 수행할 작업을 자유롭게 명시할 수 있다.

- 우선, 코드를 보자.

```kotlin
class JWTRequestFilter(private val jwtTokenUtil: JwtTokenUtil,
    private val authenticationEntryPoint: AuthenticationEntryPoint
) : OncePerRequestFilter() {

    companion object {
        private const val BEARER_SCHEME = "Bearer"
        private const val AUTHORIZATION_HEADER = "Authorization"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val authorizationHeader = request.getHeader(AUTHORIZATION_HEADER)
                ?: throw AuthenticateException("Authorization Header is missing.")
            val token = extractAccessToken(authorizationHeader)
            if (jwtTokenUtil.isTokenExpired(token)) throw AuthenticateException("AccessToken has been expired.")
            val authentication = jwtTokenUtil.getAuthentication(token)
            val context = SecurityContextHolder.getContext()
            context.authentication = authentication
            filterChain.doFilter(request, response)
        } catch(e: AuthenticateException) {
            authenticationEntryPoint.commence(request, response, e)
        }
    }

    private fun validateAuthorizationHeader(splits: List<String>) {
        if(splits.size != 2) throw AuthenticateException("Authorization Header is malformed.")
        val scheme = splits[0]
        if(scheme != BEARER_SCHEME) throw AuthenticateException("Scheme is not Bearer.")
    }

    private fun extractAccessToken(authorizationHeader: String): String {
        val splits = authorizationHeader.split(" ")
        validateAuthorizationHeader(splits)
        return splits[1]
    }
}
```

- **위 필터에 `@Component` 어노테이션이 없는 것에 유의하자.**

- 우선, `validateAuthorizationHeader()`는 `Authorization`을 key로 하는 값이 헤더에 잘 왔는지 검증을 함과 동시에,  
  value로 온 JWT가 Bearer Scheme를 사용하는지를 검증한다.

- 다음으로 `extractAccessToken()`은 `validateAuthorizationHeader()`에서 나온 Authorization Header의  
  value로부터 Access Token을 추출하여 반환한다.

- 위 필터의 생성자에서 `JWTAuthenticationEntryPoint`가 아닌 `AuthenticationEntryPoint`을 자동으로 주입받도록  
  지정했는데, `JWTAuthenticationEntryPoint`가 컴포넌트로 등록되어 있기에 이렇게 작성해도 된다.

- 마지막으로 가장 중요한 `doFilterInternal()` 메소드는 try-catch로 묶여져 있다.  
  만약 인증 과정에서 예외가 발생한다면 try 블록의 가장 마지막에 있는 `filterChain.doFilter()`가 호출되지 않는다.  
  `filterChain.doFilter()`는 filterChain에 등록된 다음 filter를 수행하라는 뜻인데, 이 부분이 호출되지 않으면  
  Spring 입장에서는 필터를 수행할 수 없게된 것이므로 내부적인 문제가 판단하여 500(INTERNAL_SERVER_ERROR)를  
  반환하게 된다. 따라서 부득이하게 try-catch로 묶어준 것이다.

- 인증 과정에서 예외(`AuthenticationException`)이 발생한다면, catch 블록으로 이동하고 `authenticationEntryPoint.commence()`를  
  호출한다. 그러면 위에서 컴포넌트로 등록한 `JWTAuthenticationEntryPoint#commence()`가 호출되어 클라이언트에게  
  401(UNAUTHORIZED)의 상태 코드와 함께 정보성 메시지를 전달할 것이다.

- 또한, 인증이 성공했을 때 `JwtTokenUtil#getAuthentication()`을 호출하여 받은 값을 `SecurityContextHolder`의  
  `SecurityContext`의 authentication에 저장하는 부분을 보자. 이 메소드는 아래와 같다.

```kotlin
@Component
class JwtTokenUtil {

    // Other methods

    fun getAuthentication(token: String): UsernamePasswordAuthenticationToken {
        val userDetails = UserDetailsImpl(extractUserId(token))
        return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
    }
}
```

- `UsernamePasswordAuthenticationToken`은 `Authentication` 인터페이스의 구현체로,  
  principal, credentials, authorities를 필드로 가진다.  
  위의 `JwtTokenUtil#getAuthentication()`에서는 principal에 `UserDetailsImpl` 인스턴스를 전달하고,  
  credentials과 authorities로는 아무런 값도 주지 않았다.

<hr/>
