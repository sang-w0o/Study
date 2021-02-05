<h1>JPA에서 연관 관계 삭제하기</h1>

<h2>문제 상황</h2>

* 시나리오는 다음과 같다.

  * `User`라는 테이블이 있으며, `Information`이라는 테이블이 이 테이블과 `1:N` 연관의   
    관계를 가진다.
  * 사용자가 한 번에 여러 개의 `Information`들의 내용을 수정하게 할 수 있는 서비스가 필요하다.
  * 기존에는 `informationsRepository.deleteByUserId(Integer userId)` 메소드를 사용해서   
    해당 user의 `Information`을 모두 삭제한 후 새로 넣는 방식으로 작성하였으나, 이렇게 하면   
    너무나 비효율 적이고 `Information`의 AUTO_INCREMENT id값도 기하급수적으로 증가하게 되므로   
    다른 방식이 필요했다. 또한 이 상황은 사용자가 `Information` 중 몇개를 삭제하여 개수를 줄이는 경우와   
    `Information`에 새로운 값들을 추가하는 상황도 생각해야 한다.

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
    private List<Information> informations = new ArrayList<>();
}
```

* 다음으로 `Information` 코드를 보자.
```java
@NoArgsConstructor
@Getter
@Entity
@Table(name = "informations")
public class Information {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "information_id")
    private Integer id;

    @Column
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Information(String content, User user) {
        this.content = content;
        this.user = user;
    }
}
```
<hr/>

<h2>서비스 코드 생각하기</h2>

* 위 코드에서 보다시피 `Information`은 고유의 AUTO_INCREMENT로 증가하는 question_id라는 PK가 있다.   
  사용자가 여러개의 `Information`의 content를 수정하는 API를 보낼때마다 `informationsRepository.deleteByUserId(Integer userId)`를   
  호출하는 것은 우선 `DELETE` query를 수행한 후 새로운 값들을 넣기 때문에 성능 상에서도 시간이 더 오래 걸리며,   
  `Information`의 id값이 수정할 때마다 증가하게 될 것이다.

* 이를 해결하기 위해 아래의 3가지 경우를 생각했다.
  1. 수정된 `Information`의 개수가 기존(수정 전)과 동일한 경우   
    * 이 경우에는 각 `Information`의 content들만 수정해주면 된다.
  2. 수정된 `Information`의 개수가 기존 개수보다 더 많을 경우
    * 이 경우에는 기존 개수만큼 것들을 update한 후 새로운 것들만 추가해주면 된다.
  3. 수정된 `Information`의 개수가 기존 개수보다 적을 경우
    * 이 경우에는 새로 들어온 것들의 개수만큼 기존 content들을 업데이트한 후,   
      기존에 남아있는 것들에 대해서는 delete를 수행하면 된다.
<hr/>

<h2>코드 보기</h2>

* 우선 아래와 같은 테스트 코드를 작성했다.
```java

```