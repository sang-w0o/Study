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

