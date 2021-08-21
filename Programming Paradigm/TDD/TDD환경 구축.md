<h1>TDD 개발환경 구축</h1>

* Gradle 프로젝트에서 `JUnit`을 사용하기 위한 설정은 아래와 같다.
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

* `testCompile('org.junit.jupiter:junit-jupiter:5.5.0)` 의존을 테스트의 목적으로 추가했으며, `JUnit 5`로 작성된 테스트 코드를 실행하기 위해   
  `useJUnitPlaftorm()`을 설정했다.

* 테스트를 위한 코드는 `src/test/java`의 하위에 둔다.
<hr/>