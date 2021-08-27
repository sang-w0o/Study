# LocalDateTime의 직렬화

- Java8에 등장한 `LocalDateTime`은 특정 날짜 및 시간에 대한 정보를 담고 있는 객체이다.  
  이 객체를 그 자체로 Request Body로 반환하면 어떻게 되는지 보자.

- 아래는 예시로 반환될 dto의 모습이다.

```kt
data class SimpleLocalDateTimeDto(
    val dateTime: LocalDateTime
)

// API 호출 시 아래의 코드로 SimpleLocalDateTimeDto가 생성된다.

@Service
class TestService {

    fun test(): SimpleLocalDateTimeDto {
        return SimpleLocalDateTimeDto(LocalDateTime.now())
    }
}
```

- 위 코드대로 실행하고, 간단한 API를 만들어 호출해보면, 아래와 같은 응답이 온다.

```json
{
  "dateTime": [2021, 8, 27, 21, 42, 10, 915000000]
}
```

- 총 8개의 int 형 값을 가진 배열이 JSON 형식으로 오는 것을 볼 수 있는데,  
  위와 동일한 객체를 콘솔에 `println()`으로 출력해보면 `2021-08-27T21:42:10.915`가 나온다.  
  즉 콘솔에 출력된 값이 숫자 단위로 짤려서 json의 응답에 배열로 가게 되는 것이다.

- 실제로 만약 시간에 대한 정보를 클라이언트에게 위와 같이 넘겨주면, 클라이언트가 다양한 라이브러리(내장 또는 외부)를  
  사용하여 위의 데이터로 연산을 하는데에 불편함을 겪을 것이다.  
  이를 어떻게 해결할지 알아보자.

<h2>String으로 변환하여 반환해주기</h2>

- JSON에는 지정된 시간에 대한 형식이 없기 때문에, 클라이언트와 협의하여 문자열 형식으로 넘겨줘도 문제가 없다.  
  나는 아래와 같은 유틸리티 클래스를 만들어서 `LocalDateTime`을 원하는 형식의 String으로 변환하여 반환해주었다.

```kt
class LocalDateTimeConverter {
    companion object {
        fun convert(localDateTime: LocalDateTime): String {
            return localDateTime.format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            )
        }
    }
}
```

- 이제 Dto도 아래와 같이 변경하면, `2021-8-27 21:42:10`와 같은 형식의 문자열이 출력된다.

<hr/>

<h2>@JsonFormat</h2>

- Jackson에서 제공하는 어노테이션인 `@JsonFormat`을 사용하여 포맷을 바로 지정할 수 있다.  
  아래처럼 어노테이션을 적용해보자.

```kt
data class SimpleLocalDateTimeDto(
    @field:JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd HH:mm:ss",
        locale = "Asia/Seoul"
    )
    val dateTime: LocalDateTime
)
```

<hr/>

- 이제 응답(Response)에 `LocalDateTime`객체를 반환해줄 때 어떻게 직렬화를 할 수 있는지 알아보았다.  
  다음으로는 반대로 요청(Request)에서 날짜 형식을 받아 `LocalDateTime`으로 처리하는 방식을 알아보자.

- 우선 클라이언트에서 `GET` 요청을 보낸다고 해보자.  
  예시로 `/test/yyyy-MM-dd HH:mm:ss` 형식으로 Path Variable로 날짜 값을 넘겨주고, Spring에서 이를 받아  
  파싱해야 한다고 해보자.

```kt
@RestController
class TestController(
    private val testService: TestService
) {

    @GetMapping("/test/{localDateTime}")
    fun testGet(@PathVariable localDateTime: LocalDateTime): SimpleLocalDateTimeDto {
        return testService.test(localDateTime)
    }
}
```

- 우선 위 처럼 컨트롤러를 만들어 놓고, 요청을 `/test/2021-08-27T10:11:22`으로 보내면, 아래와 같은 에러가 출력된다.

```
Failed to convert value of type 'java.lang.String' to required type 'java.time.LocalDateTime'; nested exception is org.springframework.core.convert.ConversionFailedException: Failed to convert from type [java.lang.String] to type [@org.springframework.web.bind.annotation.PathVariable java.time.LocalDateTime] for value '2021-08-27T10:11:22'; nested exception is java.lang.IllegalArgumentException: Parse attempt failed for value [2021-08-27T10:11:22]
```

- 즉 `@PathVariable`이 받은 값의 타입은 String이지만, 이를 `LocalDateTime`으로 파싱할 수 없다는 뜻이다.  
  이때 Spring에서 제공하는 `@DateTimeFormat` 어노테이션을 사용하여 String이 `LocalDateTime`으로 변환된 값을 받을 수 있다.  
  아래와 같이 어노테이션을 적용해보자.

```kt
@RestController
class TestController(
    private val testService: TestService
) {

    @GetMapping("/test/{localDateTime}")
    fun testGet(
        @PathVariable
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        localDateTime: LocalDateTime): SimpleLocalDateTimeDto {
        return testService.test(localDateTime)
    }
}
```

- 이렇게 하면 정상적으로 값을 `LocalDateTime`으로 받아오는 것을 확인할 수 있다.  
  `@PathVariable`이 아닌 `@RequestParam`으로 값을 넘겨받아도 동일하게 `@DateTimeFormat` 어노테이션을  
  적용해주면 값을 잘 파싱하여 가져온다.

```kt
// @RequestBody에 지정될 Dto class
data class SimpleRequestDto(
    val dateTime: LocalDateTime = LocalDateTime.now()
)

// Controller 코드
@PostMapping("/test")
fun testRequestBody(@RequestBody dto: SimpleRequestDto): SimpleLocalDateTimeDto {
    return SimpleLocalDateTimeDto(dto.dateTime)
}
```

- 위 API에 아래와 같이 `LocalDateTime`이 `toString()`이 호출되었을 때의 포맷을 그대로 전달하면  
  별도의 세팅 없이 정상적으로 처리된다. 하지만 `yyyy-MM-dd HH:mm:ss` 와 같이 커스텀 포맷을 사용하면  
  아래의 에러가 발생한다.

```
JSON parse error: Cannot deserialize value of type `java.time.LocalDateTime` from String \"2021-08-27 10:11:22\": Failed to deserialize java.time.LocalDateTime: (java.time.format.DateTimeParseException) Text '2021-08-27 10:11:22' could not be parsed at index 10; nested exception is com.fasterxml.jackson.databind.exc.InvalidFormatException: Cannot deserialize value of type `java.time.LocalDateTime` from String \"2021-08-27 10:11:22\": Failed to deserialize java.time.LocalDateTime: (java.time.format.DateTimeParseException) Text '2021-08-27 10:11:22' could not be parsed at index 10\n at [Source: (PushbackInputStream); line: 2, column: 17] (through reference chain: com.template.common.dto.SimpleRequestDto[\"dateTime\"])
```

- 이렇게 기본 `LocalDateTime` 형식이 아닌 다른 형식을 String이 아니라 `LocalDateTime`으로 `@RequestBody`가 적용된  
  클래스에서 사용하려면 `@JsonFormat`을 사용하면 된다. 특이한 점은 `@DateTimeFormat`으로는 해결할 수 없다는 것이다.

```kt
data class SimpleRequestDto(
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val dateTime: LocalDateTime = LocalDateTime.now()
)
```

<hr/>

<h2>@DateTimeFormat vs @JsonFormat</h2>

- `@DateTimeFormat`은 Spring이 제공하는 어노테이션인 반면, `@JsonFormat`은 Jackson이 제공하는 어노테이션이다.  
  Spring에서는 Jackson을 직렬화, 역직렬화에 사용하지만 Jackson은 꼭 Spring과 같이 사용되지 않는다.
- 따라서 직렬화, 역직렬화가 필요한 Request Body, Response Body의 경우에는 `@JsonFormat` 어노테이션을 사용하여  
  Jackson이 직렬화를 할 수 있도록 해주고, 반대로 URL에 전달되는 Path Variable 또는 Request Parameter는  
  직렬화가 필요없기에 Spring이 제공하는 `@DateTimeFormat` 어노테이션을 사용하는 것이다.

<hr/>

<h2>결론</h2>

- `LocalDateTime`을 String으로 변환하거나, String을 `LocalDateTime`으로 변환하지 않고 `LocalDateTime`만 사용하려면

  - `GET`의 Path Variable 또는 Request Parameter에는 `@DateTimeFormat`
  - Request Body에는 `@JsonFormat`
  - Response Body로 줄 때는 `@JsonFormat`

<hr/>
