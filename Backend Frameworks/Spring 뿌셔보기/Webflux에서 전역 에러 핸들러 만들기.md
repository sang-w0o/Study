# Webflux에서 전역 에러 핸들러 만들기

## 예외 클래스 구조

- 예외 클래스들을 어떻게 구성할지는 매우 자유롭지만, 나는 개인적으로 MVC에서는 아래의  
  상속 계층 구조를 갖도록 예외 클래스를 구성했다. 이를 Webflux에서도 동일하게 사용할 수 있다.

```
ResponseStatusException
    |
    +- ApiException
        |
	+- BadRequestException
	+- NotFoundException
	+- ForbiddenException
	+- UnauthorizedException
	+- NotAcceptableException
	+- ConflictException
```

- 비즈니스 로직에서 발생하는 예외들은 모두 `ApiException`의 자식 클래스들이며,  
  이들은 모두 `ApiException`을 직접 상속하는 대신 `BadRequestException` 등을  
  상속해 예외 상황에 알맞은 HTTP 상태 코드를 갖도록 했다.

- 예를 들어 인증된 사용자가 아닐 때 던지도록 만들어진 `UserUnauthorizedException`은  
  `UnauthorizedException`을 상속받도록 했다.

```kt
// UnauthorizedException
@ResponseStatus(HttpStatus.UNAUTHORIZED)
abstract class UnauthorizedException(message: String) : ApiException(message, HttpStatus.UNAUTHORIZED)

// UserUnauthorizedException
class UserUnAuthorizedException : UnauthorizedException {
    constructor(message: String) : super(message)
    constructor() : super("인증되지 않은 사용자 입니다.")
}
```

- 참고로 `ResponseStatusException`은 기본적으로 HTTP 상태 코드를 `message` 프로퍼티에 함께 담아 반환한다.  
  즉, 만약 `BadRequestException`을 상속하는 클래스의 message로 "this is message"를 주게 되면,  
  이를 `ApiException` 타입으로 받아 message 프로퍼티를 읽어오면 `400 BAD REQUEST this is message"가  
  반환된다. 이를 피하기 위해 message 프로퍼티만 오버라이딩했다.

```kt
abstract class ApiException(message: String, status: HttpStatus) : ResponseStatusException(status, message) {
    override val message: String
        get() = reason ?: "No message provided."
}
```

<hr/>

## MVC의 전역 예외 핸들러

- MVC에서 전역 에러 핸들러를 구현하기는 꽤 간단하다. `@RestControllerAdvice` 혹은 `@ControllerAdvice`를  
  적용하고, `@ExceptionHandler`를 메소드에 적용해 각 예외를 처리하도록 하면 된다.  
  나는 편의상 `@RestControllerAdvice`가 붙은 클래스가 `ResponseEntityExceptionHandler`를  
  상속하도록 했다. 그리고 알맞은 메소드들을 오버라이드했다.

```kt
@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(value = [Exception::class])
    protected fun handleApiException(exception: Exception, request: WebRequest): ResponseEntity<Any> {
        return when (exception) {
            is ApiException -> {
                handleExceptionInternal(exception, null, HttpHeaders(), exception.status, request)
            }
            is AuthenticateException -> {
                handleExceptionInternal(exception, null, HttpHeaders(), HttpStatus.UNAUTHORIZED, request)
            }
            else -> handleExceptionInternal(exception, null, HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request)
        }
    }

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        val servletWebRequest = request as ServletWebRequest
        val errorResponseDto = ErrorResponseDto(LocalDateTime.now(), status.value(), status.reasonPhrase, ex.bindingResult.fieldErrors[0].defaultMessage!!, servletWebRequest.request.requestURI, servletWebRequest.request.remoteAddr)
        return ResponseEntity(errorResponseDto, headers, status)
    }

    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        val errorResponseDto: ErrorResponseDto
        val servletWebRequest = request as ServletWebRequest
        errorResponseDto = if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            ErrorResponseDto(LocalDateTime.now(), status.value(), status.reasonPhrase, "Internal Server Error", servletWebRequest.request.requestURI, servletWebRequest.request.remoteAddr)
        } else {
            ErrorResponseDto(LocalDateTime.now(), status.value(), status.reasonPhrase, ex.message!!, servletWebRequest.request.requestURI, servletWebRequest.request.remoteAddr)
        }
        return ResponseEntity(errorResponseDto, headers, status)
    }
}
```

- 참고로 `handleExceptionInternal`에서 사용하는 `ErrorResponseDto`는 아래와 같다.

```kt
data class ErrorResponseDto(
    @field:JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd HH:mm:ss",
        locale = "Asia/Seoul"
    )
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val remote: String?
)
```

<hr/>

## Webflux의 전역 에러 핸들러

- MVC의 `GlobalExceptionHandler`를 Webflux에서 동일하게 구현하기 위해서는  
  `AbstractErrorWebExceptionHandler`를 상속하는 클래스를 정의해야 한다.  
  바로 코드를 보자.

```kt
@Component
@Order(-2)
class GlobalExceptionHandler(
    errorAttributes: GlobalErrorAttributes,
    applicationContext: ApplicationContext,
    configurer: ServerCodecConfigurer,
) : AbstractErrorWebExceptionHandler(
    errorAttributes, WebProperties.Resources(), applicationContext
) {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    init {
        this.setMessageReaders(configurer.readers)
        this.setMessageWriters(configurer.writers)
    }

    override fun getRoutingFunction(errorAttributes: ErrorAttributes): RouterFunction<ServerResponse> {
        return RouterFunctions.route(RequestPredicates.all(), this::createErrorResponse)
    }

    private fun createErrorResponse(request: ServerRequest): Mono<ServerResponse> {

        val throwable = getError(request)
        var status = HttpStatus.INTERNAL_SERVER_ERROR
        when (throwable) {
            is ResponseStatusException -> {
                status = throwable.status
            }
            else -> logger.error(throwable.stackTraceToString())
        }
        val errorPropertiesMap = getErrorAttributes(request, ErrorAttributeOptions.defaults())
        return ServerResponse
            .status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(errorPropertiesMap))
    }
}
```

- `@Order(-2)`를 지정해준 이유는 Webflux가 기본적으로 제공하는 `DefaultErrorWebExceptionHandler`가  
  `@Order(-1)`이 적용되어 있기 때문이다. 더 낮은 값을 주어 `GlobalExceptionHandler`가 먼저 작동하도록  
  지정한 것이다.

- 초기화 (init) 부분에서 설정해준 것은 HTTP 본문(body)를 읽고 쓸 객체들을 설정한 것인데,  
  별도 설정 없이 기본적으로 제공되는 `ServerCodecConfigurer`를 사용했다.

- `createErrorResponse()` 메소드는 처음에 `AbstractErrorWebExceptionHandler#getError()`를  
  사용해 `ServerRequest`에서 예외 인스턴스를 가져온다. 그리고 예외의 타입에 알맞게  
  응답을 반환한다. 이때, 한 가지 알아둬야 할 것은 `GlobalExceptionHandler`의 생성자 매개변수 중  
  `GlobalErrorAttributes`가 있는 것이다. 이 클래스는 `DefaultErrorAttributes`를  
  상속한 클래스로 아래와 같다.

```kt
@Component
class GlobalErrorAttributes : DefaultErrorAttributes() {

    @Autowired
    private lateinit var clock: Clock

    override fun getErrorAttributes(request: ServerRequest, options: ErrorAttributeOptions): MutableMap<String, Any> {
        val map = mutableMapOf<String, Any>()
        val throwable = getError(request)
        fillErrorAttributes(request, throwable, map)
        return map
    }

    private fun fillErrorAttributes(request: ServerRequest, throwable: Throwable, map: MutableMap<String, Any>) {
        fillCommonAttributes(request, throwable, map)
        when (throwable) {
            is ResponseStatusException -> {
                map["status"] = throwable.status.value()
            }
            else -> {
                map["status"] = HttpStatus.INTERNAL_SERVER_ERROR.value()
            }
        }
    }

    private fun fillCommonAttributes(request: ServerRequest, throwable: Throwable, map: MutableMap<String, Any>) {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        map["timestamp"] = dateTimeFormatter.format(LocalDateTime.now(clock))
        map["message"] = throwable.message!!
        map["path"] = request.path()
        val optionalRemoteAddress = request.remoteAddress()
        var remoteAddress = "UNKNOWN"
        if (optionalRemoteAddress.isPresent) {
            remoteAddress = optionalRemoteAddress.get().toString()
        }
        map["remote"] = remoteAddress
    }
}
```

- `GlobalErrorAttributes`를 상속한 이유와 필요성은 두 개다.

  - (1) `AbstractErrorWebExceptionHandler#getError()`를 인스턴스화할 때 `DefaultErrorAttribute`  
    타입의 매개변수를 받아야만 한다.

  - (2) 기본적으로 제공되는 에러 응답 본문에 마음대로 원하는 값을 넣을 수 있다.  
    나의 경우 `fillCommonAttributes()`와 `fillErrorArrtibutes()`를 통해  
    응답 본문에 timestamp, messate, path, remote, status를 추가했다.  
    기본적으로 `DefaultErrorAttributes`는 응답 본문에 timestamp, path, requestId, status,  
    error, message 등을 포함한다.

- 다시 `GlobalExceptionHandler`로 돌아가 `getRoutingFunction()`을 재정의한 부분을 보자.
  위 메소드가 반환하는 것은 `return RouterFunctions.route(RequestPredicates.all(), this::createErrorResponse)`  
  이다. 우선 `RouterFunctions.route()`는 아래처럼 정의되어 있다.

```java
public static <T extends ServerResponse> RouterFunction<T> route(
    RequestPredicate predicate, HandlerFunction<T> handlerFunction) {
    return new DefaultRouterFunction<>(predicate, handlerFunction);
}
```

- 첫 번째 인자인 `RequestPredicate`가 true를 반환하면, 두번째 인자로 들어온 `HandlerFunction<T>`가  
  에러를 처리한다. `GlobalExceptionHandler`에서는 첫 번째 인자로 `RequestPredicates.all()`을  
  주어 모든 요청에 대해 두 번째 인자(`this::createErrorResponse`)가 처리하도록 했다.

```java
public abstract class RequestPredicates {

    /**
     * Return a {@code RequestPredicate} that always matches.
     * @return a predicate that always matches
     */
    public static RequestPredicate all() {
	return request -> true;
    }
    //..
}
```

- `HandlerFunction`은 함수형 인터페이스이다.

```java
@FunctionalInterface
public interface HandlerFunction<T extends ServerResponse> {

    /**
     * Handle the given request.
     * @param request the request to handle
     * @return the response
     */
    Mono<T> handle(ServerRequest request);
}
```

- 참고로 bean validation 실패 시 예외 처리를 위해서 MVC에서는 `ResponseEntityExceptionHandler`의  
  `handleMethodArgumentNotValid()` 메소드를 재정의하면 되는 반면, Webflux에서는 조금 다르게  
  처리해줘야 하는 것 같다.

- 우선 JSON Body가 애초에 잘못 되었을 때 발생하는 예외는 `org.springframework.cord.codec.DecodingException`이다.  
  이를 위한 별도의 예외 처리 핸들러와, Bean validation이 실패했을 때 발생하는  
  `org.springframework.bind.support.WebExchangeBindException`을 처리할 핸들러를 정의했다.

```kt
@RestControllerAdvice
class RequestBodyValidatingExceptionHandler {

    @ExceptionHandler(DecodingException::class)
    fun handleDecodingException(exception: DecodingException) {
        throw InvalidMemberFieldException("Wrong request body received.")
    }

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleWebExchangeBindException(exception: WebExchangeBindException) {
        val error = exception
            .bindingResult
            .allErrors
            .stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.toList())[0]
        throw InvalidMemberFieldException(error ?: "Invalid member field.")
    }
}
```

- Bean Validation이 실패했을 때는 내가 직접 지정한 예외 메시지를 에러 응답 본문에  
  넣기 위해 해당 에러 메시지를 가져와 `InvalidMemberFieldException`을 던지도록 했다.  
  이 예외는 단순히 `BadRequestException`을 상속하는 클래스다.

```kt
class InvalidMemberFieldException(message: String) : ApiException(HttpStatus.BAD_REQUEST, message)
```

<hr/>
