Spring Boot 프로젝트의 생성
======

* 먼저 intellij의 gradle 프로젝트를 생성하고, `build.gradle` 파일을 아래와 같이 수정한다.
```js
buildscript {
    ext{
        springBootVersion = '2.1.7.RELEASE'
    }
    repositories {
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath("org.springframework.bootspring-boot-gradle-plugin:${springBootVersion}")
    }
}

apply plugin: 'java'
apply plugin: 'eclipse'
apply plugin: 'org.springframework.boot'
apply plugin: 'io.spring.dependency-management'

group 'org.sangwoo.book'
version '1.0-SNAPSHOT'

sourceCompatibility = 1.8

repositories {
    mavenCentral()
}

dependencies {
    compile('org.springframework.boot:spring-boot-starter-web')
    testCompile('org.springframework.boot:spring-boot-starter-test')
}
```
* 위에서 추가한 이 프로젝트의 의존성 관리를 위한 설정이다. `ext`키워드는 `build.gradle`에서 사용하는 전역 변수를 설정한다는 것인데,   
  위에서는 `springBootVersion`을 전역변수로 생성하고, 그 값을 "2.1.7.RELEASE"로 지정하고, 그 값을 `dependencies`절에서 사용했다.
* `apply plugin:` 절에서 지정한 `io.spring.dependency-management`는 Spring Boot의 의존성들을 관리해주는 플러그인이며,   
  그 외 3가지는 Java와 Spring Boot를 사용하기 위한 필수 플러그인이다.
* `repositories`는 각종 의존성(라이브러리)들을 어떠한 원격 저장소에서 받을 지를 지정하는 것이다.   
  기본적으로는 `mavenCentral`을 많이 사용하지만, 최근에는 `jcenter`도 많이 사용하는 추세이다.
  * `jcenter`는 `mavenCentral`에 비해 라이브러리 업로드 시, 간단한 과정과 jcenter에 업로드하면 mavenCentral에 자동으로   
    업로드될 수 있게 됐기 때문에 `jcenter` 의 사용량이 높아지는 추세이다.
* `dependencies`는 프로젝트 개발에 필요한 의존성들을 선언하는 절인데, 위에서는 두 가지 의존성을 설정했다.
<hr/>

<h2>TestCode 작성의 기본</h2>

* TDD(Test-Driven Development)와 Unit Test는 다른 것이다. TDD는 __테스트 코드를 작성하는 것__ 에서 시작한다.   
  반면 Unit Test는 __기능 단위의 테스트 코드를 작성하는 것__ 을 의미한다. 먼저 테스트 코드를 보자.
* Test Code를 작성함으로써 얻는 이점은 다음과 같다.
  * Unit Test는 개발단계 초기에 문제를 발견하게 도와준다.
  * Unit Test는 개발자가 나중에 코드를 refactoring하거나 라이브러리 업그레이드 등에서 기존 기능이 올바르게 작동하는지 확인할 수 있다.
  * Unit Test는 기능에 대한 불확실성을 감소시킬 수 있다.
  * Unit Test는 시스템에 대한 실제 문서를 제공한다. 즉, Unit Test 자체가 문서로 사용될 수 있다.
* 즉, 새로운 기능이 추가될 때, Unit Test의 test code를 사용하면 __기존 기능이 잘 작동함을 보장__ 할 수 있다.

* 대표적인 Unit Test Framework로는 xUnit이 있는데, Java에서는 `JUnit`을 제공한다.
<hr/>

<h3>간단한 Test Code 작성</h3>

```java
package com.sangwoo.chap1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

* 위에서 작성한 `Application` 클래스는 프로젝트의 Main Class가 된다.   
  __@SpringBootApplication__ 어노테이션으로 인해 Spring Boot의 자동 설정, Spring Bean읽기와 생성을 모두 자동으로 설정한다.   
  특히 __@SpringBootApplication__ 이 있는 위치부터 설정을 읽어가기 때문에 이 클래스는 항상 __프로젝트의 최상단에 위치__ 해야 한다.

* `main()` 메소드 내의 `SpringApplication.run()`으로 인해 내장 WAS를 실행한다.   
  이렇게 내장 WAS를 사용하게 되면 Tomcat을 사용할 필요가 없고, Spring Boot로 만들어진 Jar파일을 실행하면 된다.

* Spring Boot는 __언제 어디서나 같은 환경에서 Spring Boot를 배포__ 하기 위해 내장 WAS의 사용을 권장하고 있다.

```java
package com.sangwoo.chap1.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }
}
```
* __@RestController__ : 컨트롤러를 JSON을 반환하는 컨트롤러로 만들어준다.
* __@GetMapping__ : Http Method 중 하나인 GET요청을 받을 수 있는 API를 만들어 준다.

* 다음으로는 Test Code를 작성할 `HelloControllerTest` 클래스를 만들어보자. 관례적으로 테스트 클래스는 대상 클래스 이름의   
  뒤에 Test를 붙인다. 이 클래스는 `src/test/java` 하위에 만든다.
```java
package com.sangwoo.chap1.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = HelloController.class)
public class HelloControllerTest {
    
    @Autowired
    private MockMvc mvc;
    
    @Test
    public void hello_is_returned() throws Exception {
        String hello = "hello";
        
        mvc.perform(get("/hello")).andExpect(status().isOk()).andExpect(content().string(hello));
    }
}
```
* __@RunWith(SpringRunner.class)__ : 테스트를 진행할 때 `JUnit`에 내장된 실행자 외에 다른 실행자를 실행시킨다.   
  위에서는 `SpringRunner`라는 Spring 실행자를 사용한다. 즉, Spring Boot test와 JUnit 사이의 연결자 역할을 한다.
* __@WebMvcTest__ : Spring MVC의 테스트 어노테이션으로, 이 어노테이션을 적용할 경우, 해당 클래스 내에서는   
  __@Controller__, __@ControllerAdvice__ 등은 사용할 수 있다. 하지만 __@Service__, __@Component__, __@Repository__ 등은 사용하지 못한다.
* __@Autowired__ : Spring이 관리하는 Bean을 주입받는다.
* `private MockMvc mvc` : Web API를 테스트할 때 사용하는 것으로, Spring MVC test의 시작점이다.   
  이 객체를 통해 HTTP GET, POST 등에 대한 API Test를 할 수 있다.
* `mvc.perform(get("/hello"))` : `MockMvc`를 통해 /hello 주소로 HTTP GET 요청을 한다.
* `.andExpect(status().isOk())` : `mvc.perform()`의 결과를 검증하는 것으로, Http Header의 Status를 검증한다.   
  이 메소드에서는 OK, 즉 Status Code가 200인지 아닌지를 검증한다.
* `.andExpect(content.string(hello))` : `mvc.perform()`의 결과를 검증하는 것으로, 응답 본문의 내용을 검증한다.   
  `HelloController`에서 /hello 경로의 GET 요청에 대해 "hello"를 반환하기 때문에 이 값이 맞는지를 검증한다.

* __TestCode 작성을 습관화하고, 마지막으로 브라우저 상에서 검증하자__.
<hr/>

<h2>Lombok의 소개 및 설치</h2>

* `Lombok`은 Java 개발자들의 필수 라이브러리로, Getter, Setter, 기본 생성자, toString 등의 메소드들을 어노테이션으로   
  자동 생성해준다.
* Lombok설치를 위해서는 `build.gradle`의 `dependencies` 절에 다음 코드를 추가한다.
```js
compile('org.projectlombok:lombok')
```
* 그 후에는 Plugins 창에서 lombok를 Marketplace에서 받으면 된다.
<hr/>

<h3>HelloController 코드를 Lombok으로 전환하기</h3>

```java
package com.sangwoo.chap1.web.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class HelloResponseDto {
    private final String name;
    private final int amount;
}
```
* __@Getter__ : 선언된 모든 필드의 getter 메소드를 생성해준다.
* __@RequiredArgsConstructor__ : 선언된 모든 __final 필드가 포함된__ 생성자를 생성해준다.   
  final이 없는 필드는 생성자에 포함되지 않는다.

* 다음으로는 위의 `HelloResponseDto`의 테스트 코드인 `HelloResponseDtoTest`클래스를 작성해보자.
```java
package com.sangwoo.chap1.web.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HelloResponseDtoTest {
    
    @Test
    public void lombok_test() {
        //given
        String name = "test";
        int amount = 1000;
        
        //when
        HelloResponseDto dto = new HelloResponseDto(name, amount);
        
        //then
        assertThat(dto.getName()).isEqualTo(name);
        assertThat(dto.getAmount()).isEqualTo(amount);
    }
}
```
* `assertThat()` 메소드는 assertj 테스트 검증 라이브러리의 검증 메소드이다. 이 메소드는 검증하고 싶은 대상을   
  인자로 받으며, 메소드 체이닝이 지원되어 `isEqualTo()` 등의 메소드를 이어서 사용할 수 있다.
* `isEqualTo()` 메소드는 assertj의 동등 비교 메소드로 `assertThat()`에 있는 값과 `isEqualTo()`의 인자의 값을 비교해서   
  같을 때 성공으로 판단한다.

* 이제 `HelloController`에 `HelloResponseDto`를 사용하도록 코드를 추가해보자.
```java
@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }
    
    @GetMapping("/hello/dto")
    public HelloResponseDto helloDto(@RequestParam("name") String name, @RequestParam("amount") int amount) {
        return new HelloResponseDto(name, amount);
    }
}
```
* __@RequestParam__ 은 외부에서 API로 넘긴 파라미터를 가져오는 어노테이션이다.
* 마지막으로 위에서 추가된 API를 테스트하는 코드를 `HelloControllerTest`에 추가해보자.
```java
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = HelloController.class)
public class HelloControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void hello_is_returned() throws Exception {
        String hello = "hello";

        mvc.perform(get("/hello")).andExpect(status().isOk()).andExpect(content().string(hello));
    }
    
    @Test
    public void helloDto_is_returned() throws Exception {
        String name="hello";
        int amount = 1000;
        
        mvc.perform(get("/hello/dto").param("name", name).param("amount", String.valueOf(amount)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name", is(name)))
                .andExpect(jsonPath("$.amount", is(amount)));
    }
}
```
* `param()` 메소드는 API Test 시 사용될 요청 파라미터를 key, value의 쌍으로 설정하는데, 값은 __String만 가능__ 하다.
* `jsonPath()` 메소드는 JSON응답값을 필드별로 검증할 수 있는 메소드이다.   
  `$`를 기준으로 필드명을 명시하며, 위에서는 name, amount를 검증하니 `$.name`와 `$.amount`를 사용했다.