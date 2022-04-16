# VPC Fundamentals

- Allows 5 VPCs per region, per account.

## Subnets

- Subnet을 사용하면 1개의 VPC를 여러 개의 그룹으로 나눠 사용할 수 있다.

- VPC를 생성할 때는 이름(name)과 CIDR Block Address를 지정해줘야 한다.  
  CIDR Block은 IP 주소들의 범위를 나타낸다. `/16 ~ /28`

- 예를 들어 VPC를 CIDR Block에 `10.0.0.0/16`을 지정하고 생성했다고 해보자.  
  이부분이 중요한 이유는 당연하게도 VPC내의 subnet들은 무조건 VPC의 CIDR Block range 내에 있어야하기 때문이다.

- Subnet을 만들 때도 VPC를 만들때와 마찬가지로 CIDR Block을 지정해줘야 한다.  
  앞서 만든 VPC에 아래 2개의 subnet을 만들었다 해보자.

  - Public subnet: `10.0.1.0/24`
  - Private subnet: `10.0.2.0/24`

- Public subnet

  - Accessible from the internet.
  - Resources inside this subnet has 2 IP addresses, one for the internet(public ip) and private ip.
  - To make a subnet public, you should:
    - Attach an Internet Gateway(IGW) to the VPC.
    - Add a route to the public subnet's route table.

- Private subnet

  - Inaccessible from the internet.

- 모든 subnet의 Route Table은 기본적으로 Destination이 VPC의 CIDR Block, Target이 `local`인 규칙이 있다.  
  이 규칙은 수정 또는 삭제가 불가하며, VPC내의 subnet끼리 서로 통신할 수 있도록 해준다.

- 또다른 예시로 `10.0.0.0/16`의 CIDR Block을 가진 VPC에 1개의 public subnet, 2개의 private subnet을 만든다 해보자.

![picture 11](/images/AWS_SAA_VPC_FD_1.png)

- 위처럼 subnet들을 적절히 AZ별로 구성하면, 아래처럼 AZ-1이 장애가 나더라도 서비스가 장애로 이어지지 않는다.

![picture 12](/images/AWS_SAA_VPC_FD_2.png)

> AZ-2, AZ-3이 각각 장애가 나도 서비스가 장애로 이어지지 않는다.

- CIDR Block overview

- `10.0.0.0/16`의 CIDR Block에 subnet을 만든다고 해보자.  
  만든 subnet의 CIDR Block은 `10.0.1.0/24`이다. 이 subnet은 그러면 `2^8 = 256`개의 IP 주소를 가질 수 있다.  
  하지만 실제로 사용 가능한 IP 주소는 251개이다.

- `10.0.1.0/24`에서 IP주소는 `10.0.1.0` ~ `10.0.1.255`의 범위를 갖는다. 사용 불가능한 IP 주소들을 보자.
  - `10.0.1.0`: _Network Address_ 로 사용된다.
  - `10.0.1.1`: AWS Routing을 위해 사용된다.
  - `10.0.1.2`: AWS DNS에 의해 사용된다.
  - `10.0.1.3`: Reserved by AWS for future use.
  - `10.0.1.255`: _Broadcast Address_ 로 사용된다.

---
