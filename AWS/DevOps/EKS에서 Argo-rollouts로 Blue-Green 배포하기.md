# Argo-rollouts로 EKS에서 Blue-Green 배포하기

## 설치

- [문서](https://argoproj.github.io/argo-rollouts/installation/)에 따라 설치

```sh
kubectl create namespace argo-rollouts
kubectl apply -n argo-rollouts -f https://github.com/argoproj/argo-rollouts/releases/latest/download/install.yaml
```

---
