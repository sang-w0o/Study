<h1>Lambda식과 Functional Interface</h1>

<h2>Standard Functional Interfaces</h2>

* Functional Interface들은 `java.util.function` 패키지 내에 위치해 있다.   
  이 패키지는 개발자가 Lambda 식과 Method Reference를 사용하기 쉽게 해준다.

* 이 패키지에 있는 모든 인터페이스들은 General 타입을 수용하며, 추상적으로 되어 있어   
  사실상 거의 모든 Lambda식을 수용할 수 있게 해준다.

* `Foo`라는 인터페이스가 있다고 가정해보자.
```java
@FunctionalInterface
public interface Foo {
    String method(String string);
}
```

* 그리고 `UseFoo`라는 클래스의 `add()` 메소드에서 위의 인터페이스를 파라미터로 가진다고 하자.
```java
public class UseFoo {
   public String add(String string, Foo foo) {
       return foo.method(string);
   }
}
```

* 위의 코드를 실행하려면 아래와 같이 작성할 것이다.
```java
Foo foo = parameter -> parameter = " from lambda.";
String result = useFoo.add("Message", foo);
```

* 코드를 보면, `Foo`는 단지 하나의 인자를 받고, 새로운 값을 반환하는 함수에 불과하다.   
  Java 8은 이러한 경우를 위해 `java.util.function` 패키지에 `Function<T, R>`라는 인터페이스를 제공한다.

* 위의 패키지를 이용해서 `Foo`를 사용하지 않고, 코드를 작성하면 아래와 같다.
```java
public String add(String string, Function<String, String> fn) {
    return fn.apply(string);
}
```

* 이제 위 코드를 실행하는 코드를 작성해보자.
```java
Function<String, String> fn = parameter -> parameter + " from lambda.";
String result = useFoo.add("Message", fn);
```
<hr/>

<a href="https://www.baeldung.com/java-8-lambda-expressions-tips">참고 링크</a>