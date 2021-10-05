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
