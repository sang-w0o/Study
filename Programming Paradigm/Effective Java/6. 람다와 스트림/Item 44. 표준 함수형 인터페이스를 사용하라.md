# 표준 함수형 인터페이스를 사용하라

- Java가 람다를 지원하기 시작하면서 API를 작성하는 모범 사례도 크게 바뀌었다.  
  예를 들어, 상위 클래스의 기본 메소드를 재정의해 원하는 동작을 구현하는 템플릿 메소드 패턴의 매력이  
  크게 줄었다. 이를 대체하는 현대적인 해법은 같은 효과의 함수 객체를 받는 정적 팩토리나 생성자를  
  제공하는 것이다. 이 내용을 일반화해서 말하면 함수 객체를 매개변수로 받는 생성자와 메소드를 더 많이  
  만들어야 한다. 이때 함수형 매개변수의 타입을 올바르게 선택해야 한다.

- `LinkedHashMap`을 생각해보자. 이 클래스의 protected 메소드인 `removeEldestEntry()`를 재정의하면  
  캐시로 사용할 수 있다. Map에 새로운 key를 추가하는 `put()` 메소드는 이 메소드를 호출하여 true가  
  반환되면 Map에서 가장 오래된 원소를 제거한다. 예를 들어, `removeEldestEntry()`를 아래처럼 재정의하면  
  Map에 원소가 100개가 될 때까지 커지다가, 그 이상이 되q면 새로운 key가 더해질 때마다 가장 오래된 원소를  
  하나씩 제고한다. 즉, 가장 최근 원소 100개를 유지한다.

```java
public class LinkedHashMapChild extends LinkedHashMap {
    //..
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
	return size() > 100;
    }
}
```

- 위 코드대로도 잘 동작하지만, 람다를 사용하면 훨씬 잘 해낼 수 있다. `LinkedHashMap`을 오늘날 다시 구현한다면  
  함수 객체를 받는 정적 팩토리나 생성자를 제공했을 것이다. `removeEldestEntry()`의 선언을 보면, 이 함수 객체는  
  `Map.Entry<K, V>`를 받아 boolean을 반환해야 할 것 같지만, 꼭 그렇지는 않다. `removeEldestEntry()`는  
  `size()`를 호출해 Map 안의 원소 개수를 알아내는데, 이는 `removeEldestEntry()`가 인스턴스 메소드라 가능하다.  
  하지만 생성자에 넘기는 함수 객체는 이 Map의 인스턴스 메소드가 아니다. 팩토리나 생성자를 호출할 때는 Map의  
  인스턴스가 존재하지 않기 때문이다. 따라서 Map은 자기 자신도 함수 객체에 건네줘야 한다. 이를 반영한 함수형 인터페이스는  
  아래처럼 선언할 수 있다.

```java
@FunctionalInterface
interface EldestEntryRemovalFunction<K, V> {
    boolean remove(Map<K, V> map, Map.Entry<K, V> eldest)l
}
```

- 이 인터페이스도 잘 동작하긴 하지만, 굳이 사용할 이유는 없다. Java 표준 라이브러리에 이미 같은 모양의  
  인터페이스가 준비되어 있기 때문이다. `java.util.function` 패키지를 보면 다양한 용도의 표준 함수형  
  인터페이스가 담겨 있다. **필요한 용도에 맞는 게 있다면, 직접 구현하지 말고 표준 함수형 인터페이스를 사용하자.**  
  그러면 API가 다루는 개념의 수가 줄어들어 익히기 더 쉬워진다. 또한 표준 함수형 인터페이스들은 유용한 default  
  메소드를 많이 제공하므로, 다른 코드와의 상호 운용성도 크게 좋아질 것이다. 예를 들어, `Predicate` 인터페이스는  
  predicate들을 조합하는 메소드를 제공한다. 앞의 `LinkedHashMap` 예시에서는 직접 만든  
  `EldestEntryRemovalFunction` 대신 표준 인터페이스인 `BiPredicate<Map<K, V>, Map.Entry<K, V>>`를  
  사용할 수 있다.

- `java.util.function` 패키지에는 총 43개의 인터페이스가 담겨 있다. 전부 기억하기는 어렵겠지만, 기본 인터페이스  
  6개만 기억한다면 나머지는 충분히 유추해낼 수 있다. 이 기본 인터페이스들은 모두 참조 타입용이다. 하나씩 보자.

- `Operator` 인터페이스는 인수가 1개인 `UnaryOperator`와 2개인 `BinaryOperator`로 나뉘며,  
  반환값과 인수의 타입이 같은 함수를 뜻한다. `Predicate` 인터페이스는 인수 하나를 받아 boolean을 반환하는  
  함수를 뜻하며, `Function` 인터페이스는 인수와 반환타입이 다른 함수를 뜻한다. `Supplier` 인터페이스는  
  인수를 받지 않고 값을 반환하는 함수를, `Consumer` 인터페이스는 인수를 하나 받고 반환값은 없는  
  함수를 뜻한다. 아래는 이 기본 함수형 인터페이스들을 정리한 표다.

| 인터페이스          | 함수 시그니처         | 예시                  |
| :------------------ | :-------------------- | :-------------------- |
| `UnaryOperator<T>`  | `T apply(T t)`        | `String::toLowerCase` |
| `BinaryOperator<T>` | `T apply(T t1, T t2)` | `BigInteger::add`     |
| `Predicate<T>`      | `boolean test(T t)`   | `Collection::isEmpty` |
| `Function<T, R>`    | `R apply(T t)`        | `Arrays::asLit`       |
| `Supplier<T>`       | `T get()`             | `Instant::now`        |
| `Consumer<T>`       | `void accept(T t)`    | `System.out::println` |

- 기본 인터페이스는 기본 타입인 int, long, double 용으로 각 3개씩 변형이 생겨난다.  
  그 이름도 기본 인터페이스의 이름 앞에 해당 기본 타입명을 붙여 지었다. 예를 들어 int를 받는  
  `Predicate`는 `IntPredicate`가 되고, long을 받아 반환하는 `BinaryOperator`는  
  `LongBinaryOperator`가 되는 식이다. 이 변형들 중 유일하게 `Function`의 변형만  
  매개변수화 됐다. 정확히는 반환 타입만 매개변수화됐는데, 예를 들어 `LongFunction<int[]>`는  
  long 인수를 받아 `int[]`를 반환한다.

- `Function` 인터페이스에는 기본 타입을 반환하는 변형이 총 9개가 더 있다.  
  인수와 같은 타입을 반환하는 함수는 `UnaryOperator`이므로, `Function` 인터페이스의 변형은  
  입력과 결과의 타입이 항상 다르다. 입력과 결과 타입이 모두 기본 타입이면 접두어로 `SrcToResult`를  
  사용한다. 예를 들어, long을 받아 int를 반환하면 `LongtoIntFunction`이 되는 식이다.(총 6개)  
  나머지는 입력이 객체참조이고 결과가 int, long, double인 변형들로, 앞서와 달리 입력을 매개변수화하고  
  접두어로 `ToResult`를 사용한다. 즉, `ToLongFunction<int[]>`는 `int[]` 인수를 받아 long을  
  반환한다.(총 3개)

- 기본 함수형 인터페이스 중 3개에는 인수를 2개씩 받는 변형이 있다. 그 주인공은 `BiPredicate<T, U>`,  
  `BiFunction<T, U, R>`, `BiConsumer<T, U>`이다. `BiFunction`에는 다시 기본 타입을 반환하는  
  3개의 변형 `ToIntBiFunction<T, U>`, `ToLongBiFunction<T, U>`, `ToDoubleBiFunction<T, U>`가  
  존재한다. `Consumer`에도 객체 참조와 기본 타입 하나, 즉 인수를 2개 받는 변형인 `ObjDoubleConsumer<T>`,  
  `ObjIntConsumer<T>`, `ObjLongConsumer<T>`가 존재한다. 이렇게 해서 기본 인터페이스의 인수 2개짜리  
  변형은 총 9개다.

- 마지막으로 `BooleanSupplier` 인터페이스는 boolean을 반환하도록 한 `Supplier`의 변형이다.  
  이것이 표준 함수형 인터페이스 중 boolean을 이름에 명시한 유일한 인터페이스이지만, `Predicate`와  
  그 변형 4개도 boolean 값을 반환할 수 있다. 앞서 본 42개의 인터페이스에 이 `BooleanSupplier`까지  
  더해서 표준 함수형 인터페이스는 총 43개다.

- 표준 함수형 인터페이스 대부분은 기본 타입만 지원한다. 그렇다고 **기본 함수형 인터페이스에 박싱된 기본 타입을**  
  **넣어 사용하지는 말자.** 동작은 하지만 _"박싱된 기본 타입 대신 기본 타입을 사용하라"_ 는 Item 61의  
  조언을 위배한다. 특히 계산량이 많을 때는 성능이 처참히 느려질 수 있다.

- 이제 대부분의 상황에서는 직접 작성하는 것보다 표준 함수형 인터페이스를 사용하는 편이 나음을 알았을 것이다.  
  그렇다면 코드를 직접 작성해야 할 때는 언제일까? 물론 표준 함수형 인터페이스 중 필요한 용도에 맞는게 없다면  
  직접 작성해야 한다. 예를 들어, 매개변수 3개를 받는 `Predicate`라든가, 검사 예외를 던지는 경우가  
  있을 수 있다. 그런데 구조적으로 똑같은 함수형 인터페이스가 있더라도 직접 작성해야만 할 때가 있다.

- 자주 보아온 `Comparator<T>` 인터페이스를 떠올려보자. 구조적으로는 `ToIntBiFunction<T, U>`와 동일하다.  
  심지어 Java 라이브러리에 `Comparator<T>`를 추가할 당시 `ToIntBiFunction<T, U>`가 이미 존재했더라도  
  `ToIntBiFunction<T, U>`를 사용하면 안됐다. `Comparator`가 독자적인 인터페이스로 살아남아야 하는 이유가  
  몇 가지 있다.

  - 첫째, API에서 굉장히 자주 사용되는데, 지금의 이름이 그 용도를 아주 훌륭히 설명해준다.
  - 둘째, 구현하는 쪽에서 반드시 지켜야 하는 규약을 담고 있다.
  - 셋째, 비교자들을 변환하고 조합해주는 유용한 default 메소드들을 많이 담고 있다.

- 이상의 `Comparator` 특징을 정리하면 아래의 세 가지인데, 이 중 하나 이상을 만족한다면 전용 함수형  
  인터페이스를 구현해야하는 것은 아닌지 진중히 고민해야 한다.

  - 자주 쓰이며, 이름 자체가 용도를 명확히 설명해준다.
  - 반드시 따라야 하는 규약이 있다.
  - 유용한 default 메소드를 제공할 수 있다.

- 전용 함수형 인터페이스를 작성하기로 했다면, 자신이 작성하는 것이 다른 것도 아닌 _'인터페이스'_ 임을 명심해야 한다.  
  아주 주의해서 설계해야 한다는 뜻이다.

- 이전에 본 `EldestEntryRemovalFunction` 함수형 인터페이스에 `@FunctionalInterface` 어노테이션이 달려  
  있음에 주목하자. 이 어노테이션을 사용하는 이유는 `@Override`를 사용하는 이유와 비슷하다. 프로그래머의 의도를  
  명시하는 것으로, 크게 세 가지 목적이 있다. 첫째, 해당 클래스의 코드나 설명 문서를 읽을 이에게 그 인터페이스가  
  람다용으로 설계된 것임을 알려준다. 둘째, 해당 인터페이스가 추상 메소드를 오직 하나만 가지고 있어야 컴파일되게 해준다.  
  셋째, 그 결과 유지보수 과정에서 누군가 실수로 메소드를 추가하지 못하게 막아준다.  
  그러니 **직접 만든 함수형 인터페이스에는 항상 `@FunctionalInterface`를 사용하자.**

- 마지막으로, 함수형 인터페이스를 API에서 사용할 때의 주의점을 살펴보자.  
  서로 다른 함수형 인터페이스를 같은 위치의 인수로 받는 메소드들을 다중 정의해서는 안된다.  
  이는 클라이언트에게 불필요한 모호함만 안겨줄 뿐이며, 이 모호함으로 인해 실제로 문제가 일어나기도 한다.  
  `ExecutorService`의 `submit()` 메소드는 `Callable<T>`를 받는 것과 `Runnable`을 받는 것을  
  다중정의했다. 그래서 올바른 메소드를 알려주기 위해 형변환해야 할 때가 종종 생긴다. 이런 문제를 피하는 가장 쉬운  
  방법은 서로 다른 함수형 인터페이스를 같은 위치의 인수로 사용하는 다중 정의를 피하는 것이다.

<hr/>

## 핵심 정리

- Java가 람다를 지원한다는 뜻은 프로그래머가 이제는 API를 설계할 때 람다도 염두해야 한다는 뜻이다.  
  입력값과 반환값에 함수형 인터페이스 타입을 활용하자. 보통은 `java.util.function` 패키지의  
  표준 함수형 인터페이스를 사용하는 것이 가장 좋은 선택이다. 단, 흔하지는 않지만 직접 새로운 함수형  
  인터페이스를 만들어 쓰는 편이 나을 수도 있음을 잊지 말자.

<hr/>
