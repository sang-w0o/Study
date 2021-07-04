# Bearer Schema

- 프로젝트를 진행할 때 Bearer Schema를 사용하여 인증을 진행했었는데, 이 인증 방식은  
  Authorization Header에 `Bearer {TOKEN}` 형식으로 token을 전달하는 것이다.  
  그런데 막상 왜 앞에 `Bearer`를 사용하는지를 몰라서, 알기 위해 정리해 보았다.

<hr/>

<h2>소개</h2>

- OAuth는 클라이언트가 `access token`을 통해 클라이언트가 보호된 리소스에 접근할 수 있도록 해준다.  
  이는 `RFC7649`에 아래와 같이 명시되어 있다.

> Access Token은 클라이언트의 인증 정보를 직접적으로 가져오는 대신,  
>  클라이언트에게 부여된 권한을 나타내는 문자열이다.

- Token은 인증 서버(Authorization Server)가 리소스에 접근할 권한이 있다고  
  판단한 클라이언트에게 생성해서 부여해준다. 클라이언트는 이 access token을 이용해서  
  리소스 서버(Resource Server)의 보호된 리소스에 접근한다.  
  이 특징을 이용해서 OAuth Access Token이 Bearer Token일 때 보호된 리소스들에 대해  
  요청을 보낼 수 있다.

- Bearer Token은 HTTP1.1의 TLS(Transport Layer Security) `RFC5246`을 이용해서  
  보호된 리소스에 접근한다. 따라서 이 인증 방식을 사용할 때 서버는 TLS를 필수적으로 구현해야 한다.

<h2>용어 정의</h2>

- Bearer Token

  - 보안 토큰(Security Token)으로, 이 토큰을 가진 사람(소유자, "bearer")이 해당 토큰을  
    이용하여 해당 사람이 권한을 가진 리소스에 접근할 수 있게 해준다. Bearer Token을 사용하는 것은  
    해당 토큰의 소유자가 암호화된 key를 가지고 있지 않아도 되게 한다.

  > A security token with the property that any party in possession of  
  > the token (a "bearer") can use the token in any way that any other  
  > party in possession of it can. Using a bearer token does not  
  > require a bearer to prove possession of cryptographic key material  
  > (proof-of-possession).

- 참고 문서: <a href="https://datatracker.ietf.org/doc/html/rfc6750">링크</a>
