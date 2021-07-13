# AWS General Immersion Day (Network) TODO

- VPC(Virtual Private Cloud): 사용자가 정의한 가상의 네트워크 공간
- 완벽한 네트워크 제어 가능
  - IP 범위
  - Subnet
  - Routing Table
  - Network ACL, 보안 그룹
  - 다양한 Gateway

<h2>Default VPC</h2>

- Default VPC는 어느 리전에 있든 `172.31.0.0/16`의 대역을 사용한다.
- VPC는 Region Level의 자원이기에, Default VPC는 모든 가용 영역에 거쳐 생성되어 있다.
- 각 가용 영역마다 1개의 서브넷을 가지며, 20bit로 구성되어 있다. (`172.31.0.0/20`)

<h3>Routing Table</h3>

- EC2와 같은 리소스가 VPC내에서 통신하는 방법을 정의한 것.

- 인터넷 게이트웨이를 통해서 외부 인터넷과 통신 => 인터넷 퍼블릭 통신이 가능함

- Routing table은 가장 구체적인 경로를 따라간다.
- subnet이 있고, routing table이 있다면 어떤 서브넷이 어떤 routing table을 사용할지 연결(association)을 맺어준다.
- 기본적으로는 inbound, outbound 모두 허용되어 있다.
- NACL(Network Access Control List)는 순서대로 파악한다.(Rule Number)

<h3>Security Group</h3>

- Default VPC는

- 모든 가용 영역에 걸쳐서 20bit의 서브넷을 가진다.
- 각 가용영역별 하나의 서브넷만 가지고, 모든 서브넷은 퍼블릭 인터넷 통신이 가능하다.
- 블로그나 간단한 웹 사이트를 위한 퍼블릭 인스턴스용으로는 적합하다.  
  하지만 프로덕션용으로는 절대 적합하지 않다.

  - 최소한 퍼블릭과 프라이빗 서브넷 분리 (-> 라우팅 테이블)

<h2>보안 그룹과 NACL의 동작 방식 비교</h2>

- 상태 비저장(Stateless)과 상태 저장(Stateful) 방화벽

  - NACL: Stateless
  - Security Group: Stateful

- Default VPC의 NACL은 모든 inbound, outbound를 허용한다.  
  만약 22 port로 inbound가 허용되어 있는데, outbound를 막는다면 통신이 안된다.
  outbound에 대해 명시적으로 허용을 해줘야 한다.

- Security Group끼리는 서로 참조가 가능하다.(다른 subnet에 대해 사용 가능)

- Prefix의 활용: 네트워크 라우팅과 보안 그룹에서 활용할 수 있는 CIDR 블록 묶음
- prefix list를 사용하면 이 list에 대해서 오는 요청에 대한 규칙을 지정할 수 있다.

<h2>VPC를 위한 최소한의 보안</h2>

- 보안 그룹은 Stateful, NACL은 Stateless(outbound에 대한 명시적 지정 필수)
- 보안 그룹은 인스턴스 레벨, NACL은 서브넷 레벨
- 보안 그룹은 prefix list를 지원하며 서로 참조가 가능하다.
- NACL은 Deny 정책을 지원한다. (명시적으로 어디로부터 오는 요청을 거부할 수 있다.), 보안 그룹은 허용 정책만 지정 가능
  - AWS WAF: Web Application Firewall (더 advanced한 보안 정책 설정 가능)
- 보안 그룹은 네트워크 인터페이스당 5개까지 적용 가능하다.
- VPC에서 인터넷으로 노출시킬 표면적을 Routing 구성으로 최소화할 수 있다.
- 권한 제어 및 로깅 -> IAM, CloudWatch, VPC FlowLog 활용

<h2>운영 환경을 위한 VPC 디자인</h2>

- Private IP

  - 외부에서 직접 접근을 막기 위해
  - 외부에서 접근할 필요는 일부 있으나 우회 가능
  - 외부에서 접근을 원천 봉쇄

- CIDR(Classless Inter-Domain Routing)
  - 네트워크 뒤에 붙은 `/n` 에서 n. 총 32bit 중 호스트로 사용할 bit의 개수를 의미한다. (개수: 32 - n)
    숫자가 커질 수록 호스트 개수가 줄어든다.

<h2></h2>

<h2></h2>

<h2></h2>
