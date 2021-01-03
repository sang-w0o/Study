<h1>Java Stream API</h1>

<h2>Stream API</h2>

* Java 8에서 등장한 새로운 핵심 기능 중 하나는 Stream functionality, `java.util.stream`으로,   
  이는 요소들(elements)에 대한 특정 기능을 수행할 수 있는 클래스들을 담고 있다.
<hr/>

<h3>Stream 만들기</h3>

* Stream 객체는 컬렉션, 배열 등에서 만들어질 수 있으며, `stream()`, `of()` 메소드 등으로 만들어질 수 있다.

```java
String[] arr = new String[]{"a", "b", "c"};
Stream<String> stream = Arrays.stream(arr);
stream = Stream.of("a", "b", "c");
```

* Default 메소드인 `stream()` 메소드는 `Collection` 인터페이스에 추가되어 있으며,   
  원소를 담는 컬렉션의 타입에 관계없이 `Stream<T>`를 생성할 수 있게 해준다.

<h3>Stream으로 하는 Multi-Threading</h3>

* Stream API는 `parallelStream()`을 통해 multi threading을 간결하게 제공한다.   
  이 메소드는 Stream내의 원소들에 대해 병렬적으로 작업을 수행할 수 있게 해준다.
```java
list.parallelStream().forEach(element -> doWork(element));
```

* 위 코드는 병렬적으로 Stream의 모든 원소들에 대해 `doWork()` 메소드를 호출하도록 한다.
<hr/>

<h2>Stream Operations</h2>

* Stream에 대해 수행될 수 있는 여러개의 유용한 operation들이 있다.   
  이 operation들은 `Intermediate Operations`(`Stream<T>` 반환)과 `Terminal Operations(원소의 타입에 알맞게 반환)`으로 구분된다.
* `Intermediate Operation`들은 chaining을 지원한다.
  
* Stream에 대한 operation들은 __원본을 수정하지 않는다__.

```java
long count = list.stream().distinct().count();
```

* 위의 `distinct()` 메소드는 `Intermediate Operation`중 하나로, 이전 `Stream`에서 각각 Unique한 원소들을 가져와서   
  새로운 `Stream`을 반환한다. 그리고 `count()` 메소드는 `Stream`의 size를 반환한다.

<h3>Iterating</h3>

* Stream API는 for문, while문에 대한 대안을 제시한다. 아래 코드를 보자.
```java
for(String string : list) {
    if(string.contains("a")) {
        return true;
    }
}
```

* 위 코드를 Stream이 제공하는 메소드로 변환하면 아래와 같다.
```java
boolean isExist = list.stream().anyMatch(element -> element.contains("a"));
```

<h3>Filtering</h3>

* `filter()` 메소드는 Stream에 있는 원소들에 대해 특정 필터를 두고, 필터를 통과하는 원소만 반환하게 해준다.
```java
ArrayList<String> list = new ArrayList<>();
list.add("One");
list.add("OneAndOnly");
list.add("Derek");
list.add("Change");
list.add("Italy");
list.add("Italy");
list.add("Thursday");
list.add("");
list.add("");
```

* 아래의 코드는 `List<String>`으로부터 `Stream<String>`을 생성함과 동시에, "d"를 포함하는 원소만으로 이루어지게 해준다.
```java
Stream<String> streams = list.stream().filter(element -> element.contains("d"));
```

<h3>Mapping</h3>

* `map()` 메소드를 활용하면, 새로운 타입의 원소들로 이루어진 `Stream`으로 만들 수 있다.   
  예를 들어, 아래 코드는 기존에 `String`으로 이루어진 `List`를 `Path`로 이루어진 `Stream`으로 만들어준다.
```java
List<String> uris = new ArrayList<>();
uris.add("C:\\example.txt");
Stream<Path> stream = uris.stream().map(uri -> Paths.get(uri));
```

* 만약 `Stream`의 원소들이 내부에 또다른 클래스를 두고 있고, 이 내부의 클래스에 대한 `Stream`을   
  만들고 싶다면, `flatMap()` 메소드를 사용하면 된다.
```java
// Detail 클래스 안에는 List<String> 타입의 parts 멤버 변수가 있다.
List<Detail> details = new ArrayList<>();
details.add(new Detail());
Stream<String> stream = details.stream().flatMap(detail -> detail.getParts().stream());
```

<h3>Matching</h3>

* Stream API는 각 원소에 대한 특정 검증(validation)을 지원한다.   
  이를 지원하는 메소드로는 `anyMatch()`, `allMatch()`, `noneMatch()`가 있다.
```java
boolean isValid = list.stream().anyMatch(element -> element.contains("h")); // 원소 중 하나라도 h를 포함하면 true 반환
boolean isValidOne = list.stream().allMatch(element -> element.contains("h"));  // 모든 원소가 h를 포함하면 true 반환
boolean isValidTwo = list.stream().noneMatch(element -> element.contains("h"));  // 모든 원소가 h를 포함하지 않으면 true 반환
```

* 원소가 없는 `Stream`에 대해서, `allMatch()` 메소드는 모든 제약에 대해 true를 반환한다.
```java
Stream.empty().allMatch(Objects::nonNull);  // true
```

* 마찬가지로, 원소가 없는 `Stream`에 대해 `anyMatch()`는 모든 제약에 대해 false를 반환한다.
```java
Stream.empty().anyMatch(Objects::nonNull);  // false
```

* 위의 코드가 false를 반환하는 것은 해당 제약을 만족하는 원소가 하나도 없기 때문이다.

<h3>Reduction</h3>

* Stream API는 `Stream#reduce()` 메소드를 이용해 원소를 줄인 `Stream`을 만들어준다.   
  `reduce()` 메소드는 두 개의 파라미터가 있는데, 첫 번째 파라미터는 Start Value이며, 두 번째 파라미터는   
  함수로, 수행할 함수를 받는다.
```java
List<Integer> integers = Arrays.asList(1, 1, 1);
Integer reduced = integers.stream().reduce(23, (a, b) -> a + b);
```

* 위 코드의 결과로 reduced는 26(23 + 1 + 1 + 1)가 된다.

<h3>Collecting</h3>

* Reduction은 `Stream#collect()` 메소드로도 수행될 수도 있다.   
  이 메소드는 `Stream`을 `Collection` 또는 `Map`으로 변환하게 하는 작업을 수월하게 해준다.   
  이 작업에는 collecting 작업에 대한 대부분을 지원하는 `Collectors` 클래스를 주로 같이 활용한다.

* 예를 들어, 아래 코드는 `Stream<String>`을 `List<String>`으로 변환해준다.
```java
List<String> resultList = list.stream().map(element -> element.toUpperCase()).collect(Collectors.toList());
```
<hr/>

<h1>Stream 더 깊이 파고들기</h1>

<h2>Stream 객체 생성하기</h2>

<h3>비어 있는(Empty) Stream 생성</h3>

* `empty()` 메소드가 없는 비어 있는 `Stream` 객체를 생성해준다.
```java
Stream<String> streamEmpty = Stream.empty();
```

* `empty()` 메소드는 주로 원소가 없는 `Stream`에 대해 null을 반환하는 것을 방지하는 용도로 사용된다.
```java
public Stream<String> streamOf(List<String> list) {
    return list == null || list.isEmpty() ? Stream.empty() : list.stream();
}
```

<h3>Collection으로부터 Stream 생성</h3>

* `Stream`은 `Collection`들의 어느 타입에 대해서도 생성될 수 있다.
```java
Collection<String> collection = Arrays.asList("a", "b", "c");
Stream<String> streamOfCollection = collection.stream();
```

<h3>배열로부터 Stream 생성</h3>

* 배열(Array) 또한 `Stream`의 제공자가 될 수 있다.
```java
Stream<String> streamOfArray = Stream.of("a", "b", "c");

String[] arr = new String[]{"a", "b", "c"};
Stream<String> streamOfArrayFull = Arrays.stream(arr);
Stream<String> streamOfArrayPart = Arrays.stream(arr, 1, 3);
```

<h3>Stream.builder()</h3>

* 만약 `Stream`의 타입으로 들어가는 원소가 Builder Pattern을 사용한다면,   
  __원하는 타입을 무조건 명시해야 한다__. 만약 명시를 안한다면 `Stream<Object>`를 반환한다.
```java
Stream<String> streamBuilder = Stream.<String>builder().add("a").add("b").build();
```

<h3>Stream.generate()</h3>

* `generate()` 메소드는 `Stream`의 생성을 위해 `Supplier<T>` 타입을 원소로 받는다.   
  결과적으로 나오는 `Stream`은 무한정일 수 있으므로, 개발자는 원하는 size를 지정해야 한다.   
  그렇지 않을 경우 `generate()` 메소드는 메모리 초과가 날 때 까지 작동할 것이다.

* 아래 코드는 "element"를 원소로 가지고, 원소의 개수가 10개인 `Stream`을 반환한다.
```java
Stream<String> streamGenerated = Stream.generate(() -> "element").limit(10);
```

<h3>Stream.iterate()</h3>

* 무제한 길이의 `Stream`을 생성하는 또다른 방법은 `iterate()` 메소드를 사용하는 것이다.   
  `iterate()`의 첫 번째 파라미터의 타입에 맞게 `Stream`이 생성된다.   
  `iterate()`의 두 번째 파라미터는 이전 원소에 대해 특정 작업을 수행할 함수가 들어간다
```java
Stream<Integer> streamIterated = Stream.iterate(40, n -> n + 2).limit(20);
```

* 위의 예시로는, `Stream`에 있는 두 번째 원소는 42가 된다.

<h3>Primitive Type으로 이루어진 Stream</h3>

* Java 8은 int, double, long에 대해서 `Stream`을 생성할 수 있게 해준다.   
  `Stream<T>`가 Generic Interface이고, Generic은 Primitive Type을 타입 파라미터로 지정할 수 없기 때문에   
  `IntStream`, `DoubleStream`, `LongStream`의 세 가지 특별한 인터페이스들이 제공된다.

* 이 3개의 인터페이스를 사용하면 불필요한 auto-boxing 작업 등이 불필요하다.
```java
IntStream intStream = IntStream.range(1, 3);
LongStream longStream = LongStream.rangeClosed(1, 2);
```

* `range(int startInclusive, int endExclusive)`는 순서가 정해진 `Stream`을 생성한다.   
  즉, startInclusive 부터 endExclusive 전까지 1씩 증가하는 원소들로 이루어진 `Stream`을 반환한다.

* `rangeClosed(int startInclusive, int endInclusive)`는 `range()`와 마찬가지의 작업을 수행하지만,   
  endInclusive 까지 작업을 수행한다.

* 즉, 위의 intStream과 longStream의 원소들은 같다. (1, 2)
<hr/>

<h2>Stream 참조하기</h2>

* `Stream` 객체를 생성하고 참조하는 것은 `Intermedia Operation`만 수행한다면 가능하다.   
  만약 `Terminal Operation`을 수행한다면, 해당 `Stream` 객체를 참조할 수 없다.

* 이를 이해하기 위해, `Stream`을 다루는 최선의 방법이 chaining이라는 것을 배제해보자.   
  우선, 아래 코드는 적절한 코드이다.
```java
Stream<String> stream = Stream.of("a", "b", "c").filter(element -> element.contains("b"));
Optional<String> anyElement = stream.findAny();
```

* 하지만 같은 객체에 대해 `Terminal Operation`을 수행하고 참조하는 것은 `IllegalStateException`을 발생시킨다.
```java
Optional<String> firstElement = stream.findFirst();
```

* `IllegalStateException`은 `RuntimeException`을 상속하므로, 컴파일러는 해당 예외에 대한 처리를 강요하지 않는다.   
  따라서 __Java 8 Stream은 재사용이 불가하다는 것__ 을 숙지해야 한다.

* 위의 코드를 정상적으로 작동하게 하려면 아래와 같이 작성해야 한다.
```java
List<String> elements = Stream.of("a", "b", "c").filter(element -> element.contains("b"))
    .collect(Collectors.toList());

Optional<String> anyElement = elements.stream().findAny();
Optional<String> firstElement = elements.stream().findFirst();
```
<hr/>

<h2>Stream Pipeline</h2>

* 특정 데이터들에 대해 일련의 작업들을 수행하기 위해서는 세 개의 부분들이 필요한데, 이는 각각   
  `source`, `Intermediate Operation(s)`, `Terminal Operation`이다.

* `Intermediate Operation`은 수정된 새로운 `Stream`을 반환한다.   
  예를 들어 기존 `Stream`에 대해 몇 개의 원소를 제외한 새로운 `Stream`을 생성하려면 `skip()` 메소드가 사용되어야 한다.
```java
Stream<String> onceModifiedStream = Stream.of("abcd", "bbcd", "cbcd").skip(1);
```

* 만약 1개 이상의 수정 작업이 필요하다면, `Intermediate Operation`들은 chaining이 가능하다.   
  기존의 `Stream<String>`의 모든 원소에 대해 처음 몇개의 글자들로 이루어진 새로운 `Stream`을 만든다고 해보자.
```java
Stream<String> twiceModifiedStream = stream.skip(1).map(element -> element.substring(0, 3));
```

* `map()` 메소드는 함수를 파라미터로 받는다.

* `Stream`은 그 자체로는 무쓸모하다. `Stream`을 사용하는 이유는 특정 원소들에 대해 일련의 작업을 수행하여   
  원하는 결과로 이루어진 객체를 만들어내는 것이다.

* __`Stream` 하나는 무조건 단 하나의  `Terminal Operation`이 수행될 수 있다__.

* 가장 올바르고 쉬운 방법으로 `Stream`을 다루는 것은 stream pipeline, 즉 Stream Source에 대한   
  `Intermediate Operation` method chaining을 사용하고 마지막으로 `Terminal Operation`을 수행하는 것이다.
```java
List<String> list = Arrays.asList("abc1", "abc2", "abc3");
long size = list.stream().skip(1).map(element -> element.substring(0, 3)).sorted().count();
```
<hr/>



<a href="https://www.baeldung.com/java-8-streams-introduction">참고 링크1</a>
<a href="https://www.baeldung.com/java-8-streams">참고 링크2</a>