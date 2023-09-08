# Docker image 크기 줄이기

## Docker image 크기의 중요성

- 컨테이너로 애플리케이션을 배포할 때 자주 `docker build` 명령어로 애플리케이션의 image를 생성하는데,  
  생각보다 의외로 이 image의 크기가 꽤 중요하다. 그 이유는 아래와 같다.

  - Container orchestration: ECS, Kubernetes 등의 다양한 container orchestration 도구들을  
    사용할 때, image의 크기가 작을수록 더 적은 리소스를 사용하게 되고, 처리량 등 일정 기준에 따라 새로운  
    container를 실행시키는 시간이 줄어든다. 따라서 더 빠르게 scale-out이 가능하다.

  - 이미지 로딩 속도: 당연하게도 네트워크를 타서 이미지를 전송하거나(`docker push`), 이미지를 받아와 실행하는  
    경우에도 이미지의 크기가 작으면 작을수록 더 빨리 가져올 수 있다.

  - 이미지 빌드 속도: CI, 그리고 local 환경에서도 마찬가지로 이미지의 크기가 작을수록 더욱 빠르게 빌드할 수 있다.

- 이렇게 docker image의 크기가 작은 것은 개발 및 지속적인 배포에도 다양한 이점들을 가져다 준다.

---

## 이미지 크기 줄여보기

- 기존에는 아래와 같이 `Dockerfile`이 구성되어 있었다.  
  Spring boot 애플리케이션의 이미지를 만들어내기 위한 `Dockerfile`이었으며, 빌드된 이미지의 크기는 대략 854MB 였다.

```dockerfile
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

- 이미지의 크기를 줄이고 빌드 속도를 줄이는 작업이 일부 적용되어 있는 상황이었다.  
  다만 생각보다 이미지의 크기가 거대했고, 그 이유를 찾던 도중 base image의 크기를 보게 되었다.

- 위에 있는 `openjdk:11-jdk` 이미지의 경우 317.68MB의 공간을 차지하고 있었다. [openjdk:11-jdk image layers](https://hub.docker.com/layers/library/openjdk/11-jdk/images/sha256-e81b7f317654b0f26d3993e014b04bcb29250339b11b9de41e130feecd4cd43c?context=explore)

- 위 상황을 줄이기 위한 방법에는 두 가지가 존재한다.

  - (1) JDK 대신 JRE 사용하기

    - JDK는 Java Development Kit의 약자로, Java 환경을 위한 도구들, 실행 파일, 바이너리 등 컴파일, 디버깅 및 실행을 위한 모든 내용을  
      담고 있으며, JRE의 superset(상위 집합)이다.

    - JRE는 Java Runtime Environment의 약자로 JVM의 구현체이며, Java 프로그램들을 실행하기 위한 플랫폼을 말한다.  
      JDK와 마찬가지로 JVM, Java 바이너리 파일들, 그리고 프로그램을 정상적으로 실행하기 위한 여러 클래스들을 가지지만  
      JDK와는 달리 Java 컴파일러, 디버거, JShell 등을 포함하지 않는다. 즉, **JRE는 Java 프로그램을 실행하기 위해 필수적으로 필요한 것들만을 포함하고 있다.**

    - 위 비교를 통해 base image를 `openjdk:11-jdk`가 아닌 `openjdk:11-jre` 로 바꾸면 이미지의 크기를 줄일 수 있음을 알 수 있다.

  - (2) slim tag 사용하기

    - `openjdk:11-jre`가 있다면, `openjdk:11-jre-slim` 이 존재한다. 이렇게 많은 이미지들은 `slim` 이라는 단어가 포함되어 tagging된  
      이미지를 제공하는데, `slim`이 있는 이미지들은 `slim`이 없는 이미지들에 비해 base image에 포함되어 있는 도구들이 적기에 이미지 크기가  
      훨씬 작다. 예를 들어, `openjdk:11-jre`의 크기는 [116.79MB](https://hub.docker.com/layers/library/openjdk/11-jre/images/sha256-762d8d035c3b1c98d30c5385f394f4d762302ba9ee8e0da8c93344c688d160b2?context=explore)인데 반해, `openjdk:11-jre-slim`의 크기는 [75.33MB](https://hub.docker.com/layers/library/openjdk/11-jre-slim/images/sha256-884c08d0f406a81ae1b5786932abaf399c335b997da7eea6a30cc51529220b66?context=explore)이다.

    - 이렇게 base image를 slim으로 바꾸는 것 만으로도 이미지 크기를 상당히 줄일 수 있다.

- 따라서 바뀐 Dockerfile은 아래와 같다.

```dockerfile
FROM openjdk:11-jre-slim AS builder
WORKDIR application

ARG JAR_FILE=build/libs/planit-server-1.0-SNAPSHOT.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM openjdk:11-jre-slim
WORKDIR application
RUN  apt-get update \
  && apt-get install -y curl \
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

---

## 결과

- 기존 이미지의 크기는 854MB였던 반면, 경량화 후의 이미지 크기는 385MB로, 약 55% 정도 줄어들었다.

---
