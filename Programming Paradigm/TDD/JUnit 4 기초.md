<h1>JUnit 4 기초</h1>

<h2>의존 설정</h2>

* JUnit 5는 여러 모듈로 구성되어 있는데 반해 JUnit 4의 모듈 구성은 간단하다. 아래는 메이븐 의존성이다.
```xml
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.12</version>
    <scope>test</scope>
</dependency>
```

* 아래는 gradle의 의존성이다.
```gradle
dependencies {
    testCompile('junit:junit:4.12')
}

test {
    useJUnit()
}
```

* JUnit 5는 Java 8이나 그 이상의 버전이 필요하지만, JUnit 4는 Java 1.5 이상에서 작동한다.
<hr/>

<h2>기본 테스트 어노테이션</h2>

* JUnit 4의 기본 테스트 어노테이션은 `@Before`, `@Test`, `@After`이다. 아래는 JUnit 4의 테스트 코드 기본 골격이다.
```java
public class AppTest {

    @Before
    public void setUp() {
        //..
    }

    @Test
    public void plus() {
        assertEquals(2, 1 + 1);
    }

    @After
    public void tearDown() {
        //..
    }
}
```

* 각 어노테이션은 다음의 JUnit 5 어노테이션에 대응한다.
  * `@Before` --> JUnit 5의 `@BeforeEach`
  * `@Test` --> JUnit 5의 `@Test`
  * `@After` --> JUnit 5의 `@AfterEach`

* 테스트 메소드는 JUnit 5와 동일하게 아래의 순서대로 실행된다.
  1. 테스트 클래스 객체 생성
  2. `@Before` 메소드 실행
  3. `@Test` 메소드 실행
  4. `@After` 메소드 실행

* JUnit 5와 달리 JUnit 4의 테스트 메소드는 public이어야 한다.   
  `@Before`과 `@After`도 public 메소드에 붙여야 한다.
<hr/>

<h2>단언 메소드</h2>

* `org.junit.Assert` 클래스는 `assertEquals()`를 포함한 기본적인 검증 메소드를 제공하며, 아래는 메소드들의 목록이다.

<table>
    <tr>
        <td>메소드</td>
        <td>설명</td>
    </tr>
    <tr>
        <td>assertEquals(expected, actual)</td>
        <td>actual과 expected가 동일한지 검사한다.</td>
    </tr>
    <tr>
        <td>assertNotEquals(unexpected, actual)</td>
        <td>unexpected와 actual이 다른지 검사한다.</td>
    </tr>
    <tr>
        <td>assertSame(Object expected, Object actual)</td>
        <td>두 객체가 동일한 객체인지 검사한다.</td>
    </tr>
    <tr>
        <td>assertNotSame(Object expected, Object actual)</td>
        <td>두 객체가 동일하지 않은 객체인지 검사한다.</td>
    </tr>
    <tr>
        <td>assertTrue(boolean condition)</td>
        <td>condition이 true인지 검사한다.</td>
    </tr>
    <tr>
        <td>assertFalse(boolean condition)</td>
        <td>condition이 false인지 검사한다.</td>
    </tr>
    <tr>
        <td>assertNull(Object actual)</td>
        <td>actual객체가 null인지 검사한다.</td>
    </tr>
    <tr>
        <td>assertNotNull(Object actual)</td>
        <td>actual객체가 null이 아닌지 검사한다.</td>
    </tr>
    <tr>
        <td>fail()</td>
        <td>테스트를 실패 처리한다.</td>
    </tr>
</table>

* JUnit 4는 `assertAll()`이나 `assertThrows()`는 제공하지 않는다.   
  JUnit 4에서 예외 발생 여부를 테스트할 때에는 `@Test` 어노테이션의 expected 속성을 이용한다. 아래는 예시이다.
```java
@Test(expected = ArithmeticException.class)
public void throwEx() {
    divide(1, 0);
}
```

* 발생한 예외 객체를 사용해서 추가 검증을 해야할 때는 expected 속성을 사용할 수 없다.   
  대신 아래와 같이 try-catch를 이용해서 직접 검증 처리를 해야 한다.
```java
ArithmeticException thrown = null;
try {
    divide(1, 0);
} catch(ArithmeticException exception) {
    thrown = exception;
}

assertNotNull(thrown);
assertTrue(thrown.getMessage().contains("zero"));
```
<hr/>