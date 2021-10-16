# Load Balancer들의 종류

- AWS의 ELB(Elastic Load Balancing)은 아래의 세 가지 Load Balancer들을 지원한다.

  - Application Load Balancer
  - Network Load Balancer
  - Classic Load Balancer

- AWS의 ECS(Elastic Container Service)에서는 세 개의 Load Balancer들을 사용할 수 있다.

- Application Load Balancer는 OSI Layer7에 있는 HTTP, HTTPS 트래픽을 라우팅하는 데에 사용되며,  
  Network Load Balancer와 Classic Load Balancer는 OSI Layer4의 TCP 트래픽을 라우팅하는 데 사용된다.

## Application Load Balancer

- Application Load Balancer는 Application Layer(HTTP, HTTPS)에서 라우팅을 어떻게 할지 결정한다.  
  요청 주소 기반의 라우팅(Path-based routing)과 Cluster 내의 하나 이상의 포트를 가지는 Container
  인스턴스들에 대해서도 라우팅을 해줄 수 있다. 또한 동적 호스트 포트 매핑도 지원한다.

- 예를 들어, ECS Task의 작업 정의가 NGINX Container port로 80번을, 그리고 host port로 0번을  
  명시한다면 host port는 수명이 짧은 포트(ex. 최신 Amazon ECS-optimized AMI 기준 32768 ~ 61000)로  
  동적으로 선택될 것이다.

- 작업이 실행되면, NGINX Container는 Application Load Balancer에 해당 Instance ID와 포트번호로  
  등록되며, Application Load Balancer에 오는 트래픽 중 NGINX에 가야할 트래픽이 자동으로 NGINX Container로  
  포워딩된다.

- 이러한 동적 매핑은 하나의 Container에 있는 하나의 Service가 여러 개의 작업을 가질 수 있게 해준다.

<hr/>

## Network Load Balancer

- Network Load Balancer는 Transport Layer(TCP/SSL)에서 라우팅을 어떻게 할지 결정한다.  
  이 로드 밸런서는 초당 100만 개의 요청을 처리할 수 있다. Load Balancer가 연결 요청을 받으면  
  대상 그룹(Target Group)에서 대상을 Flow Hash Routing Algorithm을 통해 결정한다.  
  선택된 대상에 대해서 TCP 연결을 수립하며, 이때 Listener Configuration을 참조한다.

- Network Load Balancer 또한 Application Load Balancer과 같이 동적 호스트 포트 매핑을 지원한다.

## Classic Load Balancer

- Classic Load Balancer는 라우팅을 어떻게 할지 Transport Layer(TCP/SSL) 또는  
  Application Layer(HTTP/HTTPS)에서 모두 결정할 수 있다. 현재 시점에서 Classic Load Balancer는  
  Load Balancer의 포트번호와 Container Instance의 포트번호 사이의 고정된 관계를 지정해줘야 한다.  
  예를 들어, Load Balancer port 80을 Container Instance port 3030과 매핑하고,  
  Load Balancer Port 4040을 Container Instance port 4040에 매핑할 수 있다.  
  하지만 하나의 Load Balancer Port를 두 개 이상의 Container Instance port에 매핑할 수는 없다.  
  예를 들어, Load Balancer port 80을 Container Instance A의 port 3030에 매핑하고,  
  동시에 Container Instance B의 port 4040에 매핑할 수 없다는 뜻이다.

<hr/>
