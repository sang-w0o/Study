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

## Pod의 자원 사용량 제한

### limits

- limits는 pod의 자원 사용량을 명시적으로 설정한다. Pod의 CPU, memory 사용량을 제한하기 위함이다.

```yaml
apiVersion: v1
kind: Pod
metadata:
#..
spec:
  containers:
    - name: nginx
      image: nginx:latest
  resources:
    limits:
      cpu: "1000m"
      memory: "256Mi"
```

### requests

- Requests의 기능을 보기 전, 자원의 overcommit에 대해 먼저 살펴보자.  
  1GB의 메모리를 가진 node에 2개의 container(A, B)를 생성했으며 각각에 500MB의 메모리를 할당했다고 가정해보자.  
  이렇게 하면 실제로 메모리 샤옹량이 500MB보다 적은 container가 생겨도 놀고 있는 메모리가 생기기에 비효율적이다.  
  그리고 실제로 container가 자원을 얼마나 사용할지 예측하기 어려운 경우가 빈번히 존재한다.

- K8S는 overcommit을 통해 실제 물리 자원보다 더 많은 양의 자원을 할당하도록 해준다.  
  예를 들어 메모리 용량이 1GB인 서버에 메모리 제한이 750MB인 pod를 2개 생성할 수 있는 것이다.  
  물론 물리적으로 1GB라는 제한이 있기 때문에 이를 깨고 1.5GB의 메모리를 사용하도록 하는 것은 아니다.  
  대신, 만약 container A의 메모리 샤용량이 750MB보다 낮다면, container B가 A로부터 남는 메모리 자원을 사용하도록  
  해주는 것이다.

```yaml
apiVersion: v1
kind: Pod
metadata:
#..
spec:
  containers:
    - name: nginx
      image: nginx:latest
  resources:
    limits:
      memory: "256Mi"
      cpu: "1000m"
    requests:
      memory: "128Mi"
      cpu: "500m"
```

- 위 yaml 파일은 **"최소한 128Mi의 메모리 사용은 보장하지만, 유휴 메모리 자원이 있다면 최대 256Mi까지 사용할 수 있다."** 는 뜻이다.

### QoS class와 메모리 자원 사용량 제한 원리

- K8S는 가용 메모리를 확보하기 위해 우선순위가 낮은 pod 또는 프로세스를 강제로 종료할 수 있다.  
  이렇게 강제로 종료된 pod는 다른 node로 옮겨지는데, 이를 eviction이라 한다.

- 이 _"우선순위"_ 는 QoS(Quality of Service) class를 사용해 결정한다.

> OOM(Out Of Memory): K8S의 node는 MemoryPressure, DiskPressure 등의 상태 정보(condition)이 있으며, 이들은  
> K8S agent인 kubelet에 의해 주기적으로 검사된다. 만약 메모리가 부족하면 MemoryPressure가 True로 변경되는 식이다.  
> 이렇게 MemoryPressure가 발생하면 K8S는 가장 우선순위가 낮은 pod를 다른 node로 evict 시킨다.(eviction)  
> 뿐만 아니라 MemoryPressure가 True인 node에는 더이상 pod를 할당시키지 않는다. 여기서 사용되는 우선순위가 QoS Class 및  
> 메모리 사용량에 따라 정렬된다.
>
> OOM Killer는 linux에 기본적으로 내장된 기능으로, 우선순위 점수에 oom_score]\_adj 와 oom_score를 사용한다.  
> OOM Killer는 oom_score의 값에 따라 종료할 프로세스를 선정한다. 모든 프로세스는 자동으로 OOM 점수가 매겨지며, 이 점수가  
> 높으면 높을 수록 강제로 종료될 가능성이 커진다. 예를 들어 K8S를 설치할 때 실행되는 docker daemon은 OOM 점수가 기본적으로 -999이다.

- QoS class - `Guaranteed`

  - limits, requests가 정확히 동일한 pod(limits만 명시하면 requests는 limits와 동일하게 설정된다.)
  - Guaranteed class의 pod 내부에서 실행되는 프로세스들은 모두 oom_score_adj가 -998이다.  
    즉, 거의 강제로 종료되는 일이 없다.

- QoS class - `BestEffort`

  - `spec.resources`를 명시하지 않은 pod
  - 보장받을 수 있는 자원이 존재하지 않기에 언제든지 강제로 종료될 수 있다.

- QoS class - `Burstable`

  - requests, limits가 설정되어 있지만 limits가 requests보다 큰 pod
  - 필요에 따라 순간적으로 자원의 한계를 확장해 사용할 수 있다.

- 정확히 말하자면 **pod가 사용하는 메모리량이 큰수록 우선순위가 낮아진다.**  
  하지만 일반적으로 node에 메모리가 부족하면 가장 먼저 `BestEffort`가 종료되고, 그 다음에 `Burstable` pod가 종료된다.  
  `Guaranteed`는 낮은 우선순위의 pod가 존재하지 않을 때 마지막으로 종료된다.

### ResourceQuota

- ResourceQuota의 기능은 아래와 같다.

  - Namespace에서 할당할 수 있는 자원(CPU, memory, PVC의 크기, container ephemeral storage size) 등의 총합 제한
  - Namespace에서 생성할 수 있는 리소스(service, deployment 등)의 개수 제한

```yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: resource-quota-example
  namespace: default
spec:
  hard:
    requests.cpu: "1000m"
    requests.memory: "500Mi"
    limits.cpu: "1500m"
    limits.memory: "1000Mi"
```

- requests, limits를 함께 제한할 필요는 없으며 CPU, memory 를 하나의 ResourceQuota에서 제한할 필요도 없다.  
  그리고 단일 pod의 자원 할당향을 제한하는 것이 아닌, namespace에서 사용할 수 있는 자원 할당량의 합에 대한 제한이라는 것에 유의하자.

- ResourceQuota는 아래의 K8S object의 개수를 제할할 수 있다.

  - Deployment, service, pod, secret, configmap, pvc 등의 개수
  - NodePort 또는 LoadBalancer 타입의 service 개수
  - QoS class가 `BestEffort`인 pod 개수

```yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: resource-quota-example
  namespace: default
spec:
  hard:
    requests.cpu: "1000m"
    requests.memory: "500Mi"
    limits.cpu: "1500m"
    limits.memory: "1000Mi"
    count/pods: 3
    count/services: 5
```

- 만약 ResourceQuota에 `limits.cpu`나 `limits.memory` 등을 사용해 namespace에 사용 가능한 자원의 총합을  
  설정했다면, pod 생성시 반드시 해당 항목을 함께 정의해야 한다. 그렇지 않으면 에러가 발생한다.

### LimitRange

- LimitRange의 용도는 아래와 같다.

  - Pod의 container에 CPU, memory 할당량이 설정되어 있지 않은 경우, container에 자동으로 기본 requests 또는 limits를 설정할 수 있다.
  - Pod 또는 container의 CPU, memory, PVC storage size의 최소, 최대값을 설정할 수 있다.

- LimitRange는 ResourceQuota와 마찬가지로 namespace에 종속되는 object이다.

```yaml
apiVersion: v1
kind: LimitRange
metadata:
  name: mem-limit-range
spec:
  limits:
    - default: # 자동으로 설정될 기본 limits 값
        memory: 256Mi
        cpu: 200m
      defaultRequest: # 자동으로 설정될 기본 requests 값
        memory: 128Mi
	cpu: 100m
      max: # 자원 할당량의 최대값
        memory: 1Gi
	cpu: 1000m
      min: # 자원 할당량의 최소값
        memory: 16Mi
	cpu: 50m
      type: Container
```

- LimitRange를 생성하면 해당 namespace에서 min, max의 범위를 벗어나는 pod의 container는 생성할 수 없다.

- LimitRange에서 maxLimitRequestRatio를 사용해 pod의 container에서 overcommit되는 자원의 비율을 제한할 수 있다.

---
