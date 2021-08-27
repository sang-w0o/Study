# LocalDateTime 반환하기

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
