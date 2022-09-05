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

## EC2 Node Group 생성하기

- Prometheus가 실행될 EC2 worker node가 사용할 IAM role을 먼저 생성하자.  
  나는 아래와 같은 policy를 가진 role을 생성했다.

  ![picture 36](/images/AWS_DEVOPS_EKS_PG_2.png)

- 이전 글에서 eksctl로 EKS cluster를 생성했으나, 파일 기반으로 기존에 존재하는 eksctl로 만들어진 EKS cluster를 변경하는 방법은 존재하지 않는다.  
  따라서 AWS Management Console에서 EKS에 접속한 후 `Cluster 선택 -> Compute -> Add node group`으로 node group 생성 페이지로 이동하자.

- `Step 1: Configure node group`은 아래와 같이 입력하고, 나머지 부분들은 기본값으로 둔다.

  ![picture 37](/images/AWS_DEVOPS_EKS_PG_3.png)

- `Step 2: Set compute and scaling configuration`에서 Instance type만 `m5.large`로 변경 후, 나머지는 기본값으로 둔다.

- `Step 3: Specify networking`은 private subnet이 있는 기본값으로 둔다.

- 마지막으로 검토 후 생성한다.

- 우선 위처럼 node group을 생성하면 해당 node group의 status는 계속 `Creating`에 머물고, 실제로 `Nodes` 탭으로 가서 node 중 하나를 선택해보면  
  아래처럼 status가 `Not ready`이며 `Taints`에 `node.kubernetes.io/not-ready:NoSchedule`, `node.kubernetes.io/unreachable:NoExecute`가 있다.

  ![picture 38](/images/AWS_DEVOPS_EKS_PG_4.png)
  ![picture 39](/images/AWS_DEVOPS_EKS_PG_5.png)

- 이를 해결하기 위해 [이 문서](https://docs.aws.amazon.com/eks/latest/userguide/cni-iam-role.html)처럼 Amazon VPC CNI plugin을 설정하자.

  - (1) eksctl로 Kubernetes service account에 IAM role 부여

    ```sh
    eksctl create iamserviceaccount \
      --name aws-node \
      --namespace kube-system \
      --cluster $EKS_CLUSTER_NAME \
      --role-name "AmazonEKSVPCCNIRole" \
      --attach-policy-arn arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy \
      --override-existing-serviceaccounts \
      --approve
    ```

  - (2) Amazon VPC CNI 재배포

    - 인증을 위한 환경 변수를 적용하기 위해 이미 존재하는 pod들을 삭제한다. 아래 명령은 `aws-node` DaemonSet pod들을 삭제하고 다시 배포한다.

      ```sh
      kubectl delete pods -n kube-system -l k8s-app=aws-node
      ```

    - 아래와 같이 응답이 오면 재배포가 잘 된 것이다.

      ```sh
      ❯ kubectl get pods -n kube-system -l k8s-app=aws-node
      NAME             READY   STATUS    RESTARTS   AGE
      aws-node-6szct   1/1     Running   0          4s
      aws-node-nz5cd   1/1     Running   0          8s
      ```

---
