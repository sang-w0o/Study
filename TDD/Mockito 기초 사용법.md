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