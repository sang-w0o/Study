# Github Actions로 AWS 배포 시 IAM Role 사용하기

## 흔히 보이는 Github Actions workflow 파일 및 문제점

- Github Actions를 사용해 AWS에 배포할 때, 일반적으로 아래와 같이 `@aws-actions/configure-aws-credentials`를  
  활용해 AWS 관련 인증을 수행한다.

```yml
# trigger
name: Example of using AWS

jobs:
  deploy:
    name: Deploy to AWS
    runs-on: ubuntu-latest
  steps:
    - name: Checkout
      uses: actions/checkout@v2

    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v1
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: ${{ secrets.AWS_REGION }}
```

- 위와 같이 로그인하는 과정은 아래의 절차를 선행해야 한다.

  - (1) 필요한 권한을 가진 IAM User 생성 후 access key, secret access key 발급
  - (2) access key, secret access key 값을 Github secret에 등록

- 하지만 위와 같이 IAM user의 credential을 바로 사용하는 것은 아래와 같은 문제점이 있다.

  - 과연 Github 자체가 안전한가? Github도 언제든지 보안 사고가 일어날 수 있는 소프트웨어이고, 이러한 소프트웨어에  
    IAM credential을 저장하는 것이 위험할 수도 있다. 아무리 least-privilage를 적용해도 위험하다. IAM credential을  
    탈취한 누군가가 마음대로 배포를 할 수 있기 때문이다.

---

## IAM Role을 사용해 개선하기

### (1) Identity Provider 생성

- Identity Provider(IdP)는 간단히 말해 AWS 외부의 주체에게 해당 주체가 AWS 리소스에 접근할 수 있는 권한을 부여하는 기능이다.

- 먼저 AWS IAM console에 접속해 좌측에 `Identity providers` 탭에 들어가 `Add provider`를 클릭해 새로운 provider를 추가해보자.

  - `Provider type`: `OpenID Connect`를 선택한다.
  - `Provider name`: `token.actions.githubusercontent.com` 을 입력한다. 이 provider는 Github action이  
    수행될 때마다 token을 발급해주는 역할을 한다.
  - `Audience`: `sts.amazonaws.com` 을 입력한다. 이는 AWS가 Github에서 발급한 token을 인식할 수 있도록 하는 역할을 한다.

  ![picture 1](/images/DEVOPS_AWS_USING_IAM_ROLE_IN_GH_ACTIONS_1.png)

### (2) IAM Role 생성

- 이제 Github Actions가 실행되는 환경에게 적용할 IAM Role을 생성할 차례이다.  
   IAM console에서 `Roles` 탭에 들어가 `Create role`를 클릭해 새로운 role을 생성해보자.

  - 하단에 생기는 `Custom trust policy`에는 아래의 JSON 내용을 입력한다.

    ```json
    {
      "Version": "2012-10-17",
      "Statement": [
        {
          "Sid": "RoleForPlanitGithubActions",
          "Effect": "Allow",
          "Principal": {
            "Federated": "arn:aws:iam::12341234:oidc-provider/token.actions.githubusercontent.com"
          },
          "Action": "sts:AssumeRoleWithWebIdentity",
          "Condition": {
            "StringLike": {
              "token.actions.githubusercontent.com:sub": "repo:orgName/repoName:*"
            },
            "ForAllValues:StringEquals": {
              "token.actions.githubusercontent.com:aud": "sts.amazonaws.com",
              "token.actions.githubusercontent.com:iss": "https://token.actions.githubusercontent.com"
            }
          }
        }
      ]
    }
    ```

  - `Principal.Federated`의 값으로는 (1)에서 생성한 IdP의 ARN을 입력한다.
  - `token.actions.githubusercontent.com:sub`에는 해당 Role을 사용할 Github 레포지토리의 organization name 및  
    레포지토리 이름을 입력한다. 만약 특정 organization 하위의 모든 레포지토리에서 사용하도록 하고 싶으면 `repo:orgName/*:*`를  
    입력하면 된다.

  ![picture 2](/images/DEVOPS_AWS_USING_IAM_ROLE_IN_GH_ACTIONS_2.png)

- 다음으로 권한에는 Github action에서 AWS에 접근하기 위해 필요한 권한을 자유롭게 추가해주면 된다.

---
