# VPC Security and Control

## NACLs(Network Access Control Lists)

- NACL은 subnet level에서 inbound, outbound traffic에 대한 제어를 할 수 있게 해준다.
- NACL is stateless.

---

## Security Groups

- Security Group은 NACL과 다르게 Allow에 대한 rule만 지정할 수 있다.  
  그리고 Security Group은 NACL과는 다르게 Resource Level에서 제어할 수 있다.

- Inbound traffic이 allow되는지 판단하는 과정은 아래와 같다.

  - (1) 해당 리소스가 속한 subnet의 NACL에서 조건에 맞는 allow inbound rule이 있는지 검사
  - (2) 해당 리소스에 해당하는 Security Group에 조건에 맞는 rule이 있는지 검사

- Security Groups are stateful.

---

## NAT Gateway

- NAT Gateway는 private subnet 내의 리소스들이 외부 인터넷과 통신을 하기 위해 사용된다.  
  NAT Gateway는 public subnet에 둬야 하고, public subnet에 있기에 public IP(Elastic IP)가 할당된다.  
  NAT Gateway를 만들었으면 private subnet의 Route table을 수정해야 한다.  
  Private subnet의 route table에서 destination이 `0.0.0.0/0`, target이 public subnet에 둔 NAT Gateway인 rule을 추가하면  
  private subnet 내의 리소스들이 외부 인터넷과 연결해야 할 때 NAT Gateway를 통해 통신하게 된다.

- NAT Gateway는 외부 인터넷으로부터 시작되는 요청은 절대 허용하지 않는다. 오로지 private subnet 내의 리소스들로부터 인터넷 연결이 시작되어야만  
  통신을 허용한다.

![picture 13](/images/AWS_SAA_VPC_SC_1.png)

- A NAT Gateway allows private instances to be able to access the internet while blocking  
  connections initiated from the internet.

---

## Bastion Hosts

- 외부에서 private subnet에 접근하기 위해 사용할 수 있는 다양한 방법 중 하나로 Bastion Host를 사용할 수 있다.

- Bastion host는 public subnet 내에 있고 private subnet 내의 리소스들에 접근을 허용하기에 꽤나 보안을 엄중히 구성해야 한다.

![picture 14](/images/AWS_SAA_VPC_SC_2.png)

- EC2 인스턴스에 접근하기 위해서는 private key가 필요하다.  
  외부에서는 Bastion Host instance의 private key를 통해 bastion host에 ssh를 할 수 있지만, bastion host에 접근하는 목적은  
  bastion host를 통해 private subnet 내의 ec2 instance에 접근하는 것이다.

- private subnet내의 ec2들도 ssh를 할 때 private key가 필요할 것이다.  
  이때 이 private key를 bastion host에 두는 것은 private key가 compromise 되었을 때 꽤나 심각한 보안 문제를 일으키기에 권장되지 않는다.  
  그럼 private subnet내의 ec2 instance들의 private key는 어디에 보관해야 할까? => SSH Agent Forwarding

---
