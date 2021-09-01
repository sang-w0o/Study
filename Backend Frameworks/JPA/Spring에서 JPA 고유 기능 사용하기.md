# Spring에서 JPA의 고유 기능 사용하기

- Spring Framework를 사용하여 백엔드 애플리케이션을 개발할 때, 보통 아래와 같이 Entity class를  
  정의하고, 바로 `JpaRepository`를 상속받는 인터페이스를 선언하여 Persistence Layer를 구축한다.

```kt
// Entity class: User

@Entity
@Table(name = "users")
class User(name: String, email: String, password: String): CreatedAtEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, name = "user_id")
    val id: Int? = null

    @Column(nullable = false, length = 100)
    var name: String = name

    @Column(length = 60)
    var password: String? = password

    @Column(nullable = false, length = 100, unique = true)
    var email: String = email
}

// Repository Interface for user
@Repository
interface UserRepository : JpaRepository<User, Int> {
    fun findByEmail(email: String): User?
}
```

- 이렇게 JPA가 정의한 규칙 대로 레포지토리 인터페이스에 함수를 선언하기만 하면,  
  JPA가 JPA Provider(대표적으로 Hibernate)를 사용하여 DB에 수행할 쿼리를 직접 만들어준다.

- 하지만 `JpaRepository`, `PagingandSortingRepository`, `CrudRepository`는  
  JPA를 Spring에서 편하게 사용하기 위해 만들어진 라이브러리에 속해있다.  
  실제로 이들의 위치는 `org.springframework.data.repository`에 있으며,  
  Spring-Data-JPA에서 제공하는 라이브러리에 있다.

- 이번에는 이를 사용하지 않고, JPA가 제공하는 순수 기능을 Spring에서 다뤄보려 한다.

<h2>EntityManager</h2>

- 우선 `findById`부터 직접 구현해보자.  
  가장 먼저 아래와 같은 레포지토리 인터페이스가 있다.

```kt
interface UserRepository {
	fun findById(userId: Int): Optional<User>
}
```

- 다음으로는 위 `UserRepsitory`를 구현하는 구현체에서 `EntityManager`를 사용하여 쿼리 작업을 하게 하자.

```kt
@Repository
class UserRepositoryImpl(entityManagerFactory: EntityManagerFactory) : UserRepository {

    private val entityManager = entityManagerFactory.createEntityManager()

    override fun findById(userId: Int): Optional<User> {
        return try {
            entityManager.transaction.begin()
            val user = entityManager.find(User::class.java, userId)
            entityManager.transaction.commit()
            Optional.of(user)
        } catch(e: Exception) {
            entityManager.transaction.rollback()
            e.printStackTrace()
            Optional.empty<User>()
        }
    }
}
```

- `EntityManager` 객체는 `EntityManagerFactory`를 통해 얻어올 수 있고, `EntityManagerFactory`는  
  생성자를 통한 DI가 가능하다. 따라서 `findById()` 함수 내에서는 `entityManager.transaction.begin()`를  
  호출하여 트랜잭션을 시작하고, `entityManager.find()`를 통해 원하는 쿼리를 수행했으며 마지막으로  
  정상적으로 찾았을 때는 `entityManager.transaction.commit()`으로, 오류가 발생했을 경우에는  
  catch 문에서 `entityManager.transaction.rollback()`으로 트랜잭션을 상황에 맞게  
  commit 또는 rollback하여 종료시켰다.

- 이제 위 레포지토리를 사용할 Service layer의 코드는 아래와 같이 작성 가능하다.

```kt
@Service
class TestService(
    private val userRepository: UserRepository
) {

    fun testFindByUserId(userId: Int): UserInfoResponseDto {
        val user = userRepository.findById(userId).orElseThrow { UserIdNotFoundException("User not found") }
        return UserInfoResponseDto(user)
    }
}
```

<h2>JPQL, TypedQuery, Criteria API</h2>

- 위 코드는 상당히 간단했는데, 이유는 `EntityManager#find()` 메소드가 아래와 같이 정의되어 있기 때문이다.

```java
public interface EntityManager {
	//..
	public <T> T find(Class<T> entityClass, Object primaryKey);
}
```

- 즉, 두 번째 인자로 Entity Class의 PK를 받기에 간단히 작성할 수 있었던 것이다.  
  이번에는 `findByEmail()` 메소드, 즉 `SELECT * FROM users WHERE email = ?` 형식의  
  쿼리를 수행하는 메소드를 구현해보자.
