# `readObject()` 메소드는 방어적으로 작성하라

- [Item 50](https://github.com/sang-w0o/Study/blob/master/Programming%20Paradigm/Effective%20Java/7.%20%EB%A9%94%EC%86%8C%EB%93%9C/Item%2050.%20%EC%A0%81%EC%8B%9C%EC%97%90%20%EB%B0%A9%EC%96%B4%EC%A0%81%20%EB%B3%B5%EC%82%AC%EB%B3%B8%EC%9D%84%20%EB%A7%8C%EB%93%A4%EB%9D%BC.md)에서는 불변인 날짜 범위 클래스를 만드는 데 가변인 `Date` 필드를 사용했다. 그래서 불변식을 지키고  
  불변성을 유지하기 위해 생성자와 접근자에서 `Date` 객체를 방어적으로 복사하느라 코드가 상당히 길어졌다.

```java
public final class Period {
  private final Date start;
  private final Date end;

  /**
   * @param start 시작 시각
   * @param end 끝 시각; 시작 시각보다 뒤여야 한다.
   * @throws IllegalAgrumentException 시작 시간이 종료 시각보다 늦을 때 발생한다.
   * @throws NullPointerException start나 end가 null이면 발생한다.
   */
  public Period(Date start, Date end) {
    if (start.compareTo(end) > 0) {
      throw new IllegalArgumentException("start must be before end");
    }
    this.start = new Date(start.getTime());
    this.end = new Date(end.getTime());
  }

  public Date start() { return new Date(start.getTime()); }

  public Date end() { return new Date(end.getTime()); }

  public String toString() { return start = " - " + end; }

  //..
}
```

- 이 클래스를 직렬화하기로 결정했다 해보자. `Period` 객체의 물리적 표현이 논리적 표현과 부합하므로 기본 직렬화 형태를  
  사용해도 나쁘지 않다. 그러니 이 클래스 선언에 `implements Serializable`을 추가하는 것으로 모든 일을 끝낼 수  
  있을 것 같다. 하지만 이렇게 해서는 이 클래스의 중요한 불변식을 더는 보장하지 못하게 된다.

- 문제는 `readObject()`가 실질적으로 또 다른 public 생성자이기 때문이다. 따라서 다른 생성자와 똑같은 수준으로  
  주의를 기울여야 한다. 보통의 생성자처럼 `readObject()` 에서도 인수가 유효한지 검사해야 하고, 필요하다면 매개변수를  
  방어적으로 복사해야 한다. `readObject()`가 이 작업을 제대로 수행하지 못하면 공격자는 아주 손쉽게 해당 클래스의  
  불변식을 깨뜨릴 수 있다.

- 쉽게 말해, `readObject()`는 매개변수로 바이트 스트림을 받는 생성자라고도 할 수 있다. 보통의 경우 바이트 스트림은  
  정상적으로 생성된 인스턴스를 직렬화해 만들어진다. 하지만 불변식을 깨뜨릴 의도로 임의 생성한 바이트 스트림을 건네면  
  문제가 생긴다. 정상적인 생성자로는 만들어낼 수 없는 객체를 생성해낼 수 있기 때문이다.

- 단순히 `Period`의 선언에 `implements Serializable`만 추가했다 해보자.  
  그러면 아래의 괴이한 프로그램을 수행해 종료 시각이 시작 시각보다 앞서는 `Period` 인스턴스를 만들 수 있다.

```java
public class BogusPeriod {
  private static final byte[] serializedForm = {
    (byte)0xac, (byte)0xed, (byte)0x00, (byte)0x05, 0x73, 0x72, 0x00, 0x06,
    0x50, 0x65, 0x72, 0x69, 0x6f, 0x64, 0x74, 0x40, 0x7e, (byte)0xf8,
    //..
  }
}

public static void main(String[] args) throws Exception {
  Period p = (Period) deserialize(serializedForm);
  System.out.println(p);
}

static Object deserialize(byte[] data) {
  try {
    return new ObjectInputStream(new ByteArrayInputStream(data)).readObject();
  } catch (Exception e) {
    throw new IllegalArgumentException(e);
  }
}
```

- 위 프로그램을 실행하면 end가 start보다 빠른 `Period` 인스턴스가 만들어질 수도 있다.

- 이 문제를 고치려면 `Period`의 `readObject()`가 `defaultReadObject()`를 호출한 다음, 역직렬화된 객체가  
  유효한지 검사해야 한다. 이 유효성 검사에 실패하면 `InvalidObjectException`을 던지게 하여 잘못된 역직렬화가  
  일어나는 것을 막을 수 있다.

```java
public final class Period implements Serializable {
  //..

  private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();

    if(start.compareTo(end) > 0) throw new InvalidObjectException("start > end");
  }
}
```

- 이상의 작업으로 공격자가 허용되지 않는 `Period` 인스턴스를 생성하는 일을 막을 수 있지만, 아직도 미묘한 문제가 하나  
  숨어 있다. 정상 `Period` 인스턴스에서 시작된 바이트 스트림의 끝에 private `Date` 필드로의 참조를 추가하면  
  가변 `Period` 인스턴스를 만들어낼 수 있다. 공격자는 `ObjectInputStream`에서 `Period` 인스턴스를 읽은 후  
  스트림 끝에 추가된 이 _'악의적인 객체 참조'_ 를 읽어 `Period` 객체의 내부 정보를 얻을 수 있다.  
  이제 이 참조로 얻은 `Date` 인스턴스들을 수정할 수 있으니, `Period`는 더 이상 불변이 아니게 되는 것이다.  
  아래는 이 공격이 어떻게 일어나는지를 보여주는 예시이다.

```java
public class MutablePeriod {
  public final Period period;

  public final Date start;

  public final Date end;

  public MutablePeriod() {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(bos);

      // 유효한 Period 인스턴스를 직렬화한다.
      out.writeObject(new Period(new Date(), new Date()));

      /*
       * 악의적인 '이전 객체 참조'. 즉 내부 Date로의 참조를 추가한다.
       */
      byte[] ref = {0x71, 0x7e, 0, 5};
      bos.write(ref); // start 필드
      ref[4] = 4;
      bos.write(ref); // end 필드

      // Period를 역직렬화한 후 Date 참조를 '훔친다'.
      ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
      period = (Period) in.readObject();
      start = (Date) in.readObject();
      end = (Date) in.readObject();
    } catch(IOException | ClassNotFoundException e) {
      throw new AssertionError(e);
    }
  }
}
```

- 아래 코드를 실행하면 이 공격이 실제로 이뤄지는 모습을 확인할 수 있다.

```java
public static void main(String[] args) {
  MutablePeriod mp = new MutablePeriod();
  Period p = mp.period;
  Date pEnd = mp.end;

  // 시간을 되돌린다.
  pEnd.setYear(78);

  // 60년대로 회귀
  pEnd.setYear(69);
}
```

- 이 예시에서 `Period` 인스턴스 자체는 불변식을 유지한 채 생성되었지만, 의도적으로 내부 값을 수정할 수 있었다.  
  이처럼 변경할 수 있는 `Period` 인스턴스를 획득한 공격자는 이 인스턴스가 불변이라고 가정하는 클래스에 넘겨 엄청난  
  보안 문제를 일으킬 수 있다. 이것이 너무 극단적인 예가 아닌 것이, 실제로도 보안 문제를 `String`이 불변이라는  
  사실에 기댄 클래스들이 존재하기 때문이다.

- 이 문제의 근원은 `Period`의 `readObject()`가 방어적 복사를 충분히 하지 않는 데 있다.  
  **객체를 역직렬화할 때는 클라이언트가 소유해서는 안되는 객체 참조를 갖는 필드를 모두 반드시 방어적으로 복사해야 한다.**  
  따라서 `readObject()` 에서는 불변 클래스 내의 모든 private 가변 요소를 방어적으로 벅사해야 한다.  
  아래의 `readObject()` 메소드라면 `Period`의 불변식과 불변 성질을 지켜내기에 충분하다.

```java
public final class Period implements Serializable {
  //..

  private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();

    // 가변 요소들을 방어적으로 복사한다.
    start = new Date(start.getTime());
    end = new Date(end.getTime());

    // 불변식 만족 여부 검사
    if(start.compareTo(end) > 0) throw new InvalidObjectException("start > end");
  }
}
```

- 방어적 복사를 유효성 검사보다 앞서 수행하며, `Date#clone()`을 사용하지 않았음에 주목하자. 두 조치 모두 `Period`를  
  공격으로부터 보호하는 데 필요하다. 또한 final 필드는 방어적 복사가 불가능하니 주의하자. 그래서 이 `readObject()`를  
  사용하려면 start와 end 필드에서 final 한정자를 제거해야 한다. 아쉬운 일이지만, 앞서 본 공격 위험에 노출되는 것보다야  
  낫다. start와 end에서 final 한정자를 제거하고 이 새로운 `readObject()`를 사용하면, `MutablePeriod`도  
  힘을 쓰지 못한다.

- 기본 `readObject()` 메소드를 써도 좋은지를 판단하는 간단한 방법을 보자. transient 필드를 제외한 모든 필드의 값을  
  매개변수로 받아 유효성 검사 없이 필드에 대입하는 public 생성자를 추가해도 괜찮을까? 이 질문에 대한 대답이 _"아니오"_ 라면  
  커스텀 `readObject()`를 만들어 생성자에서 수행했어야 할 모든 유효성 검사와 방어적 복사를 수행해야 한다.  
  혹은 프록시 직렬화 패턴을 사용하는 방법도 있다. 이 패턴은 역직렬화를 안전하게 만드는 데 필요한 노력을 상당히 절감해주기에  
  적극 권장된다.

- final이 아닌 직렬화 가능 클래스라면 `readObject()`와 생성자의 공통점이 하나 더 있다. 마치 생성자처럼 `readObject()`도  
  재정의 가능 메소드를 직접적으로, 혹은 간접적으로든 호출해서는 안 된다. 이 규칙을 어겼는데 해당 메소드가 재정의된다면  
  하위 클래스의 상태가 완전히 역직렬화되기 전에 하위 클래스에서 재정의된 메소드가 실행된다. 결국 프로그램은 오작동으로 이어질 것이다.

---

## 핵심 정리

- `readObject()`를 작성할 때는 언제나 public 생성자를 작성하는 자세로 임해야 한다. `readObject()`는 어떤  
  바이트 스트림이 넘어오더라도 유효한 인스턴스를 만들어내야 한다. 바이트 스트림이 진짜 직렬화된 인스턴스라고 가정해서는 안된다.  
  이번 아이템에서는 기본 직렬화 형태를 사용한 클래스를 예시로 봤지만, 커스텀 직렬화를 사용하더라도 모든 문제가 그대로  
  발생할 수 있다. 이어서 안전한 `readObject()`를 작성하는 지침을 보자.

  - private이어야 하는 객체 참조 필드는 각 필드가 가리키는 객체를 방어적으로 복사하자.  
    불변 클래스 내의 가변 요소가 여기 속한다.

  - 모든 불변식을 검사해 어긋나는 것이 발견되면 `InvalidObjectException`을 던지자.  
    방어적 복사 다음에는 반드시 불변식 검사가 뒤따라야 한다.

  - 역직렬화 후 객체 그래프 전체의 유효성을 검사해야 한다면 `ObjectInputValidation` 인터페이스를 사용하자.

  - 직접적이든 간접적이든, 재정의할 수 있는 메소드는 호출하지 말자.

---
