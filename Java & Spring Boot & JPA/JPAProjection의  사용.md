<h1>JPA Projection의 사용</h1>

* Spring Boot에서 JPA로 데이터베이스(MariaDB, MySql)와 연동을 하고, 아래 상황을 가정해보자.

  1. `users` 테이블에는 id, email, password, name, phoneNumber, telephoneNumber,   
     created_at, last_modified_at 컬럼이 있다.
  2. 특정 API를 통해 id를 제공하면 email과 name 컬럼만 조회하고 싶다.

<h2>프로젝트 Setup</h2>

* 기본적으로 프로젝트를 setup 해보자.   
  우선 도메인 클래스인 `User`이다.
```java
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, name = "userId")
    private Integer id;

    @Column(nullable = false, length = 400)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 30, unique = true)
    private String email;

    @Column(length = 40)
    private String phoneNumber;
}
```

* 다음으로는 Repository Layer에서 작동하는 레포지토리 인터페이스이다.
```java
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<User, Integer> {
    Optional<User> findById(Integer userId);
}
```

* 그리고 우리가 원하는 정보인 email, name에 대한 정보를 가진 DTO 클래스이다.
```java
@NoArgsConstructor
@Getter
@Setter
public class UserSimpleInfoResponseDto {

    private Integer userId;
    private String name;
    private String email;

    public UserSimpleInfoResponseDto(User user) {
        this.userId = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
    }
}
```

* 마지막으로 서비스 코드와 컨트롤러이다.
```java
// UserService.java
@RequiredArgsConstructor
@Service
public class UserService {

    private final UsersRepository usersRepository;

    @Transactional(readOnly = true)
    public UserSimpleInfoResponseDto getUserInfo(Integer userId) {
        User user = usersRepository.findById(userId).orElseThrow(UserIdNotFoundException::new);
        return new UserSimpleInfoResponseDto(user);
    }
}

// UserApiController.java
@RequiredArgsConstructor
@RestController
public class UserApiController {

    private final UserService userService;

    @GetMapping("/users/{userId}")
    public UserSimpleInfoResponseDto getUserInfo(@PathVariable Integer userId) {
        return userService.getUserInfo(userId);
    }
}
```

* 이렇게 하고 프로젝트를 실행한 후, `/users/{userId}`로 요청을 보내면 아래와 같이   
  응답이 잘 온다는 것을 알 수 있다.
```json
{
    "userId": 15,
    "name": "Sangwoo",
    "email": "robbyra@gmail.com"
}
```

* 하지만 여기서 문제점은 __우리가 원하는 정보는 email, name이지만 Hibernate가 해당 users 테이블에서__   
  __해당 id를 가진 행 모두에 대해 `SELECT` 쿼리를 실행한다는 것이다.__   
  즉 필요하지 않은 정보까지 모두 SELECT Query를 통해 가져오고 있다는 것이다.

* 실제로 Hibernate가 수행하는 쿼리문은 아래와 같다.
```
select user0_.user_id as user_id1_0_0_, user0_.created_at as created_2_0_0_, user0_.last_modified_at as last_mod3_0_0_, user0_.email as email5_0_0_, user0_.name as name6_0_0_, user0_.password as password7_0_0_, user0_.phone_number as phone_nu8_0_0_ from users user0_ where user0_.user_id=15
```

<hr/>

<h2>해결법 1 - Native Query</h2>

* 우선 Native Query를 사용하기 위해 Repository Layer 코드부터 변경해보자.
```java
@Repository
public interface UsersRepository extends JpaRepository<User, Integer> {

    @Query(value = "SELECT user_id, name, email FROM users WHERE user_id = ?", nativeQuery = true)
    List<Object[]> findById_(Integer userId);
}
```

* 또한 위 메소드를 적용한 서비스 코드와 DTO는 아래와 같다.
```java
// UserService.java
@RequiredArgsConstructor
@Service
public class UserService {

    private final UsersRepository usersRepository;

    @Transactional(readOnly = true)
    public UserSimpleInfoResponseDto getUserInfo(Integer userId) {
        List<Object[]> user = usersRepository.findById_(userId);
        if(user.size() != 0) {
            return new UserSimpleInfoResponseDto(user.get(0));
        } else throw new UserIdNotFoundException();
    }
}

// UserSimpleInfoResponseDto.java
@NoArgsConstructor
@Getter
@Setter
public class UserSimpleInfoResponseDto {

    private Integer userId;
    private String name;
    private String email;

    public UserSimpleInfoResponseDto(Object[] objects) {
        this.userId = Integer.parseInt(objects[0].toString());
        this.name = objects[1].toString();
        this.email = objects[2].toString();
    }
}
```

* 위 코드도 동작하긴 하지만 여러 가지 문제점이 있다.

* 첫 번째로, 위와 같이 Natie Query를 사용하면 Hibernate는 WHERE절이 있음에도 불구하고   
  해당 메소드 수행 결과가 1개의 결과를 가진다는 것을 알지 못하고, `ArrayList`로 반환한다.   
  따라서 서비스 코드에서 항상 `List#get(0)`을 수행하여 처리해야 한다.

* 두 번째로, 서비스 코드에서 `List#get(0)`으로 정보를 가져와도 해당 객체는 `Object[]` 타입이기에   
  DTO 클래스에서 해준 것과 같이 각 자리에 맞는 값을 가져와서 타입에 맞게 변경해줘야 한다.

* 마지막으로 위로 조회된 결과는 당연히 JPA의 영속성 컨텍스트에 보관되지 않는다.   
  이는 곧 JPA를 사용하는 의미가 없다고도 할 수 있다.

* 여러모로 코드가 많이 복잡해졌음을 느낄 수 있는 방법이다.

<h2>해결법 2 - JPQL 사용</h2>

* 마찬가지로 Repository 코드부터 변경해보자.
```java
@Repository
public interface UsersRepository extends JpaRepository<User, Integer> {

    @Query(value = "SELECT new example.dto.UserSimpleInfoResponseDto(u.id, u.name, u.email) FROM User u WHERE u.id = :userId")
    List<UserSimpleInfoResponseDto> findById_(Integer userId);
}
```

* 다음으로 Service와 DTO 코드를 보자.
```java
// UserService.java
@RequiredArgsConstructor
@Service
public class UserService {

    private final UsersRepository usersRepository;

    @Transactional(readOnly = true)
    public UserSimpleInfoResponseDto getUserInfo(Integer userId) {
        List<UserSimpleInfoResponseDto> dtoList = usersRepository.findById_(userId);
        if(dtoList.size() == 0) {
            throw new UserIdNotFoundException();
        } else return dtoList.get(0);
    }
}

// UserSimpleInfoResponseDto.java
@NoArgsConstructor
@Getter
@Setter
public class UserSimpleInfoResponseDto {

    private Integer userId;
    private String name;
    private String email;

    public UserSimpleInfoResponseDto(Integer id, String name, String email) {
        this.userId = id;
        this.name = name;
        this.email = email;
    }
}
```

* 위처럼 Repository가 `List`를 반환하게 하지 말고 `Optional`로 할 수도 있다.   
  아래는 `Optional` 타입을 적용한 코드이다.
```java
// UsersRepository.java
@Repository
public interface UsersRepository extends JpaRepository<User, Integer> {

    @Query(value = "SELECT new example.dto.UserSimpleInfoResponseDto(u.id, u.name, u.email) FROM User u WHERE u.id = :userId")
    Optional<UserSimpleInfoResponseDto> findById_(Integer userId);
}

// UserService.java
@RequiredArgsConstructor
@Service
public class UserService {

    private final UsersRepository usersRepository;

    @Transactional(readOnly = true)
    public UserSimpleInfoResponseDto getUserInfo(Integer userId) {
        UserSimpleInfoResponseDto dto = usersRepository.findById_(userId).orElseThrow(UserIdNotFoundException::new);
        return dto;
    }
}
```

* 위 `@Query`의 value로 들어간 JPQL문은 users 테이블에서 id, name, email을 가져온 후   
  해당 정보를 생성자로 받는 DTO 클래스에 전달하여 바로 DTO 클래스를 만든 것이다.

* 수행되는 쿼리문은 아래와 같다.
```
select user0_.user_id as col_0_0_, user0_.name as col_1_0_, user0_.email as col_2_0_ from users user0_ where user0_.user_id=15
```

* 내가 아는 선에서 딱히 문제될 것은 안보인다... 쿼리문을 직접 써준다는 단점 뿐..?
<hr/>

<h2>해결법 3 - Projection 사용</h2>

* 마지막 해결법은 `Spring Data Projection`으로 쿼리문의 결과를 조회하는 것이다.

* Interface-based Projection을 사용하기 위해서는 인터페이스를 정의해야 하는데,   
  각 `getter` 메소드들은 JavaBean Getter에 알맞게 작성해야 한다.   
  이 경우에는 id, email, name을 조회하고 싶은 것이기에 아래와 같이 작성한다.
```java
public interface UserIdAndNameAndEmailProjection {
    Integer getId();
    String getName();
    String getEmail();
}
```

* 참고로 인터페이스명은 네이밍 규칙이 없으며 자유로 선택 가능하다.

* 이 Projection Interface를 사용하는 방법은 두 가지가 있는데,   
  첫 번째로는 JPQL과 함께 사용하는 것이다.

* JPQL과 같이 사용하는 Repository 코드는 아래와 같다.
```java
@Repository
public interface UsersRepository extends JpaRepository<User, Integer> {

    @Query(value = "SELECT u.id AS id, u.name AS name, u.email AS email FROM User u WHERE u.id = :userId")
    Optional<UserIdAndNameAndEmailProjection> findById_(Integer userId);
}
```

* 위 `@Query`의 value에서 유의할 점은 AS 구문이 컬럼마다 붙어있다는 것이다.   
  만약 AS 구문으로 Aliasing을 하지 않으면 모든 값이 null로 오게 된다.

* Service 코드는 아래와 같다.
```java
@RequiredArgsConstructor
@Service
public class UserService {

    private final UsersRepository usersRepository;

    @Transactional(readOnly = true)
    public UserIdAndNameAndEmailProjection getUserInfo(Integer userId) {
        UserIdAndNameAndEmailProjection projection = usersRepository.findById_(userId).orElseThrow(UserIdNotFoundException::new);
        return projection;
    }
}
```

* 위 방법의 단점은 JPQL을 사용해야 하고, 모든 컬럼에 대해 AS문으로 Aliasing을 해줘야 한다는 것이다.   
  또한 Aliasing된 이름으로 interface의 getter 메소드를 선언해 줘야 한다.

* 두 번째 방법은 JPQL을 사용하지 않는 것인데, Repository 코드부터 보자.
```java
@Repository
public interface UsersRepository extends JpaRepository<User, Integer> {

    <T> Optional<T> findById_(Integer userId, Class<T> clazz);
}
```

* 해당 메소드를 사용하는 Service 코드를 보자.
```java
@RequiredArgsConstructor
@Service
public class UserService {

    private final UsersRepository usersRepository;

    @Transactional(readOnly = true)
    public UserIdAndNameAndEmailProjection getUserInfo(Integer userId) {
        UserIdAndNameAndEmailProjection projection = usersRepository.findById_(userId, UserIdAndNameAndEmailProjection.class).orElseThrow(UserIdNotFoundException::new);
        return projection;
    }
}
```

* 위 코드로 인해 수행되는 쿼리문은 아래와 같다.
```
select user0_.user_id as col_0_0_, user0_.name as col_1_0_, user0_.email as col_2_0_ from users user0_ where user0_.user_id=15
```

* 만약 아래와 같이 `User` 엔티티의 필드에 없는 대상에 대한 Getter 메소드를 작성하면 오류가 뜬다.
```java
public interface UserIdAndNameAndEmailProjection {
    Integer getIDDD();
    String getName();
    String getEmail();
}
```

* 오류 메시지는 아래와 같다.
```j
org.springframework.data.mapping.PropertyReferenceException: No property IDDD found for type User!
```
<hr/>