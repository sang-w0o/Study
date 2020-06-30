MVC1 : 요청 매핑, 커맨드 객체, redirect, 폼 태그, 모델
======

<h2>요청 매핑 어노테이션을 이용한 경로 매핑</h2>

* 웹 app의 개발은 다음 코드를 작성하는 것이다.
  * 특정 요청 URL을 처리하는 코드.
  * 처리 결과를 HTML과 같은 형식으로 응답하는 코드.

* 위 중 첫 번째는 __@Controller__ 어노테이션을 사용한 컨트롤러 클래스를 이용해서 구현한다. 컨틀롤러 클래스는   
  요청 매핑 어노테이션을 사용해서 메소드가 처리할 요청 경로를 지정할 수 있는데, 요청 매핑 어노테이션에는   
  __@RequestMapping, @GetMapping, @PostMapping__ 등이 있다.

* 예를 들어 회원 가입 절차에는 `약관 동의` ==> `회원 정보 입력` ==> `가입 완료`가 있다 하자.   
  그리고 각 과정을 위한 URL을 다음과 같이 정해보자.
```java
@Controller
public class RegistController {

    @RequestMapping("/register/step1")
    public String handleStep1() {
        return "register/step1";
    }

    @RequestMapping("/register/step2")
    public String handleStep2() {
        return "register/step2";
    }

    @RequestMapping("/register/step3")
    public String handleStep3() {
        return "register/step3";
    }
}
```
* 위 코드를 보면 각 요청 매핑 어노테이션의 경로가 "/register"로 시작한다. 이러한 경우에는 아래와 같이    
  __공통되는 부분의 경로를 담은 __@RequestMapping__ 어노테이션을 클래스에 적용__ 하고 각 메소드는 __나머지 경로를 값으로 갖는__   
  요청 매핑 어노테이션을 적용할 수 있다.
```java
@Controller
@RequestMapping("/register")
public class RegistController {

    @RequestMapping("/step1")
    public String handleStep1() {
        return "register/step1";
    }

    @RequestMapping("/step2")
    public String handleStep2() {
        return "register/step2";
    }

    @RequestMapping("/step3")
    public String handleStep3() {
        return "register/step3";
    }
}
```
* Spring MVC는 __클래스에 적용한 요청 매핑 어노테이션의 경로와 메소드에 적용한 요쳥 매핑 어노테이션의 경로를 합쳐셔__ 찾기   
  때문에 위 코드에서 `handleStep1()` 메소드가 처리하는 경로는 "/register/step1"이 된다.

* 아래의 RegisterController 클래스를 보자.
```java
@Controller
public class RegisterController {
	
	@RequestMapping("/pages/register/step1")
	public String handleStep1() {
		return "/register/step1";
	}
}
```
* 그리고 step1.jsp 를 보자.
```jsp
<html>
<head>
<title>회원 가입</title>
</head>
<body>
	<h2>약관</h2>
	<p>약관 내용</p>
	<form action="step2" method="post">
		<label>
			<input type="checkbox" name="agree" value="true"/>약관 동의
		</label>
		<input type="submit" value="다음 단계"/>
	</form>
</body>
</html>
```
* 위 코드를 실행하고, `localhost:8080/chap11/register/step1 에 들어가면 step1.jsp를 볼 수 있다.
<hr/>

<h2>GET과 POST의 구분 : @GetMapping, @PostMapping</h2>

* 위의 jsp 코드의 form 태그를 보면, 전송 방식을 `post`로 지정했다.
```jsp
<form action="step2" method="post">
```
* 주로 form을 전송할 때는 `POST` 방식을 사용하는데, Spring MVC는 별도의 설정이 없으면 `GET`과 `POST`방식에   
  관계없이 __@RequestMapping__ 에 지정한 경로와 일치하는 요청을 처리한다. 만약 `POST` 요청만 처리하고 싶다면   
  아래와 같이 __@PostMapping__ 어노테이션을 사용해서 제한할 수 있다.
```java
@Controller
public class RegisterController {
	
	@PostMapping("/pages/register/step1")
	public String handleStep1() {
		return "/register/step1";
	}
}
```
* 위와 같이, `GET` 방식의 요청만 처리하고 싶다면, __@GetMapping__ 어노테이션을 사용하면 된다.
* __@GetMapping, @PostMapping__ 은 Spring 4.3버전에 추가된 것으로, 이전까지는 __@RequestMapping__ 어노테이션의   
  method 속성을 사용해서 HTTP 요청 방식을 제한했다.
```java
@Controller
public class RegisterController {
	
	@RequestMapping(value="/pages/register/step1", method=RequestMethod.GET)
	public String handleStep1() {
		return "/register/step1";
	}
}
```
<hr/>

<h2>요청 파라미터 접근</h2>

* 위에서 작성한 약관 화면인 step1.jsp를 보면, `약관 동의` 체크박스에 체크할 경우, 값이 true인 "agree" 요청 파라미터의 값을   
  POST 방식으로 전송했다. 따라서 form에 지정한 agree 요청 파라미터의 값을 사용해서 약관 동의 유무를 확인할 수 있다.
```jsp
<form action="step2" method="post">
    <label>
        <input type="checkbox" name="agree" value="true">약관 동의
    </label>
    <input type="submit" value="다음 단계">
</form>
```
* 위의 요청 파라미터를 사용하는 첫 번째 방법은 `HttpServletRequest` 객체를 직접 이용하는 것이다.
```java
@Controller
public class RegisterController {
	
	@RequestMapping("/pages/register/step1")
	public String handleStep1() {
		return "/register/step1";
	}

    @PostMapping("/pages/register/step2")
    public String handleStep2(HttpServletRequest request) {
        String agreeParam = request.getParameter("agree");
        if(agreeParam == null || !agreeParam.equals("true")) {
            return "register/step1";
        }
        return "register/step2";
    }
}
```
* 요청 파라미터에 접근하는 또다른 방법은 __@RequestParam__ 어노테이션을 사용하는 것이다.
```java
@Controller
public class RegisterController {
	
    //..

	@PostMapping("/pages/register/step2")
	public String handleStep1(@RequestParam(value="agree", defaultValue="false") Boolean agreeVal) {
        if(!agreeVal) {
            return "/register/step1";
        }
		return "/register/step2";
	}
}
```
* __@RequestParam__ 어노테이션은 다음 속성을 제공한다.

<table>
    <tr>
        <td>value</td>
        <td>String</td>
        <td>HTTP 요청 파라미터의 이름을 지정한다.</td>
    </tr>
    <tr>
        <td>required</td>
        <td>boolean</td>
        <td>필수 여부를 지정한다. 이 값이 true일 때, 해당 요청 파라미터의 값이 없으면 예외가 발생된다. (기본값 : true)</td>
    </tr>
    <tr>
        <td>defaultValue</td>
        <td>String</td>
        <td>요청 파라미터가 값이 없을 때 사용할 문자열 값을 지정한다.</td>
    </tr>
</table>

* __@RequestParam__ 어노테이션을 사용한 코드를 보면 다음과 같이 agreeVal 파라미터의 타입이 Boolean이다.
```java
@RequestParam(value="agree", defaultValue="false") Boolean agreeVal
```
* Spring MVC는 파라미터의 타입에 맞게 String값을 변환해준다. 위 코드는 agree 요청 파라미터의 값을 읽어와   
  Boolean타입으로 변환해서 agreeVal 파라미터에 전달한다.

* 이제 step2.jsp를 다음과 같이 작성하자.
```jsp
<html>
<head>
<title>회원 가입</title>
</head>
<body>
	<h2>회원 정보 입력</h2>
	<form action="step2" method="post">
		<p>
			<label>이메일 :<br>
			<input type="text" name="email" id="email">
			</label>
		</p>
		<p>
			<label>이름:<br>
			<input type="text" name="name" id="name">
			</label>
		</p>
		<p>
			<label>비밀번호:<br>
			<input type="password" name="password" id="password">
			</label>
		</p>
		<p>
			<label>비밀번호 확인:<br>
			<input type="password" name="confirmPassword" id="confirmPassword">
			</label>
		</p>
		<input type="submit" value="가입 완료">
	</form>
</body>
</html>
```
<hr/>

<h2>Redirect 처리</h2>

* 위 코드의 `RegisterController`는 `handleStep2()`에서 __@PostMapping__ 어노테이션을 적용했기 때문에   
  주소창에 직접 주소를 입력하는 GET방식은 처리할 수 없기 때문에 에러 화면이 노출된다.
* 잘못된 전송 방식으로 요청이 왔을 때 에러 화면보다 알맞은 경로로 redirect하는 방법이 있는데,   
  컨트롤러에서 특정 페이지로 redirect 시키는 방법은 "redirect:경로" 를 View이름으로 반환하면 된다.
```java
@Controller
public class RegisterController {

    //..

    @GetMapping("/pages/register/step2")
	public String handleStep2Get() {
		return "redirect:/pages/register/step1";
	}
}
```
* 이때, register: 다음에 "/"를 사용하지 않으면 상대 경로를 사용하고, "/"로 시작하게 하면 web application을   
  기준으로 이동 경로를 생성한다.
<hr/>

<h2>커맨드 객체를 이용해서 요청 파라미터 사용하기</h2>

* step2.jsp는 `email`, `name`, `password`, `confirmPassword`를 이용해서 정보를 서버에게 전송한다.   
  위와 같이 많은 요청 파라미터를 보낼 때 `HttpServletRequest#getParameter()` 메소드를 사용하는 방법과   
  __@RequestParam__ 어노테이션을 사용해도 되지만, 그만큼 코드도 길어지게 된다.
* Spring은 위와 같은 불편함을 줄이기 위해 요청 파라미터의 값을 `Command` 객체에 담아주는 기능을 제공한다.   
  예를 들어 이름이 name인 요청 파라미터의 값을 커맨드 객체의 `setName()` 메소드를 사용해서 커멘드 객체에   
  전달하는 기능을 제공한다. __단지 요청 파라미터의 값을 전달받을 수 있는 setter 메소드를 포함하는 객체를__   
  __커맨드 객체로 사용하면 된다__.
```java
@Controller
public class RegisterController {

    //..

    @PostMapping("/pages/register/step3")
	public String handleStep3(RegisterRequest regReq) {
		try {
			memberRegisterService.regist(regReq);
			return "/register/step3";
		} catch(DuplicateMemberException e) {
			return "/register/step2";
		}
	}
}
```
* `RegisterRequest` 클래스에는 `setEmail()`, `setName()`, `setPassword()`, `setConfirmPassword()` 메소드가 있다.   
  Spring은 이들 메소드를 이용해서 email, name, password, confirmPassword 요청 파라미터의 값을 커맨드 객체에   
  복사한 뒤 regReq 파라미터로 전달한다. 즉, __Spring MVC가__ `handleStep3()` __메소드에 전달할__ `RegisterRequest`   
  __객체를 생성하고 그 객체의 setter메소드를 이용해서 일치하는 요청 파라미터의 값을 전달__ 한다.
* `handleStep3()` 메소드는 `MemberRegisterService`를 이용해서 회원 가입을 처리한다. 성공 시에는 View이름으로   
  "/register/step3"를 반환하고, 실패하면 "/register/step2"를 반환한다.
<hr/>

<h2>View JSP 코드에서 커맨드 객체 사용하기</h2>

```jsp
<html>
<head>
<title>회원가입</title>
</head>
<body>
	<p><strong>${registerRequest.name}님</strong>의 회원 가입을 완료했습니다.</p>
	<p><a href="<c:url value='/main'/>"></a></p>
</body>
</html>
```
* 위 코드의 `${registerRequest.name}` 에서, `registerRequest`가 커맨드 객체에 접근할 때 사용한 속성 이름이다.   
  Spring MVC는 커맨드 객체의 __첫 글자를 소문자로 바꾼 클래스명과 동일한 속성명을 사용해서 커맨드 객체를 뷰에 전달__ 한다.   
  따라서 커맨드 객체의 이름이 `RegisterRequest`인 경우, JSP 코드는 `registerRequest`로 커맨드 객체에 접근할 수 있다.
<hr/>

<h2>@ModelAttribute 어노테이션으로 커맨드 객체 속성 이름 변경</h2>

* 위에서는 `RegisterRequest`에 접근하기 위해 JSP에서 `registerRequest`로 접근했다. 만약 커맨드 객체에   
  접근할 때 사용할 속성명을 변경하고 싶다면 __@ModelAttribute__ 어노테이션을 커맨드 객체로 사용할 파라미터에   
  적용하면 된다.
```java
@PostMapping("/pages/register/step3")
public String handleStep3(@ModelAttribute("formData") RegisterRequest regReq) {

}
```
* 위와 같이 하면, JSP코드에서는 `formData`라는 이름으로 커맨드 객체에 접근할 수 있다.
<hr/>

<h2>커맨드 객체와 Spring Form연동</h2>

* Spring MVC가 제공하는 커스텀 태그를 이용하면 좀 더 간단하게 커맨드 객체의 값을 출력할 수 있다.   
  Spring은 `<form:form>` 태그와 `<form:input>` 태그를 지원한다.
```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html>
<head>
<title>회원 가입</title>
</head>
<body>
	<h2>회원 정보 입력</h2>
	<form:form action="step3" modelAttribute="registerRequest">
		<p>
			<label>이메일 :<br>
			<form:input path="email"/>
			</label>
		</p>
		<p>
			<label>이름:<br>
			<form:input path="name"/>
			</label>
		</p>
		<p>
			<label>비밀번호:<br>
			<form:password path="password"/>
			</label>
		</p>
		<p>
			<label>비밀번호 확인:<br>
			<form:password path="confirmPassword"/>
			</label>
		</p>
		<input type="submit" value="가입 완료">
	</form:form>
</body>
</html>
```
* 위에서는 Spring이 제공하는 Form태그를 사용하기 위해 taglib 를 설정했다.
* `<form:form>` 태그는 HTML의 `<form>`태그를 생성하며, 이 태그의 속성은 다음과 같다.
  * action : `<form>` 태그의 action속성과 동일한 값을 사용한다.
  * modelAttribute : 커맨드 객체의 속성 이름을 지정한다. (지정하지 않을 경우 : "command"를 기본값으로 사용)
* `<form:input>` 태그는 `<input>` 태그를 생성한다. __path__ 로 지정한 커맨드 객체의 프로퍼티를 `<input>`   
  태그의 __value__ 속성값으로 사용한다.   
  예를 들어 `<form:input path="name" />`는 커맨드 객체의 name 프로퍼티 값을 __value__ 속성으로 사용한다.   
  만약 커맨드 객체의 name 프로퍼티 값이 "스프링" 이었다면, 위 코드는 아래와 같은 `<input>`태그를 생성한다.
```jsp
<input id="name" name="name" type="text" value="스프링" />
```
* `<form:form>` 태그를 사용하려면 __커맨드 객체가 존재해야 한다__. 위 코드에서는 `<form:form>` 태그를   
  사용하기 때문에 step1에서 step2로 넘어오는 단계에서 이름이 "registerRequest"인 객체를 모델에 넣어야   
  이 태그가 정상적으로 동작한다.
```java
@PostMapping("/pages/register/step2")
public String handleStep2(@RequestParam(value="agree", defaultValue="false") Boolean agree, Model model) {
	if(!agree) {
		return "/register/step1";
	}
	model.addAttribute("registerRequest", new RegisterRequest());
	return "/register/step2";
}
```
<hr/>

<h2>컨트롤러 구현 없는 경로 매핑</h2>

* step3.jsp를 보면 아래의 코드가 있다.
```jsp
<p><a href="<c:url value='/main/'/>">[첫 화면 이동]</a></p>
```
* step3.jsp는 위 코드에서 회원 가입 완료 후 첫 화면으로 이동할 수 있는 링크를 보여준다.   
  이 첫 화면은 단순히 환영 문구와 회원 가입으로 이동할 수 있는 링크만 제공한다고 하자.   
  그렇다면 컨트롤러 클래스는 특별히 처리할 것이 없기에 다음과 같이 단순히 view이름만   
  반환하도록 구현하자.
```java
@Controller
public class MainController {

	@RequestMapping("/pages/main")
	public String main() {
		return "main";
	}
}
```
* 이 컨트롤러는 요청 경로와 view이름을 연결해주는 것에 불과하다. 이러한 단순 연결을 위한 코드를 작성하는   
  불편함을 없애주기 위해 Spring은 `WebMvcConfigurer` 인터페이스의 `addViewControllers()` 메소드를 제공한다.
```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {
	
	//..
	
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/pages/main").setViewName("main");
	}
}
```
* 위처럼 `addViewcontrollers()` 메소드를 재정의하면 컨트롤러의 구현 없이 요청 경로와 View이름을 연결할 수 있다.   
  위 코드는 "/pages/main" 경로 요청에 view 이름으로 "main"을 사용한다고 설정한다.
<hr/>

<h2>커맨드 객체의 중첩, 컬렉션 프로퍼티</h2>
