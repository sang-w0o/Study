Spring Boot에서 JPA의 사용
======

* 웹 서비스를 개발 및 운영하면, DB를 다루는 일은 불가피하다.   
  관계형 DB를 이용하는 프로젝트에서 객체 지향 프로그래밍을 하는 방법은 __JPA, 자바 표준 ORM(Object Relational Mapping)__   
  기술을 활용하는 것이다.
<hr/>

<h2>JPA란</h2>

* 현대 Web Application에서 RDBMS는 빠질 수 없는 요소이며, 대부분 Oracle, Mysql, MSSQL 등을 사용한다.   
  따라서 __객체를 관계형 DB에서 관리하는 것__ 또한 필수 요소가 되었다.

* RDBMS가 계속해서 Web Service의 중심이 되면서 모든 코드는 SQL 중심이 됐으며, RDBMS는 SQL만 인식할 수 있기에   
  각 테이블마다 기본적인 CRUD SQL을 매번 생성해야 한다. 예를 들어 User 객체를 테이블로 관리한다면 아래 코드는 피할 수 없다.
```sql
INSERT INTO users VALUES(?,?,?..);
SELECT * FROM users WHERE ...;
UPDATE users SET ... WHERE ...;
DELETE FROM users WHERE ...;
```
* 이는 반복적인 SQL문 사용을 남발하며, 현업에서는 수십, 수백개의 테이블이 있는데, 이 테이블의 몇 배가 되는 SQL구문을 만들고   
  유지 보수해야 한다.
* 단순 반복 외에도 __패러다임 불일치__ 문제가 있는데, RDBMS는 __어떻게 데이터를 저장할지__ 에 초점이 맞춰진 기술인 반면,   
  OOP는 __메시지를 기반으로 기능과 속성을 한 곳에서 관리하는 것__ 에 초점이 맞춰진 기술이다.   
  이렇게 서로 다른 목적을 가진 기술을 사용할 때 __패러다임 불일치 문제__ 가 발생한다.

* 서로 지향하는 바가 다른 2개의 영억(RDBMS, OOP)의 중간에서 __패러다임 일치를 시켜주는 기술이 JPA__ 이다.   
  즉, 개발자는 OOP 방식으로 프로그래밍을 하고, __JPA가 이를 RDBMS에 맞게 SQL을 대신 생성해서 실행한다__.   
  개발자는 항상 OOP원칙에 맞게 코드를 표현할 수 있으니, 더는 __SQL에 종속적인 개발을 하지 않아도 된다__.
<hr/>

<h3>Spring Data JPA</h3>

* JPA는 인터페이스로, Java 표준 명세서이다. Interface인 JPA를 사용하기 위해서는 이를 구현하는 구현체가 필요하다.   
  구현체는 대표적으로 `Hibernate`, `EclipseLink`등이 있다. 하지만 Spring에서 JPA를 사용할 때에는 이 구현체들을   
  직접 다루지는 않는다.

* Spring에서는 구현체들을 좀 더 쉽게 사용하고자 추상화 시킨 `Spring Data JPA`라는 모듈을 이용하여 JPA 기술을 다룬다.   
  이들의 관계는 `JPA <== Hibernate <== Spring Data JPA` 와 같다.

* `Hibernate`를 쓰는 것과 `Spring Data JPA`를 사용하는 것은 큰 차이가 없지만, Spring에서는 `Spring Data JPA`의 사용을 권장한다.   
  `Spring Data JPA`가 등장한 이유는 크게 다음 두 가지가 있다. 
  * 구현체 교체의 용이성 : `Hibernate`외에 다른 구현체로 쉽게 교체하기 위함.
  * 저장소 교체의 용이성 : RDBMS외에 다른 저장소로 쉽게 교체하기 위함. (ex. MySQL --> MongoDB)

* Spring Data의 하위 프로젝트들은 기본적인 __CRUD 인터페이스가 같다__. 즉, `Spring Data JPA`, `Spring Data Redis`,   
  `Spring Data MongoDB` 등 Spring Data의 하위 프로젝트들은 `save()`, `findAll()`, `findOne()`등을 인터페이스로 갖는다.   
  그렇기에 저장소가 교체되어도 기본적인 기능은 변경할 것이 없다. 이것이 `Spring Data` 프로젝트의 사용이 권장되는 이유이다.
<hr/>

<h3>실무에서의 JPA</h3>

* 실무에서 JPA를 사용하지 못하는 가장 큰 이유는 __높은 러닝 커브__ 인데, JPA를 잘 사용하려면 `OOP`, `RDBMS`를 모두 이해해야 한다.   
  하지만 그만큼 JPA를 사용할 때 얻는 이점은 CRUD 쿼리를 직접 작성하지 않아도 되는 것과 테이블간의 부모-자식 관계 표현,   
  1:N관계 표현, 상태와 행위를 한 곳에서 관리하는 등 OOP를 쉽게 할 수 있기 때문이다.
* 또한 JPA는 성능상의 이슈 해결책이 잘 마련돼있어 잘 활용하면 Native Query만큼의 성능을 낼 수 있다.
<hr/>

<h3>요구사항 분석</h3>

* 지금부터 할 프로젝트는 하나의 게시판(Web Application)을 만들어보고, 이를 AWS에 무중단 배포하는 것이다.
* 게시판의 요구사항은 다음과 같다.
  * 게시판 기능 : 게시글 조회, 등록, 수정, 삭제 
  * 회원 기능 : 구글 / 네이버 로그인, 로그인한 사용자에 대해 글 작성 권한 부여, 본인이 작성한 글에 대한 권한 관리
<hr/>

<h2>프로젝트에 Spring Data JPA 적용</h2>

* `Spring DATA JPA`를 적용하기 위해서는 `build.gradle`에 아래와 같은 의존성을 추가한다.
```js
dependencies {
    compile('org.springframework.boot:spring-boot-starter-data-jpa')
    compile('com.h2database:h2')
}
```
* `spring-boot-start-data-jpa` : Spring Boot용 Spring Data JPA 추상화 라이브러리로, Spring Boot의 버전에 맞춰   
  JPA 관련 라이브러리들의 버전을 관리해준다.
* `h2` : in-memory 관계형 DB로, 별도의 설치없이 프로젝트 의존성만으로 관리할 수 있다.   
  메모리에서 실행되기 때문에 app을 재시작할 때 마다 초기화된다는 점을 이용하여 Test용도로 많이 사용된다.

* `domain` 패키지를 만드는데, 이 패키지는 도메인을 담을 패키지이다.   
  도메인은 게시글, 댓글, 회원, 정산, 결제 등 소프트웨어에 대한 요구사항 혹은 문제 영역을 의미한다.
* 다음으로는 `domain` 패키지 하위에 `posts` 패키지와 `Posts` 클래스를 만든다.
* 아래는 `Posts.java` 클래스이다.
```java
package com.sangwoo.board.domain.posts;

import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
public class Posts {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 500, nullable=false)
    private String title;
    
    @Column(columnDefinition="TEXT", nullable=false)
    private String content;
    
    private String author;
    
    @Builder
    public Posts(String title, String content, String author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }
}
```
* __@Entity__ 는 JPA의 어노테이션이며, __@Getter, @NoArgsConstructor__ 는 Lombok의 어노테이션이다.
* `Posts` 클래스는 실제 DB의 테이블과 매칭될 클래스이며, 이를 보통 `Entity Class`라 한다.   
  JPA를 사용하면 DB 데이터에 작업할 경우 실제 Query를 날리기보다는, 이 Entity Class의 수정을 통해 작업한다.
* __@Entity__ : 해당 클래스가 Table과 링크될 클래스임을 나타낸다. 기본값으로 Class의 Camel-casing 이름을 _으로   
  테이블 이름을 매칭한다. (ex. SalesManager.java --> sales_manager 테이블)
* __@Id__ : 해당 테이블의 PK 필드를 나타낸다.
* __@GeneratedValue__ : PK의 생성 규칙을 나타내며, `GenerationType.IDENTITY`를 지정해야만 auto-increment가 된다.
* __@Column__ : 테이블의 column을 나타내며, 굳이 선언하지 않더라도 __@Entity__ 어노테이션이 적용된 클래스의 필드는   
  모두 column으로 취급된다. 문자열의 경우 기본값은 `VARCHAR(255)`인데, 자료형을 바꾸고 싶다면 `columnDefinition`   
  속성을 통해 자료형을 지정해주면 된다.
* __@NoArgsContructor__ : 기본 생성자를 자동으로 추가해준다.
* __@Getter__ : 클래스 내 모든 필드의 Getter메소드를 자동으로 생성해준다.
* __@Builder__ : 해당 클래스의 Builder-Pattern 클래스를 생성한다. 생성자 상단에 선언할 경우, 생성자에 포함된 필드만 빌더에 포함된다.

* Java Bean 규약을 생각하면서 무작정 getter/setter 메소드를 생성하는 경향이 있지만, 이렇게 하면 해당 클래스의 인스턴스 값들이   
  언제 어디서 변해야 하는지 코드상으로 명확히 구분할 수 없어 차후 기능 변경 시 복잡해진다.   
  따라서 __Entity Class에는 절대 Setter 메소드를 만들지 않는다__.

* Setter메소드가 없는 상황에서 값을 채워 DB에 insert하는 기본적인 구조는 다음과 같다.
  1. __생성자를 통해 최종값을 채운 후__ DB에 insert 한다.
  2. 값 변경이 필요한 경우, __해당 이벤트에 맞는 public 메소드를 호출__ 하여 변경한다.

* 위에서는 생성자 대신에 __@Builder__ 를 통해 제공되는 빌더 클래스를 이용한다. 생성자나 빌더나 생성 시점에 값을 채워주는   
  역할은 똑같지만, 생성자의 경우에는 생성 시 채워야할 필드가 무엇인지 명확히 지정할 수 없다.

* 다음으로는 `Posts` 클래스로 DB를 접근하게 해줄 `JpaRepository`를 생성하자. 
```java
package com.sangwoo.board.domain.posts;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostsRepository extends JpaRepository<Posts, Long> {
    
}
```
* 보통 DAO 라고 불리는 DB Layer 접근자는 JPA에서 `Repository`라 불리며, Interface로 생성한다.   
  단순히 interface 생성 후, 그 interface가 `JpaRepository<Entity Class, PK Type>`을 상속하면 기본적인 CRUD 메소드가 자동 생성된다.   
  __단, 반드시 Entity Class와 Entity Repository는 함께 위치해야 한다__. 이 둘은 아주 밀접한 관계이며, Entity Class는   
  기본 Repository없이는 제대로 역할을 할 수 없다.
* 나중에 프로젝트의 규모가 커져 도메인별로 프로젝트를 분리해야 한다면, 이 때 Entity Class와 Entity Repository는 함께   
  움직여야하므로 __domain package__ 에서 함께 관리한다.
<hr/>

<h2>Spring Data JPA Testcode의 작성</h2>

* `test` 폴더에 `domain.posts` 패키지를 생성하고, test class는 `PostsRepositoryTest`로 생성해보자.   
  이 클래스에서는 `save()`, `findAll()` 기능을 테스트해보자.
```java
package com.sangwoo.board.domain.posts;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PostsRepositoryTest {

    @Autowired
    PostsRepository postsRepository;

    @After
    public void cleanup() {
        postsRepository.deleteAll();
    }

    @Test
    public void get_all_boards() {
        //given
        String title = "Test title";
        String content = "Test content";

        postsRepository.save(Posts.builder().title(title).content(content).author("test@test.com").build());

        //when
        List<Posts> postsList = postsRepository.findAll();

        //then
        Posts posts = postsList.get(0);
        assertThat(posts.getTitle()).isEqualTo(title);
        assertThat(posts.getContent()).isEqualTo(content);
    }
}
```
* __@After__ 어노테이션은 JUnit에서 단위 테스트가 끝날 때 마다 수행되는 메소드를 지정한다.   
  보통은 배포 전 전체 테스트를 수행할 때 테스트간 데이터 침범을 막기 위해 사용하며, 여러 테스트가 동시에 수행되면   
  테스트용 DB인 H2에 데이터가 그대로 남아있어 다음 테스트 실행 시 테스트가 실패할 수 있다.   
  따라서 위에서는 DB의 모든 내용을 삭제하는 `deleteAll()` 메소드를 __@After__ 어노테이션이 붙은 메소드에서 호출했다.
* `postsRepository.save()` : 테이블 posts에 insert, update query를 수행한다.   
  id값이 있다면 update가, 없다면 insert가 실행된다.
* `postsRepository.findAll()` : 테이블 posts에 있는 모든 데이터를 조회하여 List 형태로 반환한다.

* 테스트 코드 실행 시, 실제 수행된 query문을 보고 싶다면 `src/main/resources/`에 `application.properties` 파일을 만들고,   
  아래와 같은 옵션을 추가하면 된다.
```text
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL5InnoDBDialect
```
* 위 코드는 JPA 실행 시 사용된 SQL 구문을 콘솔에 출력해주며, H2 DB를 사용하기에 기본적으로는 H2의 Query문이 출력되지만,   
  H2는 MySQL의 query를 수행해도 정상적으로 작동하기 때문에 2번째 줄을 추가하여 출력되는 query문을 MySQL 버전으로 변경한다.
<hr/>

<h2>등록, 수정, 조회 API의 생성</h2>

* API를 만들기 위해서는 총 3개의 클래스가 필요하다.
  * Request 데이터를 받을 Dto
  * API 요청을 받을 Controller
  * Transaction, Domain 기능간의 순서를 보장하는 Service

<hr/>

<h3>Sprng Web 계층</h3>

* Spring Web 계층은 아래와 같은 영역으로 구성된다.
* Web Layer
  * 흔히 사용하는 컨트롤러(@Controller)와 JSP 등의 View Template 영역이다.   
    이외에도 필터(@Filter), Interceptor, @ControllerAdvice 등 __외부 요청과 응답__ 에 대한 전반적인 영역이다.
* Service Layer
  * @Service에 사용되는 서비스 영억으로, 일반적으로 Controller와 Dao의 중간 영역에서 사용된다.   
    @Transactional이 사용되어야 하는 영역이기도 하다.
* Repository Layer
  * DB와 같이 데이터 저장소에 접근하는 영역이다. DAO 영역과 동일하다.
* Dtos
  * Dto(Data Transfer Object)는 __계층간에 데이터 교환을 위한 객체__ 를 의미하며, Dtos는 이들의 영역이다.   
    예를들어 View Template에서 사용될 객체나 Repository Layer에서 결과로 넘겨준 객체 등이 이에 해당한다.
* Domain Model
  * Domain이라 불리는 개발 대상을 모든 사람이 동일한 관점에서 이해할 수 있고 공유할 수 있도록 단순화시킨 것을 의미한다.
  * 예를들어 택시 앱이라면 배차, 탑승, 요금 등이 모두 도메인에 해당한다.
  * @Entity가 사용되는 영역 역시 domain 영역이라 한다.
  * 단, 무조건 DB의 테이블과 관계가 있어야하는 것은 아니다. VO객체들도 이 영역에 해당하기 때문이다.

* 위에서 본 5가지 Layer에서 Business Logic의 처리를 담당해야하는 부분은 __Domain__ 이다.

* 기존에 서비스로 처리하던 방식을 __Transaction Script__ 라 한다. 주문 취소 로직을 예로 들면 아래와 같다.
```java
@Transactional
public OrdersDto cancelOrder(int orderId) {

    OrdersDto order = ordersDao.selectOrders(orderId);
    BillingDto billing = billingDao.selectBilling(orderId);
    DeliveryDto delivery = deliveryDto.selectDelivery(orderId);

    String deliveryStatus = delivery.getStatus();

    if("IN_PROGRESS".equals(deliveryStatus)) {
        delivery.setStatus("CANCEL");
        deliveryDao.update(delivery);
    }

    order.setStatus("CANCEL");
    ordersDao.update(order);

    billing.setStatus("CANCEL");
    deliveryDao.update(billing);

    return order;
}
```
* 위에서는 모든 로직이 Service class 내부에서 처리된다. 따라서 서비스 계층이 무의미해지며, 객체란 단순히 데이터의 덩어리   
  역할만 하게 된다. 반면 이를 Domain영역에서 처리할 경우, 아래와 같은 코드가 될 수 있다.
```java
@Transactional
public Orders cancelOrder(int orderId) {

    Orders order = ordersRepository.findById(orderId);
    Billing billing = billingRepository.findByOrderId(orderId);
    Delivery delivery = deliveryRepository.findByOrderId(orderId);

    delivery.cancel();
    order.cancel();
    billing.cancel();

    return order;
}
```
* 위 코드에서 order, billing, delivery는 각자 본인의 취소 이벤트 처리를 하며, 서비스 메소드는 __Transaction과 domain간의 순서를 보장__   
  해주는 역할만 한다.
<hr/>

<h3>등록, 삭제, 수정 기능의 구현</h3>

* 먼저 게시글을 등록하는 기능을 구현해보자.
* `web` 패키지에 `PostsApiController`를, `web.dto` 패키지에 `PostsSaveRequestDto`를, 그리고 `service.posts` 패키지에   
  `PostsService`를 작성하자.
```java
// PostsApiController.java

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class PostsApiController {
    
    private final PostsService postsService;
    
    @PostMapping("/api/v1/posts")
    public Long save(@RequestBody PostsSaveRequestDto requestDto) {
        return postsService.save(requestDto);
    }
}
```

```java
// PostsService.java

import com.sangwoo.board.domain.posts.PostsRepository;
import com.sangwoo.board.web.dto.PostsSaveRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PostsService {
    
    private final PostsRepository postsRepository;
    
    @Transactional
    public Long save(PostsSaveRequestDto requestDto) {
        return postsRepository.save(requestDto.toEntity()).getId();
    }
}
```

* Spring에서는 Bean 객체를 주입 받는 방식이 @Autowired, setter, 생성자 3가지로 분류된다.   
  이 중 가장 권장되는 방식은 __생성자로 주입__ 받는 방식이다. 즉, 생성자로 Bean 객체를 받도록 하면 __@Autowired__ 와   
  동일한 효과를 볼 수 있다. 위 코드에서 생성자는 __final이 선언된 모든 필드를 인자값으로 하는 생성자를 lombok의__   
  __@RequiredArgsConstructor__ 가 대신 생성해준 것이다. 생성자를 직접 안쓰고 Lombok 어노테이션을 사용하는 이유는   
  해당 클래스의 의존성 관계가 변경될 때 마다 생성자 코드를 계속해서 수정하는 번거로움을 덜기 위해서이다.

```java
// PostsSaveRequestDto

import com.sangwoo.board.domain.posts.Posts;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostsSaveRequestDto {
    private String title;
    private String content;
    private String author;

    @Builder
    public PostsSaveRequestDto(String title, String content, String author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }

    public Posts toEntity() {
        return Posts.builder().title(title).content(content).author(author).build();
    }
}
```

* 위 코드에서 `PostsSaveRequestDto`는 Entity class와 거의 유사한 형태임에도 Dto 클래스를 추가로 생성했다.   
  __절대로 Entity Class를 Request/Response를 위한 클래스로 사용하면 안된다.__ 그 이유는 Entity class를 기준으로 테이블이 생성되고,   
  스키마가 변경되기 때문이다. 수많은 서비스 클래스나 비즈니스 로직들이 Entity class를 기준으로 동작한다. 만약 Entity Class가   
  변경되면 여러 클래스에 영향을 끼치지만, Request/Response용 Dto는 View를 위한 클래스이기에 자주 변경이 필요하다.   
  따라서 꼭 Entity Class와 Controller에서 사용할 Dto는 분리해서 따로 사용해야 한다.

* 이제 위 코드를 테스트하기 위해 아래 클래스를 `/test/java/` 하위의 `web` 폴더에 작성하자.
```java
package com.sangwoo.board.web;

import com.sangwoo.board.domain.posts.Posts;
import com.sangwoo.board.domain.posts.PostsRepository;
import com.sangwoo.board.web.dto.PostsSaveRequestDto;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostsApiControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PostsRepository postsRepository;

    @After
    public void tearDown() throws Exception {
        postsRepository.deleteAll();
    }

    @Test
    public void posts_is_saved() throws Exception {
        //given
        String title = "title";
        String content = "content";
        PostsSaveRequestDto requestDto = PostsSaveRequestDto.builder().title(title).content(content).author("author").build();

        String url = "http://localhost:" + port + "/api/v1/posts";

        //when
        ResponseEntity<Long> responseEntity = restTemplate.postForEntity(url, requestDto, Long.class);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isGreaterThan(0L);

        List<Posts> all = postsRepository.findAll();
        assertThat(all.get(0).getTitle()).isEqualTo(title);
        assertThat(all.get(0).getContent()).isEqualTo(content);
    }
}
```
* 위 코드에서는 Api Controller의 테스트 코드에서 __@WebMvcTest__ 어노테이션을 사용하지 않았다.   
  __@WebMvcTest__ 는 JPA 기능이 동작하지 않기 때문인데, 지금처럼 JPA기능까지 한번에 테스트할 경우에는   
  __@SpringBootTest__ 어노테이션과 `TestRestTemplate`를 사용하면 된다.
* `WebEnvironment.RANDOM_PORT`는 랜덤 포트번호로 test를 수행한다는 것이다.
* `TestRestTemplate#postForEntity()` 메소드는 인자로 주어진 url로 두번째 인자로 주어진 requestDto를   
  HTTP Method 중 POST 방식으로 전송하는 메소드이다.

* 다음으로는 수정 및 조회 기능을 구현해보자.
* `PostsApiController.java`에 아래 메소드를 추가한다.
```java
@PutMapping("/api/v1/posts/{id}")
public Long update(@PathVariable Long id, @RequestBody PostsUpdateRequestDto requestDto) {
    return postsService.update(id, requestDto);
}
    
@GetMapping("/api/v1/posts/{id}")
public PostsResponseDto findById(@PathVariable Long id) {
    return postsService.findById(id);
}
```

* 다음은 `PostsResponseDto`를 작성한다.
```java
import com.sangwoo.board.domain.posts.Posts;
import lombok.Getter;

@Getter
public class PostsResponseDto {
    
    private Long id;
    private String title;
    private String content;
    private String author;
    
    public PostsResponseDto(Posts entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.content = entity.getContent();
        this.author = entity.getAuthor();
    }
}
```
* 위 클래스는 Entity의 필드 중 일부만 사용하므로 생성자로 Entity 객체를 받아 필드에 값을 넣는다.   
  굳이 모든 필드를 가진 생성자가 필요하지 않으므로 Dto는 Entity를 생성자의 파라미터로 받아 처리한다.

* 아래 코드는 `PostsUpdateRequestDto.java`의 코드이다.
```java
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostsUpdateRequestDto {
    
    private String title;
    private String content;
    
    @Builder
    public PostsUpdateRequestDto(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
```

* 아래는 `Posts.java`에 추가한 메소드이다.
```java
public void update(String title, String content) {
    this.title = title;
    this.content = content;
}
```

* 다음으로는 `PostsService.java`에 `update()`와 `findById()` 메소드를 추가하자.
```java
@Transactional
public Long update(Long id, PostsUpdateRequestDto requestDto) {
    Posts posts = postsRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("해당 게시글이 존재하지 않습니다. id = " + id));
    return id;
}
    
public PostsResponseDto findById(Long id) {
    Posts entity = postsRepository.findById(id).orElseThrow(()->new IllegalArgumentException("해당 게시글이 존재하지 않습니다. id = " + id));
    return new PostsResponseDto(entity);
}
```
* 위 코드에서 신기한 점은 `update` 기능에서 __DB에 Query를 날리는 부분이 없다__ 는 점이다.   
  이는 JPA의 __영속성 컨텍스트__ 때문에 가능한 것인데, 영속성 컨텍스트란 __Entity를 영구 저장하는 환경__ 을 의미한다.   
  JPA의 핵심 내용은 __Entity가 영속성 컨텍스트에 포함되어 있는지의 유무로 갈린다__.
* JPA의 `EntityManager`가 활성화된 상태로(Spring Data JPA 사용 시 기본 설정) __Transaction 안에서 DB에서 데이터를 가져오면__ ,   
  이 데이터는 영속성 컨텍스트가 유지된 상태이다. 이 상태에서 해당 데이터의 값을 변경하면 __Transaction이 끝나는 시점에 해당 테이블에__   
  __변경된 내용을 반영__ 한다. 즉, Entity 객체의 값만 변경하면 __별도로 Update Query를 날릴 필요가 없다__.   
  이 개념을 __Dirty Checking__ 이라 한다.

* 이제 위에서 작성한 Update를 위한 Controller를 테스트하는 코드를 `PostsApiControllerTest.java`에 작성해보자.
```java
@Test
public void posts_is_updated() throws Exception {
    //given
    Posts savedPosts = postsRepository.save(Posts.builder().title("title2").content("content2").author("author").build());
    Long updateId = savedPosts.getId();
    String expectedTitle = "title2";
    String expectedContent = "content2";

    PostsUpdateRequestDto requestDto = PostsUpdateRequestDto.builder().title(expectedTitle).content(expectedContent).build();
    String url = "http://localhost:" + port + "/api/v1/posts/" + updateId;

    HttpEntity<PostsUpdateRequestDto> requestEntity = new HttpEntity<>(requestDto);

    //when
    ResponseEntity<Long> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Long.class);

    //then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getBody()).isGreaterThan(0L);

    List<Posts> all = postsRepository.findAll();
    assertThat(all.get(0).getTitle()).isEqualTo(expectedTitle);
    assertThat(all.get(0).getContent()).isEqualTo(expectedContent);
}
```
* 조회 기능은 실제로 Tomcat을 실행해서 확인해보자. 로컬 환경에서는 DB로 H2를 사용하는데, 이는 메모리에서 실행하기 때문에   
  직접 접근하려면 웹 콘솔을 사용해야 한다. 웹 콘솔을 위한 설정은 `application.properties`에 다음 설정을 추가하면 된다.   
  `spring.h2.console.enabled=true`
* 이 후 `Application`클래스의 `main()`을 실행하면, `http://localhost:8080/h2-console` 로 접속한 후, JDBC URL 값으로   
  `jdbc:h2:mem:testdb`로 접속하면 쿼리문을 직접 작성할 수 있다.
<hr/>

<h2>JPA Auditing으로 생성시간, 수정시간 자동화하기</h2>

* 보통 Entity에는 해당 데이터의 생성시간과 수정시간을 포함한다.   
  그러다보니 매번 DB에 insert하기 전, update하기 전에 날짜 데이터를 등록, 수정하는 코드가 남발하게 된다.   
  이 문제를 해결하기 위한 방법이 `JPA Auditing` 이다.
<hr/>

<h3>LocalDate의 사용</h3>

* `domain` 패키지에 `BaseTimeEntity` 클래스를 생성해보자.
```java
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {
    
    @CreatedDate
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    private LocalDateTime modifiedDate;
}
```
* __@MappedSuperclass__ : JPA Entity 클래스들이 `BaseTimeEntity`를 상속할 경우 필드들(createdDate, modifiedDate)도   
  컬럼으로 인식하도록 한다.
* __@EntityListeners__ : `BaseTimeEntity` 클래스에 Auditing 기능을 포함시킨다.
* __@CreatedDate__ : Entity가 생성되어 저장될 때 시간이 자동 저장된다.
* __@LastModifiedDate__ : 조회한 Entity의 값을 변경할 때 시간이 자동 저장된다.

* 다음으로 `Posts` 클래스가 `BaseTimeActivity`를 상속받도록 수정하고, JPA Auditing을 활성화할 수 있도록 `Application`   
  클래스에 첫 번째 어노테이션으로 __@EnableJpaAuditing__ 어노테이션을 추가하자.

* 마지막으로 `PostsRepositoryTest`에 테스트 메소드를 추가하고, 수행해보자.
```java
@Test
public void register_BaseTimeEntity() {
    //given
    LocalDateTime now = LocalDateTime.of(2020, 7, 16, 0,0,0);
    postsRepository.save(Posts.builder().title("title").content("content").author("author").build());

    //when
    List<Posts> postsList = postsRepository.findAll();
        
    //then
    Posts posts = postsList.get(0);

    System.out.println(">>>>>>Created Date : " + posts.getCreatedDate());
    System.out.println(">>>>>>Last Modified Date : " + posts.getModifiedDate());

    assertThat(posts.getCreatedDate()).isAfter(now);
    assertThat(posts.getModifiedDate()).isAfter(now);
}
```