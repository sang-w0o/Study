# 인터페이스는 타입을 정의하는 용도로만 사용하라

- 인터페이스는 자신을 구현한 클래스의 인스턴스를 참조할 수 있는 타입의 역할을 한다.  
  달리 말해, 클래스가 어떤 인터페이스를 구현한다는 것은 자신의 인스턴스로 무엇을  
  할 수 있는지를 클라이언트에게 알려주는 것이다. 인터페이스는 오직 이 용도로만  
  사용해야 한다.

- 이 지침에 맞지 않는 예로, 소위 _상수 인터페이스_ 라는 것이 있다. 상수 인터페이스란  
  메소드 없이, 상수를 뜻하는 static final 필드로만 가득찬 인터페이스를 말한다.  
  그리고 이 상수들을 사용하려는 클래스에서는 정규화된 이름(Qualified Name)을  
  쓰는 것을 피하고자 그 인터페이스를 구현하곤 한다. 아래 예시를 보자.

```java
public interface PhysicalConstants {
    // 아보가드로 수(1/몰)
    static final double AVOGADROS_NUMBER = 6.022_140_857e23;

    // 볼츠만 상수(J/K)
    static final double BOLTZMANN_CONSTANT = 1.380_648_52e-23;

    // 전자 질량(kg)
    static final double ELECTRON_MASS = 9.109_383_56e-31;
}
```

- **상수 인터페이스 안티패턴은 인터페이스를 잘못 사용한 예시이다.**  
  클래스 내부에서 사용하는 상수는 외부 인터페이스가 아니라 내부 구현에 속한다.  
  따라서 상수 인터페이스를 구현하는 것은 이 내부 구현을 클래스의 API로 노출하는 행위다.  
  클래스가 어떤 상수 인터페이스를 사용하든 사용자에게는 아무런 의미가 없다. 오히려 사용자에게는  
  혼란을 주기도 하며, 더 심하게는 클라이언트 코드가 내부 구현에 해당하는 이 상수들에  
  종속되게 한다. 그래서 다음 릴리즈에서 이 상수들을 더는 쓰지 않게 되더라도, 바이너리  
  호환성을 위해 여전히 상수 인터페이스를 구현하고 있어야 한다. final이 아닌 클래스가  
  상수 인터페이스를 구현한다면 모든 하위 클래스들의 namespace가 그 인터페이스가 정의한  
  상수들로 오염되어 버린다.

- `java.io.ObjectsStreamConstants` 등 Java 플랫폼 라이브러리에도 상수  
  인터페이스가 몇 개 있으나, 인터페이스를 잘못 활용한 예시이니 따라해서는 안된다.

- 상수를 공개할 목적이라면 더 합당한 선택지가 몇 가지 있다.  
  특정 클래스나 인터페이스와 강하게 연관된 상수라면 그 클래스나 인터페이스 자체에 추가해야 한다.  
  모든 숫자 기본 타입의 Boxing 클래스가 대표적으로, `Integer`와 `Double`에 선언된  
  `MIN_VALUE`와 `MAX_VALUE`가 이런 예시이다. 열거 타입으로 나타내기 적합한 상수라면  
  열거 타입으로 만들어 공개하면 된다. 그것도 아니라면, 인스턴스화할 수 없는 유틸리티 클래스에  
  담아 공개하자. 아래 코드는 위에서 본 `PhysicalConstants`의 유틸리티 클래스 버전이다.

```java
public class PhysicalConstants {

    private PhysicalConstants() { }

    // 아보가드로 수(1/몰)
    public static final double AVOGADROS_NUMBER = 6.022_140_857e23;

    // 볼츠만 상수 (J/K)
    public static final double BOLTZMANN_CONSTANT = 1.380_648_52e-23;

    // 전자 질량 (kg)
    public static final double ELECTRON_MASS = 9.109_383_56e-31;
}
```

- 유틸리티 클래스에 정의된 상수를 클라이언트에서 사용하려면 클래스명까지 함께 명시해야 한다.  
  `PhysicalConstants.ELECTRON_MASS` 처럼 말이다. 유틸리티 클래스의 상수를 빈번히 사용한다면  
  static import하여 클래스명은 생략할 수 있다.

```java
import static path.to.file.PhysicalConstants.*;

public class Test {
    double atoms(double mols) {
	return AVOGADROS_NUMBER * mols;
    }
}
```

<hr/>
