<h1>테스트 코드 속도 향상시키기</h1>

* 기존 테스트 코드를 위한 Spring 설정 파일인 `application-test.properties`는 아래와 같았다.
```properties
spring.jpa.show-sql=false
server.tomcat.uri-encoding=UTF-8
spring.jpa.database-platform=org.hibernate.dialect.MySQL5InnoDBDialect
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.open-in-view=false
spring.datasource.url=${TEST_DATASOURCE_URL}
spring.datasource.username=${DATASOURCE_USERNAME}
spring.datasource.password=${DATASOURCE_PASSWORD}
spring.jpa.hibernate.ddl-auto=none
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL5InnoDBDialect
```

* 기존에는 테스트 코드를 수행하기 위해 AWS RDS로 올린 MariaDB에 테스트용 DB를 생성하고,   
  그 위에서 CRUD 테스트를 진행했다. 여기서 문제점은 실제 클라우드상의 MariaDB에 접속하여   
  CRUD를 진행했기 때문에 속도가 매우 느릴 뿐더러 쓸데없는 리소스를 매우 낭비하는 것이었다.

* 이를 해결하기 위해 `H2 Database`를 도입해야겠다고 생각이 들었다.   
  MariaDB, MySQL은 서버 기반의 데이터베이스이기 때문에 당연히 웹 애플리케이션과는 별도의   
  프로세스로 작동한다. 반면 H2 Database는 경량화된 데이터베이스로, 인-메모리 상에서 동작하거나   
  디스크 공간, 또는 애플리케이션의 프로세스에 embed하여 사용할 수 있는 데이터베이스이다.
<hr/>

* 우선 h2 database를 사용하기 위해 `build.gradle`에 의존성을 추가해준다.   
  나는 테스트에만 사용할 것이므로 `testImplementation`을 사용했다.
```gradle
// Other gradle settings
dependencies {
    // Other dependencies
    testImplementation('com.h2database.h2')
}
```

* 다음으로는 h2 database를 사용하기 위해 `application-test.properties`를 아래와 같이 수정했다.
```properties
# src/test/resources/application-test.properties

spring.jpa.show-sql=false
server.tomcat.uri-encoding=UTF-8
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.open-in-view=false
spring.datasource.url=jdbc:h2:mem:test
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create
```

* `spring.datasource.driver-class-name`이 h2 패키지가 제공하는 것을 사용하도록 했으며   
  `spring.jpa.properties.hibernate.dialect`도 마찬가지이다.   
  또한 `spring.datasource.url`을 `jdbc:h2:mem:test`로 지정했다.

* 참고로 당연한 말이지만 `spring.jpa.hibernate.ddl-auto`를 create로 지정하지 않으면   
  기존에 테이블이 존재하지 않기에 테스트 코드는 모두 실패하게 된다.

* 마지막으로 모든 테스트 코드의 클래스들이 상속하는 슈퍼 클래스이다.
```java
import com.banchango.domain.users.UsersRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public abstract class ApiIntegrationTest {

    @Autowired
    protected UsersRepository usersRepository;

    // More autowired repositories.

    @Autowired
    protected TestRestTemplate restTemplate;

    @Before
    public void setup() {
        // Code to run before each test method is called.
    }

    @After
    public void release() {
        // Code to run after each test method is called.
    }
}
```
<hr/>

<h2>결론</h2>

* 이제 모든 테스트 케이스에서 DB 접근을 H2에서 하기 때문에 테스트 코드의 실행속도가 매우 빨라졌다.   

* 기존에 실제 클라우드상의 DB에 접근하는 테스트 코드는 실행하는데에만 20분이 걸렸는데(Github Action),   
  이제 테스트 코드가 검증이 끝날 때까지 1분이 채 걸리지 않는다.   
  ~~테스트코드가 돌아가지 않는줄 알고 일부러 잘못된 Assertion을 넣어보기도 했다.~~
<hr/>