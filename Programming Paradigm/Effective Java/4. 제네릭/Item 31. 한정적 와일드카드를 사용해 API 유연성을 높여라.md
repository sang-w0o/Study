# 한정적 와일드카드를 사용해 API 유연성을 높여라

- Item 28에서 봤듯이 매개변수화 타입은 불공변(invariant)이다.  
  즉, 서로 다른 타입 `Type1`와 `Type2`가 있을 때 `List<Type1>`은  
  `List<Type2>`의 하위 타입도, 상위 타입도 아니다. 직관적이지 않겠지만  
  `List<String>`은 `List<Object>`의 하위 타입이 아니라는 뜻인데,  
  곰곰이 따져보면 사실 이쪽이 말이 된다. `List<Object>`에는 어떤 객체든  
  넣을 수 있지만, `List<String>`에는 문자열만 넣을 수 있다.  
  즉 `List<String>`은 `List<Object>`가 하는 일을 제대로 수행하지 못하니  
  하위 타입이 될 수 없다. (LSP에도 어긋난다.)

- 하지만 때로는 불공변 방식보다 유연한 무언가가 필요하다.  
  이전에 본 `Stack`의 공개 API를 살펴보자.

```java
public class Stack<E> {
    public Stack();
    public void push(E e);
    public E pop();
    public boolean isEmpty();
}
```

- 여기에 일련의 원소를 `Stack`에 넣는 메소드를 추가해야 한다 해보자.

```java
public class Stack<E> {
    //..
    public void pushAll(Iterable<E> elements) {
	for(E element: elements) {
	    push(element);
	}
    }
}
```

- 이 메소드는 깨끗이 컴파일되지만 완벽하진 않다. `Iterable<E>`의 원소 타입이  
  `Stack`의 원소 타입과 일치하면 잘 작동한다. 하지만 `Stack<Number>`로  
  선언한 후, `pushAll(intValues)`를 호출하면 어떻게 될까? 여기서 intValues는  
  `Integer` 타입이다. `Integer`는 `Number`의 하위 타입이니 잘 동작해야 할 것  
  같지만, 실제로는 오류가 난다.

- 다행이 해결책은 있다. Java는 이런 상황에 대처할 수 있는 **한정적 와일드카드 타입**이라는  
  특별한 매개변수화 타입을 지원한다. `pushAll()`의 입력 매개변수 타입은 _`E`의 `Iterable`_ 이  
  아니라 _`E`의 하위 타입의 `Iterable`_ 이어야 하며, 와일드 카드 타입 `Iterable<? extends E>`가  
  정확히 이런 뜻이다. 사실 하위 타입이란 자기 자신도 포함하지만, 그렇다고 자신을 확장(extend)한 것은  
  아니기 때문에 `extends`라는 키워드는 조금 어색한 감이 있다.

- 와일드카드 타입을 사용하도록 `pushAll()`을 수정해보자.

```java
public class Stack<E> {
    //..
    public void pushAll(Iterable<? extends E> elements) {
	for(E element: elements) {
	    push(element);
	}
    }
}
```

- 이번 수정으로 `Stack`은 물론 이를 사용하는 클라이언트 코드도 말끔히 컴파일된다.  
  `Stack`과 클라이언트 모두 깔끔히 컴파일되었다는 것은 모든 것이 타입 안전하다는 뜻이다.

- 이제 `pushAll()`과 짝을 이루는 `popAll()`을 작성해보자.  
  `popAll()`은 `Stack`안의 모든 원소를 주어진 컬렉션으로 옮겨 담는 메소드이다.  
  아래처럼 작성했다 해보자.

```java
public class Stack<E> {
    //..
    public void popAll(Collection<E> destination) {
	while(!isEmpty()) {
	    destination.add(pop());
	}
    }
}
```

- 이번에도 주어진 컬렉션의 원소 타입이 `Stack`의 원소 타입과 일치한다면 말끔히 컴파일되고  
  문제없이 동작한다. 하지만 이번에도 역시나 완벽하진 않다. `Stack<Number>`의 원소를  
  `Object`용 컬렉션으로 옮긴다 해보자. 아래처럼 말이다.

```java
Stack<Number> numberStack = new Stack<>();
Collection<Object> objects = /* ... */;
numberStack.popAll(objects);
```

- 위 클라이언트 코드를 앞의 `popAll()`과 함께 컴파일하면 `Collection<Object>`는  
  `Collection<Number>`의 하위 타입이 아니라는 오류가 발생한다. 이번에도 와일드카드 타입으로  
  해결할 수 있다. 이번에는 `popAll()`의 입력 매개변수 타입이 *`E`의 컬렉션*이 아니라  
  _`E`의 상위 타입의 컬렉션_ 이어야 한다. 와일드카드 타입을 사용한 `Collection<? super E>`가  
  정확히 이런 의미이다. 이를 `popAll()`에 적용해보자.

```java
public class Stack<E> {
    //..
    public void popAll(Collection<? super E> destination) {
	while(!isEmpty()) {
	    destination.add(pop());
	}
    }
}
```

- 이제 `Stack`과 클라이언트 코드 모두 말끔히 컴파일된다.

- **유연성을 극대화하려면 원소의 생산자나 소비자용 입력 매개변수에 와일드카드 타입을 사용해라.**  
  한편, 입력 매개변수가 생산자와 소비자 역할을 동시에 한다면 와일드카드 타입을 써도  
  좋을 게 없다. 타입을 정확히 지정해야 하는 상황에서는 와일드카드 타입을 쓰지 말아야 한다.  
  아래 공식을 외워두면, 어떤 와일드카드 타입을 써야 하는지 기억하는 데 도움이 될 것이다.

> **PECS: Producer-extends, Consumer-super**

- 즉 매개변수화 타입 `T`가 생산자라면 `<? extends T>`를 사용하고, 소비자라면 `<? super T>`를 사용하라.  
  `Stack` 예시에서 `pushAll()`의 인자는 `Stack`이 사용할 `E` 인스턴스를 생산하므로 생산자에 해당하고,  
  그렇기에 `Iterable<? extends E>`가 되었다. 반대로 `popAll()`의 인자는 `Stack`으로부터  
  `E` 인스턴스를 소비하므로 매개변수의 적절한 타입은 `Collection<? super E>`인 것이다.  
  PECS 공식은 와일드카드 타입을 사용하는 기본 원칙이다.

- 위 공식을 기억해두고, 앞서 본 코드들의 메소드와 생성자 선언을 다시 살펴보자.  
  Item 28의 `Chooser` 생성자는 아래와 같이 선언했었다.

```java
public class Chooser<T> {
    public Chooser(Collection<T> choices) {/*...*/}
}
```

- 이 생성자로 넘겨지는 choices 컬렉션을 `T` 타입의 값을 **생산**하기만 하니, `T`를 **확장**하는  
  와일드카드 타입을 사용해 선언해야 한다. 알맞게 수정해보자.

```java
public class Chooser<T> {
    public Chooser(Collection<? extends T> choices) {/*...*/}
}
```

- 이렇게 하면 `Choose<Number>`의 생성자에 `List<Integer>`를 넘길 수도 있게 된다.

- 이번엔 두 개의 `Set`의 합집합을 반환하는 `union()`을 보자.

```java
public class SetUtils<E> {
    public static<E> Set<E> union(Set<E> s1, Set<E> s3) { /*...*/ }
}
```

- s1, s2 모두 `E`의 생산자이니 PECS 공식에 따라 아래처럼 바꿔야 한다.

```java
public class SetUtils<E> {
    public static<E> Set<E> union(Set<? extends E> s1, Set<? extends E> s2) { /*...*/ }
}
```

> 반환 타입은 여전히 `Set<E>`임에 주목하자.  
> **반환 타입에는 한정적 와일드카드 타입을 사용하면 안된다.**  
> 유연성을 높여주기는 커녕 클라이언트 코드에서도 와일드카드 타입을 써야하기 때문이다.

- 수정한 선언을 사용하면 아래 클라이언트 코드도 말끔히 컴파일된다.

```java
Set<Integer> integers = Set.of(1, 2, 4);
Set<Double> doubles = Set.of(3.0, 4.0, 5.0);
Set<Number> numbers = union(integers, doubles);
```

- 제대로만 사용한다면 클래스 사용자는 와일드카드 타입이 쓰였다는 사실조차 의식하지 못할 것이다.  
  받아들여야 할 매개변수를 받고 거절해야할 매개변수는 거절하는 작업이 알아서 이뤄지기 때문이다.  
  **클래스 사용자가 와일드카드 타입을 신경써야 한다면 그 API에 문제가 있을 가능성이 크다.**

- 앞의 코드는 Java8부터 제대로 컴파일된다. Java7 까지는 타입 추론 능력이 충분히  
  강력하지 못해 문맥에 맞는 반환 타입을 명시해야 했다. 즉, 아래처럼 해야 한다.

```java
Set<Number> numbers = SetUtils.<Number>union(integers, doubles);
```

> 매개변수(parameter)와 인수(argument)의 차이를 알아보자.  
> 매개변수는 메소드 선언에 정의한 변수이며, 인수는 메소드 호출 시 넘기는 _실제값_ 이다.  
> 예를 들어, `void add(int value) { .. }`, 그리고 `add(10)`이 있다 하자.  
> 여기서 value는 매개변수이며 10은 인수이다.  
> 이 정의를 제네릭까지 확장하면 아래와 같다.  
> `class Set<T> {..}`와 `Set<Integer> = {..}`가 있을 때 `T`는 타입 매개변수가 되고,  
> `Integer`는 타입 인수가 된다.

- 이번에는 Item 30에서 본 아래 코드를 보자.

```java
public static <E extends Comparable<E>> E max(List<E> c) {
    if(c.isEmpty())
        throw new IllegalArgumentException("empty collection");
    E result = null;
    for(E e: c) {
	if(result == null || e.compareTo(result) > 0)
	    result = Objects.requireNonNull(e);
    }
}
```

- 아래는 위 코드를 와일드카트 타입을 사용해 다듬은 모습이다.

```java
public static <E extends Comparable<? super E>> E max(List<? extends E> list) {
    //..
}
```

- 이번에는 PECS 공식을 두 번 적용했다. 둘 중 더 쉬운 입력 매개변수 목록부터 살펴보자.  
  입력 매개변수에서는 `E` 인스턴스를 생성하므로 원래의 `List<E>`를 `List<? extends E>`로 수정했다.

- 다음으로 타입 매개변수 `E`를 보자. 원래 선언에서는 `<E extends Comparable<E>>`, 즉 `E`가  
  `Comparable<E>`를 확장한다 선어했는데, 이때 `Comparable<E>`는 `E` 인스턴스를 소비한다.  
  소비하고 선후 관계를 뜻하는 정수를 생산한다. 그래서 매개변수화 타입 `Comparable<E>`를 한정적  
  와일드카드 타입인 `Comparable<? super E>`로 대체했다. `Comparable`은 언제나 소비자이므로  
  일반적으로는 **`Comparable<E>` 보다는 `Comparable<? super E>`를 사용하는 편이 낫다.**  
  `Comparator`도 마찬가지다. **`Comparator<E>` 보다는 `Comparable<? super E>`를 사용하는 편이 낫다.**

- 이렇게까지 복잡하게 선언해도 그럴만한 가치가 있다.  
  그 근거로, 아래 리스트는 오직 수정된 버전의 `max()`로만 처리할 수 있다.

```java
List<ScheduledFuture<?>> scheduledFutures = /* ... */;
```

- 수정 전의 `max()`가 이 리스트를 처리할 수 없는 이유는 `java.util.concurrent.ScheduledFuture`가  
  `Comparable<ScheduledFuture>`를 구현하지 않기 때문이다. `ScheduledFuture`는 `Delayed`의  
  하위 인터페이스이고, `Delayed`는 `Comparable<Delayed>`를 확장했다. 다시말해, `ScheduledFuture`의  
  인스턴스는 다른 `ScheduledFuture` 인스턴스 뿐 아니라 `Delayed` 인스턴스와도 비교할 수 있어서 수정 전  
  `max()`가 이 리스트를 거부하는 것이다. 더 일반화해서 말하면, `Comparable` 혹은 `Comparator`을  
  직접 구현하지 않고, 직접 구현한 다른 타입을 확장한 타입을 지원하기 위해 와일드카드가 필요하다.

- 와일드카드와 관련해 논의해야 할 주제가 하나 더 남았다. 타입 매개변수와 와일드카드에는 공통되는  
  부분이 있어서, 메소드를 정의할 때 둘 중 어느 것을 선택해도 괜찮을 때가 많다.  
  예를 들어 주어진 리스트에서 명시한 두 index의 아이템을 교환(swap)하는 정적 메소드를 두 방식 모두로  
  정의해보자. 아래 코드의 첫 번째는 비한정적 타입 매개변수를 사용했고 두 번째는 비한정적 와일드카드를 사용했다.

```java
public class ListUtils<E> {
    public static <E> void swap(List<E> list, int i, int j);
    public static void swap(List<?> list, int i, int j);
}
```

- 어떤 선언이 더 나으며, 왜 더 나을까?  
  public API라면 간단한 두 번째가 더 낫다. 어떤 리스트든 이 메소드에 넘기면 명시한 index의  
  원소드를 교환해줄 것이다. 또한 신경써야 할 타입 매개변수도 없다.

- 기본 규칙은 이렇다. **메소드 선언에 타입 매개변수가 한 번만 나오면 와일드카드로 대체하라.**  
  이때 비한정적 타입 매개변수라면 비한정적 와일드카드로 바꾸고, 한정적 타입 매개변수라면 한정적  
  와일드카드로 바꾸면 된다.

- 하지만 두 번째 `swap()` 선언에는 한 가지 문제점이 있는데, 아래처럼 아주 직관적으로  
  구현한 코드가 컴파일되지 않는다는 것이다.

```java
public class ListUtils<E> {
    public static void swap(List<?> list, int i, int j) {
	list.set(i, list.set(j, list.get(i)));
    }
}
```

- 오류는 `list.get(i)`에서 나오는데, 방금 꺼낸 원소를 리스트에 다시 넣을 수 없기 때문이다.  
  오류의 원인은 리스트의 타입이 `List<?>`인데, `List<?>`에는 null 외에는 어떤 값도 넣을 수  
  없다는 데 있다. 다행이 런타임 오류를 낼 가능성이 있는 형변환이나 리스트의 raw 타입을 사용하지  
  않고도 해결할 수 있는 방법이 있다. 바로 _와일드카드 타입의 실제 타입을 알려주는 메소드를 private_  
  _도우미 메소드로 따로 작성하여 활용_ 하는 방법이다. 실제 타입을 알아내려면 이 도우미 메소드는  
  제네릭 메소드여야 한다.

```java
public class ListUtils<E> {
    public static void swap(List<?> list, int i, int j) {
	swapHelper(list, i, j);
    }

    private static <E> void swapHelper(List<E> list, int i, int j) {
	list.set(i, list.set(j, list.get(i)));
    }
}
```

- `swapHelper()` 메소드는 리스트가 `List<E>`임을 알고 있다. 즉, 이 리스트에서 꺼낸 값의  
  타입은 항상 `E`이고, `E` 타입의 값이라면 이 리스트에 넣어도 안전함을 알고 있다.  
  다소 복잡하게 구현했지만 이제 깔끔히 컴파일된다. 이렇게 `swap()`의 내부에서는 더 복잡한 제네릭  
  메소드를 이용했지만, 덕분에 외부에서는 와일드카드 기반의 멋진 선언을 유지할 수 있게 되었다.  
  즉, `swap()`을 호출하는 클라이언트는 복잡한 `swapHelper()`의 존재를 모른 채 그 혜택을  
  누리는 것이다.

<hr/>

## 핵심 정리

- 조금 복잡하더라도 와일드카드 타입을 적용하면 API가 훨씬 유연해진다. 그러니 널리 쓰일 라이브러리를  
  작성한다면 반드시 와일드카드 타입을 적절히 사용해주자. PECS 공식을 기억하자. 즉, 생산자(Producer)는  
  `extends`를, 소비자(Consumer)는 `super`를 사용한다. `Comparable`, `Comparator`는 모두  
  소비자라는 사실도 잊지 말자.

<hr/>
