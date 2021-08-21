<h1>Mockito 기초 사용법</h1>

* `Mockito`는 모의 객체 생성, 검증, Stub을 지원하는 프레임워크이다. `Mockito`는 인기 있는 Java 모의 객체 프레임워크 중 하나이다.

<h2>의존 설정</h2>

* Mockito를 사용하려면 mockito-core 모듈을 추가하면 된다.
```xml
<dependencies>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.4.0</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <version>2.26.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

* Gradle 설정은 아래와 같다.
```gradle
dependencies {
    testCompile('org.mockito:mockito-core:2.26.0')
}
```

<hr/>

<h2>모의 객체 생성</h2>

* `Mockito.mock()` 메소드를 이용하면 특정 타입의 모의 객체를 생성할 수 있다.
```java
public class GameGenMockTest {
    @Test
    void mockTest() {
        GameNumGen genMock = mock(GameNumGen.class);
    }
}
```

* `Mockito.mock()` 메소드는 클래스, 인터페이스, 추상 클래스에 대해 모의 객체를 생성할 수 있다.   
  위 예제에서 `GameNumGen`은 인터페이스라고 가정하자.
```java
public interface GameNumGen {
    String generate(GameLevel level);
}
```

<hr/>

<h2>Stub 설정</h2>

* 모의 객체를 생성한 뒤에는 `BDDMockito` 클래스를 이용해서 모의 객체에 stub을 생성할 수 있다.   
  예를 들어 아래와 같이 `BBDMockito.given()` 메소드를 이용하면 모의 객체의 메소드가 특정 값을 반환하도록 할 수 있다.
```java
public class GameGenMockTest {
    @Test
    void mockTest() {
        GameNumGen genMock = mock(GameNumGen.class);
        given(genMock.generate(GameLevel.EASY)).willReturn("123");

        String num = genMock.generate(GameLevel.EASY);
        assertEquals("123", num);
    }
}
```

* 위 코드에서 `BBDMockito.given()` 메소드는 stub을 정의할 모의 객체의 메소드 호출을 전달한다.   
  `given()` 메소드에 이어 `willReturn()` 메소드는 stub을 정의한 메소드가 반환할 값을 지정한다.   
  즉 위 코드는 `genMock.generate(GameLevel.EASY)`가 호출되면 "1234"을 반환하라고 설정한다.

* 정리 : `given(genMock.generate(GameLEvel.EASY))` --> `genMock.generate(GameLevel.EASY)`가 __호출되는 상황이 주어지면__   
  `willReturn("123")` --> "123"이라는 값을 반환할 것이다.

* 위 코드에서 모의 객체인 genMock에 대해 `generate()`를 호출하는데, 인자 값이 `GameLeve.EASY`이므로 "123"을 반환한다.

* 지정한 값 대신에 예외를 발생시키도록 설정할 수도 있다. 아래 코드를 보자.
```java
@Test
void mockThrowTest() {
    GameNumGen genMock = mock(GameNumGen.class);
    given(genMock.generate(null)).willThrow(IllegalArgumentException.class);

    assertThrows(IllegalArgumentException.class,
        () -> genMock.generate(null)
    );
}
```

* 위 코드에서는 `willReturn()` 대신 `willThrow()`에 인자로 발생시킬 예외의 타입을 전달했다.

* 타입 대신에 예외 객체를 인자로 받는 `willThrow()`를 사용해도 된다. 아래는 예시이다.
```java
given(genMock.generate(null)).willThrow(new IllegalArgumentExcpetion());
```

* 반환값이 없는 void인 메소드에 대해 예외를 발생시키려면 `BBDMockito.willThrow()` 메소드로 시작한다.
```java
public class VoidMethodStubTest {

    @Test
    void voidMethodWillThrowTest() {
        List<String> mockList = mock(List.class);
        willThrow(UnSupportedOperationException.class)
            .given(mockList)
            .clear();

        assertThrows(UnSupportedOperationException.class,
            () -> mockList.clear()
        );
    }
}
```

* `BBDMockito.willThrow()` 메소드는 발생할 예외 타입이나 예외 객체를 인자로 받는다. 이어서 `given()` 메소드는 모의 객체를 전달받는다.   
  모의 객체의 메소드 실행이 아닌 모의 객체임에 유의하자. `given()` 메소드는 인자로 전달받은 모의 객체 자신을 반환하는데 이때   
  예외를 발생할 메소드를 호출한다. 물론 `.clear()`에서 실제로 모의 객체의 메소드를 호출하지 않는다.   
  단지 예외를 발생할 모의 객체를 설정하는 것 뿐이다.

<h3>인자 매칭 처리</h3>

* 아래 코드를 보자.
```java
given(genMock.generate(GameLevel.EASY)).willReturn("123");
String num = genMock.generate(GameLevel.NORMAL);
```

* 위 코드는 stub을 설정할 때 `generate()`의 인자로 `GameLevel.EASY`를 전달하고 있는데, 실제로 `generate()` 메소드를 호출할 때는   
  `GameLevel.NORMAl`을 인자로 전달했다. 이 경우 인자값이 맞지 않으므로 "123"이 아닌 null을 반환한다.

* `Mockito`는 일치하는 stub설정이 없을 경우 리턴 타입의 기본 값을 리턴한다. 예를 들어 리턴 타입이 int면 0을, boolean이면 false를   
  반환한다. 기본 데이터타입이 아닌 String이나 List와 같은 참조 타입이면 null을 리턴한다.

* `org.mockito.ArgumentMatcher`클래스를 이용하면 정확하게 일치하는 값 대신 임의의 값에 일치하도록 설정할 수 있다.
```java
public class AnyMatchersTest {

    @Test
    void anyMatchTest() {
        GameNumGen genMock = mock(GameNumGen.class);
        given(genMock.generate(any())).willReturn("456");

        String num = genMock.generate(GameLevel.EASY);
        assertEquals("456", num);

        String num2 = genMock.generate(GameLevel.HARD);
        assertEquals("456", num);
    }
}
```

* 위 코드에서는 stub을 설정할 때 `ArgumentMatchers.any()` 메소드를 인자 위치에 전달했다.   
  이 메소드를 사용하면 모든 값에 일치하도록 stub을 설정한다. 따라서 아래의 num, num2가 모두 "456"이 되는 것이다.

* `Mockito`와 `BDDMockito`는 `ArgumentMatcher`클래스를 상속하고 있으므로 `ArgumentMatchers.any()` 대신에   
  `Mockito.any()` 또는 `BBDMockito.any()`를 사용해도 된다.

* `ArgumentMatchers` 클래스는 `any()`외에도 다음의 메소드를 제공한다.
  * `anyInt()`, `anyShort()`, `anyLong()`, `anyByte()`, `anyChar()`, `anyDouble()`, `anyFloat()`, `anyBoolean()`   
    (기본 데이터 타입에 대한 임의 값 일치)
  * `anyString()` : 문자열에 대한 임의 값 일치
  * `any()` : 임의 타입에 대한 일치
  * `anyList()`, `anySet()`, `anyMap()`, `anyCollection()` : 임의 컬렉션에 대한 일치
  * `matches(String)`, `matches(Pattern)` : 정규표현식을 이용한 String값 일치 여부
  * `eq(값)` : 특정 값과의 일치 여부
  
* stub을 설정할 메소드의 인자가 두 개 이상인 경우 주의할 점이 있다. 아래 코드를 보자.
```java
List<String> mockList = mock(List.class);
given(mockList.set(anyInt(), "123")).willReturn("456");
String old = mockList.set(5, "123");
```

* `mockList.set()` 메소드의 stub을 설정할 때 첫 번째 인자는 `anyInt()`를 주어 임의의 int 값에 일치하도록 했고,   
  두 번째 인자는 "123"을 사용해서 정확한 값에 일치하도록 했다. 그리고 `mockList.set(5, "123")`을 실행하고 있다.   
  이 코드는 문제가 없어보이지만 실제로 실행하면 예외가 발생한다.

* `ArgumentMatchers`의 `anyInt()`나 `any()`등의 메소드는 내부적으로 인자의 일치 여부를 판단하기 위해 `ArgumentMatcher`를 등록한다.   
  `Mockito`는 한 인자라도 `ArgumentMatcher`를 사용해서 설정한 경우 __모든 인자를 `ArgumentMatcher`를 이용해서 설정하도록 한다__.

* 임의의 값과 일치하는 인자와 정확하게 일치하는 인자를 함께 사용하고 싶다면 아래와 같이 `ArgumentMatcher.eq()`를 사용해야 한다.
```java
@Test
void mixAnyAndEq() {
    List<String> mockList = mock(List.class);
    given(mockList.set(anyInt(), eq("123"))).willReturn("456");
    String old = mockList.set(5, "123");
    assertEquals("456", old);
}
```
<hr/>

<h2>행위 검증</h2>

* 모의 객체의 역할 중 하나는 실제로 모의 객체가 호출되었는지를 검증하는 것이다. 이 예시는 아래와 같다.
```java
public class GameTest {

    @Test
    void init() {
        GameNumGen genMock = mock(GameNumGen.class);
        Game game = new Game(genMock);
        game.init(GameLevel.EASY);

        then(genMock).should().generate(GameLevel.EASY);
    }
}
```

* `BBDMockito.then()`은 메소드 호출 여부를 검증할 모의 객체를 전달받는다.   
  `should()` 메소드는 모의 객체의 메소드가 불려야 한다는 것을 설정하고, `should()` 다음에 실제로 호출되어야할 메소드를 지정한다.   
  위 코드는 `genMock` 객체의 `generate()`가 `GameLevel.EASY` 인자를 사용해서 호출되었는지를 검증한다.

* 정확한 값이 아니라 메소드의 호출 여부가 중요하다면 `any()`, `anyInt()`등을 사용해서 인자를 지정하면 된다.
```java
then(genMock).should().generate(any());
```

* 정확하게 한 번만 호출된 것을 검증하고 싶다면 `should()` 메소드의 인자로 `Mockito.only()`를 전달하면 된다.
```java
then(genMock).should(only()).generate(any());
```

* 메소드 호출 횟수를 검증하기 위해 `Mockito`가 제공하는 메소드는 아래와 같다. (`should()`의 인자로 들어간다.)
  * `only()` : 한 번만 호출
  * `times(int)` : 지정한 횟수만큼 호출
  * `never()` : 호출하지 않음
  * `atLeast(int)` : 적어도 지정한 횟수만큼 호출
  * `atLeastOnce()` : `atLeast(1)`과 동일
  * `atMost(int)` : 최대 지정한 횟수만큼 호출
<hr/>

<h2>인자 캡쳐</h2>

* 단위 테스트를 실행하다보면 모의 객체를 호출할 때 사용한 인자를 검증해야할 때가 있다.   
  String이나 int와 같은 타입은 쉽게 검증할 수 있지만 많은 속성을 가진 객체는 쉽게 검증하기 어렵다.   
  이럴 때 사용할 수 있는 것이 `인자 캡쳐` 이다.

* `Mockito`의 `ArgumentCaptor`를 사용하면 메소드 호출 여부를 검증하는 과정에서 실제 호출할 때 전달한 인자를 보관할 수 있다.
```java
public class UserRegisterMockTest {

    private UserRegister userRegister;
    private EmailNotifier mockEmailNotifier = mock(EmailNotifier.class);

    //..

    @Test
    void whenRegisterThenSendEmail() {
        userRegister.register("id", "pw", "email@email.com");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        then(mockEmailNotifier)
            .should().sendRegisterEmail(captor.capture());
        
        String realEmail = captor.getValue();
        assertEquals("email@email.com", realEmail);
    }
}
```

* `ArgumentCaptor.forClass(String.class)`는 String 타입의 인자를 보관할 수 있는 `ArgumentCaptor`를 생성한다.   
  이렇게 생성된 객체를 모의 객체 호출 여부를 검증하는 코드에서 인자로 전달한다.   
  인자로 전달할 때에는 `ArgumentCaptor#capture()`를 전달한다.

* 모의 객체를 실행할 때 사용한 인자값은 `ArgumentCaptor#getValue()`로 구할 수 있다.
<hr/>

<h2>JUnit5 확장 설정</h2>

* `Mockito`의 JUnit 5 확장 기능을 사용하면 어노테이션을 이용해서 모의 객체를 생성할 수 있다. 아래 의존을 추가해보자.
```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <version>2.26.0</version>
    <scope>test</scope>
</dependency>
```

* 의존을 추가하면 `MockitoExtension` 확장을 사용할 수 있다. 이 확장 기능을 사용하면   
  `@Mock` 어노테이션을 붙인 필드에 대해 자동으로 모의 객체를 생성해준다. 아래는 예시이다.
```java
@ExtendWith(MockitoExtension.class)
public class JUnit5ExtensionTest {

    @Mock
    private GameNumGen genMock;

    //..
}
```
<hr/>