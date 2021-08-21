<h1>JUnit 5 기초</h1>

<h2>JUnit 5 모듈 구성</h2>

* JUnit 5는 크게 세 개의 요소로 구성되어 있다.
  * JUnit Platform : 테스팅 프레임워크를 구동하기 위한 런처와 테스트 엔진을 위한 API를 제공한다.
  * JUnit Jupiter : JUnit 5를 위한 테스트 API와 실행 엔진을 제공한다.
  * JUnit Vintage : JUnit 3, 4로 작성된 테스트를 JUnit 5 플랫폼에서 실행하기 위한 모듈을 제공한다.

* 이들 구성 요소의 주요 모듈 구조는 아래와 같다.

![](2020-10-27-14-30-23.png)

* JUnit 5는 테스트를 위한 API로 Jupiter API를 제공한다. Jupiter API를 사용해서 테스트를 작성하고 실행하려면 아래와 같이   
  Jupiter 관련 모듈을 의존에 추가하면 된다.
```xml
<dependencies>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.5.0</version>
        <scope>test</scope>
    </dependency>
    <!-- other dependencies -->
</dependencies>

<build>
    <plugins>
        <plugin>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>2.22.1</version>
        </plugin>
        <!-- other plugins -->
    </plugins>
</build>
```

* JUnit5를 이용해서 테스트를 실행하려면 JUnit5 플랫폼이 제공하는 플랫폼 런처를 사용해야 한다. Maven은 `maven-surefire-plugin 2.22.0`   
  버전부터 JUnit5 플랫폼을 지원하므로 따로 플랫폼을 설정하지 않아도 된다.

* Gradle도 유사하다.
```gradle
apply plugin: 'java'
apply plugin: 'eclipse'

group 'org.tdd'
version '1.0-SNAPSHOT'
sourceCompatibility = 1.8

repositories {
    mavenCentral()
}

dependencies {
    testCompile('org.junit.jupiter:junit-jupiter:5.5.0')
}

test {
    useJUnitPlatform()
    testLogging {
        events "passed", "skipped", "failed"
    }
}
```
<hr/>

<h2>@Test 어노테이션과 테스트 메소드</h2>

* JUnit 5 모듈을 설정했다면 JUnit을 이용해서 테스트 코드를 작성하고 실행할 수 있다. JUnit 코드의 기본 구조는 간단하다.   
  테스트로 사용할 클래스를 만들고 `@Test` 어노테이션을 메소드에 붙이기만 하면 된다.
```java
public class SumTest {

    @Test
    void sum() {
        int result = 2 + 3;
        assertEquals(5, result);
    }
}
```

* 테스트 클래스의 이름을 작성하는데에 특별한 규칙은 없지만 보통 다른 클래스와의 구분을 위해 Test를 접미사로 붙인다.   
  테스트를 실행할 메소드에는 `@Test` 어노테이션을 붙이는데, 이때 테스트 메소드는 __private이면 안된다.__

* JUnit의 `Assertions` 클래스는 `assertEquals()`와 같이 값을 검증하기 위한 목적의 다양한 정적 메소드를 제공한다.
<hr/>

<h2>주요 단언 메소드</h2>

* `Assertions` 클래스는 `assertEquals()`를 포함해 아래 표의 다양한 단언 메소드를 제공한다.

<table>
    <tr>
        <td>메소드</td>
        <td>설명</td>
    </tr>
    <tr>
        <td>assertEquals(Object expected, Object actual)</td>
        <td>actual이 expected와 같은지 검사한다.</td>
    </tr>
    <tr>
        <td>assertNotEquals(Object unexpected, Object actual)</td>
        <td>actual이 특정 값(unexpected)와 같지 않은지 검사한다.</td>
    </tr>
    <tr>
        <td>assertSame(Object expected, Object actual)</td>
        <td>두 객체가 동일한 객체인지 검사한다.</td>
    </tr>
    <tr>
        <td>assertNotSame(Object unexpected, Object actual)</td>
        <td>두 객체가 동일하지 않은 객체인지 검사한다.</td>
    </tr>
    <tr>
        <td>assertTrue(boolean condition)</td>
        <td>값이 true인지 검사한다.</td>
    </tr>
    <tr>
        <td>assertFalse(boolean condition)</td>
        <td>값이 false인지 검사한다.</td>
    </tr>
    <tr>
        <td>assertNull(Object actual)</td>
        <td>값이 null인지 검사한다.</td>
    </tr>
    <tr>
        <td>assertNotNull(Object actual)</td>
        <td>값이 null이 아닌지 검사한다.</td>
    </tr>
    <tr>
        <td>fail()</td>
        <td>테스트를 실패 처리한다.</td>
    </tr>
</table>

* 주요 타입별로 `assertEquals()` 메소드가 존재한다. 주의점은 첫 번째 인자가 기대하는 값이고, 두 번째 인자가 검사하려는 값이라는 점이다.

* `assertEquals(Object expected, Object actual)` 메소드는 `equals()` 메소드를 이용해서 두 객체가 같은지를 비교한다.

* `fail()` 메소드는 테스트에 실패했음을 알리고 싶을 때 사용한다. 아래는 예시이다.
```java
try {
    AuthService service = new AuthService();
    service.authenticate(null, null);
    fail();  // 이 지점에 다다르면 이 메소드의 테스트 실패 에러를 발생시킨다.
} catch(IllegalArgumentException exception){}
```

* 만약 Exception의 발생 유무가 검증 대상이라면 `fail()` 보다는 아래의 두 메소드를 사용하는 것이 더욱 명시적이다.

<table>
    <tr>
        <td>메소드</td>
        <td>설명</td>
    </tr>
    <tr>
        <td>assertThrows(Class&lt;T&gt; expectedType, Executable executable)</td>
        <td>executable을 실행한 결과로 지정한 타입의 예외가 발생하는지 검사한다.</td>
    </tr>
    <tr>
        <td>assertDoesNotThrow(Executable executable)</td>
        <td>executable을 실행한 결과로 예외가 발생하지 않는지 검사한다.</td>
    </tr>
</table>

* 아래는 `assertThrows()`를 이용해서 지정한 예외가 발생하는지 검사하는 코드이다.
```java
assertThrows(IllegalArgumentException.class,
    () -> {
        AuthService service = new AuthService();
        service.authenticate(null, null);
    });
```

* `assertThrows()` 메소드는 발생한 `Exception` 객체를 반환한다. 발생한 `Exception`을 이용해서 추가로 검증이 필요하면   
  반환된 `Exception` 객체를 사용하면 된다.
```java
IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
    () -> {
        AuthService service = new AuthService();
        service.authenticate(null, null);
    });
assertTrue(thrown.getMessage().contains("id"));
```

* 참고로 `assertThrows()`와 `assertDoesNotThrow()` 메소드에서 사용하는 `Executable` 인터페이스는 아래와 같이 `execute()`   
  메소드를 가진 함수형 인터페이스이다.
```java
public interface Executable {
    void execute() throws Throwable;
}
```

* assert 메소드는 실패하면 다음 코드를 수행하지 않고 바로 예외를 발생시킨다. 하지만 경우에 따라 일단 모든 검증을 실행하고   
  그중에 실패한 것이 있는지 확인해야할 때가 있다. 이럴 때 사용하는 것이 `assertAll()` 메소드이다. 아래는 예시이다.
```java
assertAll(
    () -> assertEquals(3, 5 / 2),
    () -> assertEquals(4, 2 * 2),
    () -> assertEquals(6, 11 / 2)
);
```

* `assertAll()` 메소드는 `Executable` 목록을 가변인자로 전달받아 각 `Executable`을 실행한다.   
  실행 결과로 검증에 실패한 코드가 있으면 그 목록을 모아서 에러 메시지로 띄워준다.
<hr/>

<h2>테스트 Lifecycle</h2>

<h3>@BeforeEach와 @AfterEach 어노테이션</h3>

* JUnit은 각 테스트 메소드마다 다음 순서대로 코드를 실행한다.
  1. 테스트 메소드를 포함한 객체 생성
  2. (존재하면)`@BeforeEach` 어노테이션이 붙은 메소드 실행
  3. `@Test` 어노테이션이 붙은 메소드 실행
  4. (존재하면)`@AfterEach` 어노테이션이 붙은 메소드 실행

* 아래 코드는 동작 방식을 이해하기 위한 간단한 코드이다.
```java
public class LifecycleTest {

    public LifecycleTest() {
        System.out.println("New LifecycleTest");
    }

    @BeforeEach
    void setUp() {
        System.out.println("setUp()")
    }

    @Test
    void a() {
        System.out.println("a()");
    }

    @Test
    void b() {
        System.out.println("b()");
    }

    @AfterEach
    void tearDown() {
        System.out.println("tearDown()");
    }
}
```

* 위 코드의 실행결과는 아래와 같다.
```
new LifecycleTest
setUp()
a()
tearDown()
new LifecycleTest
b()
tearDown()
```

* 이 결과를 보면 `@Test` 메소드를 실행할 때마다 객체를 새로 생성하고, 테스트 메소드를 실행하기 전과 후에 `@BeforeEach`와   
  `@AfterEach` 어노테이션을 붙인 메소드를 실행한다는 것을 알 수 있다.

* `@BeforeEach` 어노테이션은 테스틀르 실행하는데에 필요한 준비 작업을 할 때 사용한다. 이 어노테이션을 이용해서   
  테스트에서 사용할 임시 파일을 생성한다거나 테스트 메소드에서 사용할 객체를 생성한다.

* `@AfterEach`어노테이션은 테스트를 실행한 후에 정리할 것이 있을 때 사용한다. 테스트에서 사용한 임시 파일을   
  삭제해야 할 때 이 어노테이션을 적용하면 된다.

* `@BeforeEach`와 `@AfterEach` 어노테이션을 붙인 메소드는 `@Test`와 마찬가지로 private이면 안된다.

<h3>@BeforeAll와 @AfterAll 어노테이션</h3>

* 한 클래스의 모든 테스트 메소드가 실행되기 전에 특정 작업을 수행해야 한다면 `@BeforeAll`을 사용한다.   
  이 어노테이션은 정적 메소드에 붙이는데, 이 메소드는 클래스의 모든 테스트 메소드를 실행하기 전에 한번 실행된다.

* `@AfterAll` 어노테이션은 반대로 클래스의 모든 테스트 메소드를 실행한 뒤에 실행된다.   
  이 역시 정적 메소드에 적용해야 한다.
<hr/>

<h2>테스트 메소드 간 실행 순서 의존과 필드 공유하지 않기</h2>

* 아래 코드를 보자.
```java
public class BadTest {

    private FileOperator operator = new FileOperator();
    private static File file;  // 두 테스트가 데이터를 공유할 목적으로 필드 사용

    @Test
    void fileCreateTest() {
        File createFile = operator.createFile();
        assertTrue(createdFile.length() > 0);
        this.file = createFile;
    }

    @Test
    void fileReadTest() {
        long data = operator.readData(file);
        assertTrue(data > 0);
    }
}
```

* 위 코드는 file 필드를 사용해서 `fileCreateTest()`에서 생성한 `File`객체를 보관하고, 그 file 필드를   
  `readFileTest()`에서 사용한다. 테스트 메소드를 실행할 때마다 객체를 새로 생성하므로 file을 정적 필드로 정의했다.   
  이 테스트는 `fileCreateTest()`가 `readFileTest()`보다 먼저 실행된다는 것을 가정한다.

* 실제로 원하는 순서대로 테스트 메소드가 실행될 수도 있지만, 이러한 가정 하에 테스트 메소드를 작성하면 안된다.   
  JUnit이 테스트 순서를 결정하긴 하지만 그 순서는 버전에 따라 달라질 수 있다. 순서가 달라지면 테스트도 실패한다.   
  예를 들어 `readFileTest()`가 먼저 실행되면 file이 null이므로 테스트가 실패하게 된다.

* 각 테스트 메소드는 서로 __독립적__ 으로 동작해야 한다. 한 테스트 메소드의 결과에 따라 다른 테스트 메소드의 실행 결과가   
  달라지면 안된다. 그런 의미에서 테스트 메소드가 서로 필드를 공유한다거나 실행 순서를 가정하고 테스트를 작성하면 안된다.
<hr/>

<h2>추가 어노테이션 : @DisplayName, @Disabled</h2>

* 테스트 실행 결과를 보면, 테스트 메소드명을 이용해서 테스트 결과를 콘솔에 보여준다.

* Java는 메소드명에 공백이나 특수문자를 사용할 수 없기에 메소드명만으로 테스트 내용을 설명하기 부족할 수 있다.   
  이럴 때는 `@DisplayName` 어노테이션을 사용해서 테스트에 표시될 이름을 붙일 수 있다.
```java
@DisplayName("@DisplayName 테스트")
public class DisplayNameTest {

    @DisplayName("값 같은지 비교")
    @Test
    void assertEqualsMethod() {

        //..
    }

    @DisplayName("예외 발생 여부 테스트")
    @Test
    void assertThrowsTest {

        //..
    }
}
```

* 특정 테스트를 실행하고 싶지 않을때에는 `@Disabled` 어노테이션을 사용한다.   
  JUnit은 `@Disabled` 어노테이션이 붙은 클래스나 메소드는 테스트 실행 대상에서 제외한다.
<hr/>

<h2>모든 테스트 한번에 실행하기</h2>

* 모든 테스트를 실행하는 방법은 간단하다.
  * Maven : `mvn test`(래퍼 사용 시 `mvnw test`)
  * Gradle : `gradle test`(래퍼 사용 시 `gradlew test`)
<hr/>