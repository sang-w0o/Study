# EKS에 Datadog APM 구축하기

## helm으로 Datadog agent 설치하기

- 이 [문서](https://docs.datadoghq.com/containers/kubernetes/installation/?tab=helm)에 나와있는대로 datadog agent의 helm repository를 추가하자.

```sh
helm repo add datadog https://helm.datadoghq.com
helm repo update
```

- 이후 Kubernetes에 설치될 datadog agent가 사용할 파일은 [values.yaml](https://github.com/DataDog/helm-charts/blob/main/charts/datadog/values.yaml)인데, 여기서 특정 값만 찾아서 수정해주자.

  - `registry`: 주석에 적혀있는 값들 중 AWS에 해당하는 값을 지정한다.
  - `datadog.apiKey`: Datadog에서 발급받은 API key를 입력한다.
  - `datadog.apm.portEnabled`: true로 수정한다.

- 다음으로 datadog agent를 설치해보자.

```sh
helm install datadog-agent -f values.yaml datadog/datadog
```

---

## APM 설정하기

- 이제 APM을 설정해볼 것인데, 우선 기본적으로 각 언어에 맞는 datadog agent를 설치해야 한다.  
  이 datadog agent는 위에서 설치한 agent와는 별개의 것으로, 프로그램 실행 시에 사용하도록 지정해줘야 한다.  
  예를 들어 나는 Spring boot를 사용하기에 JVM datadog agent를 설치했다.

- 이 agent가 설치되어 프로그램을 실행하는 Dockerfile은 아래와 같다.

  ```Dockerfile
  FROM openjdk:11-jdk AS builder
  WORKDIR application
  ARG JAR_FILE=build/libs/planit-server-1.0-SNAPSHOT.jar
  COPY ${JAR_FILE} application.jar
  RUN java -Djarmode=layertools -jar application.jar extract

  FROM openjdk:11-jdk
  WORKDIR application
  RUN apt-get update \
   && apt-get install -y wget \
   && rm -rf /var/lib/apt/lists/\*
  RUN wget -O dd-java-agent.jar 'https://dtdg.co/latest-java-tracer'

  COPY --from=builder application/application.jar ./application.jar
  RUN true
  COPY --from=builder application/dependencies/ ./
  RUN true
  COPY --from=builder application/spring-boot-loader/ ./
  RUN true
  COPY --from=builder application/snapshot-dependencies/ ./
  RUN true
  COPY --from=builder application/application/ ./
  RUN true

  ENV TZ Asia/Seoul

  ENTRYPOINT ["java", "-javaagent:dd-java-agent.jar", "-Ddd.profiling.enabled=true", "-XX:FlightRecorderOptions=stackdepth=256", "org.springframework.boot.loader.JarLauncher"]
  ```

- 다음으로 위 프로그램에 설치된 datadog agent가 사용할 Kubernetes secret을 보자.  
   당연히 모든 값은 base64 encoding되어 지정되어야 한다.

  ```yaml
  apiVersion: v1
  kind: Secret
  metadata:
  name: datadog-secret
  namespace: planit
  data:
  DD_API_KEY: "YOUR_BASE64_ENCODED_API_KEY"
  DD_SITE: ZGF0YWRvZ2hxLmNvbQ==
  DD_EKS_FARGATE: dHJ1ZQ==
  DD_APM_ENABLED: dHJ1ZQ==
  DD_ENV: "YOUR_BASE64_ENCODED_ENV"
  DD_SERVICE: "YOUR_BASE64_ENCODED_SERVICE"
  DD_VERSION: "YOUR_BASE64_ENCODED_VERSION"
  DD_PROFILING_ENABLED: dHJ1ZQ==
  DD_LOGS_INJECTION: dHJ1ZQ==
  ```

- 마지막으로 Kubernetes deployment 파일을 보자. APM 사용 시 모든 datadog agent는 각 pod에 sidecar로 사용되어야 한다.

  ```yaml
  apiVersion: apps/v1
  kind: Deployment
  metadata:
  name: planit-deployment
  namespace: planit
  labels:
    app: planit
  spec:
  replicas: 3
  revisionHistoryLimit: 2
  selector:
    matchLabels:
      app: planit
  template:
    metadata:
      labels:
        app: planit
    spec:
      containers:
        - name: planit-product
          image: "YOUR_IMAGE_NAME"
          imagePullPolicy: Always
          envFrom:
            - secretRef:
                name: planit-secret
          ports:
            - containerPort: 8080
              protocol: TCP
          resources:
            limits:
              cpu: 1
              memory: 4096Mi
            requests:
              cpu: 1
              memory: 4096Mi
        - name: datadog-agent
          image: datadog/agent:latest
          ports:
            - containerPort: 8126
              name: traceport
              protocol: TCP
          env:
            - name: DD_KUBERNETES_KUBELET_NODENAME
              valueFrom:
                fieldRef:
                  apiVersion: v1
                  fieldPath: spec.nodeName
            - name: DD_API_KEY
              valueFrom:
                secretKeyRef:
                  name: datadog-secret
                  key: DD_API_KEY
            - name: DD_SITE
              valueFrom:
                secretKeyRef:
                  name: datadog-secret
                  key: DD_SITE
            - name: DD_EKS_FARGATE
              valueFrom:
                secretKeyRef:
                  name: datadog-secret
                  key: DD_EKS_FARGATE
            - name: DD_APM_ENABLED
              valueFrom:
                secretKeyRef:
                  name: datadog-secret
                  key: DD_APM_ENABLED
            - name: DD_ENV
              valueFrom:
                secretKeyRef:
                  name: datadog-secret
                  key: DD_ENV
            - name: DD_SERVICE
              valueFrom:
                secretKeyRef:
                  name: datadog-secret
                  key: DD_SERVICE
            - name: DD_VERSION
              valueFrom:
                secretKeyRef:
                  name: datadog-secret
                  key: DD_VERSION
            - name: DD_PROFILING_ENABLED
              valueFrom:
                secretKeyRef:
                  name: datadog-secret
                  key: DD_PROFILING_ENABLED
            - name: DD_APM_IGNORE_RESOURCES
              valueFrom:
                secretKeyRef:
                  name: datadog-secret
                  key: DD_APM_IGNORE_RESOURCES
            - name: DD_LOGS_INJECTION
              valueFrom:
                secretKeyRef:
                  name: datadog-secret
                  key: DD_LOGS_INJECTION
  ```

- 이제 이 Kubernetes deployment를 배포하고 Datadog에 접속하면 아래처럼 APM이 활성화된다.

  ![picture 54](/images/AWS_DEVOPS_EKS_DATADOG_APM_1.png)
