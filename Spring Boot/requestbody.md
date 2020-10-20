<h1>@RequestBody에 대한 추가적인 정리</h1>

* 하나의 `JSONObject` 안에 또 다른 `JSONObject`가 들어간다고 하자.
```json
{
    "outer_string":"this is outer string",
    "outer_double":9.9,
    "outer_int":1,
    "inside": {
        "inside_string":"this is inside string",
        "inside_double":1.1,
        "inside_int":"2"
    }
}
```

* 이를 파싱하기 위해서는 컨트롤러 메소드에서 아래와 같이 기존에는 했었다.
```java
@RestController
public class TestApiController {

    @PostMapping("/test/v1")
    public void test(@RequestBody String requestObject) {
        org.json.JSONObject jsonObject = new org.json.JSONObject(requestObject);
        // 파싱 후 사용
    }
}
```

* 하지만 `Gson`과 비슷하게 클래스를 사용해서 별도의 파싱 없이 수행할 수도 있었다. 위 JSON 코드를 파싱하기 위해 아래의 두 클래스를 작성한다.
```java
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class TestDtoWrapper {

    private String outer_string;
    private double outer_double;
    private int outer_int;
    private TestDtoInside inside;
}
```

* 위 클래스는 바깥 JSONObject를 담당하는 클래스이다. 위 클래스는 `TestDtoInside`타입의 변수를 가지는데, 해당 타입은 아래와 같다.
```java
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class TestDtoInside {

    private String inside_string;
    private double inside_double;
    private int inside_int;
}
```

```java
@RestController
public class TestApiController {

    @PostMapping("/test/v1")
    public void test(@RequestBody TestDtoWrapper wrapper) {
        TestDtoInside inside = wrapper.getInside();
        // 사용
    }
}
```

* 이렇게 하면 가장 위에서 작성한 JSON문자열의 value가 알맞은 key에 대입된다. 단, 주의점은 아래와 같다.
* 위 코드에서 `TestDtoWrapper` 클래스는 `TestDtoInside` 변수의 이름을 inside로 가지는데, 이 __변수명과 JSON에서 JSONObject의 이름과 동일해야만__   
  파싱이 된다. 만약 이름이 같지 않다면, 각 값에 접근할 때 예외가 발생한다.