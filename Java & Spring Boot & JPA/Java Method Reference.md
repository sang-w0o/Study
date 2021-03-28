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
