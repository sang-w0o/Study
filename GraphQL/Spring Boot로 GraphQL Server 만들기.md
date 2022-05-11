# Spring Boot로 GraphQL Server 만들기

- Spring Boot를 사용해 간단한 GraphQL API를 제공하는 서버를 작성해보자.  
  예제 코드는 모두 [여기](https://github.com/Example-Collection/Spring-GraphQL-Example)에 있다.

## 들어가며

- 우선 이번 예시에서의 데이터베이스 스키마는 아래와 같다.

![picture 1](/images/SPRING_BOOT_GRAPHQL_EX_1.png)

> ~~스키마만 봐도 어떤 걸 할지 대충 예상될 것이다.~~

- 이번에 만들어볼 API들은 아래와 같다.

  - 고객 정보 조회(`getCustomerInfo`)
  - 고객 생성(`createCustomer`)
  - 고객의 모든 주문 조회(`findOrdersByCustomer`)
  - 주문 생성(`createOrder`)
  - 주문 수정(`updateOrder`)

---

## 기본 세팅

- 우선 Spring Boot에서 GraphQL을 사용하기 위해서는 아래의 의존성을 Spring projects 외에 따로 추가해줘야 한다.

```gradle
dependencies {
	//..
	implementation("com.graphql-java:graphql-spring-boot-starter:5.0.2")
	implementation("com.graphql-java:graphql-java-tools:5.2.4")
}
```

<details><summary>Gradle build script 전체 보기</summary>

<p>

```gradle
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.7"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    kotlin("plugin.jpa") version "1.6.21"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Database
    implementation("mysql:mysql-connector-java")
    implementation("org.hibernate:hibernate-core:5.5.7.Final")
    implementation("org.hibernate:hibernate-validator:6.0.10.Final")

    // GraphQL
    implementation("com.graphql-java:graphql-spring-boot-starter:5.0.2")
    implementation("com.graphql-java:graphql-java-tools:5.2.4")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

</p></details>

---

## 프로젝트 구조 보기

- 이 예시 프로젝트의 구조는 아래와 같다.

![picture 2](/images/SPRING_BOOT_GRAPHQL_EX_2.png)

---

## GraphQL Schema 정의 및 서비스, 클래스 구현 및 정의

- 우선 위에서 본 5개의 API를 GraphQL Schema로 정의해보자.

```graphql
type Query {
  getCustomerInfo(customerId: Int!): CustomerResponseDto!
  findOrdersByCustomer(customerId: Int!): OrdersResponseDto!
}

type Mutation {
  createCustomer(nickname: String!, email: String!): CustomerResponseDto!
  createOrder(customerId: Int!, content: String!): OrderResponseDto!
  updateOrder(
    customerId: Int!
    orderId: Int!
    content: String!
  ): OrderResponseDto!
}

type CustomerResponseDto {
  id: Int!
  nickname: String!
  email: String!
}

type OrderResponseDto {
  id: Int
  customer: CustomerResponseDto
  content: String
  createdAt: String
  isDelivered: Boolean
}

type OrdersResponseDto {
  orders: [OrderResponseDto]
}
```

- 위 스키마는 `**.graphqls` 파일에 저장하면 되고, 이 파일은 `src/main/resources/` 하위에 위치해야 한다.

- 이제 위의 GraphQL Schema에 대응되는 코드를 짜볼 것인데, 모두 다 같은 패턴으로 되어있기에 query, mutation 각각 하나씩만 살펴보자.

> 참고로 만약 GraphQL Schema 파일에 있는 함수와 다른 함수명을 사용한다면, 애플리케이션은 아래의 에러를 발생시키며 실행되지 않는다.
>
> ```
> org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'graphQLSchema' defined in class path resource
> ```
>
> 하지만 API Schema file에 정의된 반환 객체명과 다른 이름을 가진 클래스를 반환하면, 이는 애플리케이션이 정상적으로 실행된다.
> 단, 만약 반환되는 클래스의 필드명이 schema file과 다르다면 이는 아래의 예외를 발생시키며 애플리케이션을 실행시키지 않는다.
>
> ```
> Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'schemaParser' defined in class path resource
> ```

### Query 구현 - `getCustomerInfo()`

- Query 중 `getCustomerInfo`를 보자. 이 함수는 customerId를 하나 전달받고, `CustomerResponseDto`를 반환한다.  
  우선 반환할 값들을 담을 클래스를 먼저 정의해보자.

```kt
data class CustomerResponseDto(
	val id: Int,
	val nickname: String,
	val email: String
) {
	companion object {
		fun from(customer: Customer) = CustomerResponseDto(
			id = customer.id!!,
			nickname = customer.nickname,
			email = customer.email
		)
	}
}
```

- 이제 `getCustomerInfo()`를 실제로 구현할 서비스 클래스를 만들어보자.  
  이 함수가 GraphQL에 대응한다는 것을 알리기 위해, 서비스 클래스는 `GraphQLQueryResolver` 인터페이스를 구현해야 한다.  
  우선 이 인터페이스를 구현하는 또다른 인터페이스를 만들어보았다.  
  (order 관련 query도 있기에, customer와 order query를 구분하기 위해 이렇게 구조화했다.)

```kt
interface CustomerQuery : GraphQLQueryResolver {
	fun getCustomerInfo(customerId: Int): CustomerResponseDto
}
```

- 다음으로는 실제로 위 `CustomerQuery`의 구현체를 작성해보자.

```kt
@Service
class CustomerQueryImpl(
	private val customerRepository: CustomerRepository
) : CustomerQuery {

	@Transactional(readOnly = true)
	override fun getCustomerInfo(customerId: Int): CustomerResponseDto {
		val customer = customerRepository.findById(customerId)
		if (customer.isPresent) {
			return CustomerResponseDto.from(customer.get())
		} else throw IllegalArgumentException("Customer not found")
	}
}
```

### Mutation 구현 - `createCustomer()`

- 이제 데이터를 변경하는 mutation에 속하는 함수를 정의해보자. `createCustomer()`를 구현해보자.  
  마찬가지로 인터페이스부터 정의한다.

```kt
interface CustomerMutation : GraphQLMutationResolver {
	fun createCustomer(nickname: String, email: String): CustomerResponseDto
}
```

- 다음으로 위 인터페이스를 구현하는 클래스를 작성하자.

```kt
@Service
class CustomerMutation(
	private val customerRepository: CustomerRepository
) : CustomerMutation {

	@Transactional
	override fun createCustomer(nickname: String, email: String): CustomerResponseDto {
		if(customerRepository.existsByEmail(email)) {
			throw IllegalArgumentException("Email already exists")
		}
		val customer = customerRepository.save(Customer(nickname = nickname, email = email))
        return CustomerResponseDto.from(customer)
	}
}
```

---

## 테스트

- 마찬가지로 query 1개, mutation 1개를 각각 테스트해보자.

### Query 테스트 - `getCustomerInfo()`

- `getCustomerInfo()`는 아래 타입의 `CustomerResponseDto`를 반환한다.

```graphql
type CustomerResponseDto {
  id: Int!
  nickname: String!
  email: String!
}
```

- 한 번 테스트해보자. 애플리케이션을 실행하면 endpoint는 **별도의 `@RequestMapping`, `@Controller`가 없어도 `[POST] /graphql`로 생성** 된다.

![picture 3](/images/SPRING_BOOT_GRAPHQL_EX_3.png)

- 반환되는 `CustomerResponseDto` 중 원하는 필드만 아래처럼 뽑아올 수도 있다.  
  이것이 GraphQL을 사용하는 주된 목적 중 하나이기도 하다.

![picture 4](/images/SPRING_BOOT_GRAPHQL_EX_4.png)

### Mutation 테스트 - `createOrder()`

- 이번에는 `createOrder()` mutation을 테스트해보자. 이 함수는 아래와 같다.

```graphql
type Mutation {
  createOrder(customerId: Int!, content: String!): OrderResponseDto!
}

type OrderResponseDto {
  id: Int
  customer: CustomerResponseDto
  content: String
  createdAt: String
  isDelivered: Boolean
}
```

- 바로 테스트해보자.
  ![picture 5](/images/SPRING_BOOT_GRAPHQL_EX_5.png)

- Query와 마찬가지로 mutation에서도 위처럼 원하는 반환값 중 원하는 필드만 뽑아올 수 있음을 확인했다.

---
