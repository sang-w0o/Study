# HTTP2 Server Sent Events

## Server-Sent Events

- Server Sent Event(SSE)는 HTTP를 통해 server가 push한 데이터를 client가 받는 작업을 기술을 말한다.  
  이 데이터는 랜덤한 데이터일 수도 있고, 일정한 data stream일 수도 있다. 가장 큰 이점은 **클라이언트가**  
  **데이터 수신을 위해 polling할 필요가 없다** 는 점이다. Client에서 server로 필요한 상호작용이 없기 때문이다.

---

## SSE vs WebSocket

- WebSocket이 SSE보다 더 기능이 많다는 것은 사실이지만, 항상 WebSocket이 좋은 선택지인 것은 절대 아니다.  
  WebSocket의 경우, client에서 server로, 그리고 그와 동시에 server에서 client로의 양방향 통신이 가능하다.  
  하지만 이 connection을 유지하기 위한 비용이 필요하다. Connection이 stateful 하기 때문이다.  
  아래는 SSE와 WebSocket의 차이를 나타낸 그림이다.

  ![picture 1](/images/OTHERS_SSE_1.png)

---

## SSE를 왜 써야할까?

- WebSocket과 SSE의 주목할만한 차이점은 WebSocket은 별도의 프로토콜(ws)을 사용하는 반면, SSE는 HTTP를  
  사용한다는 것이다. 이는 곧 SSE를 사용하기 위해 STOMP, MQTT 등의 별도의 프로토콜, 그리고 서버의 구현 비용이  
  증가되는 일이 없다는 것이다. SSE는 여기에 더해 connection을 재수립하는 과정, event ID를 기본 기능으로 제공한다.  
  이는 WebSocket에는 없는 기능이다.

---

## HTTP/1.1 vs HTTP/2

- HTTP/1.1에는 HTTP/1.0에서 요청을 할 때 마다 connection을 수립해야 하는 비효율성을 없애기 위해  
  Persistent Connection이 추가되었다. 이를 통해 여러 요청을 하나의 connection을 통해 전송할 수 있었는데,  
  응답의 순서는 정해져있었기 때문에 앞의 응답이 지연되면 뒤의 모든 응답 packet들도 함께 지연되어 버리는  
  Head-of-Line Blocking 문제가 있었다.

- 반면 HTTP/2에서는 connection을 여러 개의 stream으로 분류하고, 각 stream에 우선순위를 부여할 수 있는  
  Stream Priority 기능을 제공하기 때문에 HoL Blocking 문제를 방지할 수 있다.

  ![picture 2](/images/OTHERS_SSE_2.png)

> 이 둘 간의 비교는 [여기](https://github.com/sang-w0o/Study/blob/master/Others/HTTP1.1_HTTP2.0.md)에서 더 자세히 확인할 수 있다.

---

## SSE와 HTTP/2

- SSE에는 여러 가지 알아둬야 할 문제점들이 있다.

  - Binary data를 전송하기 위한 native support가 없다.
  - Internet Explorer는 지원하지 않는다.
  - EventSource 객체에 header를 추가할 수 없다.
  - 단일 브라우저에서 수립 가능한 최대 connection 수는 6개이다.

- 위 문제점들 중 마지막 문제에 대해 더 살펴보자.

### Domain Sharding

- 기본적으로 웹 브라우저들은 각 domain에 대해 수립할 수 있는 active connection의 개수에 제한을 둔다.  
  그리고 만약 이 제한을 넘어설 경우, 다운로드할 리소스들이 queuing 되기에 사용자들은 느린 페이지 로딩 속도를 경험한다.

- 이를 해결하기 위해 개발자들은 여러 개의 subdomain으로 리소스를 분산시킨다. 이는 브라우저가 각 domain 마다의  
  connection limit을 가진다는 점을 활용해 늘린 subdomain의 개수만큼 더 많은 connection을 수립할 수 있게 된다.

- 하지만 이렇게 여러 개의 subdomain으로 분리하는 것이 항상 좋은 것은 아니다. 웹 브라우저 입장에서는 domain을  
  보면, 각 domain들에 대해 DNS lookup을 수행하고, connection을 수립하고 유지해야하기 때문에 오히려  
  성능 저하가 발생할 수 있다.

### HTTP/2 Multiplexing

- HTTP/2에는 위에서 봤듯이 하나의 connection이 여러 개의 stream으로 구성되고, stream마다의 우선순위를  
  지정할 수 있다. 이를 통해 하나의 connectioin 내에서 요청과 응답의 과정이 병렬적으로 수행될 수 있으며,  
  HoL Blocking 문제를 해결한다. 이를 multiplexing이라고 한다.

- Multiplexing 덕분에 클라이언트는 하나의 connection 내에서 병렬적으로 여러 개의 stream을 만들고,  
  비동기적으로 요청, 응답을 주고받을 수 있게 된다.

- SSE는 HTTP/2에서 stream을 통해 데이터를 전송하게 된다. 따라서 HTTP/1.1에서의 HoL Blocking 문제도  
  발생하지 않고, domain sharding을 통해 connection을 여러 개 수립하는 것도 필요하지 않다.

---

## SSE Considerations

- SSE가 만능 해결책이 되진 않는다. 아래의 문제점들을 고민해봐야 한다.

### TCP HoL Blocking

- 사실 HTTP/2를 사용한다고 해서 HoL Blocking 문제로부터 완전히 자유로워지는 것은 아니다.  
  대표적으로 packet이 유실되는 경우를 들 수 있다.

- 만약 stream에서 packet이 유실되면, 뒤이어 해당 stream을 통해 오는 packet들은 유실된 packet이 재전송되고 클라이언트가  
  수신할 때까지 blocking된다. 이는 HTTP의 문제가 아니라, TCP의 서비스에서 오는 문제인데, TCP는 packet loss, 그리고 packet의  
  순서를 보장하는 프로토콜이기 때문이다.

- 비슷하게 packet이 유실되면 해당 connection에 소속된 다른 stream들 또한 함께 blocking된다.

### SSE event data type

- 현재 SSE는 이벤트에 담을 수 있는 데이터가 newline 2개로 끝나는 text로 한정되어 있다.

---

- [참고 문서 1](https://ordina-jworks.github.io/event-driven/2021/04/23/SSE-with-HTTP2.html)
- [참고 문서 2](https://medium.com/blogging-greymatter-io/server-sent-events-http-2-and-envoy-6927c70368bb)
- [참고 문서 3](https://www.mnot.net/blog/2022/02/20/websockets)
