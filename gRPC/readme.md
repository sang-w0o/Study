# gRPC

- [gRPC 공식 문서](https://grpc.io/docs/what-is-grpc/introduction/)

## Overview

- gRPC에서는 클라이언트가 서버 애플리케이션의 함수를 마치 local object인 것처럼 호출할 수 있다.  
  이로써 분포된 애플리케이션과 서비스를 쉽게 구축할 수 있다. 다른 RPC 시스템과 마찬가지로 gRPC도  
  서비스를 정의하고, 파라미터와 반환값과 함께 원격으로 호출할 수 있는 메소드를 기반으로 만들어졌다.  
  서버는 이러한 인터페이스를 구현하고 클라이언트의 요청을 처리하기 위해 gRPC 서버를 사용한다.  
  클라이언트 측에서는 stub를 사용해 서버와 동일한 메소드를 제공한다.

![picture 1](../images/GRPC_OVERVIEW.png)

- gRPC 클라이언트와 서버는 다양한 환경에서 서로 소통할 수 있다.  
  물론 gRPC가 지원하는 언어를 사용해야 한다. 예를 들어, Java로 작성된 gRPC를 Go로 작성된 클라이언트와  
  소통하게 할 수 있다.

## Wroking with Protocol Buffers

- 기본적으로 gRPC는 Protocol Buffer를 사용한다. 이는 Google에서 만든 구조화된 데이터를 직렬화하는  
  메커니즘을 가진 오픈 소스이다. 간단히 어떻게 작동하는지 보자.

- 우선 처음에는 `*.proto` 파일에 직렬화하고 싶은 데이터 구조를 정의해야 한다.  
  Protocol Buffer의 데이터는 _message_ 들로 구조화되며, 각 메시지는 name-value의 쌍을 가지는  
  _field_ 들을 가지는 논리적 단위이다.

```proto
message Person {
    string name = 1;
    int32 id = 2;
    bool has_ponycopter = 3;
}
```

- 데이터 구조를 정의한 후에는 Protocol Buffer의 컴파일러인 `protoc`를 사용해 원하는 프로그래밍 언어에서  
  해당 데이터구조를 사용할 수 있도록 컴파일해야 한다. 컴파일 후에는 각 field에 접근하기 위해 `name()`,  
  `set_name()`과 같은 메소드와 함께 전체 데이터를 raw bytes로 직렬화/역직렬화하기 위한 메소드도 제공된다.  
  예를 들어 C++을 사용한다면, 위 proto 파일을 컴파일하면 `Person`이라는 클래스가 생긴다.  
  이후에는 이 클래스를 애플리케이션에서 자유롭게 생성하고, Protocol Buffer 데이터로 직렬화하고,  
  `Person`을 담은 Protocl Buffer Message를 역직렬화하기 위해 사용할 수 있다.

```proto
// The greeter service definition.
service Greeter {
	// Sends a greeting
	rpc SayHello (HelloRequest) returns (HelloReply) {}
}

// The request message containing the user's name.
message HelloRequest {
	string name = 1;
}

// The response message containing the greetings
message HelloReply {
	string message = 1;
}
```

---
