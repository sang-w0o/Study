# 반환 타입으로는 스트림보다 컬렉션이 낫다

- 원소 시퀀스, 즉 일련의 원소를 반환하는 메소드는 수없이 많다. Java7까지는 이런 메소드의  
  반환 타입으로 `Collection`, `Set`, `List` 같은 컬렉션 인터페이스, 혹은  
  `Iterable`이나 배열을 썼다. 이 중 가장 적합한 타입을 선택하기란 그렇게 어렵지 않았다.  
  기본은 컬렉션 인터페이스다. for-each문에서만 쓰이거나 반환된 원소 시퀀스가 주로 `contains()`  
  같은 일부 `Collection`메소드를 구현할 수 없을 때는 `Iterable` 인터페이스를 썼다.  
  반환 원소들이 기본 타입이거나 성능에 민감한 상황이라면 배열을 썼다. 그런데 Java8에 `Stream`이라는  
  개념이 들어오면서 이 선택이 아주 복잡한 일이 되어버렸다.

- 원소 시퀀스를 반환할 때는 당연히 `Stream`을 사용해야 한다는 이야기를 들어봤을 수 있겠지만,  
  [Item 45](https://github.com/sang-w0o/Study/blob/master/Programming%20Paradigm/Effective%20Java/6.%20%EB%9E%8C%EB%8B%A4%EC%99%80%20%EC%8A%A4%ED%8A%B8%EB%A6%BC/Item%2045.%20%EC%8A%A4%ED%8A%B8%EB%A6%BC%EC%9D%80%20%EC%A3%BC%EC%9D%98%ED%95%B4%EC%84%9C%20%EC%82%AC%EC%9A%A9%ED%95%98%EB%9D%BC.md)에서 봤듯이 `Stream`은 반복(iteration)을 지원하지 않는다. 따라서 스트림과 반복을  
  알맞게 조합해야 좋은 코드가 나온다. API를 스트림만 반환하도록 만들어 놓으면 반환된 스트림을  
  for-each로 반복하길 원하는 사용자는 당연히 불만을 느낄 것이다. 여기서 재밌는 사실이 있는데,  
  사실 `Stream` 인터페이스는 `Iterable` 인터페이스가 정의한 추상 메소드들을 전부 포함할 뿐만  
  아니라, `Iterable` 인터페이스가 정의한 방식대로 동작한다. 그럼에도 for-each로 스트림을  
  반복할 수 없는 이유는 바로 `Stream`이 `Iterable`을 확장하기 않기 때문이다.

- 안타깝게도 이 문제를 해결해줄 수 있는 멋진 우회로는 없다. 얼핏 보면 `Stream`의 `iterator()`메소드에  
  메소드 참조를 건네면 해결될 수도 있을 것 같다.

```java
for(ProcessHandle ph : ProcessHandle.allProcesses()::iterator) {
  // iterating..
}
```

- 위 코드는 메소드 참조가 잘못 쓰였다는 컴파일 오류를 낸다. 이를 바로잡으려면 메소드 참고를  
  매개변수화된 `Iterable`로 적절히 형변환해줘야 한다.

```java
for(ProcessHandle ph: (Iterable<ProcessHandle>) ProcessHandle.allProcesses()::iterator) {
  // iterating..
}
```

- 작동은 하지만 실전에서 사용하기엔 너무 난잡하고 직관성이 떨어지는 코드가 생겼다.  
  다행이 어댑터 메소드를 사용하면 상황이 나아진다. Java에서 기본적으로 제공하지는 않지만, 아래 코드처럼  
  쉽게 만들어낼 수는 있다. 이 경우에는 Java의 타입 추론이 문맥을 잘 파악하기에 어댑터 메소드 내에서  
  따로 형변환하지 않아도 된다.

```java
public static <E> Iterable<E> iterableOf(Stream<E> stream) {
  return stream::iterator;
}
```

- 어댑터를 사용하면 어떠한 스트림에도 for-each를 사용해 반복할 수 있다.

```java
for(ProcessHandler ph : iterableOf(ProcessHandle.allProcessors())) {
  // iterating..
}
```

- [Item 45](https://github.com/sang-w0o/Study/blob/master/Programming%20Paradigm/Effective%20Java/6.%20%EB%9E%8C%EB%8B%A4%EC%99%80%20%EC%8A%A4%ED%8A%B8%EB%A6%BC/Item%2045.%20%EC%8A%A4%ED%8A%B8%EB%A6%BC%EC%9D%80%20%EC%A3%BC%EC%9D%98%ED%95%B4%EC%84%9C%20%EC%82%AC%EC%9A%A9%ED%95%98%EB%9D%BC.md)의 아나그램 프로그램에서 스트림 버전은 사전을 읽을 때 `Files.lines()`를 이용했고,  
  반복 버전을 `Scanner`를 사용했다. 둘 중 파일을 읽는 동안 발생하는 모든 예외를 알아서  
  처리해준다는 점에서 `Files.lines()` 쪽이 더 우수하다. 그래서 이상적으로는 반복 버전에서도  
  `Files.lines()`를 써야 한다. 이는 스트림만 반환하는 API가 반환한 값을 for-each로  
  반복하길 원하는 프로그래머가 감수해야할 부분이다.

- 반대로, 만약 API가 `Iterable`만 반환하면 이를 스트림 파이프라인에서 처리하려는 사용자가  
  불편함을 느낄 것이다. Java는 이를 위한 어댑터도 제공하지 않기에, 직접 구현해야 한다.

```java
public static <E> Stream<E> streamOf(Iterable<E> iterable) {
    return StreamSupport.stream(iterable.splititerator(), false);
}
```

- 객체 시퀀스를 반환하는 메소드를 작성하는데, 이 메소드가 오직 스트림 파이프라인에서만 쓰일 것을 안다면  
  스트림을 반환하게 해줘도 된다. 반대로 반환된 객체들이 반복문에서만 쓰일 것을 안다면 `Iterable`을  
  반환하자. 하지만 공개 API를 작성할 때는 이 두가지 경우 모두를 배려해야 한다.

- `Collection` 인터페이스는 `Iterable`의 하위 타입이고 `stream()` 메소드도 제공하기에 반복과  
  스트림을 동시에 지원한다. 따라서 **원소 시퀀스를 반환하는 공개 API의 반환 타입에는 `Collection`이나**  
  **그 하위 타입을 쓰는 것이 일반적으로 최선이다.** `Arrays` 역시 `Arrays.asList()`와  
  `Stream.of()`메소드로 쉽게 반복과 스트림을 지원할 수 있다. 반환하는 시퀀스의 크기가 메모리에 올려도  
  안전할 만큼 작다면 `ArrayList`나 `HashSet` 같은 표준 컬렉션 구현체를 반환하는 게 최선일 수 있다.  
  하지만 **단지 컬렉션을 반환한다는 이유로 덩치 큰 시퀀스를 메모리에 올려서는 안된다.**

- 반환할 시퀀스가 크지는 않지만 표현을 간결하게 할 수 있다면 전용 컬렉션을 구현하는 방법을 생각해보자.  
  예를 들어 주어진 집합의 멱집합(한 집합의 모든 부분집합을 원소로 하는 집합)을 반환하는 상황이라 해보자.  
  원소 개수가 n개면 멱집합의 원소 개수는 2^n 개가 된다. 그러니 멱집합을 표준 컬렉션 구현체에 저장하려는  
  생각은 위험하다. 하지만 `AbstractList`를 이용하면 훌륭한 전용 컬렉션을 손쉽게 구현할 수 있다.

- 비결은 멱집합을 구성하는 각 원소의 인덱스를 비트 벡터로 사용하는 것이다. 인덱스의 n번째 비트값은  
  해당 원소가 원래 집합의 n번째 원소를 포함하는지 여부를 알려준다. 따라서 0부터 2^n-1 까지의  
  이진수와 원소 n개인 집합의 멱집합과 자연스럽게 매핑된다.

```java
public class PowerSet {
  public static final <E> Collection<Set<E>> of(Set<E> s) {
    List<E> src = new ArrayList<>(s);
    if(src.size() > 30) {
      throw new IllegalArgumentException("To many elements in set(max: 30)");
    }
    return new AbstractList<Set<E>>() {
      @Override public int size() {
        return 1 << src.size();
      }

      @Override public boolean contains(Object o) {
        return o instanceof Set && src.containsAll((Set)o);
      }

      @Override public Set<E> get(int index) {
        Set<E> result = new HashSet<>();
        for(int i = 0; i != 0; i++, index >>= 1) {
          if((index & 1) == 1) {
            result.add(src.get(i));
          }
        }
        return result;
      }
    };
  }
}
```

> 입력 집함의 원소 개수를 30개로 제한한 이유는 `Collection.size()`가 int값을 반환하므로 `PowerSet.of()`가  
>  반환해야 할 시퀀스의 최대 길이는 `Integer.MAX_SIZE` 혹은 2^31-1 로 제한된다.

- `AbstractCollection`을 활용해 `Collection` 구현체를 작성할 때는 `Iterable`용 메소드 외에  
  2개만 더 구현하면 된다. 바로 `contains()`와 `size()`이다. 반복이 시작되기 전까지는 시퀀스의 내용을  
  확정할 수 없는 등의 사유로 `contains()`와 `size()`를 구현하는 게 불가능해 보일 때는 컬렉션보다는  
  스트림이나 `Iterable`을 반환하는 편이 낫다. 원한다면 별도의 메소드를 두어 두 방식을 모두 제공해도 된다.

- 때로는 단순히 구현하기 쉬운 쪽을 선택하기도 한다. 예를 들어 입력 리스트의 연속적인 부분리스트를 모두  
  반환하는 메소드를 작성한다 해보자. 필요한 부분리스트를 만들고, 표준 컬렉션에 담는 코드는 단 3줄이면 충분하다.  
  하지만 이 컬렉션은 입력 리스트의 거듭제곱만큼 메모리를 차지한다. 기하급수적으로 늘어나는 멱집합보다는 낫지만,  
  역시나 좋은 방법이 아닌 것은 명백하다. 멱집합 때처럼 전용 컬렉션을 구현하기란 지루한 일이다. 특히 Java는  
  이럴 때 쓸만한 골격 `Iterator`를 지원하지 않으니 지루함이 더 심해진다.

- 하지만 입력 리스트의 모든 부분 리스트를 스트림으로 구현하기는 어렵지 않다. 첫 번째 원소를 포함하는 부분리스트를  
  그 리스트의 prefix라 해보자. 예를 들어, `(a, b, c)`의 prefix는 `(a)`, `(a, b)`, `(a, b, c)`가 된다.  
  같은 식으로 마지막 원소를 포함하는 부분리스트를 그 리스트의 suffix라 하자. 따라서 `(a, b, c)`의 suffix는  
  `(a, b, c)`, `(b, c)`, `(c)`가 된다. 이렇게 봤을 때 어떤 리스트의 부분리스트는 단순히 그 리스트의  
  prefix의 suffix 혹은 suffix의 prefix에 빈 리스트 하나만 추가하면 된다.

```java
public class SubLists {
  public static <E> Stream<List<E>> of(List<E> list) {
    return Stream.concat(Stream.of(Collections.emptyList()),
      prefixes(list).flatMap(SubLists::suffixes));
  }

  private static <E> Stream<List<E>> prefixes(List<E> list) {
    return IntStream.rangeClosed(1, list.size())
      .mapToObj(end -> list.subList(0, end));
  }

  private static <E> Stream<List<E>> suffixes(List<E> list) {
    return IntStream.range(0, list.size())
      .mapToObj(start -> list.subList(start, list.size()));
  }
}
```

- `Stream.concat()`는 반환되는 스트림에 빈 리스트를 추가하며, `flatMap()`은 모든 prefix의  
  모든 suffix로 구성된 하나의 스트림을 만든다. 마지막으로 prefix들과 suffix들의 스트림은  
  `IntStream.range()`와 `IntStream.rangeClosed()`가 반환하는 연속되는 정수값들을 매핑해  
  만들었다. 쉽게 말해 이 관용구는 정수 인덱스를 사용한 표준 for문의 스트림 버전이라 할 수 있다.  
  따라서 이 구현은 for 반복문을 중첩해 만든 것과 취지가 비슷하다.

```java
for(int start = 0; start < src.size(); start++) {
  for(int end = start + 1; end <= src.size(); end++) {
    System.out.println(src.subList(start, end));
  }
}
```

- 이 반복문을 그대로 스트림으로 변환할 수 있다. 그렇게 하면 앞서의 구현보다 간결해지지만, 가독성은  
  떨어진다. 이 방식의 취지는 [Item 45](https://github.com/sang-w0o/Study/blob/master/Programming%20Paradigm/Effective%20Java/6.%20%EB%9E%8C%EB%8B%A4%EC%99%80%20%EC%8A%A4%ED%8A%B8%EB%A6%BC/Item%2045.%20%EC%8A%A4%ED%8A%B8%EB%A6%BC%EC%9D%80%20%EC%A3%BC%EC%9D%98%ED%95%B4%EC%84%9C%20%EC%82%AC%EC%9A%A9%ED%95%98%EB%9D%BC.md)에서 본 데카르트 곱용 코드와 비슷하다.

```java
public static <E> Stream<List<E>> of(List<E> list) {
  return IntStream.range(0, list.size())
    .mapToObj(start -> IntStream.rangeClosed(start + 1, list.size())
    .mapToObj(end -> list.subList(start, end)))
    .flatMap(x -> x);
}
```

- 바로 앞에서 본 for 반복문처럼 이 코드도 빈 리스트는 반환하지 않는다. 이 부분을 고치려면 앞에서처럼  
  `Stream.concat()`을 이용하거나, `IntStream.rangeClosed()` 호출 코드의 1을  
  `Math.signum(start)`로 바꿔주면 된다.

- 이상으로 스트림을 반환하는 두 가지 구현을 알아봤는데, 모두 쓸만은 하다. 하지만 반복을 사용하는게 더 자연스러운  
  상황에서도 사용자는 그냥 스트림을 쓰거나 `Stream`을 `Iterable`로 변환해주는 어댑터를 이용해야 한다.  
  하지만 이러한 어댑터는 클라이언트 코드를 어수선하게 만들고 속도도 느리다. 반면, 직접 구현한 전용 컬렉션을  
  사용하니 속도는 빠르지만, 스트림을 활용한 구현보다 가독성은 떨어진다.

---

## 핵심 정리

- 원소 시퀀스를 반환하는 메소드를 작성할 때는 이를 스트림으로 처리하기를 원하는 사용자와 반복으로  
  처리하기를 원하는 사용자가 모두 있을 수 있음을 떠올리고, 양쪽을 다 만족시키려 노력하자.  
  컬렉션을 반환할 수 있다면 그렇게 하자. 반환 전부터 이미 원소들을 컬렉션에 담아 관리하고 있거나  
  컬렉션을 하나 더 만들어도 될 정도로 원소 개수가 적다면 `ArrayList`와 같은 표준 컬렉션에  
  담아 반환하자. 그렇지 않으면 위의 멱집합 예시처럼 전용 컬렉션을 구현할지 고민해야 한다.  
  컬렉션을 반환하는 것이 불가능하다면 스트림과 `Iterable` 중 더 자연스러운 것을 반환하자.  
  만약 나중에 `Stream` 인터페이스가 `Iterable`을 지원하도록 Java가 수정된다면, 그때는  
  안심하고 스트림을 반환하면 될 것이다.(스트림 처리 및 반복 모두에 사용 가능하기 때문)

---
