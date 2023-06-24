# 확장할 수 있는 열거 타입이 필요하면 인터페이스를 사용하라

- 대부분의 상황에서 열거 타입을 확장하려는 건 좋지 않은 생각이다. 확장한 타입의 원소는  
  기반 타입의 원소로 취급하지만 그 반대는 성립하지 않기 때문이다. 또한 기반 타입과  
  확장된 타입들의 원소 모두를 순회할 방법도 마땅치 않다. 또한 확장성을 높이려면 고려할 요소가  
  늘어나 설계와 구현이 더 복잡해진다.

- 그런데 확장할 수 있는 열거 타입이 어울리는 쓰임이 최소한 하나는 있다.  
  바로 연산 코드(operation code)이다. 연산 코드의 각 원소는 특정 기계가 수행하는 연산을  
  뜻한다.(ex. [Item 34](https://github.com/sang-w0o/Study/blob/master/Programming%20Paradigm/Effective%20Java/5.%20%EC%97%B4%EA%B1%B0%20%ED%83%80%EC%9E%85%EA%B3%BC%20%EC%96%B4%EB%85%B8%ED%85%8C%EC%9D%B4%EC%85%98/Item%2034.%20int%20%EC%83%81%EC%88%98%20%EB%8C%80%EC%8B%A0%20%EC%97%B4%EA%B1%B0%20%ED%83%80%EC%9E%85%EC%9D%84%20%EC%82%AC%EC%9A%A9%ED%95%98%EB%9D%BC.md)의 `Operation` 타입) 이따끔 API가 제공하는 기본 연산 외에  
  사용자 확장 연산을 추가할 수 있도록 열어줘야 할 때가 있다.

- 다행이 열거 타입으로 이 효과를 내는 멋진 방법이 있다. 기본 아이디어는 열거 타입이 임의의  
  인터페이스를 구현할 수 있다는 사실을 이용하는 것이다. 연산 코드용 인터페이스를 정의하고  
  열거 타입이 이 인터페이스를 구현하게 하면 된다. 이때 열거 타입이 그 인터페이스의 표준 구현체  
  역할을 한다. 아래는 [Item 34](https://github.com/sang-w0o/Study/blob/master/Programming%20Paradigm/Effective%20Java/5.%20%EC%97%B4%EA%B1%B0%20%ED%83%80%EC%9E%85%EA%B3%BC%20%EC%96%B4%EB%85%B8%ED%85%8C%EC%9D%B4%EC%85%98/Item%2034.%20int%20%EC%83%81%EC%88%98%20%EB%8C%80%EC%8B%A0%20%EC%97%B4%EA%B1%B0%20%ED%83%80%EC%9E%85%EC%9D%84%20%EC%82%AC%EC%9A%A9%ED%95%98%EB%9D%BC.md)의 `Operation` 타입을 확장할 수 있게 만든 코드다.

```java
public interface Operation {
    double apply(double x, double y);
}

public enum BasicOperation implements Operation {
    PLUS("+") {
        public double apply(double x, double y) {
            return x + y;
        }
    },
    MINUS("-") {
        public double apply(double x, double y) {
            return x - y;
        }
    },
    TIMES("*") {
        public double apply(double x, double y) {
            return x * y;
        }
    },
    DIVIDE("/") {
        public double apply(double x, double y) {
            return x / y;
        }
    };

    private final String symbol;

    BasicOperation(String symbol) { this.symbol = symbol; }

    @Override public String toString() { return symbol; }
}
```

- 열거 타입인 `BasicOperation`은 확장할 수 없지만 인터페이스인 `Operation`은 확장할 수 있고,  
  이 인터페이스를 연산의 타입으로 사용하면 된다. 이렇게 하면 `Operation`을 구현한 또 다른 열거  
  타입을 정의해 기본 타입인 `BasicOperation`을 대체할 수 있다. 예를 들어 앞의 연산 타입을 이용해  
  지수 연산(`EXP`)와 나머지 연산(`REMAINDER`)을 추가해보자. 이를 위해 우리가 할 일은  
  `Operation` 인터페이스를 구현하는 열거 타입을 작성하는 것 뿐이다.

```java
public enum ExtendedOperation implements Operation {
    EXP("^") {
        public double apply(double x, double y) {
            return Math.pow(x, y);
        }
    },
    REMAINDER("%") {
        public double apply(double x, double y) {
            return x % y;
        }
    };

    private final String symbol;

    ExtendedOperation(String symbol) { this.symbol = symbol; }

    @Override public String toString() { return symbol; }
}
```

- 새로 작성한 연산은 기존 연산을 쓰던 곳이면 어디든 쓸 수 있다.  
  `BasicOperation`이 아닌 `Operation` 인터페이스를 사용하도록 작성되어 있기만 하면 된다.  
  `apply()`가 `Operation` 인터페이스에 선언되어 있으니 열거 타입에 따로 추상 메소드로  
  선언하지 않아도 된다.

- 개별 인터페이스 수준에서뿐 아니라 타입 수준에서도, 기본 열거 타입 대신 확장된 열거 타입을 넘겨  
  확장된 열거 타입의 원소 모두를 사용하게 할 수도 있다. 예를 들어 아래 코드는 `ExtendedOperation`의  
  모든 원소를 테스트하는 코드이다.

```java

public class Client {
    public static void main(String[] args) {
        double x = Double.parseDouble(args[0]);
        double y = Double.parseDouble(args[1]);
        test(ExtendedOperation.class, x, y);
    }

    private static <T extends Enum<T> & Operation> void test(Class<T> opEnumtype, double x, double y) {
        for(Operation op: opEnumType.getEnumConstants()) {
            System.out.printf("%f %s %f = %f%n", x, op, y, op.apply(x, y));
        }
    }
}
```

- `main()`에서는 `test()`에 `ExtendedOperation`의 Class 리터럴을 넘겨 확장된 연산들을 알려준다.  
  여기서 Class 리터럴은 한정된 타입 토큰 역할을 한다. opEnumType 매개변수의 선언 `<T extends Enum<T> & Operation> Class<T>`는  
  복잡한데, _`Class` 객체가 열거 타입인 동시에 `Operation`의 하위 타입이어야 한다._ 는 뜻이다.  
  열거 타입이어야 원소를 순회할 수 있고, `Operation`이어야 원소가 뜻하는 연산을 수행할 수 있기 때문이다.

- 두 번째 대안은 `Class` 객체 대신 한정적 와일드카드 타입을 넘기는 방법이다.

```java
public class Client {
    public static void main(String[] args) {
        double x = Double.parseDouble(args[0]);
        double y = Double.parseDouble(args[1]);
        test(Arrays.asList(ExtendedOperation.values()), x, y);
    }

    private static void test(Collection<? extends Operation> opSet, double x, double y) {
        for(Operation op: opSet) {
            System.out.printf("%f $s %f = %f%n", x, op, y, op.apply(x, y));
        }
    }

}
```

- 위 코드는 그나마 덜 복잡하고 `test()` 메소드가 살짝 더 유연해졌다. 다시 말해, 여러 구현 타입의  
  연산을 조합해 호출할 수 있게 되었다. 반면 특정 연산에서는 `EnumSet`과 `EnumMap`을 사용하지 못한다.

- 인터페이스를 이용해 확장 가능한 열거 타입을 흉내 내는 방식에도 한 가지 사소한 문제가 있는데, 바로  
  **열거 타입끼리 구현을 상속할 수 없다**는 점이다. 아무 상태에도 의존하지 않는 경우에는 default 구현을  
  이용해 인터페이스에 추가하는 방법이 있다. 반면 `Operation` 예시에는 연산 기호를 저장하고 찾는 로직이  
  `BasicOperation`과 `ExtendedOperation` 모두에 들어가야만 한다. 이 경우에는 중복량이 많지 않으니  
  문제되진 않지만, 공유하는 기능이 많다면 그 부분을 별도의 helper 클래스나 정적 helper 메소드로 분리하는 방식으로  
  코드 중복을 없앨 수 있을 것이다.

- Java 라이브러리도 이번 아이템에서 본 패턴을 사용한다. 그 예시로 `java.nio.file.LinkOption` 열거 타입은  
  `CopyOption`과 `OpenOption` 인터페이스를 구현했다.

---

## 핵심 정리

- 열거 타입 자체는 확장할 수 없지만, **인터페이스와 그 인터페이스를 구현하는 기본 열거 타입을 함께 사용해**  
  **같은 효과를 낼 수 있다.** 이렇게 하면 클라이언트는 이 인터페이스를 구현해 자신만의 열거 타입(혹은 다른 타입)을  
  만들 수 있다. 그리고 API가 기본 열거 타입을 직접 명시하지 않고 인터페이스 기반으로 작성되었다면 기본 열거 타입의  
  인스턴스가 쓰이는 모든 곳을 새로 확장한 열거 타입의 인스턴스로 대체해 사용할 수 있다.

---
