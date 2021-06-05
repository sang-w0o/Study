# AWS VPC

- Amazon Vitrual Private Cloud(Amazon VPC)는 AWS의 다양한 리소스들을  
  사용자가 정의한 가상 네트워크(Virtual Network)에서 실행할 수 있도록 해준다.

- 이 가상 네트워크는 온프레미스 형식의 데이터 센터로 구축된 전통적인 네트워크 구조를 매우 비슷하게 띈다.

<h2>Concepts</h2>

- 위에서 말한 *AWS의 다양한 리소스*가 EC2 인스턴스라고 했을 때, VPC는 EC2의 Networking Layer로서 작동한다.
- 아래는 간단한 VPC의 Concept이다.

- Virtual Private Cloud(VPC): AWS 계정 및 그의 리소스들에 대해 작동하는 가상 네트워크 환경
- Subnet: VPC 내의 IP 주소 범주
- Routing Table: 네트워크의 트래픽이 어디로 갈지를 정하는 규칙(Route)들로 정의된 테이블
- Internet Gateway: 외부 인터넷과 정의한 VPC 내의 리소스들의 통신(Communication)을 활성화하기 위해  
  VPC에 붙히는(attach)하는 Gateway
