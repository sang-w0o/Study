# yml 파일 속성을 객체로 다루기

- yml 파일에는 Spring Application의 실행에 필요한 속성값들을  
  지정할 수 있다. 예를 들어, AWS에 특정 작업을 하기 위해 Access Key, Secret Key가  
  필요하다고 해보자. 이를 코드 자체에 하드 코딩으로 쓰게 된다면  
  개발 환경, 운영 환경에 따라 코드 자체가 바뀌어야 하고, 유지 보수하기도  
  힘들 것이다.

- 그래서 이런 값들을 yml 파일에 넣고, 코드에서 가져다 사용할 수 있다.

- 가져오는 방법에는 두 가지가 있는데, 하나는 `@Value`이고,  
  다른 하나는 `@ConfigurationProperties`이다.

- `application.yml`에 아래 커스텀 값들이 있다고 해보자.

```yml
# application.yml

# Other configuration properties..

my:
  value: HELLO

hello:
  name:
    is: Sangwoo

today:
  weather_is: Sunny
```

- 위 3개 값들을 코드에서 가져오는 방식을 하나씩 살펴보자.

<h2>@Value</h2>

- Lombok을 사용한다면 Lombok이 제공하는 `@Value` 어노테이션이 아님에 주의해야 한다.  
  이 어노테이션은 `org.springframework.beans.factory.annotation.Value`이다.

- 간단한 컴포넌트를 만들어 위 3개 값을 모두 가져오도록 해보자.

```kt
@RestController
class DemoController {
    @Value("\${my.value}")
    private lateinit var myValue: String

    @Value("\${hello.name.is}")
    private lateinit var helloNameIs: String

    @Value("\${today.weather_is}")
    private lateinit var todayIsWeather: String

    @GetMapping("/test")
    fun doSomething(){
        println(myValue)
        println(helloNameIs)
        println(todayIsWeather)
    }
}
```

- 위처럼 하면 yml 파일에 있는 속성값들을 잘 읽어오는 것을 확인할 수 있다.  
  한 가지 유의해야할 점은 `@Value`를 사용하는 클래스가 Spring Bean으로  
  등록되지 않은 클래스라면 값을 읽어오지 못하거나, 에러를 내뱉을 수 있다.

<hr/>

<h2>@ConfigurationProperties</h2>

- `@ConfigurationProperties`를 사용하면 yml 파일에 있는  
  속성 값들을 POJO(Plain Old Java Object)로 매핑하여 객체로 다룰 수 있다.

- `my.value`부터 해보자.

```kt
@Component
@ConfigurationProperties(prefix = "my")
class MyProperties {
    var value: String = ""
}
```

- yml에서 `my`로 시작하기 때문에 `@ConfigurationProperties`의 prefix 속성에  
  my를 지정해주었다. 그리고 필드로 value가 있는데, 이는 yml파일에 my 하위에 value를 key로 가지는  
  값이 있기 때문이다.

- yml파일의 값이 `@ConfigurationProperties`가 지정된 클래스로 넘어가는 과정에서는  
  해당 필드의 standard setter 호출로 진행된다. Kotlin에서 변수를 var로 선언하면  
  Standard Setter를 알아서 만들어주기 때문에 추가적인 작업을 해줄 필요가 없다.  
  만약 val로 변수를 선언했다면, setter를 찾을 수 없다면서 에러가 발생한다.

- 다음으로 `hello.name.is`를 해보자.

```kt
@Component
@ConfigurationProperties(prefix = "hello.name")
class HelloNameProperties {
    var `is`: String = ""
}
```

- `@ConfigurationProperties`의 prefix에 hello.name을  
  지정해줬으므로 `MyProperties`와 동일한 원리로 is가 잘 들어간다.

- 만약 이 yml에 아래처럼 속성이 depth별로 있다고 하면,  
  각각의 클래스를 만들지 않고 어떻게 처리할 수 있을까?

```yml
hello:
  name:
    is: Sangwoo
  age: 24
```

- 그럴 때는 알맞은 타입을 지정해주면 되는데, 위 yml 파일의 값들을  
  하나의 클래스에서 관리하기 위해서는 아래처럼 해주면 된다.

```kt
@Component
@ConfigurationProperties(prefix = "hello")
class HelloProperties {
    var name: Map<String, String> = mutableMapOf()
    var age: Int = 0
}
```

- name의 타입은 `Map<String, String>`이다.  
  이대로 실행해보면, name에는 key가 is이고, value가 Sangwoo인 값이 추가된다.  
  age또한 제대로 들어간다.

- 마지막으로 `today.weather_is`를 처리하기 위한 POJO를 보자.

```kt
@Component
@ConfigurationProperties(prefix = "today")
class TodayProperties {
    var weather_is: String = ""
}
```

- 예상한대로 값이 잘 들어간다. 하지만 아래처럼 변수명을 Camel Case로 지어도 동작한다.

```kt
@Component
@ConfigurationProperties(prefix = "today")
class TodayProperties {
    var weatherIs: String = ""
}
```

- 이렇게 만약 yml의 key값이 snake case라면 가져오는 클래스에서  
  동일하게 snake case로 받아올 수도 있고, camel case로 받아올 수도 있다.

<hr/>

<h2>테스트 코드에서 확인하기</h2>

- 간단하게 테스트하기 위한 코드를 보자.

```kt
@SpringBootTest
class YmlValueTest {

    @Autowired
    private lateinit var myProperties: MyProperties

    @Autowired
    private lateinit var helloProperties: HelloProperties

    @Autowired
    private lateinit var todayProperties: TodayProperties

    @DisplayName("yml 파일에서 값을 잘 읽어온다.")
    @Test
    fun readAllPropertiesCorrectly() {
        assertEquals("HELLO", myProperties.value)
        assertEquals("Sangwoo", helloProperties.name.get("is"))
        assertEquals(24, helloProperties.age)
        assertEquals("Sunny", todayProperties.weatherIs)
    }
}
```

- 위 테스트 클래스는 `@SpringBootTest`가 적용되어 있기에 실질적으로 테스트 코드에서  
  사용해야 할 Spring Bean외에 모든 Bean들을 application context에 등록한다.

- 좀 더 startup 시간을 단축하고 싶다면 아래처럼 해도 된다.

```kt
@ExtendWith(SpringExtension::class)
@EnableConfigurationProperties(*[MyProperties::class, HelloProperties::class, TodayProperties::class])
@ContextConfiguration(initializers = [ConfigDataApplicationContextInitializer::class])
class YmlValueTest {

    @Autowired
    private lateinit var myProperties: MyProperties

    @Autowired
    private lateinit var helloProperties: HelloProperties

    @Autowired
    private lateinit var todayProperties: TodayProperties

    @DisplayName("yml 파일에서 값을 잘 읽어온다.")
    @Test
    fun readAllPropertiesCorrectly() {
        assertEquals("HELLO", myProperties.value)
        assertEquals("Sangwoo", helloProperties.name.get("is"))
        assertEquals(24, helloProperties.age)
        assertEquals("Sunny", todayProperties.weatherIs)
    }
}
```

- `@ExtendWith(SpringExtention::class)` : Spring Bean들을 `@AutoWired`로 주입받기 위해 사용
- `@EnableConfigurationProperties(..)` : 원하는 클래스들을 Spring Bean으로 등록하기 위해 사용  
  (여기서는 `MyProperties`, `HelloProperties`, `TodayProperties`를 등록하고 있다.)
- `@ContextConfiguration(initializers = [ConfigDataApplicationContextInitializer::class])` :  
  application context에서 `application.properties`, `application.yml`과 같은  
  ConfigData 파일을 불러오기 위해 사용.

<hr/>
