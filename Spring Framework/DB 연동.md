DB 연동
======

<h2>JDBC의 프로그래밍의 단점을 보완하는 Spring</h2>

* JDBC API를 사용하면, DAO의 메소드마다 `Connection` 객체와 `PreparedStatement` 또는   
  `Statement` 객체에 대해 `close()` 메소드를 호출하는 코드가 반복된다.
* Spring의 JdbcTemplate 클래스는 이러한 구조적인 반복을 줄이기 위해 __템플릿 메소드 패턴__ 과   
  __전략 패턴__ 을 함께 엮은 클래스를 제공한다.
```java
// email로 특정 Member객체를 가져오는 코드
List<Member> results = jdbcTemplate.query(
    "select * from MEMBER where EMAIL=?",
    new RowMapper<Member>() {
        @Override
        public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
            Member member = new Member(rs.getString("EMAIL"),
            rs.getString("PASSWORD"), rs.getString("NAME"), rs.getString("REGDATE"));
            member.setId(rs.getLong("ID"));
            return member;
        }
    },
    email);
    return results.isEmpty() ? null : results.get(0);
)
```
* Spring을 사용하면 트랜잭션을 적용하고 싶은 메소드에 __@Transactional__ 어노테이션을 붙이면 된다.
```java
@Transactional
public void insert(Member member) {

    //..

}
```
<hr/>

* Spring JdbcTemplate를 사용하려면 다음 모듈을 `dependency`에 추가해야 한다.
```xml
<dependencies>
    <!-- 기존 dependency -->
    <dependency>
	    <groupId>org.springframework</groupId>
	    <artifactId>spring-jdbc</artifactId>
	    <version>5.0.2.RELEASE</version>
	</dependency>
	<dependency>
	    <groupId>org.apache.tomcat</groupId>
	    <artifactId>tomcat-jdbc</artifactId>
	    <version>8.5.27</version>
	</dependency>
	<dependency>
	    <groupId>mysql</groupId>
	    <artifactId>mysql-connector-java</artifactId>
	    <version>5.1.45</version>
	</dependency>
</dependencies>
```
* spring-jdbc : JdbcTemplate 등 JDBC 연동에 필요한 기능을 제공한다.
* tomcat-jdbc : DB Connection Pool 기능을 제공한다.
* mysql-connector-java : MySQL 연동에 필요한 JDBC 드라이버를 제공한다.
<hr/>

<h2>DataSource 설정</h2>

* members 데이터베이스에 MEMBERS 테이블이 있다고 가정하자.

* DataSource를 이용하면 다음 방식으로 `Connection` 객체를 구할 수 있다.
```java
Connection con = null;
try {
    // dataSource는 생성자나 설정 메소드를 이용하여 주입받는다.
    con = dataSource.getConnection();
}
```
* Spring이 제공하는 DB연동 기능은 DataSource를 사용해서 DB Connection을 구한다.   
  DB 연동에 사용할 DataSource를 Spring Bean으로 등록하고, DB 연동 기능을 구현한   
  Bean 객체는 DataSource를 주입받아 사용한다.

* Tomcat JDBC 모듈은 `javax.sql.DataSource`를 구현한 `DataSource`클래스를 제공한다.   
  이 클래스를 Spring Bean으로 등록해서 DataSource로 사용할 수 있다.
```java
@Configuration
public class DbConfig {
	
	@Bean(destroyMethod="close")
	public DataSource dataSource() {
		DataSource ds = new DataSource();
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setUrl("jdbc:mysql://localhost/members?characterEncoding=utf8");
		ds.setUsername("sangwoo");
		ds.setPassword("Tkddnfk0!");
		ds.setInitialSize(2);
		ds.setMaxActive(10);
		return ds;
	}
}
```
* 위 코드에서 __@Bean(destroyMethod=close)__ 에서, `close()` 메소드는 커넥션 풀에 보관된 `Connection` 객체를 닫는다.
<hr/>

<h3>Tomcat JDBC의 주요 프로퍼티</h3>

* Tomcat JDBC 모듈의 `org.apache.tomcat.jdbc.pool.DataSource` 클래스는 커넥션 풀 기능을 제공하는   
  DataSource 구현 클래스이다. DataSource 클래스는 커넥션을 몇 개 만들지 지정할 수 있는 메소드를 제공한다.

<table>
    <tr>
        <td>setInitialSize(int)</td>
        <td>커넥션 풀을 초기화할 때 생성할 초기 커넥션 개수를 지정한다. (기본값 : 10)</td>
    </tr>
    <tr>
        <td>setMaxActive(int)</td>
        <td>커넥션 풀에서 가져올 수 있는 최대 커넥션 개수를 지정한다. (기본값 : 100)</td>
    </tr>
    <tr>
        <td>setMaxIdle(int)</td>
        <td>커넥션 풀에 유지할 수 있는 최대 커넥션 개수를 지정한다. (기본값 : maxActive)</td>
    </tr>
    <tr>
        <td>setMinIdle(int)</td>
        <td>커넥션 풀에 유지할 최소 커넥션 개수를 지정한다. (기본값 : initialSize)</td>
    </tr>
    <tr>
        <td>setMaxWait(int)</td>
        <td>커넥션 풀에서 커넥션을 가져올 때 대기할 최대 시간을 지정한다. (기본값 : 30000ms)</td>
    </tr>
    <tr>
        <td>setMaxAge(long)</td>
        <td>최초 커넥션 연결 후 커넥션의 최대 유효 시간을 지정한다. (기본값 : 0ms, 유효 시간이 없음을 의미)</td>
    </tr>
    <tr>
        <td>setTestWhileIdle(boolean)</td>
        <td>커넥션이 풀에 유휴 상태로 있는 동안에 검사할지의 여부를 지정한다. (기본값 : false)</td>
    </tr>
    <tr>
        <td>setMinEvictableTimeMillis(int)</td>
        <td>커넥션 풀에 유휴 상태로 유지할 최소 시간을 ms 단위로 지정한다. testWhileIdle이 true이면 유휴 시간이 이 값을 초과한 커넥션을
            풀에서 제거한다. (기본값 : 60000ms)</td>
    </tr>
    <tr>
        <td>setTimeBetweenEvictionRunsMillis(int)</td>
        <td>커넥션 풀의 유휴 커넥션을 검사할 주기를 ms 단위로 지정한다. (기본값 : 5000ms, 1초 이하로 설정 불가)</td>
    </tr>
</table>

* Connection Pool은 __커넥션을 생성하고 유지__ 한다. 커넥션 풀에 커넥션을 요청하면, 해당 커넥션은   
  __활성(Active)__ 상태가 되며, 해당 커넥션을 커넥션 풀에 반환하면 __유휴(Idle)__ 상태가 된다.   
  `DataSource.getConnection()`을 실행하면 커넥션 풀에서 커넥션을 가져와 커넥션이 Active 상태가 된다.
```java
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

public class DbQuery {
	
	private DataSource dataSource;
	
	public DbQuery(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public int count() {
		Connection con = null;
		int result = 0;
		try {
			con = dataSource.getConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select count(*) from MEMBER");
			rs.next();
			result = rs.getInt(1);
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				con.close();
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}
```
* 위 코드를 실행하면, DataSource에서 커넥션을 구하는데, 이 때 풀에서 커넥션을 가져온다.   
  이 시점에서 커넥션 `con`은 활성 상태이며, `con.close()`를 호출하여 커넥션을 종료하면,   
  실제 커넥션을 끊지 않고 풀에 반환한다. 풀에 반환된 커넥션은 다시 유휴 상태가 된다.

* 커넥션 풀을 사용하는 이유는 __성능__ 때문이다. 매번 새로운 커넥션을 생성하면 그때마다   
  연결 시간이 소모된다. 커넥션 풀을 사용하면 __미리 커넥션을 생성했다가 필요할 때에 커넥션을__   
  __꺼내 쓰므로 커넥션을 구하는 시간이 줄어 전체 응답시간도 짧아진다.__   
  그래서 커넥션 풀을 초기화할 때 최소 수준의 커넥션은 미리 생성하는 것이 좋다.   
  이때 생성할 커넥션 개수를 __initialSize__ 로 지정한다.

* 커넥션 풀에 생성된 커넥션은 지속적으로 재사용된다. 하지만 DBMS의 설정에 따라 일정 시간 내에   
  쿼리를 실행하지 않으면 연결이 끊기는 경우도 있다. 만약 이렇게 커넥션이 끊긴 상태에서 해당   
  커넥션을 풀에서 가져와 사용하면 Exception이 발생한다. 이를 방지하기 위해서는 커넥션 풀의   
  커넥션이 유효한지를 주기적으로 검사해야하는데, 이와 관련된 속성이 `minEvictableIdleTimeMillis`,   
  `timeBetweenEvictionRunsMillis`, `testWhileIdle` 이다.

  * 만약 10초 주기로 유휴 커넥션이 유효한지를 검사하고 최소 유휴 시간을 3분으로 지정한다면 다음과 같이한다.
```java
@Bean(destroyMethod="close")
public DataSource dataSource() {

    DataSource ds = new DataSource();

    //...

    ds.setTestWhileIdle(true);  // 유휴 커넥션 검사
    ds.setMinEvictableTimeMillis(1000 * 60 * 3);  // 최소 유휴 시간 3분
    ds.setTimeBetweenEvictionRunsMillis(1000 * 10);  // 10초 주기
    return ds;
}
```
<hr/>

<h2>JdbcTemplate을 이용한 Query 실행</h2>

* Spring을 사용하면 `DataSource`나 `Connection`, `Statement`, `ResultSet`을 직접 사용하지 않고   
  `JdbcTemplate`을 이용해서 편리하게 Query를 실행할 수 있다.
<hr/>

<h3>JdbcTemplate 생성하기</h3>

* p.190