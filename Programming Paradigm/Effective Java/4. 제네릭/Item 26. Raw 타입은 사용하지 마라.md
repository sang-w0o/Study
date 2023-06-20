# Raw 타입은 사용하지 마라

## 용어 정의

- 클래스나 인터페이스 선언에 타입 매개변수(Type-Parameter)가 쓰이면, 이를  
  **제네릭 클래스** 혹은 **제네릭 인터페이스**라 한다. 예를 들어, `List` 인터페이스는  
  원소의 타입을 나타내는 타입 매개변수 `E`를 받는다.(`List<E>`) 그래서 이 인터페이스의  
  완전한 이름은 `List<E>`이지만, 짧게 그냥 `List`라고도 자주 쓴다. 제네릭 클래스와  
  제네릭 인터페이스를 통틀어 **제네릭 타입(Generic Type)** 이라 한다.

- 각각의 제네릭 타입은 일련의 **매개변수화 타입(Parameterized Type)** 을 정의한다.  
  먼저 클래스 혹은 인터페이스 이름이 나오고, 이어서 `<>` 안에 실제 타입 매개변수들을 나열한다.  
  예를 들어, `List<String>`은 원소의 타입이 `String`인 리스트를 뜻하는 매개변수화 타입이다.  
  여기서 `String`이 정규(formal) 타입 매개변수 `E`에 해당하는 실제(actual) 타입 매개변수다.

- 마지막으로, 제네릭 타입을 하나 정의 하면, 그에 딸린 **Raw 타입**도 함께 정의된다.  
  Raw 타입이란 제네릭 타입에서 타입 매개변수를 전혀 사용하지 않을 때를 말한다.  
  예를 들어, `List<E>`의 raw 타입은 `List`이다. Raw 타입은 타입 선언에서  
  제네릭 타입 정보가 전부 지워진 것처럼 작동하는데, 제네릭이 도래하기 전 코드와 호환되도록 하기 위한  
  궁여지책이라 할 수 있다.

- 제네릭을 지원하기 전에는 컬렉션을 아래와 같이 선언했다. Java9에서도 여전히 동작하지만  
  좋은 예라고 볼 수는 없다.

```java
// Stamp 인스턴스만 취급한다.
private final Collection stamps = /* ... */;
```

- 위 코드를 사용하면 실수로 `Stamp` 대신 `Coin`을 넣어도 아무 오류 없이 컴파일되고 실행된다.  
  단 컴파일러가 모호한 경고 메시지를 보여주긴 할 것이다.

- 컬렉션에서 이 `Coin`을 다시 꺼내기 전까지는 오류를 알아채지 못한다.

```java
for(Iterator i = stamps.iterator(); i.hasNext(); ) {
  Stamp stamp = (Stamp) i.next();  // @throws ClassCastException
  stamp.use();
}
```

- 오류는 가능한 한 발생 즉시, 이상적으로는 컴파일 시에 발견하는 것이 좋다. 이 예시에서는 오류가  
  발생하고 한참 뒤인 런타임에서야 알아챌 수 있는데, 이렇게 되면 런타임에 문제를 겪는 코드와  
  원인을 제공한 코드가 물리적으로 상당히 떨어져 있을 가능성이 커진다. `ClassCastException`이  
  발생하면 stamps에 `Coin`을 넣은 지점을 찾기 위해 코드 전체를 훑어봐야 할 수도 있다.  
  주석은 어짜피 컴파일러는 이해하지 못하니 별 도움이 되지 못한다.

- 제네릭을 활용하면 이 정보가 주석이 아닌 타입 선언 자체에 녹아든다.

```java
private final Collection<Stamp> stamps = /* ... */;
```

- 이렇게 선언하면 컴파일러는 stamps에는 `Stamp` 인스턴스만 넣어야 함을 인지하게 된다.  
  따라서 아무런 경고 없이 컴파일된다면 의도대로 동작할 것임이 보장된다.

- 컴파일러는 컬렉션에서 원소를 꺼내는 모든 곳에 보이지 않는 형변환을 추가하여 절대 실패하지 않음을  
  보장한다. `BigDecimal`용 컬렉션에 `BigInteger`가 들어가는 일 등을 막아주는 것이다.

- 앞서도 봤듯이, Raw 타입(타입 매개변수가 없는 제네릭 타입)을 쓰는 것을 언어적 차원에서는  
  막아놓지 않았지만 절대로 써서는 안된다. **Raw 타입을 쓰면 제네릭이 안겨주는 안전성과 표현력을**  
  **모두 잃게 된다.** 그렇다면 절대 써서는 안될 raw 타입을 왜 만들어 놓은걸까?  
  바로 호환성 때문이다. Java를 쓰는 개발자들이 매우 많아졌기에 기존 코드를 모두 수용하면서  
  제네릭을 사용하는 새로운 코드와도 맞물리게 돌아가게 해야만 했다. Raw 타입을 사용하는 메소드에  
  매개변수 타입의 인스턴스를 넘겨도 동작해야만 했던 것이다. 이 마이그레이션 호환성을 위해 raw 타입을  
  지원하고, 제네릭 구현에는 소거(erasure) 방식을 사용하기로 했다.

- `List` 같은 raw 타입은 사용해서는 안 되나, `List<Object>` 처럼 임의 객체를 허용하는  
  매개변수화 타입은 괜찮다. Raw 타입인 `List`와 매개변수화 타입인 `List<Object>`의 차이는 무엇일까?  
  간단히 이야기하자면, `List`는 제네릭 타입에서 타입을 완전히 제외한 것이고, `List<Object>`는  
  모든 타입을 허용한다는 의사를 컴파일러에게 전달한 것이다. 매개변수로 `List`를 받는 메소드에  
  `List<String>`을 넘길 수 있지만, `List<Object>` 를 받는 메소드에는 넘길 수 없다.  
  이는 제네릭의 하위 타입 규칙 때문이다. 즉, `List<String>`은 raw 타입인 `List`의 하위 타입이지만,  
  `List<Object>`의 하위 타입은 아니다. 그 결과, **`List<Object>` 같은 매개변수화 타입을 사용할 때와**  
  **달리 `List` 같은 raw 타입을 사용하면, 타입 안정성을 잃게 된다.**

- 아래 예시 코드를 보자.

```java
public static void main(String[] args) {
  List<String> strings = new ArrayList<>();
  unsafeAdd(strings, Integer.valueOf(42));
}

private static void unsafeAdd(List list, Object o) {
  list.add(o);
}
```

- 위 코드는 컴파일은 되지만, raw 타입인 `List`를 사용하기에 아래와 같은 경고가 나온다.

```
warning: [unchecked] unchecked call to add(E) as a member of the raw type java.util.List
```

- 이 프로그램을 이대로 실행하면, `strings.get(0)`의 결과를 형변환하여 할 때 `ClassCastException`을  
  던진다. `Integer`를 `String`으로 변환하려다 실패한 것이다. 이 형변환은 컴파일러가 자동으로  
  만들어준 것이라 보통은 실패하지 않는다. 하지만 이 경우에는 컴파일러의 경고를 무시하여  
  그 대가를 치른 것이다.

- 이제 raw 타입인 `List`를 매개변수화 타입인 `List<Object>`으로 바꾼 다음 다시 컴파일해보자.  
  이제는 오류 메시지가 나오며 컴파일 조차 되지 않는다.

```
error: incompatible types: List<String> cannot be converted to List<Object>
```

- 또 다른 예시로, 2개의 집합(`Set`)을 받아 공통 원소를 반환하는 메소드를 작성한다 해보자.  
  아래는 제네릭을 처음 접하는 사람이 작성할 법한 코드다.

```java
static int numElementsInCommon(Set s1, Set s2) {
  int result = 0;
  for (Object o : s1) {
    if (s2.contains(o)) {
      result++;
    }
  }
  return result;
}
```

- 위 메소드는 동작은 하지만 raw 타입을 사용하기에 안전하지 않다.  
  따라서 비한정적 와일드카드 타입(Unbounded Wildcard Type)을 대신 사용하는게 좋다.  
  제네릭 타입을 쓰고 싶지만 실제 타입 매개변수가 무엇인지 신경쓰고 싶지 않는다면 `?`를  
  사용하자. 예컨데 제네릭 타입인 `Set<E>`의 비한정적 와일드카드 타입은 `Set<?>`이다.  
  이것이 어떤 타입이라도 담을 수 있는 가장 범용적인 매개변수화 `Set` 타입이다.  
  아래는 비한정적 와일드카드 타입을 사용해 위 코드를 다시 작성한 모습이다.

```java
static int numElementsInCommon(Set<?> s1, Set<?> s2) { /* ... */ }
```

- 비한정적 와일드카드 타입인 `Set<?>`와 raw 타입인 `Set`의 차이는 무엇일까?  
  물음표가 어떤 역할을 하는걸까? 특징을 간단히 말하자면 와일드 카드 타입은 안전하고,  
  raw 타입은 안전하지가 않다. Raw 타입 컬렉션에는 아무런 원소나 넣을 수 있으니 타입  
  불변식을 훼손하기 쉽다. 반면, **`Collection<?>`에는 null 외에는 어떤 원소도 넣을 수 없다.**  
  다른 원소를 넣으려면 컴파일할 때 아래의 오류 메시지를 보게 될 것이다.

```java
Set<?> s1 = new HashSet<>();
s1.add("ASDF");

// error: incompatible types: String cannot be converted to CAP#1
```

- 컴파일러는 컬렉션의 타입 불변식을 훼손하지 못하게 막은 것이다. 구체적으로는, null 이외의  
  어떤 원소도 `Set<?>`에 넣지 못하게 했으며, 이 `Set`에서 꺼낼 수 있는 객체의 타입도  
  전혀 알 수 없게 했다. 이러한 제약을 받아들일 수 없다면 제네릭 메소드나 한정적 와일드카드  
  타입을 사용하면 된다.

- Raw 타입을 쓰지 말라는 규칙에도 소소한 예외가 몇 가지 있다.  
  **class 리터럴에는 raw 타입을 써야 한다.** Java 명세는 class 리터럴에  
  매개변수화 타입을 사용하지 못하게 했다.(배열과 기본타입은 허용).  
  예를 들어, `List.class`, `String[].class`, `int.class`는 허용되고  
  `List<String>.class`, `List<?>.class`는 허용하지 않는다.

- 두 번째 예외는 instanceof 연산자와 관련이 있다. 런타임에는 제네릭 타입 정보가 지워지므로  
  instanceof 연산자는 비한정적 와일드카드 타입 이외의 매개변수화 타입에는 적용할 수 없다.  
  그리고 raw 타입이든 비한정적 와일드카드 타입이든 instanceof는 완전히 똑같이 동작한다.  
  비한정적 와일드카드 타입의 `<?>`는 아무런 역할 없이 코드만 지저분하게 만드므로, 차라리  
  raw 타입을 쓰는 편이 깔끔하다.

- 아래는 **제네릭 타입에 instanceof를 사용하는 올바른 예시**이다.

```java
static boolean foo(Object o) {
  if(o instanceof Set) {
    Set<?> s = (Set<?>) o;
    //..
  }
  //..
}
```

> o 의 타입이 `Set`임을 확인한 다음 와일드카드 타입인 `Set<?>`로 형변환해야 한다.  
> (raw 타입인 `Set`이 아니다.) 이는 검사 형변환(checked cast)이므로 컴파일러  
> 경고가 뜨지 않는다.

---

## 핵심 정리

- Raw 타입을 사용하면 런타임에 예외가 일어날 수 있으니 사용해서는 안된다. Raw 타입은  
  제네릭이 도입되기 이전 코드와의 호환성을 위해 제공될 뿐이다. 빠르게 훑어보자면, `Set<Object>`는  
  어떤 타입의 객체도 저장할 수 있는 매개변수화 타입이고, `Set<?>`는 모든 종의 타입 객체만  
  저장할 수 있는 와일드카드 타입이다. 그리고 이들의 raw 타입인 `Set`은 제네릭 타입 시스템에  
  속하지 않는다. `Set<Object>`와 `Set<?>`는 안전하지만, raw 타입인 `Set`은 안전하지 않다.

---
