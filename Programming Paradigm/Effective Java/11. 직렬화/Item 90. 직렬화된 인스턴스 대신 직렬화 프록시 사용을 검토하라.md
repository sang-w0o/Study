# 직렬화된 인스턴스 대신 직렬화 프록시 사용을 검토하라

- 계속 봐왔듯이, `Serializable`을 구현하기로 결정한 순간 언어의 정상 메커니즘인 생성자 이외의 방법으로  
  인스턴스를 생성할 수 있게 된다. 버그와 보안 문제가 일어날 가능성이 커진다는 뜻이다. 하지만 이 위험을  
  크게 줄여줄 기법이 하나 있다. 바로 직렬화 **프록시 패턴(Serialization Proxy Pattern)** 이다.

- 직렬화 프록시 패턴은 그리 복잡하지 않다. 먼저, 바깥 클래스의 논리적 상태를 정밀하게 표현하는 중첩 클래스를  
  설계해 private static으로 선언한다. 이 중첩 클래스가 바로 바깥 클래스의 직렬화 프록시다.  
  중첩 클래스의 생성자는 단 하나여야 하며, 바깥 클래스를 매개변수도 받아야 한다. 이 생성자는 단순히 인수로  
  넘어온 인스턴스의 데이터를 복사한다. 일관성 검사나 방어적 복사도 필요 없다. 설계상, 직렬화 프록시의 기본  
  직렬화 형태는 바깥 클래스의 직렬화 형태로 쓰기에 이상적이다. 그리고 바깥 클래스와 직렬화 프록시 모두  
  `Serializable`을 구현한다고 선언해야 한다.

- 이전에 본 `Period` 클래스를 다시 보자. 아래는 이 클래스의 직렬화 프록시다.  
  `Period`는 아주 간단하여 직렬화 프록시도 바깥 클래스와 완전히 같은 필드로 구성되었다.

```java
public final class Period implements Serializable {
    private Date start;
    private Date end;

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

     private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
	s.defaultReadObject();

	// 가변 요소들을 방어적으로 복사한다.
	start = new Date(start.getTime());
	end = new Date(end.getTime());

	// 불변식 만족 여부 검사
	if(start.compareTo(end) > 0) throw new InvalidObjectException("start > end");
    }

    // 직렬화 프록시
    private static class SerializationProxy implements Serializable {
	private final Date start;
	private final Date end;

	SerializationProxy(Period p) {
	    this.start = p.start;
	    this.end = p.end;
	}

	private static final long serialVersionUID = 1L; // 아무 값이나 상관 없다.
    }
}
```

- 다음으로, 바깥 클래스에 아래와 같이 `writeReplace()` 메소드를 추가한다. 이 메소드는 범용적이니  
  직렬화 프록시를 사용하는 모든 클래스에 그대로 복사해 쓰면 된다.

```java
public final class Period implements Serializable {
    //..

    // 직렬화 프록시 패턴을 위한 메소드
    private Object writeReplace() {
	return new SerializationProxy(this);
    }
}
```

- 이 메소드는 Java의 직렬화 시스템이 바깥 클래스의 인스턴스 대신 `SerializationProxy`의 인스턴스를  
  반환하게 하는 역할을 한다. 달리 말해, 직렬화가 이뤄지기 전에 바깥 클래스의 인스턴스를 직렬화 프록시로  
  변환해준다.

- `writeReplace()` 덕분에 직렬화 시스템은 결고 바깥 클래스의 직렬화된 인스턴스를 생성해낼 수 없다.  
  하지만 공격자는 불변식을 훼손하고자 이런 시도를 해볼 수 있다. 아래의 `readObject()`를 바깥 클래스에  
  추가하면, 이 공격을 가볍게 막아낼 수 있다.

```java
public final class Period implements Serializable {

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
	throw new InvalidObjectException("Proxy required.");
    }
}
```

- 마지막으로 바깥 클래스와 논리적으로 동일한 인스턴스를 반환하는 `readResolve()` 메소드를 `SerializationProxy`에  
  추가한다. 이 메소드는 역직렬화 시에 직렬화 시스템이 직렬화 프록시를 다시 바깥 클래스의 인스턴스로 변환하게 해준다.

- `readResolve()`는 공개된 API만을 이용해 바깥 클래스의 인스턴스를 생성하는데, 이 패턴이 아름다운 이유가 여기에 있다.  
  직렬화는 생성자를 이용하지 않고도 인스턴스를 생성하는 기능을 제공하는데, 이 패턴은 직렬화의 이런 언어도단적 특성을  
  상당 부분 제거한다. 즉, 일반 인스턴스를 만들 때와 똑같은 생성자, 정적 팩토리, 혹은 다른 메소드를 사용해 역직렬화된  
  인스턴스를 생성하는 것이다. 따라서 역직렬화된 인스턴스가 해당 클래스의 불변식을 만족하는지 검사할 또 다른 수단을  
  강구하지 않아도 된다. 그 클래스의 정적 팩토리나 생성자가 불변식을 확인해주고 인스턴스 메소드들이 불변식을 잘 지켜준다면,  
  따로 다 해줘야 할 일이 없는 것이다.

```java
private static class SerializationProxy implements Serializable {
    private final Date start;
    private final Date end;

    SerializationProxy(Period p) {
	this.start = p.start;
	this.end = p.end;
    }

    private Object readResolve() {
	return new Period(start, end); // public 생성자 사용
    }

    private static final long serialVersionUID = 1L; // 아무 값이나 상관 없다.
}
```

- 방어적 복사처럼, 직렬화 프록시 패턴은 가짜 바이트 스트림 공격과 내부 필드 탈취 공격을 프록시 수준에서 차단해준다.  
  앞서의 두 접근법과 달리, 직렬화 프록시는 `Period`의 필드를 final로 선언해도 되므로 `Period` 클래스를  
  진정한 불변으로 만들 수도 있다. 또한 이리저리 고민할 거리도 거의 없다. 어떤 필드가 기만적인 직렬화 공격의  
  목표가 될지 고민하지 않아도 되며, 역직렬화할 때 유효성 검사를 수행하지 않아도 된다.

- 직렬화 프록시 패턴이 `readObject()`에서의 방어적 복사보다 강력한 경우가 하나 더 있다. 직렬화 프록시 패턴은  
  역직렬화한 인스턴스와 원래의 직렬화된 인스턴스의 클래스가 달라도 정상 작동한다.

- `EnumSet`의 사례를 생각해보자. 이 클래스는 public 생성자 없이 정적 팩토리들만 지원한다. 클라이언트 입장에서는  
  이 팩토리들이 `EnumSet` 인스턴스를 반환하는 것으로 보이지만, 현재의 OpenJDK를 보면 열거 타입의 크기에 따라  
  두 하위 클래스 중 하나의 인스턴스를 반환한다. 열거 타입의 원소가 64개 이하면 `RegularEnumSet`을 사용하고,  
  그보다 크면 `JumboEnumSet`을 사용하는 것이다.

- 이제 원소 64개짜리 열거 타입을 가진 `EnumSet`을 직렬화한 다음, 원소 5개를 추가하고 역직렬화하면 어떻게  
  되는지 보자. 처음 직렬화된 것은 `RegularEnumSet` 인스턴스다. 하지만 역직렬화는 `JumboEnumSet`으로 하면  
  좋을 것이다. 그리고 `EnumSet`은 직렬화 프록시 패턴을 사용해서, 실제로도 이렇게 동작한다.

```java
private static class SerializationProxy<E extends Enum<E>> implements Serializable {
    // 이 EnumSet의 원소 타입
    private final Class<E> elementType;

    // 이 EnumSet 내의 원소들
    private final Enum<?>[] elements;

    SerializationProxy(EnumSet<E> s) {
	elementType = s.elementType();
	elements = s.toArray(new Enum<?>[0]);
    }

    private Object readResolve() {
	EnumSet<E> result = EnumSet.noneOf(elementType);
	for(Enum<?> e : elements) {
	    result.add((E) e);
	}
	return result;
    }

    private static final long serialVersionUID = -22393799407077455L;
}
```

- 직렬화 프록시 패턴에는 한계가 두 가지 있다. 첫 번째, 클라이언트가 멋대로 확장할 수 있는 클래스에는 적용할 수 없다.  
  두 번째, 객체 그래프에 순환이 있는 클래스에도 적용할 수 없다. 이런 객체의 메소드를 직렬화 프록시의 `readResolve()`  
  안에서 호출하려 하면 `ClassCastException`이 발생할 것이다. 직렬화 프록시만 가졌을 뿐, 실제 객체는 아직  
  만들어진 것이 아니기 때문이다.

- 마지막으로, 직렬화 프록시 패턴이 주는 강력함과 안전성에도 대가는 따르다. 속도가 방어적 복사보다 느려진다.

<hr/>

## 핵심 정리

- 제3자가 확장할 수 없는 클래스라면 가능한 한 직렬화 프록시 패턴을 사용하자. 이 패턴이 아마도 중요한  
  불변식을 안정적으로 직렬화해주는 가장 쉬운 방법일 것이다.

<hr/>
