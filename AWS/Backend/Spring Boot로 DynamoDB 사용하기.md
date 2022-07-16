In this post, I will talk about how to use Amazon DynamoDB with Spring Boot application made with Kotlin.

The codes written here are all stored in this [Github repository](https://github.com/Example-Collection/Spring-Boot-DynamoDB-Example).

### Situation

Here we will think about a situation where we have to store posts created. Below is the example scheme of data that we will store in DynamoDB.

```json
{
  "post_id": "123a",
  "user_id": "roy",
  "title": "Title of this post",
  "content": "Content of this post",
  "created_at": "2022-07-16T14:07:31.000Z"
}
```

And below are our application's requirements.

- Get a specific post using `post_id`.
- Search posts with `title` and sort it using `created_at`.
- Search posts by written by a specific user(`user_id`), and sort it using `created_at`.

To meet the application's requirements, we can configure this DynamoDB table as below.

- Primary Key: post_id(partition key)
- Global Secondary Indexes:
  - `post_user_id_created_at_idx`: HASH: `user_id`, RANGE: `created_at`
  - `post_title_created_at_idx`: HASH: `title`, RANGE: `created_at`

### Setup

To demonstrate DynamoDB without actually using it, we will run a docker container with a DynamoDB image.

(1) Clone this [repository](https://github.com/Example-Collection/Spring-Boot-DynamoDB-Example) to your local machine and _change directory_ into it.

(2) Run docker container using below commands.

```sh
# Run
docker compose -f docker-compose.yml up -d
# Remove
docker compose -f docker-compose.yml down
```

When you run the "Run" command, it will run a docker container acting as DynamoDB in `localhost:54000`.

(3) Configure DynamoDB table with `scripts/create-dynamodb-table.sh`

```sh
# Add permission
chmod +x ./scripts/create-dynamodb-table.sh
# Execute
./scripts/create-dynamodb-table.sh
```

This script will configure the table needed.  
`create-dynamodb-table.sh` contains all the commands required to create a DynamoDB table with aws-cli, and all global secondary indexs are defined in `scripts/gsi.json` file.
`create-dynamodb-table.sh` reads information about global secondary indexes from `gsi.json`, and configures it.

### Configuring DynamoDB in Spring Boot Application

#### Adding dependencies

First we have to add required dependencies to our code.
Assuming using Gradle, let's add two dependencies as below.

```gradle
dependencies {
    //..
    implementation("com.amazonaws:aws-java-sdk-dynamodb:1.12.258")
    implementation("io.github.boostchicken:spring-data-dynamodb:5.2.5")
}
```

> spring-data-dynamodb is not an official library maintained by AWS nor Spring team. It originated from [michaellavelle/spring-data-dynamodb](https://github.com/michaellavelle/spring-data-dynamodb), and was forked and maintained at [derjust/spring-data-dynamodb](https://github.com/derjust/spring-data-dynamodb) to support Spring Boot versions up to 2.1.x. Now it is being maintained at [boostchicken/spring-data-dynamodb](https://github.com/boostchicken/spring-data-dynamodb), supporting Spring Boot versions up to 2.2.x.

#### DynamoDBConfig.kt

`DynamoDBConfig` class defines configurations to use DynamoDB.

```kotlin
Configuration
@EnableDynamoDBRepositories(basePackages = ["com.example.post.domain"])
class DynamoDBConfig(
    @Value("\${amazon.dynamodb.endpoint}") private val endpoint: String,
    @Value("\${amazon.aws.accessKey}") private val accessKey: String,
    @Value("\${amazon.aws.secretKey}") private val secretKey: String,
    @Value("\${amazon.aws.region}") private val region: String
) {

    @Primary
    @Bean
    fun dynamoDBMapper(amazonDynamoDB: AmazonDynamoDB): DynamoDBMapper {
        return DynamoDBMapper(amazonDynamoDB, DynamoDBMapperConfig.DEFAULT)
    }

    @Bean
    fun amazonDynamoDB(): AmazonDynamoDB {
        val awsCredentials = BasicAWSCredentials(accessKey, secretKey)
        val awsCredentialsProvider = AWSStaticCredentialsProvider(awsCredentials)
        val endpointConfiguration = AwsClientBuilder.EndpointConfiguration(endpoint, region)
        return AmazonDynamoDBClientBuilder.standard()
            .withCredentials(awsCredentialsProvider)
            .withEndpointConfiguration(endpointConfiguration)
            .build()
    }

    @Bean
    fun awsCredentials() = BasicAWSCredentials(accessKey, secretKey)
}
```

As you can see, we are configuring AWS credentials, and registering `AmazonDynamoDB` as a Spring Bean.

#### Post.kt

`Post` class represents the entities which will be saved in `posts` DynamoDB table. Let's define field, add appropriate annotations based on requirements.

```kotlin
@DynamoDBTable(tableName = "posts")
class Post(
    @field:DynamoDBHashKey
    @field:DynamoDBAttribute(attributeName = "post_id")
    val id: String = UUID.randomUUID().toString(),

    @field:DynamoDBAttribute(attributeName = "user_id")
    @field:DynamoDBIndexHashKey(globalSecondaryIndexName = "post_user_id_created_at_idx")
    val userId: String,

    @field:DynamoDBAttribute(attributeName = "title")
    @field:DynamoDBIndexHashKey(globalSecondaryIndexName = "post_title_created_at_idx")
    val title: String,

    @field:DynamoDBAttribute(attributeName = "content")
    val content: String,

    @field:DynamoDBAttribute(attributeName = "created_at")
    @field:DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
    @field:DynamoDBIndexRangeKey(globalSecondaryIndexNames = ["post_user_id_created_at_idx", "post_title_created_at_idx"])
    val createdAt: LocalDateTime = now()
)
```

#### PostRepository.kt

`PostRepository` interface is where you declare _spring-data-jpa-styled_ methods to query items from DynamoDB table.

```kotlin
@EnableScan
interface PostRepository : CrudRepository<Post, String> {
    fun findByUserIdOrderByCreatedAtAsc(userId: String): List<Post>
    fun findByTitleOrderByCreatedAtDesc(title: String): List<Post>
}
```

### Testing read, write and resolving issues

Above code works fine when we try to insert new item into DynamoDB table using `PostRepository.save()`. However, when we call `PostRepository.findByUserIdOrderByCreatedAtAsc()`, it throws an error saying:

```
java.lang.NoSuchMethodException: com.example.post.domain.Post.<init>()
```

- This means that we have to add default constructor for `Post` class, so let's simply add default values for each properties to implement this.

```kotlin
@DynamoDBTable(tableName = "posts")
class Post(
    @field:DynamoDBHashKey
    @field:DynamoDBAttribute(attributeName = "post_id")
    val id: String = UUID.randomUUID().toString(),

    @field:DynamoDBAttribute(attributeName = "user_id")
    @field:DynamoDBIndexHashKey(globalSecondaryIndexName = "post_user_id_created_at_idx")
    val userId: String = "",

    @field:DynamoDBAttribute(attributeName = "title")
    @field:DynamoDBIndexHashKey(globalSecondaryIndexName = "post_title_created_at_idx")
    val title: String = "",

    @field:DynamoDBAttribute(attributeName = "content")
    val content: String = "",

    @field:DynamoDBAttribute(attributeName = "created_at")
    @field:DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
    @field:DynamoDBIndexRangeKey(globalSecondaryIndexNames = ["post_user_id_created_at_idx", "post_title_created_at_idx"])
    val createdAt: LocalDateTime = now()
)
```

After that when we invoke the repository method again, we get different error saying:

```
java.lang.NullPointerException: null
	at com.amazonaws.services.dynamodbv2.datamodeling.StandardBeanProperties$MethodReflect.set(StandardBeanProperties.java:133) ~[aws-java-sdk-dynamodb-1.12.258.jar:na]
```

This error occurs because spring-data-dynamodb first creates `Post` instance using default constructor, and sets each values using _setters_. Since every properties of `Post` class is declared as `val`, there are no _setters_ created. Let's simply declare all properties with `var` instead of `val`.

```kotlin
@DynamoDBTable(tableName = "posts")
class Post(
    @field:DynamoDBHashKey
    @field:DynamoDBAttribute(attributeName = "post_id")
    var id: String = UUID.randomUUID().toString(),

    @field:DynamoDBAttribute(attributeName = "user_id")
    @field:DynamoDBIndexHashKey(globalSecondaryIndexName = "post_user_id_created_at_idx")
    var userId: String = "",

    @field:DynamoDBAttribute(attributeName = "title")
    @field:DynamoDBIndexHashKey(globalSecondaryIndexName = "post_title_created_at_idx")
    var title: String = "",

    @field:DynamoDBAttribute(attributeName = "content")
    var content: String = "",

    @field:DynamoDBAttribute(attributeName = "created_at")
    @field:DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
    @field:DynamoDBIndexRangeKey(globalSecondaryIndexNames = ["post_user_id_created_at_idx", "post_title_created_at_idx"])
    var createdAt: LocalDateTime = now()
)
```

Now, when we call the repository method, we get different error saying:

```
java.lang.IllegalArgumentException: argument type mismatch
```

While this error message is quite unkind, if we think carefully, the only property that has different type from Kotlin code and DynamoDB is `createdAt`. This field's Kotlin type is `LocalDateTime`, while DynamoDB attribute type is `S`, indicating string.

So let's simply remove the `@field:DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)` annotation from `createdAt` property, and invoke the repository method again.

Now we get another error, saying:

```
com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMappingException: Post[created_at]; only scalar (B, N, or S) type allowed for key
```

Why is this happening? We previously defined that `createdAt`'s field type is `S` using `@DynamoDBTyped` annotation, but error message is indicating that we should define DynamoDB type for `createdAt`!

The problem is that while `Date` can be automatically converted to `S` in spring-data-dynamodb, `LocalDateTime` cannot. So we have to declare a _converter_ for this field which is responsible of conversion between `Date` and `LocalDateTime`.

Getting back to `DynamoDBConfig` class, let's add this converter.

```kotlin
@Configuration
@EnableDynamoDBRepositories(basePackages = ["com.example.post.domain"])
class DynamoDBConfig(
    @Value("\${amazon.dynamodb.endpoint}") private val endpoint: String,
    @Value("\${amazon.aws.accessKey}") private val accessKey: String,
    @Value("\${amazon.aws.secretKey}") private val secretKey: String,
    @Value("\${amazon.aws.region}") private val region: String
) {

    companion object {
        class LocalDateTimeConverter : DynamoDBTypeConverter<Date, LocalDateTime> {
            override fun convert(source: LocalDateTime): Date {
                return Date.from(source.toInstant(ZoneOffset.UTC))
            }

            override fun unconvert(source: Date): LocalDateTime {
                return source.toInstant().atZone(TimeZone.getDefault().toZoneId()).toLocalDateTime()
            }
        }
    }

    // Spring Beans..
}
```

After that, we have to make `createdAt` property to use this converter.

```kotlin
@DynamoDBTable(tableName = "posts")
class Post(
    // Other properties..

    @field:DynamoDBAttribute(attributeName = "created_at")
    @field:DynamoDBTypeConverted(converter = DynamoDBConfig.Companion.LocalDateTimeConverter::class)
    @field:DynamoDBIndexRangeKey(globalSecondaryIndexNames = ["post_user_id_created_at_idx", "post_title_created_at_idx"])
    var createdAt: LocalDateTime = now()
)
```

Boom! Now we can use repository methods successfully!

### Wrapping up

It's been a quite long journey, resolving all the new errors upcoming. But this is the fun of developing, isn't it?

By the way if we have to configure primary key of DynamoDB table using both partition key and sort key, we need to configure domain class(in this example, `Post` class) more tha n just adding properties and applying `@DynamoDBHashKey` and `@DynamoDBRangeKey` annotation for each property. I will discuss how to implement this in the later post.
