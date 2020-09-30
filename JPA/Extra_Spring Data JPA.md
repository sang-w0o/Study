<h1>Spring Data JPA 시행착오 모음</h1>

* 우선 `Todos`라는 Entity Class가 아래와 같이 있다고 하자.
```java
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashMap;

@NoArgsConstructor
@Getter
@Entity
public class Todos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int todo_id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TodoState todo_state;

    @Builder
    public Todos(String email, String content, TodoState todo_state) {
        this.email = email;
        this.content = content;
        this.todo_state = todo_state;
    }

    public HashMap<String, Object> convertMap() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("email", email);
        hashMap.put("content", content);
        hashMap.put("state", todo_state);
        return hashMap;
    }
}
```

* DB 접근을 수행하는 `TodosRepository` 는 아래와 같다.
```java
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodosRepository extends JpaRepository<Todos, Integer> {}
```

<h2>findOne() 메소드의 사용</h2>

* `JpaRepository`가 상속하고 있는 부모 인터페이스인 `CrudRepository`가 제공하는 메소드 중 `findOne()`을 보자.
```java
public interface CrudRepository<T, ID extends Serializable> extends Repository<T, ID> {

    //.. 생략
    T findOne(ID id);
}
```

* 위 코드에서 `TodosRepository`는 `CrudRepository`를 상속받는 `JpaRepository`를 상속받음에도 불구하고 `findOne()` 메소드가 제대로   
  수행되지 않았다. 대신, `JpaRepository`에만 있는 `getOne()` 메소드를 사용하니 해결 되었다. `getOne()` 메소드는 아래와 같다.
```java
public interface JpaRepository<T, ID extends Serializable> extends PagingAndSortingRepository<T, ID>, QueryByExampleExecutor<T> {
    
    //.. 생략

    T getOne(ID id);
}
```