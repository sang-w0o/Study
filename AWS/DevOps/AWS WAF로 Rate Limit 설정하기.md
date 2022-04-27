# AWS WAF로 Rate Limit 설정하기

- Rate Limiter를 구현하고 구축하는 데에는 꽤나 큰 리소스가 든다.  
  그리고 특히나 개발 인력이 적은 프로젝트 또는 회사에서는 직접 구현하기 보다는, Cloud Provider가 제공하는 Managed Service를 사용하는 것이 좋다.

> Rate Limiter가 왜 필요한지, 어떤 구현 방식이 있는지는 [여기](https://github.com/sang-w0o/Study/blob/master/Infrastructure/%EA%B0%80%EC%83%81%20%EB%A9%B4%EC%A0%91%20%EC%82%AC%EB%A1%80%EB%A1%9C%20%EB%B0%B0%EC%9A%B0%EB%8A%94%20%EB%8C%80%EA%B7%9C%EB%AA%A8%20%EC%8B%9C%EC%8A%A4%ED%85%9C%20%EC%84%A4%EA%B3%84%20%EA%B8%B0%EC%B4%88/3.%20Rate%20Limiter%EC%9D%98%20%EC%84%A4%EA%B3%84.md)에서 확인할 수 있다.

- 이번에는 AWS WAF(Web Application Firewall)를 이용해 기존에 존재하는 Application Load Balancer에 대해  
  Rate Limit을 적용해보는 방법을 알아보자.

## Web ACL 생성

- 우선 [AWS WAF Console](https://console.aws.amazon.com/wafv2/homev2#)에 접속해, `Create Web ACL`을 선택한다.

![picture 1](/images/AWS_WAF_1.png)

- 다음으로 원하는 이름과 알맞은 region을 선택하고, 정보를 입력한다.

![picture 2](/images/AWS_WAF_2.png)

- 하단에 `Add AWS Resources`가 있는데, 현재는 WAF를 아래의 3가지 AWS Resource에 대해 적용할 수 있다.

  - Amazon API Gateway
  - Application Load Balancer
  - AWS AppSync

- 나는 기존에 ECS Cluster에 연결된 Application Load Balancer가 있었기에, Application Load Balancer를 선택했다.

![picture 3](/images/AWS_WAF_3.png)

- 다음으로 넘어가면, 규칙과 규칙 그룹을 만들 수 있게 된다.

- 나는 이 문서에서는 IP 주소를 기준으로 1초에 요청을 단 1번만 허용하도록 해보려고 한다.  
  물론 IP 주소를 기준으로 할지, 사용자를 기준으로 할지 등 기준에 대한 선택은 자유롭다.

- **IP 주소를 기준으로 1초에 요청을 단 1회 허용**하도록 하게끔 하기 위해, 우선 아래처럼 규칙을 생성했다.  
  참고로 현재 AWS WAF는 Rate-based Rule에 대해 무조건 **5분을 기준**으로 두게끔 한다.  
  따라서 나는 300을 rate limit으로 두었다.

![picture 7](/images/AWS_WAF_4.png)

- 규칙을 성공적으로 추가한 후 모습은 아래와 같다.  
  **Default web ACL action for requests that don't match any rules** 는 `ALLOW`로 설정해야 한다.

- 지금은 Rule이 하나밖에 없어서 priority(우선 순위)를 설정할 것이 없지만, 만약 다양한 Rate Limit 조건이 있다면  
  다음 Step인 `Set rule priority`에서 우선순위를 설정할 수 있다.

- Metric(CloudWatch) 설정은 기본값으로 넘겼고(추가할 게 아직 없기에..) 리뷰 후 생성을 모두 마쳤다.

- 마지막으로 Rate-based Rule을 초과한 요청들에 대해서는 아래처럼 응답이 가게끔 해주었다.

![picture 8](/images/AWS_WAF_5.png)

---
