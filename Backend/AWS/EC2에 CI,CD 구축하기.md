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
