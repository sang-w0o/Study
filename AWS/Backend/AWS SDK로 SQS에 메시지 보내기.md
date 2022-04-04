# AWS SDK로 SQS에 메시지 보내기

- AWS SDK(Java)를 사용해 Amazon SQS(Simple Queue Service)로 메시지를 보내는 방법을 살펴보자.

## IAM 사용자 생성

- 우선 프로그래밍 방식으로 액세스할 수 있는 IAM 사용자를 생성해야 한다.  
  SQS를 접근할 수 있어야 하기에, 아래처럼 policy를 지정해주었다.  
  여기서 만든 SQS의 이름은 "test-queue" 라고 해보자.

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": ["sqs:*"],
      "Effect": "Allow",
      "Resource": "arn:aws:sqs:[region]:[id-number]:test-queue"
    }
  ]
}
```

> 물론 프로덕션에서는 최소 권한 정책에 따라, `Action`도 최소한의 값만 지정해줘야 한다.

---

## 환경 설정

- 아래의 dependency를 추가해 aws sdk를 사용하도록 하자.

```groovy
// build.gradle.kts

// Other configurations

dependencies {
	implementation("software.amazon.awssdk:sqs:2.17.161")
}
```

---

## 코드 보기

- 위에서 발급받은 IAM의 Access Key와 Secret Key를 가지고 SQS에 메시지를 보낼 수 있다.  
  aws sdk를 사용해 SQS에 메시지를 전송하는 코드는 다음과 같다.

```kt
@Component
class SQSProducer(
    @Value("\${aws.sqs.url}") val sqsUrl: String,
    @Value("\${aws.sqs.access_key}") val accessKey: String,
    @Value("\${aws.sqs.secret_key}") val secretKey: String
) : SQSProducer {

    override fun sendMessage(message: String) {
        val sqsClient = SqsClient.builder()
            .credentialsProvider { AwsBasicCredentials.create(accessKey, secretKey) }
            .region(Region.AP_NORTHEAST_2).build()
        val sendMessageRequest = SendMessageRequest.builder()
            .queueUrl(sqsUrl)
            .messageBody(message)
            .build()
        sqsClient.sendMessage(sendMessageRequest)
        sqsClient.close()
    }
}
```

---
