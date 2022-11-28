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
