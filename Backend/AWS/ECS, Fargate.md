<h1>ECS + Fargate</h1>

* `AWS Fargate`는 AWS REINVENT 2017에서 소개된 서비스로, AWS ECS를 기반으로 작동한다.   
  Fargate는 __Docker Container를 EC2 인스턴스 없이 독립적으로 실행할 수 있게 해준다__.

<h2>ECS(Elastic Container Service) 기초</h2>

* Fargate는 ECS를 기반으로 해야만 사용 가능하다.

* ECS는 AWS에서 제공하는 Managed Container Orchestration Service로, 컨테이너를 기반으로   
  서비스를 배포 및 운영하는 기능을 제공한다.

* `클러스터(Cluster)` : ECS의 가장 기본적인 단위이다.   
  Cluster는 논리적인 개념으로 서비스나 태스크가 실행되는 공간이다.   
  따라서 Cluster가 없으면 ECS에서 Container를 실행할 수 없다.

* `Container Instance` : 컨테이너 인스턴스는 클러스터에서 서비스나 태스크를 실행하기 위해   
  사용되는 컴퓨팅 자원이다. 클러스터 스케쥴러는 클러스터 인스턴스를 조작할 수 있는 권한을 가지며,   
  클러스터에서 서비스나 태스크를 실행하면 조건을 만족하는 컨테이너 인스턴스를 찾아 컨테이너로 실행한다.   
  단, Fargate를 사용하면 컨테이너 인스턴스 없이 컨테이너를 실행할 수 있다.

* `Image` : ECS는 Container Orchestration의 도구로 컨테이너를 관리한다.   
  컨테이너는 이미지로부터 실행되며, 이미지는 특정 애플리케이션을 실행가능한 환경을 재현하기 위한   
  파일들의 집합이다. 이 이미지는 ECS와는 별개로 생성 및 관리된다. 따라서 ECS 사용자는 먼저   
  자신이 실행하고자 하는 애플리케이션을 Image로 가지고 있어야 한다. 일반적으로 이미지 빌드 파일은   
  Dockerfile로 관리되며, Docker Hub나 ECR에 업로드해서 사용한다.

* `Task Definition` : Task Definition은 ECS의 최소 실행 단위인 `Task`를 실행하기 위한 설정을   
  저장하고 있는 리소스이다. Task Definition은 하나 또는 두 개 이상의 컨테이너에 대한 정보를 포함할 수 있다.   
  컨테이너별로 실행하고자 하는 이미지를 지정해야 하며, CPU, RAM과 같은 리소스 제한 정보와   
  컨테이너의 실행에 필요한 옵션들을 지정한다.

* `Task` : Task는 ECS의 최소 실행 단위로, 하나 또는 두 개 이상의 컨테이너의 묶음이다.   
  Task는 독립 실행되거나 서비스에 의해 실행될 수 있다. 클러스터는 적절한 컨테이너 인스턴스를 찾아   
  Task Definition을 기반으로 Task를 실행한다. 독립적으로 실행된 Task는 한 번 실행된 이후로는   
  관리되지 않는다.

* `Service` : Service는 Task를 지속적으로 관리하는 단위이다. Service는 클러스터 내에서 태스크가   
  지정된 수 만큼 지속적으로 실행될 수 있도록 관리한다. 또한 AWS의 Load Balancer와 연동해서   
  실행중인 Task를 찾아 자동적으로 Load Balancer에 등록 및 제거하는 역할도 담당한다.
<hr/>

<h2>ECS, Fargate를 도입하게된 이유</h2>

* Fargate를 사용하기 전에는 EC2 상에서 서버 코드를 돌렸다.   
  그리고 CD는 아래와 같은 순서로 구축되어 있었다.
  1. Github Action으로 코드 테스트 및 빌드
  2. 빌드된 코드를 압축하여 S3에 업로드
  3. 업로드된 코드를 EC2에서 받아와서 실행

* 위 과정에서의 문제점은 3번 단계였는데, 기존에 실행되고 있던 코드를 멈추고 새로운 코드를 실행하는   
  과정에서 약 1분의 down time이 발생했다.

* 이를 어떻게 하면 해결할 수 있을지에 대한 고민을 하던 도중 AWS에서 Fargate라는   
  서비스를 제공한다는 것을 알게 되었고, Fargate들을 관리 및 운영하는 ECS에서   
  `Blue/Green Deployment`를 제공한다는 것을 알게 되었다.
<hr/>

<h2>ECS, Fargate 사용하기</h2>

* 너무 피곤한 관계로 다음에 적겠습..