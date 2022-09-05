# EKS에 Prometheus, Grafana 설정하기

- 이 글은 [여기](https://github.com/sang-w0o/Study/blob/master/AWS/DevOps/EKS%20%EB%B0%B0%ED%8F%AC%ED%95%98%EA%B8%B0.md)에서 구축한 EKS Cluster에 Prometheus, Grafana를 설정하는 방법을 다룬다.  
  EKS, Fargate를 사용한다.

## Amazon EBS CSI Driver 설치

- Amazon EBS(Elastic Block Store) CSI(Container Storage Interface) Driver는 Amazon EKS cluster들이 PV(Persistent Volume)으로 Amazon EBS volume을 사용 및 관리할 수 있도록 CSI interface를 제공한다.

### IAM policy 설정하기

- 아래 명령어로 IAM policy document를 받는다.

  ```sh
  curl -sSL -o ebs-csi-policy.json https://raw.githubusercontent.com/kubernetes-sigs/aws-ebs-csi-driver/master/docs/example-iam-policy.json
  ```

- 다음으로 IAM policy를 생성하자.

  ```sh
  export EBS_CSI_POLICY_NAME=AmazonEBSCSIPolicy
  aws iam create-policy \
  --region $AWS_REGION \
  --policy-name $EBS_CSI_POLICY_NAME \
  --policy-document file://ebs-csi-policy.json
  export EBS_CSI_POLICY_ARN=$(aws --region ap-northeast-2 iam list-policies --query 'Policies[?PolicyName==`'$EBS_CSI_POLICY_NAME'`].Arn' --output text)
  echo $EBS_CSI_POLICY_ARN
  # arn:aws:iam::12345678:policy/AmazonEBSCSIPolicy
  ```

### Service Account를 위한 IAM Role 설정하기

- Kubernetes service account에 IAM role을 연동하게 되면 해당 service account를 사용하는 모든 pod에게 AWS의 특정 리소스에 대한 접근 권한을 부여할 수 있다.

- 우선 eksctl을 사용해 방금 생성한 IAM policy인 AmazonEBSCSIPolicy를 포함하는 IAM role을 생성하고, `ebs-csi-controller-irsa`라 불리는 Kubernetes service account에 연동해보자.

  ```sh
  eksctl create iamserviceaccount \
    --cluster $EKS_CLUSTER_NAME \
    --name ebs-csi-controller-irsa \
    --namespace kube-system \
    --attach-policy-arn $EBS_CSI_POLICY_ARN \
    --override-existing-serviceaccounts --approve
  ```

> Kubernetes Service Account에 IAM role을 연동하기 전 먼저 IAM OIDC provider를 EKS cluster에 생성해야 하나, 이전에 이미 설정했기에 아래 명령은 건너뛰어도 된다.
>
> ```sh
> eksctl utils associate-iam-oidc-provider \
>   --region=$AWS_REGION \
>   --cluster=$EKS_CLUSTER_NAME \
>   --approve
> ```

- 생성하면 아래처럼 응답이 온다.

  ![picture 35](/images/AWS_DEVOPS_EKS_PG_1.png)

---
