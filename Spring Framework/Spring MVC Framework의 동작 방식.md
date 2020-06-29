Spring MVC Framework의 동작 방식
======

<h2>Spring MVC의 핵심 구성 요소</h2>

* Spring MVC의 핵심 구성 요소와 각 요소간의 관계는 다음과 같이 정리할 수 있다.
  * (1) 요청 전송 : `Web Browser` ==> `DispatcherServlet`
  * (2) 요청 URL과 매칭되는 컨트롤러 검색 : `DispatcherServlet` ==> `<<spring bean>> : HandlerMapping` ==> `DispatcherServlet`
  * (3) 처리 요청 : `DispatcherServlet` ==> `<<spring bean>> : HandlerAdapter`
  * (4) 실행 : `HandlerAdapter` ==> `Controller`
  * (5) 결과 반환 : `Controller` ==> `HandlerAdapter`
  * (6) 컨트롤러의 실행 결과를 ModelAndView로 변환하여 반환 : `HandlerAdapter` ==> `DispatcherServlet`
  * (7) 컨트롤러의 실행결과를 보여줄 View 검색 : `DispatcherServlet` ==> `<<spring bean>> : ViewResolver`
  * (8) 응답 생성 요청 : `DispatcherServlet` ==> `View`
  * (9) 응답 생성 : `View` ==> `jsp`

* (2)번 과정 : `DispatcherServlet`은 모든 연결을 담당한다. 웹 브라우저로부터 요청이 들어오면   
  `DispatcherServlet`은 그 요청을 처리하기 위해 알맞은 컨트롤러 객체를 검색한다. 이때, 직접   
  검색하는 것이 아니라 `HandlerMapping`이라는 Bean 객체에게 컨트롤러 검색을 요청한다.
* (3,4,5,6)번 과정 : `HandlerMapping`은 클라이언트의 요청 경로를 이용하여 이를 처리할 Bean 객체를   
  `DispatcherServlet`에게 전달한다. 예를 들어 웹 요청 경로가 "/hello" 라면 등록된 컨트롤러   
  Bean 객체들 중에서 '/hello'로 GetMapping 어노테이션이 지정된 객체를 찾는다.   
  하지만 컨트롤러 객체를 `DispatcherServlet`이 전달받았다고 해서 바로 컨트롤러 객체의 메소드를   
  실행할 수 있는 것은 아니다. `DispatcherServlet`은 __@Controller__ 어노테이션을 이용해서   
  구현한 컨트롤러 뿐만 아니라 Spring2.5에 사용됐던 컨트롤러들까지 찾아 동일한 방식으로 수행할 수   
  있게 한다. 이렇게 다양한 버전의 Spring Controller들을 동일한 방식으로 처리하기 위해 중간에   
  사용되는 것이 `HandlerAdapter` Bean 이다.   
  `DispatcherServlet`은 `HandlerMapping`이 찾아준 컨트롤러 객체를 처리할 수 있는 `HandlerAdpater`   
  Bean에게 요청의 처리를 위임한다. `HandlerAdapter`는 컨트롤러의 알맞은 메소드를 호출해서 요청을   
  처리하고, 그 결과를 `DispatcherServlet`에게 반환한다. 이때, `HandlerAdapter`는 컨트롤러의   
  처리 결과를 `ModelAndView`라는 객체로 변환하여 `DispatcherServlet`에게 반환한다.
* (7,8)번 과정 : `HandlerAdapter`로부터 컨트롤러의 요청 처리 결과를 `ModelAndView`로 받으면,   
  `DispatcherServlet`은 결과를 보여줄 view를 찾기 위해 `ViewResolver` Bean 객체를 사용한다.   
  `ModelAndView`는 __컨트롤러가 반환한 view의 이름을 담고 있는데__, `ViewResolver`는 이 view이름에   
  해당하는 `View`객체를 찾거나 생성해서 반환한다. 응답을 생성하기 위해 JSP를 사용하는 `ViewResolver`는   
  매번 새로운 `View`객체를 생성해서 `DispatcherServlet`에게 반환한다.   
  `DispatcherServlet`은 `ViewResolver`가 반환한 `View` 객체에게 응답 결과의 생성을 요청한다.   
  JSP를 사용하는 경우 `View`객체는 JSP를 실행함으로써 웹 브라우저에 전송할 응답 결과를 생성하고,   
  이로써 모든 과정이 끝난다.
<hr/>

<h3>Controller와 Handler</h3>

* __Client의 요청을 실제로 처리하는 것은 컨트롤러__ 이고, `DispatcherServlet`은 client의 요청을   
  전달받는 창구 역할을 한다. 

* Spring MVC는 웹 요청을 처리할 수 있는 범용 프레임워크이다. 이 경우에는 __@Controller__ 어노테이션을   
  붙인 클래스를 이용해서 client의 요청을 처리하지만, 실제로는 직접 만든 클래스를 이용해서 client의   
  요청을 처리할 수도 있다. 즉, `DispatcherServlet`의 입장에서는 client의 요청을 처리하는 객체의 타입이   
  반드시 __@Controller__ 를 적용한 클래스일 필요가 없다. 그 예시로는 `HttpRequestHandler`가 있다.

* 위와 같은 이유로 Spring MVC는 웹 요청을 실제로 처리하는 객체를 __Handler__ 라고 표현하며,   
  __@Controller__ 적용 객체나 Controller 인터페이스를 구현한 객체는 모두 Spring MVC의 입장에서는   
  핸들러가 된다. 따라서 __특정 요청 경로를 처리해주는 핸들러를 찾아주는 객체를__ `HandlerMapping`이라 한다.

* `DispatcherServlet`은 핸들러 객체의 실제 타입에 관계없이 실행 결과를 `ModelAndView`라는 타입으로만   
  받으면 된다. 하지만 핸들러의 실제 구현 타입에 따라 `ModelAndView`를 반환하는 객체도 있고, 그렇지 않은   
  객체도 있다. 따라서 핸들러의 처리 결과를 `ModelAndView`로 변환해주는 객체가 필요하며, `HandlerAdapter`가   
  이러한 변환을 처리해준다.

* 핸들러 객체의 실제 타입마다 그에 알맞은 `HandlerMapping`과 `HandlerAdapter`가 존재하기 때문에,   
  사용할 핸들러의 종류에 따라 해당 `HandlerMapping`과 `HandlerAdapter`를 Spring Bean으로 등록해야 한다.   
<hr/>

<h2>DispatcherServlet과 Spring Container</h2>

* 앞서 작성한 web.xml 파일을 보자.
```xml
<servlet>
  	<servlet-name>dispatcher</servlet-name>
  	<servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
  	<init-param>
  		<param-name>contextClass</param-name>
  		<param-value>
  			org.springframework.web.context.support.AnnotationConfigWebApplicationContext
  		</param-value>
  	</init-param>
  	<init-param>
  		<param-name>contextConfigLocation</param-name>
  		<param-value>
  			config.MvcConfig
  			config.ControllerConfig
  		</param-value>
  	</init-param>
    <load-on-startup>1</load-on-startup>
</servlet>
```
* 위에서는 `DispatcherServlet`의 `contextConfigLocation` 초기화 파라미터를 이용해 Spring 설정 클래스 목록을   
  전달했다. `DispatcherServlet`은 전달받은 설정 파일을 이용해서 Spring Container를 생성하는데, 위에서 설명한   
  `HandlerMapping`, `HandlerAdapter`, `Controller`, `ViewResolver` 등의 Bean 객체는 `DispatcherServlet`이   
  생성한 Spring Container에서 구한다. 따라서 `DispatcherServlet`이 사용하는 설정 파일에 이들 Bean에 대한   
  정의가 포함되어 있어야 한다.
<hr/>

<h2>@Controller를 위한 HandlerMapping과 HandlerAdapter</h2>

* __@Controller__ 적용 객체는 `DispatcherServlet`의 입장에서 보면 한 종류의 핸들러 객체이다.   
  `DispatcherServlet`은 웹 브라우저의 요청을 처리할 핸들러 객체를 찾기 위해 `HandlerMapping`을   
  사용하고, 핸들러를 실행하기 위해 `HandlerAdapter`를 사용한다. `DispatcherServlet`은   
  Spring Container에서 `HandlerMapping`과 `HandlerAdapter` 타입의 Bean을 사용하므로 핸들러에   
  알맞은 `HandlerMapping` Bean과 `HandlerAdapter` Bean이 Spring 설정에 등록돼야 한다.   
  하지만 이전에는 config 클래스에 위 두 객체를 등록하지 않고, __@EnableWebMvc__ 어노테이션만   
  아래와 같이 붙였다.
```java
@Configuration
@EnableWebMvc
public class MvcConfig {

    //..

}
```
* __@EnableWebMvc__ 어노테이션을 적용하면, 다음 두 객체를 Bean으로 추가해준다.
  * `org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping`
  * `org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter`
* `RequestMappingHandlerMapping`은 __@Controller__ 어노테이션이 적용된 객체의 요청 매핑 어노테이션인   
  __@GetMapping__ 값을 이용해서 웹 브라우저의 요청을 처리할 컨트롤러 Bean을 찾는다.
* `RequestMappingHandlerAdapter`는 컨트롤러의 메소드를 알맞게 실행하고, 그 결과를 `ModelAndView`로   
  변환해서 `DispatcherServlet`에 반환한다.
```java
@Controller
public class HelloController {
	
	@GetMapping("/pages/hello")
	public String hello(Model model, @RequestParam(value="name", required=false) String name) { 
		model.addAttribute("greeting", "WELCOME " + name);
		return "hello";
	}
}
```
* 위 코드에서 `RequestMappingHandlerAdapter` 클래스는 "/pages/hello" 요청 경로에 대해 `hello()`   
  메소드를 호출한다. 이때, `Model` 객체를 생성해서 첫 번째 파라미터로 전달한다. 그리고 이름이   
  "name"인 HTTP 요청 파라미터의 값을 두 번째 파라미터로 전달한다.
* `RequestMappingHandlerAdapter`는 컨트롤러 메소드의 결과 값이 String 타입이면 해당 값을   
  View이름으로 갖는 `ModelAndView` 객체를 생성해서 `DispatcherServlet`에 반환한다.   
  이때, 첫 번째 파라미터로 전달한 `Model` 객체에 보관된 값도 `ModelAndView` 객체에 함께 전달한다.
<hr/>

<h2>WebMvcConfigurer 인터페이스와 설정</h2>

* __@EnableWebMvc__ 어노테이션을 사용하면 __@Controller__ 어노테이션을 붙인 컨트롤러를 위한   
  설정을 생성한다. 또한 `WebMvcConfigurer` 타입의 Bean을 이용해서 MVC설정을 추가로 생성한다.
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
		registry.jsp("/pages/", ".jsp");
	}
}
```
* 위의 `MvcConfig` 설정 클래스는 `WebMvcConfigurer` 인터페이스를 구현한다. 이 때, __@Configuration__   
  어노테이션이 붙은 클래스 역시 컨테이너에 Bean으로 등록되므로 `MvcConfig` 클래스는   
  ``WebMvcConfigurer` 타입의 Bean이 된다.
* __@EnableWebMvc__ 어노테이션을 적용하면 `WebMvcConfigurer` 타입인 Bean객체의 메소드를   
  호출해서 MVC 설정을 추가한다. 예를 들어 `ViewResolver` 설정을 추가하기 위해 `WebMvcConfigurer`   
  타입인 Bean 객체의 `configureViewResolvers()` 메소드를 호출한다. 따라서 `WebMvcConfigurer`를   
  구현하는 설정 클래스는 `configureViewResolvers()` 메소드를 재정의해서 알맞은 View 관련   
  설정을 해주면 된다. 위에서는 JSP를 위한 설정을 추가한 것이다.
<hr/>

<h2>JSP를 위한 ViewResolver</h2>

* 우리는 컨트롤러의 처리 결과를 JSP를 이용해서 생성하기 위해 다음 설정을 사용했다.
```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {
	
    //..

	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		registry.jsp("/pages/", ".jsp");
	}
}
```
* `WebMvcConfigurer` 인터페이스에 정의된 `configureViewResolvers()` 메소드는   
  `ViewResolverRegistry` 타입의 registry 파라미터를 갖는다. `ViewResolverRegistry#jsp()` 메소드를   
  사용하면 JSP를 위한 `ViewResolver`를 설정할 수 있다. 위 설정은 아래와 같은 Bean을 등록해준다.
```java
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Bean
public ViewResolver viewResolver() {
    InternalResourceViewResolver vr = new InternalResourceViewResolver();
    vr.setPrefix("/pages/");
    vr.setSuffix(".jsp");
    return vr;
} 
```

* 컨트롤러의 실행 결과를 받은 `DispatcherServlet`은 `ViewResolver`에게 view 이름에 해당하는   
  `View` 객체를 요청한다. 이때, `InternalResourceViewResolver`는 "prefix + 뷰이름 + suffix"에   
  해당하는 경로를 view 코드로 사용하는 `InternalResourceView` 타입의 `View` 객체를 반환한다.   
  예를 들어 view이름이 "hello"라면 "/pages/hello.jsp" 경로를 사용하는 `InternalResourceView`   
  객체를 반환한다. `DispatcherServlet`이 `InternalResourceView` 객체에 응답 요청을 생성하면   
  `InternalResourceView` 객체는 경로에 지정한 JSP 코드를 실행하여 응답 결과를 생성한다.

* `DispatcherServlet`은 컨트롤러의 실행 결과를 `HandlerAdapter`를 통해서 `ModelAndView`의   
  형태로 받는다. `Model`에 담긴 값은 `View` 객체에 Map형식으로 전달된다. 예를 들어   
  `HelloController` 클래스는 다음과 같이 `Model`에 "greeting" 속성을 설정했다.
```java
@Controller
public class HelloController {
	
	@GetMapping("/pages/hello")
	public String hello(Model model, @RequestParam(value="name", required=false) String name) { 
		model.addAttribute("greeting", "WELCOME " + name);
		return "hello";
	}
}
```
* 위의 경우 `DispatcherServlet`은 `View` 객체에 응답 생성을 요청할 때 "greeting" 키를 갖는   
  Map 객체를 `View` 객체에 전달한다. `View`객체는 전달받은 Map 객체에 담긴 값을 이용해서   
  알맞은 응답 결과를 출력한다. `InternalResourceView`는 Map객체에 담겨있는 키 값을   
  `request.setAttribute()`를 이용해서 request의 속성에 저장하고, 해당 경로의 JSP를 실행한다.
<hr/>

<h2>Default Handler와 HandlerMapping의 우선순위</h2>

* web.xml을 보면 `DispatcherServlet`에 대한 매핑 경로를 다음과 같이 "/"로 정의했다.
```xml
<servlet-mapping>
    <servlet-name>dispatcher</servlet-name>
    <url-pattern>/</url-pattern>
</servlet-mapping>
```
* 매핑 경로가 '/'인 경우, __.jsp로 끝나는 요청을 제외한 모든 요청을 `DispatcherServlet`이   
  처리한다. 즉, index.html과 같이 확장자가 .jsp가 아닌 모든 요청을 처리하게 된다.   
  그런데 __@EnableWebMvc__ 어노테이션이 등록하는 `HandlerMapping`은 __@Controller__   
  어노테이션을 적용한 Bean 객체가 처리할 수 있는 요청 경로만 대응할 수 있다.
* .jsp 확장자가 아닌 다른 경로를 처리하기 위해서는 `WebMvcConfigurer`의   
  `configureDefaultServletHandling()` 메소드를 사용하는 것이 편리하다.
```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }
}
```
* 위 설정에서 `DefaultServletHandlerConfigurer#enable()`는 다음 2개의 Bean 객체를 추가한다.
  * `DefaultServletHttpRequestHandler`
  * `SimpleUrlHandlerMapping`

* `DefaultServletHttpRequestHandler`는 client의 모든 요청을 WAS가 제공하는 Default Servlet에   
  전달한다. 예를 들어 "/index.html" 에 대한 처리를 `DefaultServletHttpRequestHandler`에 요청하면   
  이 요청을 다시 Default Servlet에 전달해서 처리하도록 한다. 그리고 `SimpleUrlHandlerMapping`을   
  이용해서 모든 경로("/**")를 `DefaultServletHttpRequestHandler`를 이용해서 처리하도록 설정한다.

* __@EnableWebMvc__ 어노테이션이 등록하는 `RequestMappingHandlerMapping`의 적용 우선순위가   
  `DefaultServletHandlerConfigurer#enable()` 메소드가 등록하는 `SimpleUrlHandlerMapping`의   
  우선순위보다 높다. 따라서 웹 브라우저의 요청이 들어오면 `DispatcherServlet`은 다음 방식으로   
  요청을 처리한다.
  * (1) `RequestMappingHandlerMapping`을 사용해서 요청을 처리할 핸들러를 검색한다.
    * 존재하면 해당 컨트롤러를 이용해서 요청을 처리한다.
  * (2) 존재하지 않으면 `SimpleUrlHandlerMapping`을 사용해서 처리할 핸들러를 검색한다.
    * `DefaultServletHandlerConfigurer#enable()` 메소드가 등록한 `SimpleUrlHandlerMapping`은   
        "/**" 경로, 즉 모든 경로에 대해 `DefaultServletHttpRequestHandler`를 반환한다.
    * `DispatcherServlet`은 `DefaultServletHttpRequestHandler`에 처리를 요청한다.
    * `DefaultServletHttpRequestHandler`는 Default Servlet에게 처리를 위임한다.