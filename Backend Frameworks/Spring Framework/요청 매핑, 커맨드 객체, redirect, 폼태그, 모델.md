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
* 위 코드를 실행하고, `localhost:8080/chap11/register/step1` 에 들어가면 step1.jsp를 볼 수 있다.
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
	<form action="step3" method="post">
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

* 아래의 두 코드가 있다고 하자.
```java
package survey;

public class Respondent {
	
	private int age;
	private String location;
	
	public int getAge() {
		return age;
	}
	
	public void setAge(int age) {
		this.age = age;
	}
	
	public String getLocation() {
		return location;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
}
```

```java
public class AnsweredData {
	
	private List<String> responses;
	private Respondent res;
	
	public List<String> getResponses() {
		return responses;
	}
	
	public void setResponses(List<String> responses) {
		this.responses = responses;
	}
	
	public Respondent getRes() {
		return res;
	}
	
	public void setRes(Respondent res) {
		this.res = res;
	}
}
```
* 위 `AnsweredData` 클래스는 앞서 커맨드 객체로 사용한 클래스와 다음 차이점을 갖는다.
  * List 타입의 프로퍼티가 존재한다.
  * 중첩 프로퍼티를 갖는다. 즉, `AnsweredData`는 `Respondent`를 갖고, `Respondent`는 그 프로퍼티로   
    age와 location을 갖는다.
* Spring MVC는 커맨드 객체가 컬렉션 타입의 프로퍼티를 가졌거나 중첩 프로퍼티를 가진 경우에도 요청 파라미터의   
  값을 알맞게 커맨드 객체에 설정해주는 기능을 제공하는데, 다음 규칙을 지켜야 한다.
  * HTTP요청 파라미터 이름이 "프로퍼티명[인덱스]" 형식이면 List 타입 프로퍼티의 값 목록으로 처리한다.
  * HTTP요청 파라미터 이름이 "프로퍼티명.프로퍼티명" 과 같은 형식이면 중첩 프로퍼티 값을 처리한다.

* 다음으로는 아래와 같이 `AnsweredData` 클래스를 커맨드 객체로 사용하는 컨트롤러 클래스를 작성하자
```java
@Controller
@RequestMapping("/pages/survey")
public class SurveyController {
	
	@GetMapping
	public String form() {
		return "/survey/surveyForm";
	}
	@PostMapping
	public String submit(@ModelAttribute("ansData") AnsweredData data) {
		return "/survey/submitted";
	}
}
```

* 위를 테스트하기 위해 요청 파라미터를 저장하는 surveyForm.jsp는 다음과 같다.
```jsp
<body>
	<h2>설문 조사</h2>
	<form method="post">
		<p>
			1. 당신의 역할은?<br/>
			<label><input type="radio" name="responses[0]" value="서버">서버 개발자</label>
			<label><input type="radio" name="responses[0]" value="프론트">프론트 개발자</label>
			<label><input type="radio" name="responses[0]" value="풀스택">풀스택 개발자</label>
		</p>
		<p>
			2. 가장 많이 사용하는 개발 도구는?<br/>
			<label><input type="radio" name="responses[1]" value="Eclipse">Eclipse</label>
			<label><input type="radio" name="responses[1]" value="Intellij">Intellij</label>
			<label><input type="radio" name="responses[1]" value="Sublime">Sublime</label>
		</p>
		<p>
			3. 하고싶은 말<br/>
			<input type="text" name="responses[2]"/>
		</p>
		<p>
			<label>응답자 위치 :<br/>
				<input type="text" name="res.location"/>
			</label>
		</p>
		<p>
			<label>응답자 나이 :<br/>
				<input type="text" name="res.age"/>
			</label>
		</p>
		<input type="submit" value="전송"/>
	</form>
</body>
```
* 이전에 작성한 `파라미터명[인덱스]`로 개발 분야와 개발 도구, 하고싶은 말을 저장했으며,   
  `파라미터명.파라미터명` 형식으로 나이와 위치를 저장했다.
* 위에 저장한 파라미터를 읽어와 페이지에 출력해주는 submitted.jsp는 다음과 같다.
```jsp
<body>
	<p>응답 내용</p>
	<ul>
		<c:forEach var="response" items="${ansData.responses}" varStatus="status">
			<li>${status.index + 1}번 문항 : ${response}</li>
		</c:forEach>
	</ul>
	<p>응답자 위치 : ${ansData.res.location}</p>
	<p>응답자 나이 : ${ansData.res.age}</p>
</body>
```

<hr/>

<h2>Model을 통해 컨트롤러에서 view에 데이터 전달하기</h2>

* 컨트롤러는 view가 응답 화면을 구성하는데 필요한 데이터를 생성해서 전달해야 한다. 이 때 사용하는 것이 `Model`이다.

```java
@Controller
public class HelloController {

    @RequestMapping("/pages/hello")
    public String hello(Model model, @RequestParam(value="name", required=false) String name) {
        model.addAttribute("WELCOME " + name);
        return "/hello";
    }
}
```
* View에 데이터를 전달하는 컨트롤러는 위의 `hello()` 메소드처럼 다음 두 가지를 하면 된다.
  * 요청 매핑 어노테이션이 적용된 메소드의 파라미터로 `Model` 객체 추가
  * `Model#addAttrbute()` 메소드로 view에서 사용할 데이터 전달

```java
@Controller
@RequestMapping("/pages/survey")
public class SurveyController {
	
	@GetMapping
	public String form(Model model) {
		List<Question> questions = createQuestions();
		model.addAttribute("questions", questions);
		return "/survey/surveyForm";
	}
	
	private List<Question> createQuestions() {
		Question q1 = new Question("당신의 역할은?", Arrays.asList("서버", "프론트", "풀스택"));
		Question q2 = new Question("사용하는 개발도구는?", Arrays.asList("이클립스", "인텔리J", "서브라임"));
		Question q3 = new Question("하고 싶은 말을 적어주세요.");
		return Arrays.asList(q1, q2, q3);
	}
	
	@PostMapping
	public String submit(@ModelAttribute("ansData") AnsweredData data) {
		return "/survey/submitted";
	}
}
```
* 위에서는 `form()` 메소드의 인자로 `Model` 객체를 넣어줬다.
* 이제 아래와 같이 jsp에서 Model 객체의 attribute에 접근할 수 있다.
```jsp
<c:forEach var="q" items="${questions}" varStatus="status">
	<p>
		${status.index + 1}.${q.title}<br/>
		<c:if test="${q.choice}">
			<c:forEach var="option" items="${q.options}">
				<label><input type="radio" name="responses[${status.index}]" value="${option}">${option}</label>
			</c:forEach>
		</c:if>
		<c:if test="${!q.choice}">
			<input type="text" name="responses[${status.index}]"/>
		</c:if>
	</p>
</c:forEach>
```
<hr/>

<h3>ModelAndView를 통한 View 선택과 Model 전달</h3>

* 위에서 구현한 `SurveyController`는 아래의 두 가지 기능을 담당한다.
  * `Model`을 이용해서 View에 전달할 데이터 설정
  * 결과를 보여줄 View 이름 반환

* `ModelAndView` 객체를 사용하면 위 두가지를 한번에 처리할 수 있다. 요청 매핑 어노테이션을 적용한 메소드는   
  `String` 타입 대신 `ModelAndView`를 반환할 수 있다. `ModelAndView`는 Model과 View이름을 함께 제공한다.
```java
@GetMapping
public ModelAndView form() {
	List<Question> questions = createQuestions();
	ModelAndView mav = new ModelAndView();
	mav.addObject("questions", questions);
	mav.setViewName("/survey/surveyForm");
	return mav;
}
```
* View에 전달할 Model 데이터는 `addObject()` 메소드로 추가하며, View 이름은 `setViewName()` 메소드를 이용하여 지정한다.
<hr/>

<h3>GET 방식과 POST 방식에 동일 이름의 커맨드 객체 사용하기</h3>

* `<form:form>` 태그를 사용하려면 커맨드 객체가 반드시 존재해야 하며, Model 객체도 존재해야 한다.
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
* 이때, 커맨드 객체를 파라미터로 추가하면 `addAttribute()` 메소드를 사용하지 않아도 된다.
```java
@PostMapping("/pages/register/step2")
public String handleStep2(@RequestParam(value="agree", defaultValue="false") Boolean agree, RegisterRequest registerRequest) {
    if(!agree) {
        return "/register/step1";
    }
    return "/register/step2";
}
```
* 커맨드 객체의 이름을 명시적으로 지정하려면 __@ModelAttribute__ 어노테이션을 사용해야 한다.   
  예를 들어 "/login" 요청 경로일 때 GET방식이면 로그인 폼을 보여주고, POST방식이면 로그인을 처리하도록 구현한   
  컨트롤러를 만들어야 한다고 하자. 입력 폼과 폼 전송 처리에서 사용할 커맨드 객체의 속성 이름이 클래스명과 다르다면   
  아래와 같이 GET요청과 POST요청을 처리하는 메소드에 __@ModelAttribute__ 어노테이션을 붙인 커맨드 객체를   
  파라미터에 추가해야 한다.
```java
@Controller
@RequestMapping("/login")
public class LoginController {

    @GetMapping
    public String form(@ModelAttribute("login")LoginCommand loginCommand) {
        return "login/loginForm";
    }

    @PostMapping
    public String form(@ModelAttribute("login")LoginCommand loginCommand) {

        //..

    }
}
```
<hr/>

<h2>주요 form 태그</h2>

* Spring MVC는 `<form:form>`, `<form:input>` 등 HTML form과 커맨드 객체를 연동하기 위한 JSP 태그라이브러리를 제공한다.   

<hr/>

<h3> form 태그를 위한 커스텀 태그</h3>

* `<form:form>` 커스텀 태그는 `<form>` 태그를 생성할 때 사용된다.
```jsp
<%@ taglib prefix="c" uri="http://www.springframework.org/tags/form" %>
...
<form:form>
...
<input type="submit" value="가입 완료">
</form:form>
```
* `<form:form>` 태그의 method 속성과 action속성을 지정하지 않으면, method는 __post__ 로 설정되고,   
  action은 __현재 요청 URL__ 로 설정된다.
* `<form:form>` 태그에서 커맨드 객체 사용 시, 커맨드 객체의 이름이 기본값인 "command"가 아니면 아래와 같이   
  modelAttribute 속성값으로 커맨드 객체의 이름을 설정해야 한다.
```jsp
<form:form modelAttribute="loginCommand">
...
</form:form>
```
* `<form:form>` 태그는 `<form>` 태그와 관련하여 다음 속성을 추가적으로 제공한다.
  * action : 폼 데이터를 전송할 URL
  * enctype : 전송될 데이터의 인코딩 타입
  * method : 전송 방식
<hr/>

<h3> input 태그 관련 커스텀 태그</h3>

* Spring은 `<input>` 태그를 위해 아래의 커스텀 태그들을 제공한다.

<table>
    <tr>
        <td>form:input</td>
        <td>text 타입의 input 태그</td>
    </tr>
    <tr>
        <td>form:password</td>
        <td>password 타입의 input 태그</td>
    </tr>
    <tr>
        <td>hidden</td>
        <td>hidden 타입의 input 태그</td>
    </tr>
</table>

* `<form:input>` 커스텀 캐그는 아래와 같이 __path__ 속성을 사용해서 연결할 커맨드 객체의 프로퍼티를 지정한다.
```jsp
<form:form modelAttribute="registerRequest" action="step3">
    <p>
        <label>이메일:<br/>
        <form:input path="email" />
        </label>
    </p>
</form:form>
```
* 위 코드가 생성하는 실제 HTML의 input 태그는 아래와 같다.
```jsp
<form id="registerRequest" action="step3" method="post">
    <p>
        <label>이메일:<br/>
        <input id="email" name="email" type="text" value="" />
        </label>
    </p>
</form>
```
<hr/>

<h3>select 관련 커스텀 태그</h3>

<table>
    <tr>
        <td>form:select</td>
        <td>select 태그를 생성한다. option태그를 생성할 때 필요한 컬렉션을 전달받을 수도 있다.</td>
    </tr>
    <tr>
        <td>form:options</td>
        <td>지정한 컬렉션 객체를 이용하여 option 태그를 생성한다.</td>
    </tr>
    <tr>
        <td>form:option</td>
        <td>option 태그 1 개를 생성한다.</td>
    </tr>
</table>

* `<select>` 태그는 선택 옵션을 제공할 때 주로 사용한다. 아래 메소드를 보자.
```java
@GetMapping("/login")
public String form(Model model) {
    List<String> loginTypes = new ArrayList<>();
    loginTypes.add("일반 회원");
    loginTypes.add("기업 회원");
    loginTypes.add("인턴");
    model.addAttribute("loginTypes", loginTypes);
    return "login/form";
}
```
* `<form:select>` 커스텀 태그를 사용하면 view에 전달한 `Model` 객체를 갖고 간단하게 `<select>, <option>` 태그를 생성할 수 있다
```jsp
<form:form modelAttribute="login">
    <p>
        <label for="loginType">로그인 타입</label>
        <form:select path="loginType" items="${loginTypes}"/>
    </p>
</form:form>
```
* __path__ 속성은 커맨드 객체의 프로퍼티이름을 지정하며, __items__ 속성에는 `<option>` 태그를 생성할 때   
  사용할 컬렉션 객체를 지정한다.위 코드의 `<form:select>` 태그는 아래의 HTML 태그를 생성한다.
```jsp
<select id="loginType" name="loginType">
    <option value="일반 회원">일반 회원</option>
    <option value="기업 회원">기업 회원</option>
    <option value="인턴">인턴</option>
</select>
```
* 위 코드는 `<form:options>` 태그를 사용할 수도 있다. `<form:select>` 태그 내에 `<form:options>` 태그를   
  중첩하셔 사용한다. `<form:options>` 태그의 items 속성에 값 목록으로 사용할 Model이름을 설정하면 된다.
```jsp
<form:select path="loginType">
    <option value="">---선택하세요---</option>
    <form:options items="${loginTypes}"/>
</form:select>
```
* 위와 같이 `<form:options>` 태그는 주로 컬렉션에 있는 값을 `<option>`태그로 추가할 때 사용한다.

* `<form:option>` 태그는 `<option>` 태그를 직접 지정할 때 사용된다.

```jsp
<form:select path="loginType">
    <form:option value="일반 회원"/>
    <form:option value="기업 회원">기업</form:option>
    <form:option value="인턴" label="인턴" />
<form:select>
```
* `<form:option>` 커스텀 태그의 value속성은 `<option>` 태그의 value 속성 값을 지정한다.   
  만약 `<form:option>` 커텀태그의 몸체 내용을 입력하지 않으면 value속성에 지정한 값을 텍스트로 사용한다.

<hr/>

<h3>체크 박스 관련 커스텀 태그></h3>

* 한 개 이상의 값을 커멘드 객체의 특정 프로퍼티에 저장하고 싶다면 배열이나 List와 같은 타입을 사용해서 저장한다.
```java
public class MemberRegistRequest {

    private String[] favoriteOs;

    public String[] getFavoriteOs() {
        return favoriteOs;
    }

    public void setFavoriteOs(String[] favoriteOs) {
        this.favoriteOs = favoriteOs;
    }
}
```

* Spring이 제공하는 checkbox 타입의 `<input>` 태그는 다음과 같다.

<table>
    <tr>    
        <td>form:checkboxes</td>
        <td>커맨드 객체의 특정 프로퍼티와 관련된 checkbox 타입의 input 태그 목록을 생성한다.</td>
    </tr>
    <tr>    
        <td>form:checkbox</td>
        <td>커맨드 객체의 특정 프로퍼티와 관련된 한 개의 checkbox 타입의 input 태그를 생성한다.</td>
    </tr>
</table>

* `<form:checkboxes>` 태그는 items 속성을 이용하여 값으로 사용할 컬렉션을 지정한다.   
  path속성으로 커맨드 객체의 플퍼티를 지정한다.
```jsp
<p>
    <label>선호 OS</label>
    <form:checkboxes items="${favoriteOsNames}" path="favoriteOs" />
</p>
```

* `<form:checkbox>` 커스텀 태그는 한 개의 checkbox 타입의 `<input>` 태그를 한 개 생성할 때 사용된다.   
  이 태그는 value 속성과 label속성을 사용해서 값과 텍스트를 설정한다.
```jsp
<form:checkbox path="favoriteOs" value="WIN8" label="Widnows8" />
<form:checkbox path="favoriteOs" value="WIN10" label="Windows10" />
```
<hr/>

<h3>라디오버튼 관련 커스텀 태그</h3>

* 여러 가지 옵션들 중 한 가지를 선택해야 하는 경우, radio 타입의 `<input>` 태그를 사용한다.
* Spring이 제공하는 radio 타입의 `<input>` 태그들은 다음과 같다.

<table>
    <tr>    
        <td>form:radiobuttons</td>
        <td>커맨드 객체의 특정 프로퍼티와 관련된 radio타입의 input 태그 목록을 생성한다.</td>
    </tr>
    <tr>    
        <td>form:radiobutton</td>
        <td>커맨드 객체의 특정 프로퍼티와 관련된 한 개의 radio 타입의 input 태그를 생성한다.</td>
    </tr>
</table>

* `<form:radiobuttons>` 커스텀 태그는 다음과 같이 items속성에 값으로 사용할 컬렉션을 전달받고,   
  path속성에 커맨드 객체의 프로퍼티를 지정한다.
```jsp
<p>
    <label>주로 사용하는 개발 툴</label>
    <form:radiobuttons items="${tools}" path="tool" />
</p>
```

* `<form:radiobutton>` 커스텀 태그는 1개의 radio 타입 `<input>` 태그를 생성할 때 사용되며,   
  value와 label속성을 사용하여 값과 텍스트를 설정한다. 사용 법은 `<form:checkbox>`와 동일하다.
<hr/>

<h3>textarea 태그를 위한 커스텀 태그</h3>

* 게시글 내용과 같이 여러 줄을 입력받아야 하는 경우, `<textarea>` 태그를 사용한다.   
  Spring은 `<form:textarea>` 커스텀 태그를 제공한다. 이 태그를 이용하면 커맨드 객체와 관련된   
  `<textarea>` 태그를 생성할 수 있다.
```jsp
<p>
    <label for="etc">기타</label>
    <form:textarea path="etc" cols="20" rows="3"/>
</p>
```
* 위 코드는 아래와 같은 HTML 태그를 생성한다.
```jsp
<p>
    <label for="etc">기타</label>
    <textarea id="etc" name="etc" rows="3" cols="20"></textarea>
</p>
```