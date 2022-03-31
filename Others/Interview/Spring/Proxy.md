# Proxy in Spring

## Proxy

- Proxy의 정의는 **타겟(대상)을 감싸서 타켓의 요청을 대신 받아주는 객체** 이다.  
  실제로 호출하려는 대상을 감싸주기 때문에 호출되기 전에 처리하는 선처리, 호출 후에 처리할 후처리가 가능해서  
  AOP에 필수적인 요소이다.

- 또다른 정의는 아래와 같다.

> Proxy 객체: **핵심 기능의 실행은 다른 객체에 위임하고, 부가적인 기능을 제공하는 객체**  
> 위 정의에서 알 수 있듯이 Proxy 객체들의 특징은 **핵심 기능을 구현하지 않는다는 것**이다.  
> 대신, **여러 객체에 공통으로 적용할 수 있는 기능을 구현**한다.

---

## Proxy의 종류

- Spring AOP에서는 런타임에 _Weaving_ 을 통해 Proxy 객체를 생성하게 된다.  
  Weaving은 간단히 말해 AOP의 Advice(공통적으로 적용된 기능이 적용되는 곳)를 적용하는 것이다.

### JDK Dynamic Proxy

- JDK Dynamic Proxy는 Spring이 초반에 채택한 Proxy 생성 방식으로, Proxy 객체를 생성하는 과정에서  
  **인터페이스를 기준**으로 생성한다.

- 따라서 인터페이스의 구현체는 반드시 인터페이스를 구현해야 하고, JDK Proxy가 만들어낸 Proxy Bean을  
  사용하기 위해서는 사용하는 곳에서 반드시 인터페이스 타입으로 지정해줘야 한다.

- 간단한 예시 코드를 보자.

```kt
// Service라는 인터페이스
interface Service {
  fun getName(): String
}

// Service를 구현하는 ServiceImpl
@Service
class ServiceImpl : Service {
  override fun getName(): String {
    return "Spring"
  }
}

// ServiceImpl이 JDK Dynamic Proxy를 통해 Proxy Bean으로 등록되고,
// 인터페이스 타입으로 지정하여 주입받는다.
@RestController
class Controller(
  private val service: Service
) {
  //..
}
```

- JDK Dynamic Proxy에 대해 알기 위해서는 `java.lang.reflect.Proxy`와 `java.lang.reflect.InvocationHandler`에  
  대해 알아야 한다.

- Proxy 객체는 `java.lang.reflect.Proxy`의 public static 메소드인 `newProxyInstance()`로 만들어진다.

```java
public class Proxy implements java.io.Serializable {
  //..

  public static Object newProxyInstance(ClassLoader loader,Class<?>[] interfaces, InvocationHandler h) {
    //..
  }
}
```

- 위 `newProxyInstance()`의 3번째 인자에는 `InvocationHandler`가 있는데, `Proxy.newProxyInstance()`로 만들어진  
  Proxy 객체의 모든 메소드 호출은 `InvocationHandler`의 `invoke()` 메소드를 호출로 변환된다.

- 아래는 실제 프로젝트의 오류 로그인데, 보면 나는 작성한 적이 없는 `invoke()`가 나와있는 것을 확인할 수 있다.

![picture 1](/images/SPRING_PROXY_INVOKE_ERROR_LOG.png)

### CGLib Proxy

- JDK Dynamic Proxy는 인터페이스가 있고, 해당 인터페이스를 구현하는 구현체에 대해 Proxy 객체를 생성하는 방식이었다면,  
  CGLib Proxy는 인터페이스가 아닌 클래스 자체에 대해 Proxy 객체를 생성하는 방식이다.

- Java의 클래스들은 런타임에 동적으로 loading되는데, cglib은 이러한 Java의 특성을 이용해 이미 실행 중인 Java 프로그램에  
  새로운 클래스를 추가한다.

- 아래의 `TestService`가 있다고 하자. 위의 JDK Dynamic Proxy의 예시 코드와는 달리, 인터페이스 + 구현체 방식이 아닌  
  하나의 클래스이다.

```java
@Service
public class TestService {
  public String test() {
    return "test";
  }
}
```

- JDK Dynamic Proxy가 `java.lang.reflect`의 `InvocationHandler`와 `Proxy`를 사용했다면, CGLib에서  
  Proxy 객체를 생성해내는 방법은 아래와 같다.

```java
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

public class TestCGLibProxy {
  public void foo() {
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(TestService.class);
    enhancer.setCallback((FixedValue) () -> "Hello World");
    TestService proxy = (TestService) enhancer.create();
    String resultFromProxy = proxy.test();
    System.out.println(resultFromProxy.equals("Hello World")); // true
  }
}
```

- 위의 `resultFromProxy.equals("Hello World")`의 결과가 true를 반환하는 이유는 proxy 객체를 만드는  
  `Enhancer`의 `setCallback()`에서 "Hello World"를 반환하도록 했기 때문이다.

- 아래처럼 `TestService`가 어떤 메소드에서는 `String`을, 어떤 메소드에서는 `Integer`를 반환하다고 해보자.

```java
@Service
public class TestService {
  public String test() {
    return "test";
  }
  public Integer test2() {
    return 2;
  }
}
```

- 이런 경우에는 `setCallback()`에 더 상세한 명세를 기술하는 람다식을 전달해 처리할 수 있다.

```java
public class TestCGLibProxy {
  public void foo() {
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(TestService.class);
    enhancer.setCallback((MethodInterceptor)(obj, method, args, proxy) -> {
      if(method.getReturnType() == String.class) {
        return "Hello World";
      } else {
        return proxy.invokeSuper(obj, args);
      }
    });
    TestService proxy = (TestService) enhancer.create();
    String stringResultFromProxy = proxy.test();
    System.out.println(stringResultFromProxy.equals("Hello World")); // true
    Integer intResultFromProxy = proxy.test2();
    System.out.println(intResultFromProxy.equals(2)); // true
  }
}
```

- Spring Framework가 클래스의 Proxy 객체를 생성할 때도 위와 마찬가지로 `Enhancer`를 사용하는데,  
  이때 클래스에 포함된 모든 메소드를 재정의한다. 그리고 `Enchancer#setSuperClass()`에서 알 수 있듯이  
  proxy 객체는 해당 클래스의 자식 클래스로 생성된다.

- 따라서 **final 클래스나 final 메소드는 override가 불가하기에 Proxy 객체로 만들어질 수 없다.**

### 참고

- 참고로 CGLib은 Spring 3.2부터 Spring Core 패키지에 포함되기 시작했고, CGLib 자체로는 반드시  
  아무런 파라미터가 없는 default 생성자가 있어야 한다. Spring 4에서는 Objensis 의 도움을 받아  
  default 생성자 없이도 클래스의 Proxy 객체를 만들어낼 수 있게끔 하였다.

- Spring 4.3, Spring Boot 1.4 부터 JDK Dynamic Proxy가 아닌 CGLib이 Spring Framework의  
  Proxy 객체 생성 방식의 default 방식으로 채택되었다.

---

### 레퍼런스

- [JDK Dynamic Proxy with Spring](https://medium.com/@spac.valentin/java-dynamic-proxy-mechanism-and-how-spring-is-using-it-93756fc707d5)

- [CGLib](https://www.baeldung.com/cglib)
