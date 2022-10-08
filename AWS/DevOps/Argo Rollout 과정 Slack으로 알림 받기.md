# Argo-Rollouts 배포 Slack 알림 받기

- 이 글은 아래의 문서들을 번역 및 정리한 글이다.

  - [공식 문서 - Notifications](https://argoproj.github.io/argo-rollouts/features/notifications/)
  - [공식 문서 - Notifications to Slack](https://argoproj.github.io/argo-rollouts/generated/notification-services/slack/)

---

## 파라미터

- token(필수) : Slack app token(필수)
- apiURL(선택) : 서버의 url (ex. `https://example.com/api`)
- username(선택) : Slack app username
- icon(선택) : Slack app 아이콘 (ex. `:robot_face:` 또는 `https://example.com/image.png`)
- insecureSkipVerify(선택) : bool 값 (`true`, `false`)

> apiURL, username, icon, insecureSkipVerify는 여기서 설정하지 않을 것이다.
> 추가로 나는 username, icon은 Slack app 설정 페이지에서 직접 설정해주었다.

---

## 설정하기

### (1) Slack App 생성

- `https://api.slack.com/apps/`에 접속해 `Create New App`을 클릭한다.

  ![picture 58](/images/AWS_DEVOPS_ARGO_ROLLOUTS_SLACK_1.png)

- 그리고 아래처럼 이름과 Slack workspace를 선택해주자.

  ![picture 59](/images/AWS_DEVOPS_ARGO_ROLLOUTS_SLACK_2.png)

### (2) Slack App 설정하기

- 위에서 App을 생성하면 자동으로 설정 페이지로 넘어가는데, 해당 페이지에서 `OAuth & Permissions` 탭을 클릭한다.  
  그리고 아래와 같이 `Scopes` 부분에 `chat:write` permission을 추가해주자.

  ![picture 60](/images/AWS_DEVOPS_ARGO_ROLLOUTS_SLACK_3.png)

- 그리고 왼쪽의 `App Home` 탭을 클릭해 아래와 같이 `Display Information`을 설정해주자.

  ![picture 62](/images/AWS_DEVOPS_ARGO_ROLLOUTS_SLACK_4.png)

- 이제 마지막으로 다시 `OAuth & Permissions` 탭으로 이동한 후 해당 페이지의 최상단으로 이동해 `Install to Workspace` 버튼을 클릭한다.

  ![picture 61](/images/AWS_DEVOPS_ARGO_ROLLOUTS_SLACK_5.png)

- 클릭 후에는 아래와 같이 Slack App을 Workspace에 설치할 것이라는 내용이 보인다.

  ![picture 63](/images/AWS_DEVOPS_ARGO_ROLLOUTS_SLACK_6.png)

- 설치가 완료되면 자동으로 `OAuth & Permissions` 페이지로 이동하는데, 이때 보이는 `Bot User OAuth Token`을 복사해두자.

  ![picture 64](/images/AWS_DEVOPS_ARGO_ROLLOUTS_SLACK_7.png)

### (3) Slack channel에 App 추가하기

- Slack channel에 위에서 만든 App을 추가해주자. 나의 경우 이 app이 `#dev_운영서버_배포기록` 이라는 channel에  
  Argo Rollout의 상태를 알려주도록 할 것이기에 해당 channel에 아래와 같이 추가해주었다.

  ![picture 65](/images/AWS_DEVOPS_ARGO_ROLLOUTS_SLACK_8.png)
