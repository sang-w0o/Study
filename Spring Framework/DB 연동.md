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
    }, email);
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

* 먼저 `JdbcTemplate` 객체를 생성하자.
```java
public class MemberDao {
	
	private JdbcTemplate jdbcTemplate;
	
	public MemberDao(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
}
```
* `JdbcTemplate` 객체를 생성하려면 `DataSource`객체를 생성자에 전달하면 된다.

* 다음으로는 Spring 설정에 `MemberDao` Bean을 추가하자.
```java
@Configuration
public class AppCtx {

    @Bean(destroyMethod = "close")
	public DataSource dataSource() {
		DataSource ds = new DataSource();
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setUrl("jdbc:mysql://localhost/sangwoo?characterEncoding=utf8");
		ds.setUsername("sangwoo");
		ds.setPassword("Tkddnfk0!");
		ds.setInitialSize(2);
		ds.setMaxActive(10);
		return ds;
	}

    @Bean
    public MemberDao memberDao() {
        return new MemberDao(dataSource());
    }

    //..

}
```
<hr/>

<h3>JdbcTemplate을 이용한 조회 Query 실행</h3>

* `JdbcTemplate` 클래스는 `SELECT` 쿼리 실행을 위한 `query()` 메소드를 제공한다.   
  자주 사용되는 쿼리 메소드는 다음과 같다.
  * `List<T> query(String sql, RowMapper<T> rowMapper)`
  * `List<T> query(String sql, Object[] args, RowMapper<T> rowMapper)`
  * `List<T> query(String sql, RowMapper<T> rowMapper, Object... args)`

* `query()` 메소드는 sql 파라미터로 전달받은 쿼리를 실행하고, `RowMapper`를 이용해서 `ResultSet`의   
  결과를 Java 객체로 변환한다. sql 파라미터가 아래와 같이 index 기반 파라미터를 가진 쿼리이면   
  __args__ 파라미터를 이용하여 각 index 파라미터의 값을 지정한다.
```sql
select * from member where email = ?
```

* __쿼리 실행 결과를 자바 객체로 변환할 때 사용__ 하는 `RowMapper` 인터페이스는 다음과 같다.
```java
package org.springframework.jdbc.core;

public interface RowMapper<T> {
    T mapRow(ResultSet rs, int rowNum) throws SQLException;
}
```
* `RowMapper`의 `mapRow()` 메소드는 __SQL 실행 결과로 구한__ `ResultSet` __에서 한 행의 데이터를 읽어와__   
  ___자바 객체로 변환하는 매퍼 기능__ 을 구현한다.
```java
public class MemberDao {

    //...

    public Member selectByEmail(String email) {
        List<Member> results = jdbcTemplate.query(
            "SELECT * FROM MEMBER where EMAIL=?",
            new RowMapper<Member>() {
                @Override
                public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Member member = new Member(rs.getString("EMAIL"), /* 생략 */);
                    return member;
                }
            }, email // email은 Query문의 ? 에 들어갈 값이다.
        );
    }
}
```
* 만약 `RowMapper`를 동일하게 구현해야 하는 곳이 많다면, 따로 클래스로 빼도 된다.
```java
public class MemberRowMapper implements RowMapper<Member> {
    @Override
    public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
        Member member = new Member(rs.getString("EMAIL"), /* 생략 */);
        return member;
    }
}

// Use MemberRowMapper in MemberDao.selectByEmail()
List<Member> results = jdbcTemplate.query(
    "SELECT * FROM MEMBER where EMAIL=?",
    new MemberRowMapper(), email);
)
```
* 아래는 `MemberDao` 안에 있는 모든 회원 정보를 읽어오는 메소드이다.
```java
public List<Member> selectAll() {
		
	List<Member> results = jdbcTemplate.query("SELECT * FROM MEMBER",
		new RowMapper<Member>() {
			@Override
			public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
				Member member = new Member(rs.getString("EMAIL"), rs.getString("PASSWORD"), rs.getString("NAME"), rs.getTimestamp("REGDATE").toLocalDateTime());
				member.setId(rs.getLong("ID"));
				return member;
			}
		});
	return results;
}
```
<hr/>

<h3>결과 행이 1개인 경우 사용할 수 있는 queryForObject() 메소드</h3>

* 예를 들어 MEMBER 테이블의 전체 행의 개수를 구하는 쿼리문이 있다고 하자. 이 쿼리문의 결과 행은 1개일 것이다.   
  이럴 때, `queryForObject()` 메소드를 사용할 수 있다.
```java
// Get total count of rows in MEMBERS table.
public int count() {
    Integer count = jdbcTemplate.queryForObject(
        "select count(*) from MEMBER", Integer.class);
        return count;
    )
}
```
* `queryForObject()` 메소드도 쿼리문에 `?`가 있다면, 가변 인자로 뒤에 지정하면 된다.
```java
public double getAvg() {

    Double avg = jdbcTemplate.queryForObject(
        "SELECT avg(height) FROM FURNITURE WHERE TYPE=? AND STATUE=?", Double.class,
        100, "S");
    return avg;
}
```
* 만약 특정 ID를 갖는 회원 칼럼을 `queryForObject()`로 읽어오고 싶다면 다음과 같이 하면 된다.
```java
public Member seleteById(String id) {

    Member member = jdbcTemplate.queryForObject(
        "SELECT * FROM MEMBER WHERE ID=?",
        new RowMapper<Member>() {
            @Override
            public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
                Member member = new Member(rs.getString("EMAIL"), /*생략*/);
                member.setId(rs.getLong("ID"));
                return member;
            }
        }
    ,id);
}
```
* 위 코드 예시들에서 알 수 있듯이 주요 `queryForObject` 메소드는 다음과 같이 오버로딩 되어 있다.
  * `T queryForObject(String sql, Class<T> requiredType)`
  * `T queryForObject(String sql, Class<T> requiredType, Object...args)`
  * `T queryForObject(String sql, RowMapper<T> rowMapper)`
  * `T queryForObject(String sql, RowMapper<T> rowMapper, Object...args)`

* `queryForObject()` 메소드를 사용하려면 __쿼리 실행 결과가 반드시 한 행이어야 한다__.   
  만약 쿼리 실행 결과 행이 없거나 2개 이상이면 `IncorrectResultSizeDataAccessException` 이 발생한다.   
  행의 개수가 0이면 `EmptyResultDataAccessException` 이 발생한다.
<hr/>

<h3>JdbcTemplate을 이용한 변경 쿼리 실행</h3>

* INSERT, UPDATE, DELETE 쿼리는 `update()` 메소드를 사용한다.
  * `int update(String sql)`
  * `int update(String sql, Object...args)`
* `update()` 메소드는 쿼리 실행 결과로 __변경된 행의 개수를 반환__ 한다.
```java
public void update(Member member) {
		jdbcTemplate.update(
			"UPDATE MEMBER set NAME=?, PASSWORD=? where EMAIL=?", member.getName(), member.getPassword(), member.getEmail());
}
```
<hr/>

<h3>PreparedStatementCreator를 이용한 쿼리 실행</h3>

* 지금까지 작성한 코드는 다음과 같이 쿼리에서 사용할 값을 인자로 전달했다.
```java
public void update(Member member) {
		jdbcTemplate.update(
			"UPDATE MEMBER set NAME=?, PASSWORD=? where EMAIL=?", member.getName(), member.getPassword(), member.getEmail());
}
```
* 위 코드는 각 index 파라미터에 알맞은 인자를 전달했다.
* `PreparedStatement`의 `set` 메소드를 사용해서 직접 index 파라미터의 값을 설정해야할 때도 있다. 이 경우   
  `PreparedStatementCreator`를 인자로 받는 메소드를 이용해서 직접 `PreparedStatement`를 생성하고 설정해야한다.

* `PreparedStatementCreator` 인터페이스는 다음과 같다.
```java
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementCreator {
    PreparedStatement createPreparedStatement(Connection con) throws SQLException;
}
```
* `PreparedStatementCreator` 인터페이스의 `createPreparedStatement()` 메소드는 `Connection` 객체를   
  인자로 받는다. 다음은 `PreparedStatementCreator`의 예제 코드이다.
```java
jdbcTemplate.update(new PreparedStatementCreator() {
    @Override
    public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement pstmt = con.prepareStatement(
            "INSERT INTO MEMBER(EMAIL, PASSWORD, NAME, REGDATE) values(?,?,?,?)");
        pstmt.setString(1, member.getEmail());
        pstmt.setString(2, member.getPassword());
        pstmt.setString(3, member.getName());
        pstmt.setTimestamp(4, Timestamp.valueOf(member.getRegisterDateTime()));
        return pstmt;
    }
});
```
* `JdbcTemplate` 클래스가 제공하는 메소드 중에서 `PreparedStatementCreator` 인터페이스를   
  파라미터로 갖는 메소드는 다음과 같다.
  * `List<T> query(PreparedStatementCreator psc, RowMapper<T> rowMapper)`
  * `int update(PreparedStatementCreator psc)`
  * `int update(PreparedStatementCreator psc, KeyHolder generatedKeyHolder)`
<hr/>

<h3>INSERT 쿼리 실행 시 KeyHolder를 이용하여 자동 생성 Key값 구하기</h3>

* MySQL의 `AUTO_INCREMENT` 컬럼은 행이 추가되면 자동으로 값이 할당되는 칼럼으로,   
  Primary Key 칼럼에 사용된다. 이 경우, MEMBER 테이블의 ID 컬럼이 AUTO_INCREMENT에 해당한다.

* `JdbcTemplate`는 자동으로 생성된 Key값을 `KeyHolder` 객체를 통해 구할 수 있게 해준다.
```java
public void insert(final Member member) {
		
	KeyHolder keyHolder = new GeneratedKeyHolder();
	jdbcTemplate.update(new PreparedStatementCreator() {
		@Override
		public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
			PreparedStatement pstmt = con.prepareStatement(
				"INSERT INTO MEMBER(EMAIL, PASSWORD, NAME, REGDATE) VALUES(?,?,?,?)", new String[] {"ID"});
			pstmt.setString(1, member.getEmail());
			pstmt.setString(2,member.getPassword());
			pstmt.setString(3, member.getName());
			pstmt.setTimestamp(4, Timestamp.valueOf(member.getRegisterDateTime()));
			return pstmt;
		}
	}, keyHolder);
	Number keyValue = keyHolder.getKey();
	member.setId(keyValue.longValue());
}
```
* `KeyHolder` 객체에 보관된 Key값은 `Number` 타입을 반환하는 `getKey()` 메소드를 통해 가져올 수 있다.
<hr/>

<h2>DB 연동 과정에서 발생 가능한 Exception</h2>

* DB 연동 과정에서는 다양한 Exception이 발생할 수 있다.

* `SQLException` : DB 과정에서 발생하는 Exception 객체들의 부모 객체
* `CannotGetJdbcConnectionException` : MySQL 서버에 연결할 권한이 없는 경우 및   
  DBMS에 연결할 수 없을 때의 예외
* `BadSqlGrammerException` : SQL 구문에 오류가 있을 때 발생하는 예외

* 위 오류들을 콘솔에서 확인하기 위해 `java.lang.Exception.printStackTrace()` 메소드를 `try-catch`   
  절에 호출하는 것이 좋다.
<hr/>

<h2>Spring의 Exception 변환 처리</h2>

* SQL 문법이 잘못됐을 때 발생한 메시지를 보자.
```text
org.springframework.jdbc.BadSqlGrammerException : ..생략
..생략
Caused by : com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException : ..생략
```
* `BadSqlGrammerException`을 발생한 메소드는 `JdbcTemplate` 클래스의 `update()` 메소드이다.   
  `JdbcTemplate`의 `update()` 메소드는 DB 연동을 위해 JDBC API를 사용하는데, 이를 사용하는 과정에서   
  `SQLException`이 발생하면 이 예외를 알맞은 `DataAccessException`으로 변환해서 발생한다.
```java
try {

    //..JDBC 사용 코드

} catch(SQLException e) {
    throw convertSqlToDataException(ex);
}
```
* 예를 들어 MySQL용 JDBC 드라이버는 SQL 문법이 잘못된 경우 `SQLException`을 상속하는   
  `MySQLSyntaxErrorException`을 발생하는데, `JdbcTemplate`는 이 예외를   
  `DataAccessException`을 상속받은 `BadSqlGrammerException`으로 변환한다.

* `DataAccessException`은 Spring이 제공하는 예외 타입으로, 데이터 연결에 문제가 있을 때 Spring 모듈이 발생시킨다.

* 이와 같이 `SQLException`을 그대로 전파하지 않고 `DataAccessException`으로 변환하는 이유는   
  연동 기술에 관계없이 동일하게 예외를 처리할 수 있도록 하기 위함이다. Spring은 JDBC 뿐만 아니라   
  JPA, Hibernate 등에 대한 연동을 지원하고, MyBatis는 자체적으로 Spring연동 기능을 제공한다.   
  이렇게 다양한 연동 기술에 따라 발생하는 예외를 동일하게 처리하기 위해 변환하는 것이다.

* `DataAccessException`은 `RuntimeException`으로, JDBC를 직접 이용하면 `try-catch`로 예외를   
  처리해야 하거나 thows로 예외를 전가해야 하는데, `DataAccessException`은 `RuntimeException`   
  이므로 __필요한 경우에만 예외를 처리하면 된다__.
```java
// JDBC를 직접 사용하면 SQLException을 반드시 알맞게 처리해야 한다.
try {
    pstmt = con.prepareStatement(sql);
    //...
} catch(SQLException e) {
    // SQLException에 알맞은 처리
}

// Spring을 사용하면 DataAccessException을 필요한 경우에만
// try-catch로 처리하면 된다.
jdbcTemplate.update(sql, param1);
```
<hr/>

<h2>Transaction 처리</h2>

* 두 개 이상의 쿼리를 한 작업으로 실행해야 할 때, 다양한 오류를 방지하기 위해 __Transaction__ 을 사용한다.   
  Transaction은 __여러 쿼리를 논리적으로 하나의 작업으로 묶어준다. 한 Transaction으로 묶인 여러 쿼리들 중__   
  __하나라도 실패하면 전체 쿼리를 실패로 간주하고 실패 이전에 실행한 쿼리를 취소__ 한다.   
  이렇게 쿼리 실행결과를 취소하고 DB를 기존 상태로 되돌리는 것을 __rollback__ 이라 한다. 반면 Transaction으로   
  묶인 모든 쿼리가 성공해서 결과를 실제로 DB에 반영하는 것을 __commit__ 이라 한다.

* Transaction을 시작하면 Transaction을 commit하거나 rollback할 때 까지 실행한 쿼리들이 __하나의 작업 단위__   
  가 된다. JDBC는 `Connection` 객체의 `setAutoCommit(false)`를 이용하여 Transaction을 시작하고   
  `commit()`와 `rollback()`를 이용하여 Transaction을 반영하거나 취소한다.
```java
Connection con = null;
try {
    con = DriverManager.getConnection(jdbcUrl, user, pw);
    con.setAutoCommit(false);  // Transaction 범위 시작
    
    //.. 쿼리 실행

    con.commit();  // Transaction이 예외 없이 성공하면 commit한다.
} catch(SQLException e) {
    if(con != null) {
        // 쿼리 실행 중 예외가 발생하면 catch절로 오며,
        // 아래와 같이 rollback한다.
        try {con.rollback()} catch(SQLException e) {}
    }
} finally {
    if(con != null) {
        try {con.close();} catch(SQLException e) {}
    }
}
```
* Spring이 제공하는 Transaction기능을 사용하면 중복이 없고 간단한 코드로 Transaction범위를 지정할 수 있다.
<hr/>

<h3>@Transactional을 이용한 Transaction 처리</h3>

* Spring이 제공하는 __@Transactional__ 어노테이션을 사용하면 Transaction범위를 쉽게 지정할 수 있다.   
  아래와 같이 Transaction범위에서 실행하고 싶은 메소드에 어노테이션만 붙이면 된다.
```java

@Transactional
public void changePassword(String email, String oldpw, String newpw) {
    Member member = memberDao.selectByEmail(email);
    if(member == null) {
        throw new MemberNotFoundException();
    }
    member.changePassword(oldpw, newpw);
    memberDao.update(member);
}
```
* 위 코드에서는 __@Transactional__ 어노테이션이 붙은 `changePassword()` 메소드를 동일한   
  Transaction 범위에서 실행한다. 따라서 `memberDao.selectByEmail()`에서 실행하는 쿼리와   
  `memberDao.update()`에서 실행하는 쿼리는 한 Transaction으로 묶인다.

* __@Transactional__ 어노테이션의 동작을 위해선 다음 두 가지 내용을 Spring 설정 클래스에 추가해야한다.
  * PlatformTransactionManager Bean 설정
  * @Transactional 어노테이션 활성화 설정
```java
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class AppCtx {

    @Bean(destroyMethod="close")
    public DataSource dataSource() {

        //..

    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        DataSourceTransactionManager tm = new DataSourceTransactionManager();
        tm.setDataSource(dataSource());
        return tm;
    }
}
```
* `PlatformTransactionManager`는 Spring이 제공하는 Transaction 매니저 인터페이스이다. Spring은   
  구현 기술에 관계없이 동일한 방식으로 Transaction을 처리하기 위해 이 인터페이스를 사용한다.   
  JDBC는 `DataSourceTransactionManager` 클래스를 `PlatformTransactionManager`로 사용한다.   
  그리고 dataSource 프로퍼티를 이용하여 Transaction연동에 사용할 `DataSource` 객체를 지정한다.

* __@EnableTransactionManagement__ 어노테이션은 __@Transactional__ 어노테이션이 붙은 메소드를   
  Transaction범위에서 실행하는 기능을 활성화한다.
<hr/>

<h3>@Transactional과 Proxy</h3>

* Transaction도 AOP의 Aspect, 즉 공통 기능 중 하나이다. Spring은 __@Transactional__ 어노테이션을   
  이용해서 Transaction을 처리하기 위해 내부적으로 AOP를 사용한다. 즉, Transaction의 처리도   
  Proxy를 통해서 이루어진다는 것이다.

* __@Transactional__ 어노테이션을 적용하기 위해 __@EnableTransactionManagement__ 어노테이션을 사용하면   
  Spring은 __@Transactional__ 어노테이션이 적용된 Bean 객체를 찾아 __알맞은 Proxy 객체를 생성__ 한다.

```java
public class ChangePasswordService {
	
	private MemberDao memberDao;
	
	@Transactional
	public void changePassword(String email, String oldPwd, String newPwd) throws Exception {
		Member member = memberDao.selectByEmail(email);
		if(member == null)
			throw new MemberNotFoundException();
		
		member.changePassword(oldPwd, newPwd);
		
		memberDao.update(member);
	}
	
	public void setMemberDao(MemberDao memberDao) {
		this.memberDao = memberDao;
	}
}
```
* `ChangePasswordService` 클래스의 메소드에 __@Transactional__ 어노테이션이 적용되어 있으므로   
  Spring은 Transaction기능을 적용한 Proxy 객체를 생성한다. `main` 메소드가 있는 클래스에서   
  `getBean("changePwdSvc", ChangePasswordService.class)`를 실행하면, `ChangePasswordService`   
  객체 대신에 Transaction처리를 위해 생성한 Proxy 객체가 반환된다.   
  이 Proxy 객체는 __@Transactional__ 어노테이션이 붙은 메소드를 호출하면 `PlatformTransactionManager`   
  를 사용해서 Transaction을 시작한다. Transaction을 시작한 후 실제 객체의 메소드를 호출하고, 성공적으로   
  실행되면 Transaction을 commit 한다.
<hr/>

<h3>@Transactional 적용 메소드의 rollback 처리</h3>

* Commit을 수행하는 주체가 Proxy 객체인 것 처럼 Rollback을 처리하는 주체 또한 Proxy 객체이다.   
  __@Transactional__ 을 처리하기 위한 Proxy 객체는 예외가 발생했을 때, 즉 `RuntimeException`이   
  발생하면 Transaction을 rollback처리 한다.

```java
try {
    cps.changePassword("robbyra@gmail.com", "1234", "0000");
    System.out.println("Password changed.");
} catch(MemberNotFoundException e) {
    System.out.println("Member not found.");
} catch(WrongIdPasswordException e) {
    System.out.println("Wrong password");
}
```
* 별도의 설정을 하지 않으면 발생한 Exception이 `RuntimeException`일 때 Transaction을   
  rollback한다. 위 코드에서 `WrongIdPasswordException`은 `RuntimeException`을 상속하는데,   
  만약 catch절에서 `WrongIdPasswordException`이 catch되면, Transaction은 rollback 된다.

* `JdbcTemplate`는 DB연동 과정에 문제가 있으면 `DataAccessException`을 발생한다고 했는데,   
  `DataAccessException` 역시 `RuntimeException`을 상속받고 있다. 따라서 `JdbcTemplate`의   
  기능을 실행하는 도중 예외가 발생하면 Proxy 객체는 Transaction을 rollback한다.

* `SQLException`은 `RuntimeException`을 상속하고 있지 않으므로 `SQLException`이 발생하면   
  Transaction은 rollback되지 않는다. 만약 `RuntimeException` 뿐만 아니라 `SQLException`이   
  발생한 경우에도 Transaction을 rollback하고 싶다면 __@Transaction의 rollbackFor 속성을 사용__ 한다.
```java
@Transactional(rollbackFor=SQLException.class)
public void someMethod() {

    //..

}
```
* 여러 개의 Exception 객체에 대해 Transaction을 rollback하고 싶다면, 아래와 같이 한다.
```java
@Transactional(rollbackFor={SQLException.class, MyException.class})
```

* __rollbackFor__ 와 반대 설정을 제공하는 것이 __noRollbackFor__ 속성이다. 이 속성은 지정한   
  Exception이 발생해도 rollback하지 않고 commit을 수행한다.
<hr/>

<h3>@Transactional의 주요 속성</h3>

<table>
    <tr>
        <td>속성</td>
        <td>타입</td>
        <td>설명</td>
    </tr>
    <tr>
        <td>value</td>
        <td>String</td>
        <td>Transaction을 관리할 때 사용할 PlatformTransactionManager Bean의 이름을 지정한다. (기본값 : " ")</td>
    </tr>
    <tr>
        <td>propagation</td>
        <td>Propagation</td>
        <td>Transaction의 전파 타입을 결정한다. (기본값 : Propagation.REQUIRED)</td>
    </tr>
    <tr>
        <td>isolation</td>
        <td>Isolation</td>
        <td>Transaction의 격리 레벨을 지정한다. (기본값 : Isolation.DEFAULT)</td>
    </tr>
    <tr>
        <td>timeout</td>
        <td>int</td>
        <td>Transaction의 제한 시간을 초 단위로 설정한다. (기본값 : -1, DB의 타임아웃 시간 사용함을 의미)</td>
    </tr>
</table>

* __@Transactional__ 어노테이션의 __value__ 속성이 지정되지 않으면, 등록된 Bean 중에서 타입이   
  `PlatformTransactionManager`인 Bean을 사용한다.

* __Propagation__ 열거 타입에 정의되어 있는 값 목록은 다음과 같다.
<table>
    <tr>
        <td>REQUIRED</td>
        <td>메소드를 수행하는데 Transaction이 필요하다는 것을 의미한다. 현재 진행중인 Transaction이 존재하면 해당 Transaction을 사용하며,
            존재하지 않으면 새로운 Transaction을 생성한다.</td>
    </tr>
    <tr>
        <td>MANDATORY</td>
        <td>메소드를 수행하는데 Transaction이 필요하다는 것을 의미한다. 하지만 REQUIRED와 달리 진행중인 Transaction이 존재하지 않을 경우
            예외를 발생시킨다.</td>
    </tr>
    <tr>
        <td>REQUIRES_NEW</td>
        <td>항상 새로운 Transaction을 시작한다. 기존에 진행중인 Transaction이 있다면, 이를 일시 중지하고 새로운 Transaction을 시작한다.
            새로운 Transaction이 종료된 후에 기존의 Transaction이 계속된다.</td>
    </tr>
    <tr>
        <td>SUPPORTS</td>
        <td>메소드가 Transaction을 필요로 하지는 않지만, 진행중인 Transaction이 존재하면 그 Transaction을 사용한다는 것을 의미한다.
            진행중인 Transaction이 없더라도 메소드는 정상적으로 수행된다.</td>
    </tr>
    <tr>
        <td>NOT_SUPPORTED</td>
        <td>메소드가 Transaction을 필요로 하지 않음을 의미한다. SUPPORTS와 달리 진행중인 Transaction이 존재할 경우 메소드가 실행되는 동안
            그 Transaction을 일시 중지하고 메소드 실행이 종료된 후에 그 Transaction을 계속 진행한다.</td>
    </tr>
    <tr>
        <td>NEVER</td>
        <td>메소드가 Transaction을 필요로 하지 않음을 의미한다. 만약 진행중인 Transaction이 있다면 예외를 발생시킨다.</td>
    </tr>
    <tr>
        <td>NESTED</td>
        <td>진행중인 Transaction이 존재하면 기존 Transaction에 중첩된 Transaction에서 메소드를 실행한다. 진행중인 Transaction이 없다면
            REQUIRED와 동일하게 작동한다.</td>
    </tr>
</table>

* __Isolation__ 열거 타입에 정의된 값은 다음과 같다.
<table>
    <tr>
        <td>DEFAULT</td>
        <td>기본 설정을 사용한다.</td>
    </tr>
    <tr>
        <td>READ_UNCOMMITTED</td>
        <td>다른 Transaction이 commit하지 않은 데이터를 읽을 수 있다.</td>
    </tr>
    <tr>
        <td>READ_COMMITTED</td>
        <td>다른 Transaction이 commit한 데이터를 읽을 수 있다.</td>
    </tr>
    <tr>
        <td>REPEATABLE_READ</td>
        <td>처음에 읽어온 데이터와 두번째에 읽어온 데이터가 동일한 값을 찾는다.</td>
    </tr>
    <tr>
        <td>SERIALIZABLE</td>
        <td>동일한 데이터에 대해서 동시에 두 개 이상의 Transaction을 수행할 수 없다.</td>
    </tr>
</table>

<hr/>

<h3>@EnableTransactionManagement 어노테이션의 주요 속성</h3>

<table>
    <tr>
        <td>proxyTargetClass</td>
        <td>클래스를 이용해서 Proxy객체를 생성할지 여부를 결정한다. (기본값 : false, 인터페이스를 이용해서 Proxy 객체 생성함)</td>
    </tr>
    <tr>
        <td>order</td>
        <td>AOP의 적용 순서를 지정한다. (기본값 : 가장 낮은 우선순위에 해당하는 int의 최대값)</td>
    </tr>
</table>

<hr/>

<h3>Transaction의 전파</h3>

* __Propagation__ 열거 타입 목록 중 __REQUIRED__ 를 이해하기 위해 Transaction 전파가 무엇인지 알아보자.
* Propagation.REQUIRED : 메소드를 수행하는데 Transaction이 필요하다는 것을 의미한다.   
  현재 진행중인 Transaction이 존재하면 해당 Transaction을 사용하며, 존재하지 않으면 새로운 Transaction을 생성한다.

```java
// SomeService 클래스
public class SomeService {
    
    private AnyService anyService;

    @Transactional
    public void some() {
        anyService.any();
    }

    public void setAnyService(AnyService as) {
        this.anyService = as;
    }
}

// AnyService 클래스
public class AnyService {
    @Transactional
    public void any() {
        //..
    }
}

// 설정 클래스 Config
@Configuration
@EnableTransactionManagement
public class Config {
    
    @Bean
    public SomeService some() {
        SomeService some = new SomeService();
        some.setAnyService(any());
        return some;
    }

    @Bean
    public AnyService any() {
        return new AnyService();
    }

    // DataSourceTransactionManager Bean 설정
    // DataSource 설정
}
```
* `SomeService`와 `AnyService` 클래스는 모두 __@Transactional__ 어노테이션을 적용한다.   
  설정 클래스에 따르면 두 클래스에 대해 Proxy 객체가 생성된다. 즉, `SomeService.some()`가   
  호출되면 Transaction이 시작되고, `AnyService.any()`가 호출돼도 Transaction이 시작된다.   
  그러나 `some()` 메소드는 내부에서 다시 `any()`를 호출하고 있다. 이 경우 Transaction처리는   
  어떻게 될까?

* __@Transactional__ 어노테이션의 __propagation__ 속성의 기본값은 __Propagation.REQUIRED__ 이다.   
  REQUIRED는 현재 진행중인 transaction이 존재하면 해당 transaction을 사용하고 존재하지 않으면   
  새로운 transaction을 생성한다고 했다. 처음 `some()` 메소드가 호출되면 Transaction은 새로 시작된다.   
  하지만 `some()` 내에서 `any()`를 호출하면 이미 `some()`에 의해 시작된 Transaction이 존재하므로   
  `any()` 메소드를 호출하는 시점에는 Transaction을 새로 생성하지 않는다. 대신 존재하는 Transaction을   
  그대로 사용한다. 즉, `some()` 메소드와 `any()` 메소드를 한개의 Transaction으로 묶어 실행하는 것이다.

* 만약 `any()` 메소드에 적용한 __@Transactional__ 의 __propagation__ 속성이 __REQUIRES_NEW__ 라면   
  기존 Transaction이 존재하는지의 여부에 관계없이 항상 새로운 Transaction을 시작한다. 따라서 이   
  경우에는 `some()` 메소드에 의해 Transaction이 시작되고, 다시 `any()` 메소드에 의해 Transaction이 생성된다.

```java
public class ChangePasswordService {

    //..

    @Transactional
	public void changePassword(String email, String oldPwd, String newPwd) throws Exception {
		Member member = memberDao.selectByEmail(email);
		if(member == null)
			throw new MemberNotFoundException();
		
		member.changePassword(oldPwd, newPwd);
		
		memberDao.update(member);
	}
}

public class MemberDao {

    private JdbcTemplate jdbcTemplate;

    //..

    //@Transaction 어노테이션 없음
    public void update(Member member) {
		jdbcTemplate.update(
			"UPDATE MEMBER set NAME=?, PASSWORD=? where EMAIL=?", member.getName(), member.getPassword(), member.getEmail());
	}
}
```
* `changePassword()` 메소드는 `MemberDao.update()` 메소드를 호출하고 있지만, 그 메소드에는   
  __@Transactional__ 어노테이션이 적용되어 있지 않다.   
  이 경우에 비록 `update()` 메소드에 __@Transactional__ 이 붙어있지 않지만, `JdbcTemplate`   
  클래스 덕에 __Transaction범위 내에서 쿼리를 실행할 수 있게__ 된다.   
  `JdbcTemplate`은 진행중인 Transaction이 존재하면 해당 Transaction의 범위에서 쿼리를 실행한다.
<hr/>