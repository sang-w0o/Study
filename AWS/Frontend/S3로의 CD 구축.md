<h1>AWS S3로의 CD 구축</h1>

<h2>들어가며</h2>

- 이 설명은 AWS S3를 이용하고, 그 중에서도 정적 웹 호스팅 기능을 이용하여  
 웹 호스팅을 한다고 했을 때 Github의 특정 branch에 push가 일어나면  
 새로운 코드를 S3에 업로드하는 내용을 담은 설명입니다.
<hr/>

<h2>CD 구축하기</h2>

- 여느 Github Action과 마찬가지로 이 Github Action도  
  `.github/workflows` 하에 작성한다. 아래 yml 파일을 보자.

```yml
name: CD

on:
  push:
    branches: [master]

jobs:
  deploy:
    name: Build and Upload to Amazon S3
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v2

      - name: Configure AWS Credentials
        uses: aws-actions/configure-aws-credentials@v1
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ${{ secrets.AWS_S3_REGION }}

      - name: Install External Modules
        run: yarn install

      - name: Build
        run: yarn build

      - name: Upload Build Directory to Amazon S3
        run: aws s3 cp build s3://${{secrets.AWS_S3_BUCKET}} --recursive --acl public-read
```

- 생각보다 수행해야 하는 작업이 별로 없다.  
  우선 `Install External Modules`에서는 `yarn install` 명령어를 사용하여  
  해당 프로젝트가 사용하는 npm 패키지들을 Github Action이 수행되는 가상 머신에 설치한다.  
  다음으로는 `Build` 단계가 있는데, 이 단계에서는 `yarn build`를 통해 프로젝트를 프로덕트 스테이지로  
  빌드한다. 만약 yarn을 사용하지 않는다면 알맞은 명령어를 사용하면 된다.  
  마지막으로 `Upload Build Directory to Amazon S3`에서는 빌드된 파일을 S3의 Bucket에 실제로  
  업로드 하는 과정을 말한다. 명령어 중 `--acl public-read` 옵션은 해당 Bucket내에 업로드 되는 새로운  
  객체들에 대해서 퍼블릭 액세스가 가능하도록 설정하는 것이다.

- 팁이라면 만약 `Build` 단계에서 `yarn build`가 수행되는데, 이 명령어는 에러가 아니라  
 warning이 하나라도 있으면 빌드를 실패 처리한다. 당연히 warning은 없애는게 맞지만,  
 불가피하게 warning을 제거할 수 없는 상황이라면 명령어 앞에 `CI=false` 플래그를 주면 된다.  
 즉, `Build` 단계에서 수행할 명령어를 `CI=false yarn build`로 지정하면 된다.
<hr/>
