# 다른 타입이 적절하다면 문자열 사용을 피하라

- 문자열(`String`)은 텍스트를 표현하도록 설계되었고, 그 일을 아주 멋지게 해낸다. 그런데 문자열은  
  워낙 흔하고 Java가 또 잘 지원해주기에 원래 의도하지 않은 용도로 쓰이는 경향이 있다.  
  이번 아이템에서는 문자열을 쓰지 않아야 할 사례를 다룬다.

- **문자열은 다른 값 타입을 대신하기에 적합하지 않다.** 많은 사람이 파일, 네트워크, 키보드 입력으로부터  
  데이터를 받을 때 주로 문자열을 사용한다. 사뭇 자연스러워 보이지만, 입력받을 데이터가 진짜 문자열일 때만  
  그렇게 하는게 좋다. 받은 데이터가 수치형이라면 int, float, `BigInteger` 등 적당한 수치 타입으로  
  변환해야 한다. _'예, 아니오'_ 질문의 답이라면 적절한 열거 타입이나 boolean으로 변환해야 한다.  
  일반화해 이야기하자면, 기본 타입이든 참조 타입이든 적절한 값 타입이 있다면 그것을 사용하고, 없다면  
  새로 하나 작성하자. 당연한 말 같지만, 지켜지지 않는 경우가 꽤 많다.

- **문자열은 열거 타입을 대신하기에 적합하지 않다.** [Item 34](https://github.com/sang-w0o/Study/blob/master/Programming%20Paradigm/Effective%20Java/5.%20%EC%97%B4%EA%B1%B0%20%ED%83%80%EC%9E%85%EA%B3%BC%20%EC%96%B4%EB%85%B8%ED%85%8C%EC%9D%B4%EC%85%98/Item%2034.%20int%20%EC%83%81%EC%88%98%20%EB%8C%80%EC%8B%A0%20%EC%97%B4%EA%B1%B0%20%ED%83%80%EC%9E%85%EC%9D%84%20%EC%82%AC%EC%9A%A9%ED%95%98%EB%9D%BC.md)에서 봤듯이, 상수를 열거할 때는 문자열보다는  
  열거 타입이 훨씬 낫다.

- **문자열은 혼합 타입을 대신하기에 적합하지 않다.** 여러 요소가 혼합된 데이터를 하나의 문자열로  
  표현하는 것은 대체로 좋지 않은 생각이다. 예를 들어, 아래는 실제 시스템에서 가져온 코드다.

```java
String compoundKey = className + "#" + i.next();
```

- 이는 단점이 많은 방식이다. 혹여라도 두 요소를 구분해주는 "#"이 두 요소 중 하나에서 쓰였다면  
  혼란스러운 결과를 초래한다. 각 요소를 개별로 접근하려면 문자열을 파싱해야 해서 느리고, 귀찮고,  
  오류 가능성도 커진다. 적절한 `equals()`, `toString()`, `compareTo()` 메소드를  
  제공할 수 없으며, `String`이 제공하는 기능에만 의존해야 한다. 그래서 차라리 전용 클래스를 새로  
  만드는 편이 낫다. 이런 클래스는 보통 private 정적 멤버 클래스로 선언한다.

- **문자열은 권한을 표현하기에 적합하지 않다.** 권한(capacity)을 문자열로 표현하는 경우가  
  종종 있다. 예를 들어 스레드 지역변수 기능을 설계한다고 해보자. 그 이름처럼 각 스레드가 자신만의 변수를  
  갖게 해주는 기능이다. Java가 이 기능을 지원하기 시작한 때는 Java2 부터로, 그 전에는 프로그래머가  
  직접 구현해야 했다. 그 당시 이 기능을 설계해야 했던 여러 프로그래머가 독립적으로 방법을 모색하다가  
  종국에는 똑같은 설계에 이르렀다. 바로 클라이언트가 제공한 문자열 key로 스레드별 지역변수를 식별한 것이다.

```java
public class ThreadLocal {
  private ThreadLocal() { }

  // 현재 스레드의 값을 key로 구분해 저장한다.
  public static void set(String key, Object value);

  // key가 가리키는 현재 스레드의 값을 반환한다.
  public static Object get(String key);
}
```

- 이 방식의 문제는 스레드를 구분하기 위한 문자열 key가 전역 namespace에서 공유된다는 점이다.  
  이 방식의 의도대로 동작하려면 각 클라이언트가 고유한 key를 제공해야 한다. 그런데 만약 두  
  클라이언트가 서로 소통하지 못해 같은 key를 쓰기로 결정한다면, 의도치 않게 같은 변수를 공유하게 된다.  
  결국 두 클라이언트 모두 제대로 기능하지 못할 것이다. 보안도 취약하다. 악의적인 클라이언트라면  
  의도적으로 같은 key를 사용해 다른 클라이언트의 값을 가져올 수도 있다.

- 이 API는 문자열 대신 위조할 수 없는 key를 사용하면 해결된다. 이 key를 권한이라고도 한다.

```java
public class ThreadLocal {
  private ThreadLocal() { }

  public static class Key {
    Key() { }
  }

  // 위조 불가능한 고유 key를 생성한다.
  public static Key getKey() {
    return new Key();
  }

  public static void set(Key key, Object value);
  public static Object get(Key key);
}
```

- 이 방법은 문자열 기반 API의 문제 두 가지를 모두 해결해주지만, 개선의 여지가 있다. set과 get은  
  정적 메소드일 이유가 없으니 `Key` 클래스의 인스턴스 메소드로 바꾸자. 이렇게 하면 `Key`는 더 이상  
  스레드 지역변수를 구분하기 위한 key가 아니라, 그 자체가 스레드 지역변수가 된다. 결과적으로  
  지금의 Top-Level 클래스인 `ThreadLocal`은 별달리 하는 일이 없어지므로 치워버리고,  
  중첩 클래스 `Key`의 이름을 `ThreadLocal`로 바꿔버리자. 그럼 아래처럼 변한다.

```java
public final class ThreadLocal {
  public ThreadLocal() { }
  public void set(Object value);
  public Object get();
}
```

- 이 API에서는 get으로 얻은 `Object`를 실제 타입으로 형변환해 사용해야 하므로 타입 안전하지 않다.  
  처음의 문자열 기반 API는 타입 안전하게 만들 수 없으며, `Key`를 사용한 API도 타입 안전하게  
  만들기 어렵다. 하지만 `ThreadLocal`을 매개변수화 타입으로 선언하면 간단히 문제가 해결된다.

```java
public final class ThreadLocal<T> {
  public ThreadLocal() { }
  public void set(T value);
  public T get();
}
```

- 이제 java의 `java.lang.ThreadLocal`과 흡사해졌다. 문자열 기반 API의 문제를 해결해주며,  
  key 기반 API보다 빠르고 우아하다.

---

## 핵심 정리

- 더 적합한 데이터 타입이 있거나 새로 작성할 수 있다면, 문자열을 쓰고 싶은 유혹을 뿌리치자.  
  문자열은 잘못 사용하면 번거롭고, 덜 유연하고, 느리고, 오류 가능성도 크다. 문자열을 잘못  
  사용하는 흔한 예로는 기본 타입, 열거 타입, 혼합 타입이 있다.

---
