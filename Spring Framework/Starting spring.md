Spring 시작하기
=====

<h2>Gradle 프로젝트 생성</h2>

* Gradle Project의 생성 과정은 maven과 크게 다르지 않다. 차이점이라면   
  pom.xml 대신에 __build.gradle__ 파일을 작성하는 것뿐이다. 폴더 구조도 동일하다.

* __build.grade__ 파일
```js
// Gradle Java plugin 사용
apply plugin:'java'

// 소스와 컴파일 결과를 1.8버전에 맞춘다.
sourceCompatibility=1.8
targetCompatibility=1.9

// 소스 코드 인코딩으로 UTF-8 사용
compileJava.options.encoding="UTF-8"

// 의존 모듈을 maven 중앙 repo에서 다운로드 한다.
repositories{
	mavenCentral()
}

// spring-context 모듈에 대한 의존 설정.
dependencies{
	compile 'org.springframework:spring-context:5.0.2.RELEASE'
}

// Gradle Wrapper 설정. 소스를 공유할 때 gradle의 설치 없이
// gradle 명령어를 실행할 수 있는 wrapper를 생성해준다.
task wrapper(type:Wrapper){
	gradleVersion='4.4'
}
```
<hr/>

<h2>예제 코드 작성</h2>

* Greeter.java : 콘솔에 간단한 메시지 출력하는 Java Class
* AppContext.java : Spring 설정 파일
* Main.java : main() 메소드를 통해 spring과 Greeter를 실행하는 Java Class

```java
// chap02/src/main/java/chap02/Greeter.java

package chap02;

public class Greeter {
	
	private String format;
	
	public String greet(String guest) {
		return String.format(format, guest);
	}
	
	public void setFormat(String format) {
		this.format = format;
	}
}
```

```java
// chap02/src/main/java/chap02/AppContext.java

package chap02;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppContext {
	
	@Bean
	public Greeter greeter() {
		Greeter g = new Greeter();
		g.setFormat("%s, HELLO!");
		return g;
	}
}
```
* __@Configuration__ annotation : 해당 클래스를 __Spring 설정 클래스로 지정__ .

* Spring은 객체를 생성하고 초기화하는 기능을 담당하는데, 위 코드에서는 다음 부분이   
  한 개의 객체를 생성하고 초기화하는 설정을 담고 있다.
```java
@Bean
public Greeter greeter() {
    Greeter g = new Greeter();
    g.setFormat("%s, HELLO!");
    return g;
}
```
* __Spring이 생성하는 객체를 Bean 객체__ 라고 부르는데, 이 Bean 객체에 대한 정보를   
  담고 있는 메소드가 greeter() 메소드이다. 이 메소드에는 __@Bean annotation__ 이 붙어있다.   
* __@Bean__ annotation : 해당 메소드가 생성한 객체를 Spring이 관리하는 Bean객체로 등록
  * @Bean 어노테이션을 붙인 메소드의 이름은 Bean 객체를 구분할 때 사용된다.   
    예를 들어 위 코드의 경우, 생성된 객체를 구분할 때 greeter라는 이름을 사용한다.   
    이 이름은 Bean 객체를 참조할 때 사용된다.
  * @Bean 어노테이션을 붙인 메소드는 객체를 생성하고 알맞게 초기화해야 한다.

```java
// chap02/src/main/java/chap02/Main.java

package chap02;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
	
	public static void main(String[] args) {
		
        // 설정 정보를 이용해서 Bean 객체 생성
		AnnotationConfigApplicationContext ctx = 
				new AnnotationConfigApplicationContext(AppContext.class);

        // Bean 객체 제공
		Greeter g = ctx.getBean("greeter", Greeter.class);
		String msg = g.greet("SPRING");
		System.out.println(msg);
		ctx.close();
	}
}
```
* AnnotationConfigApplicationContext 클래스 : Java 설정에서 정보를 읽어와   
  Bean 객체를 생성하고 관리한다.
* AnnotationConfigApplicationContext 객체를 생성할 때 앞서 작성한   
  AppContext 클래스를 생성자의 인수로 전달한다.   
  AnnotationConfigApplicationContext는 AppContext에 정의한 __@Bean 설정 정보__   
  __를 읽어와 Greeter 객체를 생성하고 초기화__ 한다.
* __getBean() 메소드__ : AnnotationConfigApplicationContext가 java 설정을 읽어와   
  생성한 Bean 객체를 검색할 때 사용된다.
  * 첫 번째 인수 : @Bean 어노테이션 메소드 이름인 Bean 객체의 이름
  * 두 번째 인수 : 검색할 Bean 객체의 타입
</hr>

<h2>Spring은 객체 Container이다.</h2>

* Spring의 핵심 기능은 __객체를 생성하고 초기화하는 것__ 인데, 이와 관련된 기능은   
  __ApplicationContext 인터페이스__ 에 정의되어 있다. AnnotationConfigApplicationContext 클래스는   
  이 인터페이스를 알맞게 구현한 클래스들 중 하나이다. 이 클래스는 Java Class에서 정보를 읽어와   
  객체의 생성과 초기화를 수행한다.
* __BeanFactory__ 인터페이스 : 객체 생성과 검색에 대한 기능 정의   
  ex) getBean() 메소드는 BeanFactory에 정의되어 있다.
* __ApplicationContext__ 인터페이스 : 메시지, 프로필/환경 변수 등을 처리할 수 있는 기능을 추가 정의.
* __ApplicationContext or BeanFactory__ 는 Bean 객체의 생성, 초기화, 보관, 제거 등을   
  관리하고 있어서 ApplicationContext를 __Container__ 라고도 부른다.

```java
package chap02;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
	
	public static void main(String[] args) {
		
		AnnotationConfigApplicationContext ctx = 
				new AnnotationConfigApplicationContext(AppContext.class);
		Greeter g1 = ctx.getBean("greeter", Greeter.class);
        Greeter g2 = ctx.getBean("greeter", Greeter.class);
        System.out.println("(g1 == g2 ="+ + (g1 == g2));  // true 출력
		ctx.close();
	}
}
```
* 위 코드가 true를 출력하는 것은 g1과 g2가 같은 객체임을 의미한다.
* 별도 설정을 하지 않을 경우, Spring은 __한 개의 Bean객체만을 생성__ 하며, 이때   
  Bean객체는 __singleton 범위__ 를 갖는다고 표현한다.