# Stateless는 무엇일까?

## Stateless의 반대, Stateful

- Stateful : 서버에 클라이언트와 서버 사이의 동작, 상태 정보 등을 저장한다.  
  이때, 저장하는 방식에는 세션 등이 사용된다.

- Stateless : 서버에 클라이언트와 서버의 동작, 상태 정보 등을 **저장하지 않는 것**  
  즉, **서버의 응답이 클라이언트와의 세션 상태와 독립적으로 수행된다**.

- 아래는 Stateful한 서버 애플리케이션에서 session을 활용하는 간단한 예시이다.

```kt
@GetMapping("/asdf")
fun stateful(httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse) {
  val httpSession = httpServletRequest.session;
  val userIdFromSession = httpSession.getAttribute("user_id")
}

@GetMapping("/asdf2")
fun stateful2(httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse) {
  val httpSession = httpServletRequest.getSession(true)
  val userId = 10
  httpSession.setAttribute("userId", userId)
}
```

- 위처럼 `HttpServletRequest`에서 `getSession(boolean create)` 메소드로 `HttpSession` 객체를 사용할 수 있다.  
  만약 create 인자가 true라면, 기존에 `HttpServletRequest`에 session이 있다면 그 세션을 반환하고, 없다면 새로 생성해서 반환한다.  
  반대로 만약 false라면, 기존에 session이 없다면 null을 반환한다.

---

## Stateless한 애플리케이션에서 정보 활용

- 만약 특정 사용자의 ID가 있어야만 작동하는 API가 있다고 해보자.  
  기존에 Stateful한 애플리케이션이라면, session에 사용자의 ID를 attribute로 넣어두어 통신을 할 수 있을 것이다.  
  그렇다면 Stateless한 애플리케이션에서는 이러한 작업을 어떻게 수행할까?

- 여러 가지 방법이 있겠지만, 그 중 흔하게 사용되는 방법이 JWT(Json Web Token)을 사용하는 것이다.  
  JWT는 아래와 같은 구조로 되어 있다.

![picture 1](/images/OTHERS_JWT_1.png)

- 위와 같이 `Header`, `Payload`, `Signature`의 3가지 부분으로 나뉘어 지며, `.`을 구분자로 사용한다.

- 헤더(Header): 헤더는 아래의 2가지 정보를 가진다.

  - `typ` : 토큰의 타입 지정
  - `alg` : 서명(Signature)에 대한 해싱 알고리즘이다. 보통은 SHA256 또는 RSA가 사용되며, 이 알고리즘은  
    서버에서 추후에 JWT를 검증할 때 사용되는 Signature 부분에서 사용된다.

- 정보(Payload) : Payload에는 해당 JWT에 담을 정보들이 지정된다.  
  이 Payload에 담는 정보의 한 조각을 `Claim(클레임)`이라 하며, 이는 JSON의 key-value 형식으로 되어있다.  
  Claim의 종류는 크게 `Registered Claim`, `Public Claim`, `Private Claim`으로 나뉜다.

  - `Registered Claim(등록된 클레임)` : 등록된 클레임들은 서비스에서 필요한 정보가 아닌, 토큰 자체에 대한 정보들을 담는데에 사용된다.  
    예를 들어, 대표적으로 토큰의 만료 시간을 나타내는 `exp`와 같은 정보들이 이에 해당한다.

  - `Public Claim(공개 클레임)` : 공개 클레임들은 고유한 이름(충돌이 되면 안된다.)을 가져야 한다. 이를 위해 클레임 이름을 URI 형식으로 짓는다.
  - `Private Claim(비공개 클레임)` : 이 부분이 서버와 클라이언트 사이의 협의 하에 사용되는 클레임이다.  
    위에서 예시로 들었던 userId와 같은 정보들이 이 부분에 포함된다.

- 서명(Signature) : 토큰을 인코딩하거나 유효성을 검증할 때 사용되는 **고유한 암호화 코드** 이다.

- 서버에서 필요한 정보들을 포함하여 JWT를 발급해주면, 클라이언트는 이 토큰을 활용해서 서버와 통신한다.  
  보통 이 토큰은 HTTP Header에 추가되어 사용되며, Key 값으로는 `Authorization`, value로는 `Bearer ${token}`의 형식으로 사용된다.

---

## 코드로 보기

- 아래에서 사용되는 JWT와 관련된 부분은 `io.jsonwebtoken:jjwt:0.9.1` 라이브러리에서 제공해준다.

```kt
@Component
class JwtTokenUtil {

  // 토큰 생성 및 검증에 사용되는 고유 key
  @Value("\${jwt.secret}")
  lateinit var secretKey: String

  companion object {
    // 토큰의 exp(만료 시간) : 발급일로부터 1일(단위 : ms)
    private const val TOKEN_EXP: Int = 86400000
  }

  // 클레임으로부터 userId 추출
  private fun getUserId(claim: Claims): Int {
    try {
      return claim.get("userId", Int::class.javaObjectType)
    } catch(e: Exception) {
      throw AuthenticateException("JWT Claim에 userId가 없습니다.")
    }
  }

  // exp(만료 일자) 추출
  private fun extractExp(token: String): Date {
    return extractClaim(token, Claims::getExpiration)
  }

  // 토큰으로부터 모든 Claim 추출
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

  // 토큰 생성
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

  // 토큰이 만료되었는지의 유무 검증
  fun isTokenExpired(token: String): Boolean {
    return extractExp(token).before(Date())
  }

  // 토큰 생성
  fun generateToken(userId: Int): String {
    val claims: MutableMap<String, Any> = mutableMapOf()
    claims["userId"] = userId
    return createToken(claims, TOKEN_EXP)
  }
}
```

- 이렇게 생성된 토큰을 보면, 아래와 같다.  
  아래는 userId가 10인 클레임이 들어있는 JWT Token을 생성한 결과이다.

```
eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEwLCJpYXQiOjE2MjA2NDM1NDMsImV4cCI6MTYyMDcyOTk0M30.tmdJjLfSummaW_-5lOZcj1vpMAoWu0oLKDDW64y4K3Y
```

- 2개의 `.`이 있는 것을 확인할 수 있다.
- 위에서 얻은 토큰을 <a href="jwt.io">디코더</a> 에서 디코딩 해보면, 아래의 결과가 나온다.

![picture 2](/images/OTHERS_JWT_2.png)

- Private Claim에 넣은 userId가 다 노출되었는데, 이게 과연 안전한 것일까?!

## JWT의 장단점

### 장점

- (1): 별도의 데이터베이스 테이블 없이 인증을 구현할 수 있다.

  - 이는 곧 더 적은 Query를 의미하고, 이는 곧 더 빠른 응답 시간으로 이어진다.  
    만약 AWS DynamoDB와 같이 query 개수에 따라 이용료를 지불해야 하는 서비스를 사용한다면, JWT를 사용하는 것은  
    이러한 비용을 매우 효과적으로 줄여줄 수 있다.  
    물론 session의 경우, Redis 등을 사용해서 해결할 수는 있다.

- (2): 조심한다면 매우 간편하게 사용할 수 있다.

  - 만약 아키텍쳐가 client의 session을 사용하지 않는다면, 기존에 존재하는 라이브러리를 가져다가 개발을 하는 것보다  
    JWT를 사용한 인증 방식을 사용하는 것이 개발 시간을 줄여준다.

- (3): 서비스에 구애받지 않고 사용될 수 있다.
  - 로그인, 회원 가입 등을 수행하며 JWT를 생성하는 서버를 단 1개만 만들어도 된다.  
    Private Key는 인증 서버만 가지고 있기 때문에 모든 요청이 인증 서버로 갈 필요가 없다.  
    시그니처를 검증하기 위해 각 서버는 public key만 가지고 있어도 된다.  
    또한 인증 서버와 나머지 서버 사이에 public key를 제외한 의존성 및 연결이 필요 없다는 점도 큰 이점이다.

### 단점

- (1): Secret Key를 탈취당하면 정말 별 수 없다.

  - JWT의 가장 큰 장점이자 단점이 단 하나의 Key(Secret Key)에만 의존한다는 점이다.  
    만약 개발자의 실수로 인해 이 Secret key가 노출된다면, JWT 인증을 사용하는 모든 시스템이 탈취당한거나 마찬가지이다.  
    만약 공격자(secret key를 탈취한 자)가 비교적 알기 쉬운 특정 사용자의 userId를 가지게 된다면, 해당 사용자의 모든  
    데이터에 접근 권한을 가지게 된다. 이런 상황이 발생했을 때, 해결할 수 있는 유일한 방법은 Secret Key를 새롭게  
    발급하는 것이다. 이는 곧 변경되기 전의 Secret Key로 생성된 JWT를 가지고 있는 클라이언트를 모두 로그아웃 시키는 것과  
    같은 말이다.

- (2): 서버단에서 클라이언트를 관리할 수 없다.

  - 사용자가 단순히 _모든 기기에서 로그아웃 하기_ 버튼을 클릭하여 모든 기기의 세션에서 로그아웃 하도록 할 수 있게 할 수 없다.  
    로그아웃을 하려면 클라이언트가 가진 token을 삭제해줘야 하는데, 이는 불가능하기 때문이다.

- (3): 클라이언트에게 push 알림을 보낼 수 없다.

  - 실시간으로 로그인 된 클라이언트에 대한 정보가 DB단에 없기에, 모든 클라이언트에게 push 알림을 보낼 수 없다.  
    왜냐하면 해당 사용자가 로그인한 모든 클라이언트(e.g. 기기)를 파악할 수 없기 때문이다.  
    물론 Firebase FCM이 하는 것처럼 Device Token을 만들어서 구현할 수도 있지만, 이는 곧 DB상에  
    테이블을 추가하는 것이 되고, 이렇게 되면 JWT인증의 장점이 사라지는 것이다.

- (4): Data Overhead

  - JWT Token의 크기(길이)는 일반적인 Session Token보다 크다. JWT Token에 더 많은 정보를 담을 수록  
    그에 비례하여 JWT의 길이도 길어진다. 요청을 할 때 항상 token을 보내야 한다는 점을 생각해보자.  
    만약 JWT Token이 1KB가 된다면, 요청을 보낼 때에 1KB만큼의 업로드 overhead가 발생할 것이고, 이는 곧  
    통신 속도를 늦출 것이다. Session Token은 크기가 매우 작으면서도 보안을 갖추는것에 비해, JWT는 payload까지  
    개발자가 고려하여 생성해야 한다.

---
