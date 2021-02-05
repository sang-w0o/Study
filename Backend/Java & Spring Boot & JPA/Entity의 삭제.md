<h1>JPA에서 연관 관계 삭제하기</h1>

<h2>문제 상황</h2>

* 시나리오는 다음과 같다.

  * `User`라는 테이블이 있으며, `Question`이라는 테이블이 이 테이블과 `1:N` 연관의   
    관계를 가진다.
  * 사용자가 한 번에 여러 개의 `Question`들의 내용을 수정하게 할 수 있는 서비스가 필요하다.
  * 기존에는 `questionsRepository.deleteByUserId(Integer userId)` 메소드를 사용해서   
    해당 user의 `Question`을 모두 삭제한 후 새로 넣는 방식으로 작성하였으나, 이렇게 하면   
    너무나 비효율 적이고 `Question`의 AUTO_INCREMENT id값도 기하급수적으로 증가하게 되므로   
    다른 방식이 필요했다.

* 우선 Entity 코드는 아래와 같다.
* 먼저 `User` 코드를 보자.
```java
@NoArgsConstructor
@Getter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;

    // 그 외 column들

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Question> questions = new ArrayList<>();
}
```

* 다음으로 `Question` 코드를 보자.
```java
@NoArgsConstructor
@Getter
@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Integer id;

    @Column
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
```
<hr/>

<h2>서비스 코드 생각하기</h2>

* 위 코드에서 보다시피 `Question`은 고유의 AUTO_INCREMENT로 증가하는 question_id라는 PK가 있다.