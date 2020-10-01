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
<hr/>

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
<hr/>

<h2>정렬 메소드 명명 규칙</h2>

* `Todos` Entity의 목록을 content 속성별로 DESC(내림차순)으로 가져오고 싶어서 처음에는 아래와 같이 메소드를 작성했다.
```java
public interface TodosRepository extends JpaRepository<Todos, Integer> {
    List<Todos> findAllOrderByContentDesc();
}
```

* 그랬더니 아래와 같은 오류가 출력되었다.
```
java.lang.IllegalArgumentException: Failed to create query for method public abstract java.util.List com.yourssu.domain.TodosRepository.findAllOrderByContentDesc()! No property desc found for type String! Traversed path: Todos.content`
```

* 명명 규칙을 잘 지켰다고 생각해서 stackoverflow를 뒤져보니 아래와 같은 해결방법이 있었다.
```
I know it's a bit late but this may help others.

Just put BY before ORDER, like this: findAllByOrderByIdDesc

It should work.
```

* 그래서 아래와 같이 메소드명을 바꾸었더니 해결되었다.;;
```java
public interface TodosRepository extends JpaRepository<Todos, Integer> {
    List<Todos> findAllByOrderByContentDesc();
}
```
<hr/>

<h2>Sort 생성자의 Deprecated 처리</h2>

* 위와 같은 방식(content 속성별로 DESC 정렬)으로 정렬하여 데이터를 가져오고 싶어, 이번에는 `Sort`를 사용해보도록 했다.
* 우선, `TodosRepository`에 아래 메소드를 선언했다.
```java
List<Todos> findAll(Sort sort);
```

* 앞서 정리한 `Sort`의 사용법을 보면, 아래와 같이 사용하면 된다.
```java
@Service
public class TodosService {

    //..

    Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC, "content"));
    List<Todos> todosList = todosRepository.findAll(sort);
}
```

* 하지만 __`Sort`객체를 `new Sort()` 방식으로 생성하는 방식, 즉 `Sort`의 생성자는 Deprecated 되었으며__, 대신 `Sort.by()`를 사용한다.   
  아래는 위의 방식대로 정렬하는 코드이다.
```java
@Service
public class TodosService {

    //..

    List<Todos> todosList = todosRepository.findAll(Sort.by(new Sort.Order(Sort.Direction.DESC, "content")));
}
```
<hr/>

<h2>PageRequest 생성자의 Deprecated 처리</h2>

* 데이터를 2개씩 가져오면서 첫 번째 페이지를 가져오려면 아래와 같이 `PageRequest`객체를 생성해야 한다.
```java
PageRequest pageRequest = new PageRequest(0, 2);
List<Todos> todosList = todosRepository.findAll(pageRequest);
```

* 하지만 `PageRequest` 객체의 생성자는 Deprecated 처리 되었고, 대신 아래와 같이 `PageRequest.of()`를 사용한다.   
  아래는 위의 방식대로 페이징 처리하는 코드이다.
```java
Page<Todos> todos = todosRepository.findAll(PageRequest.of(0, 2));
```

* 만약 `PageRequest`객체에 `Sort`까지 추가하고 싶다면, 아래와 같이 하면 된다. 아래 예시는 페이징 처리와 content 속성별 DESC 정렬하여   
  가져오는 코드이다.
```java
Page<Todos> todos = todosRepository.findAll(PageRequest.of(page, itemCount, Sort.by(new Sort.Order(Sort.Direction.DESC, "content"))));
```

* 즉, 아래와 같은 기본 구조를 띈다.
```java
PageRequest.of(page, itemCount, Sort.by(/*..*/));
```
<hr/>

<h2>Specifications의 Deprecated 처리</h2>

* `Specifications` 객체는 Deprecated 처리 되었다. 대신, `Specification` 객체를 사용하도록 한다.
<hr/>