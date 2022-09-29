# Datadog APM 설정하기

- AWS ECS, Fargate의 조합을 사용하는 애플리케이션에 Datadog APM을 설정하는 과정을 살펴보자.

> 이 예시에서는 Spring boot application이겠지만, 다른 언어를 사용한 프로그램이라도 과정은 비슷할 것이다.

## Dockerfile 수정하기

- JVM 상에 동작하는 애플리케이션의 APM을 수집하기 위해서는 Datadog이 제공하는 agent를 함께 사용해야 한다.  
  Datadog 공식 문서에는 Java application(Kotlin, Groovy 등 JVM을 사용하는 애플리케이션)에 대해 아래의 명령어들을 사용해  
  agent를 설치하고, 실행하라고 작성되어 있다.

![picture 14](/images/DEVOPS_DATADOG_APM_1.png)

- 여기서 나는 도대체 `wget`으로 agent를 설치하고, `java -jar` 명령어를 어디다가 둬야 하는지 감이 잡히지 않았다.  
  해답은 간단했는데, 바로 Docker 이미지를 빌드하는 Dockerfile에 해주는 것이었다.

- 완성된 Dockerfile은 아래와 같다.

```Dockerfile
FROM openjdk:11-jdk AS builder
WORKDIR application
ARG JAR_FILE=build/libs/planit-server-1.0-SNAPSHOT.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM openjdk:11-jdk
WORKDIR application
RUN  apt-get update \
  && apt-get install -y wget \
  && rm -rf /var/lib/apt/lists/*
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

> ENTRYPOINT 배열 안의 각 원소들은 꼭 개행 없이 한 줄로 써줘야 하나보다.

---

## Task Definition 수정하기

- Datadog이 metric도 수집하고 과 APM을 수행하게끔 하기 위해선 기존에 Spring boot application을 수행하는 컨테이너에도 특정 환경 변수를  
  지정해줘야 하고, 그와 나란히 수행되는 Datadog agent container를 또 띄워줘야 한다.

- 아래는 task definition을 JSON으로 정의한 내용이다.

```json
{
  "executionRoleArn": "arn:aws:iam::598334522273:role/ecsTaskExecutionRole",
  "containerDefinitions": [
    {
      "name": "PlanitDevContainer",
      "image": "",
      "essential": true,
      "portMappings": [
        {
          "hostPort": 8080,
          "protocol": "tcp",
          "containerPort": 8080
        }
      ],
      //       Datadog 관련 환경 변수
      "environment": [
        {
          "name": "DD_ENV",
          "value": "dev"
        },
        {
          "name": "DD_SERVICE",
          "value": "Planit-Dev"
        },
        {
          "name": "DD_VERSION",
          "value": "0.1.0.dev"
        },
        {
          "name": "DD_LOGS_INJECTION",
          "value": "true"
        }
      ],
      //       Datadog을 위한 dockerLabel
      "dockerLabels": {
        "com.datadoghq.tags.env": "dev",
        "com.datadoghq.tags.service": "Planit-Dev",
        "com.datadoghq.tags.version": "0.1.0.dev"
      },
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/PlanitDev",
          "awslogs-region": "ap-northeast-2",
          "awslogs-stream-prefix": "PlanitDev"
        }
      }
    },
    //     Datadog Agent container
    {
      "name": "datadog-agent",
      "image": "public.ecr.aws/datadog/agent:latest",
      "portMappings": [
        {
          "hostPort": 8126,
          "protocol": "tcp",
          "containerPort": 8126
        }
      ],
      //       Datadog에서 발급받은 API KEY
      "secrets": [
        {
          "name": "DD_API_KEY",
          "valueFrom": "DEV_DATADOG_API_KEY"
        }
      ],
      "environment": [
        {
          "name": "ECS_FARGATE",
          "value": "true"
        },
        {
          "name": "DD_SITE",
          "value": "datadoghq.com"
        },
        {
          "name": "DD_APM_ENABLED",
          "value": "true"
        },
        {
          "name": "DD_ENV",
          "value": "dev"
        },
        {
          "name": "DD_SERVICE",
          "value": "Planit-Dev"
        },
        {
          "name": "DD_VERSION",
          "value": "0.1.0.dev"
        },
        {
          "name": "DD_LOGS_INJECTION",
          "value": "true"
        },
        {
          "name": "DD_APM_NON_LOCAL_TRAFFIC",
          "value": "true"
        },
        {
          "name": "DD_PROFILING_ENABLED",
          "value": "true"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/PlanitDevDatadog",
          "awslogs-region": "ap-northeast-2",
          "awslogs-stream-prefix": "PlanitDevDatadog"
        }
      }
    }
  ],
  "requiresCompatibilities": ["FARGATE"],
  "networkMode": "awsvpc",
  "cpu": "512",
  "memory": "1024",
  "family": "PlanitDevTask"
}
```

- 이후 빌드를 완료하고 새로운 task를 배포하면 아래와 같이 `APM -> Services`에 새로운 서비스가 추가된다.

![picture 15](/images/DEVOPS_DATADOG_APM_2.png)

---
