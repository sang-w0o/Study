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

## CD 설정하기

- 우선 [EKS에 ArgoCD 설정하기](https://github.com/sang-w0o/Study/blob/master/AWS/DevOps/EKS%EC%97%90%20ArgoCD%20%EC%84%A4%EC%A0%95%ED%95%98%EA%B8%B0.md) 등으로 이미 ArgoCD를 사용하고 있다고 가정한다.

- ArgoCD가 제시하는 best practice는 소스 코드를 위한 레포지토리와 Kubernetes manifest 파일들을 위한 레포지토리를 분리하는 것이다.  
  그리고 이 규칙을 준수한다고 가정해보자.

- Argo Rollouts는 Blue-Green 배포만을 수행해주는 반면, 새로운 이미지를 받아오는 작업은 ArgoCD에서 수행해야 한다.  
  이를 위해 소스 코드 레포지토리에 webhook 설정을 해주자. 기본적으로 ArgoCD는 연결된 git repository에 대해 3분마다  
  polling을 수행해 새로운 변경사항이 있는지를 감지하지만, 이 webhook은 push가 발생했을 때 바로 ArgoCD에 변경 사항이  
  생겼음을 알려주는 역할을 한다.

  ![picture 53](/images/AWS_DEVOPS_EKS_ARGOROLLOUT_BG_3.png)

  - Content Type은 무조건 `application/json`이어야만 한다.

- 지금은 ArgoCD Web UI가 public하게 접근 가능하기 때문에, DDoS 등의 악성 공격에 대비하기 위해 Webhook secret을 설정하는 것이 좋다.  
   내 경우, 소스코드 저장소가 github이므로 아래 명령어로 argocd가 사용하는 secret을 수정하는데, `webhook.github.secret`를 설정한다.

  ```sh
  kubectl edit secret argocd-secret -n argocd
  ```

- 이제 소스코드 레포지토리에 변경 사항을 추가해 push 해보자.  
  이미지 태그를 변경해 apply하고 `kubectl argo rollouts get rollout $ROLLOUT_NAME -w` 을 수행하면 아래처럼 나온다.

- 이전에 언급했듯이 소스 코드 파일이 담긴 레포지토리와 Kubernetes manifest 파일이 정의된 레포지토리는 분리되어 있다.  
  그리고 소스 코드 레포지토리의 특정 branch에 push가 발생하면 ECR, Dockerhub 등에 이미지를 commit hash로 tagging해  
  빌드하고 push한다고 해보자.

- 새로운 image tag 값을 Argo-Rollout 정의 파일에서 수정해줘야 한다. 그리고 image tag는 소스 코드 레포지토리의 commit hash값이다.  
  이를 구현하기 위해 소스 코드 레포지토리에서 Kubernetes manifest 파일이 담긴 레포지토리에 tag를 변경한 PR을 자동으로 생성하도록 해보자.

- 우선 아래는 소스 코드 레포지토리에서 수행될 Github action의 행동을 정의한 파일인데, 이미지 빌드, push 후 K8S manifest 파일이  
  담긴 repository에 checkout한다.

```yml
on:
  push:
    branches: [master]

name: Deploy to Amazon EKS(Product)

jobs:
  deploy:
    name: Deploy
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v2

      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v1
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ${{ secrets.AWS_S3_REGION }}

      - name: build
        run: "애플리케이션 빌드 명령"

      - name: Login to Amazon ECR
        id: login-ecr
        uses: aws-actions/amazon-ecr-login@v1

      - name: Build, tag, and push image to Amazon ECR
        id: build-image
        env:
          ECR_REGISTRY: ${{ secrets.AWS_ECR_REPOSITORY }}
          ECR_REPOSITORY: planit_product
          IMAGE_TAG: ${{ github.sha }}
        run: |
          docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG .
          docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG
          echo "::set-output name=image::$ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG"

      # 아래에서 Planit-DevOps가 K8S manifest 파일이 담긴 레포지토리이다.
      - name: checkout Planit-DevOps
        uses: actions/checkout@v3
        with:
          repository: Planit-Study/Planit-DevOps
          path: ./Planit-DevOps
          token: ${{ secrets.GHB_PAT }}

      # 위에서 repository에 checkout하게 되면, Github action이 수행되는 곳의 `Planit-DevOps` 폴더에 해당 레포지토리의 내용이 들어간다.

      # 아래는 create-pr-devops.sh 파일을 실행해 K8S manifest 파일이 담긴 레포지토리에 PR을 생성한다.
      - name: Create PR for deployment
        env:
          GHB_PAT: ${{ secrets.GHB_PAT }}
          DEVOPS_REPO_URL: ${{ secrets.DEVOPS_REPO_URL }}
          COMMIT_HASH: ${{ github.sha }}
          GH_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          chmod +x ./cd-scripts/create-pr-devops.sh
          ./cd-scripts/create-pr-devops.sh
```

- 위 Github action이 사용하는 `create-pr-devops.sh`는 아래와 같다.

```sh
#!/bin/bash

git config --global user.email "robbyra@gmail.com"
git config --global user.name "sang-w0o"
git config --global user.password $GHB_PAT

cd ./Planit-DevOps/k8s

# image의 tag 수정
sed -i "s/ECR_REPOSITORY\/IMAGE_NAME:.*/ECR_REPOSITORY\/IMAGE_TAG:$COMMIT_HASH/" deployment-rollout.yaml

git remote set-url origin $DEVOPS_REPO_URL
git checkout -b "deploy/$COMMIT_HASH"
git add .
git commit -m "Deploy product image(tag: $COMMIT_HASH)"
git push origin "deploy/$COMMIT_HASH"

export GH_TOKEN=$GHB_PAT
gh auth login
gh pr create -B master --title "Deploy product image(tag: $COMMIT_HASH)" --body "Deploy product image(tag: $COMMIT_HASH)" -a "@me" -R PlanIt-Study/Planit-DevOps
```

- 위 2개 파일에서 사용되는 내용 중 `GHB_PAT`는 K8S manifest 파일이 담긴 레포지토리에 접근할 수 있는 권한을 가진 Github Personal Access Token 값이다.

- 이제 배포를 실행해보자.

- 처음에는 새로운 이미지를 실행하는 Kubernetes replicaset이 생성된다.

  ![picture 55](/images/AWS_DEVOPS_EKS_ARGOROLLOUT_BG_4.png)

- 다음으로 새로운 이미지를 실행하는 Kubernetes replicaset이 모두 성공적으로 running 상태가 된다.

  ![picture 56](/images/AWS_DEVOPS_EKS_ARGOROLLOUT_BG_5.png)

- 마지막으로 기존에 실행되던 Kubernetes replicaset은 제거(scale down)된다.

  ![picture 57](/images/AWS_DEVOPS_EKS_ARGOROLLOUT_BG_6.png)

---

## Dashboard 사용하기

- Rollout이 배포된 namespace에 대해 아래 명령을 수행하자.

```sh
kubectl argo rollouts dashboard -n $ROLLOUT_NAMESPACE
```

- 이후 `localhost:3100`에 접속하면, 아래처럼 볼 수 있다.

  ![picture 52](/images/AWS_DEVOPS_EKS_ARGOROLLOUT_BG_2.png)

---
