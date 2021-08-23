# @Id 어노테이션의 작동 방식

- 이번에는 JPA에서 AUTO_INCREMENT 값을 어떻게 판단하고 입력하는지 알아보자.  
  사실 `@Id` 어노테이션의 기능이 아니라 `@GeneratedValue` 어노테이션에 대해 살펴볼 것이다.

<h2>상황</h2>

- 아래 쿼리문으로 생성된 users 테이블이 있다.

```sql
CREATE TABLE users(
    user_id INTEGER AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL
);
```

- 그리고 위 테이블과 JPA를 통해 매핑된 객체가 아래와 같이 있다.

```kt
@Entity
@Table(name = "users")
class User(name: String) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, name = "user_id")
    val id: Int? = null

    @Column(nullable = false, length = 45)
    var name: String = name
}
```

<hr/>
