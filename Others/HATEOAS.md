# HATEOAS

- HATEOAS는 Leonard Richardson이 제시한 REST 성숙도 모델의 3단계에서 다뤄진다.

> 레벨 3: 서비스를 HATEOAS(Hypertext As The Engine Of Application State) 원칙에 기반하여  
> 설계한다. HATEOAS는 GET 요청으로 반환된 리소스 표현형에 그 리소스에 대한 액션의 링크도 함께 태워  
> 보내자는 생각이다. 가령 클라이언트는 GET 요청으로 주문 데이터를 조회하고 이때 반환된 표현형 내부 링크를  
> 이용해서 해당 주문을 취소할 수도 있다. 이처럼 HATEOAS를 사용하면 하드 코딩한 URL을 클라이언트  
> 코드에 욱여넣지 않아도 된다.

<h2>HATEOAS</h2>

- **HATEOAS(Hypertext As The Engine Of Application State)** 는 REST Application 아키텍쳐의  
  제약 조건 중 하나로, 다른 네트워크 애플리케이션 아키텍쳐와 REST를 구별해준다.

- HATEOAS를 사용하면 클라이언트는 정보를 hypermedia를 통해 동적으로 전달해주는 애플리케이션 서버와  
  소통하게 된다. REST 클라이언트는 hypermedia의 사용법 이상으로 애플리케이션과 소통하는 방법에 대해  
  더 자세히 알거나 사전 지식이 필요 없어진다.

- 반대로 COBRA(Commmon Object Request Broker Architecture)를 통해 소통하는 서버와 클라이언트는  
  고정된 인터페이스외 문서화된 IDL(Interface Description Language)를 기반으로 상호작용한다.

<h3>Example</h3>

- 클라이언트가 간단한 URL(`/account/{accountId}`)에 HTTP 기반의 REST API 요청을 보낸다고 해보자.  
  이때 클라이언트가 추가적으로 호출할 수 있는 요청들에 대한 정보가 응답에 함께 전달된다.  
  이때 _추가적으로 호출할 수 있는 요청들에 대한 정보_ 들은 표준화 방식을 따른다.  
  클라이언트는 `links` 필드를 사용하여 애플리케이션과 추가적인 상호작용을 할지 선택할 수 있다.  
  이렇게 RESTful한 상호작용이 hypermedia를 기반으로 실행된다.

- 아래 예시를 보자. 이 GET 요청은 계정에 대한 조회를 한다.

```
GET /accounts/12345 HTTP/1.1
Host: bank.example.com
...
```

- 응답은 아래와 같다.

```
HTTP/1.1 200 OK

{
  "account": {
    "account_number": 12345,
    "balance": {
	"currency": "usd",
	"value": 100.00
    },
    "links": {
	"deposits": "/accounts/12345/deposits",
	"withrdrawals": "/accounts/12345/withdrawals",
	"transfers": "/accounts/12345/transfers",
	"close-requests": "/accounts/12345/close-requests"
    }
  }
}
```

- 위 응답을 보면 links라는 정보가 있는데, 이들은 GET `/account/{accountNumber}`를 호출한 후  
  클라이언트가 관련된 리소스에 대해 추가적인 액션을 할 수 있는 URL에 대한 정보를 담고 있다.  
  위 예시에서는 출금, 입금, 이체, 계정 닫기 API에 대한 url이 주어졌다.

- 이후에 이 계좌가 초과 인출되었다 해보자. 이때는 위와는 다른 links 들이 올 것이다.  
  요청하는 API는 위와 동일하다.

```
HTTP/1.1 200 OK

{
  "account": {
    "account_num": 12345,
    "balance": {
	"currency": "usd",
	"value": -25.00
    },
    "links": {
      "deposits": "/accounts/12345/deposits"
    }
  }
}
```

- 첫 번째 요청(일반 계좌 정보 조회)과는 달리 두 번째 요청(초과 인출된 계좌 조회)의 links 에는  
  입금에 대한 링크 하나만 주어졌다. 현재 _상태(state)_ 에서 다른 링크들은 사용 불가하다.  
  **이렇게 상태(state)를 hypermedia를 통해 보여주므로 HATEOAS의 정의에 부합하는 것이다.**

<hr/>
