<h1>JUnit 5 추가 내용</h1>

<h2>조건에 따른 테스트</h2>

* JUnit 5는 조건에 따라 테스트를 실행할지 여부를 결정하는 기능을 제공하는데, 이 어노테이션들에 대해 살펴보자.
  * `@EnabledOnOs`, `@DisabledOnOs`
  * `@EnabledOnJre`, `@DisabledOnJre`
  * `@EnabledIfSystemProperty`, `@DisabledIfSystemProperty`
  * `@EnabledIfEnvironmentVariable`, `@DisabledIfEnvironmentVariable`

* 테스트 메소드가 특정 운영체제에서만 동작해야 한다면 `@EnabledOnOs` 어노테이션을 사용한다.   
  반대의 경우는 `@DisabledOnOs` 어노테이션을 사용한다. 아래는 예시이다.
```java
public class OsTmpPathTest{

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void windowTmpPath() {
        Path tmpPath = Paths.get("C:\\temp");
        assertTrue(File.isDirectory(tmpPath));
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void linuxTmpPath() {
        Path tmpPath = Paths.get("/tmp");
        assertTrue(File.isDirectory(tmpPath));
    }
}
```

* `OS` 타입은 열거 타입으로 `WINDOWS`, `MAC`, `OS`와 같은 운영체제 이름을 값으로 정의하고 있다.   
  `@EnabledOnOs` 어노테이션과 `@DisabledOnOs` 어노테이션은 `OS` 열거 타입을 인자로 사용해서 테스트를 실행하거나   
  실행하지 않을 운영체제 조건을 지정한다.

* Java 버전에 따라 테스트를 실행하고 싶다면 `@EnabledOnJre` 어노테이션을 사용한다.   
  반대로는 `@DisabledOnJre` 어노테이션을 사용한다.
```java
@Test
@EnabledOnJre({JRE.JAVA_8, JRE.JAVA_9. JRE.JAVA_10, JRE.JAVA_11})
void testOnJre() {
    assertEquals(LocalDate.of(1919, 3, 1), LocalDate.of(2019, 3, 1).minusYears(100));
}
```

* `JRE` 열거 타입은 Java의 버전을 정의하고 있다. JUnit 5.4.0 기준으로 `JRE` 열거 타입은 `JAVA_8`부터 `JAVA_13`까지의 값을   
  정의하고 있으며 추자로 `OTHER` 값을 정의할 수 있다. Java 7이나 Java 6을 사용할 때 `JRE.OTHER`를 사용할 수 있지만,   
  Java 8부터 JUnit 5 를 사용할 수 있기 때문에 실질적으로 `JRE.OTHER`를 사용할 일은 없다.

* `@EnabledIfSystemProperty` 어노테이션과 `@DisabledIfSystemProperty` 어노테이션은 시스템 프로퍼티 값을 비교하여   
  테스트 실행 여부를 결정한다. 아래 코드는 예시인데, `java.vm.name` 시스템 프로퍼티 값이 OpenJDK를 포함하고 있으면   
  해당 테스트를 실행한다.
```java
@Test
@EnabledIfSystemProperty(named = "java.vm.name", matches = ".*OpenJDK.*")
void openJdk() {
    assertEquals(2, 1 + 1);
}
```

* `@EnabledIfSystemProperty` 어노테이션의 named 속성은 시스템 프로퍼티의 이름을 지정하고,   
  matches 속성에는 값의 일치 여부를 검사할 때 사용할 정규 표현식을 지정한다.

* `@EnabledIfEnvironmentVariable`도 named 속성과 matches 속성을 사용한다.   
  차이점은 named 속성에 환경 변수 이름을 사용한다는 것이다.
<hr/>

