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

