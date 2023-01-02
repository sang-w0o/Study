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

### (4) Secret 수정, ConfigMap 생성하기

- Argo-Rollouts는 알림(notification)을 위해 `Secret`, `ConfigMap`을 사용하는데,  
   우선 `Secret`부터 수정해보자. `kubectl edit -n argo-rollouts secret argo-rollouts-notification-secret` 명령어를  
   입력하고, 아래처럼 yaml 파일에 `stringData.slack-token`을 추가해주자.

  ```yaml
  apiVersion: v1
  kind: Secret
  metadata:
    name: argo-rollouts-notification-secret
  stringData:
    slack-token: $BOT_USER_OAUTH_TOKEN
  ```

- 다음으로는 argo-rollouts-notification-configmap이라는 `ConfigMap`을 수정해 알림을 Slack으로 전송할 것임을 지정하자.  
  아래의 yaml 파일을 작성 후 apply 해주면 된다.

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: argo-rollouts-notification-configmap
  namespace: argo-rollouts
data:
  service.slack: |
    token: $slack-token
  template.analysis-run-error: |
    message: Rollout {{.rollout.metadata.name}}'s analysis run is in error state.
    email:
      subject: Rollout {{.rollout.metadata.name}}'s analysis run is in error state.
    slack:
      attachments: |
          [{
            "title": "{{ .rollout.metadata.name}}",
            "color": "#ECB22E",
            "fields": [
            {
              "title": "Strategy",
              "value": "{{if .rollout.spec.strategy.blueGreen}}BlueGreen{{end}}{{if .rollout.spec.strategy.canary}}Canary{{end}}",
              "short": true
            }
            {{range $index, $c := .rollout.spec.template.spec.containers}}
              {{if not $index}},{{end}}
              {{if $index}},{{end}}
              {
                "title": "{{$c.name}}",
                "value": "{{$c.image}}",
                "short": true
              }
            {{end}}
            ]
          }]
  template.analysis-run-failed: |
    message: Rollout {{.rollout.metadata.name}}'s analysis run failed.
    email:
      subject: Rollout {{.rollout.metadata.name}}'s analysis run failed.
    slack:
      attachments: |
          [{
            "title": "{{ .rollout.metadata.name}}",
            "color": "#E01E5A",
            "fields": [
            {
              "title": "Strategy",
              "value": "{{if .rollout.spec.strategy.blueGreen}}BlueGreen{{end}}{{if .rollout.spec.strategy.canary}}Canary{{end}}",
              "short": true
            }
            {{range $index, $c := .rollout.spec.template.spec.containers}}
              {{if not $index}},{{end}}
              {{if $index}},{{end}}
              {
                "title": "{{$c.name}}",
                "value": "{{$c.image}}",
                "short": true
              }
            {{end}}
            ]
          }]
  template.analysis-run-running: |
    message: Rollout {{.rollout.metadata.name}}'s analysis run is running.
    email:
      subject: Rollout {{.rollout.metadata.name}}'s analysis run is running.
    slack:
      attachments: |
          [{
            "title": "{{ .rollout.metadata.name}}",
            "color": "#18be52",
            "fields": [
            {
              "title": "Strategy",
              "value": "{{if .rollout.spec.strategy.blueGreen}}BlueGreen{{end}}{{if .rollout.spec.strategy.canary}}Canary{{end}}",
              "short": true
            }
            {{range $index, $c := .rollout.spec.template.spec.containers}}
              {{if not $index}},{{end}}
              {{if $index}},{{end}}
              {
                "title": "{{$c.name}}",
                "value": "{{$c.image}}",
                "short": true
              }
            {{end}}
            ]
          }]
  template.rollout-aborted: |
    message: Rollout {{.rollout.metadata.name}} has been aborted.
    email:
      subject: Rollout {{.rollout.metadata.name}} has been aborted.
    slack:
      attachments: |
          [{
            "title": "{{ .rollout.metadata.name}}",
            "color": "#E01E5A",
            "fields": [
            {
              "title": "Strategy",
              "value": "{{if .rollout.spec.strategy.blueGreen}}BlueGreen{{end}}{{if .rollout.spec.strategy.canary}}Canary{{end}}",
              "short": true
            }
            {{range $index, $c := .rollout.spec.template.spec.containers}}
              {{if not $index}},{{end}}
              {{if $index}},{{end}}
              {
                "title": "{{$c.name}}",
                "value": "{{$c.image}}",
                "short": true
              }
            {{end}}
            ]
          }]
  template.rollout-completed: |
    message: Rollout {{.rollout.metadata.name}} has been completed.
    email:
      subject: Rollout {{.rollout.metadata.name}} has been completed.
    slack:
      attachments: |
          [{
            "title": "{{ .rollout.metadata.name}}",
            "color": "#18be52",
            "fields": [
            {
              "title": "Strategy",
              "value": "{{if .rollout.spec.strategy.blueGreen}}BlueGreen{{end}}{{if .rollout.spec.strategy.canary}}Canary{{end}}",
              "short": true
            }
            {{range $index, $c := .rollout.spec.template.spec.containers}}
              {{if not $index}},{{end}}
              {{if $index}},{{end}}
              {
                "title": "{{$c.name}}",
                "value": "{{$c.image}}",
                "short": true
              }
            {{end}}
            ]
          }]
  template.rollout-paused: |
    message: Rollout {{.rollout.metadata.name}} has been paused.
    email:
      subject: Rollout {{.rollout.metadata.name}} has been paused.
    slack:
      attachments: |
          [{
            "title": "{{ .rollout.metadata.name}}",
            "color": "#18be52",
            "fields": [
            {
              "title": "Strategy",
              "value": "{{if .rollout.spec.strategy.blueGreen}}BlueGreen{{end}}{{if .rollout.spec.strategy.canary}}Canary{{end}}",
              "short": true
            }
            {{range $index, $c := .rollout.spec.template.spec.containers}}
              {{if not $index}},{{end}}
              {{if $index}},{{end}}
              {
                "title": "{{$c.name}}",
                "value": "{{$c.image}}",
                "short": true
              }
            {{end}}
            ]
          }]
  template.rollout-step-completed: |
    message: Rollout {{.rollout.metadata.name}} step number {{ add .rollout.status.currentStepIndex 1}}/{{len .rollout.spec.strategy.canary.steps}} has been completed.
    email:
      subject: Rollout {{.rollout.metadata.name}} step number {{ add .rollout.status.currentStepIndex 1}}/{{len .rollout.spec.strategy.canary.steps}} has been completed.
    slack:
      attachments: |
          [{
            "title": "{{ .rollout.metadata.name}}",
            "color": "#18be52",
            "fields": [
            {
              "title": "Strategy",
              "value": "{{if .rollout.spec.strategy.blueGreen}}BlueGreen{{end}}{{if .rollout.spec.strategy.canary}}Canary{{end}}",
              "short": true
            },
            {
              "title": "Step completed",
              "value": "{{add .rollout.status.currentStepIndex 1}}/{{len .rollout.spec.strategy.canary.steps}}",
              "short": true
            }
            {{range $index, $c := .rollout.spec.template.spec.containers}}
              {{if not $index}},{{end}}
              {{if $index}},{{end}}
              {
                "title": "{{$c.name}}",
                "value": "{{$c.image}}",
                "short": true
              }
            {{end}}
            ]
          }]
  template.rollout-updated: |
    message: Rollout {{.rollout.metadata.name}} has been updated.
    email:
      subject: Rollout {{.rollout.metadata.name}} has been updated.
    slack:
      attachments: |
          [{
            "title": "{{ .rollout.metadata.name}}",
            "color": "#18be52",
            "fields": [
            {
              "title": "Strategy",
              "value": "{{if .rollout.spec.strategy.blueGreen}}BlueGreen{{end}}{{if .rollout.spec.strategy.canary}}Canary{{end}}",
              "short": true
            }
            {{range $index, $c := .rollout.spec.template.spec.containers}}
              {{if not $index}},{{end}}
              {{if $index}},{{end}}
              {
                "title": "{{$c.name}}",
                "value": "{{$c.image}}",
                "short": true
              }
            {{end}}
            ]
          }]
  template.scaling-replicaset: |
    message: Scaling Rollout {{.rollout.metadata.name}}'s replicaset to {{.rollout.spec.replicas}}.
    email:
      subject: Scaling Rollout {{.rollout.metadata.name}}'s replcaset to {{.rollout.spec.replicas}}.
    slack:
      attachments: |
          [{
            "title": "{{ .rollout.metadata.name}}",
            "color": "#18be52",
            "fields": [
            {
              "title": "Strategy",
              "value": "{{if .rollout.spec.strategy.blueGreen}}BlueGreen{{end}}{{if .rollout.spec.strategy.canary}}Canary{{end}}",
              "short": true
            },
            {
              "title": "Desired replica",
              "value": "{{.rollout.spec.replicas}}",
              "short": true
            },
            {
              "title": "Updated replicas",
              "value": "{{.rollout.status.updatedReplicas}}",
              "short": true
            }
            {{range $index, $c := .rollout.spec.template.spec.containers}}
              {{if not $index}},{{end}}
              {{if $index}},{{end}}
              {
                "title": "{{$c.name}}",
                "value": "{{$c.image}}",
                "short": true
              }
            {{end}}
            ]
          }]
  trigger.on-analysis-run-error: |
    - send: [analysis-run-error]
  trigger.on-analysis-run-failed: |
    - send: [analysis-run-failed]
  trigger.on-analysis-run-running: |
    - send: [analysis-run-running]
  trigger.on-rollout-aborted: |
    - send: [rollout-aborted]
  trigger.on-rollout-completed: |
    - send: [rollout-completed]
  trigger.on-rollout-paused: |
    - send: [rollout-paused]
  trigger.on-rollout-step-completed: |
    - send: [rollout-step-completed]
  trigger.on-rollout-updated: |
    - send: [rollout-updated]
  trigger.on-scaling-replica-set: |
    - send: [scaling-replicaset]
  defaultTriggers: |
    - on-scaling-replica-set
    - on-rollout-updated
    - on-rollout-step-completed
    - on-rollout-completed
    - on-rollout-paused
    - on-rollout-aborted
```

- 마지막으로 기존에 존재하는 Argo Rollout yaml 파일에 아래의 annotation을 추가해주자.

  ```yaml
  apiVersion: argoproj.io/v1alpha1
  kind: Rollout
  metadata:
  name: planit-deployment
  namespace: planit
  labels:
    app: planit
  annotations:
    notifications.argoproj.io/subscribe.on-rollout-step-completed.slack: "dev_운영서버_배포기록"
    notifications.argoproj.io/subscribe.on-rollout-updated.slack: "dev_운영서버_배포기록"
    notifications.argoproj.io/subscribe.on-rollout-completed.slack: "dev_운영서버_배포기록"
    notifications.argoproj.io/subscribe.on-scaling-replica-set.slack: "dev_운영서버_배포기록"
    notifications.argoproj.io/subscribe.on-rollout-paused.slack: "dev_운영서버_배포기록"
    notifications.argoproj.io/subscribe-on-rollout-aborted.slack: "dev_운영서버_배포기록"
  ```

---

## 테스트하기

- 테스트를 해보기 위해 컨테이너 이미지의 태그를 변경한 후 apply 해보자. 그럼 아래처럼 이전에 추가한 Slack App이 메시지를 보낸다.

  ![picture 66](/images/AWS_DEVOPS_ARGO_ROLLOUTS_SLACK_9.png)

---