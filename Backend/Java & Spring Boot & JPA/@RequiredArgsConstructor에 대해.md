<h1>@RequiredArgsConstructor</h1>

- 우선 `@RequiredArgsConstructor`는 `Lombok`에서 제공하는 어노테이션이다.  
  이 어노테이션은 초기화 되지 않은 `final` 필드나. `@NonNull` 어노테이션이 붙어 있는  
  필드에 대해 생성자를 생성해준다.  
  이렇게 생성자를 통해 의존성을 주입하는 것을 **생성자를 통한 의존성 주입** 이라 한다.

- 나는 아래와 같이 이 어노테이션을 평상시에 사용하곤 했다.

```java
@RequiredArgsConstructor
@RestController
public class TestController {

    private final TestService testService;

    @GetMapping("/test")
    public String testGet() {
        return testService.testGet();
    }
}
```

- 위 코드에서 `TestController` 클래스는 `TestService`에 대해 의존성을 가진다.  
  만약 `@RequiredArgsConstructor`를 사용하지 않는다면 의존성 주입을 직접 해야할 것이다.

```java
@RestController
public class TestController {

    @Autowired
    private TestService testService;

    @GetMapping("/test")
    public String testGet() {
        return testService.testGet();
    }
}
```

- 위 코드에서는 `@Autowired` 어노테이션으로 `TestController`에 `TestService`로의  
  의존성을 주입해 주었다.
