## Anatomy of a Helm chart

- 일반적인 Helm chart의 구성은 아래와 같다.

```
chart/mychart/
├── Chart.yaml
├── README.md
├── templates/
│   ├── NOTES.txt
│   ├── deployment.yaml
│   ├── secrets.yaml
│   └── service.yaml
└── values.yaml
```

- Helm chart는 하나의 파일이 아니라 Helm이 애플리케이션을 설치할 때 필요한 요구사항에 맞춰 작성된 여러 개의  
  파일 및 디렉토리로 이뤄진다. 이러한 Helm chart는 일반적으로 repository에 저장되며, 배포를 위해  
  localhost에 저장된다.

- 위 tree 구조는 기본적인 helm chart의 구성도를 나타낸다.

  - Chart.yaml: Helm chart에 대한 정보를 담는 yaml 파일.  
    Chart가 사용하는 API version, type, 그리고 version 정보들을 포함한다.

  - values.yaml: Helm chart를 위한 기본 설정값들을 담는다.  
    `templates/` 하위의 yaml 파일들은 이 values.yaml에 포함된 설정값들을 읽어올 수 있기에  
    매번 상황에 따라 `templates/` 하위의 yaml 파일들을 수정할 필요가 없다.

  - charts/ : 해당 Helm chart가 의존하는 다른 Helm chart들을 포함한다.
  - crds/: Custom Resource Definitions(CRDs)를 포함한다.
  - templates/: values.yaml에서 설정값들을 읽어와 유효한 Kubernetes manitest file들을 생성한다.

- 아래의 2개 yaml 파일들을 보자.

```yaml
# templates/pod.yaml
#..
metadata:
  name: {{ .Release.Name }}
  namespace: {{ .Values.ns }}
spec:
  containers:
    - name: {{ .Values.name}}
      image: {{ .Values.image }}:{{ .Values.tag }}
      ports:
        - containerPort: {{ .Values.port }}

---

# values.yaml
# namespace value
ns: dev

# container value
name: my-app
tag: 0.2

# port values
port: 8080
```

- 위에서 `{{ .Release.Name }}`과 `{{ .Values.name }}`에서 짐작할 수 있듯이, Helm은 yaml manifest 파일들을  
  template화 할 수 있는 방법을 제공하며, 이는 DRY 원칙에도 부합한다.

- 기본적으로 Helm은 template 변수들을 values.yaml 파일에서 부합하는 값으로 대체하게 된다.  
  하지만 원한다면 command line parameter로 원하는 값들을 전달할 수도 있다.

---

## Accessing Helm charts

- Helm chart package들은 chart repository에 보관된다. 그리고 Helm client를 다양한 chart repository들로부터  
  애플리케이션을 검색하고, 배포하도록 할 수 있다. 그리고 Helm client는 Helm chart repository에 chart를 push할 수도 있다.

- Artifact Hub: 개발자들이 자신이 배포한 Helm chart를 다른 사람들이 사용할 수 있도록 해주는 public Helm chart  
  repository의 중앙 저장소이다. Artifact Hub 자체는 chart package들을 저장하지 않으며, 대신 해당 chart를  
  저장하고 있는 Helm chart repository의 접근 방법을 제공한다.

- 때에 따라서 public이 아닌 private Helm repository를 구축해 사용할 수도 있다.  
  예를 들어, Helm chart package들을 Amazon ECR의 private repository에 저장하도록 할 수 있다.

- Helm chart를 hosting하기 위한 선택지들 중 하나로 Amazon S3를 사용할 수도 있다.

---

## 실습: Helm, Amazon S3로 애플리케이션 배포하기

- 실습으로 nginx 컨테이너를 배포하는 Kubernetes deployment, service object를 가진 Helm chart를 만들고, 배포해보자.

> 사전 요구사항: kubectl, AWS CLI v2, minikube가 설치되어 있다고 가정한다.
> Amazon S3를 사용할 것이므로 S3에 접근할 수 있는 IAM user가 필요하다.
> 그리고 local 환경에서 쉽게 테스트할 수 있도록 [minikube](https://github.com/kubernetes/minikube)를 사용한다.

### 1. Helm 설치 및 Helm repository로 사용할 S3 bucket 생성

#### 1.1 Helm 설치

- Mac OS 기준으로 `brew install helm` 을 입력해 Helm을 설치한다.

  ```sh
  brew install helm
  ```

- 설치가 완료되었다면, 아래 명령어로 version을 확인해보자.

  ```sh
  helm version --short
  ```

#### 1.2 Helm-s3 plugin 설치

- Helm-s3 plugin은 Helm v2, v3와 동작하며 Helm chart와 Amazon S3의 상호작용을 담당해준다.  
  아래 명령어로 설치해주자.

  ```sh
  helm plugin install https://github.com/hypnoglow/helm-s3.git
  ```

- 마찬가지로 설치가 되었음을 검증하기 위해 version을 확인해보자.

  ```sh
  helm s3 version
  ```

#### 1.3 S3 bucket 생성

- 우선 아래 사진처럼 Amazon S3 console에 접속해 bucket 생성 화면으로 넘어가보자.

  ![picture 117](/images/HELM_S3_1.png)

- 다음으로 하단에 `Block all public access`를 체크 해제해준다.  
  이때 하단에 표시되는 경고 화면에는 체크 표시를 해준다.

- 나머지 설정값들은 기본값으로 그대로 두고, bucket을 생성한다.

- 마지막으로 해당 bucket의 페이지로 이동해, bucket 이름을 복사해주자.

#### 1.4 Helm-s3로 Helm repository 초기화

- 아래 명령어로 Helm repository를 초기화해준다.  
  `<bucket-name>`을 본인이 생서한 S3 bucket 이름으로 변경해주자.

  ```sh
  export S3_BUCKET_NAME=<bucket-name>
  helm s3 init s3://$S3_BUCKET_NAME
  # Initialized empty repository at s3://$S3_BUCKET_NAME
  ```

- 다음으로 아래 명령어로 S3를 Helm client가 repository로써 가리키도록 해주자.  
  `<repo-name>`은 자신이 원하는 값으로 바꿔주자. 나는 my-nginx로 진행했다.

  ```sh
  helm repo add <repo-name> s3://$S3_BUCKET_NAME
  # "my-nginx" has been added to your repositories
  ```

### 2. Helm chart 생성 및 packaging 후 S3에 업로드

- 지금까지 Helm repository를 S3에 생성했고, 해당 S3 bucket을 Helm repository로 추가했다.  
  아래 명령어로 진행 상황을 확인해보자.

  ```sh
  helm repo list
  # my-nginx    s3://roys-nginx-helm
  ```

#### 2.1 Helm chart 생성

- 이제 본격적으로 Helm chart를 만들어보자.  
  우선 `my-chart`라는 디렉토리를 생성하자.

  ```sh
  mkdir ./my-chart
  ```

- 다음으로 `my-chart` 디렉토리로 이동해, `Chart.yaml` 파일을 생성해 아래처럼 입력해보자.

  ```yaml
  apiVersion: v1
  appVersion: "1.0"
  description: Testing Helm Chart with Nginx
  name: my-nginx-chart
  version: 1.0.0
  ```

- 위에서 설명한대로, `Chart.yaml` 파일은 Helm chart에 대한 정보를 담는 yaml 파일로, 해당 Helm chart가 사용하는  
  API version, type, 그리고 version 정보들을 포함한다.

- 다음으로 Helm chart가 Kubernetes 리소스들을 생성할 때 설정값들을 참조해오는 `values.yaml` 파일을 만들어보자.

  ```yaml
  deploymentInfo:
    replicaCount: 2
    namespace: my-chart
    name: my-nginx-deployment
    port: 80
  image:
    name: nginx
    tag: latest
    pullPolicy: Always
  serviceInfo:
    name: my-nginx-service
    type: NodePort
    targetPort: 80
  ```

- 앞으로 `templates/` 폴더 하위의 Kubernetes manifest 파일들에서 `values.yaml` 파일의 설정값들을 참조해오게 된다.

- 마지막으로 실제로 nginx를 띄울 Deployment, 그리고 Service object를 작성해보자.  
  이전에, 먼저 `templates/` 폴더를 만들어주자.

  ```sh
  mkdir templates
  ```

- 그리고 해당 `templates/` 디렉토리 하위에 아래 2개 yaml 파일들을 생성하자.

  ```yaml
  # templates/deployment.yaml
  apiVersion: apps/v1
  kind: Deployment
  metadata:
    name: {{ .Values.deploymentInfo.name }}
    namespace: {{ .Values.deploymentInfo.namespace }}
    labels:
      app: {{ .Values.deploymentInfo.name }}
  spec:
    replicas: {{ .Values.deploymentInfo.replicaCount }}
    selector:
      matchLabels:
        app: {{ .Values.deploymentInfo.name }}
    template:
      metadata:
        labels:
          app: {{ .Values.deploymentInfo.name }}
      spec:
        containers:
          - name: {{ .Values.deploymentInfo.name }}
            image: {{ .Values.image.name }}:{{ .Values.image.tag }}
            imagePullPolicy: {{ .Values.image.pullPolicy }}
            ports:
              - containerPort: {{ .Values.deploymentInfo.port }}
                protocol: TCP

  ---

  # templates/service.yaml
  apiVersion: v1
  kind: Service
  metadata:
    name: {{ .Values.serviceInfo.name }}
    namespace: {{ .Values.deploymentInfo.namespace }}
  spec:
    selector:
      app: {{ .Values.deploymentInfo.name }}
    ports:
      - protocol: TCP
        port: {{ .Values.serviceInfo.targetPort }}
        targetPort: {{ .Values.deploymentInfo.port }}
    type: {{ .Values.serviceInfo.type }}
  ```

- 위 결과로 만들어진 `my-chart` 디렉토리의 구조는 아래와 같다.

  ```
  my-chart/
  ├── Chart.yaml
  ├── templates
  │   ├── deployment.yaml
  │   └── service.yaml
  └── values.yaml
  ```

- 이제 Helm chart를 위한 내용이 모두 완성되었으니, packaging을 수행해보자.  
  `my-chart/` 레포지토리에서 아래 명령어를 수행해 packaging을 해보자.

  ```sh
  helm package .
  # Successfully packaged chart and saved it to: DIRECTORY/my-chart/my-nginx-chart-1.0.0.tgz
  ```

- 위 명령어의 결과로 `my-nginx-chart-1.0.0.tgz` 압축 파일이 생성되었음을 볼 수 있다.

- 이제 마지막으로 해당 압축 파일(packaging된 파일)을 S3, 즉 Helm chart repository에 push 해보자.

  ```sh
  helm s3 push my-nginx-chart-1.0.0.tgz my-nginx
  # Successfully uploaded the chart to the repository.
  ```

- 정상적으로 push가 되었는지 확인하기 위해 아래 명령어를 수행해보자.

  ```sh
  aws s3 ls $S3_BUCKET_NAME
  # 2022-12-18 23:15:46        406 index.yaml
  # 2022-12-18 23:15:46       9623 my-nginx-chart-1.0.0.tgz
  ```