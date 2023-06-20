# 배열 보다는 리스트를 사용하라

- 배열과 제네릭 타입에는 중요한 차이가 두 가지 있다. 첫 번째, **배열은 공변이다.**  
  어려워 보이는 단어지만 뜻은 간단하다. `Sub`가 `Super`의 하위 타입이라면 배열  
  `Sub[]`는 배열 `Super[]`의 하위 타입이 된다.(공변: 함께 변한다.)  
  반면, **제네릭은 불공변이다.** 즉, 서로 다른 타입 `Type1`과 `Type2`가 있을 때,  
  `List<Type1>`은 `List<Type2>`의 상위 타입도, 하위 타입도 아니다.  
  이것만 보면 제네릭에 문제가 있다 생각할 수도 있지만, 사실 문제가 있는건 배열 쪽이다.  
  아래는 문법상 허용되는 코드다.

```java
Object[] objectArray = new Long[1];
objectArray[0] = "타입이 달라 넣을 수 없다.";  // @throws ArrayStoreException
```

- 하지만 아래 코드는 문법상 허용되지 않는다.

```java
List<Object> ol = new ArrayList<Long>();  // 호환되지 않는 타입
ol.add("타입이 달라 넣을 수 없다.");
```

- 어느 쪽이든 `Long`용 저장소에 `String`을 넣을 수는 없다. 다만 배열에서는 그 실수를  
  런타임에서야 알게 되지만, 리스트를 사용하면 컴파일할 때 바로 알 수 있다.

- 두 번째 주요 차이로, **배열은 실체화(reify) 된다.** 즉, 배열은 런타임에도 자신이 담기로한  
  원소의 타입을 인지하고 확인한다. 그래서 위에서 배열을 쓰는 코드에서 `Long` 배열에 `String`을  
  넣으려 하면 `ArrayStoreException`이 발생한다. 반면, **제네릭은 타입 정보가 런타임에 소거(erasure)된다.**  
  원소 타입을 컴파일 타임에만 검사하며, 런타임에는 알 수조차 없다는 뜻이다. 소거는 제네릭이  
  지원되기 전의 레거시 코드와 제네릭 타입을 함께 사용할 수 있게 해주는 메커니즘으로, Java5가  
  제네릭으로 순조롭게 전환될 수 있도록 해주었다.

- 이상의 주요 차이로 인해 배열과 제네릭은 잘 어우러지지 못한다. 예를 들어 배열은 제네릭 타입,  
  매개변수화 타입, 타입 매개변수로 사용할 수 없다. 즉, 코드를 `new List<E>[]`,  
  `new List<String>[]`, `new E[]` 이런 식으로 작성하면 컴파일할 때 제네릭 배열  
  생성 오류를 일으킨다.

- 제네릭 배열을 만들지 못하게 막은 이유는 무엇일까? 타입 안전하지 않기 때문이다.  
  이를 허용한다면 컴파일러가 자동 생성한 형변환 코드에서 런타임에 `ClassCastException`이  
  발생할 수 있다. 런타임에 `ClassCastException`이 발생하는 일을 막아주겠다는 제네릭 타입  
  시스템의 취지에 어긋나는 것이다.

- 아래 코드로 구체적인 상황을 살펴보자.

```java
List<String>[] stringLists = new List<String>[1]; // (1)
List<Integer> intList = List.of(42);  // (2)
Object[] objects = stringLists; // (3)
objects[0] = intList; // (4)
String s = stringLists[0].get(0); // (5)
```

- 제네릭 배열을 생성하는 (1) 이 허용된다고 해보자. (2)는 원소가 하나인 `List<Integer>`를  
  생성한다. (3)은 (1)에서 생성한 `List<String>`을 `Object` 배열에 할당한다.  
  배열은 공변이니 아무 문제 없다. (4)는 (2)에서 생성한 `List<Integer>`의 인스턴스를  
  `Object` 배열의 첫 번째 원소로 넣는다. 제네릭은 소거 방식으로 구현되어 있어서 이 역시  
  성공한다. 즉, 런타임에는 `List<Integer>` 인스턴스의 타입은 단순히 `List`가 되고,  
  `List<Integer>[]` 인스턴스의 타입은 `List[]`가 된다. 따라서 (4)번 과정에서도  
  `ArrayStoreException`을 일으키지 않는다. 이제부터가 문제다.

- `List<String>`만 담겠다고 선언한 stringLists 배열에는 지금 `List<Integer>`의  
  인스턴스가 저장되어 있다. 그리고 (5)는 이 배열의 첫 리스트에서 첫 번째 원소를 꺼내려 한다.  
  컴파일러는 꺼낸 원소를 자동으로 `String`으로 형변환하는데, 이 원소는 `Integer`이므로  
  런타임에 `ClassCastException`이 발생한다. 이런 일을 방지하려면 제네릭 배열이 생성되지  
  않도록 (1)에서 컴파일 오류를 내야 한다.

- `E`, `List<E>`, `List<String>`과 같은 타입을 실체화 불가 타입(non-reifiable type)라고 한다.  
  쉽게 말해, 실체화되지 않아서 런타임에는 컴파일타임보다 타입 정보를 적게 가지는 타입이다. 소거 메커니즘  
  때문에 매개변수화 타입 가운데 실체화될 수 있는 타입은 `List<?>`, `Map<?, ?>`와 같은  
  비한정적 와일드카드 타입 뿐이다. 배열을 비한정적 와일드카드 타입으로 만들 수는 있지만,  
  유용하게 쓰일 일은 거의 없다.

- 배열을 제네릭으로 만들 수 없어 귀찮을 때도 있다. 예를 들어, 제네릭 컬렉션에서는 자신의 원소 타입을  
  담은 배열을 반환하는게 보통은 불가능하다. 또한 제네릭 타입과 가변인수 메소드(varargs method)를  
  함께 쓰면 해석하기 어려운 경고 메시지를 받게 된다. 가변인수 메소드를 호출할 때마다 가변인수 매개변수를  
  담을 배열이 하나 만들어지는데, 이때 그 배열의 원소가 실체화 불가 타입이라면 경고가 발생하는 것이다.  
  이 문제는 `@SafeVarargs` 어노테이션으로 대처할 수 있다.

- 배열로 형변환할 때 제네릭 배열 생성 오류나 비검사 형변환 오류가 뜨는 경우, 대부분은 배열인 `E[]`  
  대신 컬렉션인 `List<E>`를 사용하면 해결된다. 코드가 조금 복잡해지고 성능이 살짝 나빠질 수  
  있지만, 그 대신 타입 안정성과 상호운용성은 좋아진다.

- 생성자에게서 컬렉션을 받는 `Chooser` 클래스를 살펴보자. 이 클래스는 컬렉션 안의 원소 중  
  하나를 무작위로 선택해 반환하는 `choose()` 메소드를 제공한다. 생성자에 어떤 컬렉션을  
  넘기느냐에 따라 이 클래스를 다양하게 사용할 수 있다. 아래는 제네릭을 사용하지 않고 구현한  
  가장 간단한 버전이다.

```java
public class Chooser {
  private final Object[] choiceArray;

  public Chooser(Collection choices) {
    this.choiceArray = choices.toArray();
  }

  public Object choose() {
    Random random = ThreadLocalRandom.current();
    return choiceArray[random.nextInt(choiceArray.length)];
  }
}
```

- 위 클래스를 사용하려면 `choose()` 메소드를 호출할 때마다 반환된 `Object`를 원하는  
  타입으로 형변환해야 한다. 혹시나 타입이 다른 원소가 들어 있었다면, 런타임에 형변환 오류가  
  날 것이다. 이 클래스를 제네릭으로 구현해보자.

```java
public class Chooser<T> {
  private final T[] choiceArray;
  public Chooser(Collection<T> choices) {
    this.choiceArray = choices.toArray();
  }

  // choose()
}
```

- 위 클래스를 컴파일하면 아래의 오류가 나온다.

```
error: incompatible types: Object[] cannot be converted to T[]
choiceArray = choices.toArray();
                             ^
where T is type-variable:
    T extends Object declared in class Chooser
```

- 오류가 말하는 대로 `Object[]` 배열을 `T[]`로 형변환해주면 된다.

```java
choiceArray = (T[]) choices.toArray();
```

- 그런데 이번에는 경고가 뜬다.

```
warning: [unchecked] unchecked cast
choiceArray = (T[]) choices.toArray();
                                   ^
required: T[], found: Object[]
where T is type-variable:
T extends Object declared in class Chooser
```

- `T`가 무슨 타입인지 알 수 없으나, 컴파일러는 이 형변환이 런타임에도 안전한지 보장할 수  
  없다는 메시지다. **제네릭에서는 원소의 타입 정보가 소거되어 런타임에서는 무슨 타입인지 알 수**  
  **없음을 기억하자!** 그렇다면 이 프로그램은 동작할까? 동작한다. 단지 컴파일러가 안전함을  
  보장하지 못할 뿐이다. 코드를 작성하는 사람이 안전하다 확신한다면 주석을 남기고 어노테이션을 달아  
  경고를 숨겨도 된다. 하지만 애초에 경고의 원인을 제거하는 편이 훨씬 낫다.

- 비검사 형변환 경고를 제거하려면 배열 대신 리스트를 쓰면 된다.  
  다음 `Chooser`는 오류나 경고 없이 컴파일된다.

```java
public class Chooser<T> {
  private final List<T> choiceList;
  public Chooser(Collection<T> choices) {
    this.choiceList = new ArrayList<>(choices);
  }
  public T choose() {
    Random random = ThreadLocalRandom.current();
    return choiceList.get(random.nextInt(choiceList.size()));
  }
}
```

- 이 버전의 코드는 코드량이 조금 늘었고, 아마도 조금 더 느릴테지만, 런타임에 `ClassCastException`을  
  만날 일은 없으니 그만한 가치가 분명히 있다.

---

## 핵심 정리

- 배열과 제네릭에는 매우 다른 타입 규칙이 적용된다.  
  배열은 공변이고 실체화되는 반면, 제네릭은 불공변이고 타입 정보가 소거된다.  
  그 결과 배열은 런타임에는 타입 안전하지만, 컴파일 타입에는 그렇지 않다.  
  제네릭은 반대다. 그래서 둘을 섞어 쓰기란 쉽지 않다. 둘을 섞어 쓰다가 컴파일 오류나  
  경고를 만나면, 가장 먼저 배열을 리스트로 대체하는 방법을 적용해보자.

---
