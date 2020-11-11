<h1>Getting Started</h1>

* 처음 docker를 실행하면, 아래와 같은 명령어를 입력한다.
```
docker run -d -p 80:80 docker/getting-started
```

* 위 명령어에서 사용한 플래그들은 아래와 같다.
  * `-d` : Run the container in `DETACHED` mode(In the background)
  * `-p 80:80` : Map port 80 of the host to port 80 of the container
  * `docker/getting-started` : The image file to use.
  
* 아래와 같이 명령어를 조합하여 사용할 수도 있다.
```
docker run -dp 80:80 docker/getting-started
```

* What is a container?
  * `Container`란 간단히 말해 호스트 머신의 모든 프로세스들에서 분리된 내 머신에서의 하나의 프로세스이다.   
    분리되었다함은 kernal namespaces, cgroups 등의 Linux상의 기능들에 영향력은 행사한다.

* What is a container image?
  * 컨테이너를 실행할 때, 해당 컨테이너는 독립된 파일시스템을 사용한다. 이 독립되고 customized된   
    파일 시스템은 `Container Image`가 제공한다. Container image가 해당 컨테이너의 파일 시스템에 대한   
    정보들을 가지기 때문에, container image는 수행할 애플리케이션을 위한 모든 정보를 가지고 있어야 한다.   
    (Dependencies, Congifuration, Scripts, Binaries... etc)   
    Container Image는 컨테이너에 대한 추가적인 설정 사항(환경 셜정, 기본 명령 등)에 대한 메타데이터도   
    가지게 된다.
<hr/>

<h2>Our Application</h2>

* Application을 위한 코드를 작성했다면, `Dockerfile`을 사용해서 해당 애플리케이션을 빌드해야 한다.   
  `Dockerfile`은 텍스트 기반의 간단한 스크립트로, `Container Image`를 생성하기 위한 규칙들을 정의한다.   

* 이 예시는 `node.js` 기반으로 설명된다.

1. 우선 `package.json`이 있는 부분에 `Dockerfile`이라는 파일을 만들어보자.

```Dockerfile
FROM node:12-alpine
WORKDIR /app
COPY . .
RUN yarn install --production
CMD ["node", "src/index.js"]
```

  * `Dockerfile`은 확장자가 없다는 것에 주의하자.

2. 다음으로는 `Dockerfile`이 존재하는 디렉토리로 이동하여, 아래의 명령어를 실행한다.
```
docker build -t getting-started .
```

  * 위 명령어는 `Dockerfile`을 이용해서 `Container Image`를 생성한다. 설치 시 화면을 보면 많은   
    "layer"들이 설치되는데, 이는 우리가 builder에게 `node:12-alpine` image로부터 시작하도록 지정했기   
    때문이다. 하지만 이 image가 설치되어있지 않으므로, 설치를 진행한 것이다.

  * 이미지 파일이 설치된 후, 우리는 `COPY`를 사용해 애플리케이션을 복사했고, `yarn`을 이용하여   
    애플리케이션에 필요한 의존성들을 설치했다. `CMD` 명령은 이 `Dockerfile`로부터 생성된 image를 실행할 때   
    실행할 기본 명령어를 지정한다.
  
  * `-t` 플래그는 이미지를 tag 한다. 단순히 설명해, 최종 image에 대해 사람이 읽을 수 있는 값을 부여한 것이다.   
    위 명령에서는 그 값을 getting-started로 지정했기 때문에, 나중에 container를 실행할 때 이 이름을   
    사용하면 된다.

  * 명령에 마지막에 있는 `.`는 `Docker`가 현 위치(`.`)에서 `Dockerfile`을 찾게 하도록 지정해준 것이다.
<hr/>

<h2>Starting an App Container</h2>

* 이제 이미지 파일을 생성했으므로, 애플리케이션을 실행해보도록 하자.   
  애플리케이션을 실행하기 위해서는 `docker run` 명령을 사용한다.   
  맨 처음에 사용했던 `docker run` 명령어를 다시 보면 좋다.
```
docker run -dp 3000:3000 getting-started
```

  * `-d`와 `-p` 플래그를 지정했기 때문에, 위 명령으로 인해 새로운 Container가 `DETACHED` mode에서   
    (백그라운드에서 수행) 작동하게 했으며, 호스트의 3000번 포트와 컨테이너의 3000번 포트를 매핑했다.   
    포트를 매핑하지 않으면 해당 애플리케이션에 접근할 수 없다.

  * 명령이 수행되었으면, `http://localhost:3000`로 접속을 해보자.

  * 이제 Docker Dashboard를 확인하면, 두 개의 container들이 각각 80번, 3000번 포트에서 작동중임을   
    확인할 수 있다.
<hr/>

<h2>Updating our App</h2>

* 기능 요구 사항이 들어와, 새로운 기능이 추가되어 작은 업데이트가 생겼다고 해보자.   
  즉, 소스코드의 변경이 일어난 경우를 말하는 것이다.

* 기존에 했던 방식대로 다시 빌드를 해서 이미지 파일을 만들고, 컨테이너를 실행해보도록 하자.
```
docker build -t getting-started .
docker run -dp 3000:3000 getting-started
```

* 명령을 수행하면 아래와 같은 오류가 발생한다.
```
docker: Error response from daemon: driver failed programming external connectivity on endpoint sleepy_meitner (ec27e091ee1a2ff51f72b0a50dd341e9673e13b2d92237dec2e9f51542c74fe8): Bind for 0.0.0.0:3000 failed: port is already allocated.
```

* 위 오류는 이전 애플리케이션 이미지 파일을 가진 container가 이미 실행중이기 때문에 발생했다.   
  이유는 이전 container가 여전히 호스트의 3000번 포트를 사용중이고, 단 하나의 프로세스만이 장치의 포트를   
  사용할 수 있다. 이를 고치기 위해서는 이전 container를 제거해야 한다.

* CLI로 container를 제거하는 방법은 아래와 같다.

  1. 제거할 container의 ID를 `docker ps` 명령어로 가져온다.
  ```
  docker ps
  ```

  2. ID를 파악안 후 `docker stop`을 이용해 container를 중지시킨다.
  ```
  docker stop <the-container-id>
  ```

  3. 마지막으로 container를 삭제하기 위해 `docker rm`을 실행한다.
  ```
  docker rm <the-container-id>
  ```

  * Container를 중지하고 삭제하는 명령어를 `-f` 플래그(force)를 사용해서 한 줄로 처리할 수 있다.
  ```
  docker rm -f <the-container-id>
  ```

* 이제 업데이트된 container를 실행해보자.
```
docker run -dp 3000:3000 getting-started
```

<hr/>

