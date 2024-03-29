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

## ArgoCD API Server 접근하기

- 우선 ArgoCD API Server도 Fargate에서 실행되기에 service 타입을 NodePort로 바꿔주자.

  ```sh
  kubectl patch svc argocd-server -n argocd -p '{"spec": {"type": "NodePort"}}'
  ```

- 다음으로 Ingress 설정을 해줄 것이다.

- Argo CD API server는 CLI에 의해 사용되는 gRPC server도 실행하고, 그와 동시에 UI에 의해 사용되는 HTTP/HTTPS 서버도 실행한다.
  이 3가지 프로토콜은 아래의 port 번호들로 argocd-server service로 노출된다.

  - 443: gRPC/HTTPS
  - 80: HTTP (redirects to HTTPS)

- AWS ALB를 사용할 때 argocd-server service를 위한 또다른 service를 생성하는 것이 좋다.
  이는 ALB가 gRPC 트래픽과 UI 트래픽을 각각 다른 target group으로 보내기 위함이다.

> AWS ALB는 L7 Load Balancer로 gRPC, HTTP/HTTPS를 모두 처리할 수 있다.

- 우선 아래 yaml 파일을 사용해 gRPC 트래픽만 처리하는 새로운 service를 생성하자.

  ```yaml
  apiVersion: v1
  kind: Service
  metadata:
  annotations:
    alb.ingress.kubernetes.io/backend-protocol-version: HTTP2
  labels:
    app.kubernetes.io/name: argocd-server
  name: argogrpc
  namespace: argocd
  spec:
  ports:
    - name: "443"
      port: 443
      protocol: TCP
      targetPort: 8080
  selector:
    app.kubernetes.io/name: argocd-server
  sessionAffinity: None
  type: NodePort
  ```

- 그리고 아래처럼 Kubernetes Ingress yaml 파일을 작성 후 생성해주자.

  ```yaml
  apiVersion: networking.k8s.io/v1
  kind: Ingress
  metadata:
  name: argocd-ingress
  namespace: argocd
  annotations:
    alb.ingress.kubernetes.io/load-balancer-name: argocd-alb
    alb.ingress.kubernetes.io/scheme: internet-facing
    alb.ingress.kubernetes.io/target-type: ip
    alb.ingress.kubernetes.io/subnets: ${PUBLIC_SUBNET_IDs}
    alb.ingress.kubernetes.io/certificate-arn: ${ACM_CERTIFICATE_ARN}
    alb.ingress.kubernetes.io/security-groups: ${ALB_SECURITY_GROUP_ID}
    alb.ingress.kubernetes.io/backend-protocol: HTTPS
    alb.ingress.kubernetes.io/conditions.argogrpc: |
      [{"field":"http-header","httpHeaderConfig":{"httpHeaderName": "Content-Type", "values":["application/grpc"]}}]
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTPS":443}]'
  spec:
  ingressClassName: alb
  rules:
    - host: dev-argocd.planit-study.com
      http:
        paths:
          - path: /
            backend:
              service:
                name: argogrpc
                port:
                  number: 443
            pathType: Prefix
          - path: /
            backend:
              service:
                name: argocd-server
                port:
                  number: 443
            pathType: Prefix
  ```

- 이때 위에서 생성한 Ingress를 describe해보면, 아래와 같이 pod는 8080번 포트를 사용함을 알 수 있다.

  ![picture 30](/images/AWS_DEVOPS_EKS_ARGOCD_2.png)

- 따라서 ArgoCD API server가 실행되는 pod에 8080번 포트의 inbound 요청이 허용되는 security group을 설정해줘야 한다.

- 아래 yaml 파일로 8080번 포트 열어주기(argocd-server, argogrpc)

```yaml
apiVersion: vpcresources.k8s.aws/v1beta1
kind: SecurityGroupPolicy
metadata:
  name: argo-sgp
  namespace: argocd
spec:
  podSelector:
    matchLabels:
      app.kubernetes.io/name: argocd-server
  securityGroups:
    groupIds:
      - ${NEW_SECURITY_GROUP_ID}
      - ${EKS_CLUSTER_SECURITY_GROUP_ID}
```

- 위 파일을 apply한 후 `kubectl describe ingress argocd-ingress -n argocd`에서 전달되는 IP 주소를 가진 pod를 모두  
  죽여 재생성되도록 하자. 해당 pod id는 `kubectl get pods -n argocd -o=wide`로 확인할 수 있다. 확인 후 `kubectl delete pod $POD_ID` 실행하자.

- 이후 ArgoCD API Server를 위한 Kubernetes Ingress에서 지정했던 host인 `dev-argocd.planit-study.com`로 접근하면 아래처럼 ArgoCD에 접근할 수 있게 된다.

  ![picture 31](/images/AWS_DEVOPS_EKS_ARGOCD_3.png)

---

## 로그인하기

- 처음 ArgoCD를 생성하면 ArgoCD가 자동으로 admin username에 대해 비밀번호를 설정해 놓는다.  
  아래 명령어로 확인할 수 있다.

  ```sh
  kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d; echo
  ```

- 이제 아래 명령어로 로그인을 수행해보자.

  ```sh
  ❯ argocd login dev-argocd.planit-study.com
  # Username: admin
  # Password:
  # 'admin:login' logged in successfully
  # Context 'dev-argocd.planit-study.com' updated
  ```

- 아래 명령어로 비밀번호를 바꿀 수 있다.

  ```sh
  argocd account update-password
  ```

> 참고로 처음 ArgoCD가 생성한 비밀번호는 `argocd-initial-admin-secret`라는 이름의 Kubernetes Secret에 저장되어 있다.  
> 비밀번호를 갱신하고 나서는 아래 명령어로 이를 삭제해주도록 하자.
>
> ```sh
> kubectl -n argocd delete secret argocd-initial-admin-secret
> ```

---

## Github repository 연동 및 애플리케이션 설정하기

> Private repository의 경우, username에는 github username을 명시하고 password에는 Personal Access Token을 명시하면 된다.  
> PAT를 발급 받는 방법은 [여기](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token)에 있다.

- Path에는 Kubernetes 리소스 파일들이 명시된 디렉토리를 나타낸다.  
  나의 경우 [이 레포지토리](https://github.com/Example-Collection/Spring-Boot-on-EKS)를 사용했는데, `k8s/` 폴더 내에 리소스 파일들이 모두  
  존재해서 path로 k8s를 지정했다.

- ArgoCD는 애플리케이션 단위로 구성하는데, 설정 값은 아래와 같다.

  ![picture 32](/images/AWS_DEVOPS_EKS_ARGOCD_4.png)

- 애플리케이션에 사용되는 프로젝트의 설정 값은 아래와 같다.

  ![picture 33](/images/AWS_DEVOPS_EKS_ARGOCD_5.png)

- 이제 특정 Kubernetes namespace 내의 모든 리소스를 관리할 수 있다.

  ![picture 34](/images/AWS_DEVOPS_EKS_ARGOCD_6.png)

---
