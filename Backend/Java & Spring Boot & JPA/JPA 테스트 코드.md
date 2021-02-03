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

