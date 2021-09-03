# FlyWeight Pattern

<h2>개요</h2>

- FlyWeight 패턴은 메모리 절약을 위해 만들어진 패턴이다. 특히 만약 특정 객체의 인스턴스화에 많은 비용이 소모될 때,  
  이 패턴을 사용하여 비용을 절감할 수 있다.

- 간단히 말해 FlyWeight 패턴은 객체의 인스턴스를 만들고, 만들어진 인스턴스를 어딘가에 저장하는 로직을 가진  
  팩토리로 이루어진 패턴이다. 객체의 인스턴스화가 팩토리를 통해 요청될 때마다 팩토리는 해당 객체의 인스턴스화가 이미  
  된 인스턴스가 있는지를 검사한다. 있다면 해당 인스턴스를 반환하고, 없다면 새로운 인스턴스를 생성 후 어딘가에 저장한 후  
  반환한다.

- **FlyWeight의 객체들은 불변성을 가짐에 유의하자. 인스턴스에 대한 작업은 무조건 팩토리를 통해 진행되어야 한다.**

- 이 패턴을 이루는 요소들은 아래와 같다.

  - 클라이언트가 FlyWeight 객체를 변경할 수 있는 인터페이스를 제공하는 클래스(팩토리)
  - 하나 이상의 인터페이스 구현체
  - 객체의 인스턴스화와 caching 관리를 책임지는 팩토리 클래스

<hr/>

<h2>예시</h2>

- 우선 `Vehicle`이라는 인터페이스를 만들어보자. 이 인터페이스는 우리가 이후에 볼 팩토리의 반환 타입이 된다.

```java
public interface Vehicle {
	public void start();
	public void stop();
	public Color getColor();
}
```

- 다음으로 위 `Vehicle` 인터페이스의 구현체를 만들어보자.

```java
public class Car implements Vehicle {
	private Engine engine;
	private Color color;
	public void start() {
		System.out.println("Car start");
	}
	public void stop() {
		System.out.println("Car stop");
	}
	public Color getColor() {
		System.out.println("Car color is " + color);
	}
```

- `Car`는 `Engine`, `Color`의 2개 상태(state)를 가진다.

- 이제 마지막으로 `Vehicle` 타입의 객체들의 인스턴스를 관리하는 팩토리를 만들어보자.

```java
public class VehicleFactory {
	private static Map<Color, Vehicle> vehiclesCache = new HashMap<>();

	public static Vehicle createVehicle(Color color) {
		Vehicle newVehicle = vehiclesCache.computeIfAbsent(color, newColor -> {
			Engine newEngine = new Engine();
			return new Car(newEngine, newColor);
		});
		return newVehicle;
	}
}
```

- 클라이언트는 `VehicleFactory#createVehicle()`을 사용하여 `Vehicle` 타입의 인스턴스를 받아올 수 있다.  
  `VehicleFactory#createVehicle()`은 인자로 들어온 `Color`를 기준으로 기존에 생성된 인스턴스가 있다면  
  그것을 반환하고, 없다면 새로운 것을 만들고 저장한 후 반환한다.

<hr/>

<h2>결론</h2>

- 간단하게 FlyWeight 패턴을 구현해 보았는데, 이 패턴의 가장 자주 사용되는 Use Case는 아래와 같다.

  - 데이터 압축

    - FlyWeight 패턴의 목표는 최대한 기존 데이터를 공유(활용)하여 메모리 사용을 줄이는 것이다.  
      이는 압축 알고리즘에서 적절하게 사용될 수 있다.

  - 데이터 캐싱

    - 많은 현대 애플리케이션은 응답 시간을 줄이기 위해 cache를 사용한다.  
      FlyWeight 패턴은 cache의 근본 개념과 매우 유사한 속성을 가진다.  
      물론 일반적인 목적을 가진 cache와 FlyWeight는 복잡도나 구현 방식에 차이점이 존재한다.

</hr>

- 참고 문서: <a href="https://www.baeldung.com/java-flyweight">링크</a>
