# ECS+Fargate에서 Datadog Trace, Log 연계하기

- [이 글](https://github.com/sang-w0o/Study/blob/master/AWS/DevOps/ECS%2BFargate%EC%97%90%20Datadog%20APM%20%EC%84%A4%EC%A0%95%ED%95%98%EA%B8%B0.md)을 통해 ECS+Fargate 환경에서 Datadog APM 연계를 했으니, 이제 trace와 log를 보자.

- 위 글대로 진행하면 APM과 trace, 그리고 log까지 모두 Datadog에서 확인할 수 있다.  
  하지만 한 가지 답답한 점이 있는데, 바로 trace와 log가 연계되지 않는다는 것이다.  
  Trace와 log가 연계되지 않으면, 특정 요청이 흐르면서 찍은 로그들을 파악하기가 사실상 불가능하다. (눈으로 따라가야한다)

  ![picture 0](/images/DD_LOG_TRACE_CORRELATE_1.png)

- Datadog에서는 이를 위한 방법을 당연히 지원하고 있지만, 너무 문서들이 파편화되어 있어서 직접 정리한 한방 정리 문서이다.

## 1. 서버 로그 설정

- Trace와 log를 연계하기 위해서는 trace에 달리는 Trace ID와 로그가 연계되어야 한다.  
  Datadog에서는 이를 자동으로 연계시켜주는 automatic log injection이라는 기능이 있으니, 이를 활용해보자.

- 먼저 Spring Boot 기준으로, 아래의 logback appender를 추가해준다.

```kt
dependencies {
	// ..
	implementation("net.logstash.logback:logstash-logback-encoder:8.0")
}
```

- 그리고 logback 설정을 해줘야 하는데, 아래처럼 `net.logstash.logback.encoder.LogstashEncoder`를 appender로  
  설정해주어야 로그가 JSON format으로 출력된다.

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<configuration>
    <appender class="ch.qos.logback.core.ConsoleAppender" name="CONSOLE_JSON">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>
    <root level="INFO">
        <appender-ref ref="CONSOLE_JSON"/>
    </root>
</configuration>

```

---

## 2. 서버를 빌드하는 Dockerfile 수정

- [관련 가이드](https://docs.datadoghq.com/containers/docker/log/?tab=dockerfile)에 따라 서버를 빌드하는 Dockerfile에 `LABEL`을 추가해줘야한다. 그리고 서버 애플리케이션에 trace agent를 붙일 때, 다양한 Datadog 전용 환경변수들을 넣어줘야 하는데, `dd.service`, `dd.env`, `dd.version` 은 필수로 넣어줘야 한다.

- 아래는 Dockerfile 전문이다.

```Dockerfile
FROM eclipse-temurin:17-jre-alpine AS builder
LABEL "com.datadoghq.ad.logs"='[{"source": "java", "service": "Planit-Product"}]'
WORKDIR application

ARG JAR_FILE=build/libs/planit-server-1.0-SNAPSHOT.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM eclipse-temurin:17-jre-alpine
WORKDIR application
RUN  apk update \
  && apk add curl \
  && apk add wget \
  && rm -rf /var/cache/apk/*

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

ENTRYPOINT ["java", "-javaagent:dd-java-agent.jar", "-Ddd.profiling.enabled=true", "-Ddd.logs.injection=true", "-Ddd.service=Planit-Product", "-Ddd.version=2.1.0", "-Ddd.env=product", "-XX:FlightRecorderOptions=stackdepth=256", "org.springframework.boot.loader.JarLauncher"]
```

- 보면 하단의 `ENTRYPOINT` 에 `dd.version`, `dd.env`, `dd.service` 를 지정해주었다.  
  `LABEL`은 Datadog 가이드에 따라 설정해주었다.

---

## 3. Fluentbit 설정

- 지금 나는 아래 흐름으로 로그를 Datadog으로 보내고 있다.

  1. 애플리케이션이 로그를 stdout, stderr에 출력
  2. 이를 container 설정으로 fluentbit container로 전달
  3. fluentbit container가 Datadog으로 전달

- 아래는 필요한 정보만 추린 하나의 task definition 전문이다.

```json
{
  "executionRoleArn": "someArn",
  "containerDefinitions": [
    {
      "name": "PlanitDevLogRouter",
      "image": "amazon/aws-for-fluent-bitamazon/aws-for-fluent-bit:stable",
      "essential": true,
      "firelensConfiguration": {
        "type": "fluentbit",
        "options": {
          "enable-ecs-log-metadata": "true",
          "config-file-type": "file",
          "config-file-value": "/fluent-bit/configs/parse-json.conf"
        }
      },
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/PlanitDevLogRouter",
          "awslogs-region": "ap-northeast-2",
          "awslogs-stream-prefix": "PlanitDevLogRouter"
        }
      }
    },
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
          "value": "2.1.0.dev"
        }
      ],
      "dockerLabels": {
        "com.datadoghq.tags.env": "dev",
        "com.datadoghq.tags.service": "Planit-Dev",
        "com.datadoghq.tags.version": "2.1.0.dev"
      },
      "logConfiguration": {
        "logDriver": "awsfirelens",
        "options": {
          "Name": "datadog",
          "dd_service": "Planit-Dev",
          "provider": "ecs"
        },
        "secretOptions": [
          {
            "valueFrom": "DD_LOG_ROUTER_API_KEY",
            "name": "apikey"
          }
        ]
      }
    },
    {
      "name": "datadog-agent",
      "image": "datadog/agent:latest",
      "portMappings": [
        {
          "hostPort": 8126,
          "protocol": "tcp",
          "containerPort": 8126
        }
      ],
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
          "name": "DD_LOGS_ENABLED",
          "value": "true"
        },
        {
          "name": "DD_ENV",
          "value": "dev-datadog-agent"
        },
        {
          "name": "DD_SERVICE",
          "value": "Planit-Dev-Datadog-Agent"
        },
        {
          "name": "DD_VERSION",
          "value": "2.1.0.dev-datadog-agent"
        },
        {
          "name": "DD_APM_NON_LOCAL_TRAFFIC",
          "value": "true"
        },
        {
          "name": "DD_PROFILING_ENABLED",
          "value": "true"
        },
        {
          "name": "DD_APM_IGNORE_RESOURCES",
          "value": "GET /actuator/health"
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
  "family": "PlanitDevTask",
  "tags": [
    {
      "key": "project",
      "value": "planit"
    },
    {
      "key": "environment",
      "value": "production"
    }
  ]
}
```

- 여기서 중요한건 `amazon/aws-for-fluent-bit` 이미지를 사용하는 log routing을 위한 container의 설정 중,  
  `firelensConfiguration.options.config-file-value` 이다. 이 설정값으로 `/fluent-bit/configs/parse-json.conf`가  
  되어있는데, Datadog의 automatic log injection을 사용하려면 JSON 형식의 로그를 사용해야 하고, 이 설정을 적용해야만 로그를  
  JSON 형식으로 parsing해서 Datadog에 전달해준다.

  - [관련 Datadog 가이드](https://docs.datadoghq.com/integrations/ecs_fargate/?tab=webui)

- 이렇게 설정하면, Datadog에서 로그를 볼 때 아래처럼 `Event Attribute` 가 나온다.
  ![picture 1](/images/DD_LOG_TRACE_CORRELATE_2.png)

- 만약 위 설정을 사용하지 않으면 아무리 application에서 로그를 JSON으로 찍어도, 이 JSON이 통채로 stringify 되어 하나의 string으로  
  취급되기 때문에, 아래처럼 `Event Attribute`가 하나도 나오지 않고, JSON object가 통째로 찍힌다.
  ![picture 2](/images/DD_LOG_TRACE_CORRELATE_3.png)

---

- 여기까지 적용해야 log에서 trace를 찾고, 거꾸로 trace에서 log를 찾을 수 있다.

  - log에서 trace를 찾는 모습
    ![picture 3](/images/DD_LOG_TRACE_CORRELATE_4.png)

  - trace에서 log를 찾는 모습
    ![picture 4](/images/DD_LOG_TRACE_CORRELATE_5.png)
