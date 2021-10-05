# Spring Security

- 이번에는 애플리케이션을 실제 상용 환경에 배포하기 전에 반드시 갖춰야할 항목,  
  즉 **보안(Security)** 에 대해 보자.

- 애플리케이션은 이용자 접근을 제어할 수 있는 인증과 엄격한 권한 제어가 적용되기  
  전까지는 그저 장난감에 지나지 않는다.

- 여기서 다룰 내용은 아래와 같다.

  - 다양한 사용자 정보 저장소를 사용하는 Spring Security 설정
  - HTTP endpoint에 route 기반 보안 설정 적용
  - Reactive endpoint에 메소드 수준 보안 적용
  - 권한 검사를 위한 Spring Security Context 연동

## Spring Security 시작하기

- Spring Security를 사용하는 가장 간단한 방법은 Spring Boot application에  
  적용해보는 것이다. 아래 의존성을 추가해 Spring Boot가 어떤 부분을 자동 구성해주는지  
  살펴보자.

```gradle
//..

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation("org.springframework.security:spring-security-test")
}
```

- `spring-boot-starter-security`와 함께 `spring-security-test`도 함께 추가했다.  
  `spring-security-test`를 사용하면 Spring Security로 구성한 보안 기능을 주요 관심사로  
  하는 테스트 케이스를 아주 쉽게 작성할 수 있다.

- Spring Security는 아래와 같은 다중 계층 방식으로 광범위한 보안을 적용한다.

  - 여러 가지 filter가 생성되고, 적절한 순서로 등록된다.
  - 웹 페이지에 다양한 지시어(directive)가 추가된다.
    - 바람직하지 않은 정보가 브라우저 캐시에 유입되는 것 방지
    - 클릭재킹(Clickjacking), 세션 고정 공격(Session fixation), XSS Projections 등  
      보안 방어
    - 서버 응답에 적절한 보안 헤더 추가
    - CSRF 방지 활성화

- 결국 Spring Boot Application을 사용하면 Spring Security도 쉽게 적용할 수 있고,  
  Spring Security는 위처럼 다양한 공격으로부터 방어하는 보안 작용을 활성화한다.

- 하지만 아무런 추가 설정 없는 기본 Spring Security 적용은 실제 운영환경에 배포하기엔  
  턱없이 부족하다. 그래서 실제 운영 애플리케이션에는 여러 가지 정책을 추가해야 한다.

<hr/>

## 설정해보기

- 실제 운영 환경에 Spring Security를 적용하려면 확장성을 고려할 필요가 있고, 시스템에  
  접근할 모든 사용자를 등록할 수 있는 다른 방법도 있다. 하지만 보안팀에서 그 저장소를  
  애플리케이션과 별도로 분리된 도구로 관리하길 원한다면 어떻게 해야할까?

- 해답은 간단한데, 지금까지 사용해왔던 Reactive Repository와 Spring Security를  
  연결해줄 약간의 코드를 작성하면 된다.

- 보안팀의 사용자 관리 도구가 MongoDB 기반이라 가정해보자. 물론 MongoDB가 아닌 다른  
  저장소일 수 있지만, 사용자 식별 번호와 비밀번호, 역할(role)이 저장된다는 근본적인  
  개념은 어떤 저장소를 사용하더라도 같다.

- Spring Security는 사용자 정보를 하드코딩해서 저장할 수 있는 여러 가지 방법을 제공하지만,  
  데이터 저장소를 연결해서 사용하는 편이 다방면에서 낫다. 아이디, 비밀번호, 역할을 저장한다는  
  기본 개념을 구체적으로 구현하는 것도 전혀 어렵지 않다.

- 이제 사용자 정보 관리 기능을 만들어보자.  
  아래는 우선 사용자를 정의한 `User` 타입이다.

```kt
class User {

    @Id
    var id: String = ""

    val name: String

    val password: String

    val roles: List<String>

    constructor(id: String, name: String, password: String, roles: List<String>) {
        this.id = id
        this.name = name
        this.password = password
        this.roles = roles
    }

    constructor(name: String, password: String, roles: List<String>) {
        this.name = name
        this.password = password
        this.roles = roles
    }
}
```

- 그리고 이 `User` 데이터에 접근하기 위한 Spring Data Repository를 정의하자.

```kt
@Repository
interface UserRepository : CrudRepository<User, String> {
    fun findByName(name: String): Mono<User>
}
```

- 다음으로 Spring Security 설정 정보를 담는 `SecurityConfig` 클래스를 만들자.

- 이 클래스에서는 `UserRepository`를 사용해 `User`를 조회하고, Spring Security의 User로  
  변환할 수 있는 bean을 사용해야 한다. 사용자 조회를 위해 개발자가 만든 bean을 Spring Security가  
  Reactive Application 안에서 찾아 사용하게 하려면 `ReactiveUserDetailsService`를  
  구현해야 한다.

```kt
@Configuration
class SecurityConfig {

    @Bean
    fun userDetailsService(repository: UserRepository): ReactiveUserDetailsService {
        return ReactiveUserDetailsService { username: String ->
            repository.findByName(username)
                .map { user ->
                    User.withDefaultPasswordEncoder()
                        .username(user.name)
                        .password(user.password)
                        .authorities(*user.roles.toTypedArray())
                        .build()
                }
        }
    }
}
```

- 위 코드의 `userDetailsService()` Spring Bean은 `ReactiveUserDetailsService` 타입을  
  반환하는데, 이 인터페이스에는 `Mono<UserDetails>`를 반환하는 `findByusername()` 메소드  
  하나밖에 없기에 람다식을 사용해 구현체를 만들어서 반환하도록 했다.

- 우선 이렇게 하고 애플리케이션을 실행해보면, 아이디, 비밀번호 방식의 로그인을 할 수 있다.

- 그럼 정확히 어떤 기능이 활성화된 것일까? Spring Boot는 Spring Security가 제공하는  
  `@EnableWebFluxSecurity` 어노테이션을 적용할지 말지 결정한다. `@EnableWebFluxSecurity`가  
  적용되면 Spring Security는 기본적으로 아래의 기능들을 활성화한다.

  - HTTP BASIC을 활성화해서 cURL 같은 도구로도 계정명, 비밀번호 값 전달 가능
  - HTTP FORM을 활성화해 로그인되지 않은 사용자는 브라우저의 기본 로그인 팝업 창 대신  
    Spring Security가 제공하는 로그인 페이지로 redirect된다.
  - 사용자가 로그인에 성공해 인증이 완료되면 애플리케이션의 **모든 자원에 접근 가능**하다. 이는  
    인증만 받으면 애플리케이션 자원에 접근하기 위한 추가적인 허가가 필요하지 않음을 의미한다.

- 이 정도 설정으로는 절대 충분한 보안 조치가 적용되었다 할 수 없다.  
  인증된 모든 사용자에게 모든 자원에 대한 접근을 허용하는 것은 바람직하지 않다.  
  인증된 사용자가 접근할 수 있는 자원에 제약을 두는 것이 안전하며, 사용자가 볼 수 있도록  
  허가받은 화면만 보여줘야 한다. 이는 사용자가 볼 수 있는 링크를 사용자마다 다르게해서,  
  볼 수 없는 페이지에 대한 링크는 제공되지도 않게 해야함을 의미한다.

<hr/>
