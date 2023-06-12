# public 클래스에서는 public 필드가 아닌 접근자 메소드를 사용하라

- 이따금 인스턴스 필드들을 모아놓는 일 외에는 아무런 목적도 없는 퇴보한 클래스를 작성하려 할 때가 있다.

```java
class Point {
  public double x;
  public double y;
}
```

- 이런 클래스는 데이터 필드에 직접 접근할 수 있으니 캡슐화의 이점을 제공하지 못한다.  
  API를 수정하지 않고는 내부 표현을 바꿀 수 없고, 불변식을 보장할 수 없으며, 외부에서 필드에  
  접근할 때 부수 작업을 실행할 수도 없다. 철저한 객체지향 프로그래머는 이런 클래스를 상당히  
  싫어해서 필드들을 모두 private으로 바꾸고 접근자(getter)를 추가한다.

```java
class Point {
  private double x;
  private double y;

  public Point(double x, double y) {
    this.x = x;
    this.y = y;
  }

  // Standard getters, setters
}
```

- public 클래스에서라면 이 방식이 확실히 맞다.  
  **패키지 바깥에서 접근할 수 있는 클래스라면 접근자를 제공**함으로써 클래스 내부 표현 방식을  
  언제든 바꿀 수 있는 유연성을 얻을 수 있다. public 클래스가 필드를 공개하면, 이를 사용하는  
  클라이언트가 생겨날 것이므로 내부 표현 방식을 마음대로 바꿀 수 없게 된다.

- 하지만 **package-private 클래스 혹은 private 중첩 클래스라면 데이터 필드를 노출한다 해도**  
  **하등의 문제가 없다.** 그 클래스가 표현하려는 추상 개념만 올바르게 표현해주면 된다.  
  이 방식은 클래스 선언 면에서나 이를 사용하는 클라이언트 코드 면에서나 접근자 방식보다 훨씬 깔끔하다.  
  클라이언트 코드가 이 클래스 내부 표현에 묶이기는 하나, 클라이언트도 어짜피 이 클래스를 포함하는  
  패키지 안에서만 동작하는 코드일 뿐이다. 따라서 패키지 바깥의 코드는 전혀 손대지 않고도 데이터의  
  표현 방식을 바꿀 수 있다. private 중첩 클래스의 경우라면 수정 범위가 더 좁아져서 이 클래스를  
  포함하는 외부 클래스까지로 제한된다.

- Java 플랫폼 라이브러리에도 public 클래스의 필드를 직접 노출하지 말라는 규칙을 어기는 사례가  
  종종 있다. 대표적인 예가 `java.awt.package` 패키지의 `Point`와 `Demension` 클래스다.  
  이 클래스들은 절대 흉내내지 말고, 타산지석으로 삼자. 나중에 보겠지만, 내부를 노출한 `Dimension`  
  클래스의 심각한 성능 문제는 오늘날까지도 해결되지 못했다.

- public 클래스의 필드가 불변이라면 직접 노출할 때의 단점이 조금은 줄어들지만, 여전히 결코 좋은  
  생각이 아니다. API를 변경하지 않고는 표현 방식을 바꿀 수 없고, 필드를 읽을 때 부수 작업을  
  수행할 수 없다는 단점은 여전하다. 단, 불변식은 보장할 수 있게 된다. 예컨대 아래 클래스는  
  각 인스턴스가 유효한 시간을 표현함을 보장한다.

```java
public final class Time {
  private static final int HOURS_PER_DAY = 24;
  private static final int MINUTES_PER_HOUR = 60;

  public final int hour;
  public final int minute;

  public Time(int hour, int minute) {
    if (hour < 0 || hour >= HOURS_PER_DAY)
      throw new IllegalArgumentException("Hour: " + hour);
    if (minute < 0 || minute >= MINUTES_PER_HOUR)
      throw new IllegalArgumentException("Minute: " + minute);
    this.hour = hour;
    this.minute = minute;
  }

  //..
}
```

---

## 핵심 정리

- public 클래스는 절대 가변 필드를 직접 노출해서는 안된다. 불변 필드라면 노출해도 덜 위험하지만  
  완전히 안심할 수는 없다. 하지만 package-private 클래스나 private 중첩 클래스에서는 종종  
  불변이든 가변이든 필드를 노출하는 편이 나을 때도 있다.

---
