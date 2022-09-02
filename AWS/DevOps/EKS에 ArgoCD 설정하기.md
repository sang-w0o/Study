# EKS에 ArgoCD 설정하기

- [ArgoCD 공식 문서](https://argo-cd.readthedocs.io/en/stable/getting_started/)를 EKS + Fargate를 사용하는 환경에 대해 적용해보자.

## ArgoCD 설치

- 아래 명령어로 ArgoCD를 위한 Kubernetes namespace를 생성하고, ArgoCD가 사용하는 각종 리소스를 생성하자.

```sh
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

- 아래처럼 어마어마한 리소스들이 생성된다.

  ![picture 29](/images/AWS_DEVOPS_EKS_ARGOCD_1.png)

---

## ArgoCD CLI 설치

- MacOS 기준으로 아래 명령어로 설치한다.

```sh
brew install argocd
```

---
