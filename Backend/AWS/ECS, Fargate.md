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

<h3>Docker Container</h3>

* 위에서 설명한 대로 Fargate는 Docker Image를 실행시키는 컨테이너이다.   
  따라서 꼭 Fargate를 사용하지 않고, EC2에 Docker를 설치하여 ECS를 사용할 수도 있지만,   
  굳이 컨테이너 전용으로 나온 Fargate를 사용하지 않고 EC2를 사용할 마땅한 이유를 찾지 못했다.

<h3>ECS 설정 1 - 작업 정의</h3>

* ECS 콘솔에서 가장 먼저 할 작업은 `작업 정의` 이다.   
  위에서 언급한 것과 같이, 작업 정의는 ECS의 최소 실행 단위인 `작업(Task)`에 대한 정의이다.   
  Fargate를 사용하므로 호환성 요구 사항으로는 `FARGATE`를 지정해주면 된다.   

* `작업 실행 IAM 역할` : 해당 작업을 실행할 IAM 역할을 지정한다.   
  이 역할에 지정된 IAM 역할은 ECS에 대한 작업을 수행할 수 있는 권한을 가지고 있어야 한다.

* `컨테이너 정의` : 해당 작업이 수행될 컨테이너에 대한 속성을 지정하는 것이다.   
  나의 경우에 Spring Boot를 사용하며, 포트 번호 설정을 따로 하지 않았기 때문에 Spring Boot의   
  내장 서버가 실행되는 기본 포트인 8080을 지정해 주었다.
* `호스트 포트`: 8080. `컨테이너 포트` : 8080, `프로토콜` : TCP
* `환경 변수` : 프로그램을 실행할 때 환경변수들이 필요하다면 환경 변수에 값을 지정할 수 있다.   
  나의 경우 `Parameter Store`에 환경 변수들을 저장해 두었기 때문에 `valueFrom`으로 모든 값을 지정해주었다.

<h3>ECS 설정 2 - 클러스터</h3>

* `작업 정의`가 완료된 후 다음에 할 작업은 `클러스터(Cluster)`를 추가하는 것이다.   
  `클러스터`는 논리적인 개념으로, 서비스나 태스크가 실행되는 공간을 가리킨다.

* Fargate를 사용하기에 `클러스터 템플릿 선택` 단계에서는 `네트워킹 전용 - AWS Fargate 제공`을 선택한다.

* 알맞은 클러스터 명을 지정해준다.

<h3>ECS 설정 3 - 서비스</h3>

* `서비스`는 `작업`을 지속적으로 관리하는 단위이다. 

* 위에서 생성한 클러스터를 클릭하여 서비스의 생성 버튼을 클릭하여 서비스를 생성한다.   

  * `1. 서비스 구성` 에서는 알맞은 구성을 지정한다. 시작 유형은 `FARGATE`, 작업 정의는   
    맨 처음에 생성했던 작업 정의, 서비스명은 원하는 대로 작성하고 서비스 유형은   
    기본값으로 설정되어 있는 `REPLICA`를 사용한다. 아래에 있는 작업 개수는 컨테이너 작업이   
    `안정 상태(Steady State)`에 도달했을 때 실행하고 싶은 컨테이너의 개수를 지정하면 된다.   
    배포 옵션에서는 `Blue/Green Deployment`를 사용한다. 그러면 아래에 Deployment-configuration 이라는   
    선택 옵션들이 나오는데, `CodeDeployDefault.ECSAllAtOnce`는 새로운 컨테이너가 실행되면,   
    __기존의 모든 트래픽을 전부 새로 실행된 컨테이너로 전환__ 한다. 그 아래에 있는 설정값들은 각각   
    시간과 백분율에 알맞게 트래픽을 전환한다. 예를 들어 `CodeDeployDefault.ECSCanary10Percent15Minutes`는   
    까나리아 배포 방식으로 15분마다 10% 씩 트래픽을 새로 실행된 컨테이너로 이동시킨다.
  
  * `2. 배포 구성` 단계에서는 베포에 대한 설정을 진행한다. 배포 유형에는 `블루/그린 배포(CodeDeploy 기반)`이   
    설정되어 있는데, 여기서 알 수 있듯이 배포를 수행하는 서비스는 `CodeDeploy`이다.
  
  * `3. 네트워크 구성`에서는 네트워크에 대한 전반적인 설정을 할 수 있다. VPC 설정, 서브넷 허용을 지정할 수 있으며   
    보안 그룹설정을 할 수 있다. 자동 할당 퍼블릭 IP는 `ENABLED`로 해둔다.   
    `상태 검사 유예 기간`은 ELB가 Health Check를 하기 까지의 시간을 초 단위로 지정하는 부분인데, 나의 경우 Spring   
    Boot로 만들어진 Docker Image가 실행되기 까지 40~45초가 걸려서 유예 기간에 60을 지정했다.   
    아래에 있는 `로드 밸런싱`의 경우에는 적용시킬 ELB 등을 설정할 수 있다. 컨테이너 및 ELB의 대상 그룹을 지정하면 된다.   
    ~~여기서 ELB 설정이 바로바로 업데이트가 안되서 좀 많이 헤맸음..~~
  
  * `4. Auto Scaling`에서는 원하는 속성값들에 기준을 두어, 조건에 따라 컨테이너 수를 자동으로 증가 시키거나 감소 시키는   
    Auto Scaling 서비스에 대한 설정을 할 수 있다. 나는 여기에 아래 2개의 기준을 설정했다.
    * `ScaleOutPolicy` : 5분 동안의 평균 `CPUUtilization`이 50%를 넘겼을 때 2개의 작업을 추가로 실시한다.
    * `ScaleInPolicy` : 5분 동안의 평균 `CPUUtilization`이 2% ~ 50%일 때 2개의 작업 제거,   
      5분 동안의 평균 `CPUUtilization`이 50% 미만일 때 1개의 작업 제거

<h3>설정 완료</h3>

* 서비스의 배포가 정상적으로 완료되면 `서비스 --> 작업` 탭에서 실행중인 작업을 볼 수 있다.
<hr/>

<h2>Github Action을 통한 배포 자동화</h2>

* AWS 공식 문서에는 `CodeCommit`을 통한 배포 자동화에 대한 문서만 나와있어,   
  `CodeCommit` 보다는 `Github Action`이 편리한 나와 같은 사람들을 위해 간단히 정리해 보았다.

* 우리가 원하는 CI/CD 파이프라인의 작동 순서는 아래와 같다.
  1. 특정 Branch에 특정 작업이 수행되면, Github Action이 작동한다.
  2. Github Action - (1) : 컨테이너를 빌드하여 Docker Image로 만든다.
  3. Github Action - (2) : 빌드된 Docker Image를 ECR에 push 한다.
  4. Github Action - (3) : ECS 작업 정의에 새로 push된 Docker Image를 적용시킨다.
  5. Github Action - (4) : CodeDeploy로 Blue/Green 배포 수행

* 우선, 위 과정을 Github X AWS가 제공하는 yml 파일이 있는데, 링크는 아래와 같다.
<a href="/https://github.com/actions/starter-workflows/blob/5760418d4f378a531680d729f4bf0b73eea45822/ci/aws.yml">링크</a>

* 아래 yml 파일은 위의 템플릿을 바탕으로 Spring Boot를 위해 작성된 파일이다.
```yml
on:
  push:
    branches: [ master ]
    

name: Deploy to Amazon ECS

jobs:
  deploy:
    name: Deploy
    runs-on: ubuntu-latest

    steps:
    - name: Checkout
      uses: actions/checkout@v2

    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v1
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: ${{ secrets.AWS_S3_REGION }}

    - name: build
      run: ./gradlew build
      env:
        환경 변수 key값: 환경 변수 key
        Github Secret의 경우: ${{ secrets.GITHUB_SECRET_KEY값 }}
        
    - name: Login to Amazon ECR
      id: login-ecr
      uses: aws-actions/amazon-ecr-login@v1

    - name: Build, tag, and push image to Amazon ECR
      id: build-image
      env:
        ECR_REGISTRY: ECR Registry 값
        ECR_REPOSITORY: ECR Repository 이름
        IMAGE_TAG: latest
      run: |
        docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG .
        docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG
        echo "::set-output name=image::$ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG"

    - name: Fill in the new image ID in the Amazon ECS task definition
      id: task-def
      uses: aws-actions/amazon-ecs-render-task-definition@v1
      with:
        task-definition: task-definition-product.json
        container-name: 작업 정의에서 지정한 컨테이너 이름
        image: ${{ steps.build-image.outputs.image }}

    - name: Deploy Amazon ECS task definition
      uses: aws-actions/amazon-ecs-deploy-task-definition@v1
      with:
        task-definition: ${{ steps.task-def.outputs.task-definition }}
        service: 클러스터 내의 서비스명
        cluster: 클러스터 이름
        codedeploy-deployment-group: CodeDeploy에 등록된 배포 그룹명
        codedeploy-appspec: appspec-product.yaml
        wait-for-service-stability: true
```

* Github Action을 위한 위의 yml이 참조하는 2개의 파일이 있는데,   
  바로 `task-definition-product.json`과 `appspec-product.yml`이다.

* 우선 `task-definition-product.json`은 이름에서 알 수 있듯이 작업 정의에 대한   
  설정값들을 담고 있는 파일이다.
```json
{
  "executionRoleArn": "작업 정의에서 지정한 작업 실행 IAM의 ARN",
  "containerDefinitions": [{
    "name": "작업 정의에서 지정한 컨테이너 이름",
    "image": "빌드된 Docker Image 값, Github Action이 자동으로 채워주기에 아무 값이나 넣어줘도 된다.",
    "essential": true,
    "portMappings": [{
      "hostPort": "컨테이너의 호스트 포트 값, int 형",
      "protocol": "tcp",
      "containerPort": "컨테이너 포트 값, int 형"
    }],
    "secrets": [
      {
        "name": "Spring Boot에서 사용할 환경 변수의 key 값",
        "valueFrom": "Parameter Store의 KEY 값"
      },
      {
        "name": "Spring Boot에서 사용할 환경 변수의 key 값",
        "valueFrom": "Parameter Store의 KEY 값"
      }
    ]
  }],
  "requiresCompatibilities": [
    "FARGATE"
  ],
  "networkMode": "VPC 값",
  "cpu": "512",
  "memory": "1024",
  "family": "작업 정의에서 생성한 작업명"
}
```

* yml 파일은 위의 json 파일을 참조하여 ECS 설정을 확인하며, 새로운 작업을 정의하게 된다.

* 이제 `appspec-product.yml`을 보자.
```yml
version: 0.0
Resources:
  - TargetService:
      Type: AWS::ECS::Service
      Properties:
        TaskDefinition: "작업 정의 명(이 값도 Github Action이 자동으로 채워준다.)"
        LoadBalancerInfo:
          ContainerName: "작업 정의에서 지정한 컨테이너명"
          ContainerPort: "컨테이너 포트, int 형"
        PlatformVersion: "LATEST"
```