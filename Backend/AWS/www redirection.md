<h1>www가 없는 링크를 www로 리다이렉션 시키기</h1>

<h2>기본 전제</h2>

* AWS의 CloudFront, Route53, 그리고 S3의 정적 웹사이트 호스팅을 이용하여   
  `www.example.com`의 도메인을 사용중이라고 하자.

* 기존 상황은 사용자가 브라우저에 `www.example.com`으로 접속할 때와 `example.com`으로   
  접속할 때를 대비하여 각 상황에 대한 S3 bucket이 각각 있었고, CloudFront 배포도   
  각각 있었다.

* 내가 원하는 것은 사용자가 url에 `https://example.com`을 직접 입력했을 때 자동으로   
  `https://www.example.com`으로 리다이렉션이 이루어지는 것이었다.

* 참고로 S3의 정적 웹 호스팅은 https 프로토콜을 지원하지 않기 때문에, https를 사용하려면   
  꼭 CloudFront 배포가 포함되어야 한다.
<hr/>