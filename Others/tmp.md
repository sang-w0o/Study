# TMP

<h2>Session, Cookie의 차이점과 활용</h2>

<h3>Cookie</h3>

- `Cookie`는 최대 4KB의 작은 파일로, 웹 서버가 클라이언트의 컴퓨터에 저장하는 것이다.  
  Cookie가 클라이언트의 컴퓨터에 세팅되면, 이후 모든 요청은 Cookie의 name, value를 반환한다.  
  Cookie는 오로지 생성된 도메인에서만 활용될 수 있다. 예를 들어, `a.example.com`에서 생신 Cookie는  
  `b.example.domain`에서는 읽을 수 없다.

- 많은 웹 사이트는 광고 등의 컨텐츠를 페이지에 함께 포함시킨다.  
  이때, 이 광고를 제공하는 도메인의 Cookie 또한 클라이언트의 컴퓨터에 저장될 수 있는데,  
  이를 Third Party Cookie라 한다. Cookie는 특정 사용자에 한정되게 사용되며, 다른 사용자는  
  다른 사용자의 Cookie를 볼 수 없다.

<h3>Session</h3>

- Session은 서버에 저장되는 전역 변수이다. 각 Session에는 고유 ID가 부여되고, 이 ID를 통해  
  Session에 저장된 값들을 읽어올 수 있다. Session이 생성될 때, 생성된 Session의 고유 ID를  
  가지고 있는 cookie가 클라이언트의 컴퓨터에 저장된다. 만약 클라이언트의 브라우저가 cookie를  
  지원하지 않는다면, Session의 고유 ID는 URL에 나타난다.

- Session은 Cookie에 비해 더 큰 양의 데이터를 저장할 수 있다.

- Session에 저장되어 있는 값들은 브라우저가 닫히면 자동으로 삭제된다.  
  따라서 세션에 있는 값들을 항상 유지하고 싶다면 데이터베이스 같은 저장소를 활용해야 한다.

<h3>Session, Cookie 사용 예제</h3>

- Http는 무상태 프로토콜(Stateless Protocol)이기에 Cookie를 활용하면 클라이언트의 컴퓨터에 작은 파일을  
  저장함으로써 애플리케이션의 상태를 추적할 수 있다. Cookie가 저장되는 위치는 브라우저에 따라 다르다.  
  Cookie는 주로 UX의 최적화를 위해 사용되는데, 사용자는 자신이 선호하는 것들을 클릭하고,  
  그 정보들이 Cookie에 저장됨으로서 개인 맞춤화를 제공할 수 있다.

- Session은 사용자의 고유 ID 등의 중요한 정보를 안전하게 관리하기 위해 사용된다.  
  서버 자체에 저장되니 외부로서의 접근을 막기 용이하다.  
  또한 Session을 이용하면 한 페이지에서 다른 페이지로 정보를 전달할 수 있다.  
  Cookie를 지원하지 않는 브라우저에 대해 Cookie를 대체하기 위해 사용되는 경우도 있다.

<hr/>

<h2>DI는 왜 필요할까?</h2>

<h2>의존성 역전은 왜 필요할까?</h2>

<h2>서비스 추상화는 왜 할까?</h2>

<h2>OOP에서의 객체의 책임과 역할이란</h2>

<h2>Shell Script의 return vs 다른 언어의 return</h2>

<h2>JPA N + 1 문제 및 해결법</h2>

<h2>HTTP란 무엇일까?</h2>

<h2>단방향 vs 양방향 데이터 바인딩</h2>

<h2>HTTP Session, Cookie는 HTTP의 어디에 저장될까?</h2>

<h2>Promise가 무엇이며 비동기 처리는 어떻게 할까?</h2>

<h2>ES6의 주요 기능</h2>
