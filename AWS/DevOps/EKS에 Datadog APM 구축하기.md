# EKS에 Datadog APM 구축하기

## helm으로 Datadog agent 설치하기

- 이 [문서](https://docs.datadoghq.com/containers/kubernetes/installation/?tab=helm)에 나와있는대로 datadog agent의 helm repository를 추가하자.

```sh
helm repo add datadog https://helm.datadoghq.com
helm repo update
```

- 이후 Kubernetes에 설치될 datadog agent가 사용할 파일은 [values.yaml](https://github.com/DataDog/helm-charts/blob/main/charts/datadog/values.yaml)인데, 여기서 특정 값만 찾아서 수정해주자.

  - `registry`: 주석에 적혀있는 값들 중 AWS에 해당하는 값을 지정한다.
  - `datadog.apiKey`: Datadog에서 발급받은 API key를 입력한다.
  - `datadog.apm.portEnabled`: true로 수정한다.

- 다음으로 datadog agent를 설치해보자.

```sh
helm install datadog-agent -f values.yaml datadog/datadog
```

---
