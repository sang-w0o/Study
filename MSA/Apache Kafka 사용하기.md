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

- 이제 위 코드에서 Producer가 Topic이 `TOPIC_MESSAGE`이면서, value가 `SampleEvent`인  
  이벤트를 발행하므로, 이를 받아서 처리할 Kafka Consumer를 생성해야 한다.

- Consumer 쪽의 Kafka 설정 정보를 담는 `KafkaProperties`는 Produer의 `KafkaProducer`와  
  거의 비슷한 속성들을 갖는다.

```kt
@Configuration
class KafkaProperties {

    private val bootStrapServer = "localhost:9092"
    private val consumer = mutableMapOf<String, String>()

    fun getConsumerProps(): Map<String, Any> {
        val properties = this.consumer
        properties.putIfAbsent("bootstrap.servers", this.bootStrapServer)
        properties.putIfAbsent("group.id", "sample_consumer")
        properties.putIfAbsent("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        properties.putIfAbsent("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        return properties
    }
}
```

- Producer는 `key.serializer`와 `value.serializer`를 가진 반면, Consumer는 직렬화된 것을 받아  
  역직렬화를 해야하기 때문에 `key.deserializer`와 `value.deserializer`가 명시되어 있다.  
  `group.id`는 해당 Kafka Consumer가 속한 Consumer Group의 고유값이다.  
  만약 이 값이 없다면 자동으로 생성되며, 해당 group.id 내의 consumer가 모두 삭제되면 group 또한 삭제된다.

- 이제 Message Broker로부터 이벤트를 받아서 처리하는 실제 Consumer 부분을 살펴보자.

```kt
@Service
class SampleKafkaConsumer(
    kafkaProperties: KafkaProperties,
    private val sampleService: SampleService
    ) {

    private val logger = LoggerFactory.getLogger(SampleKafkaConsumer::class.java)

    companion object {
        private const val TOPIC_MESSAGE = "TOPIC_MESSAGE"
    }


    private val isClosed = AtomicBoolean(false)
    private var consumer = KafkaConsumer<String, String>(kafkaProperties.getConsumerProps())
    private val executorService = Executors.newCachedThreadPool()

    @PostConstruct
    fun start() {
        logger.info("Kafka consumer starting..")
        Runtime.getRuntime().addShutdownHook(Thread(this::shutdown))
        consumer.subscribe(Collections.singleton(TOPIC_MESSAGE))
        logger.info("Kafka consumer started.")

        executorService.execute {
            try {
                while(!isClosed.get()) {
                    val records: ConsumerRecords<String, String> = consumer.poll(Duration.ofSeconds(3))
                    for(record in records) {
                        logger.info("Consumed message in $TOPIC_MESSAGE: ${record.value()}")
                        val objectMapper = ObjectMapper()
                        val sampleEvent = objectMapper.readValue(record.value(), SampleEvent::class.java)
                        sampleService.onMessage(sampleEvent.messageOne, sampleEvent.messageTwo)
                    }
                }
                consumer.commitSync()
            } catch(e: WakeupException) {
                if(!isClosed.get()) throw e
            } catch(e: Exception) {
                logger.error(e.message, e)
            } finally {
                logger.info("Closing Kafka Consumer")
                consumer.close()
            }
        }
    }

    @PreDestroy
    fun shutdown() {
        logger.info("Shutting down Kafka Consumer")
        isClosed.set(true)
        consumer.wakeup()
    }
}
```

- 우선 isClosed는 Kafka Consumer의 상태를 저장하는 변수인데, 여러 개의 thread에서도 문제없이 작동하는 것을  
  보장하기 위해 Thread-Safe한 `AtomicBoolean` 클래스를 이용하여 생성했다.

- `consumer.subscribe()`는 이 Kafka Consumer가 `TOPIC_MESSAGE`를 topic으로 가지는 이벤트에 대해  
  구독한다는 것을 지정한다.

- `executorService`는 `Executors.newCachedThreadPool()`을 이용하여 생성했는데,  
  이는 Kafka Consumer가 작업할 thread를 최적화되게 가져오기 위함이다.

- 실제 consumer가 이벤트를 받아서 처리하는 부분은 `executorService.execute {...}`의 부분이다.  
  try 블록 내부를 보면 이벤트를 처리하는 부분이 있는데 우선 while문이 `!isClosed.get()`이라는 조건 하에  
  수행된다. 이 조건은 Consumer가 실행되고 있냐는 조건과 동일하다.

  - `consumer.poll(Duration.ofSeconds(3))`는 이 Consumer가 3초 동안 Consumer가 동작하는  
    thread를 blocking하여 message broker로부터 이벤트를 수신하도록 한다.  
    만약 아무런 이벤트가 수신되지 않으면 빈 `ConsumerRecords` 객체를 반환하고, 수신했다면 수신한 이벤트들을  
    모두 `ConsumerRecords`에 넣어서 반환한다. `ConsumerRecords<K,V>`는 `ConsumerRecord<K,V`들을  
    가지는 객체이다. 따라서 바로 아래에 있는 for문에서 하나의 `ConsumerRecord`를 처리하는 코드가 있다.

- 마지막으로 이벤트를 처리하는 부분을 살펴보자. 우선 Producer가 `SampleEvent`를 보냈으니, Consumer도  
  `SampleEvent`를 파싱할 수 있어야 한다. 따라서 Consumer에도 Producer와 동일하게 `SampleEvent`를 생성해주었다.  
  그리고 이 `SampleEvent`를 처리하는 `sampleService.onMessage()`는 아래와 같다.

```kt
@Service
class SampleService {

    fun onMessage(messageOne: String, messageTwo: String) {
        try {
            Thread.sleep(10000)
        } catch(e: InterruptedException) {
            e.printStackTrace()
        }
        println("===================================================")
        println("MESSAGE ONE : $messageOne")
        println("MESSAGE TWO : $messageTwo")
        println("===================================================")
    }
}
```

- 단순한 예시이므로 많은 시간이 소모되는 작업처럼 작동시키기 위해 Thread를 10초 동안 일시 중지시키고,  
  콘솔에 찍도록 했다.

<h2>실행 방법</h2>
