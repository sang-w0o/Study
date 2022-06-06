# RDBMS Read Replica 설정하기

- Read Replica는 write와 read 연산을 분리해 데이터베이스의 부하를 줄이기 위해 사용된다.  
  Amazon RDS에서 read replica를 생성하는 방법은 [여기](https://github.com/sang-w0o/Study/blob/master/AWS/DevOps/RDS%20Read%20Replica%20%EA%B5%AC%EC%84%B1%20%EB%B0%8F%20%EC%84%A4%EC%A0%95%ED%95%98%EA%B8%B0.md)를 참고하면 된다.

- 여기서는 위의 상황을 그대로 이어가, spring boot에서 read replica에게 read 연산을 처리하도록 하고, primary database가 write 연산을  
  처리하도록 설정하는 방법을 다룰 것이다.

> spring-data에서 기본으로 구현해서 사용할 수 있게 해주면 좋겠다...

- 기본적으로는 `@Transactional(readOnly = true)`가 적용된 메소드 내의 DB 연산을 read replica가 처리하도록 한다.

## `AbstractDataSource` 상속 및 구현

- 우선 첫 번째로 `AbstractDataSource`를 상속해 메소드를 구현해야 한다.

- `AbstractDataSource`는 `DataSource` 인터페이스를 구현하는 추상 클래스로, lookup key를 기준으로 여러 개의 `DataSource`들 중  
  어떤 `DataSource`를 사용할 것인지 결정한다. 어떤 database connection을 사용할지는 `DataSource#getConnection()`으로 결정한다.

```kt
class DBRoutingDataSource : AbstractRoutingDataSource() {
    override fun determineCurrentLookupKey(): Any {
        return if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) "replica"
        else "primary"
    }
}
```

- 위 코드에서는 `determineCurrentLookupKey()` 메소드를 오버라이딩했는데, 이 메소드는 어떤 `DataSource`를 사용할지 결정하는 역할을 한다.  
  위에서는 `TransactionSynchronizationManager.isCurrentTransactionReadOnly()`로 readOnly가 true라면 replica를,  
  false라면 primary datasource를 반환하도록 했다.

- `TransactionSynchronizationManager`는 thread마다의 리소스와 transaction 동기화를 처리하는 중앙 집중부이다.  
  특정 key에 대해 특정 리소스를 담을 수 있으며, 이 클래스가 담당하는 리소스는 thread-bound한 리소스여야 한다.

- `TransactionSynchronizationManager#isCurrentTransactinoReadOnly()`는 현재 진행되는 transaction이 read-only인지 여부를  
  반환한다.

---

## JPA 설정 클래스 작성

- 다음으로는 JPA 설정 클래스를 작성해줘야 한다. 우선 아래처럼 primary, read replica endpoint를 `application.yml`에 지정해줬다 하자.

```yml
spring:
  datasource:
    url:
      primary: ${DATASOURCE_PRIMARY_URL}
      replica: ${DATASOURCE_REPLICA_URL}
    username: ${DATASOURCE_USERNAME}
    password: ${DATASOURCE_PASSWORD}
```

- 기본적으로는 `spring.datasource.url`에 데이터베이스의 주소를 지정하면 자동으로 해당 데이터베이스와의 connection을 수립하지만, 이 경우에는  
  primary와 read replica의 endpoint가 다르고, 이 둘 각각에 대한 `DataSource`를 직접 만들어줘야 한다.

- 따라서 기본적으로 실행되는 JPA를 사용할 수 없으며, 아래와 같이 별도의 설정 클래스가 필요하다.

```kt
@Configuration
@EnableJpaAuditing
class JpaConfig(
	@Value("\${spring.datasource.driver-class-name}") private val driverClassName: String,
	@Value("\${spring.datasource.url.primary}") private val primaryDatasourceUrl: String,
	@Value("\${spring.datasource.url.replica}") private val replicaDatasourceUrl: String,
	@Value("\${spring.datasource.username}") private val username: String,
	@Value("\${spring.datasource.password}") private val password: String
) {

	companion object {
		private const val PRIMARY_DATASOURCE = "primary"
		private const val REPLICA_DATASOURCE = "replica"
	}

	private fun buildDataSource(
		driverClassName: String,
		url: String,
		username: String,
		password: String,
		poolName: String
	): DataSource {
		val hikariConfig = HikariConfig()
		hikariConfig.driverClassName = driverClassName
		hikariConfig.jdbcUrl = url
		hikariConfig.username = username
		hikariConfig.password = password
		hikariConfig.poolName = poolName
		return HikariDataSource(hikariConfig)
	}

	@Bean(name = ["primaryDataSource"])
	fun primaryDataSource() = buildDataSource(
		driverClassName,
		primaryDatasourceUrl,
		username,
		password,
		PRIMARY_DATASOURCE
	)

	@Bean(name = ["replicaDataSource"])
	fun replicaDataSource() = buildDataSource(
		driverClassName,
		replicaDatasourceUrl,
		username,
		password,
		REPLICA_DATASOURCE
	)

	@Bean(name = ["routingDataSource"])
	@Primary
	fun routingDataSource(
		@Qualifier("primaryDataSource") primaryDataSource: DataSource,
		@Qualifier("replicaDataSource") replicaDataSource: DataSource
	): DataSource {
		val dbRoutingDataSource = DBRoutingDataSource()
		val dataSourceMap = mapOf<Any, DataSource>(
			PRIMARY_DATASOURCE to primaryDataSource,
			REPLICA_DATASOURCE to replicaDataSource
		)
		dbRoutingDataSource.setTargetDataSources(dataSourceMap)
		dbRoutingDataSource.setDefaultTargetDataSource(primaryDataSource)
		return dbRoutingDataSource
	}

	@Bean
	@DependsOn("routingDataSource")
	fun dataSource(routingDataSource: DataSource) = LazyConnectionDataSourceProxy(routingDataSource)
}
```

- 우선 기본적으로 HikariCP로 인해 Connection이 관리되기 때문에 `DataSource` 인터페이스의 구현체인 `HikariDataSource`를 만들어주는  
  helper method로 `buildDataSource()` 메소드가 private으로 정의되어 있다.

- 다음으로는 `primaryDataSource` bean과 `replicaDataSource` bean을 각각 올바른 정보를 통해 등록해준다.

- 마지막으로 spring boot application이 실질적으로 사용할 `DataSource` 구현체를 `routingDataSource()` bean을 통해 등록해주고,  
  이 bean을 사용하도록 하기 위해 `@Primary` 어노테이션까지 적용해준다. 이 메소드는 `primaryDataSource()`와 `replicaDataSource()`를  
  주입받는데, 메소드 내부에서 이전에 작성했던 `DBRoutingDataSource`를 인스턴스화하고 primary, replica에 대한 `DataSource`를 담은  
  map을 `setTargetDataSources()`를 통해 지정하고, `setDefaultTargetDataSource()`에는 `primaryDataSource()`를 지정한다.  
  즉 read-only의 여부에 관계 없이 `primaryDataSource()`를 사용하도록 한다.

- 마지막으로 `dataSource()` bean은 transaction의 동기화 시점에 올바른 connection을 획득하기 위해 사용되는 bean이다.  
  이 bean이 없다면 모든 query가 read-only 지정 여부와 관계 없이 primary로 가게 된다.

---

## 테스트하기

- 어떤 데이터베이스를 사용하는지 테스트하는 것은 매우 간단한데, 아래처럼 `DataSource`를 주입받아 확인하면 된다.

```kt
@Service
class someService(
	private val someRepository: SomeRepository,
	private val dataSource: DataSource
) {
	@Transactional
	fun usingPrimary() {
		println(dataSource.connection.metaData.url)
		someRepository.save(Some())
	}

	@Transactional(readOnly = true)
	fun usingReadReplica() {
		println(dataSource.connection.metaData.url)
		someRepository.findById(1)
	}
}
```

---
