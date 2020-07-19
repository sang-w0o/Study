Spring Security, OAuth 2.0
======

* `Spring Security`는 막강한 인증(Authentication)과 인가(Authorization)의 기능을 가진 프레임워크이다.   
  이는 사실상 Spring 기반의 Application에서는 보안을 위한 표준과 동일하게 취급된다.   
  Spring은 Interceptor, Filter 등을 기반으로 보안 기능을 구현하는 것 보다 Spring-Security를 활용할 것을 권장한다.
<hr/>

<h2>Spring Security & OAuth2 Client</h2>

* 많은 서비스에서 로그인 기능을 id/password 방식보다는 구글, 페이스북 등의 소셜 로그인 기능을 활용한다.   
  이를 활용하는 이유는 로그인 구현을 소셜 서비스에 맡기고, 서비스 개발에 집중할 수 있기 때문이다.

<hr/>

<h2>Google Service 등록</h2>

* 먼저 Google Service에 신규 서비스를 생성한다. 여기서 발급한 인증 정보(clientId, clientServer)를 통해   
  로그인 기능과 소셜 서비스 기능을 사용할 수 있으니 무조건 발급받고 시작해야 한다.
* http://console.cloud.google.com
* 위 사이트에서 OAuth Client ID 생성까지 마친 후 client-id와 client-secret 코드를 아래와 같이 `src/main/resources`의 하위에   
  `application-oauth.properties` 파일을 생성하고, 입력하자.
```properties
spring.security.oauth2.client.registration.google.client-id=clientid값
spring.security.oauth2.client.registration.google.client-secret=clientsecret값
spring.security.oauth2.client.registeration.google.scope=profile,email
```
* Spring-boot에서는 properties 파일의 이름을 `application-xxx.properties`로 만들면 xxx라는 이름의 `profile`이   
  생성되어 이를 통해 관리할 수 있다. 즉 `profile=xxx`라는 식으로 호출하면 해당 properties의 설정을 가져올 수 있다.   
  호출하는 방식은 여러 방식이 있지만 여기서는 Spring-boot의 기본 설정 파일인 `application.properties`에서   
  `application-oauth.properties`를 포함하도록 구성하자. `application.properties`파일에 아래 코드를 추가하자.
```properties
spring.profiles.include=oauth
```
<hr/>

<h2>구글 로그인 연동하기</h2>

* Google의 로그인 인증정보를 발급 받았으니 프로젝트의 구현을 진행해보자.   
  먼저 사용자 정보를 담당할 도메인인 `User`클래스를 생성하자. 위치 : `domain` 하위의 패키지
```java
package com.sangwoo.board.domain.user;

import com.sangwoo.board.domain.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
public class User extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable=false)
    private String name;
    
    @Column(nullable = false)
    private String email;
    
    @Column
    private String picture;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    @Builder
    public User(String name, String email, String picture, Role role) {
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.role = role;
    }
    
    public User update(String name, String picture) {
        this.name = name;
        this.picture = picture;
        return this;
    }
    
    public String getRoleKey() {
        return this.role.getKey();
    }
}
```
* `@Enumerated(EnumType.STRING)`은 JPA로 DB로 저장할 때 Enum값을 어떤 형태로 저장할지를 결정한다.   
  기본적으로는 int로 된 숫자가 저장되는데, 숫자로 저장되면 db 확인 시 그 값이 어떤 의미를 가지는지 알기 힘들다.   
  따라서 문자열(EnumType.STRING)로 저장될 수 있도록 선언했다.

* 다음으로는 각 사용자의 권한을 관리할 `Enum` 클래스 `Role` 을 생성하자.
```java
package com.sangwoo.board.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    
    GUEST("ROLE_GUEST", "손님"),
    USER("ROLE_USER", "일반 사용자");
    
    private final String key;
    private final String title;
}
```
* Spring Security에서는 권한 코드에 항상 __ROLE_ 이 앞에 있어야만__ 한다.   
  따라서 코드별 key값을 ROLE_GUEST, ROLE_USER로 지정했다.

* 마지막으로 `User`의 CRUD를 책임질 `UserRepository`를 생성하자.
```java
package com.sangwoo.board.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```
* 위 인터페이스의 `findByEmail()` 메소드는 소셜 로그인으로 반환되는 값 중 email을 통해 이미 생성된 사용자인지,   
  처음 가입하는 사용자인지를 판단하기 위한 메소드이다.
<hr/>

<h2>Spring Security 설정</h2>

* 먼저 `build.gradle` 파일에 Spring-Security 관련 의존성을 하나 추가하자.
```gradle
compile('org.springframework.boot:spring-boot-starter-oauth2-client')
```
* 위 의존성은 소셜 로그인 등 클라이언트 입장에서 소셜 기능 구현 시 필요한 의존성이다.   
  또한 `spring-security-oauth2-client`와 `spring-security-oauth2-jose`를 기본으로 관리해준다.

* 다음으로는 OAuth 라이브러리를 이용한 소셜 로그인 설정 코드를 작성하자.   
  `config.auth` 패키지를 생성하고, 앞으로 __security 관련 모든 클래스는 이 곳에 보관__ 한다.

* 위에서 생성한 패키지에 `SecurityConfig` 클래스를 생성하고, 아래와 같이 작성한다.
```java
FROM p.180 Code
```