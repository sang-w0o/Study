# 생성자에 매개변수가 많다면 빌더를 고려하라

- 정적 팩토리와 생성자에는 똑같은 제약이 하나 있는데, 바로 **선택적 매개변수가 많을 때 대응하기 어렵다는 것**이다.  
  학생의 정보를 담는 클래스를 생각해보자. 학생의 정보를 담으려면 굉장히 많은 속성을 가져야 하는데, 그 중에서도 추려서  
  아래의 클래스가 나왔다고 하자. 이런 클래스용 생성자 혹은 정적 팩토리는 어떤 모습일까?

<h2>점층적 생성자 패턴</h2>

- 개발자들은 이럴 때 점층적 생성자 패턴(Telescoping Constructor Pattern)을 즐겨 사용했다.  
  필수 매개변수만 받는 생성자, 필수와 선택을 1개 받는 생성자, 선택을 2개까지 받는 생성자 등의 형태로  
  선택 배개변수를 전부 다 받는 생성자까지 늘려나가는 방식이다. 아래는 예시이다.

```java
public class Student {
	private final int age;  // 필수
	private final String gender;  // 선택
	private final String school;  // 선택
	private final String city;  // 선택
	private final String name;  // 필수

	public Student(int age, String name) {
		this.age = age;
		this.name = name;
	}

	public Student(int age, String name, String gender) {
		this.age = age;
		this.name = name;
		this.gender = gender;
	}

	// ...

	public Student(int age, String gender, String school, String city, String name) {
		this.age = age;
		this.gender = gender;
		this.school = school;
		this.city = city;
		this.name = name;
	}
}
```

- 이 클래스의 인스턴스를 만들려면 원하는 매개변수를 모두 포함한 생성자 중 가장 짧은 것을 호출하면 된다.

```java
Student student = new Student(24, "Male", "Some School", "Seoul", "Sangwoo");
```

- 보통 이런 생성자는 사용자가 설정하고 싶지 않은 매개변수까지 포함하기 쉬운데,  
  어쩔 수 없이 그런 매개변수에도 값을 지정해줘야 한다. 앞 코드에서는 school에 어쩔 수 없이  
  "Some School"을 넘겨주었다. 위 예시에서는 그래봤자 매개변수가 5개라 그리 나빠보이지 않을 수 있지만,  
  매개변수의 숫자가 늘어나면 금세 걷잡을 수 없게 된다.

- 즉, **점층적 생성자 패턴도 쓸 수는 있지만, 매개변수 개수가 많아지면 클라이언트 코드를**  
  **작성하거나 읽기 어렵다.** 코드를 읽을 때 각 값의 의미가 무엇인지 헷갈릴 것이고,  
  매개변수가 몇개인지도 주의해서 세어보아야 할 것이다. 타입이 같은 매개변수가 연달아 늘어서 있으면  
  찾기 어려운 버그로 이어질 수 있다. 클라이언트가 실수로 매개변수의 실수를 바꿔 건네줘도 컴파일러는 알아채지 못하고,  
  결국 런타임에 엉뚱한 동작을 하게 될 것이다.

<hr/>

<h2>JavaBeans Pattern</h2>

- 이번에는 선택적 매개변수가 많을 때 활용할 수 있는 두 번째 대안인 JavaBeans Pattern을 살펴보자.  
  이 패턴은 매개변수가 없는 생성자로 객체를 만든 후, Setter 메소드들을 호출해 원하는 매개변수의 값을  
  설정해주는 방식이다.

```java
public class Student {
	private int age = 0;  // 필수
	private String gender = "";  // 선택
	private String school = "";  // 선택
	private String city = "";  // 선택
	private String name = "";  // 필수

	// Standard Setters
}
```

- 이 패턴의 단점은 **객채 하나를 만들려면 메소드를 여러 개 호출해야 하고, 객체가 완전히 생성되기 전까지는**  
  **일관성(Consistency)이 무너진 상태에 놓기에 된다는 것** 이다. 오히려 점층적 생성자 패턴에서는 매개변수들이  
  유효한지를 생성자에서만 확인하면 일관성을 유지할 수 있었는데, 그 장치가 완전히 사라진 것이다.  
  일관성이 깨진 객체가 만들어지면, 버그를 심은 코드와 그 버그 때문에 런타임에 문제를 겪는 코드가 물리적으로 멀리  
  떨어져있을 것이므로 디버깅도 만만치 않다. 이처럼 일관성이 무너지는 문제 때문에 **JavaBeans 패턴에서는 클래스를**  
  **불변으로 만들 수 없으며** thread 안정성을 얻으려면 프로그래머가 추가적인 작업을 해줘야만 한다.

<hr/>

<h2>Builder Pattern</h2>

- Builder Pattern은 점층적 생성자 패턴의 안전성과 JavaBeans 패턴의 가독성을 겸비한 패턴이다.  
  클라이언트는 필요한 객체를 직접 만드는 대신, 필수 매개변수만으로 생성자 혹은 정적 팩토리를 호출하여  
  빌더 객체를 얻는다. 그런 다음 빌더 객체가 제공하는 일종의 setter 메소드들로 원하는 선택 매개변수를  
  설정한다. 마지막으로 매개변수가 없는 `build()` 메소드를 호출해 클라이언트가 필요한 객체를 얻는다.

- 빌더는 보통 생성할 클래스 내에 정적 멤버 클래스로 만들어둔다.  
  위의 `Student` 클래스에 빌더 패턴을 적용해보자.

```java
public class Student {
    private final int age;
    private final String gender;
    private final String school;
    private final String city;
    private final String name;

    public static class Builder {
        private final int age;
        private String gender = "";
        private String school = "";
        private String city = "";
        private final String name;

        public Builder(int age, String name) {
            this.age = age;
            this.name = name;
        }

        public Builder gender(String val) {
            gender = val;
            return this;
        }

        public Builder school(String val) {
            this.school = val;
            return this;
        }

        public Builder city(String val) {
            this.city = val;
            return this;
        }

        public Student build() {
            return new Student(this);
        }
    }

    private Student(Builder builder) {
        age = builder.age;
        gender = builder.gender;
        school = builder.school;
        city = builder.city;
        name = builder.name;
    }
}
```

- 위 빌더 패턴은 아래와 같이 사용할 수 있다.

```java
Student student = new Student.Builder(24, "Sangwoo")
    .city("Seoul")
    .gender("Male")
    .school("Some School")
    .build();
```

- `Student` 클래스는 불변이며, 모든 매개변수의 기본값들을 한 곳에 모아뒀다.  
  빌더의 setter 메소드들은 빌더 자신을 반환하기 때문에 연쇄적으로 호출할 수 있다.  
  이런 방식을 Method Chaining이라 한다.

- 빌더 패턴을 사용하는 코드는 쓰기 쉽고, 무엇보다도 읽기 쉽다.  
  참고로 빌더 패턴은 파이썬과 스칼라에 있는 **명명된 선택적 매개변수(Named Optional Parameters)** 를  
  흉내낸 것이다.

<h3>빌더 패턴은 계층적으로 설계된 클래스와 함께 쓰기에 좋다</h3>

- 각 계층의 클래스에 관련 빌더를 멤버로 정의하자. 추상 클래스는 추상 빌더를, 구체 클래스는  
  구체 빌더를 갖게 한다. 아래는 피자의 다양한 종류를 표현하는 계층구조의 루트에 놓인 추상 클래스다.

```java
public abstract class Pizza {
    public enum Topping { HAM, MUSHROOM, ONION, PEPPER, SAUSAGE }
    final Set<Topping> toppings;

    abstract static class Builder<T extends Builder<T>> {
        EnumSet<Topping> toppings = EnumSet.noneOf(Topping.class);

        public T addTopping(Topping topping) {
            toppings.add(Objects.requireNonNull(topping));
            return self();
        }

        abstract Pizza build();

        // 하위 클래스는 이 메소드를 overriding하여
        // "this"를 반환하도록 해야 한다.
        protected abstract T self();
    }

    Pizza(Builder<?> builder) {
        toppings = builder.toppings.clone();
    }
}
```

- `Pizza.Builder` 클래스는 재귀적 타입 한정을 이용하는 제네릭 타입이다.  
  여기에 추상 메소드인 `self()`를 더해 하위 클래스에서는 형변환하지 않고도 method chaining을  
  지원할 수 있다. self 타입이 없는 Java를 위한 이 우회 방법을 Simulated Self-Type 관용구라 한다.

- 이제 `Pizza`를 상속하는 클래스를 살펴보자.

```java
public class NYPizza extends Pizza {
    public enum Size { SMALL, MEDIUM, LARGE }
    private final Size size;

    public static class Builder extends Pizza.Builder<Builder> {
        private final Size size;

        public Builder(Size size) {
            this.size = Objects.requireNonNull(size);
        }

        @Override
        public NYPizza build() {
            return new NYPizza(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    private NYPizza(Builder builder) {
        super(builder);
        size = builder.size;
    }
}
```

- 하위 클래스인 `NYPizza`의 빌더가 정의한 `build()` 메소드는 자기 자신을 반환하도록 선언했다.  
  즉 `NYPizza.Builder`가 `NYPizza` 자신을 반환한다는 뜻이다.  
  하위 클래스의 메소드가 상위 클래스의 메소드가 정의한 반환 타입이 아닌 그 하위 타입을 반환하는 기능을  
  _공변 반환 타이핑(Convariant return typing)_ 이라 한다. 이 기능을 이용하면 클라이언트가  
  형변환에 신경 쓰지 않고도 빌더를 사용할 수 있다.

- 이러한 계층적 빌더를 사용하는 클라이언트의 코드도 앞서 본 `Student`를 사용하는 코드와 다르지 않다.

```java
NYPizza pizza = new NYPizza.Builder(NYPizza.Size.SMALL)
        .addTopping(Pizza.Topping.HAM)
        .addTopping(Pizza.Topping.ONION)
        .build();
```

- 생성자로는 누릴 수 없는 사소한 이점으로, 빌더를 사용하면 가변인자를 여러 개 사용할 수 있다.  
  각각을 적절한 메소드로 나누어 선언하면 된다. 아니면 메소드를 여러 번 호출하도록 하고  
  각 호출 때 넘겨진 매개변수들을 하나의 필드로 모을 수도 있다. 위 예시에서 `addToppping()`을  
  두 번 호출하는 것이 그 예시이다.

- 빌더 패턴은 상당히 유연하다. 빌더 하나로 여러 객체를 순회하면서 만들 수 있고, 빌더에 넘기는  
  매개변수에 따라 다른 객체를 만들 수도 있다. 객체마다 부여되는 일련번호와 같은 특정 필드는  
  빌더가 알아서 채우도록 할 수도 있다.

- 빌더 패턴에 장점만 있는 것은 아니다. 빌더를 사용하여 객체를 만들기 위해서는 그에 앞서 빌더부터  
  만들어야 한다. 빌더의 생성 비용이 크진 않지만, 성능에 민감한 상황에서는 문제가 될 수도 있다.  
  또한 점층적 생성자 패턴보다는 코드가 장황해서 매개변수가 4개 이상은 되어야 값어치를 한다.  
  하지만 API는 시간이 지날수록 매개변수가 많아지는 경향이 있음을 명심하자.

<hr/>

<h2>핵심 정리</h2>

- 생성자나 정적 팩토리가 처리해야 할 매개변수가 많다면 빌더 패턴을 사용하는게 더 낫다.  
  매개변수 중 다수가 필수가 아니거나 같은 타입이라면 특히 더 그렇다. 빌더는 점층적 생성자보다  
  클라이언트 코드를 읽고 쓰기가 훨씬 간결하고, JavaBeans 패턴보다 안전하다.

<hr/>
