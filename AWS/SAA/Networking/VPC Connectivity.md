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

---

## Transit Gateway

---
