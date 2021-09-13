# AOP

- Spring의 3대 요소 중 하나는 바로 AOP이다.  
  AOP는 Aspect Oriented Programming의 약자로, **여러 객체에 공통으로 적용하는 기능들을**  
  **분리해서 재사용성을 높여주는 프로그래밍 기법** 이다. 예를 들어, 서비스 메소드에서 자주 사용하는  
  `@Transactional` 어노테이션의 경우, 개발자가 직접 `transaction.commit()` 또는  
  `transaction.rollback()`을 호출하지 않아도 되게 해줌으로써 비즈니스 로직에 집중하게 해준다.

- Spring에서 AOP를 사용하려면 `aspectjweaver`와 함께 사용하는데, 이 모듈은 AOP를 설정하는 데  
  필요한 어노테이션들을 제공해준다.

- Spring에서 AOP를 사용하는 간단한 플로우는 아래와 같다.

  - (1) Aspect로 사용할 클래스에 `@Aspect` 어노테이션을 적용한다.
  - (2) `@PointCut` 어노테이션으로 공통 기능을 적용할 Pointcut을 정의한다.
  - (3) 공통 기능을 구현한 메소드에 `@Around` 어노테이션을 적용한다.

- 위 3개의 어노테이션(`@Aspect`, `@PointCut`, `@Around`)를 사용하면  
  개발자는 공통 기능을 제공하는 Aspect 클래스를 만들고, Aspect를 어디에  
  적용할지 설정할 수 있다. Aspect 클래스에는 `@Aspect` 어노테이션이  
  적용되어야 하며, 실제로 작업을 하는 Proxy는 Spring Framework가 만들어준다.

> Proxy 객체: **핵심 기능의 실행은 다른 객체에 위임하고, 부가적인 기능을 제공하는 객체**  
> 위 정의에서 알 수 있듯이 Proxy 객체들의 특징은 **핵심 기능을 구현하지 않는다는 것**이다.  
> 대신, **여러 객체에 공통으로 적용할 수 있는 기능을 구현**한다.

- AOP에서 알아야할 용어들은 아래와 같다.

| 용어      | 설명                                                                                                                                                                                     |
| --------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Advice    | 언제 공통 관심 기능을 핵심 로직에 적용할지를 정의한다.                                                                                                                                   |
| Aspect    | 여러 객체에 공통적으로 적용되는 기능                                                                                                                                                     |
| Weaving   | Advice를 핵심 로직 코드에 적용하는 것                                                                                                                                                    |
| JoinPoint | Advice를 적용 가능한 지점을 의미한다.<br/> 메소드 호출, 필드 값 변경 등이 JoinPoint에 해당한다. <br/>Spring은 Proxy를 이용하여 AOP를 구현하기에 메소드 호출에 대한 JoinPoint만 지원한다. |
| Pointcut  | JoinPoint의 부분 집합으로서, 실제 Advice가 적용되는 JoinPoint를 나타낸다.<br/>Spring에서는 Regex 또는 AspectJ의 문법을 이용하여 PointCut을 정의한다.                                     |

- Spring에서 AOP를 활용한 간단한 예제는 <a href="https://github.com/sang-w0o/Study/blob/master/Backend%20Frameworks/Spring%20Framework/AOP%20Programming.md#aop-aspect-oriented-programming">여기</a>에서 볼 수 있다.

- 이번에는 또다른 예제를 만들어보자.  
  이번에 만들어볼 것은 어노테이션인데, 이 어노테이션이 적용되면 특정 메시지를 실행될 때 콘솔에 출력하도록 해보자.

<h2>@LogMessage 어노테이션 만들기</h2>

- 테스트할 API를 담은 컨트롤러와 서비스는 각각 아래와 같다.

```kt
// TestService.kt

@Service
class TestService {

    fun doTest(arg: String): BasicMessageResponseDto {
        println("TestService.doTest()")
        return BasicMessageResponseDto(arg)
    }
}

// TestApiController.kt
@RestController
class TestApiController(
    private val testService: TestService
) {

    @GetMapping("/test")
    fun testApi(@RequestParam(required = true, name = "arg") arg: String): BasicMessageResponseDto {
        return this.testService.doTest(arg)
    }
}
```

- 우선 가장 먼저 `@LogMessage` 어노테이션 자체를 만들어보자.  
  이 어노테이션에 `@LogMessage(message = "HI")`처럼 message를 인자로 받도록 하고,  
  그 값을 출력할 것이다.

```kt
// 패키지명 주의!!
package com.template.demo

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class LogMessage(val message: String = "Default Message")
```

- `@Target`에 `AnnotationTarget.FUNCTION`을 지정하여 메소드에만 적용하도록 했다.

- 다음으로 실제로 콘솔에 찍는 Aspect 클래스를 만들어보자.

> Aspect: 여러 객체에 공통적으로 적용되는 기능

- 기능 구현을 하기 전에, 여러 가지 정보를 찍어보자.

```kt
@Around("@annotation(com.template.demo.LogMessage)")
fun logMessage(joinPoint: ProceedingJoinPoint): Any {
    val signature = joinPoint.signature
    println(joinPoint.target.javaClass.simpleName)
    println(signature.name)
    return joinPoint.proceed()
}
```

- 위처럼 하고, `TestApiController#doTest()`에 `@LogMessage(message = "Test Message")`어노테이션을 적용해보면,  
  아래와 같은 값들이 콘솔에 출력된다.

```
TestApiController
doTest
```

- 즉 이 어노테이션이 적용된 메소드의 클래스명을 `joinPoint.target.javaClass.simpleName`으로  
  가져왔고, 그 값은 TestApiController 이다. 또한 메소드명은 `signature.name`으로  
  가져왔는데, 그 값은 testApi이다.

- 위 `LoggingAspect` 클래스에는 `@Aspect` 어노테이션을 적용해서 이 클래스가  
  Aspect 클래스임을 알려줬고, `@Around`에는 `@annotation(com.template.demo.LogMessage)`를  
  지정해줘서 `@LogMessage` 어노테이션이 실행될 때 무언가를 수행하도록 해주었다.

- `logMessage()` 함수에 여러 가지 값들을 찍어봐서 joinPoint에 대해 파악해보자.

```kt
@Around("@annotation(com.template.demo.LogMessage)")
fun logMessage(joinPoint: ProceedingJoinPoint): Any {
    val signature = joinPoint.signature
    println(joinPoint.target.javaClass.simpleName)
    println(signature.name)
    return joinPoint.proceed()
}
```

- 참고로 `@Around`가 적용된 `logMessage()`에서 `joinPoint.proceed()`를  
  호출하지 않으면 이 메소드가 작업을 한 후 추후 작업이 실행되지 않으므로  
  프로그램이 비정상적으로 실행된다.  
  (이 경우에는 response body가 없이 200_OK 만ㅇ 응답으로 온다.)  
  `joinPoint.proceed()`는 사실상 `TestApiController#testApi()`를  
  호출하는 것과 같으니 `BasicMessageResponseDto`를 반환하게 된다.  
  만약 `logMessage()`에서 `return false`를 해주면 당연히 오류가 난다.

- 이제 `@Around`가 적용되는 메소드를 잘 파악했으니 이제 `@LogMessage`의  
  message를 출력하도록 하면 끝이다.

```kt
@Around("@annotation(com.template.demo.LogMessage)")
fun logMessage(joinPoint: ProceedingJoinPoint): Any {
    val methodSignature = joinPoint.signature as MethodSignature
    val annotation = methodSignature.method.getAnnotation(LogMessage::class.java)
    println(annotation.message)
    return joinPoint.proceed()
}
```

- `joinPoint.signature`를 `MethodSignature`로 형변환하여, 해당 메소드의 시그니처를  
  가져올 수 있게 한 후, `methodSignature.method.getAnnotation(LogMessage::class.java)`를  
  실행하여 `@LogMessage` 어노테이션을 가져왔다. 그리고 그 어노테이션의 message 속성을  
  콘솔에 출력하도록 했다.

<hr/>

<h2>결론</h2>

- AOP를 사용하면 공통된 기능을 Aspect 클래스로 만들어서, 코드의 중복을 없애고  
  원하는 시점에만 공통된 기능이 수행되도록 할 수 있다.

- Spring은 Spring Framework가 Proxy 객체를 만들어서 AOP를 지원하기 때문에
  public 접근제한자를 가진 메소드에만 AOP를 사용할 수 있으며, 메소드 호출에 대한  
  Joinpoint만 지원한다.

<hr/>

- Spring Framework AOP 정리: <a href="https://github.com/sang-w0o/Study/blob/master/Backend%20Frameworks/Spring%20Framework/AOP%20Programming.md">링크</a>
- 참고 링크 1: <a href="https://www.baeldung.com/spring-aop-pointcut-tutorial">링크</a>
