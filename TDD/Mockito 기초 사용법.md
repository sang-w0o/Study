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
  
