# 공개된 API 요소에는 항상 문서화 주석을 작성하라

- API를 쓸모 있게 하려면 잘 작성된 문서도 곁들여야 한다. 전통적으로 API 문서는 사람이 직접 작성하므로  
  코드가 변경되면 매번 함께 수정해줘야 하는데, Java에서는 Javadoc이라는 유틸리티가 이 귀찮은 작업을  
  도와준다. Javadoc은 소스코드 파일에서 문서화 주석(doc comment; Javadoc 주석)이라는 특수한  
  형태로 기술된 설명을 추려 API 문서로 변환해준다.

- 문서화 주석을 작성하는 규칙은 공식 언어 명세에 속하진 않지만, Java 프로그래머라면 응당 알아야 하는  
  업계 표준 API라 할 수 있다. Java 버전이 올라가며 추가된 중요한 Javadoc 태그로는 Java5의  
  `@literal`과 `@code`, Java8의 `@implSpec`, Java9의 `@index`를 꼽을 수 있다.

- **직접 만든 API를 올바르게 문서화하려면 공개된 모든 클래스, 인터페이스, 메소드, 필드 선언에 문서화**  
  **주석을 달아야 한다.** 직렬화할 수 있는 클래스라면 직렬화 형태에 대해서도 적어야 한다. 문서화  
  주석이 없다면 Javadoc도 그저 공개 API 요소들의 _'선언'_ 만 나열해주는 것이 전부다. 문서가 잘  
  갖춰지지 않은 API는 쓰기 헷갈려서 오류의 원인이 되기 쉽다. 기본 생성자에는 문서화 주석을 달 방법이  
  없으니 공개 클래스는 절대 기본 생성자를 사용하면 안된다. 한편, 유지보수까지 고려한다면 대다수의 공개되지  
  않은 클랫, 인터페이스, 생성자, 메소드, 필드에도 문서화 주석을 달아야할 것이다. 공개 API 만큼  
  친절하진 않더라도 말이다.

- **메소드용 문서화 주석에는 해당 메소드와 클라이언트 사이의 규약을 명료하게 기술해야 한다.**  
  상속용으로 설계된 클래스의 메소드가 아니라면, 그 메소드가 어떻게 동작하는지가 아닌 무엇을 하는지를  
  기술해야 한다. 즉, how가 아닌 what을 기술해야 한다. 문서화 주석에는 클라이언트가 해당 메소드를  
  호출하기 위한 전제조건(precondition)을 모두 나열해야 한다. 또한 메소드가 성공적으로 수행된 후에  
  만족해야 하는 사후조건(postcondition)도 모두 나열해야 한다. 일반적으로 전제조건은 `@throws`  
  태그로 비검사 예외를 선언하여 암시적으로 기술한다. 비검사 예외 하나가 전제조건 하나와 연결되는 것이다.  
  또한 `@param` 태그를 이용해 그 조건에 영향받는 매개변수에 기술할 수도 있다.

- 전제조건과 사후조건 뿐만 아니라 부작용도 문서화해야 한다. 부작용이란 사후조건으로 명확히 나타나지는  
  않지만 시스템의 상태에 어떠한 변화를 가져오는 것을 뜻한다. 예를 들어 백그라운드 스레드를 시작시키는  
  메소드라면 그 사실을 문서에 밝혀야 한다.

- 메소드의 계약(contract)을 완벽히 기술하려면 모든 매개변수에 `@param` 태그를, 반환 타입이 void가  
  아니라면 `@return` 태그를, 발생할 가능성이 있는 검사, 비검사 모든 예외에 `@throws` 태그를  
  달아야 한다. 각 코딩 컨벤션에 따라서 `@return`의 설명이 메소드 설명과 같을 때는 `@return`을  
  생략해도 좋다.

- 관례상 `@param`과 `@return`의 설명은 해당 매개변수가 뜻하는 값이나 반환값을 설명하는 명사구를 쓴다.  
  드물게는 명사구 대신 산술 표현식을 쓰기도 한다. 아래의 `BigInteger` API 문서를 보자. `@throws`의  
  설명은 if로 시작해 해당 예외를 던지는 조건을 설명하는 절이 뒤따른다. 역시 관례상 `@param`,  
  `@return`, `@throws` 태그의 설명에는 마침표를 붙이지 않는다. 아래는 위 규칙을 모두 반영한  
  문서화 주석의 예시이다.

```java
/**
 * Returns the element at the specified position in this list.
 *
 * <p>This method is <i>not</i> guaranteed to run in constant time.
 * In some implementations it may run in time proportional to the element position.
 *
 * @param  index index of the element to return; must be
 * non-negative and less than the size of this list
 * @return the element at the specified position in this list
 * @throws IndexOutOfBoundsException if the index is out of range
 * ({@code index < 0 || index >= this.size()})
 */
public E get(int index) { /* ... */ }
```

- 문서화 주석에 HTML 태그(`<p>`, `<i>`)를 쓴 점에 주목하자. Javadoc 유틸리티는 문서화 주석을 HTML로  
  변환하므로 문서화 주석 내의 HTML 요소들이 최종 HTML 문서에 반영된다. 드물기는 하지만 Javadoc 설명에  
  표(table)까지 집어넣는 프로그래머도 있다.

- `@throws` 절에 사용한 `{@code}` 태그도 살펴보자. 이 태그의 효과는 두 가지다.  
  첫째, 태그로 감싼 내용을 코드용 폰트로 렌더링한다. 둘째, 태그로 감싼 내용에 포함된 HTML 요소나 다른  
  Javadoc 태그를 무시한다. 두 번째 효과 덕에 HTML 메타문자인 `<` 기호 등을 별다른 처리 없이 바로 사용할  
  수 있다. 문서화 주석에 여러 줄로 된 코드 예시를 넣으려면 `{@code}` 태그를 다시 `<pre>` 태그로 감싸면 된다.  
  다시 말해 `<pre>{@code ... 코드 ...}</pre>` 형태로 쓰면 된다. 이렇게 하면 HTML의 탈출 메타문자를  
  쓰지 않아도 코드의 줄바꿈이 그대로 유지된다. 단, `@` 기호에는 무조건 탈출문자를 붙여야 하니 문서화 주석 안의  
  코드에서 어노테이션을 사용한다면 주의하자.

- 마지막으로 위의 주석에서 쓴 `this list`라는 단어에 주목하자. 관례상, 인스턴스 메소드의 문서화 주석에 쓰인  
  this는 호출된 메소드가 자리하는 객체를 가리킨다.

- 이전에 봤듯, 클래스를 상속용으로 설계할 때는 자기사용 패턴(self-use pattern)에 대해서도 문서에 남겨  
  다른 프로그래머에게 그 메소드를 올바로 재정의하는 방법을 알려줘야 한다. 자기사용 패턴은 Java8에 추가된  
  `@implSpec` 태그로 문서화한다. 다시 말하지만, 일반적인 문서화 주석은 해당 메소드와 클라이언트 사이의  
  계약을 설명한다. 반면, `@implSpec` 주석은 해당 메소드와 하위 클래스 사이의 계약을 설명하여, 하위  
  클래스들이 그 메소드를 상속하거나 super 키워드를 사용해 호출할 때 그 메소드가 어떻게 동작하는지를 명확히  
  인지하고 사용하도록 해줘야 한다.

```java
/**
 * Returns true if this collection is empty.
 *
 * @implSpec
 * This implementation returns {@code this.size() == 0}.
 *
 * @return true if this collection is empty
 */
public boolean isEmpty() { /* ... */ }
```

- Java11 까지도 Javadoc 명령줄에서 `-tag "implSpec:a:Implementation Requirements:"` 스위치를 켜주지 않으면  
  `@implSpec` 태그를 무시해버린다.

- API 설명에 `<`, `>`, `&` 등의 HTML 메타문자를 포함시키려면 특별한 처리를 해줘야한다.  
  가장 좋은 방뻐은 `{@literal}` 태그로 감싸는 것이다. 이 태그는 HTML 마크업이나 Javadoc 태그를  
  무시하게 해준다. 앞서 본 `{@code}`와 비슷하지만, 코드 폰트로 렌더링하지는 않는다.

```java
/**
 * A geometric series converges if {@literal |r| < 1}.
 * ...
 */
```

- 위 코드에서 사실 `<`만 `{@literal}`로 감싸줘도 결과는 똑같지만, 그렇게 하면 코드에서의 문서화 주석을  
  읽기 어려워진다. 문서화 주석은 코드에서든, 변환된 API 문서에서든 읽기 쉬워야 한다는 것이 일반 원칙이다.  
  양쪽을 모두 만족하지 못하겠다면 API 문서에서의 가독성을 우선하자.

- 각 문서화 주석의 첫 번째 문장은 해당 요소의 요약 설명(summary-description)으로 간주된다.  
  요약 설명은 반드시 대상의 기능을 고유하게 기술해야 한다. 헷갈리지 않으려면 **한 클래스 혹은 인터페이스 안에서**  
  **요약 설명이 똑같은 멤버 혹은 생성자가 둘 이상이면 안된다.** 다중정의된 메소드가 있다면 특히 더 조심하자.  
  다중정의된 메소드들의 설명은 같은 문장으로 시작하는 게 자연스럽겠지만, 문서화 주석에서는 허용되지 않는다.

- 요약 설명에서는 마침표(`.`)에 주의해야 한다. 예를 들어서 문서화 주석의 첫 문장이 "See if Mr. Sangwoo is here."라면  
  첫 번째 마침표가 나오는 "See if Mr." 까지만 요약 설명이 된다. 요약 설명이 끝나는 판단 기준은 처음 발견되는  
  `{<마침표> <공백> <다음 문장 시작>}` 패턴의 `<마침표>` 까지다. 여기서 `<공백>`은 space, tab, 줄바꿈이며  
  `<다음 문장 시작>`은 _'소문자가 아닌'_ 문자다. 이 예시에서는 "Mr." 다음에 공백이 나오고, 다음 단어인 "S"가  
  소문자가 아니므로 요약 설명이 끝났다고 판단할 것이다. 가장 좋은 해결책은 의도치 않은 마침표를 포함한  
  텍스트를 `{@literal}`로 감싸주는 것이다. 그래서 위 예시의 요약 설명을 제대로 끝마치려면 아래처럼 하면 된다.

```java
/**
 * See if {@literal Mr. Sangwoo is here}.
 * ..
 */
```

> Java10에는 `@summary`라는 요약 설명 전용 태그가 추가되어, 아래처럼 깔끔히 처리할 수 있다.
>
> ```java
> /*
>  * {@summary See if Mr. Sangwoo is here.}
>  * ..
>  */
> ```

- _"요약 설명이란 문서화 주석의 첫 문장이다"_ 라고 하면 살짝 오해의 소지가 있다. 주석 작성 규약에 따르면  
  요약 설명은 완전한 문장이 되는 경우가 드물기 때문이다. 메소드와 생성자의 요약 설명은 해당 메소드와 생성자의  
  동작을 설명하는 주어가 없는 동사구여야 한다. 아래 예를 보자.

  - `ArrayList(int initialCapacity)` : Constructs an empty list withe the
    specified initial capacity.
  - `Collection.size()` : Returns the number of elements in this collection.

- 한편 클래스, 인터페이스, 필드의 요약 설명은 대상을 설명하는 명사절이어야 한다. 클래스와 인터페이스의 대상은  
  그 인스턴스이고, 필드의 대상은 필드 자신이다. 아래를 보자.

  - `Instant` : An instantaneous point on the time-line.
  - `Math.PI` : The **double** value that is closer than any other to pi,  
    the ratio of the circumference of a circle to its diameter.

- Java9 부터는 Javadoc이 생성한 HTML 문서에 검색(색인) 기능이 추가되어 광대한 API 문서들을  
  누비는 일이 한결 수월해졌다. API 문서 페이지의 오른쪽 상단에 있는 검색창에 키워드를 입력하면  
  관련 페이지들이 나타난다. 클래스, 메소드, 필드 같은 API 요소의 색인은 자동으로 만들어지며, 원한다면  
  `{@index}` 태그를 사용해 직접 API에서 중요한 용어를 추가로 색인화할 수 있다.  
  아래 예시처럼 단순히 색인으로 만들 용어를 태그로 감싸면 된다.

```java
/*
 * This method compiles with the {@index IEEE 754} standard.
 * ..
 */
```

- 문서화 주석에서 제네릭, 열거 타입, 어노테이션은 특별히 주의해야 한다.  
  **제네릭 타입이나 제네레기 메소드를 문서화할 때는 모든 타입 매개변수에 주석을 달아야 한다.**

```java
/**
 * An object that maps keys to values. A map cannot contain duplicate keys;
 * each key can map to at most one value.
 *
 * ..
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public interface Map<K, V> { /* .. */ }
```

- **열거 타입을 문서화할 때는 상수들에도 주석을 달아놔야 한다.**  
  열거 타입 자체와 그 열거 타입의 public 메소드도 물론이다. 설명이 짧다면 주석 전체를 한 문장으로  
  써도 된다.

```java
/**
 * An instrument section of a symphony orchestra.
 */
public enum OrchestraSection {
  /** Woodwinds, such as flute, clarinet, and oboe. */
  WOODWIND,

  /** Brass instruments, such as french horn and trumpet. */
  BRASS,

  /** Percussion instruments, such as timpani and cymbals. */
  PERCUSSION,

  /** Stringed instruments, such as violin and cello. */
  STRING;
}
```

- **어노테이션 타입을 문서화할 때는 멤버들에게도 모두 주석을 달아야 한다.**  
  어노테이션 타입 자체도 물론이다. 필드 설명은 명사구로 한다. 어노테이션 타입의 요약 설명은  
  프로그램 요소에 이 어노테이션을 단다는 것이 어떤 의미인지를 설명하는 동사구로 한다.

```java
/**
 * Indicates that the annotated method is a test method that
 * must throw the designated exception to pass.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionTest {
  /**
   * The exception that the annotated test method must throw
   * in order to pass. (The test is permitted to throw any
   * subtype of the type described by this class object.)
   */
  Class<? extends Throwable> value();
}
```

- 패키지를 설명하는 문서화 주석은 package-info.java 에 작성한다. 이 파일은 패키지 선언을 반드시  
  포함해야 하며 패키지 선언 관련 어노테이션을 추가로 포함할 수도 있다. Java9부터 지원하는  
  모듈 시스템도 이와 비슷하다. 모듈 시스템을 사용한다면 모듈 관련 설명은 module-info.java 파일에  
  작성하면 된다.

- API 문서화에서 자주 누락되는 설명이 자주 있는데, 바로 스레드 안전성과 직렬화 가능성이다.  
  **클래스 혹은 정적 메소드가 스레드 안전하든 그렇지 않든, 스레드 안전 수준을 반드시 API 설명에 포함해야 한다.**  
  또한, 직렬화할 수 있는 클래스라면 직렬화 형태도 API 설명에 기술해야 한다.

- Javadoc은 메소드 주석을 _'상속'_ 시킬 수 있다. 문서화 주석이 없는 API 요소를 발견하면  
  Javadoc이 가장 가까운 문서화 주석을 찾는다. 이때 상위 _'클래스'_ 보다 그 클래스가 구현한  
  _'인터페이스'_ 를 먼저 찾는다.

- 또한 `{@inheritDoc}` 태그를 사용해 상위 타입의 문서화 주석 일부를 상속할 수 있다.  
  클래스는 자신이 구현한 인터페이스의 문서화 주석을 복붙하지 않고 재사용할 수 있다는 뜻이다.  
  이 기능을 활용하면 거의 똑같은 문서화 주석 여러 개를 유지보수하는 부담을 줄일 수 있지만, 사용하기  
  까다롭고 제약도 조금 있다.

- 문서화 주석에 대해 알아야할 주의사항이 하나 더 있다. 비록 공개된 모든 API 요소에 문서화 주석을  
  달았더라도, 이것만으로는 충분하지 않을 때가 있다. 여러 클래스가 상호작용하는 복잡한 API라면  
  문서화 주석 외에도 전체 아키텍쳐를 설명하는 별도의 설명이 필요할 때가 종종 있다. 이런 설명 문서가  
  있다면 관련 클래스나 패키지의 문서화 주석에서 그 문서의 링크를 제공해주면 좋다.

- Javadoc은 프로그래머가 Javadoc 문서를 올바르게 작성했는지 확인하는 기능을 제공하며,  
  이번 아이템에서 본 권장사항 중 상당수를 검사해준다. Java7에서는 명령줄에서 `-Xdoclint` 플래그를  
  제공하면 이 기능이 활성화되고, Java8부터는 기본으로 작동한다.

- 이번 아이템에서 알게된 지침을 잘 따른다면 API를 깔끔히 설명하는 문서를 작성할 수 있다.  
  하지만 정말 잘 쓰인 문서인지를 확인하는 유일한 방법은 **Javadoc 유틸리티가 생성한 웹페이지를**  
  **읽어보는 방법 뿐이다.** 다른 사람이 사용할 API라면 반드시 모든 API 요소를 검토하자.  
  프로그램을 테스트하면 어김없이 수정할 코드가 나오듯이, 생성된 API 문서를 읽어보면 고쳐 써야할  
  주석이 눈에 들어오기 마련이다.

---

## 핵심 정리

- 문서화 주석은 API를 문서화하는 가장 훌륭하고 효과적인 방법이다. 공개 API라면 빠짐없이 설명을  
  달아야 한다. 표준 규약을 일관되게 잘 지키자. 문서화 주석에 임의의 HTML 태그를 사용할 수  
  있음을 기억하자. 단, HTML 메타문자는 특별하게 취급해야 한다.

---
