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
