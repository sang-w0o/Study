# gRPC Server Go로 만들기

- [원본 코드](https://github.com/Example-Collection/go-grpc-server)

## proto 파일 정의 및 컴파일

- 우선 아래처럼 proto file을 만들어보자. `go_package` 옵션은 Github Repository의 proto 파일이  
  있는 경로로 지정해준다.

```proto
syntax = "proto3";

package proto;

option go_package="github.com/Example-Collection/go-grpc-client/proto";

service PersonService {
  // 사용자 정보 요청, Unary RPC
  rpc GetPersonInformation(PersonRequest) returns (PersonResponse) {}

  // 모든 사용자 정보 요청, Server Streaming RPC
  rpc ListPersons(ListPersonRequest) returns (stream PersonResponse) {}

  // 다수의 사용자 저장 요청, Client Streaming RPC
  rpc savePersons(stream PersonRequest) returns (BasicResponse) {}

  // 사용자 정보 실시간 요청, Bidirectional Streaming RPC
  rpc askAndGetPersons(stream PersonRequest) returns (stream PersonResponse) {}
}

message PersonRequest {
  string name = 1;
  int64 age = 2;
  string email = 3;
  string password = 4;
}

message PersonResponse {
  string name = 1;
  int64 age = 2;
  string email = 3;
  string message = 4;
}

message ListPersonRequest {
  string email = 1;
}

message BasicResponse {
  string message = 1;
}
```

- 이후 아래의 명령을 프로젝트의 root 위치에서 입력하면, go로 컴파일된 proto file이 생성된다.

```sh
protoc --go_out=. --go_opt=paths=source_relative \
    --go-grpc_out=. --go-grpc_opt=paths=source_relative \
    proto/person.proto
```

- 2개의 go 파일이 생성되는데, 각각의 설명은 아래와 같다.

  - `person.pb.go`: proto 파일에 정의한 message들을 Go에서 만들고, 직렬화하고, 요청으로 받고, 응답으로 보낼 수 있게 한다.
  - `person_grpc.pb.go`:
    - 클라이언트가 `PersonService` service에 정의된 메소드를 호출하기 위한 인터페이스 타입(stub)
    - 서버가 `PersonService` service에 정의된 메소드를 구현하기 위한 인터페이스 타입

---
