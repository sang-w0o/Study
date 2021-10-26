# Optional 반환은 신중히 하라

- Java8 전에는 메소드가 특정 조건에서 값을 반환할 수 없을 때 취할 수 있는 선택지가  
  두 가지 있었다. 예외를 던지거나, 반환 타입이 객체 참조라면 null을 반환하는 것이다.  
  두 방법에는 모두 허점이 있다. 예외는 진짜 예외적인 상황에서만 사용해야 하며 예외를 생성할 때  
  스택 추적 전체를 캡쳐하므로 비용도 만만치 않다. null을 반환하면 이런 문제가 생기지 않지만  
  그 나름의 문제가 있다. null을 반환할 수 있는 메소드를 호출할 때는 null이 반환될 일이 절대로  
  없다고 확신하지 않는 한 별도의 null 처리 코드를 추가해야 한다. null 처리를 무시하고 반환된  
  null값을 어딘가에 저장해두면 언젠가 NPE가 발생할 수 있다. 그것도 근본적인 원인, 즉 null을  
  반환하게 한 실제 원인과는 전혀 상관없는 코드에서 말이다.

- Java 버전이 8로 올라가면서 또 하나의 선택지가 생겼다. 그 주인공인 `Optional<T>`는 null이 아닌  
  `T` 타입 참조를 하나 담거나, 혹은 아무것도 담지 않을 수 있다. 아무것도 담지 않은 `Optional`은  
  _비어있다(empty)_ 고 한다. `Optional`은 원소를 최대 1개 가질 수 있는 불변 컬렉션이다.  
  `Optional<T>`가 `Collection<T>`를 구현하지는 않았지만, 원칙적으로 그렇다는 말이다.

- 보통은 `T`를 반환해야 하지만, 특정 조건에서는 아무것도 반환하지 않아야 할 때 `T` 대신 `Optional<T>`를  
  반환하도록 선언하면 된다. 그러면 유효한 반환값이 없을 때는 빈 결과를 반환하는 메소드가 만들어진다.  
  `Optional`을 반환하는 메소드는 예외를 던지는 메소드보다 유연하고 사용하기 쉬우며, null을 반환하는  
  메소드보다 오류 가능성이 적다.

- 아래 코드는 주어진 컬렉션에서 최대값을 뽑아주는 메소드다.

```java
public static <E extends Comparable<E>> E max(Collection<E> c) {
    if(c.isEmpty()) {
	throw new IllegalArgumentException("Empty collection.");
    }
    E result = null;
    for(E e : c) {
	if(result == null || e.compareTo(result) > 0) {
	    result = Objects.requireNonNull(e);
	}
    }
    return result;
}
```

- 위 메소드에 빈 컬렉션을 건네면 `IllegalAgrumentException`을 던진다.  
  이를 `Optional`을 반환하도록 수정해보자.

```java
public static <E extends Comparable<E>> Optional<E> max(Collection<E> c) {
    if(c.isEmpty()) {
	return Optional.empty();
    }
    E result = null;
    for(E e : c) {
	if(result == null || e.compareTo(result) > 0) {
	    result = Objects.requireNonNull(e);
	}
    }
    return Optional.of(result);
}
```

- 보다시피 `Optional`을 반환하도록 구현하는 것은 어렵지 않다. 적절한 정적 팩토리를 사용해 `Optional`을  
  생성해주기만 하면 된다. 위 코드에서는 두 가지 팩토리를 사용했다. 빈 `Optional`은 `Optional.empty()`로  
  만들고, 값이 든 `Optional`은 `Optional.of(value)`로 생성했다. `Optional.of(value)`에  
  null을 넣으면 NPE를 던지니 주의하자. null 값도 허용하는 `Optional`을 만들려면 `Optional.ofNullable(value)`를  
  사용하면 된다. **`Optional`을 반환하는 메소드에서는 절대 null을 반환하지 말자.** 이는 `Optional`을  
  도입한 취지를 완전히 무시하는 행위다.

- 스트림의 종단 연산 중 상당수가 `Optional`을 반환한다. 앞의 `max()` 메소드를 스트림 버전으로 다시 작성하면  
  `Stream#max()` 연산이 `Optional`을 생성해준다.

```java
public static <E extends Comparable<E>> Optional<E> max(Collection<E> c) {
    return c.stream().max(Comparator.naturalOrder());
}
```

- 그렇다면 null을 반환하거나 예외를 던지는 대신 `Optional`을 반환하게 해야할 기준을 무엇일까?  
  **`Optional`은 검사 예외와 취지가 비슷하다.** 즉, 반환값이 없을 수도 있음을 API 사용자에게  
  명확히 알려준다. 비검사 예외를 던지거나 null을 반환한다면 API 사용자가 그 사실을 인지하지 못해  
  끔찍한 결과로 이어질 수 있다. 하지만 검사 예외를 던지면 클라이언트에서는 반드시 이에 대처하는  
  코드를 작성해넣어야 한다.

- 비슷하게, 메소드가 `Optional`을 반환한다면 클라이언트는 값을 받지 못했을 때 취할 행동을 선택해야 한다.  
  그중 하나는 기본값을 설정하는 방법이다.

```java
String lastWordInLexicon = max(words).orElse("No word..");
```

- 또는 상황에 맞는 예외를 던질 수도 있다. 아래 코드에서 실제 예외가 아니라 예외 팩토리를 건넨 것에  
  주목하자. 이렇게 하면 예외가 실제로 발생하지 않는 한 예외 생성 비용은 들지 않는다.

```java
Toy myToy = max(toys).orElseThrow(TemperTantrumException::new);
```

- `Optional`에 값이 항상 채워져 있다고 확신한다면 그냥 곧바로 값을 꺼내 사용하는 선택지도 있다.  
  다만 잘못 판단한 것이라면 `NoSuchElementException`이 발생할 것이다.

```java
Element lastNobleGas = max(Elements.NOBLE_GASES).get();
```

- 가끔 기본값을 설정하는 비용이 매우 커서 부담이 될 때가 있다. 그럴 때는 `Supplier<T>`를 인수로 받는  
  `orElseGet()`을 사용하면, 값이 처음 필요할 때 `Supplier<T>`를 사용해 생성하므로 초기 설정 비용을  
  낮출 수 있다. 더 특별한 쓰임에 대비한 메소드도 준비되어 있다. 바로 `filter()`, `map()`, `flatMap()`,  
  `ifPresent()`이다. 앞서의 기본 메소드들로 처리하기 어려워 보인다면, API 문서를 참고해 이 고급  
  메소드들이 문제를 해결해줄 수 있을지 검토해보자.

- 여전히 적합한 메소드를 찾지 못했다면 `isPresent()` 메소드를 살펴보자. 이는 안전 밸브 역할의 메소드로,  
  `Optional`이 채워져 있으면 true를, 비어 있으면 false를 반환한다. 이 메소드로는 원하는 모든 작업을  
  수행할 수 있지만 신중히 사용해야 한다. 실제로 `isPresent()`를 쓴 코드 중 상당수는 앞서 언급한 메소드들로  
  대체할 수 있으며, 그렇게 하면 더 짧고 명확하고 용법에 맞는 코드가 된다.

- 아래 코드를 예시로 생각해보자. 부모 프로세스의 프로세스ID를 출력하거나, 부모가 없다면 "N/A"를 출력한다.  
  Java9에서 소개된 `ProcessHandle` 클래스를 사용했다.

```java
Optional<ProcessHandle> parentProcess = ph.parent();
System.out.println("Parent PID: " + (parentProcess.isPresent() ? String.valueOf(parentProcess.get().pid()) : "N/A"));
```

- 위 코드는 `Optional#map()`을 사용해 아래처럼 다듬을 수 있다.

```java
System.out.println("Parent PID: " + ph.parent().map(h -> String.valueOf(h.pid()).orElse("N/A")));
```

- `Stream`을 사용한다면 `Optional`들을 `Stream<Optional<T>>`로 받아서, 그 중 채워진 `Optional` 들에서  
  값을 뽑아 `Stream<T>`에 건네 담아 처리하는 경우가 드물지 않다.  
  Java8에서는 아래처럼 구현할 수 있다.

```java
streamOfOptionals
    .filter(Optional::isPresent)
    .map(Optional::get);
```

- 보다시피 `Optional`에 값이 있다면(`Optional::isPresent`) 그 값을 꺼내(`Optional::get`)  
  `Stream`에 매핑한다.

- Java9에서는 `Optional`에 `stream()` 메소드가 추가되었다. 이 메소드는 `Optional`을  
  `Stream`으로 변환해주는 어댑터다. `Optional`에 값이 있으면 그 값을 원소로 담은 `Stream`으로,  
  값이 없다면 빈 `Stream`으로 변환한다. 이를 `Stream`의 `flatMap()`과 조합하면 위의 코드를  
  아래처럼 명료하게 바꿀 수 있다.

```java
streamOfOptionals
    .flatMap(Optional::stream);
```

- 반환값으로 `Optional`을 사용한다고 해서 무조건 득이 되는 것은 아니다.  
  **`Collection`, `Stream`, `Optional`, 배열 등의 컨테이너 타입은 `Optional`로 감싸면 안된다.**  
  비어 있는 `Optional<List<T>>`를 반환하기 보다는 `List<T>`를 반환하는 것이 좋다.  
  빈 컨테이너를 그대로 반환하면 클라이언트에 `Optional` 처리 코드를 넣지 않아도 된다. 참고로 `ProcessHandle.Info`  
  인터페이스의 `arguments()` 메소드는 `Optional<String[]>`을 반환하는데, 이는 예외적인 경우이니  
  따라하지 말자.

- 그렇다면 어떤 경우에 메소드 반환 타입을 `T` 대신 `Optional<T>`로 선언해야 할까?  
  기본 규칙은 **결과가 없을 수 있으며, 클라이언트가 이 상황을 특별하게 처리해야 한다면 `Optional<T>`를 반환한다.**  
  그런데 이렇게 하더라도 `Optional<T>`를 반환하는 데는 대가가 따른다. `Optional`도 엄연히 새로  
  할당하고 초기화해야 하는 객체이고, 그 안에서 값을 꺼내려면 메소드를 호출해야 하니 한 단계를 더 거치는 셈이다.  
  그래서 성능이 중요한 상황에서는 `Optional`이 맞지 않을 수 있다. 어떤 메소드가 이 상황에 처하는지  
  알아내려면 세심히 측정해보는 수 밖에 없다.

- Boxing된 기본 타입을 담는 `Optional`은 기본 타입 자체보다 무거울 수밖에 없다.  
  값을 두 겹이나 감싸기 때문이다. 그래서 Java API 설계자는 int, long, double 전용 `Optional`  
  클래스들을 준비해놨다. 바로 `OptionalInt`, `OptionalLong`, `OptionalDouble`이다.  
  이 `Optional`들도 `Optional<T>`가 제공하는 메소드를 거의 다 제공한다.  
  이렇게 대체제까지 있으니 **Boxing된 기본 타입을 담은 `Optional`을 반환하는 일은 없도록 하자.**  
  단, _"덜 중요한 기본 타입"_ 용인 `Boolean`, `Byte`, `Character`, `Short`, `Float`는  
  예외일 수 있다.

- 지금까지 `Optional`을 반환하고, 반환된 `Optional`을 처리하는 내용을 다뤘다.  
  다른 쓰임에 대해서는 논하지 않았는데, 대부분 적절치 않기 때문이다. 예를 들어 `Optional`을 `Map`의  
  key 또는 value로 사용하면 절대 안된다. 만약 그렇게 한다면 `Map`안에 key가 없다는 사실을 나타내는 방법이  
  두 가지가 된다. 하나는 key 자체가 없는 경우고, 다른 하나는 key는 있지만 그 key가 속이 빈 `Optional`인  
  경우다. 쓸데없이 복잡성만 높여서 혼란과 오류 가능성을 키울 뿐이다. 더 일반화해 이야기하면  
  **`Optional`을 컬렉션의 key, value, 원소나 배열의 원소로 사용하는 게 적절한 상황은 거의 없다.**

- 그렇다면 커다란 의문이 하나 남는다. `Optional`을 인스턴스 필드에 저장해두는 것이 필요할 때가 있을까?  
  이런 상황 대부분은 필수 필드를 갖는 클래스와, 이를 확장해 선택적 필드를 추가한 하위 클래스를 따로  
  만들어야 함을 암시하는 _'나쁜 냄새'_ 다. 하지만 가끔은 적절한 상황도 있다. 모든 필드들의 타입이  
  기본 타입이고, 그 중 선택적 필드가 있다면 값이 없음을 나타내기가 마땅치 않다. 이런 클래스라면  
  선택적 필드들의 getter 들이 `Optional`을 반환하게 해주면 좋았을 것이다. 따라서 이럴 경우에는 필드  
  자체를 `Optional`로 선언하는 것도 좋은 방법이다.

<hr/>

## 핵심 정리

- 값을 반환하지 못할 가능성이 있고, 호출할 때마다 반환값이 없을 가능성을 염두에 둬야 하는 메소드라면  
  `Optional`을 반환해야 할 상황일 수 있다. 하지만 `Optional` 반환에는 성능 저하가 뒤따르니,  
  성능에 민감한 메소드라면 null을 반환하거나 예외를 던지는 편이 나을 수도 있다. 그리고 `Optional`을  
  반환값 이외의 용도로 쓰는 경우는 매우 드물다.

<hr/>
