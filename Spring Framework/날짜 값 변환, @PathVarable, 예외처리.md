날짜 값 변환, @PathVariable, Exception 처리
======

<h3>날짜를 이용한 회원 검색 기능</h3>

* 회원 가입 일자를 기준으로 검색하는 기능을 구현하며 Spring MVC의 몇 가지 특징을 살펴보자.
```java
// MemberDao.java

public List<Member> selectByRegdate(LocalDateTime from, LocalDateTime to) {
	
    List<Member> results = jdbcTemplate.query("SELECT * FROM MEMBER WHERE REGDATE BETWEEN ? AND ? ORDER BY REGDATE DESC",
			new RowMapper<Member>() {
				@Override
				public Member mapRow(ResultSet rs, int rowNum) throws SQLException{
					Member member = new Member(rs.getString("EMAIL"), rs.getString("PASSWORD"), rs.getString("NAME"), 
					rs.getTimestamp("REGDATE").toLocalDateTime());
					member.setId(rs.getLong("ID"));
					return member;
				}
			}, from, to);
	return results;
}   
```
* 위의 `selectByRegdate()` 메소드는 REGDATE 값이 파라미터로 전달받은 from과 to사이에 있는 Member목록을 구한다.
<hr/>

<h3>커맨드 객체 Date 타입 프로퍼티 변환 처리 : @DateTimeFormat</h3>

```java
// ListCommand.java

public class ListCommand {
	private LocalDateTime from;
	private LocalDateTime to;
	
	public LocalDateTime getFrom() {
		return from;
	}
	public void setFrom(LocalDateTime from) {
		this.from = from;
	}
	public LocalDateTime getTo() {
		return to;
	}
	public void setTo(LocalDateTime to) {
		this.to = to;
	}	
}
```
* 위의 from과 to는 검색을 위해 전달받는 파라미터이다.   
  jsp에서는 아래 입력 폼을 사용하여 데이터를 전달받는다고 하자.
```jsp
<input type="text" name="from" />
<input type="text" name="to" />
```
* 이 때의 문제는 `<input>`에 사용자가 입력한 문자열을 `LocalDateTime`형으로 변환해야한다는 것이다.   
  만약 2020년 9월 9일 오후 3시를 표현하기 위해 사용자가 2020090915 를 입력했다 하자.
* Spring은 Long, int와 같은 기본 데이터 타입으로의 변환은 기본적으로 처리해주지만, `LocalDateTime` 타입으로의 변환은   
  추가 설정이 필요하다. 이 추가설정은 필요한 곳에 __@DateTimeFormat__ 어노테이션을 적용하는 것이다.
```java
public class ListCommand {
	
	@DateTimeFormat(pattern="yyyyMMddHH")
	private LocalDateTime from;
	
	@DateTimeFormat(pattern="yyyyMMddHH")
	private LocalDateTime to;

    //..

}
```
* 위와 같이 커맨드 객체에 __@DateTimeFormat__ 어노테이션이 적용되어 있으면, pattern 속성값에 지정한 형식을   
  이용해서 문자열을 `LocalDateTime` 타입으로 변환한다. 위에선 "yyyyMMddHH"를 주었는데, 이는 "2020090915"의   
  문자열을 "2020년 9월 9일 15시" 값을 갖는 `LocalDateTime` 객체로 변환해준다.

* JSTL이 제공하는 날짜 형식 태그는 아쉽게도 Java 8의 `LocalDateTim` 타입은 지원하지 않는다.   
  따라서 아래와 같이 태그파일을 사용해서 `LocalDateTime`값을 지정한 형식으로 출력해야 한다.   
  따라서 아래와 같이 .tag 파일을 만들어야 한다.

```jsp
<%@ tag body-content="empty" pageEncoding="utf-8" %>
<%@ tag import="java.time.format.DateTimeFormatter" %>
<%@ tag trimDirectiveWhitespaces="true" %>
<%@ attribute name="value" required="true" type="java.time.temporal.TemporalAccessor" %>
<%@ attribute name="pattern" type="java.lang.Strng" %>
<%
    if(pattern == null) pattern = "yyyy-MM-dd";
%>
<%= DateTimeFormatter.ofPattern(pattern).format(value) %>
```

* `MemberListController` 의 `list()` 메소드에 맞게 `ListCommand` 객체를 위한 form을 제공하고,   
  members 의 속성을 이용해서 회원 목록을 출력하도록 JSP 코드를 구현해보자.
```jsp
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="tf" tagdir="/WEB-INF/tags" %>

<!-- 생략 -->

<c:if test="${! empty members}" >
    <table>
        <tr>
            <th>아이디</th><th>이메일</th>
            <th>이름</th><th>가입일</th>
        </tr>
        <c:forEach var="mem" items="${members}">
            <tr>
                <td>${mem.id}</td>
                <td><a href="<c:url value="/members/${mem.id}"/>">${mem.email}</a></td>
                <td>${mem.name}</td>
                <td><tf:formatDateTime value="${mem.registerDateTime}" pattern="yyyy-MM-dd" /></td>
            </tr>
        </c:forEach>
    </table>
</c:if>
```
<hr/>

<h3>변환 에러 처리</h3>

* 사용자가 yyyyMMddHH 형식으로 입력하지 않으면, 변환 시에 예외가 발생한다.   
  이를 처리하기 위해 `Errors` 타입 파라미처를 요청 매핑 어노테이션에 추가하자.
```java
@Controller
public class MemberListController {
	
	private MemberDao memberDao;
	
	@RequestMapping("/members")
	public String list(@ModelAttribute("cmd") ListCommand listCommand, Errors errors, Model model) {
		if(errors.hasErrors()) {
			return "member/memberList";
		}
		if(listCommand.getFrom() != null && listCommand.getTo() != null) {
			List<Member> members = memberDao.selectByRegdate(listCommand.getFrom(), listCommand.getTo());
			model.addAttribute("members", members);
		}
		return "member/memberList";
	}
}
```
* 요청 매핑 어노테이션 적용 메소드가 `Errors` 타입 파라미터를 가질 경우, __@DateTimeFormat__ 에 지정한 형식에   
  맞지 않으면 `Errors` 객체에 "typeMismatch" 에러 코드를 추가한다. 따라서 `Errors#hasErrors()` 메소드로   
  에러 코드가 존재하는지를 확인할 수 있다.
<hr/>

<h2>변환 처리에 대한 이해</h2>

* __@DateTimeFormat__ 어노테이션을 사용하면 지정한 형식의 문자열을 `LocalDateTime` 타입으로 변환해주는데,   
  이 변환과정을 수행해주는 것은 `WebDataBinder` 객체이다.

* Spring MVC는 요청 매핑 어노테이션 적용 메소드와 `DispatcherServlet` 사이를 연결하기 위해 `RequestMappingHandlerAdapter`   
  객체를 사용하는데, 이 객체는 요청 파라미터와 커맨드 객체 사이의 변환 처리를 위해 `WebDataBinder`를 이용한다.

* `WebDataBinder`는 직접 타입을 변환하지 않고, `ConversionService`에 그 역할을 위임한다.   
  Spring MVC를 위한 __@EnableWebMvc__ 어노테이션을 사용하면 `DefaultFormattingConversionService`를 `ConversionService`로 사용한다.

* `DefaultFormattingConversionService`는 int, long과 같은 기본 데이터타입 뿐만 아니라 __@DateTimeFormat__   
  어노테이션을 사용한 시간 관련 타입 변환 기능을 제공한다. 이런 이유로 커맨드로 사용할 클래스에 __@DateTimeFormat__   
  어노테이션만 붙이면 지정한 형식의 문자열을 시간 타입의 값으로 받을 수 있는 것이다.

* `WebDataBinder`는 `<form:input>` 태그에도 사용된다. 이 태그를 사용하면 path 속성에 지정한 프로퍼티 값을   
  String으로 변환해서 `<input>` 태그의 value 속성값으로 생성한다. 이때, 프로퍼티 값을 String으로 변환할 때   
  `WebDataBinder`의 `ConversionService`를 사용한다.
<hr/>

<h2>@PathVariable을 이용한 경로 변수 처리</h2>

* 다음은 ID가 10인 회원의 정보를 조회하기 위한 URL이라고 하자.
```text
http://localhost:8080/chap14/members/10
```
* 이 형식의 URL을 사용하면 각 회원마다 경로의 마지막 부분이 달라진다.   
  이렇게 경로의 일부가 고정되어 있지 않고 달라질 때 사용할 수 있는 것이 __@PathVariable__ 어노테이션이다.   
  다음은 __@PathVariable__ 어노테이션을 사용한 예시이다.
```java
@Controller
public class MemberDetailController {
	
	private MemberDao memberDao;
	
	public void setMemberDao(MemberDao memberDao) {
		this.memberDao = memberDao;
	}
	
	@GetMapping("/members/{id}")
	public String detail(@PathVariable("id") Long memberId, Model model) {
		Member member = memberDao.selectById(memberId);
		if(member == null) {
			throw new MemberNotFoundException();
		}
		model.addAttribute("member", member);
		return "member/memberDetail";
	}
}
```
* 매핑 경로에 '{경로변수}' 와 같이 중괄호로 둘러쌓인 부분을 __경로 변수__ 라 한다.   
  '{경로변수}'에 해당하는 값은 같은 경로 변수이름을 지정한 __@PathVariable__ 파라미터에 전달된다.   
  위의 경우, id가 memberId 파라미터에 할당된다.
<hr/>

<h2>컨트롤러의 예외 처리</h2>

* 만약 위 코드에서 없는 ID를 경로변수로 사용한다면, `MemberNotFoundException`이 발생한다.   
  이 예외는 try-catch절을 사용하여 알맞게 처리할 수 있지만, 타입 변환 실패에 따른 예외는 이 방식으로 처리할 수 없다.   
  이때 유용하게 사용할 수 있는 것이 바로 __@ExceptionHandler__ 어노테이션이다.

* 같은 컨트롤러에 __@ExceptionHandler__ 어노테이션을 적용한 메소드가 존재하면 그 메소드가 예외를 처리한다.   
  따라서 컨트롤러에서 발생한 예외를 직접 처리하고 싶다면 __@ExceptionHandler__ 어노테이션을 적용한 메소드를 구현하면된다.
```java
@Controller
public class MemberDetailController {

    //..

    @ExceptionHandler(TypeMismatchException.class)
	public String handleTypeMismatchException() {
		return "member/invalidId/";
	}
	
	@ExceptionHandler(MemberNotFoundException.class)
	public String handleNotFoundException() {
		return "member/noMember";
	}
}
```
* 위 코드에서 `TypeMismatchException`이 발생하면, `handleTypeMismatchException()` 메소드가 이를 처리하고,   
  `MemberNotFoundException`이 발생하면, `handleNotFoundException()` 메소드가 이를 처리한다.

* __@ExceptionHandler__ 어노테이션을 적용한 메소드는 컨트롤러의 요청 매핑 어노테이션 적용 메소드와 마찬가지로   
  view의 이름을 반환할 수 있다.
<hr/>

<h3>@ControllerAdvice를 이용한 공통 예외 처리</h3>

* 컨트롤러 클래스에 __@ExceptionHandler__ 어노테이션을 적용하면 해당 컨트롤러에서 발생한 예외만을 처리한다.   
  만약 다수의 컨트롤러에서 동일한 예외가 발생할 수 있고, 이에 대한 처리 코드가 동일하다면 코드의 중복이 발생한다.   
  이러한 경우에는 __@ControllerAdvice__ 어노테이션을 사용하여 중복을 없앨 수 있다.
```java
@ControllerAdvice("spring")
public class CommonExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException() {
        return "error/commonException";
    }
}
```
* __@ControllerAdvice__ 어노테이션이 적용된 클래스는 지정한 범위의 컨트롤러에 공통으로 사용될 설정을 지정할 수 있다.   
  위 코드는 "spring" 패키지와 그 하위 패키지에 속한 컨트롤러 클래스를 위한 공통 기능을 정의했다.   
  spring 패키지와 그 하위 패키지에 속한 컨트롤러에서 `RuntimeException`이 발생하면, `handleRuntimeException()` 메소드를   
  통해서 예외를 처리한다.

* __@ControllerAdvice__ 적용 클래스가 동작하려면 해당 클래스를 Spring에 Bean으로 등록해야 한다.
<hr/>

<h3>@ExceptionHandler 적용 메소드의 우선 순위</h3>

* __@ControllerAdvice__ 가 적용된 클래스에 있는 __@ExceptionHandler__ 가 적용된 메소드와, 컨트롤러 클래스에 있는   
  __@ExceptionHandler__ 메소드 중 컨트롤러 클래스에 적용된 __@ExceptionHandler__ 메소드가 우선한다.   
  즉, 컨트롤러의 메소드를 실행하는 과정에서 예외가 발생하면 다음의 순서로 예외를 처리할 __@ExceptionHandler__ 메소드를 찾는다.
  1. 같은 컨트롤러에 위치한 __@ExceptionHandler__ 적용 메소드 중 해당 예외를 처리할 수 있는 메소드 검색
  2. (1)번에 적절한 메소드가 없을 경우 __@ControllerAdvice__ 적용 클래스에 위치한 __@ExceptionHandler__ 메소드 검색

* __@ControllerAdvice__ 어노테이션은 공통 설정을 적용할 컨트롤러 대상을 지정하기 위해 다음 속성을 제공한다.

<table>
    <tr>
        <td>속성</td>
        <td>타입</td>
        <td>설명</td>
    </tr>
    <tr>
        <td>value (= basePackages)</td>
        <td>String[]</td>
        <td>공통 설정을 적용할 컨트롤러가 속하는 기준 패키지 지정.</td>
    </tr>
    <tr>
        <td>annotations</td>
        <td>Class (제네릭 타입 : ? extends Annotation)[]</td>
        <td>특정 어노테이션이 적용된 컨트롤러 대상 지정.</td>
    </tr>
    <tr>
        <td>assignableTypes</td>
        <td>Class(제네릭 타입 : ?)[]</td>
        <td>특정 타입 또는 그 하위 타입인 컨트롤러 대상 지정.</td>
    </tr>
</table>

<hr/>

<h3>@ExceptionHandler 어노테이션 적용 메소드의 반환형</h3>

* __@ExceptionHandler__ 어노테이션을 붙인 메소드는 다음 파라미터를 가질 수 있다.
  * `HttpServletRequest`, `HttpServletResponse`, `HttpSession`
  * `Model`
  * `Exception`
* 반환 가능한 타입은 다음과 같다.
  * `ModelAndView`
  * `String` (View 이름)
  * (@ResponseBody 어노테이션 적용한 경우) 임의 객체
  * ResponseEntity