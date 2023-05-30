# 자원을 직접 명시하지 말고 의존 객체 주입을 사용하라

- 많은 클래스는 하나 이상의 자원에 의존한다.  
  예를 들어 맞춤법 검사기는 사전에 의존하는데, 이런 클래스를 정적 유틸리티 클래스로  
  구현하는 경우가 많다.

```java
public class SpellChecker {
	private static final Lexicon dictionary = /*..*/;

	private SpellChecker() {}

	public static boolean isValid(String word) { /*..*/ }
	public static List<String> suggestions(String typo) { /*..*/ }
}
```

비슷하게, 싱글턴으로 구현하는 경우도 있다.

```java
public class SpellChecker {
	private static final Lexicon dictionary = /*..*/;

	private SpellChecker() {}
	public static SpellChecker INSTANCE = new SpellChecker(/*..*/);

	public boolean isValid(String word) { /*..*/ }
	public List<String> suggestions(String typo) { /*..*/ }
}
```

- 두 방식 모두 사전을 단 하나만 사용한다고 가정한다는 점에서 그렇게 좋지는 않다.  
  실전에서는 사전이 언어별로 따로 있고, 특수 어휘용 사전을 별도로 두기도 한다.  
  심지어 테스트용 사전도 필요할 수 있다. 사전 하나로 이 모든 쓰임에 대응할 수 있기를  
  바라는 건 너무 순진한 생각이다.

- `SpellChecker`가 여러 사전을 사용할 수 있도록 만들어보자.  
  간단히 dictionary 필드에서 final 한정자를 제거하고 다른 사전으로 교체하는 메소드를  
  추가할 수 있지만, 아쉽게도 이 방식은 어색하고 오류를 내기 쉬우며 멀티쓰레드 환경에서는  
  쓸 수 없다. **사용하는 자원에 따라 동작이 달라지는 클래스에서는 정적 유틸리티 클래스나**  
  **싱글턴 방식이 적합하지 않다.**

- 대신 `SpellChecker`가 여러 자원 인스턴스를 지원해야 하며, 클라이언트가 원하는  
  자원(dictionary)을 사용해야 한다. 이 조건을 만족하는 간단한 패턴은 바로 인스턴스를 생성할 때  
  생성자에 필요한 자원을 넘겨주는 방식이다. 이는 의존 객체 주입의 한 형태로, 맞춤법 검사기를 생성할 때  
  의존 객체인 사전을 주입해주면 된다.

```java
public class SpellChecker {
	private fianl Lexicon dictionary;

	public SpellChecker(Lexicon dictionary) {
		this.dictionary = dictionary;
	}

	public boolean isValid(String word) { /*..*/ }
	public List<String> suggestions(String typo) { /*..*/ }
}
```

- 의존 객체 주입은 위처럼 매우 단순하다. 예시에서는 dictionary라는 딱 하나의 자원만 사용하지만,  
  자원이 몇 개든 의존 관계가 어떻든 상관없이 잘 작동한다. 또한 불변을 보장하여 같은 자원을 사용하려는  
  여러 클라이언트가 의존 객체들을 안심하고 공유할 수 있기도 하다. 의존 객체 주입은 생성자, 정적 팩토리,  
  빌더 패턴 모두에 동일하게 응용할 수 있다.

- 이 패턴의 쓸만한 변형으로, 생성자에 자원 팩토리를 넘겨주는 방식이 있다.  
  팩토리란 호출할 때마다 특정 타입의 인스턴스를 반복해서 만들어주는 객체를 의미한다.  
  즉 Factory Method Pattern을 구현한 것이다. Java8에서 소개한 `Supplier<T>` 인터페이스가  
  팩토리를 표현한 완벽한 예시이다. `Supplier<T>`를 입력으로 받는 메소드는 일반적으로 한정적 와일드카드  
  (Bounded Wildcard) 타입을 사용해 팩토리의 타입 매개변수를 제한한다.  
  이 방식을 사용해 클라이언트는 자신이 명시한 타입의 하위 타입이라면 무엇이든 생성할 수 있는 팩토리를  
  넘길 수 있다. 아래 코드는 클라이언트가 제공한 팩토리가 생성한 `Tile`들로 구성된 `Mosaic`을 만드는 메소드다.

```java
Mosaic create(Supplier<? extends Tile> tileFactory) { /*..*/ }
```

- 의존 객체 주입이 유연성과 테스트의 용이성을 개선해주긴 하지만, 의존성이 수천개나 되는 큰 프로젝트에서는  
  코드를 어지럽게 만들 수도 있다. Dagger, Guice, Spring과 같은 의존 객체 주입 프레임워크를  
  사용하면 이러한 어지러움을 해소할 수 있다.

---

## 핵심 정리

- 클래스가 내부적으로 하나 이상의 자원에 의존하고, 그 자원이 클래스 동작에 영향을 준다면 싱글턴과  
  정적 유틸리티 클래스는 사용하지 않는 것이 좋다. 이 자원들은 클래스가 직접 만들게 해서도 안된다.  
  대신 필요한 자원을(또는 그 자원을 만들어주는 팩토리를) 생성자 혹은 정적 팩토리나 빌더에 넘겨주자.  
  의존 객체 주입이라하는 이 기법은 클래스의 유연성, 재사용성, 테스트의 용이성을 개선해준다.

---
