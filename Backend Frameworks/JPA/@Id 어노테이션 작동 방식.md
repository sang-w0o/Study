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

- 우선 `@Id` 어노테이션은 JPA가 영속성 컨텍스트에 보관된 엔티티들을 식별하는데에 사용된다.  
  그렇기에 항상 Primary Key에 지정해줘야 한다.

- 다음으로 `@GeneratedValue` 어노테이션은 Primary Key의 생성 방식을 지정할 수 있게 해주는  
  어노테이션이다. 이 어노테이션은 PK 필드에 사용될 수도 있고, `@Id` 어노테이션과 함께 `@MappedSuperClass`  
  어노테이션이 지정된 클래스에 사용될 수 있다. 이 어노테이션은 간단한 PK를 위해 사용되어야만 한다.  
  다른 로직에 의해 파생되어 생기는 PK에 대해서는 지원이 되지 않는다.

- 우선 `@GeneratedValue` 어노테이션은 2개의 속성을 받는데, 하나는 strategy, 그리고 다른 하나는 generator이다.  
  둘 다 필수 값이 아니며, 기본값은 strategy는 `GenerationType.AUTO`, generator는 빈 문자열`""` 이다.  
  우선 strategy는 PK가 생성될 전략을 지정한다. 이 속성 값에는 `javax.persistence.GenerationType`  
  enum 중 하나의 값이 들어가야 하며, 이 전략을 사용하여 데이터베이스에 PK를 어떻게 넣을지 정하게 된다.  
  다음으로 generator에는 사용자가 직접 정의한 `@SequenceGenerator` 또는 `@TableGenerator` 어노테이션이  
  적용된 PK를 만드는 로직이 담긴 generator를 지정할 수 있다.

- 우리는 PK가 AUTO_INCREMENT인 경우에 JPA가 `save()` 호출 시 어떻게 PK의 값을 정하는지를 알아볼 것이기에  
  `@GeneratedValue`의 generator 속성은 알아보지 않을 것이다.

<h2>GenerationType</h2>

- 위에서 말했듯이, `GenerationType` 열거형은 `@GeneratedValue`의 strategy 속성에 들어갈 수 있는 값으로,  
  PK가 생성되는 전략을 의미한다.

```java
public enum GenerationType {
	TABLE,
	SEQUENCE,
	IDENTITY,
	AUTO
}
```

- `GenerationType.TABLE`: 이 속성은 Persistence Provider(JPA의 구현체, Hibernate 등)이  
  기존에 데이터베이스에 존재하는 테이블을 이용하여 PK가 unique함을 보장해야 할 때 사용한다.

- `GenerationType.SEQUENCE`: 이 속성은 Persistence Provider가 데이터베이스의 sequence를  
  이용하여 PK의 값을 할당해야함을 의미한다.

- `GenerationType.IDENTITY`: 이 속성은 Persistence Provider가 데이터베이스의 identity column을  
  사용하여 PK에 값을 할당해야함을 의미한다.

- `GenerationType.AUTO`: 이 속성은 Persistence Provider가 데이터베이스에 맞게 적절한 전략을  
  선택해야한다는 것을 의미한다.

<hr/>
