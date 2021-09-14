# Comparable을 구현할지 고민하라

- 이번에는 `Comparable` 인터페이스의 유일한 메소드인 `compareTo`를 알아보자.  
  `compareTo`는 `Object`의 메소드가 아니다. 성격은 두 가지만 빼면 `equals()`와  
  같다. 다른 점은 `compareTo`는 단순 동치성 비교에 더해 순서까지 비교할 수 있으며,  
  제네릭하다. `Comparable`을 구현했다는 것은 그 클래스의 인스턴스들에는 자연적인 순서  
  (natural order)가 있음을 뜻한다. 그래서 `Comparable`을 구현한 객체들의 배열은  
  아래처럼 손쉽게 정렬할 수 있다.

```java
Arrays.sort(a);
```

- 검색, 극단값 계산, 자동 정렬되는 컬렉션 관리도 역시 쉽게 할 수 있다.  
  예를 들어, 아래 프로그램은 명령줄 인수들을 중복은 제거하고 알파벳 순으로 정렬한다.  
  이는 `String`이 `Comparable`을 구현한 덕분이다.

```java
public class WordList {
    public static void main(String[] args) {
        Set<String> s = new TreeSet<>();
	Collections.addAll(s, args);
	System.out.println(s);
    }
}
```

- 누구나 `Comparable`을 구현하여 이 인터페이스를 활용하는 수많은 제네릭 알고리즘과  
  컬렉션의 힘을 누릴 수 있다. 작은 노력으로 큰 효과를 얻는 것이다.  
  사실상 Java 플랫폼 라이브러리의 모든 값 클래스와 열거 타입이 `Comparable`을 구현했다.  
  알파벳, 숫자, 연대 같이 순서가 명확한 값 클래스를 작성한다면 반드시 `Comparable`를 구현하자.

```java
public interface Comparable<T> {
    int compareTo(T t);
}
```

- `compareTo()` 메소드의 일반 규약은 `equals()`와 비슷하다.

> 이 객체와 주어진 객체의 순서를 비교한다. 이 객체가 주어진 객체보다 작으면 음의 정수를,  
> 같으면 0을, 크면 양의 정수를 반환한다. 이 객체와 비교할 수 없는 타입의 객체가 주어지면  
> `ClassCastException`을 던진다.
>
> 아래 설명에서 sgn(표현식) 표기는 수학에서 말하는 부호 함수를 뜻하며,  
> 표현식의 값이 음수, 0, 양수일 때 -1, 0, 1을 반환하도록 정의했다.
>
> - `Comparable`을 구현한 클래스는 모든 x, y에 대해  
>   `sgn(x.compareTo(y)) === -sgn(y.compareTo(x))` 이어야 한다.  
>   따라서 `x.compareTo(y)`는 `y.compareTo(x)`가 예외를 던질 때에 한해  
>   예외를 던져야 한다.
>
> - `Comparable`을 구현한 클래스는 추이성을 보장해야 한다.  
>   즉, `x.compareTo(y) > 0 && y.compareTo(z) > 0`이면 `x.compareTo(z) > 0`이다.
>
> - `Comparable`을 구현한 클래스는 모든 z에 대해 `x.compareTo(y) == 0`이면  
>   `sgn(x.compareTo(z)) == sgn(y.compareTo(z))`이다.
>
> * 이번 권고가 필수는 아니지만 꼭 지키는게 좋다.  
>   `(x.compareTo(y) == 0) == (x.equals(y))`여야 한다. `Comparable`을  
>   구현하고 이 권고를 지키지 않는 모든 클래스는 그 사실을 명시해야 한다.  
>   아래와 같이 명시하면 적당할 것이다.  
>   _"주의: 이 클래스의 순서는 equals()와 일관되지 않다._

- 모든 객체에 대해 전역 동치 관계를 부여하는 `equals()`와 달리, `compareTo()`는  
  타입이 다른 객체를 신경쓰지 않아도 된다. 타입이 다른 객체가 주어지면 간단히 `ClassCastException`을  
  던져도 되며, 대부분 그렇게 한다. 물론 이 규약에서는 다른 타입 사이의 비교도 허용하는데, 보통은  
  비교할 객체들이 구현한 공통 인터페이스를 매개로 이뤄진다.

- `hashCode()` 규약을 지키지 못하면 해시를 사용하는 클래스와 어울리지 못하듯,  
  `compareTo()` 규약을 지키지 못하면 비교를 활용하는 클래스와 어울리지 못한다.  
  비교를 활용하는 클래스의 예로는 정렬된 컬렉션인 `TreeSet`, `TreeMap`,  
  검색과 정렬 알고리즘을 활용하는 유틸리티 클래스인 `Collections`와 `Arrays`가 있다.

- `compareTo()`의 규약을 자세히 살펴보자. 첫 번째 규약은 두 객체 참조의 순서를 바꿔  
  비교해도 예상한 결과가 나와야 한다는 얘기다. 즉, x가 두 y보다 작으면 y가 x보다 커야한다.  
  x가 y가 크기가 같다면, y는 x와 같아야 한다. 마지막으로 x가 y보다 크면 y는 x보다 작아야 한다.  
  두 번째 규약은 x가 y보다 크고 y가 z보다 크면, x는 y보다 커야한다는 뜻이다.  
  마지막 규약은 크기가 같은 객체들끼리는 어떤 객체와 비교하더라도 항상 같아야 한다는 뜻이다.

- 이상의 세 개 규약은 `compareTo()`메소드로 수행하는 동치성 검사도 `equals()` 규약과  
  똑같이 반사성, 대칭성, 추이성을 충족해야 함을 의미한다. 그래서 주의사항도 똑같다.  
  기존 클래스를 확장한 구체 클래스에서 새로운 값 컴포넌트를 추가했다면 `compareTo()`규약을  
  지킬 방법이 없다. 객체 지향적 추상화의 이점을 포기할 생각이 아니라면 말이다.  
  우회법도 같다. `Comparable`을 구현한 클래스를 확장해 값을 추가하고 싶다면,  
  확장하는 대신 독립된 클래스를 만들고, 이 클래스에 원래 클래스의 인스턴스를 가리키는 필드를 두자.  
  그런 다음 내부 인스턴스를 반환하는 View 메소드를 제공하면 된다. 이렇게 하면 바깥 클래스에  
  우리가 원하는 `compareTo()`를 구현해넣을 수 있다. 클라이언트는 필요에 따라 바깥 클래스의  
  인스턴스를 필드 안에 담긴 원래 클래스의 인스턴스로 다룰 수도 있고 말이다.

- `compareTo()`의 마지막 규약은 필수는 아니지만 꼭 지키는 것이 좋다.  
  마지막 규약은 간단히 말하면 `compareTo()`로 수행한 동치성 결과가 `equals()`와  
  같아야 한다는 것이다. 이를 잘 지키면 `compareTo()`로 줄지은 순서와 `equals()`의  
  결과가 일관되게 된다. `compareTo()`의 순서와 `equals()`의 결과가 일관되지 않은  
  클래스도 여전히 동작은 한다. 단, 이 클래스의 객체를 정렬된 컬렉션에 넣으면 해당 컬렉션이  
  구현한 인터페이스(`Collection`, `Set`, `Map` 등)에 정의된 동작과 엇박자를 낼 것이다.  
  이 인터페이스는 `equals()`의 규약을 따른다고 되어 있지만, 놀랍게도 정렬된 컬렉션들은  
  동치성을 비교할 때 `equals()` 대신 `compareTo()`를 사용하기 때문이다.  
  아주 큰 문제는 아니지만, 주의해야 한다.

- `compareTo()`와 `equals()`가 일관되지 않은 `BigDecimal` 클래스를 예로 들어보자.  
  빈 `HashSet` 인스턴스를 생성한 다음 `new BigDecimal("1.0")`과 `new BigDecimal("1.00")`을  
  차례로 추가해보자. 이 두 `BigDecimal`은 `equals()`로 비교하면 서로 다르기 때문에  
  `HashSet`은 원소를 두 개 갖게 된다. 하지만 `HashSet` 대신 `TreeSet`을 사용하면  
  원소를 하나만 갖게 된다. `compareTo()`로 비교하면 두 `BigDecimal` 인스턴스가  
  똑같기 때문이다.

- `compareTo()` 메소드 작성 요령은 `equals()`와 비슷하다. 몇 가지 차이점만  
  주의하면 된다. `Comparable`은 타입을 인수로 받는 제네릭 인터페이스이므로 `compareTo()` 메소드의  
  인수 타입은 컴파일타임에 정해진다. 입력 인수의 타입을 확인하거나 형변환할 필요가 없다는 뜻이다.  
  인수의 타입이 잘못되었다면 컴파일 자체가 되지 않는다. 또한 null을 인수로 넣어 호출하면  
  NPE를 던져야 한다. 물론 실제로도 null인 멤버에 접근하는 순간 이 예외가 던져질 것이다.

- `compareTo()`메소드는 각 필드가 동치인지를 비교하는게 아니라, 그 순서를 비교한다.  
  객체 참조 필드를 비교하려면 `compareTo()`를 재귀적으로 호출한다. `Comparable`을  
  구현하지 않은 필드나 표준이 아닌 순서로 비교해야 한다면 `Comparator`를 대신 사용한다.  
  `Comparator`는 직접 만들거나 Java가 제공하는 것 중에 골라 쓰면 된다.  
  아래 코드는 `CaseInsensitiveString`을 위한 `compareTo()`메소드로, Java가 제공하는  
  Comparator(비교자)를 사용하고 있다.

```java
public final class CaseInsensitiveString implements Comparable<CaseInsensitiveString> {
    //..

    public int compareTo(CaseInsensitiveString cis) {
	return String.CASE_INSENSITIVE_ORDER.compare(s, cis.s);
    }
}
```

- `CaseInsensitiveString`이 `Comparable<CaseInsensitiveString>`을 구현한 것에  
  주목하자. `CaseInsensitiveString`의 참조는 `CaseInsensitiveString` 참조와만  
  비교할 수 있다는 뜻으로, `Comparable`을 구현할 때 일반적으로 따르는 패턴이다.

- 정수 기본 타입 필드를 비교할 때는 박싱된 기본 타입 클래스들에 새로 추가된  
  정적 메소드인 `compare()`를 이용하면 된다.  
  **`compareTo()`에서 관계 연산자 `<`와 `>`를 사용하는 방식은 거추장스럽고 오류를 유발하니,**  
  **사용하지 말자.**

- 클래스에 핵심 필드가 여러 개라면 어느 것을 먼저 비교하느냐가 중요해진다.  
  **가장 핵심적인 필드부터** 비교해 나가자. 비교 결과가 0이 아니라면, 즉 순서가 결정되면  
  거기서 끝이다. 그 결과를 곧장 반환하자. 가장 핵심이 되는 필드가 똑같다면, 똑같지 않은  
  필드를 찾을 때까지 그 다음으로 중요한 필드를 비교해나간다. 아래는 `PhoneNumber`의 `compareTo()`를  
  이 방식으로 구현한 모습이다.

```java
public int compareTo(PhoneNumber pn) {
    int result = Short.compare(areaCode, pn.areaCode);  // 가장 중요한 필드
    if (result == 0) {
	result = Short.compare(prefix, pn.prefix);  // 두 번째로 중요한 필드
	if result == 0)
	    result = Short.compare(lineNum, pn.lineNum);  // 세 번째로 중요한 필드
    }
    return result;
}
```

- Java8에서는 `Comparator` 인터페이스가 일련의 비교자 생성 메소드와 팀을 꾸려  
  메소드 체이닝 방식으로 비교자를 생성할 수 있게 되었다. 그리고 이 비교자들을 `Comparable`이  
  원하는 `compareTo()`를 구현하는 데 멋지게 활용할 수 있다. 많은 개발자가 이 방식에  
  매혹되지만, 약간의 성능 저하가 뒤따른다. 참고로 Java의 정적 임포트 기능을 활용하면  
  정적 비교자 생성 메소드들을 그 이름만으로 사용할 수 있어 코드가 훨씬 깔끔해진다.  
  아래는 `PhoneNumber`의 `compareTo()`를 이 방식으로 구현한 모습이다.

```java
private static final Comparator<PhoneNumber> COMPARATOR =
    comparingInt((PhoneNumber pn) -> pn.areaCode)
        .thenComparingInt((pn -> pn.prefix)
	.thenComparingInt((pn -> pn.lineNum));

public int compareTo(PhoneNumber pn) {
    return COMPARATOR.compare(this, pn);
}
```

- 위 코드는 클래스를 초기화할 때 비교자 생성 메소드 2개를 이용해 비교자를 생성한다.  
  그 첫번째인 `comparingInt()`는 객체 참조를 int 타입 키에 매핑하는 키 추출 함수를  
  인수로 받아, 그 키를 기준으로 순서를 정하는 비교자를 반환하는 정적 메소드다.  
  위 코드에서 `comparingInt()`는 람다를 인수로 받으며, 이 람다는 `PhoneNumber`에서 areaCode를  
  추출한 후 이를 기준으로 순서를 정하는 `Comparator<PhoneNumber>`를 반환한다.  
  이 람다에서 입력 인수의 타입 `(PhoneNumber pn)`을 명시한 점에 주목하자.  
  Java의 타입 추론 능력이 이 상황에서 타입을 알아낼 만큼 강력하지 않기 때문에 프로그램이 컴파일되도록  
  우리가 도와준 것이다.

- 두 전화번호의 areaCode가 같을 수 있으니, 비교 방식을 더듬어야 한다.  
  이 일은 두 번째 비교자 생성 메소드인 `thenComparingInt()`가 수행한다.  
  `thenComparingInt()`는 `Comparator`의 인스턴스 메소드로, int 키 추출자 함수를 입력받아  
  다시 비교자를 반환한다. (이 비교자는 첫 번째 비교자를 적용한 다음 새로 추출한 키로 추가 비교를 수행한다.)  
  `thenComparingInt()`는 원하는 만큼 연달아 호출할 수 있다. 앞의 예에서는 2개를 연달아 호출했으며,  
  그 중 첫번째의 키로는 prefix를, 두 번째의 키로는 lineNum을 사용했다.  
  하지만 이번에는 타입을 명시하지 않았다. Java의 타입 추론 능력이 이 정도는 추론해낼 수 있기 때문이다.

- `Comparator`는 수많은 보조 생성 메소드들로 중무장하고 있다. long과 double 용으로는  
  `comparingInt()`와 `thenComparingInt()`의 변형 메소드를 준비했다.  
  short처럼 더 작은 정수 타입에는 위 예제처럼 int용 버전을 사용하면 된다. 마찬가지로 float는  
  double용을 이용해 수행한다. 이런 식으로 Java의 숫자용 기본 타입을 모두 커버한다.

- 객체 참조용 비교자 생성 메소드도 준비되어 있다. 우선, `comparing()`이라는 정적 메소드 2개가  
  다중정의되어 있다. 첫 번째는 키 추출자를 받아서 그 키의 자연적 순서를 사용한다. 두 번째는  
  키 추출자 하나와 추출된 키를 비교할 비교자까지 총 2개의 인수를 받는다. 또한, `thenComparing()`이라는  
  인스턴스 메소드가 3개 다중정의되어 있다. 첫 번째는 비교자 하나만 인수로 받아 그 비교자로 순서를 정한다.  
  두 번째는 키 추출자를 인수로 받아 그 키의 자연적 순서로 보조 순서를 정한다. 마지막 세 번째는  
  키 추출자 하나와 추출된 키를 비교할 비교자까지 총 2개의 인수를 받는다.

- _값의 차이_ 를 기준으로 첫 번째 값이 두 번째 값보다 작으면 음수를, 같으면 0을, 크면 양수를  
  반환하는 `compareTo()`나 `compare()` 메소드를 자주 만날 수 있다.

```java
static Comparator<Object> hashCodeOrder = new Comparator<>() {
    public int compare(Object o1, Object o2) {
	return o1.hashCode() - o2.hashCode();
    }
}
```

- 위 방식은 사용하면 안된다. 위 방식은 정수 오버플로우를 일으키거나 부동 소수점 계산 방식에 따른  
  오류를 낼 수 있다. 그렇다고 이번 아이템에서 설명한 방법대로 구현한 코드보다 월등히 빠르지도  
  않을 것이다. 그 대신 아래의 두 방식 중 하나를 사용하자.

```java
// 정적 compare() 메소드를 활용한 비교자
static Comparator<Object> hashCodeOrder = new Comparator<>() {
    public int compare(Object o1, Object o2) {
	return Integer.compare(o1.hashCode(), o2.hashCode());
    }
};

// 비교자 생성 메소드를 활용한 비교자
static Comparator<Object> hashCodeOrder =
    Comparator.comparingInt(o -> o.hashCode());
```

<hr/>
