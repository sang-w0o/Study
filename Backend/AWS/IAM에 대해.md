<h1>AWS의 IAM(Identity Access Management)</h1>

- 이 문서는 AWS의 공식 문서를 참조해서 정리한 것입니다.  
  <a href="https://docs.aws.amazon.com/ko_kr/IAM/latest/UserGuide/id.html">공식 문서 보기</a>

<h2>IAM(Identity Access Management)이란?</h2>

- AWS IAM은 AWS에서 생성하는 Entity로써 AWS의 다양한 서비스와 상호작용하기 위해  
  그 Entity를 사용하는 사람 또는 애플리케이션을 나타낸다.

<h3>AWS가 IAM 사용자를 식별하는 방법</h3>

- 사용자를 생성하면 IAM은 그 사용자를 식별하기 위한 방법을 아래와 같이 생성한다.

  - 사용자 생성 시 지정한 이름 확인
  - 사용자의 Amazon 리소스 이름(ARN) 확인
  - 사용자 각각의 고유 식별자 (AWS CLI를 사용하는 경우에 해당)

- `Root User`는 말그대로 Root 계정으로, AWS의 모든 서비스에 대해 접근이 가능하다.  
 반면에 `IAM User`는 특정 서비스에 대한 특정 권한만을 부여하여 특정 서비스에 대해서만  
 작업을 수행할 수 있도록 한다.
<hr/>

<h2>IAM 사용자 만들기</h2>

- AWS에서 IAM 사용자를 만드는 것은 아래 링크에 자세히 설명되어 있다.  
  <a href="https://docs.aws.amazon.com/ko_kr/IAM/latest/UserGuide/id_users_create.html">링크</a>

- 가장 중요한 것은 IAM 사용자를 만들고 그 사용자를 통해 AWS API를 이용하는 작업을 하려면  
 사용자를 생성하고 나서 보이는 `Access Key`를 사용해야 하는데, 이 키 (2개)는 그 화면에서만 볼 수 있고  
 나중에는 볼 수 없으므로 항상 저장을 해놓아야 한다. 만약 저장하지 않아서 잊어버렸다면  
 새로운 키를 발급받아 사용하는 방법 밖에 없다.
<hr/>
