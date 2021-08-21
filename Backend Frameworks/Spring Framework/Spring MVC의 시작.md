Spring MVC 시작하기
======

* 우선 Spring MVC를 위해서는 다음 Dependency절이 필요하다.
```xml
<dependencies>
  	<dependency>
  		<groupId>javax.servlet</groupId>
		<artifactId>javax.servlet-api</artifactId>
		<version>3.1.0</version> 
		<scope>provided</scope>
 	</dependency>
 	<dependency>
 		<groupId>javax.servlet.jsp</groupId>
 		<artifactId>javax.servlet.jsp-api</artifactId>
 		<version>2.3.2-b02</version>
 		<scope>provided</scope>
 	</dependency>
 	<dependency>
 		<groupId>javax.servlet</groupId>
 		<artifactId>jstl</artifactId>
 		<version>1.2</version>
 	</dependency>
 	<dependency>
 		<groupId>org.springframework</groupId>
 		<artifactId>spring-webmvc</artifactId>
 		<version>5.0.2.RELEASE</version>
 	</dependency>
</dependencies>
```
* 위 XML은 Spring을 이용해서 Web Application을 개발하는데 필요한 의존을 설정한다.
* Servlet 3.1, JSP 2.3, JSTL 1.2에 대한 의존을 추가하고, Spring MVC를 사용하기 위해 spring-webmvc에 대한 의존을 추가한다.
<hr/>

<h2>Spring MVC를 위한 설정</h2>

* Spring MVC를 실행하기 위한 최소 설정은 다음과 같다.
  * Spring MVC의 주요 설정(HandlerMapping, ViewResolver 등)
  * Spring의 DispatcherServlet 설정

<hr/>

<h3>Spring MVC 설정</h3>

```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {
	
	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}
	
	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		registry.jsp("/WEB-INF/view/", ".jsp");
	}
}
```
* __@EnableWebMvc__ 어노테이션은 Spring MVC 설정을 활성화한다. Spring MVC를 사용하는데 필요한 다양한   
  설정을 생성한다.

```java
@Override
public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
	configurer.enable();
}
```
* `DispatcherServlet`의 매핑 경로를 '/'로 주었을 때, JSP/HTML/CSS 등을 올바르게 처리하기 위한   
  설정을 추가한다. 

```java
@Override
public void configureViewResolvers(ViewResolverRegistry registry) {
	registry.jsp("/WEB-INF/view/", ".jsp");
}
```
* JSP를 이용해서 컨트롤러의 실행 결과를 보여주기 위한 설정을 추가한다.

* Spring MVC를 사용하려면 다양한 구성 요소를 설정해야 한다. Spring 2.5나 3버전에서는 이러한 설정을   
  일일이 구성해야 했지만, __@EnableWebMvc__ 어노테이션은 이러한 복잡한 설정을 대신 해준다.
* __@EnableWebMvc__ 어노테이션은 내부적으로 다양한 Bean 설정을 추가해준다.
* `WebMvcConfigurer` 인터페이스는 Spring MVC의 개별 설정을 조정할 때 사용된다.   
  `configureDefaultServletHandling()`메소드와 `configureViewResources()` 메소드는 `WebMvcConfigurer`   
  인터페이스에 정의된 메소드로, 각각 Default Servlet과 ViewResolver와 관련된 설정을 조정한다.
<hr/>

<h3>web.xml에  DispatcherServlet 설정</h3>

* Spring MVC가 웹 요청을 처리하려면 `DispatcherServlet`을 통해 웹 요청을 받아야 한다.   
  이를 위해 web.xml에 `DispatcherServlet`을 등록한다.

```xml
<servlet>
    <servlet-name>dispatcher</servlet-name>
  	<servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <!-- 위 코드는 DispatcherServlet을 dispatcher라는 이름으로 등록한다. -->
  	<init-param>
  		<param-name>contextClass</param-name>
  		<param-value>
  			org.springframework.web.context.support.AnnotationConfigWebApplicationContext
  		</param-value>
  	</init-param>
    <!-- 위 코드는 contextClass의 초기화 파라미터를 설정한다. Java 설정을 사용하는 경우, AnnotationConfigWebApplicationContext
        클래스를 사용한다. 이 클래스는 Web Application용 Spring Container 클래스이다. -->
  	<init-param>
  		<param-name>conextConfigLocation</param-name>
  		<param-value>
  			config.MvcConfig
  			config.ControllerConfig
  		</param-value>
  	</init-param>
    <!-- 위 코드는 contextConfiguration의 초기화 파라미터 값을 지정한다. 이 파라미터에는 Spring 설정 클래스 목록을 지정한다.
        각 설정 파일의 경로는 줄바꿈이나 콤마로 구분한다. -->
  	<load-on-startup>1</load-on-startup>
    <!-- 위 코드는 Tomcat 등의 WAS가 Web Application을 구동할 때 이 Servlet을 함께 실행하도록 설정한다. -->
</servlet>
  
<servlet-mapping>
  <servlet-name>dispatcher</servlet-name>
  <url-pattern>/</url-pattern>
</servlet-mapping>
  
<!-- 위 코드는 모든 요청을 DispatcherServlet이 처리하도록 Servlet Mapping을 설정한 것이다.-->
<filter>
  <filter-name>encodingFilter</filter-name>
  <filter-class>
  	org.springframework.web.filter.CharacterEncodingFilter
  </filter-class>
  <init-param>
  	<param-name>encoding</param-name>
  	<param-value>UTF-8</param-value>
  </init-param>
</filter>

<filter-mapping>
   <filter-name>encodingFilter</filter-name>
   <url-pattern>/*</url-pattern>
</filter-mapping>
<!-- 위 코드는 HTTP요청 파라미터의 인코딩 처리를 위한 Servlet Filter를 등록한다. Spring은 인코딩 처리를 위한 필터인
    CharacterEncodingFilter 클래스를 제공한다. encoding 초기화 파라미터를 설정하여 HTTP 요청 파라미터를 읽어올 때
    사용할 인코딩을 지정한다. -->
```

* `DispatcherServlet`은 초기화 과정에서 `contextConfiguration` 초기화 파라미터에 지정한 설정 파일을   
  이용하여 Spring Container를 초기화한다. 즉, 위 코드의 경우에는 `MvcConfig` 클래스와 `ControllerConfig`   
  클래스를 이용해서 Spring Container를 생성한다.
<hr/>

<h3>간단한 컨트롤러의 구현</h3>

```java
@Controller
public class HelloController {
	
	@GetMapping("/hello")
	public String hello(Model model, @RequestParam(value="name", required=false) String name) {
		model.addAttribute("greeting", "HELLO" + name);
		return "hello";
	}
}
```
* __@Controller__ 어노테이션을 적용한 클래스는 Spring MVC에서 컨트롤러로 사용한다.
* __@GetMapping__ 어노테이션은 메소드가 처리할 요청 경로를 지정한다. 위 코드의 경우는 "/hello" 경로로   
  들어온 요청을 `hello()` 메소드를 이용하여 처리한다고 설정했다. 또한 HTTP 요청 메소드 중 __GET__   
  메소드에 대한 매핑을 설정한다.
* __Model__ 파라미터는 컨트롤러의 처리 결과를 view에 전달할 때 사용한다.
* __@RequestParam__ 어노테이션은 HTTP 요청 파라미터의 값을 메소드의 파라미터로 전달할 때 사용된다.   
  위 코드의 경우, name 요청 파라미터의 값을 name 파라미터에 전달한다.
* `model.addAttribute()` 메소드는 "greeting" 이라는 모델 속성에 값을 설정한다.
* 위 `hello()` 메소드가 반환한 값은 컨트롤러의 처리 결과를 보여줄 view이름으로 "hello"를 사용한다는 것이다.

* Spring MVC Framework에서 Controller란 __웹 요청을 처리하고 그 결과를 View에 전달하는 Spring Bean 객체__ 이다.   
  Spring Controller로 사용될 클래스는 __@Controller__ 어노테이션을 붙여야 하고, __@GetMapping__ 이나 __@PostMapping__   
  어노테이션과 같은 __요청 매핑 어노테이션을 이용해 처리할 경로를 지정__ 해주어야 한다.

* 만약 다음과 같이 요청이 왔다고 하자.
  `http://localhost:8080/chap09/hello?name=sangwoo`
  * __@GetMapping__ 어노테이션의 값은 __Servlet Context 경로 (또는 웹 app 경로)__ 를 기준으로 한다. 예를 들어 톰캣의   
    경우, `webContents\chap09` 폴더는 웹 브라우저에서 `http://host/chap09`에 해당하는데, 이 때 `chap09`가 컨텍스트   
    경로가 된다. 컨텍스트 경로가 `/chap09` 이므로 `http://localhost:8080/chap09/main/list` 경로를 처리하기 위한   
    컨트롤러는 `@GetMapping("/main/list")`를 사용해야 한다.

  * __@RequestParam__ 어노테이션은 HTTP 요청 파라미터를 메소드의 파라미터로 전달받을 수 있게 해준다.   
    __@RequestParam__ 의 __value__ 속성은 HTTP 요청 파라미터의 이름을 지정하고, __required__ 속성은 필수여부를 지정한다.   
    위 경우에는 `hello()`의 파라미터로 name 요청 파라미터의 값인 sangwoo가 전달된다.

* `Model#addAttribute()` 메소드의 첫 번째 파라미터는 __데이터를 식별하는데 사용되는 속성 이름__ 이고, 두 번째 파라미터는   
  __파라미터의 속성 이름에 해당하는 값__ 이다. View 코드는 이 속성 이름을 사용해서 컨트롤러가 전달한 데이터에 접근한다.

* __@GetMapping__ 가 붙은 메소드는 컨트롤러의 실행 결과를 보여줄 View의 이름을 반환한다.

<hr/>

