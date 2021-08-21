Session, Interceptor, Cookie
======

* 우선 로그인과 관련된 코드를 작성하자.

```java
public class AuthInfo {
	
	private Long id;
	private String email;
	private String name;
	
	public AuthInfo(Long id, String email, String name) {
		this.id = id;
		this.email = email;
		this.name = name;		
	}
	
	public Long getId() {
		return id;
	}
	
	public String getEmail() {
		return email;
	}
	
	public String getName() {
		return name;
	}
}
```

```java
public class Member {
    private Long id;
    private String email;
    private String password;
    private String name;
    private LocalDateTime registerDateTime;

    //..

    public boolean matchPassword(String password){
        return this.password.equals(password);
    }
}
```
* 이제 email과 password가 일치하는지 확인하여 `AuthInfo` 객체를 생성하는 `AuthService` 클래스를 작성하자.
```java
public class AuthService {
	
	private MemberDao memberDao;
	
	public void setMemberDao(MemberDao memberDao) {
		this.memberDao = memberDao;
	}
	
	public AuthInfo authenticate(String email, String password) throws WrongIdPasswordException {
		Member member = memberDao.selectByEmail(email);
		if(member == null) {
			throw new WrongIdPasswordException();
		}
		if(!member.matchPassword(password)) {
			throw new WrongIdPasswordException();
		}
		return new AuthInfo(member.getId(), member.getEmail(), member.getName());
	}
}
```
* 이제는 `AuthService`를 이용해서 로그인 요청을 처리하는 `LoginController` 클래스를 작성하자. 또한 폼에 입력한   
  값을 전달받기 위한 `LoginCommand` 클래스와 폼에 입력된 값이 올바른지 검사하기 위한 `LoginCommandValidator` 클래스를 작성하자.
```java
public class LoginCommand {
	private String email;
	private String password;
	private boolean rememberEmail;
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public boolean isRememberEmail() {
		return rememberEmail;
	}
	
	public void setRememberEmail(boolean rememberEmail) {
		this.rememberEmail = rememberEmail;
	}
}
```

```java
public class LoginCommandValidator implements Validator {
	
	@Override
	public boolean supports(Class<?> clazz) {
		return LoginCommand.class.isAssignableFrom(clazz);
	}
	
	@Override
	public void validate(Object target, Errors errors) {
		ValidationUtils.rejectIfEmptyOrWhitespace(errors,  "email",  "required");
		ValidationUtils.rejectIfEmpty(errors, "password", "required");
	}
}
```

```java
@Controller
@RequestMapping("/login")
public class LoginController {
	
	private AuthService authService;
	
	public void setAuthService(AuthService authService) {
		this.authService = authService;
	}
	
	@GetMapping
	public String form(LoginCommand loginCommand) {
		return "login/loginForm";
	}
	
	@PostMapping
	public String submit(LoginCommand loginCommand, Errors errors) {
		new LoginCommandValidator().validate(loginCommand, errors);
		if(errors.hasErrors()) {
			return "login/loginForm";
		}
		
		try {
			AuthInfo authInfo = authService.authenticate(loginCommand.getEmail(), loginCommand.getPassword());
			return "login/loginSuccess";
		} catch(WrongIdPasswordException e) {
			errors.reject("idPasswordNotMatching");
			return "login/loginForm";
		}
	}
}
```
<hr/>

<h2>컨트롤러에서 HttpSession 사용하기</h2>

* 위 로그인 기능에서 빠진 점은 바로 __로그인 상태를 유지하는 것__ 이다. 이를 구현하는 방법은 크게   
  `HttpSession`을 이용하는 방법과 `Cookie`를 이용하는 방법이 있다. 

* 컨트롤러에서 `HttpSession`을 사용하려면 다음 두 가지 방법 중 하나를 사용하면 된다.
  * 요청 매핑 어노테이션 적용 메소드에 `HttpSession` 파라미터 추가
  * 요청 매핑 어노테이션 적용 메소드에 `HttpServletRequest` 파라미터를 추가하고, 이 객체를 이용해서 `HttpSession` 구하기.

* 아래 코드는 첫 번째 방식을 사용한 예시이다.
```java
@PostMapping
public String form(LoginCommand loginCommand, Errors errors, HttpSession session) {

    // codes using session
}
```
  * 요청 매핑 어노테이션 적용 메소드에 `HttpSession` 파라미터가 존재할 경우, Spring MVC는 컨트롤러의 메소드 호출 시   
    `HttpSession` 객체를 파라미터로 전달한다. 이 때, `HttpSession`을 생성하기 전이면 새로운 `HttpSession`을 생성하고,   
    그렇지 않으면 기존에 존재하는 `HttpSession`을 전달한다.
* 아래 코드는 `HttpServletRequest#getSession()` 메소드를 사용하는 예시이다.
```java
@PostMapping
public String submit(LoginCommand loginCommand, Errors errors, HttpServletRequest req) {

    HttpSession session = req.getSession();
}
```
* `HttpSession` 객체에 attribute를 추가하려면 `addAttribute(key, value)` 메소드를 사용하면 된다.

* 로그아웃을 위한 컨트롤러에서는 `HttpSession`을 제거하면 되는데, 이는 `HttpSession#invalidate()` 메소드를 호출하면 된다.
<hr/>

<h2>Interceptor 사용하기</h2>

* 웹 app에서는 많은 기능들에 로그인 여부를 확인해야 한다. 각 기능을 구현한 컨트롤러 코드 마다 세션 확인 코드를   
  삽입하는 것은 많은 중복을 일으킨다. 이렇게 다수의 컨트롤러에 대해 동일한 기능을 적용해야할 때 사용할 수 있는 것이   
  `HandlerInterceptor` 이다.
<hr/>

<h3>HandlerInterceptor 인터페이스 구현하기</h3>

* `org.springframework.web.HandlerInterceptor` 인터페이스를 사용하면 다음의 세 시점에 공통 기능을 넣을 수 있다.
  * 컨트롤러(핸들러)의 실행 전
  * 컨트롤러(핸들러)의 실행 후, 아직 view의 실행 전
  * view를 실행한 이후
* 위의 3가지 시점을 처리하기 위해 `HandlerInterceptor` 인터페이스는 다음 메소드를 정의하고 있다.
  * `boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception`
  * `void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception`
  * `void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception`

* `preHandle()` 메소드는 컨트롤러(핸들러) 객체를 실행하기 전에 필요한 기능을 구현할 때 사용한다.   
  handler 파라미터는 웹 요청을 처리할 컨트롤러(핸들러) 객체이다. 이 메소드를 사용하면 다음 작업이 가능하다.  
  * 로그인 하지 않은 경우 컨트롤러를 실행하지 않음.
  * 컨트롤러를 실행하기 전에 컨트롤러에서 필요로 하는 정보 생성
* `preHandle()` 메소드의 반환형은 boolean인데, 만약 __false__ 를 반환하면 컨트롤러(또는 다음 HandlerInterceptor)를 실행하지 않는다.

* `postHandle()` 메소드는 컨트롤러(핸들러)가 정상적으로 실행된 이후에 추가 기능을 구현할 때 사용된다.   
  만약 컨트롤러가 예외를 발생시키면 이 메소드는 실행되지 않는다.

* `afterCompletion()` 메소드는 view가 client의 응답을 전송한 뒤에 사용된다. 컨트롤러의 실행 과정에서 예외가 발생하면   
  이 메소드의 네 번째 파라미터로 전달되고, 예외가 발생하지 않으면 그 파라미터는 null이 된다.   
  따라서 컨트롤러 실행 이후에 예기치 않게 발생한 예외를 로그로 남기거나 실행 시간을 기록하는 등의 후처리를 하기에 적합하다.

```java
public class AuthCheckInterceptor implements HandlerInterceptor {
    @Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		HttpSession session = request.getSession(false);
		if(session != null) {
			Object authInfo = session.getAttribute("authInfo");
			if(authInfo != null) {
				return true;
			}
		}
		response.sendRedirect(request.getContextPath() + "/login");
		return false;
	}
}
```
* 위 코드에서 `preHandle()` 메소드는 `HttpSession` 객체에 "authInfo" 속성이 존재하면 true를 반환한다.   
  존재하지 않는다면 redirect 응답을 생성한 뒤 false를 반환한다.
* 참고 : `request.getContextPath()` 메소드는 현재 컨텍스트 경로를 반환하는데, 만약 웹 app의 경로가   
  `http://localhost:8080/chap13` 이라면 컨텍스트 경로는 `/chap13`이 된다. 따라서 위 코드에서 redirec하는 경로는   
  `http://localhost:8080/chap13/login` 이 된다.
<hr/>

<h3>HandlerInterceptor 설정하기</h3>

* `HandlerInterceptor`를 구현하면 `HandlerInterceptor`를 어디에 적용할지 설정해야 한다.
```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

    //..

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authCheckInterceptor()).addPathPatterns("/edit/**");
    }

    @Bean
    public AuthCheckInterceptor authCheckInterceptor() {
        return new AuthCehckInterceptor();
    }
}
```
* `WebMvcConfigurer#addInterceptors()` 메소드는 인터셉터를 설정하는 메소드이다. 
* `InterceptorRegistry#addInterceptor()` 메소드는 `HandlerInterceptor` 객체를 설정한다.   
  이 메소드는 `InterceptorRegistration` 객체를 반환하는데, 이 객체의 `addPathPatterns()` 메소드는   
  인터셉터를 적용할 경로 패턴을 지정한다. 이 경로는 Ant 경로 패턴을 사용하는데, 두 개 이상의 경로 패턴을 지정하려면   
  각 경로 패턴을 콤마로 구분하여 지정하면 된다. 위 코드에서는 /edit/로 시작하는 모든 경로에 인터셉터를 적용한다.

* Ant 경로 패턴 : Ant 패턴은 *, **, ?의 세 가지 특수 문자를 이용해서 경로를 표현한다.
  * `*` : 0개 또는 그 이상의 글자
  * `?` : 1개 글자
  * `**` : 0개 또는 그 이상의 폴더 경로
<hr/>

<h2>컨트롤러에서 쿠키 사용하기</h2>

* 쿠키는 사용자의 편의를 위해 아이디를 저장해두었다가 다음에 로그인할 때 아이디를 자동으로 넣어주는 등의 기능을 한다.

* Spring MVC에서 쿠키를 사용하는 방법 중 하나는 __@CookieValue__ 어노테이션을 사용하는 것이다.   
  이 어노테이션은 요청 매핑 어노테이션 적용 메소드의 `Cookie` 타입 파라미터에 적용한다.
```java
@Controller
@RequestMapping("/login")
public class LoginController {

    //..

    @GetMapping
    public String form(LoginCommand loginCommand, @CookieValue(value="REMEMBER", required=false) Cookie rCokie) {
        if(rCooke != null) {
            loginCommand.setEmail(rCookie.getValue());
            loginCommand.setRememberEmail(true);
        }
        return "login/loginForm";
    }
}
```
* __@CookieValue__ 어노테이션의 value 속성은 쿠키의 이름을 지정한다. 위 코드는 이름이 REMEMBER인 쿠키를   
  `Cookie` 타입으로 전달받는다. 지정한 이름을 가진 쿠키가 존재하지 않을 수도 있다면 required속성을 false로 지정한다.   
  (참고 : required 속성의 기본값은 true이며, true일 때 해당 이름을 가진 쿠기가 없다면 예외가 발생한다.)

* 위 코드는 쿠키이름을 통해 값을 읽어오는 역할을 하며, 실제로 REMEMBER 쿠키를 생성할 때에는 `HttpServletResponse`   
  객체가 필요하다. 아래 코드에서는 로그인을 처리하는 `submit()` 메소드의 파라미터로 `HttpServletResponse` 타입을 추가했다.
```java
@Controller
@RequestMapping("/login")
public class LoginController {

    //..

    @PostMapping
    public String submit(LoginCommand loginCommand, Errors errors, HttpSession session, HttpServletResponse response) {
        new LoginCommandValidator.validate(loginCommand, errors);
        if(errors.hasErrors()) {
            return "login/loginForm";
        }
        try {
            AuthInfo authInfo = authService.authenticate(loginCommand.getEmail(), loginCommand.getPassword());
            session.setAttribute("authInfo", authInfo);

            Cookie rememberCookie = new Cookie("REMEMBER", loginCommand.getEmail());
            rememberCookie.setPath("/");
            if(loginCommand.isRememberEmail()) {
                rememberCookie.setMaxAge(60 * 60 * 24 * 30);
            } else {
                rememberCookie.setMaxAge(0);
            }
            response.addCookie(rememberCookie);
            return "login/loginSuccess";
        } catch (IdPasswordNotMatchingException e) {
            errors.reject("idPasswordNotMatching");
            return "login/loginForm";
        }
    }
}
```