# Bearer Schema

- 프로젝트를 진행할 때 Bearer Schema를 사용하여 인증을 진행했었는데, 이 인증 방식은  
  Authorization Header에 `Bearer {TOKEN}` 형식으로 token을 전달하는 것이다.  
  그런데 막상 왜 앞에 `Bearer`를 사용하는지를 몰라서, 알기 위해 정리해 보았다.

---

## 소개

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

---

## 용어 정의

- Bearer Token

  - 보안 토큰(Security Token)으로, 이 토큰을 가진 사람(소유자, "bearer")이 해당 토큰을  
    이용하여 해당 사람이 권한을 가진 리소스에 접근할 수 있게 해준다. Bearer Token을 사용하는 것은  
    해당 토큰의 소유자가 암호화된 key를 가지고 있지 않아도 되게 한다.

  > A security token with the property that any party in possession of  
  > the token (a "bearer") can use the token in any way that any other  
  > party in possession of it can. Using a bearer token does not  
  > require a bearer to prove possession of cryptographic key material  
  > (proof-of-possession).

---

## 인증 과정 및 사용 예시

- 인증 및 인증 후 리소스에 접근하는 과정은 잘 알고 있듯이 아래 처럼 이루어진다.

![picture 1](/images/BEARER_SCHEME.png)

### 인증된 요청

- Access Token은 HTTP/1.1에서 정의된 인증 방식(`RFC2617`) 중, Authorization Header를  
  사용한다. Authorization Header는 Request Header의 Authorization 필드를 의미한다.  
  클라이언트는 `Bearer` 인증 스키마를 통해 access token을 서버에게 전달한다.  
  아래는 예시이다.

```
GET /resource HTTP/1.1
Host: server.example.com
Authorization: Bearer thisIsWhereTokenIsPut
```

- 클라이언트는 필수적으로 인증이 필요한 요청을 보낼 때, bearer token을 Authorization Request Header에  
  `Bearer`로 시작하는 HTTP 인증 방식을 통해 전달해야 한다. 그리고 인증이 필요한 요청을 받는  
  리소스 서버는 필수적으로 이 방식에 대한 처리를 할 수 있도록 구현되어야 한다.

---

- ~~HTTP 인증 표준 방식이었다. Bearer가 소지자 라는 의미를 갖는지 알게되는 계기였다.~~

- 참고 문서: [링크](https://datatracker.ietf.org/doc/html/rfc6750)
