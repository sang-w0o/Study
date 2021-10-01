# 태그 달린 클래스보다는 클래스 계층구조를 활용하라

- 두 가지 이상의 의미를 표현할 수 있으며, 그중 현재 표현하는 의미를 태그값으로  
  알려주는 클래스를 본 적 있을 것이다. 아래 코드는 원과 사각형을 표현할 수 있는 클래스다.

```java
class Figure {

    enum Shape { RECTANGLE, CIRCLE };

    // 태그 필드 - 현재 모양을 나타낸다.
    final Shape shape;

    // 아래 두 개 필드들은 사각형(RECTANGLE)일때만 쓰인다.
    double length;
    double width;

    // 아래 필드는 모양이 원(CIRCLE)일때만 쓰인다.
    double radius;

    // CIRCLE 용 생성자
    Figure(double raidus) {
	this.shape = Shape.CIRCLE;
	this.radius = raidus;
    }

    // RECTANGLE 용 생성자
    Figure(double length, double width) {
	this.shape = Shape.RECTANGLE;
	this.length = length;
	this.width = width;
    }

    double area() {
	switch (shape) {
	    case RECTANGLE:
	        return width * length;
	    case CIRCLE:
	        return Math.PI * radius * radius;
	    default:
	        throw new AssertionError(shape);
	}
    }
}
```

- 위처럼 태그 달린 클래스에는 단점이 한가득하다.  
  우선 열거 타입 선언, 태그 필드, switch문 등 쓸데없는 코드가 많다.  
  여러 구현이 한 클래스에 혼합돼 있어서 가독성도 나쁘다. 다른 의미를 위한 코드도 언제나  
  함께 하니 메모리도 많이 사용한다. 필드들을 final로 선언하려면 해당 의미에 쓰이지 않는  
  필드들까지 생성자에서 초기화해야 한다. 즉, 쓰지 않는 필드를 초기화하는 불필요한 코드가 생긴다.  
  생성자가 태그 필드를 설정하고 해당 의미에 쓰이는 데이터 필드들을 초기화하는 데 컴파일러가  
  도와줄 수 있는건 별로 없다. 엉뚱한 필드를 초기화해도 런타임에야 문제가 드러날 뿐이다.  
  또 다른 의미를 추가하려면 코드를 수정해야 한다. 예를 들어, 새로운 의미를 추가할 때마다  
  모든 switch문을 찾아 새로운 의미를 추가하는 코드를 추가해야 하는데, 하나라도 빠뜨리면  
  역시 런타임에서야 문제가 불거져 나올 것이다. 마지막으로, 인스턴스 타입만으로는 현재 나타내는  
  의미를 알 길이 전혀 없다. 한마디로 **태그 달린 클래스는 장황하고, 오류를 내기 쉽고,**  
  **비효율적이다.**

- 다행이 Java와 같은 객체지향 언어는 타입 하나로 다양한 의미의 객체를 표현하는 훨씬 나은  
  수단을 제공한다. 바로 **클래스의 계층 구조를 활용하는 서브타이핑(subtyping)이다.**  
  태그 달린 클래스는 클래스 계층 구조를 어설프게 흉내내는 아류일 뿐이다.

> 서브타이핑(subtyping): 서브 클래스가 슈퍼 클래스를 대체할 수 있는 것

- 그렇다면 태그 달린 클래스를 클래스 계층 구조로 바꾸는 방법을 알아보자.  
  가장 먼저 계층 구조의 root가 될 추상 클래스를 정의하고, 태그 값에 따라 동작이 달라지는  
  메소드들을 root 클래스의 추상 메소드로 선언한다. 위 코드의 `area()` 같은 메소드가  
  여기에 해당한다. 그런 다음 태그 값에 관계없이 동작히 일정한 메소드들을 root 클래스에  
  일반 메소드로 추가한다. 모든 하위 클래스에서 공통으로 사용하는 데이터 필드들도 전부 root  
  클래스로 옮긴다. `Figure` 클래스에서는 태그 값에 관계없는 메소드가 하나도 없고, 모든  
  하위 클래스에서 사용하는 공통 데이터 필드도 없다. 그 결과 root 클래스에는 추상 메소드인  
  `area()` 하나만 남게 된다.

- 다음으로, root 클래스를 확장한 구체 클래스를 의미별로 하나씩 정의한다.  
  위 예시에서는 `Figure`를 확장한 `Circle`, `Rectangle` 클래스를 만들면 된다.  
  각 하위 클래스에는 각자의 의미에 해당하는 데이터 필드들을 넣는다. `Circle`에는 radius,  
  `Rectangle`에는 length, width를 넣으면 된다. 그런 다음 root 클래스가 정의한  
  추상 메소드를 각자의 의미에 맞게 구현한다. 아래는 `Figure`를 클래스 계층구조 방식으로  
  구현한 코드다.

```java
abstract class Figure {
    abstract double area();
}

class Circle extends Figure {
    final double radius;

    Circle(double radius) { this.radius = radius; }

    @Override double area() { return Math.PI * radius * radius; }
}

class Rectangle extends Figure {
    final double length, width;

    Rectangle(double length, double width) {
	this.length = length;
	this.width = width;
    }

    @Override double area() { return length * width; }
}
```

- 위 코드의 클래스 계층 구조는 태그 달린 클래스의 단점을 모두 날려버린다.  
  간결하고 명확하며, 쓸데 없는 코드도 모두 사라졌다. 각 의미를 독립된 클래스에 담아  
  관련 없는 데이터 필드를 모두 제거했다. 살아 남은 필드들은 모두 final 이다.  
  각 클래스의 생성자가 모든 필드를 남김없이 초기화하고, 추상 메소드를 모두 구현했는지를  
  컴파일러가 알려주기도 한다. 실수로 빼먹은 case문 때문에 런타임 오류가 발생할 일도 없다.  
  Root 클래스의 코드를 건드리지 않고도 다른 프로그래머들이 독립적으로 계층구조를 확장하고  
  함께 사용할 수도 있다. 타입이 의미별로 따로 존재하니, 변수의 의미를 명시하거나 제한할 수 있고,  
  또 특정 의미만 매개변수로 받을 수도 있다.

- 또한, 타입 사이의 자연스러운 계층 관계를 반영할 수 있어서 유연성을 물론, 컴파일 타임에  
  타입 검사 능력을 높여준다는 장점도 있다. 정사각형(`Square`)도 지원하도록 해야 한다 해보자.  
  태그 달린 클래스에 해줄 변경 보다 계층 구조를 활용한 클래스에 추가할 코드는 확연히 간단하다.

<hr/>

## 핵심 정리

- 태그 달린 클래스를 써야 하는 상황은 거의 없다. 새로운 클래스를 작성하는데 태그 필드가 등장한다면  
  태그를 없애고 계층구조로 대체하는 방법을 생각해보자. 기존 클래스가 태그 필드를 사용하고 있다면  
  계층구조로 리팩토링하는 것을 고려해보자.

<hr/>
