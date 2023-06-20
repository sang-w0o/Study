# 제네릭과 가변인수를 함께 쓸 때는 신중하라

- 가변인수(varargs) 메소드와 제네릭은 Java5 에서 함께 추가되었으므로 서로 잘 어우러지리라  
  기대하겠지만, 슬프게도 그렇지 않다. 가변인수는 메소드에 넘기는 인수의 개수를 클라이언트가  
  조절할 수 있게 해주는데, 구현 방식에 허점이 있다. 가변인수 메소드를 호출하면 가변인수를  
  담기 위한 배열이 자동으로 하나 만들어진다. 그런데 내부로 감춰야 했을 이 배열을 그만  
  클라이언트에 노출하는 문제가 생겼다. 그 결과 varargs 매개변수에 제네릭이나 매개변수화  
  타입이 포함되면 알기 어려운 컴파일 경고가 발생한다.

- [Item 28](https://github.com/sang-w0o/Study/blob/master/Programming%20Paradigm/Effective%20Java/4.%20%EC%A0%9C%EB%84%A4%EB%A6%AD/Item%2028.%20%EB%B0%B0%EC%97%B4%EB%B3%B4%EB%8B%A4%EB%8A%94%20%EB%A6%AC%EC%8A%A4%ED%8A%B8%EB%A5%BC%20%EC%82%AC%EC%9A%A9%ED%95%98%EB%9D%BC.md)에서 실체화 불가 타입은 런타임에는 컴파일타임보다 타입 관련 정보를 적게 담고 있음을  
  배웠다. 그리고 거의 모든 제네릭과 매개변수화 타입은 실체화되지 않는다. 메소드를 선언할 때  
  실체화 불가 타입으로 varargs 매개변수를 선언하면 컴파일러가 경고를 보낸다.  
  가변인수 메소드를 호출할 때도 varargs 매개변수가 실체화 불가 타입으로 추론되면, 그 호출에  
  대해서도 경고를 낸다. 경고 형태는 대략 아래와 같다.

```
warning: [unchecked] Possible heap pollution from
    parameterized vararg type List<String>
```

- 매개변수화 타입의 변수가 타입이 다른 객체를 참조하면 heap 오염이 발생한다.  
  이렇게 다른 타입의 객체를 참조하는 상황에서는 컴파일러가 자동 생성한 형변환이  
  실패할 수도 있으니, 제네릭 타입 시스템이 약속한 타입 안전성의 근간이 흔들리게 된다.

- 아래 메소드를 보자.

```java
static void dangerous(List<String>... stringLists) {
  List<Integer> intList = List.of(42);
  Object[] objects = stringLists;
  objects[0] = intList;  // Heap Pollution
  String s = stringLists[0].get(0);  // @throws ClassCastException
}
```

- 위 메소드에서는 형변환하는 곳이 보이지 않는데도 인수를 건네 호출하면 `ClassCastException`을 던진다.  
  마지막 줄에 컴파일러가 생성한 보이지 않는 형변환이 숨어 있기 때문이다. 이처럼 타입 안전성이 깨지니  
  **제네릭 varargs 배열 매개변수에 값을 저장하는 것은 안전하지 않다.**

- 이 예시를 보고 한 가지 질문이 떠오른다. 제네릭 배열을 프로그래머가 직접 생성하는 것은 허용하지  
  않으면서 제네릭 varargs 매개변수를 받는 메소드를 선언할 수 있게 한 이유는 무엇일까?  
  그 답은 제네릭이나 매개변수화 타입의 varargs 매개변수를 받는 메소드가 실무에서 매우 유용하기 때문이다.  
  그래서 언어 설계자는 이 모순을 수용하기로 했다. 사실 Java 라이브러리에서도 이런 메소드를 여럿 제공하는데,  
  `Arrays.asList(T... a)`, `Collections.addAll(Collection<? super T> c, T... elements)`,  
  `EnumSet.of(E first, E... rest)`가 대표적이다. 다행인 점은 앞서 본 위험한 메소드와는 달리  
  이들은 타입 안전하다.

- Java7 전에는 제네릭 가변인수 메소드의 작성자가 호출자 쪽에서 발생하는 경고에 대해서 해줄 수 있는  
  일이 없었다. 따라서 이런 메소드는 사용하기에 조금 꺼림칙했다. 사용자들은 이 경고들을 그냥 두거나  
  더 흔하게는 호출하는 곳마다 `@SuppressWarnings("unchecked")`를 달아 경고를 숨겨야 했다.

- Java7 에서는 `@SafeVarargs` 어노테이션이 추가되어 제네릭 가변인수 메소드의 작성자가  
  클라이언트 측에서 발생하는 경고를 숨길 수 있게 되었다. **`@SafeVarargs` 어노테이션은 메소드 작성자가**  
  **그 메소드가 타입 안전함을 보장하는 장치이다.** 컴파일러는 이 약속을 믿고 그 메소드가 안전하지 않을  
  수 있다는 경고를 더 이상 하지 않는다.

- 메소드가 안전한게 확실하지 않다면 절대 `@SafeVarargs` 어노테이션을 달아서는 안된다.  
  그렇다면 메소드가 안전한지는 어떻게 확신할 수 있을까?  
  가변 인수 메소드를 호출할 때 varargs 매개변수를 담는 제네릭 배열이 만들어진다는 사실을 기억하자.  
  메소드가 이 배열에 아무것도 저장하지 않고, 그 배열의 참조가 밖으로 노출되지 않는다면 타입 안전하다.  
  달리 말하면, 이 varargs 매개변수 배열이 호출자로부터 순수하게 인수들을 전달하는 일만 한다면  
  그 메소드는 안전한 것이다.

- 이때, varargs 매개변수 배열에 아무것도 저장하지 않고도 타입 안전성을 깰 수도 있으니 주의해야 한다.  
  아래 코드는 가변인수로 넘어온 매개변수들을 배열에 담아 반환하는 제네릭 메소드다.

```java
class SomeClass {
  static<T> T[] toArray(T... args) {
    return args;
  }
}
```

- 위 메소드가 반환하는 배열의 타입은 이 메소드에 인수를 넘기는 컴파일타임에 결정되는데, 그 시점에는  
  컴파일러에게 충분한 정보가 주어지지 않아 타입을 잘못 판단할 수 있다. 따라서 자신의 varargs  
  매개변수 배열을 그대로 반환하면 Heap Pollution을 이 메소드를 호출한 쪽의 call stack으로까지  
  전이하는 결과를 낳을 수 있다.

- 구체적인 예시를 보자. 아래 메소드는 `T` 타입 인수 3개를 받아 그 중 2개를 무작위로 담은 배열을 반환한다.

```java
class SomeClass {
  static<T> T[] pickTwo(T a, T b, T c) {
    switch(ThreadLocalRandom.current().nextInt(3)) {
      case 0: return toArray(a, b);
      case 1: return toArray(a, c);
      case 2: return toArray(b, c);
    }
    throw new AssertionError();  // Unreachale code
  }
}
```

- 위 메소드는 제네릭 가변인수를 받는 `toArray()`를 호출한다는 점만 빼면 위험하지 않고, 경고도  
  내지 않을 것이다.

- 위 메소드를 본 컴파일러는 `toArray()`에 넘길 `T` 인스턴스 2개를 담을 varargs 매개변수 배열을  
  만드는 코드를 생성한다. 이 코드가 만드는 배열의 타입은 `Object[]`인데, 이는 `pickTwo()`에 어떤  
  타입의 객체를 넘기더라도 담을 수 있는 가장 구체적인 타입이기 때문이다. 그리고 `toArray()` 메소드가  
  돌려준 이 배열이 그대로 `pickTwo()`를 호출한 클라이언트에게까지 전달된다. 즉 `pickTwo()`는  
  항상 `Object[]` 타입의 배열을 반환하는 것이다.

- 이제 `pickTwo()`를 사용하는 클라이언트를 보자.

```java
public class Client {
  String[] attributes = pickTwo("Good", "Fast", "Cheap");
}
```

- 아무런 문제가 없는 메소드이니 별다른 경고 없이 컴파일된다.  
  하지만 실행해보면 `ClassCastException`을 던진다. 형변환하는 곳이 보이지 않는데도 말이다.  
  뭐가 잘못된 걸까? 바로 `pickTwo()`의 반환값을 attributes에 저장하기 위해 `String[]`으로  
  형변환하는 코드를 컴파일러가 자동 생성한다는 점을 놓친 것이다. `Object[]`는 `String[]`의  
  하위타입이 아니므로 이 형변환은 실패한다. 이 실패가 다소 황당하게 느껴질 수도 있다.  
  Heap Pollution을 발생시킨 진짜 원인인 `toArray()`로부터 두 단계나 떨어져 있고, varargs  
  매개변수 배열은 실제 매개변수가 저장된 후 변경된 적도 없으니 말이다.

- 이 예시는 **제네릭 varargs 매개변수 배열에 다른 메소드가 접근하도록 허용하면 안전하지 않다**는  
  점을 다시 한번 상기시킨다. 단, 예외가 두 가지 있다.  
  첫째, **`@SafeVarargs`로 제대로 어노테이트된 또 다른 varargs 메소드에 넘기는 것은 안전하다.**  
  둘째, **그저 이 배열의 내용의 일부 함수를 호출만 하는 (varargs를 받지 않는) 일반 메소드에 넘기는 것도 안전하다.**

- 아래 코드는 제네릭 varargs 매개변수를 안전하게 사용하는 전형적인 예시이다.  
  아래의 `flatten()` 메소드는 임의 개수의 리스트를 인수로 받아, 받은 순서대로 그 안의 모든 원소를  
  하나의 리스트로 옮겨 담아 반환한다. 이 메소드에는 `@SafeVarargs` 어노테이션이 붙어 있으니  
  선언하는 쪽과 사용하는 쪽 모두에서 경고를 내지 않는다.

```java
class SomeClass {
  @SafeVarargs
  static <T> List<T> flatten(List<? extends T>... lists) {
    List<T> result = new ArrayList<>();
    for (List<? extends T> list : lists) {
      result.addAll(list);
    }
    return result;
  }
}
```

- `@SafeVarargs` 어노테이션을 사용해야 할 때를 정하는 규칙은 간단하다.  
  **제네릭이나 매개변수화 타입의 varargs 매개변수를 받는 모든 메소드에 `@SafeVarargs`를 적용해라.**  
  그래야 사용자를 헷갈리게 하는 컴파일러 경고를 없앨 수 있다. 이 말은 곧 **안전하지 않은 varargs 메소드는 절대**  
  **작성해서는 안된다**는 뜻이기도 하다. 개발자가 통제할 수 있는 메소드 중 제네릭 varargs 매개변수를  
  사용하며 Heap Pollution 경고가 뜨는 메소드가 있다면, 그 메소드가 정말 안전하지 점검해야 한다.  
  정리하자면, 아래 두 조건을 **모두 만족**하는 제네릭 varargs 메소드는 안전하다.  
  둘 중 하나라도 어기면 안전하지 않다.

  - varargs 매개변수 배열에 아무것도 저장하지 않는다.
  - 그 배열(혹은 복제본)을 신뢰할 수 없는 코드에 노출하지 않는다.

> `@SafeVarargs` 어노테이션은 **재정의할 수 없는 메소드**에만 달아야 한다. 재정의한 메소드도  
> 안전할지는 보장할 수 없기 때문이다. Java8에서 이 어노테이션은 오직 정적 메소드와 final 인스턴스  
> 메소드에만 붙일 수 있고, Java9부터는 private 인스턴스 메소드에도 허용된다.

- `@SafeVarargs` 어노테이션이 유일한 정답은 아니다. 실체는 배열인 varargs 매개변수를  
  `List` 매개변수로 바꿀 수도 있다. 이 방식을 위의 `flatten()`에 적용해보자.  
  매개변수 선언만 수정했음에 주목하자.

```java
class SomeClass {

  static <T> List<T> flatten(List<List<? extends T>> lists) {
    List<T> result = new ArrayList<>();
    for (List<? extends T> list : lists) {
      result.addAll(list);
    }
    return result;
  }
}
```

- 정적 팩토리 메소드인 `List.of()`를 활용하면 아래 코드와 같이 이 메소드에 임의 개수의  
  인수를 넘길 수 있다. 이렇게 사용하는게 가능한 이유는 `List.of()`에도 `@SafeVarargs`  
  어노테이션이 붙어 있기 때문이다.

```java
class Client {
  Audience audience = flatten(List.of(friends, romans, countrymen));
}
```

- 이 방식의 장점은 컴파일러가 이 메소드의 타입 안전성을 검증할 수 있다는 데 있다.  
  `@SafeVarargs`를 우리가 직접 달지 않아도 되며, 실수로 안전하다고 판단할 걱정도 없다.  
  단점이라면 클라이언트 코드가 살짝 지저분해지고 속도가 조금 느려질 수 있는 정도다.

- 또한, 이 방식은 위에서 본 `toArray()` 처럼 varargs 메소드를 안전하게 작성하는게  
  불가능한 상황에서도 쓸 수 있다. 이 `toArray()`의 `List` 버전이 바로 `List.of()`로,  
  Java 라이브러리 차원에서 제공하니 우리가 직접 작성할 필요도 없다.  
  이 방식을 `pickTwo()`에 적용하면 아래처럼 된다.

```java
public class Someclass {
  static <T> List<T> pickTwo(T a, T b, T c) {
    switch(ThreadLocalRandom.current().nextInt(3)) {
      case 0: return List.of(a, b);
      case 1: return List.of(a, c);
      case 2: return List.of(b, c);
    }
    throw new AssertionError();  // unreachable
  }
}
```

- 그리고 main 메소드는 아래처럼 변한다.

```java
public class Client {
  List<String> attributes = pickTwo("Good", "Fast", "Cheap");
}
```

- 결과 코드는 배열 없이 제네릭만 사용하므로 타입 안전함이 보장된다.

---

## 핵심 정리

- 가변인수와 제네릭은 궁합이 좋지 않다. 가변인수 기능은 배열을 노출하여 추상화가 완벽하지 못하고,  
  배열과 제네릭의 타입 규칙이 서로 다르기 때문이다. 제네릭 varargs 매개변수는 타입 안전하지는  
  않지만, 허용된다. 메소드에 제네릭 혹은 매개변수화된 varargs 매개변수를 사용하고자 한다면  
  먼저 그 메소드가 타입 안전한지 확인한 다음 `@SafeVarargs` 어노테이션을 달아 사용하는 데  
  불편함이 없도록 하자.

---
