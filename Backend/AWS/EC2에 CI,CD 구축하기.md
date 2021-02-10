<h1>EC2 인스턴스에 CI/CD 구축하기</h1>

<h2>들어가며</h2>

- Spring Boot를 이용해서 EC2 서버에 CI/CD를 구축하는 방법을 정리한 글이다.
  작동 순서는 아래와 같다.

  1. Github의 `main` 브랜치에 `push`가 발생하면 Github Action이 작동한다.
  2. Github Action은 우선 빌드를 진행한 후, `*.zip` 파일로 압축한 후 AWS S3에 업로드한다.
  3. CodeDeploy가 작동하여 AWS S3에 업로드된 파일을 가져와 EC2 인스턴스에서 동작시킨다.

- 참고로 여기서 설명하는 Spring Boot Application은 Environment Variable도 사용하기에
컴파일 시 환경 변수를 주입하는 방법도 다룰 것이다.
<hr/>

<h2>EC2 인스턴스 생성 및 IAM 역할 연결</h2>

- EC2 인스턴스는 기존에 생성하는 것처럼 생성하면 된다.
  한 가지 태그를 추가하는데, 키는 `Name`, 값은 원하는 값을 넣어주면 된다.
  차이점이라면 **CodeDeploy** 가 EC2 인스턴스 상에서 작동하게 하려면
  EC2 인스턴스에 IAM 역할을 지정해줘야 한다.

- 인스턴스를 생성한 후 중지시킨 후, `작업` -> `보안` -> `IAM 역할 수정`에 가면
  인스턴스에 IAM 역할을 지정할 수 있는데, 지정하는 필드에 적합한 IAM 역할을 생성해야 한다.

- IAM Management Console로 이동해서 `역할` 로 이동하여 `EC2`를 선택한 후 아래의 2개
  권한을 추가로 지정해주자.
  - `AmazonEC2FullAccess`
  - `AmazonS3FullAccess`

* 다시 EC2 콘솔로 돌아가 IAM 역할을 지정해주고, 인스턴스를 다시 시작하자.
<hr/>

<h2>AWS S3 Bucket 생성하기</h2>

- AWS S3에 새로운 Bucket을 만들어야 하는데, public access는 막아 놓고
원하는 이름으로 만든다. 이 예제에서는 `Example Bucket`이라 하겠다.
<hr/>

<h2>Github Action을 수행할 IAM 사용자 생성</h2>

- 파일을 작성하기 이전에 Github Action에서 S3에 파일을 업로드하고, CodeDeploy를 수행시킬 수 있는
  작업을 가진 IAM 사용자가 필요하다. 새로운 IAM 사용자를 IAM Management Console에서 생성한 후,
  ACCESS_KEY와 SECRET_ACCESS_KEY를 발급받아야 하며, 이 사용자에게는 아래의 권한을 주자.

  - `AmazonEC2FullAccess`
  - `AWSCodeDeployFullAccess`
  - `AWSCodeDeployRole`
  - `AmazonS3FullAccess`

- **여기서 생성 후 발급받은 ACCESS_KEY와 SECRET_ACCESS_KEY를 다운로드 하거나 다른 곳에 미리 적어두자.**
<hr/>

<h2>CodeDeploy 설정하기</h2>

- 이 CI/CD 파이프라인에서 `CodeDeploy`의 역할은 아래와 같다.

  - AWS S3에서 압축되어 업로드되어 있는 `*.zip` 파일을 다운로드 한다.
  - 파일을 받은 후 압축을 풀고 Spring Boot 애플리케이션을 수행한다.

- 우선 CodeDeploy 콘솔로 이동해서 `애플리케이션 생성` 을 클릭하여 애플리케이션을 생성하자.
  이름에는 원하는 이름을 넣는데, 여기서는 `ExampleApplication`이라 하겠다.
  컴퓨팅 플랫폼은 우리는 EC2를 사용하기에 `EC2/온프레미스` 를 선택한 후 생성한다.

- 다음으로는 CodeDeploy 배포 그룹을 생성하자.
  배포 그룹 이름을 지정하자. 여기서는 `ExampleDeployGroup`이라 하겠다.
  서비스 역할에는 우리가 EC2 IAM Role에 지정한 역할을 선택한다.
  배포 유형은 `현재 위치`를 선택하고, 환경 구성은 `Amazon EC2 인스턴스`를 선택한다.
  배포 설정에서는 `CodeDeployDefault.AllAtOnce`를 선택하고, 로드 밸런서에서는
  `로드 밸런싱 활성화`를 체크 해제하여 선택하지 않고 넘어간다.

- 만약 https를 사용하려 한다면 로드 밸런서를 생성하고 연결해주면 된다.
<hr/>

<h2>CodeDeploy가 수행할 작업 정의하기</h2>

- 우리는 Github Action을 통해 CodeDeploy를 작동시킬 것이기 때문에
  CodeDeploy가 어떤 작업을 언제 수행할지를 지정해줘야 한다.

- 코드가 있는 Github Repository에 `scripts` 폴더를 만들고, 아래처럼 추가해주자.

- 우선은 새로운 코드를 CodeDeploy가 받아서 EC2상에서 실행 시키기 전에 할 작업을 정의한
  Shell Script 파일이다. 만약 기존에 Spring Boot 애플리케이션이 실행중이었다면
  그 프로세스를 끝내야 할 것이다.
  아래 파일 이름은 `delete-before-artifacts.sh`라고 했다.

```sh
rm -rf /home/ec2-user/YOUR_PROJECT_NAME
```

- 위 코드에서 `YOUR_PROJECT_NAME`에는 본인이 원하는 프로젝트명을 지정하면 된다.  
  아래에 나오는 Shell Script 파일들에도 마찬가지로 적용하면 된다.

- 만약 EC2 인스턴스가 Amazon Linux가 아니라면 `ec2-user`에 OS에 알맞게 지정해주면 된다.

- 다음으로는 S3에서 파일을 받아온 후 CodeDeploy가 수행할 작업을 정의하는 Shell Script파일을 작성하자.  
  여기서는 `change-script-permissions.sh`라 했다.

```sh
cd /home/ec2-user/YOUR_PROJECT_NAME

sudo chown -R ec2-user:ec2-user /home/ec2-user/YOUR_PROJECT_NAME
chmod 777 /home/ec2-user/YOUR_PROJECT_NAME
chmod 777 /home/ec2-user/YOUR_PROJECT_NAME/*/**
```

- 마지막으로 애플리케이션을 실행시키는 작업을 정의한 Shell Script 파일을 작성하자.  
  여기서는 `run-application.sh`라고 했다.

```sh
sudo pkill -6 java
source /home/ec2-user/.env
SPRING_PROFILES_ACTIVE=production nohup java -jar /home/ec2-user/YOUR_PROJECT_NAME/build/libs/*.jar 1>>/home/ec2-user/log/spring-log.log 2>>/home/ec2-user/log/spring-error.log &
```

- 우선 기존에 실행되고 있던 java 프로세스를 종료시킨 후, `~/.env`에서 환경 변수 파일을 읽어온다.  
  다음으로 빌드외어 있는 `*.jar` 파일을 실행시키는데, 1번 스트림(표준 출력)은 `~/spring-log.log`로,  
  2번 스트림(에러 출력)은 `~/spring-error.log`로 기록되게 해 놓았다.

- 참고로 환경 변수가 담겨져 있는 `/home/ec2-user`하위에 있는 `.env` 파일은 아래와 같이 작성한다.

```sh
export ENVIRONMENT_VARIABLE_NAME=VALUE
export ENVIRONMENT_VARIABLE_NAME_2=VALUE2
```

- 참고로 이 `.env` 파일에 있는 값들은 노출되면 안되는 값들이므로 Github Repository에 작성하지 않고,  
  직접 EC2에 접속하여 작성하도록 한다.

- 마지막으로 위의 Shell Script 파일들을 어떤 시점에 수행할 것인지를 정의하는 파일을  
  작성하도록 하자. 위치는 레포지토리의 최상위로 하고, `appspec.yml`이라고 하자.

```yml
version: 0.0
os: linux
files:
  - source: /
    destination: /home/ec2-user/YOUR_PROJECT_NAME
permissions:
  - object: /home/ec2-user/YOUR_PROJECT_NAME
    owner: ec2-user
  - object: /home/ec2-user/YOUR_PROJECT_NAME/*/**
    owner: ec2-user
hooks:
  BeforeInstall:
    - location: scripts/delete-before-artifacts.sh
  AfterInstall:
    - location: scripts/change-script-permissions.sh
  ApplicationStart:
    - location: scripts/run-application.sh
      runas: ec2-user
```

- `os`는 EC2의 OS를, `owner`, `runas`에는 위에서 Shell Script를 작성할 때와 마찬가지로  
  본인의 EC2 OS에 맞는 값을 지정해주면 된다.

- `hooks`에 있는 `BeforeInstall` 와 같은 것들은 CodeDeploy의 단계인데, 단계들은 아래와 같다.

  - `ApplicationStop`
  - `DownloadBundle`
  - `BeforeInstall`
  - `Install`
  - `AfterInstall`
  - `ApplicationStart`
  - `ValidateService`

- 위 단계들 중 `ApplicationStop`은 CodeDeploy가 EC2에서 실행되고 있던  
  기존의 애플리케이션을 중지시키는 작업이다.
- `DownloadBundle`은 S3에서 우리가 업로드할 `*.zip` 파일을 받아오는 것이며,  
  `BeforeInstall`는 애플리케이션을 실행하기 전에, `Install`은 애플리케이션 설치,  
  `AfterInstall`은 설치 후에, `ApplicationStart`는 애플리케이션을 실행하는 것을 의미한다.  
  `ValidateService`는 EC2의 상태 등을 확인하는 작업을 수행한다.

* 위의 `appspec.yml` 파일에서 우리는 `BeforeInstall`, `AfterInstall`, `ApplicationStart`에  
  대해 각 단계에서 수행할 작업들을 정의한 script 파일을 지정함으로써 우리가 원하는 작업을  
  수행하도록 했다.

* 다음으로 위에서 작성한 `appspec.yml`, Shell Script 파일들을 수행시킬 Github Action을 정의하자.
<hr/>

<h2>Github Action 작성하기</h2>

- 다음으로는 `main` 브랜치에 `push`가 일어났을 때 작동할 Github Action을 정의하는  
  파일을 작성해보자.

- 파일을 작성하기 이전에 Github Action에서 S3에 파일을 업로드하고, CodeDeploy를 수행시킬 수 있는  
  작업을 가진 IAM 사용자가 필요하다. 새로운 IAM 사용자를 IAM Management Console에서 생성한 후,  
  ACCESS_KEY와 SECRET_ACCESS_KEY를 발급받아야 하며, 이 사용자에게는 아래의 권한을 주자.

  - `AmazonEC2FullAccess`
  - `AWSCodeDeployFullAccess`
  - `AWSCodeDeployRole`
  - `AmazonS3FullAccess`

- 위치는 `.github/workflows` 이다. `main.yml` 파일을 아래와 같이 작성하자.

```yml
name: CD

# main 브랜치에 push가 일어나면 작동하도록 정의한다.
on:
  push:
    branches: [main]

# Github Action이 작동할 파일을 만든다.
jobs:
  # This workflow contains a single job called "build"
  build:
    name: build and upload to s3
    # Github Action의 작업들이 수행될 가상 OS를 지정한다.
    runs-on: ubuntu-latest

    # 각 작업(jobs)가 수행될 단계를 steps로 정의한다.
    steps:
      # Github Action이 수행될 가상 머신에 접속한다.
      - name: checkout
        uses: actions/checkout@v2

      # AWS에 접속한다.
      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v1
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ${{ secrets.AWS_S3_REGION }}

      # 스프링 부트 프로젝트를 빌드한다.
      - name: build
        run: ./gradlew build
        # 아래는 빌드 시 테스트를 수행하므로 환경 변수를 지정해주는 것이다.
        env:
          DATASOURCE_URL: ${{ secrets.DATASOURCE_URL }}
          DATASOURCE_ID: ${{ secrets.DATASOURCE_ID }}
          DATASOURCE_PASSWORD: ${{ secrets.DATASOURCE_PASSWORD }}

      # 빌드된 파일을 S3에 업로드 하기 위해 zip 파일로 압축한다.
      # 여기서는 빌드된 파일, appspec.yml과 Shell Script들이 들어 있는
      # scripts 파일도 함께 압축한다.
      - name: compress files for deploy
        run: zip -r codehelper-backend.zip build/libs appspec.yml scripts

      # 압축한 zip파일을 S3에 업로드한다.
      - name: upload to s3
        run: aws s3 cp codehelper-backend.zip s3://${{ secrets.AWS_S3_BUCKET }}

  # 이 아래부터는 배포 작업인데, CodeDeploy가 수행할 작업들을 정의한다.
  deploy:
    needs: build
    name: deploy to ec2
    runs-on: ubuntu-latest

    steps:
      # AWS에 로그인한다.
      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v1
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ${{ secrets.AWS_S3_REGION }}
      # 배포를 수행한다. run 에 수행할 작업들을 정의한다.
      - name: deploy
        run: aws deploy create-deployment --application-name ExampleApplication --deployment-config-name CodeDeployDefault.OneAtATime --deployment-group-name ExampleDeployGroup --s3-location bucket=${{ secrets.AWS_S3_BUCKET }},bundleType=zip,key=example-backend.zip --region ${{ secrets.AWS_S3_REGION }} --file-exists-behavior OVERWRITE
```

- 위의 `main.yml` 파일에 대해서 알아보자.  
  주석을 달아두긴 했지만 추가적으로 더 보면 아래와 같다.

* 우선 `Congigure AWS credentials`에서 `aws-actions/configure-aws-credentials@v1`는  
  AWS에서 제공하는 것으로 AWS에 로그인하여 해당 IAM 사용자가 특정 작업을 수행할 수 있는지를  
  확인하는 과정이다. 해당 부분의 아래를 보면 `with`에 `aws-access-key-id`,  
  `aws-secret-access-key`, `aws-region`이 있는데, value 부분에  
  `${{ secrets.AWS_ACCESS_KEY_ID }}`와 같이 작성되어 있다.  
  이 부부분은 `Github Secret`을 사용하는 것인데, Repository Settings에서  
  Repository Secret에 있는 값을 읽어오는 것이다. 예를 들어 `${{ secrets.AWS_ACCESS_KEY_ID }}`라면  
  Github Repository Secret에 `AWS_ACCESS_KEY_ID`를 추가하고, 값으로는 AWS IAM 사용자를 만들 때  
  발급받은 `ACCESS_KEY`를 값으로 지정해주면 된다.

* 한국의 경우 AWS Region이 ap-northeast-2이므로 이 값을 `AWS_S3_REGION`을 key로 하는  
  Github Secret에 추가해주자. 또한 `AWS_S3_BUCKET`은 빌드 파일을 압축하여 업로드하는 용도로 만든  
  S3의 Bucket명을 지정해주면 된다.

* 마지막으로 가장 아래에 있는 명령어를 보자.

```sh
aws deploy create-deployment --application-name ExampleApplication --deployment-config-name CodeDeployDefault.OneAtATime --deployment-group-name ExampleDeployGroup --s3-location bucket=${{ secrets.AWS_S3_BUCKET }},bundleType=zip,key=example-backend.zip --region ${{ secrets.AWS_S3_REGION }} --file-exists-behavior OVERWRITE
```

- 위 명령어들은 `aws deploy` 명렁을 실행하는데, `create-deployment`는 새로운 배포를 생성한다는 것을  
  의미하고, `--application-name`은 배포가 생성될 CodeDeploy 애플리케이션을 의미한다.  
  `--deployment-config-name`은 배포할 때의 설정을 의미한다. 위에서는 `CodeDeployDefault.OneAtATime`을  
  지정했는데, 이는 우리가 CodeDeploy에서 배포 그룹을 생성할 때의 작업과 일치한다.  
  `--deployment-group-name`은 우리가 CodeDeploy Console에서 생성한 배포 그룹명을 지정하면 되며,  
  `--s3-location bucket`는 빌드 파일을 다운로드할 S3 Bucket명을 지정하는 옵션이다.  
  `bundleType`은 우리가 업로드한 파일이 zip파일이라는 것을 의미하며, `key`값은  
  그 zip파일의 이름이다. 속성명이 `key`인 이유는 S3 Bucket은 각 객체명을 `key`로 구분하기 때문이다.  
  `--region`은 S3 Bucket, CodeDeploy가 수행될 AWS Region을 의미하며, 마지막으로  
  `--file-exists-behavior`는 기존에 S3에 같은 key의 객체가 존재할 때 어떻게 처리할지를 지정한다.  
  위에서는 `OVERWRITE`를 지정함으로써 기존 파일을 덮어쓰도록 지정했다.

- <hr/>

<h2>EC2에 CodeDeploy Agent 설치하기</h2>

- EC2에 SSH로 접속하여 아래 명령어를 입력해주자.

```sh
yum install wget
```

- yum으로 wget을 설치한 후 아래 명령어를 입력하여  
  EC2를 위한 CodeDeploy Agent를 다운로드 받자.

```
wget https://aws-codedeploy-ap-northeast-2.s3.amazonaws.com/latest/install
```

- 이제 CodeDeploy Agent를 수행하면 된다.

```
sudo service codedeploy-agent start
```

- 아래 명령어로 상태를 확인할 수 있다.

```
sudo service codedeploy-agent status
```

- 위 명령어의 결과가 아래와 같으면 정상적으로 작동하고 있는 것이다.

```
The AWS CodeDeploy agent is running as PID $NUMBER.
```

<hr/>

<h2>결론</h2>

- 이제 main 브랜치에 push가 일어나면 자동적으로 새로운 코드가  
  EC2로 배포될 것이다. 여기서 한가지 유의할 점은 지금 방식은  
  기존에 켜져 있던 Spring Application을 종료한 후 새로운 코드를 받아  
  실행하기 때문에 다운 타임이 발생한다는 것이다. 또한 Spring Application 자체로도  
  실행되기 까지 소요되는 시간이 약 10~15초가 있다.

- 이를 해결하는 방법에는 CodeDeploy 배포 방식에서 `Blue/Green Deployment`를 선택하면 된다.  
  물론 이 방식을 사용하면 다운타임은 발생하지 않지만 Load Balancer 등과 같은 추가적인 설정을 해줘야 한다.

- 개인적인 생각이지만 요즘에는 백엔드 애플리케이션을 수행할 때 Docker Image로 빌드하여 Docker Container에서  
  수행하는 것이 거의 당연하게 여겨지고, 이를 위해 AWS에서 Docker Image를 수행하는 Container만을 제공하는데,  
  이는 AWS ECS, Fargate를 사용하면 된다.

- ECS, Fargate에 대한 설명과 이 서비스를 이용한 `Blue/Green Deployment`를  
  Github Action으로 구축하는 방법에 대한 설명은 아래 링크에 있다.
  <a href="https://github.com/sangwoo-98/Study/blob/master/Backend/AWS/ECS%2C%20Fargate.md">AWS ECS, Fargate에 대해</a>

<hr/>
