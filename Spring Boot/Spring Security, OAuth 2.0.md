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
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    private final CustomOAuth2UserService customOAuth2UserService;
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().headers().frameOptions().disable().and()
                .authorizeRequests()
                .antMatchers("/", "/css/**", "/images/**", "/js/**", "/h2-console/**")
                .permitAll().antMatchers("/api/v1/**").hasRole(Role.USER.name())
                .anyRequest().authenticated().and()
                .logout().logoutSuccessUrl("/")
                .and().oauth2Login().userInfoEndpoint().userService(customOAuth2UserService);
    }
}
```
* __@EnableWebSecurity__ : Spring Security 설정들을 활성화시킨다.
* `csrf().disable().headers().frameOptions().disable()` : h2-console화면을 사용하기 위해 해당 option들을 disable 한다.
* `authorizeRequests()`: URL별 권한 관리를 설정하는 option의 시작점이다. 이 메소드가 선언되어야만 뒤에   
  `antMatchers()` 옵션을 사용할 수 있다.
* `antMatchers()` : 권한 관리 대상을 지정하는 option이다. URL, HTTP Method별로 관리가 가능하다.   
  "/" 등 지정된 URL들은 `permitAll()` 옵션을 통해 전체 열람 권한을 부여했다.   
  "/api/v1/**" 주소를 가진 API는 USER권한을 가진 사람만 가능하도록 설정했다.
* `anyRequest()` : 설정된 값들 이외 나머지의 URL들을 나타낸다. 위에서는 바로 다음에 `authenticated()` option을 추가하여   
  나머지 URL들은 모두 인증된 사용자들에게만 허용하게 했다. 인증된 사용자는 로그인한 사용자들을 의미한다.
* `logout().logoutSuccessUrl("/")` : 로그아웃 기능에 대한 여러 설정의 진입점으로, 로그아웃 성공 시 "/"의 주소로 이동한다.
* `oauth2Login()` : OAuth2 로그인 기능에 대한 여러 설정의 진입점이다.
* `userInfoEndpoint()` : OAuth2 로그인 성공 이후 사용자 정보를 가져올 때의 설정들을 담당한다.
* `userService()` : 소셜 로그인 성공 시 후속 조치를 진행할 `UserService`인터페이스의 구현체를 등록한다.   
  리소스 서버, 즉 소셜 서비스에서 사용자 정보를 가져온 상태에서 추가로 진행하고자 하는 기능을 명시할 수 있다.

* 이제 `CustomOAuth2UserService` 클래스를 작성하자. 이 클래스는 구글 로그인 이후 가져온 사용자의 정보(email, name, phone)   
  들을 기반으로 가입 및 정보 수정, 세션 저장 등의 기능을 지원한다.
```java
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    
    private final UserRepository userRepository;
    private final HttpSession httpSession;
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();
        
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());
        
        User user = saveOrUpdate(attributes);
        
        httpSession.setAttribute("user", new SessionUser(user));
        
        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey())),
                attributes.getAttributes(), attributes.getNameAttributeKey());
    }
    
    private User saveOrUpdate(OAuthAttributes attributes) {
        User user = userRepository.findByEmail(attributes.getEmail())
                .map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
                .orElse(attributes.toEntity());
        return userRepository.save(user);
    }
}
```
* `registrationId` : 현재 로그인 진행중인 서비스를 구분하는 코드이다. 현재는 Google만 사용하므로 불필요하지만,   
  이후 네이버 등 다른 로그인 연동 시에 어떤 서비스에 로그인하는지를 구분하기 위해 사용한다.
* `userNameAttributeName` : OAuth2 로그인 진행 시 key가 되는 필드값을 의미한다. PK와 같은 의미이다.   
  Google의 경우 기본적으로 코드를 지원하지만, 네이버, 카카오 등은 지원하지 않는다. Google의 기본 코드는 "sub" 이다.   
  이 필드는 이후 네이버 로그인과 구글 로그인을 동시에 지원할 때 사용된다.
* `OAuthAttributes` : `OAuth2UserService`를 통해 가져온 `OAuth2User`의 attribute를 담을 클래스이다.
* `SessionUser` : 세션에 사용자 정보를 저장하기 위한 Dto 클래스이다.
* `saveOrUpdate()` 메소드에는 구글 사용자 정보가 업데이트 되었을 때를 대비하여 update 기능도 구현했다.   
  사용자의 이름이나 picture가 변경되면 User Entity에도 반영된다.

* 다음으로는 `OAuthAttributes` 클래스를 작성하자.
```java
@Getter
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String picture;
    
    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email, String picture) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.picture = picture;
    }
    
    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder().name((String)attributes.get("name"))
                .email((String)attributes.get("email")).picture((String)attributes.get("picture"))
                .attributes(attributes).nameAttributeKey(userNameAttributeName).build();
    }
    
    public User toEntity() {
        return User.builder().name(name).email(email).picture(picture).role(Role.GUEST).build();
    }
}
```
* `of()` : `OAuth2User`에서 반환하는 사용자 정보는 `Map`형식이기 때문에 값 하나하나를 반환해야 한다.
* `toEntity()` : `User` Entity를 생성한다. `OAuthAttributes`에서 Entity를 생성하는 시점은 처음 가입할 때 이다.   
  가입할 때의 기본 권한을 GUEST로 주기 위해서 `role()` 빌더값에는 Role.GUEST를 지정했다.

* 다음으로는 `SessionUser` 클래스를 생성하자.
```java
@Getter
public class SessionUser implements Serializable {
    private String name;
    private String email;
    private String picture;
    
    public SessionUser(User user) {
        this.name = user.getName();
        this.email = user.getEmail();
        this.picture = user.getPicture();
    }
}
```
* `SessionUser` 클래스는 `HttpSession` 객체에 저장할 정보이므로 __인증된 사용자 정보만 필요__ 하기 때문에 name, email,   
  picture만 저장하도록 했다.
* `User`클래스를 `Serializable`을 구현하게 하지 않고, 따로 `SessionUser` 클래스를 작성하여 세션에 저장한 이유는,   
  `User`클래스는 Entity이기 때문이다. Entity class에는 언제 다른 Entity와 관계가 형성될지 모른다. 만약 직렬화 대상에   
  자식들까지 포함되면 성능상의 이슈 및 부수 효과가 발생할 확률이 높다. 따라서 직렬화 기능을 가진 Session Dto를 만든 것이다.   
  이는 이후 운영 및 유지보수 때 많은 도움이 된다.
<hr/>

<h3>Login Test</h3>

* 이제 Spring-Security가 잘 적용됐는지를 확인하기 위해 화면에 로그인 버튼을 추가해보자.
* 아래는 수정된 `index.mustache` 의 코드이다.
```mustache
{{>layout/header}}

<h1>Web Service using Spring-Boot.</h1>
<div class="col-md-12">
    <!-- 로그인 기능 영역-->
    <div class="row">
        <div class="col-md-6">
            <a href="/posts/save" role="button" class="btn btn-primary">글 등록</a>
            {{#userName}}
                Logged in as : <span id="user">{{userName}}</span>
                <a href="/logout" class="btn btn-info active" role="button">Logout</a>
            {{/userName}}
            {{^userName}}
                <a href="/oauth2/authorization/google" class="btn btn-success active" role="button">Google Login</a>
            {{/userName}}
        </div>
    </div>
    <br/>

    <!-- 목록 출력 영역 -->
    <table class="table table-horizontal table-bordered">
        <thead class="thead-strong">
            <tr>
                <th>게시글 번호</th>
                <th>제목</th>
                <th>작성자</th>
                <th>최종 수정일</th>
            </tr>
        </thead>
        <tbody id="tbody">
            {{#posts}}
                <tr>
                    <td>{{id}}</td>
                    <td><a href="/posts/update/{{id}}">{{title}}</a></td>
                    <td>{{author}}</td>
                    <td>{{modifiedDate}}</td>
                </tr>
            {{/posts}}
        </tbody>
    </table>
</div>

{{>layout/footer}}
```
* `{{#userName}}` : Mustache는 다른 언어들에 있는 if문을 제공하지 않는다. 오로지 true/false만 판별할 뿐이다.   
  따라서 mustache에는 항상 최종값을 넘겨줘야한다. 위 코드에서는 userName이 있다면 userName을 노출시키도록 한 것이다.
* `a href="/logout"` : 이 URL은 Spring Security에서 기본적으로 제공하는 로그아웃 URL 이다. 즉, 개발자가 별도로   
  위 URL에 해당하는 컨트롤러를 만들 필요가 없다.
* `{{^userName}}` : mustache에서 해당 값이 존재하지 않는 경우에는 `^` 기호를 사용한다. 위 코드에서는 userName값이   
  없다면 로그인 버튼을 노출시켰다.
* `a href="/oauth2/authorization/google` : Spring Security에서 기본적으로 제공하는 로그인 URL 이다.   
  로그아웃 URL과 마찬가지로 개발자가 별도의 컨트롤러를 생성할 필요가 없다.

* 위의 `index.mustache`에서 userName을 session에서 가져와 사용하므로, `IndexController`에 userName을 `Model`객체에   
  추가하는 코드를 작성해보자.
```java
@RequiredArgsConstructor
@Controller
public class IndexController {

    private final PostsService postsService;
    private final HttpSession httpSession;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("posts", postsService.findAllDesc());

        SessionUser user = (SessionUser)httpSession.getAttribute("user");
        if(user != null) {
            model.addAttribute("userName", user.getName());
        }
        return "index";
    }

    // 생략
}
```
