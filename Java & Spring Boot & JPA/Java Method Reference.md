# Java Method Reference

- Java 8의 등장에서 새로운 기능들 중 하나로 꼽히는 것이 Lambda 표현식이었다.  
  Method Reference 또한 Lambda표현 중 하나로, 때로 복잡한 Lambda 표현식을 더 편하게 할 수 있다.

- Method Reference에는 아래의 4가지 종류가 있다.
  - Static(정적) 메소드
  - 특정 객체의 인스턴스화 메소드
  - 추상 클래스의 인스턴스화 메소드
  - 생성자

<h2>Static Method</h2>

- 우선, 문자열(String)으로 구성된 리스트(`List`)의 각 원소들을 대문자화 하는 예시를 살펴보자.

```java
List<String> messages = Arrays.asList("hello", "sangwoo", "welcome");
```

- 위의 messages에 있는 각 원소들을 대문자화 하는 방법으로, Lambda 표헌식을 사용하면 아래와 같다.

```java
messages.forEach((word) -> StringUtils.capitalize(word));
```

- 하지만, `capitalize()` 메소드는 `StringUtils`의 static method이기에, 아래와 같이 표현할 수 있다.

```java
message.forEach(StringUtils::capitalize);
```

- 바로 위 예시가 Method Reference를 사용한 것이다.  
  **Method Reference가 `::` 연산자를 사용한다는 것을 알 수 있다.**

<hr/>

<h2>특정 객체의 인스턴스화 메소드</h2>

- 아래와 같이 간단한 `Bicycle`와 `BicycleComparator` 클래스가 있다고 하자.

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bicycle {
    private String brand;
    private Integer frameSize;
}

public class BicycleComparator implements Comparator {
    @Override
    public int compare(Bicycle a, Bicycle b) {
        return a.getFrameSize().compareTo(b.getFrameSize());
    }
}
```

- 이제 아래와 같이 `Bicycle` 객체의 frameSize를 비교하기 위한 `BicycleComparator` 인스턴스를 생성해보자.

```java
BicycleComparator bikeFrameSizeComparator = new BicycleComparator();
```

- 아래와 같이 Lambda 표현식을 사용해서 `Bicycle` 객체들로 이루어진 리스트에서, frameSize를 기준으로  
  정렬할 수 있을 것이다. 하지만, 람다식에서 꼭 2개의 인자를 명시해줘야 한다.

```java
createBicyclesList().stream()
    .sorted((a, b) -> bikeFrameSizeComparator.compare(a, b));
```

- 이 때 Method Reference를 사용하면, 아래와 같이 코드가 매우 간결해진다.

```java
createBicyclesList().stream()
    .sorted(bikeFrameSizeComparator::compare);
```

<hr/>

<h2>임의 객체의 메소드</h2>

- 이번 예시는 바로 전 예시와 매우 유사하지만, 특정 객체를 인스턴스화할 필요가 없다는 차이점이 있다.  
  우선, 정렬을 하고 싶은 정수로 이루어진 리스트를 만들어보자.

```java
List<Integer> numbers = Arrays.asList(5, 3, 50, 24, 40, 2, 9, 18);
```

- 만약 기본적인 Lambda 표현식을 사용하면, 2개의 인자들을 명시해줘야 하지만,  
  Method Reference를 사용하면 더 직관적이고 간결하게 표현할 수 있다.

```java
// 기본적인 Lambda 표현식
numbers.stream().sorted((a, b) -> a.compareTo(b));

// Method Reference
numbers.stream().sorted(Integer::compareTo);
```

<hr/>

<h2>생성자</h2>

- Method Reference에서는 객체의 생성자를 Static Method를 사용한 것처럼 사용할 수 있다.  
  유일한 차이점은 `new` 키워드를 사용한다는 것이다.

* 우선 `Bicycle`의 brand 필드를 담은 리스트를 생성해보자.

```java
List<String> bikeBrands = Arrays.asList("A", "B", "C", "D");
```

- 그 다음 `Bicycle` 클래스에 생성자를 추가하자.

```java
public Bicycle(String brand) {
    this.brand = brand;
    this.frameSize = 0;
}
```

- 이제 bikeBrands 변수로 `Bicycle` 객체들로 이루어진 배열을 생성하려면 아래와 같이 하면 된다.

```java
Bicycle[] bikes = bikeBrands.stream()
    .map(Bicycle::new)
    .toArray(Bicycle[]::new);
```

- 위 코드에서 `Bicycle`의 생성자도 호출하고, 배열의 생성자도 호출했음을 확인하자.

<hr/>

<h2>다른 예시 및 한계점</h2>

- 위에서 본 결과, Method Reference를 사용하면 코드의 목적을 명확히 드러냄과 동시에 가독성이 뛰어나게  
  작성할 수 있음을 알게 되었다. 하지만 Method Reference를 모든 Lambda 표현식에 대해 적용할 수는 없다.

- Method Reference의 가장 큰 한계점은 가장 큰 이점과도 상응하는데, 바로 **이전 표현식의 결과가**  
  **Method Reference의 시그니쳐와 일치해야 한다** 는 것이다.  
  아래 예시를 보자.

```java
createBicyclesList().forEach(bike -> System.out.printf(
    "Bike brand is '%s' and frame size is '%d'\n",
    b.getBrand(), b.getFrameSize()
));
```

- 위의 Lambda 표현식은 Method Reference로 사용될 수 없다.  
  그 이유는 위의 경우에는 `printf()` 메소드가 3개의 매개 변수를 필요하기 때문이다.  
  하지만 `createBicyclesList().forEach()` 내부에서 Method Reference를 사용하면  
  `Bicycle` 객체 하나에 대해서만 사용할 수 있다.

- 마지막으로, 아무런 동작도 수행하지 않는 no-operation function을 생성해보자.

```java
private static <T> void doNothingAtAll(Object... o) {
}
```

- 위에서 작성한 `doNothingAtAll()`도 `printf()`와 마찬가지로 가변인자를 받기 때문에  
  Lambda 표현식에서만 사용할 수 있다.

```java
createBicyclesList()
    .forEach((bike) -> MethodReferenceExample.doNothingAtAll(bike));
```

<hr/>
