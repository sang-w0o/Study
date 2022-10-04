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
