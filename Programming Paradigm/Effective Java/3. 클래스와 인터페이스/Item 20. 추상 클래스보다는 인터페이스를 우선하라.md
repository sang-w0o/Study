# 추상 클래스보다는 인터페이스를 우선하라

- Java가 제공하는 다중 구현 메커니즘은 인터페이스와 추상 클래스, 이렇게 두 가지다.  
  Java8부터 인터페이스도 default 메소드를 제공할 수 있게 되어, 이제는 두 메커니즘  
  모두 인스턴스 메소드를 구현 형태로 제공할 수 있다. 한편, 둘의 가장 큰 차이는  
  **추상 클래스가 정의한 타입을 구현하는 클래스는 반드시 추상 클래스의 하위 타입이**  
  **되어야 한다**는 점이다. Java는 단일 상속만 지원하니, 추상 클래스 방식은 새로운 타입을  
  정의하는 데 커다란 제약은 안게 되는 셈이다. 반면, 인터페이스가 선언한 메소드를 모두  
  정의하고 그 일반 규약을 잘 지킨 클래스라면 다른 어떤 클래스를 상속했든 같은 타입으로  
  취급된다.

- **기존 클래스에도 손쉽게 새로운 인터페이스를 구현해 넣을 수 있다.**  
  인터페이스가 요구하는 메소드를 추가하고, 클래스 선언에 implements만 추가하면 끝이다.  
  Java 플랫폼에서도 `Comparable`, `Iterable`, `AutoClosable` 인터페이스가  
  새로 추가됐을 때 표준 라이브러리의 수많은 기존 클래스가 이 인터페이스들을 구현한 채  
  릴리즈 됐다. 반면, 기존 클래스 위에 새로운 추상 클래스를 끼워넣는 일은 어려운게 일반적이다.  
  두 클래스가 같은 추상 클래스를 확장하길 원한다면, 그 추상 클래스는 계층 구조상 두 클래스의  
  공통 조상이어야 한다. 안타깝게도 이 방식은 클래스 계층 구조에 커다란 혼란을 일으킨다.  
  새로 추가된 추상 클래스의 모든 자손이 이를 상속하게 되는 것이다. 그렇게 하는 것이  
  적절하지 않은 상황에서도 강제로 말이다.

- **인터페이스는 mixin 정의에 안성맞춤이다.**  
  Mixin이란 클래스가 구현할 수 있는 타입으로, Mixin을 구현한 클래스에 원래의  
  _'주된 타입'_ 외에도 특정 선택적 행위를 제공한다고 선언하는 효과를 준다.  
  예를 들어, `Comparable`은 자신을 구현한 클래스의 인스턴스들끼리는 순서를  
  정할 수 있다고 선언하는 Mixin 인터페이스이다. 이처럼 대상 타입의 주된 기능에  
  선택적 기능을 _혼합(mixed-in)_ 한다 해서 Mixin이라 부른다. 추상 클래스로는  
  Mixin을 정의할 수 없다. 이유는 앞서 본 것과 같이, 기존 클래스에 덧씌울 수 없기 때문이다.  
  클래스는 두 부모를 섬길 수 없고, 클래스 계층구조에는 Mixin을 삽입하기에 합리적인  
  위치가 없기 때문이다.

- **인터페이스로는 계층구조가 없는 타입 프레임워크를 만들 수 있다.**  
  타입을 계층적으로 정의하면 수많은 개념을 구조적으로 잘 표현할 수 있지만, 현실에는  
  계층을 엄격히 구분하기 어려운 개념도 있다. 예를 들어, `Singer` 인터페이스와  
  `SongWriter` 인터페이스가 있다 해보자.

```java
public interface Singer {
    AudioClip sing(Song s);
}

public interface SongWriter {
    Song compose(int chartPosition);
}
```

- 하지만 요즘에는 작곡도 하는 가수(SingerSongWriter)도 꽤 있다.  
  위 코드처럼 타입을 인터페이스로 정의하면, 가수 클래스가 `Singer`와 `SongWriter` 모두를  
  구현해도 전혀 문제되지 않는다. 심지어 `Singer`와 `SongWriter` 모두를 확장하고,  
  새로운 메소드까지 추가한 제3의 인터페이스를 정의할 수도 있다.

```java
public interface SingerSongWriter extends Singer, SongWriter {
    AudioClip strum();
    void actSensitive();
}
```

- 이 정도의 유연성이 항상 필요한 것은 아니지만, 이렇게 만들어둔 인터페이스가 결정적인 도움을  
  줄 수도 있다. 같은 구조를 클래스로 만들려면 가능한 조합 전부를 각각의 클래스로 정의한  
  고도비만 계층구조가 만들어질 것이다. 속성이 n개라면 지원해야 할 조합의 수는 `2^n`개나 된다.  
  흑히 조합 폭발(Combination Explosion)이라 부르는 현상이다. 거대한 클래스 계층구조에는  
  공통 기능을 정의해놓은 타입이 없으니, 자칫 매개변수 타입만 다른 메소드들을 수없이 많이 가진  
  거대한 클래스를 낳을 수도 있다.

- Wrapper 클래스와 함께 사용하면 **인터페이스는 기능을 향상시키는 안전하고 강력한 수단이 된다.**  
  타입을 추상클래스로 정의해두면 그 타입에 기능을 추가하는 방법은 상속 뿐이다.  
  상속해서 만든 클래스는 Wrapper 클래스보다 활용도가 떨어지고, 깨지기는 더 쉽다.

- 인터페이스의 메소드 중 구현 방법이 명확한 것이 있다면, 그 구현을 default 메소드로 제공해  
  프로그래머들의 일감을 덜어줄 수도 있다. 이 기법의 예시로 아래 코드를 보자.

```java
package java.util;

public interface Collection<E> extends Iterable<E> {
    /**
     * Removes all of the elements of this collection that satisfy the given
     * predicate.  Errors or runtime exceptions thrown during iteration or by
     * the predicate are relayed to the caller.
     *
     * @implSpec
     * The default implementation traverses all elements of the collection using
     * its {@link #iterator}.  Each matching element is removed using
     * {@link Iterator#remove()}.  If the collection's iterator does not
     * support removal then an {@code UnsupportedOperationException} will be
     * thrown on the first matching element.
     *
     * @param filter a predicate which returns {@code true} for elements to be
     *        removed
     * @return {@code true} if any elements were removed
     * @throws NullPointerException if the specified filter is null
     * @throws UnsupportedOperationException if elements cannot be removed
     *         from this collection.  Implementations may throw this exception if a
     *         matching element cannot be removed or if, in general, removal is not
     *         supported.
     * @since 1.8
     */
    default boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        boolean removed = false;
        final Iterator<E> each = iterator();
        while (each.hasNext()) {
            if (filter.test(each.next())) {
                each.remove();
                removed = true;
            }
        }
        return removed;
    }
}
```

- 위 처럼 default 메소드를 제공할 때는 상속하려는 사람을 위한 `@implSpec` JavaDoc 태그를 붙여  
  문서화해야 한다.

- Default 메소드에도 제약은 있다. 많은 인터페이스가 `equals()`와 `hashCode()`와 같은 `Object`의  
  메소드를 재정의하고 있지만, 이들은 default 메소드로 제공해서는 안된다. 또한 인터페이스는 인스턴스 필드를  
  가질 수 없고, public이 아닌 정적 멤버도 가질 수 없다. (단, private static 메소드는 예외다.)  
  마지막으로, 클라이언트가 만들지 않은 인터페이스에는 default 메소드를 추가할 수 없다.

- 한편, **인터페이스와 추상 골격 구현(Skeletal Implementation) 클래스를 함께 제공**하는 식으로  
  인터페이스와 추상 클래스의 장점을 모두 취하는 방법도 있다. 인터페이스로는 타입을 정의하고,  
  필요하면 default 메소드 몇 개도 함께 제공한다. 그리고 골격 구현 클래스는 나머지 메소드들까지  
  구현한다. 이렇게 해두면 단순히 골격 구현을 확장하는 것만으로 이 인터페이스를 구현하는 데 필요한  
  일들이 대부분 완료된다. 이것이 바로 **템플릿 메소드 패턴**이다.

- 관례상 인터페이스 이름이 `Interface`라면 그 골격 구현 클래스의 이름은 `AbstractInterface`라  
  짓는다. 좋은 예시로 컬렉션 프레임워크의 `AbstractCollection`, `AbstractSet`,  
  `AbstractList`, `AbstractMap` 등 이 각각이 바로 핵심 컬렉션 인터페이스의 골격 구현이다.  
  제대로 설계했다면 골격 구현은 독립된 추상 클래스든, default 메소드로 이뤄진 인터페이스든  
  그 인터페이스로 나름의 구현을 만들려는 프로그래머의 일을 상당히 덜어준다.  
  예시를 보자. 아래 코드는 완벽히 동작하는 `List`의 구현체를 반환하는 정적 팩토리 메소드로,  
  `AbstractList`를 골격 구현으로 활용했다.

```java
static List<Integer> intArrayAsList(int[] a) {
    Objects.requireNonNull(a);

    return new AbstractList<Integer>() {
	@Override public Integer get(int i) {
	    return a[i];  // Auto-Boxing
	}

	@Override public Integer set(int i, Integer val) {
	    int oldVal = a[i];
	    a[i] = val;  // Auto-Unboxing
	    return oldVal;  // Auto-Boxing
	}

	@Override public int size() {
	    return a.length;
	}
    }
}
```

- `List` 구현체가 제공하는 기능들을 생각하면, 이 코드는 골격 구현의 힘을  
  잘 보여주는 인상적인 예시라 할 수 있다. 그와 별개로 위 코드는 int 배열을 받아  
  `Integer` 인스턴스의 리스트 형태로 보여주는 어댑터(Adapter)이기도 하다.  
  int와 `Integer` 사이의 변환(boxing, unboxing) 때문에 성능은 그리 좋지 않다.  
  또한, 이 구현에서 익명 클래스를 사용했음에 주목하자.

- 골격 구현 클래스의 아름다움은 추상 클래스처럼 구현을 도와주는 동시에, 추상 클래스로  
  타입을 정의할 때 따라오는 심각한 제약에서는 자유롭다는 점에 있다. 골격 구현을 확장하는  
  것으로 인터페이스 구현이 거의 끝나지만, 꼭 이렇게 해야하는 것은 아니다. 구조상 골격 구현을  
  확장하지 못하는 처지라면 인터페이스를 직접 구현해야 한다. 이런 경우라도 인터페이스가 직접  
  제공하는 default 메소드의 이점을 그대로 누릴 수 있다. 또한, 골격 구현 클래스를 우회적으로  
  이용할 수도 있다. 인터페이스를 구현한 클래스에서 해당 골격 구현을 확장한 private 내부  
  클래스를 정의하고, 각 메소드 호출을 내부 클래스의 인스턴스에 전달하는 것이다. Wrapper 클래스와  
  비슷한 이 방식을 _시뮬레이트한 다중 상속(Simulated Multiple Inheritance)_ 라 하며,  
  다중 상속의 많은 장점을 제공하는 동시에 단점은 피하게 해준다.

- 골격 구현 작성은 상대적으로 쉽다. 가장 먼저, **인터페이스를 잘 살펴 다른 메소드들의**  
  **구현에 사용되는 기반 메소드들을 선정**한다. 이 기반 메소드들은 골격 구현에서는  
  추상 메소드가 된다. 그 다음으로 **기반 메소드들을 사용해 직접 구현할 수 있는 메소드를**  
  **모두 default 메소드로 제공한다.** 단, `equals()`, `hashCode()` 같은 `Object`의  
  메소드는 default 메소드로 제공하면 안된다는 사실을 항상 유념하자. 만약 인터페이스의 메소드  
  모두가 기반 메소드와 디폴트 메소드가 된다면, 골격 구현 클래스를 별도로 만들 이유는 없다.  
  기반 메소드나 디폴트 메소드로 만들지 못한 메소드가 남아 있다면, 이 인터페이스를 구현하는  
  골격 구현 클래스를 하나 만들어 남은 메소드들을 작성해 넣는다. 골격 구현 클래스에는 필요하면  
  public이 아닌 필드와 메소드를 추가해도 된다.

- 간단한 예시로 `Map.Entry` 인터페이스를 살펴보자. `getKey()`, `getValue()`는 확실히  
  기반 메소드이며, 선택적으로 `setValue()`도 포함할 수 있다. 이 인터페이스는 `equals()`,  
  `hashCode()`의 동작 방식도 정의해놨다. `Object` 메소드들은 default 메소드로 제공해서는  
  안되므로, 해당 메소드들은 모두 골격 구현 클래스에서 정의해야 한다. `toString()`도 기반 메소드를  
  사용해 구현해놨다.

```java
public abstract class AbstractMapEntry<K, V> implements Map.Entry<K, V> {

    // 변경 가능한 Entry는 반드시 이 메소드를 재정의해야 한다.
    @Override public V setValue(V value) {
	throw new UnsupportedOperationException();
    }

    // Map.Entry.equals의 일반 규약을 구현한다.
    @Override public boolean equals(Object o) {
	if (o == this) return true;
	if (!(o instanceof Map.Entry)) return false;
	Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
	return Objects.equals(e.getKey(), getKey()) &&
	    Objects.equals(e.getValue(), getValue());
    }

    // Map.Entry.hashCode의 일반 규약을 구현한다.
    @Override public int hashCode() {
	return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
    }
}
```

- 골격 구현은 기본적으로 상속해서 사용하는 것을 가정하므로, Item 19에서 본 설계 및  
  문서화 지침을 모두 따라야 한다. 간략히 보여주기 위해 위 코드에서는 문서화 주석이 없지만,  
  인터페이스에 정의한 디폴트 메소드든, 별도의 추상 클래스든, 골격 구현은 반드시 그 동작  
  방식을 잘 정리해 문서로 남겨야 한다.

- **단순 구현(Simple Implementations) 은 골격 구현의 작은 변종** 으로,  
  `AbstractMap.SimpleEntry`가 좋은 예다. 단순 구현도 골격 구현과 같이 상속을 위해  
  인터페이스를 구현한 것이지만, 추상 클래스가 아니라는 점이 다르다. 쉽게 말해 동작하는  
  가장 단순한 구현이다. 이러한 단순 구현은 그대로 써도 되고, 필요에 맞게 확장해도 된다.

<hr/>

## 핵심 정리

- 일반적으로 다중 구현용 타입으로는 인터페이스가 가장 적합하다. 복잡한 인터페이스라면  
  구현하는 수고를 덜어주는 골격 구현 클래스를 함께 제공하는 방법을 꼭 고려하자.  
  골격 구현은 _'가능한 한'_ 인터페이스의 default 메소드로 제공하여 그 인터페이스를  
  구현한 모든 곳에서 활용하도록 하는 것이 좋다. _'가능한 한'_ 이라 한 이유는  
  인터페이스에 걸려 있는 구현상의 제약 때문에 골격 구현을 추상 클래스로 제공하는 경우가  
  더 흔하기 때문이다.

<hr/>
