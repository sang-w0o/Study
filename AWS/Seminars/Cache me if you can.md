# Cache Me If You Can

- AWS re:Invent 2017에서 발표한 [Cache Me If You Can](https://www.youtube.com/watch?v=WFRIivS2mpo)을 정리한 내용입니다.

## Caching

- 시간을 꽤나 소모하는(time consuming) 복잡한 작업을 **단 1번만 수행**
- 위에서 수행한 작업의 결과를 **여러 번 재사용**
- 이는 memory가 CPU 보다 비용이 저렴하고, 더 빠르기 때문

- 통상적인 웹 애플리케이션은 아래의 4개 layer로 구성된다.  
  그리고 각 layer마다 caching을 할 수 있다.

  - Edge
  - Web Tier
  - App Tier
  - Database

![picture 1](/images/AWS_CMIYC_1.png)

---

## Edge Caching

- Edge에서의 caching은 AWS CloudFront 등의 CDN 서비스를 이용해 진행할 수 있다.  
  CloudFront는 CSS, JS, HTML 파일 등의 정적(static) content들에 대한 캐싱을 지원하지만,  
  동적(dynamic) content도 지원한다.

- 예를 들어, 여러 Region을 지원해야 하며, 100%의 DB Consistency를 보장해야 한다고 해보자.  
  그러면 아래 처럼 여러 region들 사이에 sync를 해주는 단계가 필요하다.

![picture 2](/images/AWS_CMIYC_2.png)

- 위 아키텍쳐에서 각 region별로 수행하는 작업이 실시간 작업이어야 하며, 매번 작업을 수행할 때마다  
  작업의 결과도 다른데, 어떤 것을 caching할 수 있으며 CloudFront를 사용할 때는 어떤 이점이 있을까?

- 이 경우에는 CloudFront를 caching 목적이 아닌, latency를 최적화하는 데에 사용할 수 있다.

![picture 3](/images/AWS_CMIYC_3.png)

- 위 아키텍처를 보면, 최상단에 CloudFront가 있다. CloudFront가 제공하는 무수히 많은 edge location들을  
  자동으로 최적화해 사용할 수 있게 설정해주고, US region에 대해서만 작업을 해준다.  
  US region에 대해서만 DB Consistency 보장 작업을 해주면 CloudFront dynamic content acceleration을  
  통해 multi region에 대한 일관성을 보장할 수 있다.

- 즉, CloudFront의 Edge location들을 활용해 network acceleration을 수행하는 것이다.

### Lambda@Edge

- Lambda@Edge를 사용하면 CloudFront network의 과정 중 Edge layer에서 content를 수정할 수 있다.

- Lambda@Edge는 총 4개 단계에서 request, response에 대해 Lambda function을 호출할 수 있게 해준다.  
  아래 그림을 보자.

![picture 4](/images/AWS_CMIYC_4.png)

- Viewer Request: End user가 CloudFront cache에 대해 HTTP Request를 한 경우

  - Lambda function을 실행해 Viewer Request를 수정하는 등의 작업을 할 수 있다.

- Origin Request: Viewer Request가 충족되지 못해(CloudFront Cache가 없는 경우 등) Origin Server로 가는 경우

  - 마찬가지로 Lambda function을 실행해 Origin Request를 수정하는 등의 작업을 할 수 있다.

- Origin Response: Origin Server에서 CloudFront cache로 response를 보내는 경우

  - 마찬가지로 Lambda function을 실행해 Origin Response를 수정하는 등의 작업을 할 수 있다.

- Viewer Response: CloudFront cache에서 End user에게 반환하는 response를 보내는 경우

  - 마찬가지로 Lambda function을 실행해 Viewer Response를 수정하는 등의 작업을 할 수 있다.

- Use Cases

  - Content Customization: 예를 들어, 사진을 특정 width, height를 지정한 채로 요청한다고 했을 때  
    Lambda@Edge를 사용해 Viewer Response를 수정하는 등의 작업을 할 수 있다.

  - Visitor Validation: 특정 작업을 요청하는 client를 validate하고 싶을 때, origin backend server까지  
    요청이 가지 않고 Lambda@Edge에서 처리할 수 있다.

  - A/B Testing

### Edge Caching 정리

- CloudFront를 사용해 latency를 최적화할 수 있다.
- 거의 항상 best practice이다.
- 비용 절감도 가능하다. (Network 상으로 전송되는 트래픽의 GB당 비용이 CF가 더 저렴)
- Lambda@Edge 사용을 고려하자.

---

## Web Tier

- Web Tier에서의 caching은 기존에 Web Tier과 App Tier 사이에 Web Cache라는 layer를 둠으로써 진행할 수 있다.

![picture 5](/images/AWS_CMIYC_5.png)

- Web Cache Tier에서는 아래의 유명한 툴들이 있다.

  - Nginx
  - Varnish
  - Squid
  - Cache in-memory
  - Choosing high RAM instances (ex. R4 Family)

- Web Cache Tier의 핵심은 **http response를 caching하는 것** 이다.

- Serverless API들에 대해서는 아래 아키텍쳐 처럼 Amazon API Gateway를 사용해 Web Cache Tier를 적용할 수 있다.

![picture 6](/images/AWS_CMIYC_6.png)

- API Gateway에는 In-Memory cache를 적용할 수 있다.  
  따라서 API Gateway를 적용하는 것 만으로도 간편하게 Web Cache Tier를 적용할 수 있다.

### Web Cache Tips

- 모든 정적 content를 caching하자.
- Log 파일 등을 활용해 자주 사용되는 데이터를 확인해 caching하자.
- Cache hit, miss 수치를 모니터링하자.
- TTL을 신중히 결정하자.
  - TTL은 배포에 영향을 주지 않아야 한다.
  - 60초 정도로 짧은 TTL도 latency 향상에 큰 도움을 줄 수 있다.

### Web Caching 정리

- CloudFront를 이미 사용하고 있더라도, Web Caching을 할 이유는 충분하다. (Original Request)
- Amazon API Gateway는 built-in caching을 제공하기에 쉽게 적용할 수 있다.
- TTL을 신중히 결정하자.

---

## App Tier

- 당연히 App Tier에도 caching을 적용할 수 있다.  
  Web Tier Caching처럼 새로운 layer가 들어가는 대신, 별도의 cache server 또는 RAM을 사용한다.

![picture 7](/images/AWS_CMIYC_7.png)

- App Tier에는 아래의 것들을 포함한 _사실상 모든 것들_ 을 caching할 수 있다.

  - Sessions
  - Results
  - Aggregations
  - Templates
  - Environments
  - Configurations

- 대용량 실시간 작업이 필요한 서비스에도 App Tier cache를 적용하면, latency를 획기적으로 줄일 수 있다.  
  (초당 10,000건의 request가 있고, 각 request마다 1ms씩 단축시켜도 엄청나다.)

- 중요한 점은 **Monitoring, Logging** 을 통해 단축되는 latency를 확인하는 것이다.

- App Tier의 Caching은 Disk 대신 Ram을 사용해야 한다.

  - 모든 instance type은 RAM이 있으므로, 이를 활용하자! (모니터링은 당연히 필수다.)
  - 속도를 향상시킨다면, duplicate된 data도 좋다.
  - 자주 사용되는 data를 RAM에 넣어 pre-load 하자.
  - 사용하는 프로그래밍 언어의 caching framework를 사용하자.

- 만약 RAM이 부족하다면, Amazon ElastiCache 같은 별도의 cache server를 둘 수도 있다.

- 아래는 Amazon ElastiCache에 대한 간략한 설명이다.

  - In-Memory Key-Value Store
  - High-Performance
  - Redis, Memcached
  - Fully managed service
  - Highly available and reliable

- ElastiCache가 지원하는 두 in-memory cache solution에 대해 알아보자.

  - Memcached

    - In-memory key-value datastore
    - Fast
    - Open Source
    - No persistence
    - Very established
    - Easy to scale
    - Multi-Threaded
    - Supports strings, objects
    - Slab allocator

  - Redis
    - In-memory key-value datastore
    - Very fast
    - Open Source
    - Persistence
    - High Available(replication)
    - Powerful
    - Utility data structures(strings, list, hashes, sets, sorted sets..)
    - Simple
    - Atomic Operations (supporting transactions)

### App Tier Caching 정리

- 모든 것을 모니터링하자.
- 모든 것들을 RAM에 caching하자.
- ElastiCache의 사용을 고려하자.

---

## Database Tier

- App Tier Caching과 비슷하게, Database caching에서는 Database와 App 사이에 cache layer를 둘 수 있다.

![picture 8](/images/AWS_CMIYC_8.png)

- 전통적으로 서버에서 Disk를 사용하는 데이터베이스(Amazon RDS, Amazon DynamoDB 등)에 접근을 최소화하기 위해  
  서버와 Database 사이에 ElastiCache와 같은 캐시 서버를 둠으로써 caching을 할 수 있다.

- 이 경우, Cache Invalidation에 대한 고민을 하지 않을 수 없게 되는데, cache를 하는 데에는 두 가지 방법이 있다.

  - TTL with invalidation
  - Keep the cache in sync all the time

> "There are only two hard things in Computer Science: cache invalidation and naming thins."
>
> - Phil Karlton

- TTL을 올바르게 설정하는 것은 매우 중요하다. TTL을 너무 짧게 가져가면, 사실상 그렇게 효과적으로 latency를 줄일 수 없게 된다.  
  반면, TTL을 너무 길게 가져갔을 때는 database에 있는 최신 변동 사항이 반영되지 않은 이전 데이터가 cache되어 있기에 문제가 발생할 수 있다.

### Synchronous Writes

- 이때 Cache된 데이터와 Database가 가진 데이터를 항상 synchornize할 수 있다면, TTL을 활용할 때의 단점을 모두 해결할 수 있게 된다.  
  이를 구현하기 위해서는 Synchronous writes 기법을 사용할 수 있다. 아래 예시 아키텍처를 보자.

![picture 9](/images/AWS_CMIYC_9.png)

- 즉, 애플리케이션에서 database에 변동사항을 기록할 때 Database에만 write, update를 하는 것이 아니라 ElastiCache와 같은  
  Cache Server에도 동일한 write, update를 수행하는 것이다.

- 이 방식은 기대한 바 대로 동작은 하지만, 애플리케이션 내에서 꽤나 큰 변경사항을 필요로 한다.  
  예를 들어, DynamoDB에 연산을 수행한 후의 결과와 ElastiCache에 연산을 수행한 후의 결과를 비교해  
  이 둘이 다를 경우 ElastiCache를 즉시 모두 invalidate 시켜주는 등의 작업도 해줘야 한다.

#### DynamoDB Stream, Aurora Stored Procedure

- 애플리케이션의 변동 사항을 최소화하고 Synchornous writes를 사용하기 위해 AWS에서는 아래와 같이 AWS Lambda를 통한 방법을 제시한다.

![picture 10](/images/AWS_CMIYC_10.png)

- DynamoDB는 DynamoDB Stream이 있고, AWS Aurora는 Stored Procedure가 있어 write, update 연산이 발생한 경우에  
  Lambda를 실행시켜 ElastiCache를 갱신시켜주는 방법이다.

#### DAX(DynamoDB Accelerator)

- 하지만 만약 caching에 대해 전혀 신경쓰지 않고, 동기화를 보장하고 싶다면 어떻게 해야할까?  
  이때 바로 DAX(DynamoDB Accelerator)를 사용하면 된다.

- DAX의 concept은 **caching에 대해 전혀 신경쓰지 않고 동기화를 보장하는 것** 이다.  
  코드 변경량도 아래 예시 코드가 전부이다. 심지어 하나의 DAX Cluster내의 여러 개의 table들에 대해서도 지원된다.

```js
// Previous
var dynamodb = new AWS.DynamoDB();

// New
var dynamodb = new AmazonDaxClient({
  endpoints: ["your-dax.amazonaws.com", "your-dax2.amazonaws.com"],
  region: "us-east-1",
});
```

- 즉, Amazon DynamoDB Client 대신 Amazon DAX Client를 사용하도록 변경한 것이다.  
  이후 GetItem, PutItem 등 DynamoDB에 수행했던 연산을 그저 DAX에 수행해주면 된다.  
  그러면 DAX가 caching, TTL 관리, 동기화 등을 모두 책임지고 진행해준다. (구체적인 cache strategy는 Write-Through 이다.)

- 발표자료에서 공유해준 DAX 사용 전후의 metric 비교를 보자.

![picture 11](/images/AWS_CMIYC_11.png)

- 주로 _모든 것을 caching하라_ 고 하는데, 이 때 놓치지 말아야 할 중요 요소로 **Negative Caching** 이 있다.  
  Negative Caching의 Wikipedia 정의는 아래와 같다.

> Negative Caching: Cache that also stores the _"negative"_ responses. ex) failures  
> This means that a program remembers the result indicating a failure even after the cause has been corrected.  
> Usually negative cache is a design choice, but it can also be a software bug.
>
> 예를 들어 실패 상황까지 가는 과정이 매우 비용이 많이 드는 작업이 있다면, Negative Cache를 사용해 실제 해당 작업을 수행하지 않고도  
> 해당 작업이 실패했음을 빠르게 알려줄 수 있다.

- 예를 들어 DynamoDB의 GetItem이 아무런 결과도 반환하지 않는다고(empty result) 해보자. 그리고 이 상황이 비즈니스 로직 상 허용되는  
  예외 상황이라고 해보자. 그리고 _만약 DynamoDB의 GetItem 결과가 있다면, cache에 저장해라_ 라는 로직이 있었다면, 이는 Negative caching을  
  적용하지 않은 상황이다. 아래 표를 보자. Negative Caching의 적용 유무에 따라 Cache hit ratio가 매우 다르다.

![picture 12](/images/AWS_CMIYC_12.png)

- 즉 _"GetItem이 반환하는 결과가 없다"_ 라는 예외 상황이 충분히 valuable한 결과라면, 이 결과 까지 caching할 것을 고려해봐야 한다.

### Database Tier Caching 정리

- Negative result들을 포함한 모든 것들을 caching 하자.
- 캐시가 자동으로 DB와 동기화되도록 Lambda의 사용을 고려해보자.
- DynamoDB의 경우, DAX의 사용을 고려해보자.

---
