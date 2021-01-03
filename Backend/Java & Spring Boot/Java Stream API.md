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



<a href="https://www.baeldung.com/java-8-streams-introduction">참고 링크1</a>
<a href="https://www.baeldung.com/java-8-streams">참고 링크2</a>