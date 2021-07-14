# AWS Lambda 구축하기

<h2>Basics</h2>

- AWS의 Lambda는 대표적인 Serverless 서비스이다.  
  _Serverless_ 하다는 것이 어색할 수 있지만, 말 그대로 받아들이면 된다.

- Serverless의 특성에 맞게 Lambda는 아래와 같이 **Trigger**를 발생시켰을 때 수행된다.  
  그 **Trigger**로는 스케쥴러(특정 시간에 실행)가 될 수도 있고, AWS의 API Gateway와  
  연결하여 하나의 API를 호출하는 것이 trigger가 될 수도 있다.

- 이 예시에서는 스케쥴러가 아닌, API Gateway를 사용하여 Lambda를 초 간단하게 사용해보자.

- 참고로 Serverless 답게 Lambda의 비용 청구 방식도 매우 특이한데,  
  Lambda는 기본적으로 아래의 컨셉을 가지고 있다.

> 특정 trigger에 의해 함수가 실행된다.

- 위 컨셉에 맞게 비용 청구 방식은 _함수가 수행된 시간_ 이다.

<hr/>
