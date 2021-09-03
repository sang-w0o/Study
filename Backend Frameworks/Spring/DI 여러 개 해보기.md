# DI 여러 개 해보기

- Spring에서 무시 못하는 큰 장점 중 하나가 바로 DI(Dependency Injection)이다.  
  우선 의존성 주입을 사용하려면 구현체를 Spring Bean으로 등록해야 한다.  
  Spring Boot에는 `@SpringBootApplication` 어노테이션을 `main()` 메소드가 있는  
  클래스에 붙여줌으로써 `@Component` 또는 `@Bean` 어노테이션이 적용된 클래스들을  
  인스턴스화하여 Spring Bean 컨테이너에 등록하고, 필요한 시점에 생성된 인스턴스를 주입한다.

- 여기서는 `@Service` 어노테이션을 통해 예시를 살펴볼 것인데,  
  기본적으로 `@Service`, `@Repository`, `@Controller`, `@RestController` 등의  
  어노테이션은 구현된 부분을 보면 `@Component` 어노테이션이 적용되어 있다.  
  조금 더 자세한 내용을 보려면 <a href="https://github.com/sang-w0o/Study/blob/master/Backend%20Frameworks/Spring/%40Component%2C%20%40Service%2C%20%40Controller%2C%20%40RestController.md">이 문서</a>를 참고하자.

- 여기서 해보고 싶은건 **같은 인터페이스를 구현하는 구현체를 2개 이상 만들고, Spring이 어떻게 반응하는지를**  
  **확인하고, 해결해 보는 것이다.**

<h2>일단 시도해보기</h2>

- 우선 아래와 같은 간단한 서비스 인터페이스가 있다고 하자.

```kt
interface TestService {
    fun doTest(): BasicMessageDto
}

// BasicMessageDto
data class BasicMessageDto(
    val message: String
)
```

- 그리고 이 인터페이스를 구현하는 클래스 2개를 따로따로 만들어보자.

```kt
// TestServiceImpleOne.kt
@Service
class TestServiceImplOne : TestService {
    override fun doTest(): BasicMessageDto {
        return BasicMessageDto("Test Service implementation 1.")
    }
}

// TestServiceImpleTwo.kt
@Service
class TestServiceImplTwo : TestService {
    override fun doTest(): BasicMessageDto {
        return BasicMessageDto("Test Service implementation 2.")
    }
}
```

- 이제 controller 단에서 생성자를 통해 `TestService`의 구현체를 주입받아 사용하도록 해보자.

```kt
@RestController
class TestController(
    private val testService: TestService
) {

    @GetMapping("/test")
    fun testServiceDI(): BasicMessageDto {
        return testService.doTest()
    }
}
```

- 만약 `TestService`의 구현체가 하나였고, 구현체가 많더라도 Spring Bean으로 등록된 구현체가 한개였다면  
  해당 Spring Bean의 인스턴스가 주입되어 `TestController`가 정상적으로 작동했을 것이다.

- 우선 위 코드를 치면 Intellij 단에서 벌써 testService 쪽에 에러를 띄워준다.  
  일단 무시하고 빌드를 하고 애플리케이션을 실행하려 하면 아래의 에러 내용이 출력된다.

```
Description:

Parameter 0 of constructor in TestController required a single bean, but 2 were found:
	- testServiceImplOne: defined in file [/pathToProject/TestServiceImplOne.class]
	- testServiceImplTwo: defined in file [/pathToProject/TestServiceImplTwo.class]


Action:

Consider marking one of the beans as @Primary, updating the consumer to accept multiple beans, or using @Qualifier to identify the bean that should be consumed
```

- 즉 `TestService`의 구현체가 하나 보다 많기 때문에 두 개의 구현체들 중  
  어떤 것을 인스턴스화하여 주입할지 지정되지 않아서 발생하는 오류이다.

- 위 에러 내용에서 제시하는 `@Primary`, `@Qualifier` 를 사용하여 이를 해결해보자.

<hr/>
