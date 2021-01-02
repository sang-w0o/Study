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

