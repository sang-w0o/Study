<h1>@Valid, @NotBlank, @ValueOfEnum, 어노테이션 제작</h1>

<h2>@Valid, @NotBlank 도입 이전의 문제점</h2>

* 사용자 등록을 위한 간단한 POST API가 있다고 하자.   
  사용자 등록을 위한 정보를 담는 Dto 클래스는 아래와 같다.
```java
@NoArgsConsturctor
@Getter
@Setter
public class UserRegisterDto {
    private String name;
    private String email;

    public User toEntity() {
        return User.builder().name(name).email(email).build();
    }
}
```

* 이때, 사용자 등록을 위한 정보(name, email)이 null 또는 빈 값('')으로 온다면 개인적으로 생각했을 때,   
  클라이언트에서 필요한 값을 보내지 않은 것이므로 400(BAD REQUEST)가 맞다고 생각되었다.

* 따라서 기존에는 서비스 코드에서 아래와 같이 잘못된 값에 대한 처리를 했다.
```java
public void saveUser(UserRegisterDto dto) {
    if(dto.getName() == null || dto.getName() == "" || dto.getEmail() == null || dto.getEmail() == '') {
        throw new BadRequestException();
    } else {
        usersRepository.save(dto.toEntity());
    }
}
```

* 하지만 위와 같이 Request Body의 값 검증을 하기 위해 변수를 일일히 체크하는 것은 위 코드는 간단하지만,   
  Dto에 요구되는 데이터가 더 많아질 수록 코드가 길어지기 마련이다.

* 이때, Spring에서 제공하는 `@Valid`와 `@NotBlank`를 알게 되었다.
<hr/>

<h2>@Valid, @NotBlank</h2>

* 먼저, `@NotBlank` 어노테이션은 아래와 같이 작성되어 있다.
```java
package javax.validation.constraints;

@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
public @interface NotBlank {

	String message() default "{javax.validation.constraints.NotBlank.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		NotBlank[] value();
	}
}
```

* 위 어노테이션이 적용된 클래스, 필드, 메소드 등에 대해 Spring은 Bean Validation을 수행한다.   
  Bean Validation을 수행하게 하도록 하기 위한 어노테이션은 여러 개가 있으며, `@NotBlank`는 그 중 하나이다.

* `@NotBlank`는 어노테이션이 적용된 것이 __null이 아니며 trimmed value의 길이가 0 이상__ 이어야 한다는 제약을 건다.

* 다음으로  `@Valid` 어노테이션은 아래와 같이 작성되어 있다.
```java
package javax.validation;

@Target({ METHOD, FIELD, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
public @interface Valid {
}
```

* Spring이 `@Valid`가 적용된 인자를 보게 되면 자동으로 `Default JSR 380`을 구현한 객체와 매핑하며,   
  이는 곧 `Hibernate Validator`로 이어지고, 인자값을 주어진 제약에 맞게 검증한다.
<hr/>

<h2>@Valid, @NotBlank의 도입 이후 코드</h2>

* 위와 같은 상황에 이 두 어노테이션을 적용하면, 아래와 같이 코드가 간결해진다.
```java
// 사용자 정보 등록을 위한 DTO 클래스
@NoArgsConsturctor
@Getter
@Setter
public class UserRegisterDto {

    @NotBlank(message = "name must not be null and length must be greater than zero.")
    private String name;

    @NotBlank(message = "email must not be null and length must be greater than zero.")
    private String email;

    public User toEntity() {
        return User.builder().name(name).email(email).build();
    }
}

// 사용자 정보를 등록하는 서비스 코드
public class UsersService {

    public void saveUser(UserRegisterDto dto) {
        usersRepository.save(dto.toEntity());
    }
}

// 서비스 코드를 호출하는 부분
public class UsersApiController {

    @PostMapping("/users/save")
    public void saveUser(@Valid @RequestBody UserRegisterDto dto) {
        usersService.saveUser(dto);
    }
}
```

* 위와 같이 코드가 매우 간결해졌음을 알 수 있다.

* 추가로, 만약 Request body가 validation의 제약에 걸린다면, 해당 request는 400(BAD REQUEST)를 응답받는다.
<hr/>

<a href="https://www.baeldung.com/spring-boot-bean-validation">참고 링크</a>