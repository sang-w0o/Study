# EKS 배포하기

- 이 글은 사이드 프로젝트로 개발하는 Spring Boot application을 EKS + Fargate로 배포했던 과정을 설명한다.

- 모든 과정은 [AWS Builders 2022 - EKS](https://awskocaptain.gitbook.io/aws-builders-eks/)를 참고해 진행되었으므로, 이 글의 과정과 매우 유사하다.

## (1) - 자격 증명 및 환경 설정

- aws cli가 설치되어 있다는 가정 하에, AWS console에서 EKS를 사용하기 위한 권한 등을 포함한 IAM 사용자를 생성하고,  
  로컬 터미널에서 credential을 설정한다.

> 나는 테스트를 위해 `AdministratorAccess` 권한을 부여했다.

- kubectl, eksctl, k9s는 환경에 맞게 설치해준다.

### KMS 설정

- 암호화 key를 K8S에서 편리하기 사용하기 위해 손쉽게 암호화 key를 생성, 관리해주며 다양한 AWS의 서비스들과 연동할 수 있도록 하는  
  AWS KMS를 사용하도록 한다.

  ```sh
  aws kms create-alias --alias-name alias/planit-eks --target-key-id $(aws kms create-key --query KeyMetadata.Arn --output text)
  ```

- AWS Console에서 KMS에 접속해 생성된 CMK(Customer-Managed Key) 정보를 확인한다.

  ![picture 5](/images/AWS_DEVOPS_EKS_1.png)

- 생성된 CMK의 ARN을 사용해 접근하게 되므로, 아래처럼 환경 변수로 지정해준 후 별도의 파일에 저장하도록 한다.

  ```sh
  export MASTER_ARN=$(aws kms describe-key --key-id alias/planit-eks --query KeyMetadata.Arn --output text)
  echo $MASTER_ARN > master_arn.txt
  ```

---

## (2) - VPC 설정

![picture 6](/images/AWS_DEVOPS_EKS_2.png)

- 위 사진과 같은 구성을 eksctl과 CloudFormation으로 구성해볼 것이다.

- CloudFormation이 사용하는 yml파일에는 VPC, AZ, Subnet, Routing Table등을 구성하고, eksctl로 사전에 정의된 yaml을  
  통해 public node group, 그리고 private node group을 구성한다.

- 여기서 생성할 VPC에는 NAT Gateway가 3개 사용되므로 총 3개의 EIP가 사용된다.  
  기본적으로 한 계정에서 사용할 수 있는 EIP의 최대 개수는 5개 이므로 EIP가 3개 이상 여유가 있어야 배포가 가능하다.

- [eks-vpc-3az.yaml](https://gist.github.com/sang-w0o/9e189c34bb25e19896703f156f0da507)은 실행 시 아래의 리소스들을 생성한다.

  - VPC
  - 3개의 private subnet
  - 3개의 public subnet
  - Internet Gateway
  - 3개의 NAT Gateway
  - 3개의 Public Route Table
  - 3개의 Private Route Table
  - ControlPlane을 위한 security group

- 아래 명령어로 실행시켜보자.

  ```sh
  aws cloudformation deploy \
    --stack-name "planit-eks-vpc" \
    --template-file "eks-vpc-3az.yaml" \
    --capabilities CAPABILITY_NAMED_IAM
  ```

- 대략 5분 뒤, AWS Console의 CloudFormation에 접속하면 아래처럼 stack이 생성된 모습을 볼 수 있다.

  ![picture 7](/images/AWS_DEVOPS_EKS_3.png)

### Stack output 확인하기

- 위 화면에서 `Outputs`로 가보면, 아래의 리소스들이 생성된 것을 확인할 수 있다.

- 우리는 여기서 VPC id, Subnet id가 필요하다. 아래처럼 총 3개의 private subnet, 3개의 public subnet이 생성되었다.

  ![picture 8](/images/AWS_DEVOPS_EKS_4.png)

- VPC 구성은 완료되었지만, worker node들은 아직 생성되지 않았다. 이를 eksctl을 사용해 생성해보자.

---

## (3) - eksctl 기반 k8s 설정

- 위에서 생성한 VPC의 id, subnet id들은 이후에도 많이 사용되므로 아래 명령어를 실행해 환경 변수에 저장 및 별도 파일에 저장하도록 하자.

```sh
#VPC ID export
export vpc_ID=$(aws ec2 describe-vpcs --filters Name=tag:Name,Values=planit-eks-vpc | jq -r '.Vpcs[].VpcId')
echo $vpc_ID

#Subnet ID, CIDR, Subnet Name export
aws ec2 describe-subnets --filter Name=vpc-id,Values=$vpc_ID | jq -r '.Subnets[]|.SubnetId+" "+.CidrBlock+" "+(.Tags[]|select(.Key=="Name").Value)'
echo $vpc_ID > vpc_subnet.txt
aws ec2 describe-subnets --filter Name=vpc-id,Values=$vpc_ID | jq -r '.Subnets[]|.SubnetId+" "+.CidrBlock+" "+(.Tags[]|select(.Key=="Name").Value)' >> vpc_subnet.txt
cat vpc_subnet.txt

# VPC, Subnet ID 환경변수 저장
export PublicSubnet01=$(aws ec2 describe-subnets --filter Name=vpc-id,Values=$vpc_ID | jq -r '.Subnets[]|.SubnetId+" "+.CidrBlock+" "+(.Tags[]|select(.Key=="Name").Value)' | awk '/planit-eks-vpc-PublicSubnet01/{print $1}')
export PublicSubnet02=$(aws ec2 describe-subnets --filter Name=vpc-id,Values=$vpc_ID | jq -r '.Subnets[]|.SubnetId+" "+.CidrBlock+" "+(.Tags[]|select(.Key=="Name").Value)' | awk '/planit-eks-vpc-PublicSubnet02/{print $1}')
export PublicSubnet03=$(aws ec2 describe-subnets --filter Name=vpc-id,Values=$vpc_ID | jq -r '.Subnets[]|.SubnetId+" "+.CidrBlock+" "+(.Tags[]|select(.Key=="Name").Value)' | awk '/planit-eks-vpc-PublicSubnet03/{print $1}')
export PrivateSubnet01=$(aws ec2 describe-subnets --filter Name=vpc-id,Values=$vpc_ID | jq -r '.Subnets[]|.SubnetId+" "+.CidrBlock+" "+(.Tags[]|select(.Key=="Name").Value)' | awk '/planit-eks-vpc-PrivateSubnet01/{print $1}')
export PrivateSubnet02=$(aws ec2 describe-subnets --filter Name=vpc-id,Values=$vpc_ID | jq -r '.Subnets[]|.SubnetId+" "+.CidrBlock+" "+(.Tags[]|select(.Key=="Name").Value)' | awk '/planit-eks-vpc-PrivateSubnet02/{print $1}')
export PrivateSubnet03=$(aws ec2 describe-subnets --filter Name=vpc-id,Values=$vpc_ID | jq -r '.Subnets[]|.SubnetId+" "+.CidrBlock+" "+(.Tags[]|select(.Key=="Name").Value)' | awk '/planit-eks-vpc-PrivateSubnet03/{print $1}')
echo "export vpc_ID=${vpc_ID}" | tee -a ~/.bash_profile
echo "export PublicSubnet01=${PublicSubnet01}" | tee -a ~/.bash_profile
echo "export PublicSubnet02=${PublicSubnet02}" | tee -a ~/.bash_profile
echo "export PublicSubnet03=${PublicSubnet03}" | tee -a ~/.bash_profile
echo "export PrivateSubnet01=${PrivateSubnet01}" | tee -a ~/.bash_profile
echo "export PrivateSubnet02=${PrivateSubnet02}" | tee -a ~/.bash_profile
echo "export PrivateSubnet03=${PrivateSubnet03}" | tee -a ~/.bash_profile
source ~/.zshrc
```

- 위 명령어를 실행하면 vpc id, subnet id가 모두 환경 변수에 저정되며 `vpc_subnet.txt`에 저장된다.  
  아래는 `vpc_subnet.txt` 파일의 예시 모습이다.

```
vpc-0e88a2ed7a32c0336
subnet-02b5356084f4355cb 10.11.16.0/20 planit-eks-vpc-PublicSubnet02
subnet-0ea280f1567234a3b 10.11.253.0/24 planit-eks-vpc-TGWSubnet03
subnet-0a79d22a3acf610bf 10.11.32.0/20 planit-eks-vpc-PublicSubnet03
subnet-0c96f4c64e724524b 10.11.251.0/24 planit-eks-vpc-TGWSubnet01
subnet-0d5d255e8542cf405 10.11.64.0/20 planit-eks-vpc-PrivateSubnet02
subnet-0c3c37774542ac95c 10.11.252.0/24 planit-eks-vpc-TGWSubnet02
subnet-07f97eaa984b5ced2 10.11.0.0/20 planit-eks-vpc-PublicSubnet01
subnet-0ebb353a91c36ab17 10.11.48.0/20 planit-eks-vpc-PrivateSubnet01
subnet-0e3c3abe52dbe07e8 10.11.80.0/20 planit-eks-vpc-PrivateSubnet03
```

- 마지막으로 aws region을 환경 변수로 넣어두자.

  ```sh
  export AWS_REGION=ap-northeast-2
  ```

- 이전 단계에서 환경 변수로 설정했던 VPC id, subnet id, region, kms master arn은 eksctl을 통해 EKS cluster를 배포하는 데 사용된다.

### Cluster 생성

- Cluster를 생성하기 전, 몇 개의 추가적인 환경 변수를 설정하고 잘 나오는지 테스트해보자.

```sh
export ekscluster_name="planit-dev-eks"
export eks_version="1.21"
export instance_type="t3a.medium"
echo ${AWS_REGION}
echo ${eks_version}
echo ${PublicSubnet01}
echo ${PublicSubnet02}
echo ${PublicSubnet03}
echo ${PrivateSubnet01}
echo ${PrivateSubnet02}
echo ${PrivateSubnet03}
echo ${MASTER_ARN}
```

- [이 공식문서](https://docs.aws.amazon.com/eks/latest/userguide/fargate-profile.html)에 나와있는 것처럼 fargate는 private subnet에만 배정할 수 있다.

  ![picture 9](/images/AWS_DEVOPS_EKS_5.png)

- 이제 EKS에 cluster를 생성해볼 것인데, [eks-cluster-3az.yaml](https://gist.github.com/sang-w0o/e095a70170cf7e699377bde5e09f1b7b)을 사용해 생성해자.

- 먼저 아래 명령을 실행한다.

```sh
cat << EOF > eks-cluster-3az.yaml
# A simple example of ClusterConfig object:
---
apiVersion: eksctl.io/v1alpha5
kind: ClusterConfig

metadata:
  name: ${ekscluster_name}
  region: ${AWS_REGION}
  version: "${eks_version}"

vpc:
  id: ${vpc_ID}
  subnets:
    private:
      PrivateSubnet01:
        az: ${AWS_REGION}a
        id: ${PrivateSubnet01}
      PrivateSubnet02:
        az: ${AWS_REGION}b
        id: ${PrivateSubnet02}
      PrivateSubnet03:
        az: ${AWS_REGION}c
        id: ${PrivateSubnet03}

secretsEncryption:
  keyARN: ${MASTER_ARN}

fargateProfiles:
  - name: planit-dev-fp
    selectors:
      - namespace: planit-dev
      - namespace: kube-system
    subnets:
      - ${PrivateSubnet01}
      - ${PrivateSubnet02}
      - ${PrivateSubnet03}

cloudWatch:
  clusterLogging:
    enableTypes:
      ["api", "audit", "authenticator", "controllerManager", "scheduler"]

EOF
```

> fargateProfiles에 fargate profile을 만들어야 리소스들이 fargate 상에서 생성된다.  
> 이때 selectors가 있는데 selectors의 조건 중 하나라도 만족시키는 리소스가 fargate에서 실행되게 된다.  
> 위에서는 애플리케이션이 실행될 namespace인 `planit-dev`, 그리고 이후에 볼 aws-load-balancer-controller 등의 리소스가 실행될  
> namespace인 `kube-system`을 지정해주었다. 만약 지정하지 않으면 아래와 같이 pod가 실행될 공간이 지정되어 있지 않아 실행되지 않는다.
>
> ```
> ingress 0/2 nodes are available: 2 node(s) had taint {eks.amazonaws.com/compute-type: fargate}, that the pod didn't tolerate.
> ```

- 이제 아래 명령어로 eks cluster를 생성해보자.

```sh
eksctl create cluster --config-file=eks-cluster-3az.yaml
```

### kubectl로 연결하기

- eksctl로 클러스터는 만들어졌지만, 아래의 에러가 마지막에 출력된다.

  ![picture 10](/images/AWS_DEVOPS_EKS_6.png)

- 일단 아래 명령어로 kubeconfig를 설정해주자.

```sh
aws eks update-kubeconfig --region ap-northeast-2 --name planit-dev-eks
```

- 그리고 kubectl 명령어를 실행해보면, 아래의 에러가 발생한다.

  ```sh
  # kubectl get svc
  error: exec plugin: invalid apiVersion "client.authentication.k8s.io/v1alpha1"
  ```

  > 심지어 `kubectl version` 명령어를 수행해도 동일한 에러가 발생한다.

- 이 문제는 kubernetes의 버전과 kubectl의 버전이 불일치해서 발생하는데, 현재 우리는 kubenetes를 1.21로 가동했다.  
   따라서 이를 지원하는 kubectl 버전 1.22로 설치해 사용하도록 하자.

  > kubectl 버전과 kubernetes 버전 관련 문서는 [여기](https://docs.aws.amazon.com/eks/latest/userguide/install-kubectl.html)에서 확인할 수 있다.

  - kubectl 1.22를 설치하는 과정은 아래와 같다.

  ```sh
  curl -o kubectl https://s3.us-west-2.amazonaws.com/amazon-eks/1.22.6/2022-03-09/bin/darwin/amd64/kubectl
  chmod +x ./kubectl
  mkdir -p $HOME/bin && cp ./kubectl $HOME/bin/kubectl && export PATH=$HOME/bin:$PATH
  echo 'export PATH=$PATH:$HOME/bin' >> ~/.zshrc
  kubectl version --short --client
  kubectl version
  ```

- 올바른 kubectl 버전을 설치한 후 `kubectl get svc`를 수행하면, 아래의 내용이 출력된다.

  ```sh
  # kubectl get svc
  NAME         TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)   AGE
  kubernetes   ClusterIP   172.20.0.1   <none>        443/TCP   43m
  ```

---

## (4) - 애플리케이션 배포하기

- 우선 애플리케이션 관련 리소스들이 위치할 kubernetes namespace를 생성하자.  
  위에서 fargate profile을 생성할 때 selector에 `namespace: planit-dev`를 지정했는데, 이 값에 따라 `planit-dev`라는  
  namespace에 생성되는 리소스가 fargate로 생성되게 된다.

  ```sh
  kubectl create ns planit-dev
  ```

### AWS Load Balancer Controller add-on 설치하기

> 문서: [문서](https://docs.aws.amazon.com/eks/latest/userguide/aws-load-balancer-controller.html)

- ALB Load Balancer Controller는 Kubernetes cluster를 위한 AWS ELB를 관리해준다.  
  이 controller는 아래의 기능들을 제공한다.

  - Kubernetes `Ingress` 생성 시 AWS ALB 프로비저닝
  - Kubernetes `LoadBalancer` 생성 시 AWS NLB 프로비저닝

- ALB Load Balancere Controller를 생성하기 전에, OIDC provider를 먼저 연동해야 한다.

- 클러스터에 OIDC ID provider(IdP)를 설정하는 것은 클러스터 내에 실행되는 Fargate pod들이 IAM을 사용해 Service Account 기능을  
  사용하도록 하기 위함이다. 아래 명령어를 통해 클러스터에 OIDC provider를 설정해주자.

  ```sh
  eksctl utils associate-iam-oidc-provider --cluster planit-dev-eks --approve
  ```

- 결과는 아래와 같다.

  ```
  2022-08-23 16:04:39 [ℹ]  will create IAM Open ID Connect provider for cluster "planit-dev-eks" in "ap-northeast-2"
  2022-08-23 16:04:40 [✔]  created IAM Open ID Connect provider for cluster "planit-dev-eks" in "ap-northeast-2"
  ```

- 이제 본격적으로 EKS cluster에 AWS Load Balancer Controller를 배포하기 위해 아래의 과정들을 차례로 진행해보자.

  - (1) IAM Policy 생성

    - 아래 명령을 실행해 IAM Policy를 생성한다.

      ```sh
      curl -o iam_policy.json https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.4.3/docs/install/iam_policy.json

      aws iam create-policy \
      --policy-name AWSLoadBalancerControllerIAMPolicy \
      --policy-document file://iam_policy.json
      ```

  - (2) IAM Role 생성

    - AWS Load Balancer Controller를 위해 `aws-load-balancer-controller`라는 service account를 `kube-system`  
       namespace에 생성할 것이다. 아래는 eksctl을 사용해 이를 만드는 방식이다.

      ```sh
      eksctl create iamserviceaccount \
      --cluster=my-cluster \
      --namespace=kube-system \
      --name=aws-load-balancer-controller \
      --role-name "AmazonEKSLoadBalancerControllerRole" \
      --attach-policy-arn=arn:aws:iam::111122223333:policy/AWSLoadBalancerControllerIAMPolicy \
      --approve
      ```

  - (3) Helm V3를 사용해 AWS Load Balancer Controller를 설치한다.  
    Fargate 상에 controller를 배포할 때는 `cert-manager`에 의존하지 않는 Helm을 사용하는 것이 간편하다.

    - (a) `eks-charts` 레포지토리 추가

      ```sh
      helm repo add eks https://aws.github.io/eks-charts
      ```

    - (b) 로컬 레포지토리 갱신

      ```sh
      helm repo update
      ```

    - (c) AWS Load Balancer 설치

      ```sh
      helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
      -n kube-system \
      --set clusterName=planit-dev-eks \
      --set serviceAccount.create=false \
      --set serviceAccount.name=aws-load-balancer-controller \
      --set region=ap-northeast-2 \
      --set vpcId=vpc-0e88a2ed7a32c0336 \
      --set image.repository=602401143452.dkr.ecr.ap-northeast-2.amazonaws.com/amazon/aws-load-balancer-controller
      ```

### 애플리케이션 (진짜) 배포하기

#### 1. Secret 생성하기

- 내가 현재 띄우고 싶은 애플리케이션은 약 20개 이상의 환경 변수를 주입받아 사용한다. 그리고 이 값들 중에는 민감한 정보도 분명히  
  있기 때문에 configmap 대신 secret을 생성해 사용할 것이다.

> Secret은 base64 encoding되어 있는 값을 value로 지정해야 하는데, 아래 명령어로 encoding된 값을 받아 사용하도록 하자.  
> echo의 `-n` 옵션과 base64의 `-w 0` 옵션이 적용되지 않으면 기본적으로 newline이 뒤에 붙어 encoding된다.
>
> ```sh
> echo -n 'encoding되기 전 원래 값' | base64 -w 0
> ```

- 가장 먼저 secret을 생성해보자. [secret.yaml](https://gist.github.com/sang-w0o/de83fabd461eec7cdc97776d7a9bdb6a)처럼 환경 변수로 주입할 key, value들을 하나씩 상황에 맞게 채워놓는다.

- 이후 다음 명령어로 secret을 생성한다.

  ```sh
  kubectl apply -f secret.yaml -n planit-dev
  ```

#### 2. Pod 생성하기

- 다음으로 애플리케이션이 실행될 pod를 설정해보자. 파일은 아래와 같다.

  ```yaml
  apiVersion: apps/v1
  kind: Deployment
  metadata:
  name: planit-dev
  labels:
    app: planit
  namespace: planit-dev
  spec:
  replicas: 2
  selector:
    matchLabels:
      app: planit
  template:
    metadata:
      labels:
        app: planit
    spec:
      containers:
        - image: robbyra98/planit-dev:lamd64
          imagePullPolicy: Always
          name: planit-dev
          envFrom:
            - secretRef:
                name: planit-dev-secret
          ports:
            - containerPort: 8080
              protocol: TCP
  ```

- `app:planit`을 label로 지정했고, containerPort는 Spring boot가 실행되는 기본 포트 번호인 8080으로 지정했다.  
  `kubectl apply`를 해주면, pod가 생성된다.

> pod의 로그는 `kubectl -n NAMESPACE logs POD_NAME --follow`로 실시간으로 확인할 수 있다.
> 그리고 pod의 이름은 `kubectl -n NAMESPACE get pods`로 확인할 수 있다.

#### 3. Service 생성하기

- 다음으로 pod들을 관리해주는 service를 생성할 것인데, Fargate를 사용할 때는 무조건 Service의 타입이 `LoadBalancer`이거나  
  `NodePort`여야만 한다. 여기서는 `NodePort`를 사용해보도록 하자.

- 사용할 yaml 파일은 아래와 같다.

  ```yaml
  apiVersion: v1
  kind: Service
  metadata:
  name: planit-dev-svc
  namespace: planit-dev
  spec:
  selector:
    app: planit
  ports:
    - protocol: TCP
      port: 80
      targetPort: 8080
  type: NodePort
  ```

- Service가 외부에서 받는 요청을 80번 port에서 받도록 하고, pod의 8080번 port로 요청을 전달하도록 설정했다.  
  그리고 이전에 pod에 부여했던 label을 selector로 지정해주었다.

#### 4. Ingress 생성하기

- 다음으로는 ingress를 생성할 것인데, 이 ingress는 외부에서 요청을 받아 pod로 전달해주는 역할을 한다.

  - [Ingress 문서](https://kubernetes-sigs.github.io/aws-load-balancer-controller/v2.4/guide/ingress/annotations/)

- 바로 yaml 파일을 살펴보자.

  ```yaml
  apiVersion: networking.k8s.io/v1
  kind: Ingress
  metadata:
  name: planit-dev-ingress
  namespace: planit-dev
  annotations:
    alb.ingress.kubernetes.io/load-balancer-name: planit-dev-lb
    alb.ingress.kubernetes.io/scheme: internet-facing
    alb.ingress.kubernetes.io/target-type: ip
    alb.ingress.kubernetes.io/subnets: subnet-07f97eaa984b5ced2, subnet-02b5356084f4355cb, subnet-0a79d22a3acf610bf
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTPS": 443}]'
    alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:ap-northeast-2:598334522273:certificate/11ad2c0b-a72a-4c65-8a5b-e0d75db6afe5
    alb.ingress.kubernetes.io/security-groups: sg-0a18b0bdf0a324210
    alb.ingress.kubernetes.io/healthcheck-port: "8080"
    alb.ingress.kubernetes.io/healthcheck-path: /actuator/health
    alb.ingress.kubernetes.io/success-codes: "200"
  spec:
  ingressClassName: alb
  rules:
    - host: dev-eks-api.planit-study.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: planit-dev-svc
                port:
                  number: 80
  ```

> - 각 annotation에 대한 설명은 [여기](https://kubernetes-sigs.github.io/aws-load-balancer-controller/v2.4/guide/ingress/annotations/)에서 확인할 수 있다.

- 간단히 spec을 살펴보면, 나는 Route53에 record를 생성해 ALB로 연결해줄 것이기 때문에 host에는 Route53 record의 domain을 넣어주었다.
  그리고 그 아래의 내용은 모든 path의 요청을 `planit-dev-svc`라는 이름의 service로 전달해주겠다는 의미이다.

- 참고로 `alb.ingress.kubernetes.io/healthcheck-port` annotation의 경우, ALB가 수행하는 health check는 pod로 직접
  요청을 보내기 때문에 service의 port인 80이 아니라 pod의 port인 8080으로 설정해줘야 한다.

- 이전에 aws-load-balancer-controller를 설치했기 때문에, 위 파일을 apply하면 ALB가 생성된다.

  ![picture 12](/images/AWS_DEVOPS_EKS_7.png)

- 이 ALB는 외부의 요청을 받아 fargate pod로 전달해주기 때문에, pod와는 달리 public subnet에 위치시켜야 한다.
  생성된 listener rule과 target group을 차례로 보자.

  - ALB listener rule은 아래와 같다.

    ![picture 13](/images/AWS_DEVOPS_EKS_8.png)

  - 그리고 target group은 아래와 같다.

    ![picture 14](/images/AWS_DEVOPS_EKS_9.png)

> 참고로 처음에는 위 사진처럼 target group이 healthy하다고 나오지 않는다.
> 대신 unhealthy(request timed out)으로 나오는데, 이는 이후에 고칠 예정이다.

### 문제 및 해결 - unhealthy, 504 gateway timeout

- 위에서 언급했듯이 처음 위처럼 리소스를 생성하면, 정상적으로 ALB로의 요청이(Route53 domain도 마찬가지) 정상적으로 전달되지 않고,  
  504 gateway timeout이 발생한다. 그리고 이에 따라 ALB의 target group에 대한 health check도 실패한다.

- 이 문제는 pod가 8080번 port로의 inbound 요청을 허용하도록 security group이 구성되지 않았기 때문이다.  
  참고로 fargate는 managed node group, node group이 아닌 fargate profile로 관리하게 된다.

> AWS Console에 접속해 pod를 확인해도, 해당 pod들이 붙어있는 security group을 확인할 수 없기 때문에 별 수가 없다고  
> 생각이 들었는데, ~~개고생 및 무한한 삽질 후~~ 해결했다.

### 해결 - pod에 security group 부여하기

- [이 문서](https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/security-groups-for-pods.html#sg-pods-example-deployment)에 나와있는 방법을 따라하면 pod에 security group을 부여할 수 있다.

- 문서 내용을 요약하자면, pod에 붙힐 security group은 아래의 속성을 갖춰야만 한다.

  - Cluster security group으로부터의 TCP 53 inbound 허용
  - Cluster security group으로부터의 UDP 53 inbound 허용

- 우선 위 속성을 inbound rule에 추가하고, 우리가 원하는 8080번 port으로 inbound를 허용하는 규칙을 가지는 새로운  
  security group을 생성하자.

- 그리고 아래의 yaml 파일을 생성하고, 실행하자.

  ```yaml
  apiVersion: vpcresources.k8s.aws/v1beta1
  kind: SecurityGroupPolicy
  metadata:
  name: planit-dev-sgp
  namespace: planit-dev
  spec:
  podSelector:
    matchLabels:
      app: planit
  securityGroups:
    groupIds:
      - ${위에서 만든 security group의 id}
      - ${EKS Cluster에 적용되어 있는 security group의 id}
  ```

- 위 yaml 파일은 우리가 위에서 만든 pod들(`spec.podSelector.matchLabels`로 k8s가 판단한다)에게 security group을  
  적용하게 되는데, 주의할 점은 기존에 실행 중인 pod들에게는 영향을 끼치지 않기 때문에 기존에 실행되던 pod들을 죽이고 재생성해야 한다.

- 위 yaml 파일을 적용하고 pod를 재생성하면 정상적으로 ALB의 health check도 수행되고, 요청도 전달된다.

  ![picture 15](/images/AWS_DEVOPS_EKS_10.png)

---

### 기타

- 처음 EKS cluster를 생성하고, AWS Console에서 조회하면 root로 접속하더라도 아래의 경고가 뜨며 아무런 리소스도 보이지 않는다.

```

Your current user or role does not have access to Kubernetes objects on this EKS cluster This may be due to the current user or role not having Kubernetes RBAC permissions to describe cluster resources or not having an entry in the cluster’s auth config map.

```

- 이를 해결하기 위해 아래 명령어를 수행하자.

```sh
kubectl edit configmap aws-auth -n kube-system
```

- 그리고 `mapRoles`와 같은 depth에 아래의 `mapUsers`를 추가해준다.

  ```yaml
  mapUsers: |
  - userarn: arn:aws:iam::[account_id]:root
    groups:
    - system:masters
  ```

- 이후 AWS Console을 새로고침하면, 모든 리소스가 정상적으로 확인된다.

---
