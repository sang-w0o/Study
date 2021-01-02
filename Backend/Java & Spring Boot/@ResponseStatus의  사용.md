<h1>@ResponseStatus의 사용</h1>

<h2>@ResponseStatus를 알기 전의 코드</h2>

* `@ResponseStatus`를 알기 전에는, 예외를 일일히 try-catch문으로 감싸 각각의 예외에 적절한   
  Http Status code를 전달해야 했다.

* 실제로 내가 작성했던 코드는 아래와 같다.

* 우선 아래 `WriteToClient`는 중복되는 코드를 제거하고, 요청 클라이언트에게 JSONObject를 반환하는 클래스이다.   
  (이 때만 해도 `@RestController`가 붙은 클래스의 메소드가 DTO를 자동으로 JSONObject로 변환해준다는 것을 몰랐다..)   

```java
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.json.JSONObject;

public class WriteToClient {

    public static void send(HttpServletResponse response, JSONObject jsonObject, Integer statusCode) {
        try {
            response.setStatus(statusCode);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.getWriter().println(jsonObject.toString());
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
```

* 그리고 `@RestController`가 DTO를 자동으로 JSONObject로 변환하여 반환해준다는 것을 몰랐기 때문에,   
  서비스 코드에서 JSONObject를 반환하는 것이 맞다고 생각했다. 그 결과 나온 코드가 아래와 같다.
```java
@RequiredArgsConstructor
@RestController

public class UsersApiController {

    // 시용자 정보를 조회하는 API
    @GetMapping("/v2/users/{userId}")
    public void getUserInfo(@PathVariable Integer userId) {

        try {
            if(userId == null) throw new Exception();
            WriteToClient.send(response, usersService.viewUserInfo(userId, bearerToken), HttpServletResponse.SC_OK);
        } catch(UserIdNotFoundException exception) {
            WriteToClient.send(response, ObjectMaker.getJSONObjectWithException(exception), HttpServletResponse.SC_NOT_FOUND);
        } catch(Exception exception) {
            WriteToClient.send(response, ObjectMaker.getJSONObjectOfBadRequest(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
```

* 위 코드 처럼, Controller의 모든 코드는 위와 같은 구조를 가지게 되었다.   
  try-catch의 남발이 있었으며, 중복되는 코드가 매우 많았다.

* 이때, 같이 개발하던 동료를 통해 `@ResponseStatus`가 있음을 알게 되었다.   
<hr/>

<h2>@ResponseStatus 소개</h2>

* `@ResponseStatus`는 Spring에서 제공하는 어노테이션으로, `org.springframework.web.bind.annotation` 패키지 내에 있다.

* `@ResponseStatus`의 코드는 아래와 같다.
```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseStatus {

	@AliasFor("code")
	HttpStatus value() default HttpStatus.INTERNAL_SERVER_ERROR;

	@AliasFor("value")
	HttpStatus code() default HttpStatus.INTERNAL_SERVER_ERROR;

	String reason() default "";
}
```

* `@Target`이 `TYPE`, `METHOD`에 있으므로, 이 어노테이션은 클래스, 인터페이스, ENUM,   
  그리고 메소드에 적용할 수 있다.

* 또한 전달 값으로는 `HttpStatus`의 열거형 변수 중 하나를 전달할 수 있다.   
  `HttpStatus`는 `OK(200)`, `BAD REQUEST(400)` 등 Http status code들이 정의된 열거형 클래스이다.

* 이 어노테이션을 사용하면, 각종 예외에 대한 response status처리를 간편하게 할 수 있다.

* `@ResponseStatus`를 사용하면, Spring은 내부적으로 `HttpServletResponse#sendError()`를 호출한다.   
  위 메소드가 호출되는 기준은 아래와 같다.
  * `@ResponseStatus`를 메소드에 적용하고, __reason__ 이 제공되는 경우
  * `@ResponseStatus`를 예외 클래스에 적용한 경우

* `HttpServletResponse#sendError()`를 사용한다는 것은 클라이언트에게 HTML Error Page를 전달함을 의미한다.   
  따라서 REST Controller 메소드에 `@ResponseStatus`를 적용하는 경우, __reason__ 을 전달하지 않아야 한다.

* 나는 기존 코드에 대한 예외 처리를 아래와 같이 바꿀 수 있었다.   
  <a href="https://github.com/sangwoo-98/Study/blob/master/Backend/Java%20%26%20Spring%20Boot/RuntimeException%20vs%20Exception.md">기존 예외처리 코드</a>
<hr/>

<h2>@ResponseStatus 적용하기</h2>

* 기존 코드에 `@ResponseStatus`를 적용해보자.

* 우선 아래는 예외 클래스이다. 모든 예외처리 클래스들은 아래와 같은 구조로 값만 다르게 작성되어 있다.

```java
// 404(NOT FOUND)를 반환하는 예외 클래스
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException() {
        this("Not found exception.");
    }
}

// NotFoundException을 상속받는 UserIdNotFoundException(id값으로 조회된 결과가 없을 때 사용)
public class UserIdNotFoundException extends NoContentException {
    public static final String MESSAGE = "해당 id로 조회된 결과가 없습니다.";

    public UserIdNotFoundException() {
        super(MESSAGE);
    }
}
```

* 아래는 사용자의 정보를 조회하는 서비스 코드이다.
```java
@RequiredArgsConstructor
@Service
public class UserService {

    private final UsersRepository usersRepository;

    @Transactional(readOnly = true)
    public UserInfoResponseDto getUserInfo(Integer userId) {
        return new UserInfoResponseDto(usersRepository.findById(userId).orElseThrow(UserIdNotFoundException::new));
    }
}
```

* 마지막으로 아래는 위의 서비스 코드를 호출하는 부분이다.
```java
@RequiredArgsConstructor
@RestController
public class UsersApiController {

    private final UsersService usersService;

    @ValidateRequired
    @GetMapping("/v3/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserInfoResponseDto getUserInfo(@PathVariable Integer userId, @RequestAttribute(name = "accessToken") String token) {
        return usersService.getUserInfo(userId, token);
    }
}
```

* 위와 같이 코드 구조가 기존에 비해 말도 안되게 간결해졌음을 알 수 있다.
<hr/>

<a href="https://www.javacodegeeks.com/2019/05/using-responsestatus-http-status-spring.html#:~:text=We%20can%20use%20%40ResponseStatus%20to,one%20defined%20using%20%40ResponseStatus%20annotation.">참고 링크</a>