# HTTP 1.1과 HTTP 2.0의 차이

# 들어가며

- HTTP(HyperText Transfer Protocol)은 WWW(World Wide Web)상에서의 소통을 위한 표준화된 방식으로  
  1997년에 사용되기 시작한 통신 수단이다. 1997년부터 현재까지 이 프로토콜에는 여라 가지의 수정사항이 있었다.  
  하지만 2015년에 들어서면서 통신 지연에 대한 여러 가지 해결점(특히 모바일 플랫폼, 그래픽, 영상)들을 제공하는  
  HTTP 2.0이 사용되기 시작했다. 이후로 HTTP 2.0의 사용률은 급격하게 증가했다.

<hr/>

# HTTP 1.1

- Timoty Berners-Lee라는 사람에 의해 개발되어 WWW의 표준화된 통신 수단으로 사용된 HTTP는  
  클라이언트와 로컬 또는 원격의 서버와 통신하는 Top-Level Application Protocol이다.  
  이 통신 과정에서, 클라이언트는 텍스트 기반의 요청을 `GET`, `POST` 등의 HTTP Method를 통해  
  전송하게 된다. 이에 대한 응답으로, 서버는 JSON 또는 HTML 페이지와 같은 리소스를 읃답해준다.

- 예를 들어, 우리(클라이언트)가 `www.example.com`이라는 웹사이트를 방문한다고 해보자.  
  이 URL로 접속하게 되면, 우리가 사용하는 웹 브라우저는 텍스트 기반의 HTTP Request를 보내게 된다.  
  아래는 HTTP Request의 예시이다.

```
GET /index.html HTTP/1.1
Host: www.example.com
```

- 위 예시에서의 요청은 `GET` 메소드를 사용하며, Host로 지정되어 있는 www.example.com에 요청을 보내게 된다.  
  이에 대한 응답으로, example.com의 웹서버는 HTML페이지를 요청한 클라이언트에게 전달해준다.  
  이 과정에서, 단순히 HTML 파일만 보내주는 것이 아니라 이 페이지에서 사용하는 이미지, CSS 파일, 다른 스크립트 파일들이  
  모두 포함되어 있다. 한 가지 주의할 점은, **첫 요청에 모든 리소스가 클라이언트에게 전달되는 것이 아니다** 는 것이다.  
  요청과 응답의 과정은 서버와 클라이언트 사이에서 HTML페이지를 모두 렌더링할 수 있게 될 때 까지 여러 번 진행된다.

<hr/>

# HTTP 2.0

- HTTP 2.0은 Google에서 웹 페이지 로딩 지연을 해결하기 위해 내세운 기술들(압축, Multiplexing, Prioritization)을  
  적용한 SDPY 프로토콜로 시작되었다.

- 기술적인 관점에서 본다면 HTTP/1.1과 HTTP/2를 구분짓는 핵심적인 요소들 중 하나는 binary framing layer이다.  
  Binary framing layer는 인터넷 프로토콜 스택 중 가장 최상위에 있는 Application Layer의 일부라고 보면 된다.

- HTTP/1.1이 모든 요청과 응답에 대한 정보를 일반적인 텍스트(plain text)로 모두 저장하고 있다는 점과 다르게,  
  HTTP/2.0은 Binary framing layer를 사용하여 모든 메시지를 2진수 형태(Binary Format)로 저장한다는 것이다.  
  하지만 이렇게 2진수로 저장함과 동시에 HTTP verb, Method, Header등과 같은 기본적인 정보를 유지하도록 되어있다.

- Application Level의 API는 계속 관례적인 HTTP 형식으로 메시지를 생성하지만, 안쪽 layer에서는 이 메시지들을  
  2진수로 변환하여 저장하는 것이다. 이 절차는 HTTP/2.0 이전에 만들어진 웹 애플리케이션들이 HTTP/2.0을 사용하여  
  소통하는데 문제가 없다는 것을 보장해준다.

- 이렇게 메시지들을 2진수로 변환하는 과정은 HTTP/1.1에서는 수행될 수 없던 데이터들의 전달의 한계점을 극복해준다.

<hr/>

# Delivery Models

- 위에서 말한대로, HTTP 1.1과 HTTP 2.0은 기본적인 형식을 공유하여 서버와 클라이언트 사이에 오고가는  
  요청과 응답이 전통적으로 포맷팅 되어 있는 메시지와 헤더, Body와 함께 목적지에 도달하도록 해준다.  
  하지만 HTTP 1.1은 이 과정을 Plain Text로 구성된 메시지로 진행하는 반면, HTTP 2.0은 2진수로 진행한다.

- 이제 HTTP 1.1이 Delivery Model을 이용해서 메시지를 어떻게 효과적으로 최적화하려고 시도했는지와  
  이로 인해 생겨난 문제점을 알아보자. 그리고 HTTP 2.0의 Binary framing layer의 이점과  
  요청들의 우선순위를 어떻게 책정하는지에 대해 알아보자.

<h2>HTTP/1.1 - Pipelining and Head-Of-Line Blocking</h2>

- 클라이언트가 HTML 페이지에 대해 처음으로 `GET` 요청을 보냈을 때, 대부분의 경우에 응답으로 오는 페이지로는  
  전체적인 렌더링을 할 수 없다. 대신, 첫 번째 요청에 대한 응답은 요청한 페이지에서 추가적으로 사용할 리소스들에 대한  
  정보(링크 등)들을 포함한다. 클라이언트는 첫 번째 `GET` 요청에 대한 응답이 오고 나서야 이 페이지를 렌더링하기 위해  
  추가적인 리소스들을 더 받아와야 한다는 사실을 알게 된다. 따라서 자연스럽게 클라이언트는 서버에게 추가적인 요청을 보내게 된다.  
  HTTP/1.0에서는 클라이언트가 새로운 요청을 보낼 때마다 TCP 연걸을 다시 만들어야 했다.

- HTTP/1.1은 HTTP/1.0이 매 요청마다 새로운 TCP 연결을 수립해야 한다는 매우 많은 비용이 소모되는 작업을  
  `Persist Connection`와 `Pipelining`으로 해결한다.  
  `Persist Connection(계속되는 연결)`을 사용해서, HTTP/1.1은 해당 TCP연결을 닫으라는 메시지가 있기 전까지  
  Connection을 끊지 않는다. 이는 클라이언트가 같은 Connection 내에서 요청에 대한 응답을 기다리지 않고 비동기적으로  
  여러 개의 요청을 동시다발적으로 보낼 수 있게 해준다. 이렇게 하나의 요청을 보내고, 그에 대한 응답을 기다리고, 그 뒤에  
  새로운 요청을 보내는 작업이 HTTP/1.1이 HTTP/1.0에 비해 성능이 매우 향상될 수 있던 요인 중 하나이다.

- 하지만 `Persist Connection`을 사용한 효율화는 원천적인 단점이 있다.  
  여러 개의 Data Packet이 서로를 앞질러서 목적지(Destination)에 도달할 수 없기에, (Queue라고 보면 된다.)  
  만약 가장 먼저 수행된 요청(큐의 맨 앞)이 모종의 이유로 응답을 받아올 수 없다면, 뒤에 수행된 요청들에 대한 응답들도  
  모두 받아오지 못한다.

- 위의 문제점을 `HOL(Head-Of-Line) Blocking` 현상이라 하며, 이는 Connection을 효율화한 HTTP/1.1의  
  매우 큰 문제점이다. 이 `HOL Blocking` 현상을 방지하기 위해 HTTP/1.1은 병렬적인 TCP Connection의 수립으로 이 문제를  
  해결하려 했다. 하지만 애초에 클라이언트와 서버 사이에서 동시적으로 수립될 수 있는 TCP Connection의 개수에 제한이 있기에 완전한  
  해결책은 아니다.

<hr/>

<h2>HTTP/2.0 - Binary Framing Layer의 이점</h2>

- HTTP/2.0 에서, Binary framing layer는 요청과 응답을 2진수로 인코딩하며, 이들을 더 작은 정보의 단위로  
  쪼개게 되어 데이터 송수신 작업에 유연함을 크게 더해준다.

* 우선 HTTP/1.1이 HOL Blocking 현상을 방지하기 위해 여러 개의 TCP Connection들을 수립해야 했던 것과 반대로,  
  HTTP/2.0은 **단 1개의 TCP 연결** 만을 수립한다. 이 연걸 안에는 **data로 구성된 여러개의 Stream** 이 있다.  
  각 Stream은 익숙한 요청/응답의 형식으로 된 메시지들을 담고 있다.  
  마지막으로, 각 하나의 Stream안의 여러 메시지들을 각각 **Frames** 라 하는 더 작은 단위로 쪼개어진다.

![picture 1](../images/a95bd214171a63466aa06dd324ebd3228163c2e6567a1c56c7cfb050a39bac8d.png)

- Communication Channel은 여러 개의 2진법으로 인코딩되어 있는 Frame들으 가지며, 이 Frame들은 각각  
  특정 Stream에 소속되어 있다. 각 Frame들은 고유 식별자로 `Identifying Tag`를 가지는데, 이 태그를 통해  
  Connection이 다른 작업들을 수행하고 난 후에 다시 이 작업을 수행할 수 있도록 해준다.  
  즉, 요청과 응답의 과정이 하나의 Connection 내에서 병렬적으로 수행되며, 비동기적으로 수행될 수 있다는 것이다.  
  이 과정을 `Multiplexing`이라 한다. Multiplexing은 HTTP/1.1의 HOL Blocking 현상을 해결해준다.  
  이는 또한 서버와 클라이언트가 동시다발적으로 다수의 요청과 응답을 주고받을 수 있다는 것을 의미하며, 곧  
  훨씬 조절 가능하고 효율적인 Connection 관리가 가능해짐을 의미한다.

- Multiplexing이 클라이언트가 병렬적으로 여러 개의 Stream들을 만들 수 있도록 해주기 때문에, 이 Stream들은  
  **단 1개의 TCP Connection** 만을 사용해도 된다. Origin별로 `Persist Connection`을 가능하게끔 해준  
  HTTP/1.1 덕분에, 이는 훨씬 효율적인 네트워크의 사용과 모든 연산 비용을 최적화해준다.

- 단 하나의 TCP Connection만을 사용해도 된다는 것은 HTTPS Protocol의 성능도 향상시켜준다.  
  왜냐하면 클라이언트와 서버가 안전하게 관리되는 세션 하나로 여러 개의 요청과 응답을 주고받을 수 있기 때문이다.  
  HTTPS내에서 TLS 또는 SSL Handshake 과정에서, 클라이언트와 서버는 단 하나의 key를 해당 세션 내에서  
  사용하도록 한다. 만약 해당 세션이 종료되고, 새로운 세션이 시작되녀면 그에 맞게 key도 새롭게 발급해야 한다.  
  따라서 하나의 Connection을 유지하는 것은 HTTPS를 사용하는데에 매우 효율적일 수 밖에 없다.

- 한 가지 유의할 점은, 많은 브라우저들이 HTTP/2.0을 HTTPS 프로토콜로만 지원한다는 점이다.

<hr/>
