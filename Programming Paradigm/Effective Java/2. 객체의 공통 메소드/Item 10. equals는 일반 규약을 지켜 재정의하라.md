# equals는 일반 규약을 지켜 재정의하라

- `equals()` 메소드는 재정의하기 쉬워보이지만 곳곳에 함정이 도사리고 있어서  
  자칫하면 끔찍한 결과를 초래한다. 문제를 회피하는 가장 쉬운 길은 아예  
  재정의하지 않는 것이다. 그냥 두면 그 클래스의 인스턴스는 오직 자기 자신과만  
  같게 된다. 그러니 아래의 상황 중 하나에 해당한다면 재정의하지 않는게 최선이다.

  - **각 인스턴스가 본질적으로 고유하다.** 값을 표현하는게 아니라 동작하는 개체를  
    표현하는 인스턴스가 여기에 해당한다. `Thread`가 좋은 예로, `Object`의  
    `equals()` 메소드는 이러한 클래스에 딱 맞게 구현되어 있다.

  - **인스턴스의 '논리적 동치성(logical equality)'를 검사할 일이 없다.**  
    예를 들어 `java.util.regex.Pattern`은 `equals()`를 재정의해서 두 `Pattern`의  
    인스턴스가 같은 정규표현식을 나타내는지를 검사하는, 즉 논리적 동치성을 검사하는  
    방법도 있다. 하지만 설계자는 클라이언트가 이 방식을 원하지 않거나 애초에  
    필요하지 않다고 판단할 수 있다. 설계자가 후자로 판단했다면 `Object`의 기본  
    `equals()` 만으로 해결된다.

  - **상위 클래스에서 재정의한 `equals()`가 하위 클래스에도 딱 들어맞는다.**  
    예를 들어 대부분의 `Set` 구현체는 `AbstractSet`이 구현한 `equals()`를  
    상속받아 쓰고, `List` 구현체들은 `AbstractList`로부터, `Map` 구현체들은  
    `AbstractMap`으로부터 상속받아 그대로 쓴다.

  - **클래스가 private하거나 package-private이고 `equals()`를 호출할 일이 없다.**  
    만약 위험을 철저히 회피하는 스타일이라 `equals()`가 실수로라도 호출되는 걸  
    막고 싶다면 아래처럼 구현해두자.

  ```java
  @Override public boolean equals(Objecct o) {
      throw new AssertionError();  // 호출 금지!
  }
  ```

- 그렇다면 `equals()`를 재정의해야 할 때는 언제일까?  
  객체 식별성(object identity, 두 객체가 물리적으로 같은가)이 아니라 논리적  
  동치성을 확인해야 하는데, 상위 클래스의 `equals()`가 논리적 동치성을 비교하도록 재정의되어  
  있지 않을 때다. 주로 값 클래스들이 여기에 해당한다. 값 클래스란 `Integer`, `String`처럼  
  값을 표현하는 클래스를 말한다. 두 값 객체를 `equals()`로 비교하는 개발자는 객체가 같은지가  
  아니라 값이 같은지를 알고 싶어 할 것이다. `equals()`가 논리적 동치성을 확인하도록  
  재정의해두면, 그 인스턴스는 값을 비교하길 원하는 기대에 부응함은 물론 `Map`의 key와  
  `Set`의 원소로 사용할 수 있게 된다.

- 값 클래스라 하더라도 값이 같은 인스턴스가 둘 이상 만들어지지 않음을 보장하는  
  인스턴스 통제 클래스(<a href="https://github.com/sang-w0o/Study/blob/master/Programming%20Paradigm/Effective%20Java/1.%20%EA%B0%9D%EC%B2%B4%20%EC%83%9D%EC%84%B1%EA%B3%BC%20%ED%8C%8C%EA%B4%B4/Item%201.%20%EC%83%9D%EC%84%B1%EC%9E%90%20%EB%8C%80%EC%8B%A0%20%EC%A0%95%EC%A0%81%20%ED%8C%A9%ED%86%A0%EB%A6%AC%20%EB%A9%94%EC%86%8C%EB%93%9C%EB%A5%BC%20%EA%B3%A0%EB%A0%A4%ED%95%98%EB%9D%BC.md">아이템1</a>)라면 `equals()`를 재정의하지 않아도 된다.  
  `Enum`도 여기에 해당한다. 이런 클래스들에서는 어차피 논리적으로 같은 인스턴스가 2개 이상 만들어지지  
  않으니 논리적 동치성과 객체 식별성이 사실상 똑같은 의미가 된다. 따라서 `Object`의  
  `equals()`가 논리적 동치성까지 확인해준다고 볼 수 있다.

- `equals()`를 재정의할 때는 반드시 일반 규약을 따라야 한다.  
  아래는 `Object` 명세에 적힌 규약이다.

> `equals()`는 동치 관계를 구현하며, 아래를 만족한다.
>
> - 반사성(reflexivity): null이 아닌 모든 참조값 x에 대해 `x.equals(x)`는 true이다.
> - 대칭성(symmetry): null이 아닌 모든 참조값 x,y에 대해 `x.equals(y)`가 true이면  
>   `y.equals(x)`도 true이다.
> - 추이성(transivity): null이 아닌 모든 참조값 x,y,z에 대해 `x.equals(y)`가 true이고  
>   `y.equals(z)`도 true이면 `x.equals(z)`도 true이다.
> - 일관성(consistency): null이 아닌 모든 참조값 x,y에 대해 `x.equals(y)`를 반복해서  
>   호출하면 항상 true를 반환하거나 항상 false를 반환한다.
> - null-아님: null이 아닌 모든 참조값 x에 대해 `x.equals(null)`은 항상 false다.

- 위 규약을 어기면 프로그램이 이상하게 동작하거나 종료될 것이고, 원인이 되는 코드를  
  찾기도 굉장히 어려울 것이다. 세상에 홀로 존재하는 클래스는 없다. 한 클래스의 인스턴스는  
  다른 곳으로 빈번히 전달된다. 그리고 컬렉션 클래스들을 포함해 수많은 클래스는 전달받은 객체가  
  `equals()` 규약을 지킨다고 가정하고 동작한다.

- 이제 규약 자체를 자세히 알아보자.  
  먼저 `Object` 명세에서 말하는 _동치 관계_ 란 무엇일까? 쉽게 말해, 집합을 서로 같은  
  원소들로 이뤄진 부분 집합으로 나누는 연산이다. 이 부분집합을 동치류(equivalence class, 동치 클래스)라  
  한다. `equals()`가 쓸모 있으려면 모든 원소가 같은 동치류에 속한 어떤 원소와도  
  서로 교환할 수 있어야 한다. 이제 동치 관계를 만족시키기 위한 5가지 요건을 살펴보자.

- **반사성**은 단순히 말하면 객체는 자기 자신과 같아야 한다는 뜻이다.  
  이 요건은 일부로 어기는 경우가 아니라면 만족시키지 못하기가 더 어려워 보인다.  
  이 요건을 어긴 클래스의 인스턴스를 컬렉션에 넣은 다음 `contains()` 메소드를 호출하면  
  방금 넣은 인스턴스가 없다고 말할 것이다.

- **대칭성**은 두 객체는 서로에 대한 동치 여부에 똑같이 답해야 한다는 뜻이다.  
  반사성 요건과 달리 대칭성 요건은 자칫하면 어길 수 있어 보인다.  
  대소문자를 구별하지 않는 문자열을 구현한 아래 클래스를 예로 살펴보자. 이 클래스에서  
  `toString()`은 원본 문자열의 대소문자를 그대로 돌려주지만 `equals()`에서는  
  대소문자를 무시한다.

```java
public final class CaseInsensitiveString {
    private final String s;

    public CaseInsensitiveString(String s) {
	this.s = Objects.requireNonNull(s);
    }

    // 대칭성 위배!
    @Override
    public boolean equals(Object o) {
	if(o instanceof CaseInsensitiveString)
	    return s.equalsIgnoreCase(
		((CaseInsensitiveString) o).s);
	if(o instanceof String)
	    return s.equalsIgnoreCase((String) o);
	return false;
    }

    //..
}
```

- `CaseInsensitiveString`의 `equals()`는 순진하게 일반 문자열과도 비교를 시도한다.  
  아래처럼 `CaseInsensitiveString`과 `String`객체가 하나씩 있다 해보자.

```java
CaseInsensitiveString cis = new CaseInsensitiveString("Polish");
String s = "polish";
```

- 예상할 수 있듯 `cis.equals(s)`는 true가 된다.  
  문제는 `CaseInsensitiveString`의 `equals()`는 일반 `String`을 알고 있지만  
  `String#equals()`는 `CaseInsensitiveString`의 존재를 모른다는 데 있다.  
  따라서 `s.equals(cis)`는 false를 반환하여 대칭성을 명백히 위반한다.

```java
List<CaseInsensitiveString> list = new ArrayList<>();
list.add(cis);
```

- 이 다음에 `list.contains(s)`를 호출하면 결과가 어떻게 될까?  
  현재 OpenJDK에서는 false를 반환하긴 하지만 이는 순전히 구현하기 나름이라  
  OpenJDK 버전이 바뀌거나 다른 JDK에서는 true를 반환하거나 런타임 예외를 던질 수도 있다.  
  **`equals()` 규약을 어기면 그 객체를 사용하는 다른 객체들이 어떻게 반응할지 알 수 없다.**

- 이 문제를 해결하려면 `CaseInsensitiveString#equals()`를 `String`과도  
  연동하겠다는 허황한 꿈을 버려야 한다. 그 결과 아래처럼 간단하게 바뀐다.

```java
public final class CaseInsensitiveString {
    private final String s;

    public CaseInsensitiveString(String s) {
	this.s = Objects.requireNonNull(s);
    }

    // 대칭성 위배!
    @Override
    public boolean equals(Object o) {
	return o instanceof CaseInsensitiveString &&
	    ((CaseInsensitiveString) o).s.equalsIgnoreCase(s);
    }

    //..
}
```

- **추이성**은 첫 번째 객체가 두 번째 겍체와 같고, 두 번째 겍채와 세 번째 객체가  
  같다면 첫 번째 객체와 세 번째 객체도 같아야 한다는 뜻이다. 이 요건도 간단하지만  
  자칫하면 어기기 쉽다. 상위 클래스에는 없는 새로운 필드를 하위 클래스에 추가하는  
  상황을 생각해보자. `equals()` 비교에 영향을 주는 정보를 추가한 것이다.  
  간단히 2차원에서 점을 표현하는 클래스를 예로 들어보자.

```java
public class Point {
    private final int x;
    private final int y;

    public Point(int x, int y) {
	this.x = x; this.y = y;
    }

    @Override
    public boolean equals(Object o) {
	if(!(o instanceof Point)) return false;
	Point p = (Point)o;
	return p.x == x && p.y == y;
    }

    //..
}
```

- 이제 이 클래스를 확장해서 점에 색상을 더해보자.

```java
public class ColorPoint extends Point {
    private final Color color;

    public ColorPoint(int x, int y, Color color) {
	super(x, y);
	this.color = color;
    }

    //..
}
```

- `equals()`는 어떻게 해야할까? 그대로 둔다면 `Point`의 구현이 상속되어 색상 정보는  
  무시한 채 비교를 수행한다. `equals()` 규약을 어긴 것은 아니지만, 중요한 정보를  
  놓치게 되니 받아들일 수 없는 상황이다. 아래 코드처럼 비교 대상이 또 다른 `ColorPoint`이고  
  위치와 색상이 같을 때만 true를 반환하는 `equals()`를 작성해보자.

```java
@Override
public boolean equals(Object o) {
    if(!(o instanceof ColorPoint)) return false;
    return super.equals(o) ** ((ColorPoint) o).color == color;
}
```

- 이 메소드는 일반 `Point`를 `ColorPoint`에 비교한 결과와 그 둘을 바꿔 비교한 결과가  
  다를 수 있다. `Point#equals()`는 color를 무시하고, `ColorPoint#equals()`는  
  입력 매개변수의 클래스 종류가 다르다며 매번 false만 반환할 것이다.  
  각각의 인스턴스를 만들어 실제로 동작하는 모습을 확인해보자.

```java
Point p = new Point(1, 2);
ColorPoint cp = new ColorPoint(1, 2, Color.RED);
```

- 이제 `p.equals(cp)`는 true를, `cp.equals(p)`는 false를 반환한다.  
  아래처럼 `ColorPoint#equals()`가 `Point`와 비교할 때만 color를 무시하도록 하면 해결될까?

```java
@Override
public boolean equals(Object o) {
    if(!(of instanceof Point)) return false;

    // o가 Point라면 color는 비교하지 않는다.
    // 아래 코드 때문에 무한 재귀 발생 가능성이 생긴다.
    if(!(o instanceof ColorPoint)) return o.equals(this);

    // o가 ColorPoint라면 color까지 비교한다.
    return super.equals(o) ** ((ColorPoint) o).color == color;
}
```

- 위 방식은 대칭성은 지켜주지만 추이성을 깨버린다.

```java
ColorPoint p1 = new ColorPoint(1, 2, Color.RED);
Point p2 = new Point(1, 2);
ColorPoint p3 = new ColorPoint(1, 2, Color.BLUE);

p1.equals(p2);  // true
p2.equals(p3);  // true
p1.equals(p3);  // false => 추이성 위배!
```

- 위 방식은 추이성 위배 뿐만 아니라 무한 재귀에 빠질 위험도 있다.  
  `Point`의 또 다른 하위 클래스로 `SmellPoint`를 만들고, `equals()`는 같은 방식으로  
  구현했다 해보자. 그런 다음 `cp.equals(smellPoint)`를 호출하면 `StackOverflowError`가  
  발생한다.

- 그럼 해법은 무엇일까? 사실 이 현상은 모든 객체지향 언어의 동치 관계에서  
  나타나는 근본적인 문제이다. **구체클래스를 확장해 새로운 값을 추가하면서 equals 규약을**  
  **만족시킬 방법은 존재하지 않는다.** 객체지향적 추상화의 이점을 포기하지 않는 한 말이다.

- 이 말은 얼핏 `equals()` 안의 instanceof 검사를 `getClass()` 검사로 바꾸면  
  규약도 지키고 값도 추가하면서 구체 클래스를 상속할 수 있다는 뜻으로 들린다.

```java
@Override
public boolean equals(Object o) {
    if(o == null || o.getClass() != getClass()) return false;
    Point p = (Point) o;
    return p.x == x && p.y == y;
}
```

- 이번 `equals()`는 같은 구현 클래스의 객체와 비교할 때만 true를 반환한다.  
  괜찮아 보이지만 실제로 활용할 수는 없다. `Point`의 하위 클래스는 정의상 여전히  
  `Point`이므로 어디서든 `Point`로써 활용될 수 있어야 한다. 그런데 이 방식에서는 그렇지 못하다.  
  예를 들어 주어진 점이 반지름이 1인 단위 원 안에 있는지를 판별하는 메소드가 필요하다 해보자.  
  아래는 이를 구현한 코드이다.

```java
private static final Set<Point> unitCircle = Set.of(
    new Point(1, 0), new Point(0, 1),
    new Point(-1, 0), new Point(0, -1));

public static boolean onUnitCircle(Point p) {
    return unitCircle.contains(p);
}
```

- 이 기능을 구현하는 가장 빠른 방법은 아니겠지만, 어쨋든 동작은 한다.  
  이제 값을 추가하지 않는 방식으로 `Point`를 확장해보자. 그리고 만들어진 인스턴스의 개수를  
  생성자에서 세보도록 하자.

```java
public class CounterPoint extends Point {
    private static final AtomicInteger counter = new AtomicInteger();

    public CounterPoint(int x, int y) {
	super(x, y);
	counter.incrementAndGet();
    }

    public static int numberCreated() { return counter.get(); }
}
```

- LSP(Liscov Substitution Principle)에 따르면, 어떤 타입에 있어 중요한 속성이라면  
  그 하위 타입에서도 마찬가지로 중요하다. 따라서 그 타입의 모든 메소드가 하위 타입에서도  
  똑같이 잘 작동해야 한다. 즉 _`Point`의 하위 클래스는 정의상 여전히 `Point`이므로_  
  _어디서든 `Point`로써 활용될 수 있어야 한다._ 는 것을 의미한다.

- 그런데 `CounterPoint`의 인스턴스를 `onUnitCircle()`에 넘기면 어떻게 될까?  
  `Point#equals()`를 `getClass()`를 이용해 작성했다면 `onUnitCircle()`은 false를  
  반환할 것이다. `CounterPoint` 인스턴스의 x, y값과는 무관하게 말이다. 왜 그럴까?  
  원인은 컬렉션 구현체에서 주어진 원소를 담고 있는지를 확인하는 데에 있다. 이 예시에서는  
  `Set#contains()`가 여기에 해당한다. 이 외에도 대부분의 컬렉션은 이와 같은 작업에  
  내부적으로 `equals()`를 사용하는데, `CounterPoint`의 인스턴스는 어떤 `Point`와도  
  같을 수 없기 때문이다. 반면, `Point#equals()`를 instanceof 기반으로 올바르게 구현했다면  
  `CounterPoint` 인스턴스를 넘겨줘도 `onUnitCircle()`은 원하는대로 동작할 것이다.

- 구체 클래스의 하위 클래스에서 값을 추가할 방법은 없지만, 괜찮은 우회 방법이 있다.  
  바로 _상속 대신 컴포지션을 사용_ 하는 것이다. `Point`를 상속하는 대신 `Point`를  
  `ColorPoint`의 private 필드로 두고, `ColorPoint`와 같은 위치의 일반 `Point`를  
  반환하는 view method를 public으로 추가하는 식이다.

```java
public class ColorPoint {
    private final Point point;
    private final Color color;

    public ColorPoint(int x, int y, Color color) {
	point = new Point(x, y);
	this.color = Objects.requireNonNull(color);
    }

    /**
     * 이 ColorPoint의 Point 뷰 반환
    */
    public Point asPoint() { return point; }

    @Override
    public boolean equals(Object o) {
	if(!(o instanceof ColorPoint)) return false;
	ColorPoint cp = (ColorPoint) o;
	return cp.point.equals(point) && cp.color.equals(color);
    }
}
```

- Java 라이브러리에도 구체 클래스를 확장해 값을 추가한 클래스가 종종 있다.  
  예를 들어 `java.sql.Timestamp`는 `java.util.Date`를 확장한 후 nanoseconds 필드를 추가했다.  
  그 결과로 `Timestamp#equals()`는 대칭성을 위배하며, `Date` 객체와 한 컬렉션에 같이  
  넣거나 서로 섞어 사용하면 엉뚱하게 동작할 수 있다. 그래서 `Timestamp`의 API 설명에는  
  `Date`와 섞어 쓸 때의 주의사항을 언급하고 있다. 둘을 명확히 분리해 사용하는 한 문제될 것은  
  없지만, 섞이지 않도록 보장해줄 수단은 없다. 자칫 실수하면 디버깅하기 어려운 이상한 오류를  
  경험할 수 있으니 주의하자.
