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

<h2>Lazy Invocation</h2>

* `Intermediate Operation`들은 Lazy 하다. 이는 즉 이 작업들은 `Terminal Operation`이 수행되기 위해   
  필수적으로 실행되어야 할 때만 수행된다는 뜻이다.

* 이를 이해하기 위해, `wasCalled()` 메소드가 있다고 가정하자. `wasCalled()` 메소드는 해당 메소드가 호출될 때마다   
  내부적으로 카운터를 1씩 증가시킨다.

```java
private long counter;

private void wasCalled() { counter++; }
```

* 이제 `filter()`에서 `wasCalled()`를 호출해보자.
```java
List<String> list = Arrays.asList("abc1", "abc2", "abc3");
counter = 0;
Stream<String> stream = list.stream().filter(element -> { wasCalled(); return element.contains("2");});
```

* 상식적으로는 위 코드가 수행된 후 counter값은 0에서 3으로 증가해야 맞지만, 실제로는 그대로 0이다.   
  그 이유는 `Terminal Operation`이 호출되지 않았기 때문에 `filter()`가 아예 호출되지 않았기 때문이다.

* 이제 위 코드에 대해 `map()`과 `Terminal Operation` 중 하나인 `findFirst()`를 수행해보자.
```java
Optional<String> stream = list.stream().filter(element -> {
    log.info("filter() was called.");
    return element.contains("2");
}).map(element -> {
    log.info("map() was called.");
    return element.toUpperCase();
}).findFirst();
```

* 위 코드의 수행 결과, `filter()`는 2번 호출되었고, `map()`은 1번 호출되었다.   
  이는 pipeline이 수직적으로 작동하기 때문이다. 위 예시에서 첫 번째 원소는 `filter()` 메소드의 필터를 통과하지 못했고,   
  두 번째 원소에 대해 `filter()` 메소드가 호출되었다. 두 번째 원소는 필터를 통과했기 때문에   
  세 번째 원소에 대해 `filter()`를 호출하지 않고 바로 pipeline을 타고 다음 작업인 `map()`을 호출한 것이다.

* 마지막으로 `findFirst()` 메소드는 단 하나의 원소(첫 번째 원소)로 만족하기 때문에,   
  위 코드에서는 Lazy Invocation이 동작하여 2번의 메소드 호출을 피한 것이다. (`filter()`, `map()`).
<hr/>

<h2>작업 수행의 순서</h2>

* 성능의 관점에서 본다면 최적의 순서는 method chaining 중 필요한 작업만 수행되는 것이다.

```java
long size = list.stream().map(element -> {
    wasCalled();
    return element.substring(0, 3);
}).skip(2).count();
```

* 위 코드의 수행 결과, `wasCalled()`는 3번 호출되지만, size는 1이 된다.   
  결국 결론적으로 `Stream`은 1개의 원소를 가지지만, 3번의 `map()` 메소드가 호출된 것이다.

* 이 때, `skip()`과 `map()` 메소드의 호출 순서를 바꾼다면, `wasCalled()`는 한 번 호출되며,   
  마찬가지로 `map()` 메소드도 1번만 호출될 것이다.
```java
long size = list.stream().skip(2).map(element -> {
    wasCalled();
    return element.substring(0, 3);
}).count();
```

* 이는 규칙을 도출하는데, __`Intermediate Operation` 중 `Stream`의 크기를 조절하는 함수들은 모든 원소 각각에 대해 호출되는__   
  __메소드의 호출보다 앞서 호출되어야 한다__ 는 것을 알 수 있다.

* 즉, `skip()`, `filter()`, `distinct()`와 같은 메소드들을 Stream Pipeline의 최상위에 놓는 것이 효율적이다.
<hr/>

<h2>Stream Reduction</h2>

* Stream API는 `Stream`을 Primitive Type으로 변환해주는 `count()`, `max()`, `min()`, `sum()`등의   
  메소드를 제공한다. 하지만 이 메소드들은 미리 정의된 동작에 맞추어 작동한다.   
  만약 개발자가 직접 Stream의 reduction 메커니즘을 작성해야 한다면 어떨까?   
  이를 가능하게 해주는 메소드는 2개가 있는데, 바로 `reduce()`와 `collect()` 메소드이다.

<h3>reduce() 메소드</h3>

* `reduce()`는 총 3가지로 오버로딩 되어 있는데, 시그니처와 리턴 타입으로 구분된다. 아래는 파라미터 목록이다.
  * identity : 초기값
  * accumulator : 원소들에 대해 작업할 함수, reducing의 단계별로 accumulator는 새로운 값을 반환하므로,   
    새로운 값들의 개수는 stream의 size와 동일하며, 마지막 value 값만 유용하다.
  * combiner : accumulator의 결과에 대해 적용되는 함수, combiner는 __병렬로만 호출__ 되며, accumulator의   
    결과를 다른 스레드에서 처리하도록 해준다.

* 아래는 위의 3가지를 모두 하나씩 다룬 코드이다.
```java
// 1, 2, 3을 reduce하여 6 반환
OptionalInt reduced = IntStream.range(1, 4).reduce((a, b) -> a + b);

// 1, 2 ,3에 대하여 accumulator의 초기값으로 10 지정.
// 즉 결과는 16이 된다.
int reducedTwoParams = IntStream.range(1, 4).reduce(10, (a, b) -> a + b);
```

```java
// Stream이 parallel이 아니기에 combiner가 호출되지 않으므로
// 결과값은 16이 된다.
int reducedParams = Stream.of(1, 2, 3)
    .reduce(10, (a, b) -> a + b, (a, b) -> {
        log.info("combiner was called.");
        return a + b;
    });
```

* 위 코드를 수행하면, 로그가 찍히지 않는다. 즉, combiner가 호출되지 않은 것이다.   
  combiner가 호출되게 하려면, stream은 병렬이어야 한다.
```java
int reducedParallel = Arrays.asList(1, 2, 3).parallelStream()
    .reduce(10, (a + b) -> a + b, (a + b) -> {
        log.info("combiner was called.");
        return a + b;
    });
```

* 위 코드의 결과 도출 순서는 아래와 같다.
  * (1) accumulator가 병렬적으로 호출되기에 Stream의 원소는 11(10+1), 12(10+2), 13(10+3)이다.
  * (2) combiner가 결과를 합쳐 11 + 12 + 13인 36이 결과값으로 나온다.

<h3>collect() 메소드</h3>

* `Stream`에 대한 reduction은 `collect()`로도 수행될 수 있다.   
  `collect()` 메소드는 `Collector` 타입을 매개변수로 받는데, `Collector`는 reduction의   
  동작 방식이 정의되어 있다.

* 대부분의 `Collector` 작업들은 `Collectors`에 정의되어 있다.

* 아래의 코드가 기본적으로 있다고 하자.
```java
List<Product> productList = Arrays.asList(new Product(23, "potatoes"),
  new Product(14, "orange"), new Product(13, "lemon"),
  new Product(23, "bread"), new Product(13, "sugar"));
```

* 위 productList를 `List<String>`으로 변환해보면 아래와 같다.
```java
String listToString = productList.stream().map(Product::getName).collect(Collectors.toList());
```

* 이번에는 위의 productList의 `Product.price`의 평균값과 총 합을 구해보자.
```java
double averagePrice = productList.stream().collect(Collectors.averagingInt(Product::getPrice));

int summingPrice = productList.stream().collect(Collectors.summingInt(Product::getPrice));
```

* `avaragingXX()`, `summingXX()`, `summarizingXX()`는 int, long, double과 그들의 Wrapper 클래스인   
  `Integer`, `Double`, `Long`에 대해서도 작동한다.

* `Stream`의 원소들에 대해 지정된 함수로 grouping을 수행할 수도 있다.
```java
Map<Integer, List<Product>> collectorMapOfLists = productList.stream()
    .collect(Collectors.groupBy(Product::getPrice));
```

* `Stream`의 원소들을 특정 조건에 따라 grouping할 수도 있다.
```java
Map<Boolean, List<Product>> mapPartitioned = productList.stream()
    .collect(Collectors.partitionBy(element -> element.getPrice() > 15));
```

* Collector에게 추가적인 작업을 수행하게 할 수도 있다.
```java
Set<Product> unmodifiableSet = productList.stream()
    .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
```

* 위 코드는 `Stream`을 `Set`으로 변환한 후, 그 `Set`을 수정 불가하게 했다.

* `Collector` 직접 만들기 : 특정 경우에는 기존에 제공되는 `Collector` 대신에 필요한 `Collector`를 직접 만들어야 한다.   
  이를 해결하는 가장 쉬운 방법은 `Collector#of()`를 사용하는 것이다.
```java
Collector<Product, ?, LinkedList<Product>> toLinkedList = 
    Collector.of(LinkedList::new, LinkedList::add,
    (first, second) -> {
        first.addAll(second);
        return first;
    });

LinkedList<Product> linkedListOfProducts = productList.stream().collect(toLinkedList);
```

* 위 코드는 `Collector`의 인스턴스를 `LinkedList<Person>`으로 reduce 했다.
<hr/>

<a href="https://www.baeldung.com/java-8-streams-introduction">참고 링크1</a>
<a href="https://www.baeldung.com/java-8-streams">참고 링크2</a>