# Lambda로 SQS 메시지 처리하기

- 이전에 [AWS SDK로 SQS에 메시지 보내기](https://github.com/sang-w0o/Study/blob/master/AWS/Backend/AWS%20SDK%EB%A1%9C%20SQS%EC%97%90%20%EB%A9%94%EC%8B%9C%EC%A7%80%20%EB%B3%B4%EB%82%B4%EA%B8%B0.md)를 작성했는데, 이번에는 메시지를 처리(consume)하는 방법을 보자.  
  아래 그림처럼, SQS가 메시지를 받으면 Lambda를 trigger해 처리하도록 해볼 것이다.

![picture 1](/images/AWS_LAMBDA_CONSUME_SQS.png)

## IAM 사용자 생성

- Lambda 함수를 배포하기 위해 serverless 프레임워크를 사용할 것이다.  
  따라서 지금 생성할 IAM 사용자는 serverless 배포를 위한 정책을 꼭 가져야 하는데, SQS에 메시지가 수신될 때마다  
  해당 메시지를 queue에서 꺼내 처리할 것이기에 `sqs:ReceiveMessage` 정책이 추가적으로 부여되어야 한다.

---

## 코드 보기

- 우선 serverless.yml 파일은 여느 serverless 배포 스크립트와 동일하게 작성하면 된다.  
  이번에는 메시지를 수신해, 해당 메시지를 Slack으로 보내는 코드를 작성해보자.

```ts
const consumeSqsMessage: Handler = async (event: SQSEvent) => {
  const app = new App({
    token: OAUTH_TOKEN,
    signingSecret: SIGNING_SECRET,
  });
  const message = event.Records[0]?.body;
  await sendMessageToSlack(app, message);
};

const sendMessageToSlack = async (app: App, message: string) => {
  try {
    await app.client.chat.postMessage({
      token: OAUTH_TOKEN,
      channel: CHANNEL_DEV,
      text: message,
    });
    console.log("Sent!");
  } catch (error) {
    console.log("Failed to send message. error: ", { error });
  }
};

export { consumeSqsMessage };
```

---

## SQS 설정하기

- Lambda를 배포하고 나서 SQS Console에 접속해보자.  
  이후 하단의 `Lambda 트리거` 탭에 들어가서 위에서 배포한 Lambda ARN을 선택해주자.  
  그러면 SQS에 메시지가 수신될 때마다 위의 Lambda가 trigger되어 메시지를 consume할 것이다.

---
