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

<h2>Enum에 대한 제약 걸기</h2>

* 위의 사용자에 사용자의 종류(일반 사용자(USER), 관리자(ADMIN)) 분류가 추가된다고 하자.

* 우선 사용자를 위한 Entity 클래스는 아래와 같다.
```java
@Entity
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserType type;
}

// 아래는 사용자 종류를 담는 UserType 클래스이다.
public enum UserType {
    USER, ADMIN
}
```

* 사용자 정보 등록을 위한 Request Body의 예시를 보자.
```json
{
    "name": "user name",
    "email": "test@test.com",
    "type": "USER"
}
```

* 위 Request Body는 올바르다. 하지만 내가 고민했던 것은 "만약 type이 `USER`, `ADMIN`이 아닌 다른 값이 온다면,   
  기존에는 500(Internal Server Error)이 오며 실패하지만, 이는 400(Bad Request)가 더 어울리지 않을까?" 라는 점이었다.   
  __물론 내 생각이 틀렸을 수도 있다.__

* 이를 해결하는 가장 간단한 방법은 Request Body내의 type을 if문으로 일일히 검증하는 것인데, 위의 `@Valid`와 `@NotBlank`를   
  도입한 이유와 마찬가지로 이러한 열거형 변수들이 많아질 수록 코드는 길고 복잡해질 것이다.

* 이를 해결하기 위해 직접 어노테이션을 만들어 도입하도록 결정했다.
  
* 먼저, Validator를 이용해 Spring에서 제약을 직접 확인하도록 하기 위해 아래의 클래스를 작성한다.
```java
public class ValueOfEnumValidator implements ConstraintValidator<ValueOfEnum, CharSequence> {
    private List<String> acceptedValues;

    @Override
    public void initialize(ValueOfEnum constraintAnnotation) {
        acceptedValues = Stream.of(constraintAnnotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if(value == null) return true;
        if(!acceptedValues.contains(value.toString())) {
            throw new BadRequestException();
        } else {
            return true;
        }
    }
}
```

* 위의 `ValueOfEnumValidator`가 구현하는 인터페이스인 `ConstraintValidator`는 아래와 같다.
```java
public interface ConstraintValidator<A extends Annotation, T> {

	default void initialize(A constraintAnnotation) {
	}

	boolean isValid(T value, ConstraintValidatorContext context);
}
```

* 공식 문서에 따르면, `A`와 `T` 타입에 대한 설명은 아래와 같다.
  * `T` must resolve to a non parameterized type or generic parameters of T must be unbounded wildcard types.
  * `A` : The annotation type handled by an implementation.
  * `T` : The target type supported by an implementation.
  
* 즉, `ConstraintValidator#isValid()`를 `T` 타입인 `CharSequence`에 들어온 값을 검증하도록 오버라이딩 한 것이다.   
  실제 메소드를 보면 `value: CharSequence`가 enum형에 있는지를 확인하고 있다.

* 다음으로는 `@ValueOfEnum`을 작성한다.
```java
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ValueOfEnumValidator.class)
public @interface ValueOfEnum {
    Class<? extends Enum<?>> enumClass();
    String message() default "Must be any of enum {enumClass}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

* 위에서 사용한 `@Constraint` 어노테이션의 validatedBy 의 값으로 전달한 클래스는 `ConstraintValidator` 타입의 클래스여야 한다.   
  아래는 `@Constraint`의 코드이다.
```java
package javax.validation;

/**
 * Marks an annotation as being a Jakarta Bean Validation constraint.
 * <p>
 * A given constraint annotation must be annotated by a {@code @Constraint}
 * annotation which refers to its list of constraint validation implementations.
 * <p>
 * Each constraint annotation must host the following attributes:
 * <ul>
 *     <li>{@code String message() default [...];} which should default to an error
 *     message key made of the fully-qualified class name of the constraint followed by
 *     {@code .message}. For example {@code "{com.acme.constraints.NotSafe.message}"}</li>
 *     <li>{@code Class<?>[] groups() default {};} for user to customize the targeted
 *     groups</li>
 *     <li>{@code Class<? extends Payload>[] payload() default {};} for
 *     extensibility purposes</li>
 * </ul>
 * <p>
 * When building a constraint that is both generic and cross-parameter, the constraint
 * annotation must host the {@code validationAppliesTo()} property.
 * A constraint is generic if it targets the annotated element and is cross-parameter if
 * it targets the array of parameters of a method or constructor.
 * <pre>
 *     ConstraintTarget validationAppliesTo() default ConstraintTarget.IMPLICIT;
 * </pre>
 * This property allows the constraint user to choose whether the constraint
 * targets the return type of the executable or its array of parameters.
 *
 * A constraint is both generic and cross-parameter if
 * <ul>
 *     <li>two kinds of {@code ConstraintValidator}s are attached to the
 *     constraint, one targeting {@link ValidationTarget#ANNOTATED_ELEMENT}
 *     and one targeting {@link ValidationTarget#PARAMETERS},</li>
 *     <li>or if a {@code ConstraintValidator} targets both
 *     {@code ANNOTATED_ELEMENT} and {@code PARAMETERS}.</li>
 * </ul>
 *
 * Such dual constraints are rare. See {@link SupportedValidationTarget} for more info.
 * <p>
 * Here is an example of constraint definition:
 * <pre>
 * &#64;Documented
 * &#64;Constraint(validatedBy = OrderNumberValidator.class)
 * &#64;Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
 * &#64;Retention(RUNTIME)
 * public &#64;interface OrderNumber {
 *     String message() default "{com.acme.constraint.OrderNumber.message}";
 *     Class&lt;?&gt;[] groups() default {};
 *     Class&lt;? extends Payload&gt;[] payload() default {};
 * }
 * </pre>
 *
 * @author Emmanuel Bernard
 * @author Gavin King
 * @author Hardy Ferentschik
 */
@Documented
@Target({ ANNOTATION_TYPE })
@Retention(RUNTIME)
public @interface Constraint {

	/**
	 * {@link ConstraintValidator} classes implementing the constraint. The given classes
	 * must reference distinct target types for a given {@link ValidationTarget}. If two
	 * {@code ConstraintValidator}s refer to the same type, an exception will occur.
	 * <p>
	 * At most one {@code ConstraintValidator} targeting the array of parameters of
	 * methods or constructors (aka cross-parameter) is accepted. If two or more
	 * are present, an exception will occur.
	 *
	 * @return array of {@code ConstraintValidator} classes implementing the constraint
	 */
	Class<? extends ConstraintValidator<?, ?>>[] validatedBy();
}
```

* 위의 JavaDoc을 보면, `Constraint#message()`는 default 에러 메시지를 지정할 수 있도록 한다.   
  `Contstraint#groups()`는 사용자가 어노테이션이 적용될 target group을 수정할 수 있도록 해주며,   
  `Constraint#payload()`는 확장성을 위한 메소드라고 쓰여져 있다.

* 이제 `@ValueOfEnum`을 적용해보자.
```java
// 사용자 정보 등록을 위한 Dto
@NoArgsConsturctor
@Getter
@Setter
public class UserRegisterDto {

    @NotBlank(message = "name must not be null and length must be greater than zero.")
    private String name;

    @NotBlank(message = "email must not be null and length must be greater than zero.")
    private String email;

    @NotBlank(message = "type must not be null and length must be greater than zero.")
    @ValueOfEnum(enumClass = UserType.class)
    private String type;

    public User toEntity() {
        return User.builder().name(name).email(email).build();
    }
}
```

* 이제 Request Body에 type으로 `USER`, `ADMIN`이 아닌 다른 값이 오게 된다면,   
  `ValueOfEnumValidator#isValid()`가 `BadRequestException`을 던져 응답값으로 500이 아닌 400이 오게 된다.

* 참고로 `BadRequestException`은 내가 작성한 것인데, 아래와 같다.
```java
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException() {
        this("Bad Request Exception.");
    }
}
```

<a href="https://www.baeldung.com/spring-boot-bean-validation">참고 링크1</a>
<a href="https://www.baeldung.com/javax-validations-enums">참고 링크2</a>