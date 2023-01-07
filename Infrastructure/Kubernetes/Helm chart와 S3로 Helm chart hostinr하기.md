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