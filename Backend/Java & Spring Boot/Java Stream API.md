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
