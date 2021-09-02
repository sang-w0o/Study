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

<h3>JPQL + TypedQuery</h3>

- JPQL과 `TypedQuery`의 조합으로 구현해본 메소드는 아래와 같다.

```kt
@Repository
class UserRepositoryImpl(entityManagerFactory: EntityManagerFactory) : UserRepository {
	private val entityManager = entityManagerFactory.createEntityManager()

	//..

	override fun findByEmail(email: String): Optional<User> {
        return try {
            entityManager.transaction.begin()
            val query = entityManager.createQuery("SELECT u FROM User u WHERE u.email = :email", User::class.java)
            query.setParameter("email", email)
            val user = query.singleResult
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

<h3>Criteria API</h3>

- 이번에는 JPA가 제공하는 Criteria API를 사용하여 함수를 구현해보자.

```kt
@Repository
class UserRepositoryImpl(entityManagerFactory: EntityManagerFactory) : UserRepository {

    //..

    override fun findByEmail(email: String): Optional<User> {
        return try {
            entityManager.transaction.begin()
            val criteriaBuilder = entityManager.criteriaBuilder
            val criteriaQuery = criteriaBuilder.createQuery(User::class.java)
            val root = criteriaQuery.from(User::class.java)
            criteriaQuery.select(root)
            criteriaQuery.where(criteriaBuilder.equal(root.get<String>("email"), email))
            val query = entityManager.createQuery(criteriaQuery)
            val user = query.singleResult
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

- Criteria API에 대한 더 자세한 내용은 <a href="https://github.com/sang-w0o/Study/blob/master/Backend%20Frameworks/JPA/%EC%9D%B4%EB%A1%A0/13.%20Criteria%20API%EB%A5%BC%20%EC%9D%B4%EC%9A%A9%ED%95%9C%20query.md">여기</a>에서 확인 가능하다.

<h4>약간의 간결화</h4>

- 위 코드에서는 `entityManager.transaction.begin()`, `commit()`, `rollback()` 등의 트랜잭션  
  관리를 직접 해주었다. 하지만 JPA가 제공하는 `@PersistenceContext` 어노테이션을 사용하면 이를 조금  
  간결화할 수 있다.

- `@PersistenceContext` 어노테이션이 적용된 필드가 있다면, `EntityManager`객체가 JEE Container에  
  의해 주입된다. JEE Container는 `@Transactional` 어노테이션이 적용된 메소드를 transaction 범위에서  
  실행하는데, 이 때 `@PersistenceContext`를 이용하여 주입받은 `EntityManager`는 JEE가 관리하는  
  transaction에 참여한다. 따라서 기존처럼 application code에서 트랜잭션을 직접 관리하지 않아도 된다.

- 이렇게 컨테이너에 의해 생명주기가 관리되는 것을 Container-managed EntityManager라고 하며,  
  주의점은 **`EntityManager`의 생성과 종료를 Container가 관리하기에 application code에서**  
  **직접 `close()`를 호출하면 안된다는 점이다.** JPA Spec에 따르면 Container-Managed  
  EntityManager에 대해 명시적으로 `close()`를 호출하는 경우 `IllegalArgumentException()`이  
  발생하게 되어 있으며, 이 때문에 작업을 올바르게 수행한 트랜잭션도 rollback되는 상황이 발생할 수 있다.

- 그러면 이제 `@PersistenceContext`를 사용한 코드로 바꿔보자.

```kt
@Repository
class UserRepositoryImpl : UserRepository {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Transactional
    override fun findById(userId: Int): Optional<User> {
        val user = entityManager.find(User::class.java, userId)
        return Optional.of(user)
    }

    @Transactional
    override fun findByEmail(email: String): Optional<User> {
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteriaQuery = criteriaBuilder.createQuery(User::class.java)
        val root = criteriaQuery.from(User::class.java)
        criteriaQuery.select(root)
        criteriaQuery.where(criteriaBuilder.equal(root.get<String>("email"), email))
        val query = entityManager.createQuery(criteriaQuery)
        val user = query.singleResult
        return Optional.of(user)
    }
}
```

- 이때 `@Transactional` 어노테이션은 `org.springframework.transaction.annotation.Transactional`이 아니라  
  `javax.transaction.Transactional`을 사용했다.

- 또한 try-catch문의 제거는 `@PersistenceContext`를 사용한 것과는 관계가 없다.

- 바뀐 부분은 직접 commit, rollback 처리를 해주지 않았줘도 되는 것이며 `EntityManager`를 생성자를 통한 주입이 아닌  
  필드를 통한 의존성 주입을 받아 사용하게 되었다는 것이다.

<hr/>
