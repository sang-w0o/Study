# 이왕이면 제네릭 메소드로 만들라

- 클래스와 마찬가지로, 메소드도 제네릭으로 만들 수 있다. 매개변수화 타입을 받는 정적  
  유틸리티 클래스들의 메소드는 보통 제네릭이다. 예컨데 `Collections`의 _'알고리즘 메소드'_ 인  
  `binarySearch()`, `sort()` 등은 모두 제네릭이다.

- 제네릭 메소드 작성법은 제네릭 타입 작성법과 비슷하다.  
  아래는 두 집합의 합집합을 반환하는, 문제가 있는 메소드다.

```java
public static Set union(Set s1, Set s2) {
    Set result = new HashSet(s1); // (1)
    result.addAll(s2); // (2)
    return result;
}
```

- 하지만 위 코드는 (1), (2) 모두에서 경고가 발생한다.  
  경고를 없애려면 이 메소드를 타입 안전하게 만들어야 한다. 즉, 메소드 선언에서의 세 집합  
  (입력 2, 반환 1)의 원소 타입을 타입 매개변수로 명시하고, 메소드 안에서도 이 타입  
  매개변수만 사용하게 수정하면 된다. **타입 매개변수들을 선언하는 타입 매개변수 목록은**  
  **제한자와 반환 타입 사이에 온다.** 다음 코드에서 타입 매개변수 목록은 `<E>`이고,  
  **반환 타입은 `Set<E>`이다.** 타입 매개변수의 명명 규칙은 제네릭 메소드나  
  제네릭 타입이나 똑같다.

```java
public static <E> Set<E> union(Set<E> s1, Set<E> s2) {
    Set<E> result = new HashSet<E>(s1);
    result.addAll(s2);
    return result;
}
```

- 단순한 제네릭 메소드라면 이 정도면 충분하다. 이 메소드는 경고 없이 컴파일되며 타입 안전하고,  
  쓰기도 쉽다. 다음은 이 메소드를 활용하는 간단한 프로그램이다. 직접 형변환하지 않아도  
  어떤 오류나 경고 없이 컴파일된다.

```java
public static void main(String[] args) {
    Set<String> guys = Set.of("Tom", "Dick", "Harry");
    Set<String> stooges = Set.of("Larry", "Moe", "Curly");
    Set<String> alfCio = union(guys, stooges);
    System.out.println(alfCio);
    // {Tom, Dick, Harry, Larry, Moe, Curly}
}
```

- 위 코드의 `union()` 메소드는 집합 3개(입력 2개, 반환 1개)의 타입이 모두  
  같아야 한다. 이를 한정적 와일드카드 타입을 사용하여 더 유연하게 개선할 수 있다.

- 때때로 불변 객체를 여러 타입으로 활용할 수 있게 만들어야 할 때가 있다. 제네릭은 런타임에  
  타입 정보가 소거되므로 하나의 객체를 어떤 타입으로든 매개변수화할 수 있다.  
  하지만 이렇게 하려면 요청한 타입 매개변수에 맞게 매번 그 객체의 타입을 바꿔주는  
  정적 팩토리를 만들어야 한다. 이 패턴을 제네릭 싱글턴 패턴이라 하며,  
  `Collections.reverseOrder()` 같은 함수 객체나 `Collections.emptySet()`  
  같은 컬렉션용으로 사용한다.

- 이번에는 항등함수(Identity Function)를 담은 클래스를 만들고 싶다 해보자.  
  Java 라이브러리의 `Function.identity`를 사용하면 되지만, 공부를 위해서 직접 한번  
  작성해보자. 항등함수 객체는 상태가 없으니 요청할 때마다 새로 생성하는 것은 낭비다.  
  Java의 제네릭이 실체화된다면 항등함수를 타입별로 하나씩 만들어야 했겠지만 소거 방식을  
  사용한 덕에 제네릭 싱글턴 하나로 충분하다. 예시를 보자.

```java
private static UnaryOperator<Object> IDENTITY_FN = (t) -> t;

@SuppressWarnings("unchecked")
public static <T> UnaryOperator<T> identityFunction() {
    return (UnaryOperator<T>) IDENTITY_FN;
}
```

- `IDENTITY_FN`을 `UnaryOperator<T>`로 형변환하면, 비검사 형변환 경고가 발생한다.  
  `T`가 어떤 타입이든 `UnaryOperator<Object>`는 `UnaryOperator<T>`가 아니기 때문이다.  
  하지만 항등함수란 입력 값을 수정 없이 그대로 반환하는 특별한 함수이므로, `T`가 어떤 타입이든  
  `UnaryOperator<T>`를 사용해도 타입 안전하다. 우리는 이 사실을 알고 있으니, 이 메소드가  
  내보내는 비검사 형변환 경고는 숨겨도 안심할 수 있다.

- 다음 코드는 위 코드의 제네릭 싱글턴을 `UnaryOperator<String>`과 `UnaryOperator<Number>`로  
  사용하는 모습이다. 지금까지와 마찬가지로 형변환을 하지 않아도 컴파일 오류나 경고가 발생하지 않는다.

```java
public static void main(String[] args) {

    //..

    String[] strings = {"a", "b", "c"};
    UnaryOperator<String>  sameString = identityFunction();
    for(String s: strings) {
	System.out.println(sameString.apply(s));
    }

    Number[] numbers = {1, 2.0, 3L};
    UnaryOperator<Number> sameNumber = identityFunction();
    for(Number n: numbers) {
	System.out.println(sameNumber.apply(n));
    }
}
```

- 상대적으로 드물기는 하지만, 자기 자신이 들어간 표현식을 사용하여 타입 매개변수의  
  허용 범위를 줄일 수 있다. 바로 **재귀적 타입 한정(recursive type bound)** 이다.  
  재귀적 타입 한정은 주로 타입의 자연적 순서를 정해야 하는 `Comparable`와 함께 쓰인다.  
  예시를 보자.

```java

public interface Comparable<T> {
    int compareTo(T o);
}
```

- 위 코드에서 타입 매개변수 `T`는 `Comparable<T>`를 구현한 타입이 비교할 수 있는 원소의  
  타입을 정의한다. 실제로 거의 모든 타입은 자신과 같은 타입의 원소와만 비교할 수 있다.  
  따라서 `String`은 `Comparable<String>`을 구현하고 `Integer`는  
  `Comparable<Integer>`를 구현하는 식이다.

- `Comparable`을 구현한 원소의 컬렉션을 입력받는 메소드들은 주로 그 원소들을 정렬 혹은  
  검색하거나, 최소값이나 최대값을 구하는 식으로 사용한다. 이 기능을 수행하려면 컬렉션 안에  
  담긴 모든 원소가 상호 비교될 수 있어야 한다. 아래는 이 제약 사항을 코드로 표현한 모습이다.

```java
public static <E extends Comparable<E>> E max(Collection<E> c);
```

- 타입 한정인 `<E extends Comparable<E>>`라는 것은 _'모든 타입 `E`는 자신과 비교할 수 있다.'_ 고  
  읽을 수 있다. 상호 비교 가능하단 뜻을 아주 정확하게 표현할 수 있다.

- 아래는 방금 선언한 메소드의 구현이다. 컬렉션에 담긴 원소의 자연적 순서를 기준으로  
  최대값을 계산하며, 컴파일 오류나 경고 메시지는 발생하지 않는다.

```java
public static <E extends Comparable<E>> E max(Collection<E> c) {
    if(c.isEmpty())
        throw new IllegalArgumentException("empty collection");
    E result = null;
    for(E e: c) {
	if(result == null || e.compareTo(result) > 0)
	    result = Objects.requireNonNull(e);
    }
}
```

> 위 메소드에 빈 컬렉션을 건네면 `IllegalArgumentException`이 발생하기에,  
> `Optional<E>`를 반환하도록 고치는 편이 더 나을 것이다.

- 재귀적 타입 한정은 훨씬 더 복잡해질 가능성이 있긴 하지만, 다행이 그런 일은 잘  
  일어나지 않는다. 이번 아이템에서 설명한 관용구, 여기에 와일드카드를 사용한 변형,  
  그리고 시뮬레이트한 셀프 타입 관용구를 이해하고 나면 실전에서 마주치는 대부분의  
  재귀적 타입 한정을 무리 없이 다룰 수 있을 것이다.

<hr/>

## 핵심 정리

- 제네릭 타입과 마찬가지로, 클라이언트에서 입력 매개변수와 반환값을 명시적으로 형변환해야 하는  
  메소드보다, 제네릭 메소드가 더 안전하며 사용하기도 쉽다. 타입과 마찬가지로, 메소드도  
  형변환 없이 사용할 수 있는 편이 좋으며, 많은 경우 그렇게 하려면 제네릭 메소드가 되어야 한다.  
  역시 타입과 마찬가지로, 형변환을 해줘야 하는 기존 메소드는 제네릭하게 만들자.  
  기존 클라이언트는 그대로 둔 채 새로운 사용자의 삶을 훨씬 편하게 만들어줄 것이다.

<hr/>
