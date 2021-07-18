# Apache Kafka 사용하기

<h2>Apache Kafka</h2>

- Apache Kafka는 간단히 말해 이벤트 기반으로 비동기 작업을 처리할 수 있게 해주는 도구이다.  
  MSA 구조에서 각 마이크로서비스 끼리 소통할 때 자주 사용되며, 데이터의 결과적 일관성을 추구할 때에도  
  유용하게 사용된다.

- 이 글에서는 Apache Kafka를 Spring Boot와 함께 사용하여 간단한 프로젝트를 작성해 보도록 하겠다.  
  구현할 프로젝트의 아키텍쳐는 아래와 같다.

  ![picture 1](../images/4277c4adb29c0f60a5763b129bc6b7059a377241491548cf5c73b29b3be7e67e.png)

- 여기서 주의할 점은 Kafka Producer가 Client에 Response를 보낼 때, Kafka Consumer가 받은 이벤트에 대한 처리를  
  완료하는 것을 기다리지 않고 바로 Response를 보낸다는 점이다.

- 이러한 아키텍쳐는 MSA에서 하나의 서비스가 다른 서비스에 의존할 때, 다른 서비스에 오류가 발생해서 응답이 지연되는 등의 상황이  
  발생했을 때, 클라이언트는 서비스가 잘못된 것을 느끼지 못하게끔 할 때 유용하다.

<h3>기본 개념</h3>

- Kafka는 Producer, Broker, Consumer로 구성된다. 각각의 역할은 아래와 같다.

  - Producer: 이벤트 발행
  - Consumer: 이벤트 처리
  - Broker: Producer와 Consumer 사이에서 이 둘을 중개

- Kafka의 모든 메시지는 Key, Value의 형식으로 이루어지는데, 이 때 Key에는 **TOPIC** 이 들어간다.  
  TOPIC은 이벤트의 주제를 의미하며, 이벤트를 Consumer가 식별하여 적절하게 처리하기 위해 사용된다.  
  Value에는 Consumer가 해당 event를 처리하기 위해 필요한 적절한 정보들이 들어간다.

<h2>Spring Boot에서 사용하기</h2>

- Spring Boot에서 Kafka를 사용하기 위해 아래의 의존성 패키지를 추가해주자.

```gradle
implementation("org.apache.kafka:kafka-clients")
implementation("com.fasterxml.jackson.core:jackson-databind:2.11.4")
implementation("com.fasterxml.jackson.core:jackson-core:2.11.4")
```

- `org.apache.kafka:kafka-clients`는 Spring Boot에서 Kafka를 사용하기 위한 라이브러리이다.
- `com.fasterxml.jackson.core`는 파싱을 위한 라이브러리인데, 간혹 Kafka를 실행했을 때  
  Topic에 대한 에러 메시지가 출력되는 경우가 있다. 이를 해결하기 위해 추가해준 라이브러리이며,  
  버전은 Spring Boot가 가진 버전과 동일하게 설정해줘야 한다.  
  Spring Boot가 가진 jackson의 버전을 알기 위해서는 아래의 명령어를 수행하면 된다.

> `./gradlew dep | grep jackson`

<h2>Producer</h2>

- Kafka에서 이벤트를 발행하는 부분을 **Producer** 라고 부른다.  
  Producer는 적절한 Topic과 해당 Topic을 가진 이벤트의 정보를 포함하는 객체를 만들어서  
  Kafka Message Broker에 전달한다.

- 우선 Producer와 Consumer 각각에 필요한 Kafka에 대한 설정 정보를 담는 `KafkaProperties`를 보자.

```kt
@Configuration
class KafkaProperties {

    private val bootStrapServer = "localhost:9092"
    private val producer = mutableMapOf<String, String>()

    fun getProducerProps(): Map<String, Any> {
        val properties = this.producer
        properties.putIfAbsent("bootstrap.servers", this.bootStrapServer)
        properties.putIfAbsent("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        properties.putIfAbsent("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        return properties
    }
}
```

- `getProducerProps()`는 Map에 3개의 키와 값을 저장하는데, 각각에 대한 설명은 아래와 같다.

  - `bootstrap.servers`: Message Broker 서버의 주소
  - `key.serializer`: 이벤트의 Key(topic)을 직렬화할 때 사용할 유틸리티 클래스
  - `value.serializer`: 이벤트의 value를 직렬화할 때 사용할 유틸리티 클래스

- 실제로 Kafka Message Broker로 이벤트를 전송하는 `SampleKafkaProducer`, `SampleKafkaProducerImpl`

```kt
// SampleKafkaProducer: 내부 영역이 사용할 추상화된 인터페이스
interface SampleKafkaProducer {
    fun sendEvent(dto: SampleRequestDto)
}

// SampleKafkaProducerImpl: 실제 SampleKafkaProducer를 구현한 구현체

@Service
class SampleKafkaProducerImpl(kafkaProperties: KafkaProperties): SampleKafkaProducer {

    private val logger = LoggerFactory.getLogger(SampleKafkaProducerImpl::class.java)

    companion object {
        private const val TOPIC_MESSAGE = "TOPIC_MESSAGE"
    }

    private val objectMapper: ObjectMapper = ObjectMapper()
    private var producer = KafkaProducer<String, String>(kafkaProperties.getProducerProps())
    @PostConstruct
    fun initialize() {
        logger.info("Kafka Producer initializing..")
        Runtime.getRuntime().addShutdownHook(Thread(this::shutdown))
    }

    @PreDestroy
    fun shutdown() {
        logger.info("Shutting down Kafka Producer..")
        producer.close()
    }

    override fun sendEvent(dto: SampleRequestDto) {
        val sampleEvent = SampleEvent(dto.messageOne, dto.messageTwo)
        val message = objectMapper.writeValueAsString(sampleEvent)
        producer.send(ProducerRecord(TOPIC_MESSAGE, message)).get()
    }
}
```

- 위 코드에서는 `SampleEvent`, `SampleRequestDto`를 사용하는데, 이들은 시연을 위한  
  간단한 클래스이다. `SampleEvent`는 Kafka Producer가 발행할 이벤트의 value를 담은  
  클래스이며, `SampleRequestDto`는 클라이언트가 Restful API를 호출할 때 request body로  
  전달할 내용을 담는 클래스이다.

```kt
// SampleEvent

class SampleEvent(
    val messageOne: String,
    val messageTwo: String
)

// SampleRequestDto

data class SampleRequestDto(
    val messageOne: String = "",
    val messageTwo: String = ""
)
```

- 클라이언트가 실제로 호출할 Restful API와 이로 인해 실행되는 서비스 코드는 아래와 같다.  
  우선 서비스 코드 부터 보자.

```kt
@Service
class SampleService(private val kafkaProducer: SampleKafkaProducer) {

    fun sendMessage(dto: SampleRequestDto): SimpleResponseDto {
        kafkaProducer.sendEvent(dto)
        return SimpleResponseDto("Message successfully sent!")
    }
}

// SimpleResponseDto는 message를 가지는 간단한 DTO 이다.
```

- 마지막으로 Restful API를 노출시키는 컨트롤러는 아래와 같다.

```kt
@RestController
class SampleController(private val sampleService: SampleService) {

    @PostMapping("/v1/sample/kafka")
    fun sendEvent(@RequestBody dto: SampleRequestDto): SimpleResponseDto {
        return sampleService.sendMessage(dto)
    }
}
```

<h2>Consumer</h2>

<h2>실행 방법</h2>
