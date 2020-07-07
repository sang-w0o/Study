JSON 요청과 응답 처리
======

<h2>JSON 개요</h2>

* JSON(JavaScript Object Notation)은 간단한 형식을 갖는 문자열로, 데이터 교환에 주로 사용한다.   
  아래는 JSON 형식으로 표현한 데이터의 예시이다.
```json
{
    "name":"sangwoo",
    "birthday":"1998-09-09",
    "age":23,
    "edu":[
        {
            "title":"Soongsil Univ.",
            "year":2018
        },
        {
            "title":"Sewha High",
            "year":2014
        }
    ]
}
```
* JSON의 규칙은 __중괄호를 사용하여 객체롤 표현__ 하며, 객체는 __이름, 값의 쌍을 갖는다__.   
  이때 이름과 값은 `:`로 구문한다. 값에는 다음이 올 수 있다.
  * 문자열, 숫자, boolean, null
  * 배열
  * 다른 객체
<hr/>

<h2>JACKSON 의존 설정</h2>

* Jackson은 Java 객체와 JSON 형식 문자열 간의 변환을 처리하는 라이브러리 이다.   
  이를 사용하기 위해선 pom.xml에 다음 의존을 추가해야 한다.
```xml
<!-- Jackson core와 Jackson Annotation 의존 -->
<dependency>
  	<groupId>com.fasterxml.jackson.core</groupId>
  	<artifactId>jackson-databind</artifactId>
  	<version>2.9.4</version>
</dependency>
<!-- java8 date/time 지원을 위한 Jackson 모듈 -->
<dependency>
	<groupId>com.fasterxml.jackson.datatype</groupId>
	<artifactId>jackson-datatype-jsr310</artifactId>
	<version>2.9.4</version>
</dependency>
```
* Jackson은 아래와 같이 Java 객체와 JSON 사이의 변환을 처리한다.
```java
public class Person {
    private String name;
    private int age;

    // getters, setters
}
```
  * 위 Java 객체를 JSON으로 변환하면 아래와 같이 된다.
```json
{
    "name":"name",
    "age":23
}
```
* Jackson은 프로퍼티(getters 또는 설정에 따라 필드)의 이름과 값을 JSON객체의 (이름, 값) 쌍으로 사용한다.
<hr/>

<h2>@RestController로 JSON 형식 응답하기</h2>

* Spring MVC에서 JSON 형식으로 데이터를 응답하는 것은 __@Controller__ 대신 __@RestController__ 어노테이션을 적용하면 된다.
```java
@RestController
public class RestMemberController {

	private MemberDao memberDao;
	private MemberRegisterService registerService;
	
	@GetMapping("/api/members")
	public List<Member> members() {
		return memberDao.selectAll();
	}
	
	@GetMapping("/api/members/{id}")
	public Member member(@PathVariable Long id, HttpServletResponse response) throws IOException {
		Member member = memberDao.selectById(id);
		if(member == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		return member;
	}
	
	public void setMemberDao(MemberDao memberDao) {
		this.memberDao = memberDao;
	}
	
	public void setRegisterService(MemberRegisterService registerService) {
		this.registerService = registerService;
	}
}
```
* __@RestController__ 어노테이션을 붙인 경우, Spring MVC는 요청 매핑 어노테이션을 붙인 메소드가 반환한 객체를 알맞은   
  형식으로 변환해서 응답 데이터로 전송한다. 이때 Class Path에 Jackson이 존재하면, JSON 형식의 문자열로 변환해서 응답한다. 
* 마찬가지로 `RestMemberController`를 설정 클래스에 추가하자.
```java
@Configuration
public class ControllerConfig {

    //..

    @Bean
    public RestMemberController restApi() {
        RestMemberController cont = new RestMemberController();
        cont.setMemberDao(memberDao());
        cont.setMemberDao(memberRegSvc());
    }
}
```
<hr/>

<h3>@JsonIgnore를 이용한 제외 처리</h3>

* 비밀번호 등의 민감한 데이터는 응답 결과에 포함시키면 안된다.   
  password 데이터를 응답 결과에서 제외시킨다 하면, __@JsonIgnore__ 어노테이션을 적용하면 된다.
```java
public class Member {
    private Long id;
    private String email;

    @JsonIgnore
    private String password;

    private String name;
    private LocalDateTime registerDateTime;
}
```
<hr/>

<h3>날짜 형식 변환 처리 : @JsonFormat 사용</h3>

* 위 `Member` 클래스의 registerDateTime 값은 `LocalDateTime` 타입인데, JSON값은 아래와 같이 배열로 바뀐다.
```json
{
    "registerDateTime":[
        2020,
        7,
        7,
        10,
        39,
        20
    ]
}
```
* 보통 날짜나 시간은 배열이나 숫자 보다는 "2020-07-07- 10:39:20"과 같이 특정 형식을 갖는 것을 선호한다.   
  Jackson에서 날짜나 시간 값을 특정한 형식으로 표현하는 가장 쉬운 방법은 __@JacksonFormat__ 어노테이션을 적용하는 것이다.   
  예를 들어 `ISO-8601` 형식으로 변환하고 싶다면 아래와 같이 shape 속성으로 `Shape.STRING`을 지정하면 된다.
```java
public class Member {
    private Long id;
    private String email;
    private String name;

    @JsonFormat(shape=Shape.STRING)
    private LocalDateTime registerDateTime;
}
```
* 위와 같이 어노테이션을 적용하면 registerDateTime은 ISO-8601 형식으로 출력된다.
* 만약 원하는 형식으로 변환하여 출력하고 싶다면 __@JsonFormat__ 의 pattern 속성을 사용하면 된다.
```java
public class Member {
    private Long id;
    private String email;
    private String name;

    @JsonFormat(pattern="yyyyMMddHHmmss")
    private LocalDateTime registerDateTime;
}
```
* 위와 같이 pattern을 지정하면, 날짜는 20200707103920 와 같이 출력된다.   
  pattern 속성은 `java.time.format.DateTimeFormatter` 이나 `java.text.SimpleDateFormat` 클래스의   
  API 문서에 정의된 패턴을 따른다.
<hr/>

<h3>날짜 형식 변환 처리 : 기본 적용 설정</h3>

* 만약 날짜 타입에 해당하는 모든 대상에 동일한 변환 규칙을 적용하고 싶다면, 변수마다 __@JsonFormat__   
  어노테이션을 적용하는 것 보단 __Spring MVC 설정 변경__ 이 더 편리하다.

* Spring MVC는 Java 객체를 HTTP 응답으로 변환할 때 `HttpMessageConverter`을 사용한다.   
  예를 들어 Jackson을 이용해서 Java 객체를 JSON으로 변환할 때는 `MappingJackson2HttpMessageConverter`를 사용하고,   
  Jaxb를 이용해서 XML로 변환할 때에는 `Jaxb2RootElementHttpMessageConverter`를 사용한다.   
  따라서 JSON으로 변환할 때 사용하는 `Mapping2JacksonHttpMessageConverter`를 새롭게 등록해서 날짜 형식을 원하는 형식으로   
  변환하도록 설정하면 모든 날짜 형식에 동일한 변환 규칙을 적용할 수 있다.
```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

    //..

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().featuresToDisable(
            SerializationFeature.WRITE_DATE_AS_TIMESTAMPS).build();
        converters.add(0, new MappingJackson2HttpMessageConverter(objectMapper));
    }
}
```
* `extendMessageConverters()` 메소드는 `WebMvcConfigurer`에 정의된 메소드로서, `HttpMessageConverter`를 추가로   
  설정할 때 사용한다. __@EnableWebMvc__ 어노테이션을 사용하면 Spring MVC는 여러 형식으로 변환할 수 있는   
  `HttpMessageConverter`를 미리 등록한다. `extendMessageConverters()`는 등록된 `HttpMessageConverter`목록을 인자로 받는다.

* 미리 등록된 `HttpMessageConverter`에는 Jackson을 이용하는 것도 포함되어 있기 때문에 __새로 생성한 `HttpMessageConverter`는__   
  __목록의 제일 앞에 위치시켜야 한다. 그래야 가장 먼저 적용된다.__

```java
ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().featuresToDisable(
        SerializationFeature.WRITE_DATE_AS_TIMESTAMPS).build();
converters.add(0, new MappingJackson2HttpMessageConverter(objectMapper));
```
* 위 코드는 JSON으로 변환할 때 사용할 `ObjectMapper`를 생성한다. `Jackson2ObjectMapperBuilder`는 `ObjectMapper`를 보다 쉽게   
  생성할 수 있도록 Spring이 제공하는 클래스이다. 위 코드는 `featuresToDisable()` 메소드를 이용하여 Jackson이 날짜 형식을   
  출력할 때 Unix Timestamp로 출력하는 기능을 비활성화 한다. 이 기능을 비활성화하면 `ObjectMapper`는 날짜 타입의 값을   
  `ISO-8601` 형식으로 출력한다.

* 새로 생성한 `ObjectMapper`를 사용하는 `MappingJackson2HttpMessageConverter` 객체를 converters의 첫 번째 항목으로   
  등록하면 설정이 끝난다.

* 만약 모든 `java.util.Date` 타입의 값을 원하는 형식으로 출력하도록 설정하고 싶다면   
  `Jackson2ObjectMapperBuilder#simpleDateFormat()` 메소드를 이용하여 패턴을 지정하면 된다.
```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

    //..

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().simpleDateFormat("yyyyMMddHHmmss").build();
        converters.add(0, new MappingJackson2HttpMessageConverter(objectMapper));
    }
}
```
* 만약 모든 `LocalDateTime` 타입에 대해 원하는 패턴을 설정하고 싶다면 `serializerByType()` 메소드를 이용해서   
  `LocalDateTime` 타입에 대한 `JsonSerializer`를 직접 설정하면 된다.
```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

    //..

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().serializerByType(LocalDateTime.class,
            new LocalDateTimeSerializer(formatter)).build();
        converters.add(0, new MappingJackson2HttpMessageConverter(objectMapper));
    }
}
```
* 주의 : `MappingJackson2HttpMessageConverter`가 사용할 `ObjectMapper` 자체에 시간 타입을 위한 변환 설정을 추가해도   
  __개별 속성에 적용한 @JsonFormat 어노테이션 설정이 우선한다__.
<hr/>

<h2>@RequestBody로 JSON 요청 처리</h2>

* 위의 과정은 응답을 JSON으로 변환하는 것에 대한 것이다. 이제 반대로 JSON 형식의 요청 데이터를   
  JAVA 객체로 변환하는 기능에 대해 알아보자. POST나 PUT 방식을 사용하면 `name=이름&age=23`와 같은 쿼리 문자열   
  형식이 아니라 `{"name":"이름","age":23}`와 같은 JSON 형식의 데이터를 요청 데이터로 전송할 수 있다.

* JSON 형식으로 전송된 요청 데이터를 커맨드 객체로 전달받는 방식은 매우 간단한데, __@RequestBody__ 어노테이션을 적용하면 된다.
```java
@RestController
public class RestMemberController {
    private MemberDao memberDao;
    private MemberRegisterService registerService;

    //..

    @PostMapping("/api/members")
    public void newMember(@RequestBody @Valid RegisterRequest regReq, HttpServletResponse response) throws IOException {

        try {
            Long newMemberd = registerService.regist(regReq);
            response.setHeader("Location", "/api/members/" + newMemberId);
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch(MemberException e) {
            response.sendError(HttpServletResponse.SC_CONFLICT);
        }
    }

    //..
}
```
* __@RequestBody__ 어노테이션을 커맨드 객체에 붙이면, JSON 형식의 문자열을 해당 JAVA 객체로 변환한다.

* Spring MVC가 JSON 형식으로 전송된 데이터를 올바르게 처리하려면 요청 컨텐츠 타입이 `application/json` 이어야 한다.   
  보통 POST방식의 폼 데이터는 쿼리 문자열인 `p1=v1&p2=v2`로 전송되는데, 이 컨텐츠 타입은 `application/x-www-form-urlencoded` 이다.
<hr/>

<h3>JSON 데이터의 날짜 형식 다루기</h3>

* 별도 설정을 하지 않으면 `yyyy-MM-ddTHH:mm:ss` 패턴, (시간대가 없는 JSR-8601 형식)의 문자열을 `LocalDateTime`과   
  `Date`로 변환한다.
* 특정 패턴을 가진 문자열을 `LocalDateTime`이나 `Date` 타입으로 변환하고 싶다면 __@JsonFormat__ 어노테이션의   
  pattern 속성을 사용해서 패턴을 지정해야 한다.
```java
@JsonFormat(pattern="yyyyMMddHHmmss")
private LocalDateTime birthDateTime;

@JsonFormat(pattern="yyyyMMdd HHmmss")
private Date birthDate;
```
* 특정 속성이 아니라 해당 타입을 갖는 모든 속성에 패턴을 적용하고 싶다면 Spring MVC 설정을 추가하면 된다.
```java
@Configuration
@EnableWebMvc

public class MvcConfig implements WebMvcConfigurer {

    //..

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().featuresToEnable(SerializationFeature.INDENT_OUTPUT)
            .deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(formatter))
            .simpleDateFormat("yyyyMMdd HHmmss")
            .build();
        
        converters.add(0, new MappingJackson2HttpMessageConverter(objectMapper));
    }
}
```
* `deserializerByType()` 메소드는 JSON 데이터를 `LocalDateTime`타입으로 변환할 때 사용할 패턴을 지정하고,   
  `simpleDateFormat()`은 `Date` 타입으로 변환할 때 사용할 패턴을 지정한다.
* 주의 : `simpleDateFormat()`은 `Date`타입을 JSON데이터로 변환할 때에도 사용된다.
<hr/>

<h3>요청 객체 검증하기</h3>

* 위에서 작성한 `newMember()` 메소드를 다시 보자.
```java
@PostMapping("/api/members")
public void newMember(@RequestBody @Valid RegisterRequest regReq, HttpServletResponse response) throws IOException {

    //..

}
```
* JSON 형식으로 전송한 데이터를 변환한 객체도 동일한 방식으로 __@Valid__ 어노테이션이나 별도 Validator를 이용해서 검증할 수 있다.   
  __@Valid__ 어노테이션을 사용한 경우, 검증에 실패하면 400(Bad Request) 상태 코드를 응답한다.

* Validator를 사용할 경우, 다음과 같이 직접 상태 코드를 처리해야 한다.
```java
@PostMapping("/api/members")
public void newMember(@RequestBody RegisterRequest regReq, Errors errors, HttpServletResponse response) throws IOException {

    try {
        new RegisterRequestValidator().validate(regReq, errors);
        if(errors.hasErrors()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
    } catch(MemberException ex) {
        response.sendError(HttpServletResponse.SC_CONFLICT);
    }
}
```
<hr/>

<h2>ResponseEntity로 객체 반환하고 응답 코드 지정하기</h2>

* 지금까지는 상태 코드를 지정하기 위해 `HttpServletResponse#setStatus(), sendError()` 메소드를 사용했다.   
  문제는 위와 같은 메소드를 사용하여 404 응답을 하면, JSON형식이 아닌 서버사 기본으로 제공하는 HTML을   
  응답 결과로 제공한다는 점이다. API를 호출하는 프로그램 입장에서 JSON응답과 HTML응답을 모두 처리하는 것은   
  부담스럽다. 404나 500과 같이 처리에 실패한 경우, HTML응답 데이터 대신에 JSON형식의 응답 데이터를 전송해야   
  API 호출 프로그램이 일관된 방법으로 응답을 처리할 수 있을 것이다.
<hr/>

<h3>ResponseEntity를 이용한 응답 데이터 처리</h3>

* 정상인 경우와 비정상인 경우 모두 JSON응답을 전송하는 방법은 `ResponseEntity`를 사용하는 것이다.   
  아래는 에러 상황일 때 응답으로 사용할 클래스이다.
```java
public class ErrorResponse {
    private String message;

    public void ErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
```
* `ResponseEntity`를 사용하면 `member()` 메소드를 아래와 같이 구현할 수 있다.
```java
@RestController
public class RestMemberController {
    private MemberDao memberDao;

    //..

    @GetMapping("/api/member/{id}")
    public ResponseEntity<Object> member(@PathVariable Long id) {
        Member member = memberDao.selectById(id);
        if(member == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("no member"));
        }
        return ResponseEntity.status(HttpStatus.OK).body(member);
    }
}
```
* Spring MVC는 반환형이 `ResponseEntity`이면 `ResponseEntity`의 body로 지정한 객체를 사용해서 변환을 처리한다.   
  위에서는 member를 body로 지정했는데, 이 경우 member 객체를 JSON으로 변환한다.
* `ResponseEntity`의 status로 지정한 값을 응답 상태 코드로 사용한다.

* `ResponseEntity`를 생성하는 기본 방법은 status와 body를 이용해서 상태 코드와 JSON으로 변환할 객체를 지정하는 것이다.
```java
ResponseEntity.status(상태 코드).body(객체);
```
* 상태 코드는 `HttpStatus` 열거 타입에 정의된 값을 이용해서 정의한다.   
  만약 200(OK)응답 코드와 body를 생성할 경우, 다음과 같이 `ok()` 메소드를 사용하여 생성할 수 도 있다.
```java
ResponseEntity.ok(member);
```
* 만약 body부분이 없다면, body를 지정하지 않고 바로 `build()` 메소드로 생성할 수 있다.
```java
ResponseEntity.status(HttpStatus.NOT_FOUND).build();
```
* 몸체 내용이 없는 경우, `status()` 메소드 대신에 아래의 관련 메소드들을 사용할 수 있다.
  * `noContent()` : 204
  * `badRequest()` : 400
  * `notFound()` : 404
```java
ResponseEntity.notFound().build();
```

* `newMember()` 메소드는 아래와 같이 201(Created) 상태 코드와 Location 헤더를 함께 전송했다.
```java
response.setHeader("Location", "/api/members" + newMemberId);
response.setStatus(HttpServletResponse.SC_CREATED);
```
* 위 코드를 `ResponseEntity`로 구현하면 다음과 같다.   
  `ResponseEntity.created()` 메소드에 Location 헤더로 전달할 URI를 설정하면 된다.
```java
@RestController
public class RestMemberController {

    //..

    @PostMapping("/api/members")
    public ResponseEntity<Object> newMember(@RequestBody @Valid RegisterRequest regReq) {

        try {
            Long newMemberId = registerService.regist(regReq);
            URI uri = URI.create("/api/members/" + newMemberId);
            return ResponseEntity.created(uri).build();
        } catch(MemberException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
```
<hr/>

<h3>@ExceptionHandler 적용 메소드에서 ResponseEntity로 응답하기</h3>

* 한 메소드에서 정상 응답과 에러 응답을 `ResponseBody`로 생성하면 코드가 중복될 수 있다.
```java
@GetMapping("/api/members/{id}")
public ResponseEntity<Object> member(@PathVariable Long id) {
    Member member = memberDao.selectById(id);
    if(member == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("no member"));
    }
    return ResponseEntity.ok(member);
}
```
* 위 코드는 member가 존재하지 않을 때 기본 HTML 에러 응답 대신에 JSON 응답을 제공하기 위해 `ResponseEntity`를 사용했다.   
  그런데 회원이 존재하지 않을 때 404 상태 코드를 응답해야하는 기능이 많다면, 에러 응답을 위해 `ResponseEntity`를   
  생성하는 코드가 여러 곳에 중복된다.

* 이럴 때 __@ExceptionHandler__ 를 적용한 메소드에서 에러 응답을 처리하도록 구현하면 중복을 없앨 수 있다.
```java
@GetMapping("/api/members/{id}")
public Member member(@PathVariable Long id) {
    Member member = memberDao.selectById(id);
    if(member == null) {
        throw new MemberNotFoundException();
    }
    return member;
}

@ExceptionHandler(MemberNotFoundException.class)
public ResponseEntity<Object> handleNoData() {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("no member"));
}
```

* __@RestControllerAdvice__ 어노테이션을 이용해서 에러 처리 코드를 별도의 클래스로 분리할 수 도 있다.   
  __@RestControllerAdvice__ 는 __@ControllerAdvice__ 와 동일한데, 차이점이라면 __@RestController__ 와 동일하게   
  응답을 JSON이나 XML 등의 형식으로 변환한다는 것이다.
```java
@RestControllerAdvice("controller")
public class ApiExceptionAdvice {

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<Object> handleNoData() {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("no member"));
}
}
```
* __@RestControllerAdvice__ 어노테이션을 사용하면 에러 처리 코드가 한 곳에 모여 효과적으로 에러 응답 관리가 가능하다.
<hr/>

<h3>@Valid 에러 결과를 JSON으로 응답하기</h3>

* 아래와 같이 기존 코드에서는 __@Valid__ 어노테이션을 붙인 커맨드 객체가 값 검증에 실패하면 400 상태코드를 응답한다.
```java
@PostMapping("/api/members")
public ResponseEntity<Object> newMember(@RequestBody @Valid RegisterRequest regReq) {

    try {
        Long newMemberId = registerService.regist(regReq);
        URI uri = URI.create("/api/members/" + newMemberId);
        return ResponseEntity.created(uri).build();
    } catch(MemberException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
}
```
* 문제는 `HttpServletResponse`를 이용해서 상태 코드를 응답할 때와 마찬가지로 HTTP 응답을 전송한다는 것이다.
* __@Valid__ 어노테이션을 이용한 검증에 실패했을 때 HTML 응답 대신에 JSON 형식 응답을 제공하고 싶다면   
  다음과 같이 `Errors` 타입 파라미터를 추가해서 직접 에러 응답을 생성하면 된다.

```java
@PostMapping("/api/members")
public ResponseEntity<Object> newMember(@RequestBody @Valid RegisterRequest regReq, Errors errors) {
    if(errors.hasErrors()) {
        String errorCodes = errors.getAllErrors();  // List<ObjectError> 반환
        errorCodes.stream().map(error -> error.getCodes()[0]).collect(Collectors.joining(","));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("errorCodes = " + errorCodes));
    }

    //..

}
```
* 위 코드는 `hasErrors()` 메소드를 이용해서 검증 에러가 존재하는지를 확인한다. 검증 에러가 존재하면 `getAllErrors()`   
  메소드로 모든 에러 정보를 구하고, 각 에러의 코드 값을 연결한 문자열을 생성해서 errorCodes 변수에 할당한다.
* __@RequestBody__ 어노테이션을 붙인 경우, __@Valid__ 어노테이션을 붙인 객체의 검증에 실패했을 때 `Errors` 타입의   
  파라미터가 존재하지 않으면 `MethodArgumentNotValidException`이 발생한다. 따라서 다음과 같이 __@ExceptionHandler__   
  어노테이션을 이용해서 검증 실패 시 에러 응답을 생성해도 된다.
```java
@RestControllerAdvice("controller")
public class ApiExceptonAdvice {

    //..

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleBindException(MethodArgumentNotValidException ex) {
        String errorCodes = ex.getBindingResult().getAllErrors()
            .stream().map(error -> error.getCodes()[0])
            .collect(Collectors.joining(","));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("errorCodes = " + errorCodes));
    }
}
```