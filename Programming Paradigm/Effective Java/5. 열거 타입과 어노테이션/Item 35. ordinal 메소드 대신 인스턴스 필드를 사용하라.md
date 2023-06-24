# ordinal 메소드 대신 인스턴스 필드를 사용하라

- 대부분의 열거 타입 상수는 자연스럽게 하나의 정수값에 대응된다.  
  그리고 모든 열거 타입은 해당 상수가 그 열거 타입에서 몇 번째 위치인지를  
  반환하는 `ordinal()`이라는 메소드를 제공한다. 이런 이유로 열거 타입  
  상수와 연결된 정수값이 필요하면 `ordinal()`을 사용하고 싶은 유혹이 빠진다.  
  아래 코드는 합주단의 종류가 연주자가 1명이 solo부터 10명인 dectet까지  
  정의한 열거 타입이다.

```java
public enum Ensemble {
  SOLO, DUET, TRIO, QUARTET, QUINTET, SEXTET, SEPTET, OCTET, DECTET;

  public int numberOfMusicians() { return ordinal() + 1; }
}
```

- 동작은 하지만 유지보수 하기가 끔직한 코드다. 상수 선언 순서를 바꾸는 순간  
  `numberOfMusicians()`가 오동작하며, 이미 사용 중인 정수와 값이 같은 상수는  
  추가할 방법이 없다. 예를 들어 8중주 상수가 이미 있으니 똑같이 8명이 연주하는  
  복4중주는 추가할 수 없다.

- 또한 값을 중간에 비워둘 수도 없다. 예를 들어 12명이 연주하는 3중 4중주를 추가한다 해보자.  
  그러려면 중간에 11명짜리 상수도 채워야 하는데, 11명으로 구성된 연주를 일컫는 이름은 없다.  
  그래서 3중 4중주를 추가하려면 쓰이지 않는 dummy 상수를 같이 추가해야만 한다.  
  코드가 깔끔하지 못할 뿐 아니라, 쓰이지 않는 값이 많아질수록 실용성이 떨어진다.

- 해결책은 간단하다. **열거 타입 상수에 연결된 값은 `ordinal()`로 얻지 말고,**  
  **인스턴스 필드에 저장하자.**

```java
public enum Ensemble {
  SOLO(1), DUET(2), TRIO(3), QUARTET(4), QUINTET(5), SEXTET(6), SEPTET(7),
  DOUBLE_QUARTET(8), NONET(9), DECTET(10), TRIPLE_QUARTET(12);

  private final int numberOfMusicians;
  Ensemble(int size) { this.numberOfMusicians = size; }
  public int numberOfMusicians() { return numberOfMusicians; }
}
```

- `Enum`의 API 문서를 보면 `ordinal()`에 이렇게 쓰여있다.

> 대부분 프로그래머는 이 메소드를 쓸 일이 없다. 이 메소드는 `EnumSet`과 `EnumMap` 같이  
> 열거 타입 기반의 범용 자료구조에 쓸 목적으로 설계되었다.

- 따라서 이런 용도가 아니라면 `ordinal()`을 절대 사용하지 말자.

---
