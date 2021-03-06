<h1>Spring Security Authentication 지원 클래스</h1>

# SecurityContext

- `SecurityContext`는 `SecurityContextHolder`에 사용되며, `Authentcation` 객체를 가진다.

<hr/>

# Authentication

- `Authentication` 클래스는 아래의 2개 목적으로 Spring Security에서 사용된다.

  1. `AuthenticationManager`에게 사용자가 제공한 인증을 위한 정보(credentials)들을 제공한다.  
     이 과정에서 `Authentication#isAuthenticated()`는 false를 반환한다.
  2. 현재 인증되어 있는 사용자에 대한 정보를 담는다. **현재 인증된 사용자** 에 대한 정보를 담은 `Authentication`객체는  
     `SecurityContext` 클래스로부터 받아올 수 있다.

- `Authentication` 객체는 아래의 정보들을 가진다.
  - `principal` : 사용자를 식별한다. username/password로 인증하는 경우, `UserDetails`의 인스턴스가 이 필드에 사용된다.
  - `credentials` : 보통 비밀번호를 담는다. 대다수의 경우, 인증 후 정보 유출을 방지하기 위해 이 필드는 초기화된다.
  - `authorities` : 보통 사용자가 가진 권한을 `GrantedAuthorities`객체에 담는다.  
     때로는 역할(role) 또는 권한 범위(scope)에 대한 정보도 갖는다.

<hr/>

# GrantedAuthority

- `GrantedAuthority`는 사용자에게 부여된 high-level 권한들을 가진다. 때로는 역할(role) 또는 범위(scope)를 가지기도 한다.

- `GrantedAuthority`객체는 `Authentication#getAuthorities()` 메소드를 통해 가져올 수 있다.  
   이 메소드는 `GrantedAuthority`객체들을 담은 `Collection` 객체를 반환한다.

- `GrantedAuthority`는 principal(사용자 식별)에 부여된 권한에 대한 정보를 가진다.  
  이 권한은 대부분 역할(role)으로, `ROLE_ADMINISTRATOR`, `ROLE_HR_SUPERVISOR` 등의 값을 가진다.  
  이 역할들은 추후에 웹 인증, HTTP 메소드 인증, 도메인 리소스 인증 등에 활용된다.  
  Spring Security의 다른 부분에서는 이 역할 권한들을 해석하며, 이들이 제공될 것이라고 예상한다.  
  username/password 방식의 인증을 사용할 때, `GrantedAuthority`객체들은 `UserDetailsService`로부터 받아올 수 있다.

- 보통 `GrantedAuthority` 객체들은 애플리케이션 전반에 걸쳐서 사용된다. 이 객체는 특정 도메인 리소스에 한정되어 있지 않다.  
  특정 리로스에 대해서만 이 객체를 사용하는 것은 메모리 초과 등의 문제를 일으킬 수 있다.

<hr/>
