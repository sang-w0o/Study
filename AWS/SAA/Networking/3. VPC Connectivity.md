# VPC Connectivity

## VPN

- `10.0.0.0/16`의 CIDR Block을 가지는 VPC 내의 private subnet에 `192.168.0.0/16`의 CIDR을 가지는 Data Center에서  
  접근하고 싶다고 하자.

- 이를 위해 VPN을 활용할 수 있는데, 구축하는 방법은 아래와 같다.

  - (1) VPC에 Virtual Gateway 생성
  - (2) Data Center에 Customer Gateway 생성
  - (3) Virtual Gateway와 Customer Gateway를 잇는 VPN Tunnel 생성
    - VPN Tunnel은 오직 Customer Gateway에서만 연결을 시작할 수 있다.
  - (4) Private subnet의 Route table 규칙 수정
    - Destination: `192.168.0.0/16`(data center), Target: `VGW`
  - (5) Private subnet 내의 리소스들에 대한 Security Group 수정
    - `192.168.0.0/16`으로부터의 inbound traffic allow하는 rule 추가

![picture 15](/images/AWS_SAA_VPC_CN_1.png)

---

## Direct Connect

- VPN은 인터넷을 통해 VPC 내의 private 리소스들에 접근하는 반면, Direct Connect는 인터넷을 통하지 않고 완전히 분리된 private infrastructue를  
  사용해 VPC 내의 private 리소스들에 접근한다. 여기서 _private infrastructure_ 는 대부분 APN에 의해 관리되는 infrastructure이다.

- Private infastructure는 APN이 관리하는 부분과 AWS가 관리하는 부분으로 나뉘며, Data center와 함께 이 두 부분도 router를 가진다.

> Private infrastructure를 **Direct Connect Location** 이라 한다.

- 마지막으로, Direct Connect를 사용했을 때 실제 연결하는 대상은 VPC가 아니라 AWS Region 자체이다.

- Direct Connection으로는 private connection과 public connection 모두를 수립할 수 있다.  
  아래 그림에서 회색 선은 private connection을 나타낸다.

![picture 16](/images/AWS_SAA_VPC_CN_2.png)

---

## VPC Peering

- VPC Peering은 서로 다른 2개의 VPC들이 통신할 수 있도록 해준다.

- VPC Peering은 1:1 연결만 지원한다.
- VPC Peering 시, 서로 다른 2개의 VPC는 CIDR Block 상에서 서로 겹치는 IP Address가 존재하면 안된다.
- VPC Peering은 서로 다른 region 내의 VPC들 사이에서도 동작한다.

- 2개의 VPC(VPC-1, VPC-2) 간의 VPC Peering이 맺어지는 과정을 보자.  
  VPC-1을 requester, VPC-2를 acceptor라 해보자.

  - (1) requester가 acceptor에게 VPC Peering 연결 요청을 보낸다.
  - (2) acceptor가 requestor에게 연결 허가 응답을 보낸다. (ACK)
  - (3) requester, acceptor 사이의 Peering Connection이 수립된다.

- VPC Peering이 동작하기 위해서는 각 VPC의 routing table도 수정해줘야 한다.

![picture 2](/images/AWS_SAA_VPC_CN_3.png)

---

## Transit Gateway

- VPC Peering은 VPC 간의 1:1 연결만을 지원했지만, Transit Gateway를 사용하면 _Central Hub_ 를 기준으로 여러 개의 VPC들에 대한  
  연결을 맺을 수 있다.

- 아래 그림에는 4개의 VPC(원)과 2개의 Remote Data Center(사각형)이 있다.  
  이들은 각각 Central Hub에 연결을 맺어 VPC에 연결할 수 있는데, 이 연결은 VPN Connection일 수도 있고 Direct Connect Connection일 수도 있다.

![picture 3](/images/AWS_SAA_VPC_CN_4.png)

---
