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

<h2>@Primary</h2>

- 가장 간단한 해결책으로, 주입하고 싶은 구현체에 `@Primary` 어노테이션을 적용하면 된다.  
  `TestService`의 구현체 중 `TestServiceImplOne`을 주입하고 싶다고 하자.

```kt
// TestServiceImpleOne.kt
@Service
@Primary
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

- 이렇게하면 `TestService`의 구현체를 주입받는 `TestController`에는  
  `TestServiceImpleOne`의 구현체가 주입된다.  
  만약 `TestServiceImpleTwo`에도 `@Primary` 어노테이션을 적용하면 처음과 마찬가지로  
  Spring이 2개의 구현체 중 어느 것을 주입할지 모르기 때문에 빌드 시 에러가 발생한다.

<h3>@Primary에 대해</h3>

- `@Primary` 어노테이션은 주입될 후보가 여러개(이 경우에는 `TestServiceImplOne`, `TestServiceimplTwo`)  
  있을 때, 어떤 것이 주입될지를 지정한다. 이러한 후보들 중 정확히 한 개의 후보에만 `@Primary` 어노테이션이  
  적용되어 있다면 이 어노테이션이 적용된 후보가 주입된다.

- 이 어노테이션은 Spring XML로 Bean을 지정할 때 사용하는 `<bean>` 태그와 동일한 성격을 띈다.

- `@Component`가 적용된 클래스 또는 `@Bean`이 적용된 메소드와 함께 사용할 수 있다.

- 한 가지 주의할 점은, component scanning이 사용되지 않는 상황에서 `@Primary`를 클래스에게  
  적용하는 것은 아무런 효과를 발생시키지 않는다. 만약 `@Primary`가 적용된 클래스가 XML로 선언되어 있다면  
  `@Primary` 어노테이션은 무시되며, `<bean primary="true|false">` 가 적용된다.

<hr/>

<h2>@Qualifier</h2>

- `@Qualifier`를 사용하여 어떤 구현체가 주입될지도 결정할 수 있다.  
  빠르게 적용해보자.

```kt
// TestServiceImpleOne.kt
@Service
@Qualifier("testServiceImplOne")
class TestServiceImplOne : TestService {
    override fun doTest(): BasicMessageDto {
        return BasicMessageDto("Test Service implementation 1.")
    }
}

// TestServiceImpleTwo.kt
@Service
@Qualifier("testServiceImplTwo")
class TestServiceImplTwo : TestService {
    override fun doTest(): BasicMessageDto {
        return BasicMessageDto("Test Service implementation 2.")
    }
}
```

- `TestService`의 구현체를 주입받을 `TestController`에도 동일한 Qualifier value를  
  지정해줘야 한다. `@Primary`를 사용할 때도 가능하지만, 생성자 주입과 setter 주입, 그리고  
  필드 주입 모두 가능하다.  
  만약 주입받을 클라이언트에서 잘못된 Qualifier value를 제공하면 빌드 조차 실패한다.

```kt
// 생성자를 통한 의존성 주입
@RestController
class TestController(
    @Qualifier("testServiceImplOne")
    private val testService: TestService
) {

    @GetMapping("/test")
    fun testServiceDI(): BasicMessageDto {
        return testService.doTest()
    }
}

// 필드 의존성 주입
@RestController
class TestController {
    @Autowired
    @Qualifier("testServiceImplOne")
    private lateinit var testService: TestService

    @GetMapping("/test")
    fun testServiceDI(): BasicMessageDto {
        return testService.doTest()
    }
}
```

- 이제 간단하게 `@Bean`에 대해서 이를 적용하는 예시를 살펴보자.  
  우선 추상 클래스 `AbstractUser`가 있고, 이를 구현하는 구현 클래스인  
  `UserOne`과 `UserTwo`가 있다.

```kt
// AbstractUser.kt

abstract class AbstractUser {
    abstract fun printName()
}

// UserOne.kt
class UserOne(private val name: String) : AbstractUser() {
    override fun printName() {
        println("NAME OF USER ONE : $name")
    }
}

// UserTwo.kt
class UserTwo(private val name: String) : AbstractUser() {
    override fun printName() {
        println("NAME OF USER TWO : $name")
    }
}
```

- 두 개의 구현체를 반환하는 `@Bean`이 적용된 메소드를 가지는 작성해보자.  
  이때, 이 클래스에는 꼭 `@Configuration` 어노테이션을 적용해줘야 하는데,  
  이 어노테이션이 적용되어야 있을 때 Spring이 Component scanning을 수행할 때  
  해당 클래스 내에 `@Bean`이 적용된 것들에 대해 Spring Bean 처리를 해주기 때문이다.  
  만약 `@Configuration`이 없다면 Component scanning에서 제외되고, 실행이 되지 않는다.

```kt
@Configuration
class UserConfiguration {

    @Bean
    @Qualifier("userOne")
    fun getUserOne(): AbstractUser {
        return UserOne("sangwoo")
    }

    @Bean
    @Qualifier("userTwo")
    fun getUsertwo(): AbstractUser {
        return UserTwo("sangwoo")
    }
}
```

- 이제 마지막으로 `AbstractUser`의 구현체를 주입 받을 곳을 작성하자.  
  마찬가지로 `@Qualifier`로 어떤 구현체를 주입할지 지정해야 한다.

```kt
@Service
@Qualifier("testServiceImplOne")
class TestServiceImplOne : TestService {

    @Autowired
    @Qualifier("userOne")
    private lateinit var user: AbstractUser

    override fun doTest(): BasicMessageDto {
        user.printName()
        return BasicMessageDto("Test Service implementation 1.")
    }
}
```

<h3>@Qualifier를 사용해야할 때</h3>

- 보통 이렇게 하나의 서비스 인터페이스의 구현체가 두 개인 경우에는 내부 구현 동작이  
  다르기 때문일 것이다. 동일한 인터페이스를 여러 개 선언하고, 구현체를 각각 따로  
  만드는 코드는 불필요한 중복이 일어나고, 유지 보수가 훨씬 어려워질 것이다.

<hr/>
