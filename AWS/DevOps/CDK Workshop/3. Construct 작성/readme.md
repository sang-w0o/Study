# Construct 작성하기

- 이번 장에서는 직접 `HitCounter`라는 construct를 작성해볼 것이다. 이 construct는 API Gateway를 backend로 사용하는 어떠한  
  Lambda function에 적용할 수 있으며 각 URL pattern에 요청이 발생한 개수를 저장한다. 이 counter는 DynamoDB table에 기록된다.

![picture 36](/images/AWS_CDK_3_1.png)
