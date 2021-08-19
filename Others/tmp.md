# TMP

<h2>Session, Cookie의 차이점과 활용</h2>

<h3>Cookie</h3>

- `Cookie`는 최대 4KB의 작은 파일로, 웹 서버가 클라이언트의 컴퓨터에 저장하는 것이다.  
  Cookie가 클라이언트의 컴퓨터에 세팅되면, 이후 모든 요청은 Cookie의 name, value를 반환한다.  
  Cookie는 오로지 생성된 도메인에서만 활용될 수 있다. 예를 들어, `a.example.com`에서 생신 Cookie는  
  `b.example.domain`에서는 읽을 수 없다.

- 많은 웹 사이트는 광고 등의 컨텐츠를 페이지에 함께 포함시킨다.  
  이때, 이 광고를 제공하는 도메인의 Cookie 또한 클라이언트의 컴퓨터에 저장될 수 있는데,  
  이를 Third Party Cookie라 한다. Cookie는 특정 사용자에 한정되게 사용되며, 다른 사용자는  
  다른 사용자의 Cookie를 볼 수 없다.

<h3>Session</h3>

- Session은 서버에 저장되는 전역 변수이다. 각 Session에는 고유 ID가 부여되고, 이 ID를 통해  
  Session에 저장된 값들을 읽어올 수 있다. Session이 생성될 때, 생성된 Session의 고유 ID를  
  가지고 있는 cookie가 클라이언트의 컴퓨터에 저장된다. 만약 클라이언트의 브라우저가 cookie를  
  지원하지 않는다면, Session의 고유 ID는 URL에 나타난다.

- Session은 Cookie에 비해 더 큰 양의 데이터를 저장할 수 있다.

- Session에 저장되어 있는 값들은 브라우저가 닫히면 자동으로 삭제된다.  
  따라서 세션에 있는 값들을 항상 유지하고 싶다면 데이터베이스 같은 저장소를 활용해야 한다.

<h3>Session, Cookie 사용 예제</h3>

- Http는 무상태 프로토콜(Stateless Protocol)이기에 Cookie를 활용하면 클라이언트의 컴퓨터에 작은 파일을  
  저장함으로써 애플리케이션의 상태를 추적할 수 있다. Cookie가 저장되는 위치는 브라우저에 따라 다르다.  
  Cookie는 주로 UX의 최적화를 위해 사용되는데, 사용자는 자신이 선호하는 것들을 클릭하고,  
  그 정보들이 Cookie에 저장됨으로서 개인 맞춤화를 제공할 수 있다.

- Session은 사용자의 고유 ID 등의 중요한 정보를 안전하게 관리하기 위해 사용된다.  
  서버 자체에 저장되니 외부로서의 접근을 막기 용이하다.  
  또한 Session을 이용하면 한 페이지에서 다른 페이지로 정보를 전달할 수 있다.  
  Cookie를 지원하지 않는 브라우저에 대해 Cookie를 대체하기 위해 사용되는 경우도 있다.

<hr/>

<h2>DI는 왜 필요할까?</h2>

- 의존성 주입을 사용하면 해당 클래스에 의존하는 클래스가 주입되는 클래스와 독립적이게 된다.  
  객체지향의 5대 원칙인 SOLID 원칙의 핵심은 코드의 재사용성을 높이는 것인데,  
  DI를 사용하면 객체간의 결합을 느슨하게 할 수 있기에 재사용성을 높일 수 있다.  
  또한 객체들의 책임을 분리할 수 있어서 단일 책임 원칙(SRP)를 따를 수 있게 된다.

- 클래스 A가 클래스 B의 기능을 사용해야 한다고 하자. 이때, B의 인터페이스를 만들어서 A에는  
  B의 인터페이스만 준다면, A와 B는 각각 B의 인스턴스에만 의존성을 가질 뿐, 더 이상의 깊은  
  의존성을 가지지 않는다. 이렇게 하여 느슨한 결합을 갖출 수 있다. A가 B의 인터페이스만  
  가지고 있다면, A가 인스턴스화될 때 B도 인스턴스화를 하여 A가 B를 사용하도록 한다.

- DI의 목적은 클래스간의 직접적인 의존성을 사용하는 부분과 객체를 생성하는 부분을 분리하여  
  없애는 것이다.

- DI에는 크게 4개의 클래스들이 있다.

  - 사용할 Service
  - Service를 사용할 Client
  - Client가 사용하며, Service가 구현하는 Interface
  - 실제 구현된 Service 인스턴스를 client에게 주입하는 Injector

- 만약 위 4개 중 interface를 없애고 구현체를 Client에게 넘긴다면 이는 의존성 역전 원칙을 깨며,  
  client가 service 구현체에 직접적인 의존성을 가지게 된다.

<h2>의존성 역전은 왜 필요할까?</h2>

- 객체 간의 의존성은 **하위 수준의 클래스** 가 **상위 수준의 클래스** 에 의존하도록 해야한다.  
  만약 **상위 수준의 클래스** 가 **하위 수준의 클래스** 에 의존하게 된다면, 상위 수준의 클래스를  
  재사용할 때 하위 수준의 클래스도 필요하기에 재사용하기가 어려워진다. 또한 이러한 상황에서는  
  하위 수준의 클래스가 변경되면, 상위 수준의 클래스 또한 변경된다.

- 이러한 상황에서 **추상화** 를 사용하여 두 클래스 사이에 추상화된 클래스를 두고,  
  상위 수준의 클래스는 추상화된 클래스에 의존하고, 하위 수준의 클래스는 추상화된 클래스를 구현한다고 하자.  
  이렇게 되면 하위 수준의 클래스의 변경으로 인해 상위 수준의 클래스가 영향 받는 것을 방지할 수 있고  
  상위 수준의 클래스를 재사용할 때 하위 수준의 클래스에 얽매이지 않고 다양한 컨텍스트에서 재사용할 수 있다.  
  즉 가장 중요한 것은 **두 클래스 사이의 추상화** 이다.

<h2>서비스 추상화는 왜 할까?</h2>

- DI의 4개 클래스 설명 + 의존성 역전과 중복

<h2>OOP에서의 객체의 책임, 역할, 협력 및 객체 간의 메시지가 무엇일까</h2>

- 책임: 역할의 묶음
- 역할: 해당 객체가 책임지고 수행해야할 행위 (동작)
- 협력: 객체지향 프로그램은 자율적인 객체들의 공동체이며, 이 객체들의 **협력** 을 통해 기능을 구현한다.  
  객체 사이의 협력을 위해 사용할 수 있는 유일한 커뮤니케이션 수단이 **메시지 전송** 이다.  
  객체는 다른 객체의 상세한 내부 구현에 직접 접근할 수 없기에 오직 메시지 전송을 통해서만  
  자신의 요청을 전달할 수 있다. 메시지를 수신한 객체는 **메소드를 실행** 하여 요청에 응답한다.
- 객체 간의 메시지: 객체 사이의 협력을 위한 유일한 커뮤니케이션 수단

<h2>layered architecture는 왜 필요할까</h2>

- 백엔드 코드 내에서는 주로 `Presentation` - `Business` - `Persistance` - `Database`의 총 4개 layer가 사용된다.

  - `Presentation Layer`: 클라이언트에게 view 또는 응답을 보내주는 layer
  - `Business Layer`: 비즈니스 로직이 위치하는 Layer
  - `Persistence Layer`: ORM 등 데이터베이스와 연관된 작업을 수행하는 layer
  - `Database Layer`: 데이터가 저장되는 곳

<h3>장점</h3>

- 각 layer가 뚜렷한 책임을 가지게 된다. 따라서 layer 단위의 단일 책임 원칙을 만족한다.
- 코드의 가독성도 올라가며 layer별로 테스트를 수행하기가 쉽다.

<h3>단점</h3>

- 하나의 요청이 불필요하게 많은 layer를 왔다가면서 데이터 오버헤드가 발생할 수 있다.
- layer가 많아질 수록 관리하기 힘들어진다.

<h2>Shell Script의 return vs 다른 언어의 return</h2>

- 아래 스크립트를 보자.

```sh
#!/bin/bash

function add()
{
	sum=$(($1 + $2))
	echo "Sum = $sum"
	return "A"
}

a=10
b=20

add $a $b
```

- 위 스크립트를 실행하면 아래의 오류가 뜬다.

```
line 7: return: A: numeric argument required
```

- 기본적으로 쉘 스크립트는 return value가 해당 shell script가 실행된 프로세스의 exit 코드 값이다.  
  0은 정상 종료, 그 외의 값(1~255)는 오류의 상태를 나타낸다.

<h2>JPA N + 1 문제 및 해결법</h2>

- 게시글(posts)와 댓글(comments) 테이블이 있다고 하자.  
   하나의 게시글에는 여러 개의 댓글들이 달릴 수 있으므로 posts와 comments의 관계는 1:N이 될 것이다.  
   이 관계를 JPA 로 표현해보자.

```kotlin
// Post.kt
@Entity
@Table(name = "posts")
data class Post(

  @Id
  @Column(name = "post_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  var id: Int? = null,

  @Column
  val title: String,

  @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL])
  val comments: List<Comment> = mutableListOf()
)

// Comment.kt
@Entity
@Table(name = "comments")
data class Comment(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "comment_id")
  val id: Int? = null,

  @Column
  val content: String,

  @ManyToOne
  @JoinColumn(name = "post_id")
  val post: Post
)
```

- 이제 아래와 같은 레포지토리 코드가 있다고 해보자.

```kt
@Repository
interface PostRepository : JpaRepository<Post, Int>
```

- 이때, 아래와 같이 모든 post들을 가져와서 각 post의 comments를 읽어오는 코드가 있다고 하자.

```kt
@Service
class TestService {
  private fun logAllComments(posts: List<Post>)  {
    for(post in posts) {
      post.comments.stream().forEach { c -> print(c.id!!)}
    }
  }

  @Transactional(readOnly = true)
  fun nPlusOneProblem(): BasicMessageDto {
    val posts = postRepository.findAll()
    logAllComments(posts)
    return BasicMessageDto("Done!")
  }
}
```

- posts 테이블에 4개의 데이터가 저장되어 있다고 할 때, 아래와 같은 SQL이 수행된다.

```sql
## posts에서 모든 데이터 가져옴
select p0.post_id as post_id1_1_, p0.title as title2_1_ from posts p0;

select c0.post_id as post_id3_0_0_, c0.comment_id as comment_1_0_0_, c0.comment_id as comment_1_0_1_, c0.content as content2_0_1_, c0.post_id as post_id3_0_1_ from comments c0 where c0.post_id=1;

select c0.post_id as post_id3_0_0_, c0.comment_id as comment_1_0_0_, c0.comment_id as comment_1_0_1_, c0.content as content2_0_1_, c0.post_id as post_id3_0_1_ from comments c0 where c0.post_id=2;

select c0.post_id as post_id3_0_0_, c0.comment_id as comment_1_0_0_, c0.comment_id as comment_1_0_1_, c0.content as content2_0_1_, c0.post_id as post_id3_0_1_ from comments c0 where c0.post_id=3;

select c0.post_id as post_id3_0_0_, c0.comment_id as comment_1_0_0_, c0.comment_id as comment_1_0_1_, c0.content as content2_0_1_, c0.post_id as post_id3_0_1_ from comments c0 where c0.post_id=4;
```

- 바로 위에서 N + 1 문제가 발생한 것이다. N은 각 post에 대한 comment들을 가져오는 쿼리(post가 4개이므로 N = 4),  
  1은 전체 posts를 가져오는 부분이다.

- `@OneToMany`의 fetch 속성 기본 값은 `FetchType.LAZY`이다. 하지만 fetch를 `FetchType.EAGER`로  
  설정해주는 것으로는 이 문제를 해결할 수 없다.

<h3>FETCH JOIN</h3>

- JPA의 `@Query` 어노테이션에 JPQL 쿼리를 직접 작성하여 해결할 수 있다.  
  우리의 경우, 아래 처럼 작성할 수 있다.

```kt
@Repository
interface PostRepository : JpaRepository<Post, Int>  {

  @Query("SELECT p FROM Post p JOIN FETCH p.comments")
  fun findAllFetchJoin(): List<Post>
}
```

- 위와 같이 가져올 엔티티에 대해 `JOIN FETCH` 키워드를 지정하면, 아래의 쿼리가 수행된다.

```sql
select
  p0.post_id as post_id1_1_0_, c1.comment_id as comment_1_0_1_,
  p0.title as title2_1_0_, c1.content as content2_0_1_,
  c1.post_id as post_id3_0_1_,
  c1.post_id as post_id3_0_0__,
  c1.comment_id as comment_1_0_0__
from posts p0
inner join comments c1 on p0.post_id=c1.post_id;
```

- 부모 테이블(posts)의 데이터가 4개, 자식 테이블(comments)의 데이터가 22개였을 때  
  `INNER JOIN`을 사용했으므로 위 쿼리의 결과는 22개가 된다.

- 위처럼 Fetch Join을 사용하면 INNER JOIN을 사용하여 한 번의 쿼리로  
  필요한 모든 정보를 가져오는 것을 확인할 수 있다.

<h3>@EntityGraph</h3>

- Repository 인터페이스의 메소드에 `@EntityGraph` 어노테이션을 적용해서 해결할 수도 있다.  
  이 어노테이션은 attributes 속성을 받는데, 이 속성값에 수행시 바로 가져올 필드를 지정하면 된다.

```kt
@Repository
interface PostRepository : JpaRepository<Post, Int>  {

  @EntityGraph(attributePaths = ["comments"])
  override fun findAll(): List<Post>
}
```

- 수행된 쿼리는 아래와 같다.

```sql
select
  p0.post_id as post_id1_1_0_,
  c1.comment_id as comment_1_0_1_,
  p0.title as title2_1_0_,
  c1.content as content2_0_1_,
  c1.post_id as post_id3_0_1_,
  c1.post_id as post_id3_0_0__,
  c1.comment_id as comment_1_0_0__
from posts p0
left outer join comments c1 on p0.post_id=c1.post_id
```

- 부모 테이블(posts)의 데이터가 5개, 자식 테이블(comments)의 데이터가 22개였을 때  
  부모 테이블의 데이터 중 자식 테이블에서 참조하지 않는 데이터가 1개 있다면,  
  `LEFT OUTER JOIN`을 사용했으므로 위 쿼리의 결과는 23개가 된다.

<h3>결론</h3>

- Fetch Join 방식은 `INNER JOIN`으로, `@EntityGraph` 방식은 `LEFT OUTER JOIN`을 사용함을 주의하자.

<hr/>

<h2>HTTP Header vs Body</h2>

- 메타데이터 또는 전체 애플리케이션에서 동일하게 사용될 수 있는 정보를 Header에 넣자.  
  예를 들어, JWT Token같이 전체 애플리케이션에서 사용되는 것이 있고, `Content-Type`과 같은  
  메타 데이터가 있을 것이다.

- 반면, Body는 특정 API에 정보 전달이 필요할 때 사용하자.

<h3>HEAD</h3>

- `HEAD` method는 특정 리소스를 `GET` method로 요청했을 때 돌아올 **헤더만을 요청** 한다.  
  `HEAD` method에 대한 응답은 본문을 가져서는 안되며, 본문이 존재하더라도 부시해야 한다.  
  그러나 `Content-Length` 처럼 본문 컨텐츠에 대한 정보를 가리키는 *개체 헤더*는 포함될 수 있다.

<h3>OPTIONS</h3>

- `OPTIONS` method는 **목표 리소스와의 통신 옵션**을 설명하기 위해 사용된다. 아래 요청 예시를 보자.

```
curl -X OPTIONS http://example.org -i
```

- 이에 대한 응답으로 아래와 같이 올 수 있다.

```
HTTP/1.1 200 OK
Allow: OPTIONS, GET, HEAD, POST
Cache-Control: max-age=604800
Date: Tue, 17 Aug 2021 11:45:00 GMT
Expires: Tue, 24 Aug 2021 11:45:00 GMT
Server: EOS (lax004/2813)
x-ec-custom-error: 1
Content-Length: 0
```

- `Allow` 헤더 부분을 통해 허용되는 HTTP Method를 확인할 수 있다.

- 이 메소드는 CORS의 Preflight Request에도 사용되는데, 서버에게 *사전 요청*을 보내 서버가 해당  
  파라미터들을 포함한 요청을 보내도 되는지에 대한 응답을 줄 수 있게 한다.  
  예를 들어, 아래 요청에서 `Access-Control-Request-Method`는 Preflight Request의 일부분으로,  
  서버에게 실제 요청이 전달될 때는 `POST` method를 사용할 것임을 명시한다.  
  `Access-Control-Request-Headers` 헤더는 서버에게 실제 요청이 전달될 때 `X-PINGOTHER`와  
  `Content-Type`이라는 커스텀 헤더와 함께 전달할 것임을 명시한다.  
  서버는 그럼 이러한 요구 사항들을 보고, 요청의 수락 여부를 결정할 수 있다.

```
OPTIONS /resources/post-here/ HTTP/1.1
Host: bar.other
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
Accept-Language: en-us,en;q=0.5
Accept-Encoding: gzip,deflate
Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7
Connection: keep-alive
Origin: http://foo.example
Access-Control-Request-Method: POST
Access-Control-Request-Headers: X-PINGOTHER, Content-Type
```

- 서버는 `Access-Control-Allow-Methods` 헤더로 `POST`, `GET`, `OPTIONS` method가 지원됨을 알려준다.

```
HTTP/1.1 200 OK
Date: Mon, 01 Dec 2008 01:15:39 GMT
Server: Apache/2.0.61 (Unix)
Access-Control-Allow-Origin: http://foo.example
Access-Control-Allow-Methods: POST, GET, OPTIONS
Access-Control-Allow-Headers: X-PINGOTHER, Content-Type
Access-Control-Max-Age: 86400
Vary: Accept-Encoding, Origin
Content-Encoding: gzip
Content-Length: 0
Keep-Alive: timeout=2, max=100
Connection: Keep-Alive
Content-Type: text/plain
```

<h2>단방향 vs 양방향 데이터 바인딩</h2>

<h2>ES6의 주요 기능</h2>

- <a href="http://es6-features.org/#Constants">Link</a>
- `const` keyword => constants
- `let` keyword => block-scoped variables
- arrow functions
- default parameter values
- spread operator
- rest parameter
- template literal
- object property shorthand
- object, array destructuring
- Iterator, For-Of operator
- generator functinos, iterator protocol
- Promise reject, resolve
