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

