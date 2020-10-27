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

