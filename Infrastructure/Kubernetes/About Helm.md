# About Helm

- Helm은 Kubernetes의 package manager중 하나로, 아래의 기능들을 제공한다.

  - 표준화되고 재사용 가능한 template 생성
  - 배포 오류 최소화
  - 애플리케이션 versioning
  - In-place upgrades

- Helm을 사용하면 복잡한 컨테이너 애플리케이션들을 단 하나의 command으로 설치, 업데이트, 그리고 삭제할 수 있다.  
  하나의 환경에서 다른 환경으로 복잡한 yaml manitest들을 옮겨야 하는 상황에 발생할 수 있는 실수들, 그리고  
  오류들도 최소화하면서 관리까지 가능하게끔 해준다.

---

## Common Helm tasks

- 일반적으로 helm을 사용한 작업들은 아래와 같다.

  - 애플리케이션 설치: Localhost에 Helm repository를 추가하고, 애플리케이션을 설치한다.
  - 애플리케이션 업그레이드: 업그레이드를 진행하고, 쉽게 rollback할 수 있다.
  - 애플리케이션 배포: 새로운 Helm chart를 만들어 배포한다.

---
