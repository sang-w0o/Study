# int 상수 대신 열거 타입을 사용하라

- 열거 타입은 일정 개수의 상수 값을 정의한 다음, 그 외의 값은 허용하지 않는 타입이다.  
  사계절, 태양계의 행성, 카드게임의 카드 종류 등이 좋은 예시다. Java에서 열거 타입을  
  지원하기 전에는 아래 코드처럼 정수 상수를 한 묶음 선언해서 사용하곤 했다.

```java
public static final int APPLE_FUJI = 0;
public static final int APPLE_PIPPIN = 1;
public static final int APPLE_GRANNY_SMITH = 2;

public static final int ORANGE_NAVEL = 0;
public static final int ORANGE_TEMPLE = 1;
public static final int ORANGE_BLOOD = 2;
```

- 정수 열거 패턴(int enum pattern) 기법에는 단점이 많다. 타입 안전을 보장할 방법이  
  없으며 표현력도 좋지 않다. 오렌지를 건네야 할 때 사과를 보내고 동등 연산자(`==`)로  
  비교하더라도 컴파일러는 아무런 경고 메시지를 출력하지 않는다.

```java
int i = (APPLE_FUJI - ORANGE_TEMPLE) / APPLE_PIPPIN;
```

- 사과용 상수의 이름은 모두 `APPLE_`로 시작하고 오렌지용 상수는 `ORANGE_`로 시작한다.  
  Java가 정수 열거 패턴을 위한 별도 namespace를 지원하지 않기 때문에 어쩔 수 없이 이처럼  
  접두어를 써서 이름 충돌을 방지하는 것이다. 예를 들어 영어로는 둘다 mercury인 수은과 수성의  
  이름을 각각 `ELEMENT_MERCURY`와 `PLANET_MERCURY`로 지어 구분하는 것이다.

- 정수 열거 패턴을 사용하는 프로그램은 깨지기 쉽다. 평범한 상수를 나열한 것 뿐이라 컴파일하면 그 값이  
  클라이언트 파일에 그대로 새겨진다. 따라서 상수의 값이 바뀌면 클라이언트도 반드시 다시 컴파일해야 한다.  
  다시 컴파일하지 않은 클라이언트는 실행이 되더라도 엉뚱하게 동작할 것이다.

- 정수 상수는 문자열로 출력하기가 다소 까다롭다. 그 값을 출력하거나 디버거로 살펴보면 의미가 아닌  
  단지 숫자로만 보여서 썩 도움이 되지 않는다. 같은 정수 열거 그룹에 속한 모든 상수를 한 바퀴  
  순회하는 방법도 마땅치 않다. 심지어 그 안에 상수가 몇 개인지 알 수도 없다.

- 정수 대신 문자열 상수를 사용하는 변형 패턴도 있다. 문자열 열거 패턴(string enum pattern)이라 하는  
  이 변형은 더 나쁘다. 상수의 의미를 출력할 수 있다는 점은 좋지만, 경험이 부족한 프로그래머가  
  문자열 상수의 이름 대신 문자열 값을 그대로 하드코딩하게 만들기 때문이다. 이렇게 하드코딩한  
  문자열에 오타가 있어도 컴파일러는 확인할 길이 없으니 자연스럽게 런타임 버그가 생긴다.  
  문자열 비교에 따른 성능 저하 역시 당연한 결과다.

- 다행이 Java는 열거 패턴의 단점을 말끔히 씻어주는 동시에 여러 장점을 안겨주는 대안을 제시했다.  
  바로 **열거 타입(enum type)** 이다. 아래는 열거 타입의 가장 단순한 형태다.

```java
public enum Apple { FUJI, PIPPIN, GRANNY_SMITH }
public enum Orange { NAVEL, TEMPLE, BLOOD }
```

- 겉보기에는 C, C++, C# 같은 다른 언어의 열거 타입과 비슷해보이지만, 보이는게 다가 아니다.  
  **Java의 열거 타입은 완전한 형태의 클래스**라서 다순한 정수 값 뿐인 다른 언어의 열거 타입보다  
  훨씬 강력하다.

- Java 열거 타입을 뒷받침하는 아이디어는 단순하다. **열거 타입 자체는 클래스이며, 상수 하나당 자신의**  
  **인스턴스를 하나씩 만들어 public static final 필드로 공개**한다. 열거 타입은 **밖에서 접근할 수**  
  **있는 생성자를 제공하지 않으므로 사실상 final 이다**. 따라서 클라이언트가 인스턴스를 직접 생성하거나  
  확장할 수 없으니 열거 타입 선언으로 만들어진 인스턴스들은 딱 하나씩만 존재함이 보장된다.  
  다시말해 **열거 타입은 인스턴스 통제된다**. 싱글턴은 원소가 하나뿐인 열거 타입이라 할 수 있고,  
  거구로 열거 타입은 싱글턴을 일반화한 형태라 할 수 있다.

- 열거 타입은 컴파일타임 타입 안전성을 제공한다. 만약 위 코드의 `Apple` 열거 타입을 매개변수로  
  받는 메소드를 선언했다면, 건네받은 참조는 null이 아니라면 `Apple`의 세 가지 값 중 하나임이  
  확실하다. 다른 타입의 값을 넘기려 하면 컴파일 오류가 난다. 타입이 다른 열거 타입 변수에  
  할당하려 하거나 다른 열거 타입의 값끼리 `==` 연산자로 비교하려는 꼴이기 때문이다.

- 열거 타입에는 각자의 namespace가 있어서 이름이 같은 상수도 평화롭게 공존한다.  
  열거 타입에 새로운 상수를 추가하거나 순서를 바꿔도 다시 컴파일하지 않아도 된다. 공개되는 것이 오직  
  필드명뿐이라, 정수 열거 패턴과 달리 상수 값이 클라이언트로 컴파일되어 각인되지 않기 때문이다.  
  마지막으로 열거 타입의 `toString()`은 출력하기 적절한 문자열을 내어준다.

- 이처럼 열거 타입은 정수 열거 패턴의 단점들을 해소해준다. 여기서 끝이 아니다. 열거 타입에는 임의의  
  메소드나 필드를 추가할 수 있고 임의의 인터페이스를 구현하게 할 수도 있다. `Object`의 메소드들을  
  높은 품질로 구현해놨고, `Comparable`과 `Serializable`을 구현했으며, 그 직렬화 형태도  
  웬만큼 변형을 가해도 문제 없이 동작하게끔 구현되어 있다.

- 그런데 열거 타입에 메소드나 필드를 추가해야 할 일이 있을까?  
  가볍게 시작해보자. 각 상수와 연관된 데이터를 해당 상수 자체에 내재시키고 싶다 해보자.  
  `Apple`과 `Orange`를 예로 들면, 과일의 색을 알려주거나 과일 이미지를 반환하는  
  메소드를 추가하고 싶을 수 있다. 열거 타입에는 어떠한 메소드도 추가할 수 있다.  
  가장 단순하게는 그저 상수 모음일 뿐인 열거 타입이지만, 실제로는 클래스이므로 고차원의  
  추상 개념 하나를 완벽히 표현해낼 수도 있는 것이다.

- 태양계의 8개 행성에는 질량, 반지름이 있고 그리고 이 둘을 이용해 표면중력을 계산할 수 있다.  
  따라서 어떤 객체의 질량이 주어지면 그 객체가 행성의 표면에 있을 때의 무게도 계산할 수 있다.  
  이를 열거 타입으로 나타내보자.

```java
public enum Planet {
  MERCURY(3.303e+23, 2.4397e6),
  VENUS(4.869e+24, 6.0518e6),
  EARTH(5.976e+24, 6.37814e6),
  MARS(6.421e+23, 3.3972e6),
  JUPITER(1.9e+27, 7.1492e7),
  SATURN(5.688e+26, 6.0268e7),
  URANUS(8.686e+25, 2.5559e7),
  NEPTUNE(1.024e+26, 2.4746e7);

  // 질량(kg)
  private final double mass;
  // 반지름(m)
  private final double radius;
  // 표면중력(m / s^2)
  private final double surfaceGravity;

  Planet(double mass, double radius) {
    this.mass = mass;
    this.radius = radius;
    surfaceGravity = G * mass / (radius * radius);
  }

  public double mass() { return mass; }
  public double radius() { return radius; }
  public double surfaceGravity() { return surfaceGravity; }

  public double surfaceWeight(double mass) {
    return mass * surfaceGravity;
  }
}
```

- 위처럼 거대한 열거 타입도 보다시피 만들기가 어렵지 않다.  
  **열거 타입 상수 각각을 특정 데이터와 연결지으려면 생성자에서 데이터를 받아 인스턴스 필드에**  
  **저장하면 된다.** 열거 타입은 근본적으로 불변이라 모든 필드는 final 이어야 한다. 필드를  
  public으로 선언해도 되지만, private으로 두고 별도의 public 접근자 메소드를 두는게 낫다.

- 한편, `Planet`의 생성자에서 표면중력(surfaceGravity)을 계산해 저장하는 이유는 단순히  
  최적화를 위함이다. 사실 질량과 반지름이 있으니 표면중력은 언제든 계산할 수 있다.  
  `surfaceWeight()`는 대상 객체의 질량을 입력받아 그 객체가 행성 표면에 있을 때의 무게를  
  반환한다.

- `Planet` 열거 타입은 단순하지만, 놀랍도록 강력하다. 어떤 객체의 지구에서의 무게를 입력받아  
  여덟 행성에서의 무게를 출력하는 코드를 아래처럼 단순하게 작성할 수 있다.

```java
public class WeightTable {
  public static void main(String[] args) {
    double earthWeight = Double.parseDouble(args[0]);
    double mass = earthWeight / Planet.EARTH.surfaceGravity();
    for(Planet p : Planet.values()) {
      System.out.printf("Your weight on %s is %f%n", p, p.surfaceWeight(mass));
    }
  }
}
```

- Java가 열거 타입을 지원하기 시작한 후로 2년이 지난 2006년 까지는 명왕성(Pluto)도  
  행성 대접을 받았다. 이제는 그렇지 않은데, 그러면 _열거 타입에서 상수를 하나 제거하려면 어떻게 해야할까?_  
  그 대답은 **제거한 상수를 참조하지 않는 클라이언트에는 아무런 영향이 없다.** 이다.  
  `WeightTable` 프로그램이라면 단지 출력하는 줄 수가 하나 줄어들 뿐이다.  
  그렇다면 제거된 상수를 참조하는 클라이언트는 어떻게 될까? 클라이언트 프로그램을 다시 컴파일하면  
  제거된 상수를 참조하는 줄에서 디버깅에 유용한 메시지를 담은 컴파일 오류가 발생할 것이다.  
  클라이언트를 다시 컴파일하지 않으면 런타임에, 역시 같은 줄에서 유용한 예외가 발생할 것이다.  
  정수 열거 패턴에서는 기대할 수 없는 가장 바람직한 대응이라 볼 수 있다.

- 열거 타입을 선언한 클래스 혹은 그 패키지에서만 유용한 기능은 private이나 package-private  
  메소드로 구현한다. 이렇게 구현된 열거 타입 상수는 자신을 선언한 클래스 혹은 패키지에서만  
  사용할 수 있는 기능을 담게 된다. 일반 클래스와 마찬가지로 그 기능을 클라이언트에게 노출해야 할  
  합당한 이유가 없다면 private으로, 혹은 필요하다면 package-private으로 선언하라.

- 널리 쓰이는 열거 타입은 Top-Level 클래스로 만들고, 특정 Top-Level 클래스에서만 쓰인다면  
  해당 클래스의 멤버 클래스로 만들자. 예를 들어 소수 자릿수의 반올림 모드를 뜻하는 열거 타입인  
  `java.math.RoundingMode`는 `BigDecimal`이 사용한다. 그런데 반올림 모드는  
  `BigDecimal`과 관련 없는 영역에서도 유용한 개념이라 Java 라이브러리 설계자는 `RoundingMode`를  
  Top-Level 클래스로 올렸다. 이 개념을 많은 곳에서 사용해 다양한 API가 더 일관된 모습을  
  갖출 수 있도록 장려한 것이다.

- `Planet`의 예시에서 본 특성만으로 열거 타입을 사용하는 상황 대다수를 훌륭히 설명할 수 있다.  
  하지만 상수가 더 다양한 기능을 제공해줬으면 할 때도 있다. `Planet` 상수들은 서로 다른 데이터와  
  연결되는 데 그쳤지만, 한 걸음 더 나아가 상수마다 동작이 달라져야 하는 상황도 있을 것이다.  
  예를 들어 사칙연산 계산기의 연산 종류를 열거 타입으로 선언하고, 실제 연산까지 열거 타입 상수가  
  직접 수행했으면 한다 해보자. 먼저 switch문을 이용해 상수값에 따라 분기하는 방법을 시도해보자.

```java
public enum Operation {
  PLUS, MINUS, TIMES, DIVIDE;

  public double apply(double x, double y) {
    switch (this) {
      case PLUS: return x + y;
      case MINUS: return x - y;
      case TIMES: return x * y;
      case DIVIDE: return x / y;
    }
    throw new AssertionError();
  }
}
```

- 동작은 하지만 그다지 예쁜 코드는 아니다. 마지막의 throw 문은 실제로는 도달할 수 없지만, 기술적으로는  
  도달할 수 있기 때문에 생략하면 컴파일조차 되지 않는다. 더 나쁜 점은 깨지기 쉬운 코드라는 점이다.  
  예를 들어 새로운 상수를 추가하면 그에 맞는 case문도 추가해야 한다. 혹시라도 깜빡했다면  
  컴파일은 되지만 런타임에 `AssertionError`가 발생할 것이다.

- 다행이 열거 타입은 상수별로 다르게 동작하는 코드를 구현하는 더 나은 수단을 제공한다.  
  열거 타입에 `apply()`라는 추상 메소드를 선언하고, 각 상수별 클래스 몸체(constant-specific class body),  
  즉 각 상수에서 자신에 맞게 재정의하는 방법이다. 이를 **상수별 메소드 구현(constant-specific method implementation)**  
  이라 한다.

```java
public enum Operation {
  PLUS { public double apply(double x, double y) { return x + y; } },
  MINUS { public double apply(double x, double y) { return x - y; } },
  TIMES { public double apply(double x, double y) { return x * y; } },
  DIVIDE { public double apply(double x, double y) { return x / y; } };

  public abstract double apply(double x, double y);
}
```

- 위 코드에서 보다시피 `apply()`가 상수 선언 바로 옆에 붙어 있으니 새로운 상수를 추가할 때  
  `apply()`도 재정의해야 한다는 사실을 깜빡하기에는 어려울 것이다. 그뿐만 아니라 `apply()`가  
  추상 메소드이므로 재정의하지 않았다면 컴파일 오류로 알려준다.

- 상수별 메소드 구현을 상수별 데이터와 결합할 수도 있다. 예를 들어 아래는 `Operation`의  
  `toString()`을 재정의해 해당 연산을 뜻하는 기호를 반환하도록 한 예시다.

```java
public enum Operation {
  PLUS("+") { public double apply(double x, double y) { return x + y; } },
  MINUS("-") { public double apply(double x, double y) { return x - y; } },
  TIMES("*") { public double apply(double x, double y) { return x * y; } },
  DIVIDE("/") { public double apply(double x, double y) { return x / y; } };

  private final String symbol;

  Operation(String symbol) { this.symbol = symbol; }

  @Override public String toString() { return symbol; }

  public abstract double apply(double x, double y);
}
```

- 열거 타입에는 상수 이름을 입력받아 그 이름에 해당하는 상수를 반환해주는 `valueOf(String)`  
  메소드가 자동 생성된다. 한편 **열거 타입의 `toString()`을 재정의하려거든 `toString()`이**  
  **반환하는 문자열을 해당 열거 타입 상수로 변환해주는 `fromString()` 메소드도 함께 제공하는 것을**  
  **고려해보자**. 아래 코드는 모든 열거 타입에서 사용할 수 있도록 구현한 `fromString()`이다.  
  단, 타입 이름을 적절히 바꿔야 하고 모든 상수의 문자열 표현이 고유해야 한다.

```java
private static final Map<String, Operation> stringToEnum =
  Stream.of(values()).collect(toMap(Object::toString, e -> e));

public static Optional<Operation> fromString(String symbol) {
  return Optional.ofNullable(stringToEnum.get(symbol));
}
```

- `Operation` 상수가 stringToEnum 맵에 추가되는 시점은 열거 타입 상수 생성 후 정적  
  필드가 초기화될 때다. 위 코드는 `values()` 메소드가 반환하는 배열 대신 `Stream`을  
  사용했다. Java8 이전에는 빈 `HashMap`을 만든 다음 `values()`가 반환하는 배열을  
  순회하며 `<문자열, 열거 타입 상수>` 쌍을 맵에 추가했을 것이다. 물론 지금도 이렇게 구현해도 된다.  
  하지만 열거 타입 상수는 생성자에서 자신의 인스턴스를 맵에 추가할 수 없다. 이렇게 하려면  
  컴파일 오류가 나는데, 만약 이 방식이 허용되었다면 런타임에 NPE가 발생했을 것이다.  
  열거 타입의 정적 필드 중 열거 타입의 생성자에서 접근할 수 있는 것은 상수 변수 뿐이다.  
  열거 타입 생성자가 실행되는 시점에는 정적 필드들이 아직 초기화되기 전이라, 자기 자신을 추가하지  
  못하게 하는 제약이 꼭 필요하다. 이 제약의 특수한 예로, 열거 타입 생성자에서는 같은 열거  
  타입의 다른 상수에도 접근할 수 없다.

- `fromString()`이 `Optional<Operation>`을 반환한다는 것도 주의하자.  
  이는 주어진 문자열이 가리키는 연산이 존재하지 않을 수 있음을 클라이언트에게 알리고, 그 상황을  
  클라이언트에서 대처하도록 한 것이다.

- 한편, 상수별 메소드 구현에는 열거 타입 상수끼리 코드를 공유하기 어렵다는 단점이 있다.  
  급여 명세서에서 쓸 요일을 표현하는 열거 타입을 예로 생각해보자. 이 열거 타입은 직원의  
  시간당 기본 임금과 그날 일한 시간이 주어지면, 일당을 계산해주는 메소드를 갖고 있다. 주중에  
  오버타임이 발생하면 잔업수당이 주어지고, 주말에는 무조건 잔업수당이 주어진다. switch문을  
  이용하면 case문을 날짜별로 두어 이 계산을 쉽게 수행할 수 있다.

```java
enum PayrollDay {
  MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

  private static final int MINS_PER_SHIFT = 8 * 60;

  int pay(int minutesWorked, int payRate) {
    int basePay = minutesWorked * payRate;
    int overtimePay;
    switch(this) {
      case SATURDAY: case SUNDAY:
        overtimePay = basePay / 2;
        break;
      default:
        overtimePay = minutesWorked <= MINS_PER_SHIFT ? 0 :
          (minutesWorked - MINS_PER_SHIFT) * payRate / 2;
    }
    return basePay + overtimePay;
  }
}
```

- 분명 간결한 코드이지만 관리 관점에서는 위험한 코드다. 휴가와 같은 새로운 값을 열거 타입에  
  추가하려면 그 값을 처리하는 case문을 잊지 말고 쌍으로 넣어줘야 하는 것이다. 자칫 깜빡하는  
  날에는 프로그램은 멀쩡하지면 휴가 기간에 열심히 일해도 평일과 같은 임금을 받게 된다.

- 상수별 메소드 구현으로 급여를 정확히 계산하는 방법은 두 가지다.  
  첫째, 잔업수당을 계산하는 코드를 모든 상수에 중복해서 넣으면 된다.  
  둘째, 계산 코드를 평일용과 주말용으로 나눠 각각을 도우미 메소드로 작성한 다음, 각 상수가  
  자신에게 필요한 메소드를 적절히 호출하게 하면 된다. 두 방식 모두 코드가 장황해져  
  가독성이 크게 떨어지고 오류 발생 가능성이 높아진다.

- `PayrollDay`에 평일 잔업수당 계산용 메소드인 `overtimePay()`를 구현해놓고, 주말 상수에서만  
  재정의해서 쓰면 장황한 부분은 줄일 수 있다. 하지만 switch문을 썼을 때와 똑같은 단점이 나타난다.  
  즉 새로운 상수를 추가하면서 `overtimePay()`를 재정의하지 않으면 평일용 코드를 그대로  
  물려받게 되는 것이다.

- 가장 깔끔한 방법은 새로운 상수를 추가할 때 잔업수당 _'전략'_ 을 선택하도록 하는 것이다.  
  다행이 멋진 방법이 있는데, 잔업 수당 계산을 private 중첩 열거 타입으로 옮기고 `PayrollDay`  
  열거 타입의 생성자에서 이중 적당한 것을 선택한다. 그러면 `PayrollDay` 열거 타입은  
  잔업수당 계산을 그 전략 열거 타입에 위임하여, switch문이나 상수별 메소드 구현이 필요 없게 된다.  
  이 패턴은 switch문보다는 복잡하지만 더 안전하고 유연하다.

```java
enum PayrollDay {
  MONDAY(WEEKDAY), TUESDAY(WEEKDAY), WEDNESDAY(WEEKDAY), THURSDAY(WEEKDAY), FRIDAY(WEEKDAY),
  SATURDAY(WEEKEND), SUNDAY(WEEKEND);

  private final PayType payType;

  PayrollDay(PayType payType) { this.payType = payType; }

  int pay(int minutesWorked, int payRate) {
    return payType.pay(minutesWorked, payRate);
  }

  // 전략 열거 타입
  enum PayType {
	  WEEKDAY {
      int overtimePay(int minutesWorked, int payRate) {
        return minutesWorked <= MINS_PER_SHIFT ? 0 :
          (minutesWorked - MINS_PER_SHIFT) * payRate / 2;
      }
    },
    WEEKEND {
      int overtimePay(int minutesWorked, int payRate) {
        return minutesWorked * payRate / 2;
      }
    };

    abstract int overtimePay(int minutesWorked, int payRate);
    private static final int MINS_PER_SHIFT = 8 * 60;

    int pay(int minutesWorked, int payRate) {
      int basePay = minutesWorked * payRate;
      return basePay + overtimePay(minutesWorked, payRate);
    }
  }
}
```

- 보다시피 switch문은 열거 타입의 상수별 동작을 구현하는 데 적합하지 않다.  
  하지만 **기존 열거 타입에 상수별 동작을 혼합해 넣을 때는 switch문이 좋은 선택이 될 수 있다.**  
  예를 들어 서드파티에서 가져온 `Operation` 열거 타입이 있는데, 각 연산의 반대 연산을  
  반환하는 메소드가 필요하다 해보자. 아래는 이러한 효과를 내주는 정적 메소드다.

```java
public class SomeClass {
  public static Operation inverse(Operation op) {
    switch (op) {
      case PLUS: return Operation.MINUS;
      case MINUS: return Operation.PLUS;
      case TIMES: return Operation.DIVIDE;
      case DIVIDE: return Operation.TIMES;
      default: throw new AssertionError();
    }
  }
}
```

- 추가하려는 메소드가 의미상 열거 타입에 속하지 않는다면 직접 만든 열거 타입이라도 이 방식을  
  적용하는게 좋다. 종종 쓰이지만 열거 타입 안에 포함할 만큼 유용하지는 않은 경우도 마찬가지다.

- 대부분의 경우 열거 타입의 성능은 정수 상수와 별반 다르지 않다. 열거 타입을 메모리에 올리는 공간과  
  초기화하는 시간이 들긴 하지만, 체감될 정도는 아니다.

- 그래서 열거 타입을 과연 언제 써야 할까?  
  **필요한 원소를 컴파일타임에 다 알 수 있는 상수 집합이라면 항상 열거 타입을 사용하자.**  
  태양계 행성, 한 주의 요일 등 본질적으로 열거 타입인 타입들은 당연히 포함된다.  
  그리고 메뉴 아이템, 연산 코드, 명령줄 플래그 등 허용하는 값 모두를 컴파일타임에 이미  
  알고 있을 때도 쓸 수 있다. **열거 타입에 정의된 상수의 개수가 영원히 고정 불변일 필요는 없다.**  
  열거 타입은 나중에 상수가 추가돼도 바이너리 수준에서 호환되도록 설계되었다.

---

## 핵심 정리

- 열거 타입은 확실히 정수 상수보다 뛰어나다. 더 읽기 쉽고 안전하며 강력하다.  
  대다수의 열거 타입이 명시적 생성자나 메소드 없이 쓰이지만, 각 상수를 특정 데이터와 연결짓거나  
  상수마다 다르게 동작하게 할 때는 필요하다. 드물게는 하나의 메소드가 상수별로 다르게 동작해야  
  할 때도 있다. 이런 열거 타입에서는 switch문 대신 상수별 메소드 구현을 사용하자.  
  열거 타입 상수 일부가 같은 동작을 공유한다면 전략 열거 타입 패턴을 사용하자.

---
