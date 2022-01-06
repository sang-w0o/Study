# gRPC Client Go로 만들기

- [원본 코드](https://github.com/Example-Collection/go-grpc-client)

- Proto 파일을 컴파일하고 import하는 과정은 [여기](https://github.com/sang-w0o/Study/blob/master/gRPC/gRPC%20Server%20Go%EB%A1%9C%20%EB%A7%8C%EB%93%A4%EA%B8%B0.md#grpc-server-go%EB%A1%9C-%EB%A7%8C%EB%93%A4%EA%B8%B0)에서 확인할 수 있다.

<details><summary>Proto File</summary>

<p>

```proto
syntax = "proto3";

package proto;

option go_package="github.com/Example-Collection/go-grpc-client/proto";

service PersonService {
  // 사용자 정보 저장 요청, Unary RPC
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

</p></details>
