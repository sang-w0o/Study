# Kubernetes Overview

## PV, PVC

- PV(Persistent Volume), PVC(Persistent Volume Claim)

- K8S에서 지원하는 대부분의 volume type들은 pod, deployment의 yaml 파일에 직접 정의해 사용할 수 있고,  
  이렇게 하면 yaml 파일에 volume의 정보를 직접 입력해야 한다.

- PV, PVC object는 pod가 volume의 세부적인 사항을 몰라도 volume을 사용할 수 있도록 추상화한다.  
  즉 pod를 생성하는 yaml 입장에서는 network volume이 NFS인지, AWS EBS인지가 상관이 없다.

- PV, PVC를 사용하는 과정은 아래와 같다.

  - (1) 인프라 관리자가 PV를 먼저 생성한다. 여기에 volume의 endpoint 등 세부 정보가 들어간다.
  - (2) 개발자가 PVC를 생성하고, pod를 정의하는 yaml에 PVC를 사용하도록 설정한다.
  - (3) K8S가 PVC와 조건이 일치하는 PV를 찾아 pod에 volume을 마운트한다.

- 아래는 EBS를 위한 PV의 예시 yaml 파일이다.

```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: ebs-pv
spec:
  capacity:
    storage: 5Gi
  accessModes:
    - ReadWriteOnce
  awsElasticBlockStore:
    fsType: ext4
    volumeId: ${volumeId}
```

- `spec.accessModes`에는 아래와 같은 값들이 들어갈 수 있다.

  - `ReadWriteOnce`: 1:1 mount만 가능, read write 가능
  - `ReadOnlyMany`: 1:N mount 가능, read 전용
  - `ReadWriteMany`: 1:N mount 가능, read write 가능

- 그리고 이를 사용하기 위한 PVC는 아래와 같다.

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: ebs-pvc
spec:
  storageClassName: ""
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 5Gi
```

- 이제 이 PVC를 사용하는 pod의 yaml은 아래와 같다.

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: ebs-pod
spec:
  containers:
    - name: ebs-mount-container
      image: busybox
      args: ["tail", "-f", "/dev/null"]
      volumeMounts:
	- name: ebs-mount
	  mountPath: /mnt
  volumes:
    - name: ebs-volume
      persistentVolumeClaim:
        claimName: ebs-pvc
```

- 위처럼 pod에 PVC 이름을 지정하면 K8S가 PVC와 accessMode, storage size가 일치하는 PV를 찾아서  
  pod에 volume을 마운트한다. 만약 특정한 PV를 사용하고 싶다면 storageClassName을 사용할 수도 있고, label selector를  
  사용할 수도 있다.

- PV는 namespace에 종속되지 않고 PVC는 namespace에 종속된다.

---

## ServiceAccount, RBAC

- kubectl로 수행하는 모든 명령어는 kube-apiserver를 통해 수행된다. 수행 과정은 아래와 같다.

  - (1) 사용자가 kubectl로 명령어 수행
  - (2) kube-apiserver의 HTTP handler가 요청 수신
  - (3) Authentication, Authorization 수행
  - (4) Admission Controller 기능 수행 후 작업 수행

### ServiceAccount, Role, ClusterRole

- ServiceAccount: 체계적으로 권한을 관리하기 위한 K8S object, 한 명의 사용자나 애플리케이션에 해당한다. 그리고 namespace에 종속적이다.

- Role, ClusterRole은 부여할 권한을 나타내는 K8S object이다.  
  단지 ClusterRole은 cluster 단위의 권한을 정의할 때 사용한다는 차이점만을 가진다.

- 아래는 Role object의 예시이다.

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  namespace: default
  name: service-reader
rules:
  - apiGroups: [""]
    resources: ["services"]
    verbs: ["get", "watch", "list"]
```

- 이렇게 생성한 Role, ClusterRole은 RoleBinding, ClusterRoleBinding object로 ServiceAccount에 연결한다.

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: service-reader-rolebinding
  namespace: default
subjects:
  - kind: ServiceAccount # 권한 부여 대상이 ServiceAccount
    name: some-service-account
    namespace: default
roleRef:
  kind: Role # Role에 정의된 권한 부여
  name: service-reader
  apiGroup: rbac.authorization.k8s.io
```

- ClusterRole은 다른 ClusterRole의 권한을 포함해 사용할 수 있다. 이를 Role aggregation이라 한다.

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: parent-clusterrole
  labels:
    rbac.authorization.k8s.io/aggregate-to-child-clusterrole: "true"
rules:
  - apiGroups: [""]
    resources: ["nodes"]
    verbs: ["get", "list"]

---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: child-clusterrole
aggregationRule:
  clusterRoleSelectors:
    - matchLables:
      rbac.authorization.k8s.io/aggregate-to-child-clusterrole: "true"
rules: [] # 어떠한 권한도 정의하지 않는다.
```

- 위처럼 했을 때 `child-clusterrole`은 아무런 권한이 없도록 선언되어 있지만 `aggregationRule.clusterRoleSelectors.matchLabels`  
  에 의해 `parent-clusterrole`의 권한을 포함하게 된다.

### User, Group

- User는 실제 사용자를 뜻하며 Group은 여러 User들을 모아 놓은 집합을 의미한다.  
  따라서 RoleBinding이나 ClusterRoleBinding의 `subjects.kind`에 User, Group을 지정할 수 있다.

---
