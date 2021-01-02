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

    // 시용자 정보를 수정하는 URL
    @PatchMapping("/v2/users/{userId}")
    public void updateUserInfo(@RequestBody UserSignupRequestDto requestDto, @PathVariable Integer userId, @RequestHeader(name = "Authorization") String bearerToken, HttpServletResponse response) {
        try {
            if(bearerToken == null) throw new AuthenticateException();
            if(userId == null) throw new Exception();
            WriteToClient.send(response, usersService.updateUserInfo(userId, requestDto, bearerToken), HttpServletResponse.SC_OK);
        } catch(UserException exception) {
            if(exception instanceof UserIdNotFoundException) {
                WriteToClient.send(response, ObjectMaker.getJSONObjectWithException(exception), HttpServletResponse.SC_NOT_FOUND);
            } else if(exception instanceof UserEmailInUseException) {
                WriteToClient.send(response, ObjectMaker.getJSONObjectWithException(exception), HttpServletResponse.SC_CONFLICT);
            } else if(exception instanceof UserNotFoundException) {
                WriteToClient.send(response, ObjectMaker.getJSONObjectWithException(exception), HttpServletResponse.SC_NOT_FOUND);
            } else if(exception instanceof UserInvalidAccessException) {
                WriteToClient.send(response, ObjectMaker.getJSONObjectWithException(exception), HttpServletResponse.SC_FORBIDDEN);
            }
        } catch(AuthenticateException exception) {
            WriteToClient.send(response, ObjectMaker.getJSONObjectWithException(exception), HttpServletResponse.SC_UNAUTHORIZED);
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

