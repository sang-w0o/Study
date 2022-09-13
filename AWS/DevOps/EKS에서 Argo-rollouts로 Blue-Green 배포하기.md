# Argo-rollouts로 EKS에서 Blue-Green 배포하기

## 설치

- [문서](https://argoproj.github.io/argo-rollouts/installation/)에 따라 설치

  ```sh
  kubectl create namespace argo-rollouts
  kubectl apply -n argo-rollouts -f https://github.com/argoproj/argo-rollouts/releases/latest/download/install.yaml
  ```

---

## 기존 Deployment 수정 및 Service 생성하기

- Argo rollouts manifest는 Kubernetes deployment와 매우 유사.  
  기존 deployment는 아래와 같았다.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: "DEPLOYMENT_NAME"
  namespace: "NAMESPACE"
  labels:
    app: foo
spec:
  replicas: 3
  selector:
    matchLabels:
      app: foo
  template:
    metadata:
      labels:
        app: foo
      name: foo
    spec:
      containers:
        - name: "CONTAINER_NAME"
          image: "MY_IMAGE"
          imagePullPolicy: Always
          envFrom:
            - secretRef:
                name: "SECRET_NAME"
          ports:
            - containerPort: 8080
              protocol: TCP
          resources:
            limits:
              cpu: 1
              memory: 4096Mi
            requests:
              cpu: 1
              memory: 4096Mi
```

- 위를 아래처럼 수정해 argo rollouts로 수정하자.

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Rollout
metadata:
  name: "DEPLOYMENT_NAME"
  namespace: "NAMESPACE"
  labels:
    app: foo
spec:
  replicas: 3
  selector:
    matchLabels:
      app: foo
  template:
    metadata:
      labels:
        app: foo
      name: foo
    spec:
      containers:
        - name: "CONTAINER_NAME"
          image: "MY_IMAGE"
          imagePullPolicy: Always
          envFrom:
            - secretRef:
                name: "SECRET_NAME"
          ports:
            - containerPort: 8080
              protocol: TCP
          resources:
            limits:
              cpu: 1
              memory: 4096Mi
            requests:
              cpu: 1
              memory: 4096Mi
  strategy:
    type: BlueGreen
      activeService: planit-service
      previewService: planit-service-preview
      autoPromotionEnabled: true
```

- 수정된 부분들은 아래와 같다.

  - apiVersion: `apps/v1`에서 `argoproj.io/v1alpha1`로 수정
  - kind: `Deployment`에서 `Rollout`로 수정
  - `spec.strategy.type` 추가
  - `spec.strategy.type.activeService` 추가
  - `spec.strategy.type.previewService` 추가
  - `spec.strategy.type.autoPromotionEnabled` 추가

- `spec.strategy.type.activeService`는 현재 사용되는 Kubernetes service의 이름을 지정한다.  
  그리고 `spec.strategy.type.previewService`는 새로운 이미지를 실행시킬 Kubernetes service의 이름을 지정한다.  
  이후 previewService가 안정화되면 기존 activeService로 변경해주는 작업을 argo-rollouts가 수행해준다.

- previewService를 처음에는 기존에 수행되던 activeService와 동일하게, 이름만 다르게 지정해 생성해주자.

- 이렇게 cluster에 apply하고, `kubectl argo rollouts get rollout $DEPLOYMENT_NAME`을 수행하면 아래처럼 나온다.

  ![picture 51](/images/AWS_DEVOPS_EKS_ARGOROLLOUT_BG_1.png)

---
