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
