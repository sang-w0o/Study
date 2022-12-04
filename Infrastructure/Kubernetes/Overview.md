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

## K8S Scheduling

- Scheduling: 컨테이너나 VM 같은 인스턴스를 새롭게 생성할 때, 해당 인스턴스를 어느 서버에 생성할지 결정하는 일

### Pod가 실제로 node에 생성되기까지의 과정

- (1) ServiceAccount, RoldBinding 등을 이용해 pod 생성을 요청한 사용자의 인증 및 인가 작업 수행
- (2) ResourceQuota, LimitRange 등의 admission controller가 해당 pod 생성 요청을 적절히 mutate(변경)하거나 validate(검증)
- (3) Admission controller의 검증을 통과해 최종적으로 pod 생성 요청이 승인되면 worker node들 중 하나에 생성

- 여기서는 위의 3단계 중 (3)번 단계를 좀 더 자세히 살펴보도록 하자.

- K8S에는 여러 가지 핵심 컴포넌트들이 실행되고 있으며, 이들은 kube-system namespace에서 실행되고 있다.  
  예를 들어 kubectl 명령을 처리하는 kube-apiserver 등이 있다. 추가적으로 scheduling에 관여하는 kube-scheduler, etcd도 있다.  
  kube-scheduler는 K8S scheduler에 해당하며 etcd는 K8S cluster의 전반적인 상태 데이터를 저장하는 일종의 데이터베이스 이다.

- etcd는 distributed coordinator로 클라우드 플랫폼 등의 환경에서 여러 컴포넌트가 정상적으로 상호 작용할 수 있도록 데이터를 조정한다.  
  예를 들어 현재 생성된 deployment나 pod의 목록과 정보, cluster 자체의 정보 등의 대부분의 데이터가 etcd에 저장되어 있다.

- etcd에 저장된 데이터는 무조건 kube-apiserver로만 접근할 수 있다. 그리고 etcd에 저장된 pod의 데이터에는 해당 pod가 어떤 worker  
  node에서 실행되고 있는지를 나타내는 nodeName 항목이 존재한다.

- 처음 인증, 인가, admission controller 등의 단계를 모두 거친 pod 생성 요청은 kube-apiserver에 의해 etcd에 pod의 데이터를  
  기록한다. 이때 nodeName은 설정되어 있지 않다.(scheduling 전이기 때문)

- kube-scheduler는 이후 kube-apiserver의 watch를 통해 nodeName이 설정되지 않은 pod 데이터가 저장되었다는 사실을 전달받고,  
  해당 pod들을 scheduling 대상으로 판단한다. 이러한 pod들을 할당할 적절한 node를 선택한 다음, kube-apiserver에게 해당 node와  
  pod를 binding할 것을 요청한다. 그 후에 nodeName에 해당 node의 이름이 기록된다.

### Pod가 생성될 node를 선택하는 scheduling 과정

- 가장 중요한 것은 **"scheduler가 적절한 node를 어떻게 설정하느냐"** 이다.  
  Scheduler는 크게 node filtering, node scoring의 단계를 거쳐 최종적으로 node를 선택한다.

  - Node filtering: Pod를 할당할 수 있는 node와 그렇지 않은 node를 filtering하는 단계이다. 예를 들어 CPU, memory가 요청하는  
    것보다 적은 node라면 해당 node는 pod를 할당할 수 없다. 이러한 filtering을 통해 적절한 node를 선택한다.

  - Node scoring: K8S의 소스 코드에 미리 정의된 알고리즘의 가중치에 따라 node의 점수를 계산한다. 예를 들어, pod가 실행할 image가  
    이미 node에 존재할 때 더욱 빠르게 pod를 생성할 수 있으므로 더 높은 점수가 부여된다. 이러한 알고리즘들의 값을 합산함으로써  
    후보 node들의 점수를 모두 계산한 다음, 가장 점수가 높은 node를 최종적으로 선택한다.

### NodeSelector, Node Affinity, Pod Affinity

- 특정 worker node에 pod를 할당하는 가장 확실한 방법은 pod의 yaml 파일에 nodeName을 직접 명시하는 것이다.

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: nginx
spec:
  nodeName: node01
  containers:
    - name: nginx
      image: nginx:latest
```

- 이렇게 nodeName을 사용하면 pod가 실행될 node를 확실히 지정할 수 있지만, node의 이름을 고정으로 설정했기에 다른 환경에서 yaml 파일을  
  보편적으로 사용하기 어렵다는 문제가 있고, 장애 발생시에도 유연하게 대처할 수 없다.

- nodeName 대신 사용 가능한 선택지로는 node의 label을 사용하는 것이다. 즉, 특정 label이 적용되어 있는 node에만 pod가 생성되도록 하는 것이다.

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: nginx-nodeselector
spec:
  nodeSelector:
    mylabel/disk: hdd
  containers:
    - name: nginx
      image: nginx:latest
```

- nodeSelector도 나쁘지 않은 방법이지만, 단순히 label의 key-value pair가 같은지만 비교해 node를 선택하기에 활용 방법이 다양하진 않다.  
  이를 보완하기 위해 K8S는 Node affinity라는 scheduling 방법을 제공한다.

- Node Affinity는 nodeSelector에서 조금 더 확장된 label 선택 기능을 제공하며, 반드시 충족해야 하는 조건(Hard)과  
  선호하는 조건(Soft)을 별도로 정의할 수 있다.

- Node Affinity에는 2가지의 옵션이 있다.

  - `requiredDuringSchedulingIgnoredDuringExecution`
  - `preferredDuringSchedulingIgnoredDuringExecution`

- 먼저 `requiredDuringSchedulingIgnoredDuringExecution`을 보자.

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: nginx-nodeaffinity-required
spec:
  affinity:
    nodeAffinity:
      requiredDuringSchedulingIgnoredDuringExecution:
	nodeSelectorTerms:
	  - matchExpressions:
	      - key: mylabel/disk
		operator: In
		values:
		  - ssd
		  - hdd
  containers:
    - name: nginx
      image: nginx:latest
```

- 위 yaml 파일의 `operator: In`은 key, value pair 중 하나라도 만족하는 node에 pod를 scheduling하도록 하는 것이다.  
  이러한 operator에는 `In`, `NotIn`, `Exists`, `DoesNotExist`, `Gt`, `Lt`가 있다.

- `requiredDuringSchedulingIgnoredDuringExecution`는 반드시 만족시켜야 하는 조건을 명시할 때 사용하는 반면,  
  `preferredDuringSchedulingIgnoredDuringExecution`는 "선호하는 제약 조건"을 의미한다.  
  "선호" 하기 때문에 선호도를 아래처럼 지정할 수 있다.

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: nginx-nodeaffinity-preferred
spec:
  affinity:
    nodeAffinity:
      preferredDuringSchedulingIgnoredDuringExecution:
        - weight: 80 # 조건을 만족하는 node에 1~100의 가중치 부여
	  preference:
	    matchExpressions:
	      - key: mylabel/disk
	        operator: In
		values:
		  - ssd
  containers:
    - name: nginx
      image: nginx:latest
```

- 즉 `preferredDuringSchedulingIgnoredDuringExecution`를 사용하면 조건에 맞는 node가 가중치가 부여돼 선택될 확률이  
  높아지기에 이를 Soft 하다고 하는 것이다.

- Node Affinity가 특정 조건을 만족하는 node를 선택하는 방법이라면, Pod Affinity는 특정 조건을 만족하는 pod와 함께 실행되도록  
  scheduling해준다. Node Affinity와 동일한 option들을 사용할 수 있다.

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: nginx-podaffinity
spec:
  affinity:
    podAffinity:
      requiredDuringSchedulingIgnoredDuringExecution:
	- labelSelector:
	    matchExpressions:
	      - key: mylabel/database
	        operator: In
		values:
		  - mysql
	  topologyKey: failure-domain.beta.kubernetes.io/zone
  containers:
    - name: nginx
      image: nginx:latest
```

- 이번에는 `spec.affinity`에 nodeAffinity 대신 podAffinity를 사용했고, topologyKey라는 항목이 추가되었다.  
  위 yaml 파일은 `requiredDuringSchedulingIgnoredDuringException`을 통해 이전과 비슷한 의미로 사용되고 있지만,  
  이 label을 갖는 pod와 무조건 같은 node에 할당하라는 뜻은 아니다. topologyKey는 해당 label을 갖는 topology 범위의  
  node를 선택하라는 것을 의미한다.

- 예를 들어 K8S node들이 topologyKey에 설정된 label의 key-value pair에 따라 여러 개의 group(topology)로  
  분류된다고 해보자. 그리고 kubernetes.io/zone이라는 key의 value가 ap-northeast-2a, ap-northeast-2b인  
  group이 있는 상황이라고 해보자. 이때 matchExpression의 label 조건을 만족하는 pod가 위치한 group의 node들 중 하나에  
  pod를 할당한다. 따라서 조건을 만족하는 pod와 동일한 node에 할당될 수도 있지만, 해당 node와 동일한 group(topology)에  
  속하는 다른 node에 pod가 할당될 수도 있다.

- PodAffinity와 반대되는 기능으로 Pod Anti-Affinity가 있다. Pod Affinity가 특정 pod와 동일한 topology에 속하는  
  node를 선택했다면, Pod Anti-Affinity는 특정 pod와 같은 topology에 속하는 node를 선택하지 않는다.  
  사용법은 아주 간단한데, 단지 podAffinity를 podAntiAffinity로 바꿔주기만 하면 된다.

### Taints, Tolerations 사용하기

- Taint: taint(얼룩)을 특정 node에 지정함으로써 해당 node에 pod가 할당되는 것을 막는다.
- Toleration: taint에 대응하는 toleration을 지정해 taint가 설정된 node에도 pod가 할당되도록 한다.

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: nginx-toleration-test
spec:
  tolerations:
    - key: some/taint
      value: dirty
      operator: Equal # some/taint가 dirty인 taint가 있다면
      effect: NoSchedule # 해당 node에 pod를 할당하지 않는다.
  containers:
    - image: nginx:latest
      name: nginx
```

- Taint의 effect에는 NoSchedule외에 PreferNoSchedule과 NoExecute가 있다.  
  NoSchedule은 node에 설정되더라도 기존에 실행 중이던 pod는 정상적으로 동작하는 반면, NoExecute는 해당 node에서 실행 중인 pod를 종료시킨다.

### Cordon, Drain, PodDistributionBudget

- Taint, toleration을 사용해 node에 pod가 scheduling되는 것을 막을 수도 있지만, K8S는 이보다 더 명시적인 방법인 cordon을  
  제공한다. `kubectl cordon $NODE_NAME` 명령어를 사용하면 된다. Cordon을 해제하려면 `kubectl uncordon $NODE_NAME` 명령어를 사용하면 된다.

- Drain은 cordon과 마찬가지로 특정 node에 scheduling을 금지한다는 점은 같지만, node에서 기존에 실행 중이던 pod를 다른 node로  
  옮겨가도록 evict한다는 점이 다르다. Drain을 사용하면 해당 node에는 pod가 실행되지 않기에 kernel version upgrade, 유지보수 등의  
  이유로 잠시 node를 중지해야 할 때 유용하게 사용할 수 있다. `kubectl drain $NODE_NAME`

- Drain으로 인해 pod가 evict될 때 애플리케이션이 중단될 수도 있다. 이를 방지하기 위해 PodDistributionBudget을 사용할 수 있다.  
  PodDistributionBudget은 `kubectl drain`으로 pod에 eviction이 발생할 때 특정 개수 또는 비율 만큼의 pod는 반드시  
  정상적인 상태를 유지하기 위해 사용한다.

```yaml
apiVersion: policy/v1beta1
kind: PodDistributionBudget
metadata:
  name: simple-pdb-example
spec:
  maxUnavailable: 1 # 비활성화될 수 있는 pod의 최대 개수 또는 비율
  selector:
    matchLabels:
      app: webserver # PDB의 대상이 될 pod를 선택하는 label selector
```

---

## K8S 애플리케이션 상태와 배포

### Deployment를 통한 rolling update

- 일반적으로는 deployment를 생성하고, deployment에 속하는 replicaSet이 pod를 생성하도록 한다.  
  Pod를 생성할 때 deployment를 사용하는 이유는 replicaSet의 변경 사항을 저장하는 revision을 deployment에서  
  관리함으로써 애플리케이션의 배포를 쉽게 하기 위함이다.

- Deployment에 변경 사항이 생기면 새로운 replicaSet이 생성되고, 그에 따라 새로운 버전의 애플리케이션이 배포된다.  
  이때 --record option을 적용해 새로운 deployment를 배포하면 기존 replicaSet의 정보가 deployment의 history에  
  기록된다. 그리고 revision을 이용해 언제든지 원하는 버전의 replicaSet으로 rollback할 수 있다.

  - `kubectl apply -f my-deployment.yaml --record`

- Deployment를 통해 새로운 버전의 pod를 생성하는 작업 그 자체는 매우 단순하지만, 배포 중에 애플리케이션이 일시적으로 중단되어도  
  괜찮은지에 따라 어떠한 배포 방법을 선택할 것인지 고민해야 한다.

- 일시적으로 사용자의 요청을 처리하지 못해도 괜찮은 애플리케이션이라면 K8S가 제공하는 ReCreate 방법을 사용하면 된다.  
  이 방법은 기존 버전의 pod를 모두 삭제한 후, 새로운 버전의 pod를 생성한다.

- 이러한 배포 전략은 deployment yaml 파일의 `spec.strategy`에서 선택할 수 있다.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: deployment-recreate
spec:
  replicas: 3
  strategy:
    type: ReCreate
```

- 하지만 만약 사용자의 요청을 배포 도중에도 언제든지 처리하려면 어떻게 해야 할까? 바로 K8S가 제공하는 Rolling update를 사용하면 된다.  
  Rolling update를 사용하면 deployment를 업데이트하는 도중에도 사용자의 요청을 처리할 수 있는 pod가 계속 존재하기 때문에  
  애플리케이션의 중단이 발생하지 않는다.

- Deployment의 기본 배포 방식은 rolling update이며, 기존 pod는 몇 개씩 삭제할 것인지, 새로운 버전의 pod는 몇 개씩 생성할 것인지를  
  아래와 같이 직접 설정할 수 있다.

```yaml
#..
spec:
  replicas: 3
  stragegy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 2 # rolling update 도중 전체 pod 개수가 spec.replicas보다 얼마나 더 많이 존재할 수 있는지. %도 사용 가능.
      maxUnavailable: 2 # rolling update 도중 사용 불가능한 상태가 되는 pod의 최대 개수 설정. %도 사용 가능
```

### Pod의 lifecycle

- Pod의 lifecycle은 아래와 같은 state들로 이뤄진다.

  - `Pending`: Pod 생성 요청은 승인되었지만, 어떠한 이유로 인해 실제로 생성되지는 않은 상태. ex) scheduling 되기 전
  - `Running`: Pod에 포함된 컨테이너들이 모두 생성되어 pod가 정상적으로 실행된 상태
  - `Completed`: Pod가 정상적으로 실행되어 종료된 상태.(init process의 exit code가 0)
  - `Error`: Pod가 정상적으로 실행되지 않은 상태로 종료된 상태.(init process의 exit code가 0이 아님)
  - `Terminating`: Pod가 삭제 또는 evict되기 위해 삭제 상태에 머물러 있는 경우

- Pod가 종료된 후에 재시작 여부를 결정하는 restartPolicy는 기본적으로 항상 재실행 시키는 Always로 설정되어 있고,  
  추가적으로 절대로 재시작하지 않는 Never, 0이 아닌 exit code를 반환했을 때만 재시작시키는 OnFailure가 있다.

- Pod는 재시작하는 기간을 exponential backoff로 두는데, CrashLoopBackoff는 종료 후 재시도하기 전의 상태를 의미한다.

- Pod를 생성하고, Running 상태가 되었다고 해서 컨테이너 내부의 애플리케이션이 제대로 동작할 것이라는 보장은 없다.  
  이를 위해 K8S에서는 init container, postStart, livenessProve, readinessProbe라는 기능을 제공한다.

  - init container: Pod의 컨테이너 내부에서 애플리케이션이 실행되기 전, 초기화를 수행하는 컨테이너

    - 만약 init container 중 하나라도 실행에 실패하면 pod의 애플리케이션 컨테이너는 실행되지 않으며 restartPolicy에 의해 재시작된다.

  ```yaml
  apiVersion: v1
  kind: Pod
  metadata:
    name: init-container-example
  spec:
    initContainers: # 초기화 컨테이너 지정
      - name: my-init-container
        image: busybox
        command: ["sh", "-c", "echo Hello from the init container"]
    containers:
      - name: nginx
        image: nginx
  ```

  - postStart: Pod의 컨테이너가 실행되거나 삭제될 때 특정 작업을 수행하도록 하는 lifecycle hook이다.  
    이러한 hook에는 컨테이너가 시작될 때 실행되는 postStart와 종료될 때 실행되는 preStop이 있다.

    - postStart는 컨테이너가 시작된 직후 특정 주소로 HTTP 요청을 보내는 HTTP, 컨테이너 시작 직후 컨테이너 내부에 특정  
      명령어를 실행시키는 exec이 있다.

  ```yaml
  apiVersion: v1
  kind: Pod
  metadata:
    name: poststart-hook
  spec:
    containers:
      - name: poststart-hook
        image: nginx
        lifecycle:
          postStart:
            exec:
              command: ["sh", "-c", "touch /myFile"]
  ```

  - postStart의 exec 명령이나 HTTP request가 제대로 실행되지 않으면 컨테이너는 running 상태로 전환되지 않으며,  
    init container와 마찬가지로 restartPolicy에 의해 재시작한다.

  - livenessProbe: 컨테이너 내부의 애플리케이션이 살아있는지(liveness) 검사한다. 검사에 실패할 경우, 해당 컨테이너는  
    restartPolicy에 의해 재시작된다.

  - readinessProbe: 컨테이너 내부의 애플리케이션이 사용자 요청을 처리할 준비가 되었는지(readiness) 검사한다.  
    검사에 실패할 경우, 해당 컨테이너는 **service의 routing 대상에서 제외한다.**

  - livenessProbe에 실패했다는 것은 애플리케이션 내부에 무언가 문제가 생겼다는 것이기에 정상 상태로 되돌리기 위해 컨테이너를  
    재시작하지만, readinessProbe에 실패한 것은 애플리케이션이 실행 직후 초기화 등의 작업으로 아직 준비가 되지 않았다는  
    뜻이기에 사용자의 요청이 전달되지 않도록 service의 routing 대상에서 pod의 IP를 제거한다.

  ```yaml
  apiVersion: v1
  kind: Pod
  metadata:
    name: livenessprobe-pod
  spec:
    containers:
      - name: livenessprobe-pod
        image: nginx
        livenessProbe: # 이 컨테이너에 대해 livenessProbe 설정
          httpGet: # HTTP GET 요청을 통해 애플리케이션의 상태 검사
            port: 80 # $POD_IP:80으로 요청
            path: /
  ```

  - livenessProbe, readinessProbe는 httpGet외에 exec, tcpSocket을 사용할 수 있다.  
    tcpSocket은 TCP connection이 맺어질 수 있는지를 체크함으로써 상태를 검사한다.

  - readinessProbe를 사용하는 예시는 아래와 같다.

  ```yaml
  apiVersion: v1
  kind: Pod
  metadata:
    name: readinessprobe-pod
  spec:
    containers:
      - name: readinessprobe-pod
        image: nginx
        readinessProbe:
          httpGet:
            port: 80
            path: /
  ```

  - 만약 컨테이너가 너무 빨리 시작되어 readinessProbe를 적용하기 어렵거나, 초기화 시간이 어느정도 필요한 경우에는  
    `spec.minReadySeconds`를 사용하면 된다. 이 옵션에 지정된 시간 동안 readinessProbe는 검사를 대기하게 된다.

  - 마지막으로 readinessProbe, livenessProbe는 아래의 옵션들을 제공한다.

    - periodSeconds: 검사 진행 주기
    - initDelaySeconds: pod 생성 후 검사를 시작할 때 까지의 대기 시간
    - timeoutSeconds: 요청에 대한 timeout
    - successThreshold: 상태 검사를 성공으로 판단하는 검사 성공 횟수
    - failureThreshold: 상태 검사를 실패로 판단하는 검사 실패 횟수

  ```yaml
  apiVersion: v1
  #...
  readinessProbe:
    httpGet:
      port: 80
      path: /
    periodSeconds: 10 # 10초마다 검사
    initDelaySeconds: 5 # pod 생성 후 5초 후 검사 시작
    timeoutSeconds: 1 # 1초 이상 응답이 없으면 실패로 판단
    successThreshold: 1 # 1번의 검사 성공으로 성공으로 판단
    failureThreshold: 3 # 3번의 검사 실패로 실패로 판단
  ```

- 마지막으로 pod를 종료할 때 벌어지는 상황을 보자.

  - (1) 리소스가 삭제될 예정이라는 의미인 deletionTimestamp값이 pod의 데이터에 추가되고, pod가 `Terminating` 상태로 바뀐다.
  - (2) 아래 내용들이 동시에 수행된다.
    - Pod에 preStop lifecycle hook이 있다면 실행한다.
    - Pod가 replicaSet으로부터 생성된 경우, replicaSet의 관리 영역에서 벗어나며 replicaSet은 새로운 pod를 생성하려 시도한다.
    - Pod가 service의 routing 대상에서 제외된다.
  - (3) preStop hook이 완료되면 linux의 SIGTERM 시그널이 pod의 컨테이너에 전달된다.
  - (4) 특정 유예 기간이 지나도 컨테이너 내부의 프로세스가 여전히 종료되지 않으면 SIGKILL 시그널이 전달된다.  
    이 유예 기간은 yaml의 `spec.terminationGracePeriodSeconds`에 지정할 수 있고, 기본값은 30초이다.

- 개발자는 애플리케이션을 gracefully shutdown하기 위해 SIGTERM을 수신했을 때 어떠한 행동을 취할지를 정의할 수 있다.

### HPA를 활용한 auto scaling

- HPA(Horizontal Pod Autoscaler)는 리소스 사용량에 따라 deployment의 pod 개수를 자동으로 조절하는 K8S가 제공하는 기능이다.  
  비록 기본 기능이지만, 리소스 metric 수집 도구인 metrics-server를 설치해야지만 autoscaling을 사용할 수 있다.  
  왜냐하면 CPU, memory 사용량 등을 수집해야 하기 때문이다.

```yaml
apiVersion: autoscaling/v1
kind: HorizontalPodAutoscaler
metadata:
  name: simple-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: hostname-deployment # hostname-deployment의 자원 사용량 중
  targetCPUUtilizationPercentage: 50 # CPU 활용률이 50% 이상인 경우
  maxReplicas: 5 # pod 개수를 최대 5개까지 늘린다.
  minReplicas: 1 # pod 개수를 최소 1개까지 줄인다.
```

- targetCPUUtilizationPercentage는 pod의 절대적인 리소스 사용량이 아닌, pod에 requests로 할당된 리소스 대비 사용률을 의미한다.

---
