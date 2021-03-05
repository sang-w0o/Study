<h1>CORS에 대해서</h1>

- 이 글은 <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS">문서</a> 를 한글로 번역하며 정리한 글 입니다.

<h2>CORS란?</h2>

- `CORS(Cross-Origin Resource Sharing)`는 HTTP-header에 기반을 둔 메커니즘으로, 서버로 하여금  
  브라우저가 허용하는 다른 origin(도메인, 스키마, 포트 등)을 표시할 수 있게 해준다.  
  CORS는 브라우저가 cross-origin resource를 호스팅하는 서버에게 "preflight" request를 보내서 서버가 해당 요청을  
  수락할지, 기각할지를 결정하는 메커니즘에도 기반한다. 이 "preflight" request에서 브라우저는 실제 요청에 사용될  
  HTTP Method와 헤더 정보를 포함한 헤더를 보낸다.

- Cross-Origin Request의 예시를 들어보자.  
  JavaScript로 작성된 프론트엔드 코드가 작동하는 `https://domain-a.com`에서 `XMLHttpRequest`를 사용하여  
  `https://domain-b.com/data.json` 파일에 HTTP 요청을 보낸다고 해보자.
