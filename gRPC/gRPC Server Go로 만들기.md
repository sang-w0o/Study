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

## Server 구축하기

- 이제 위에서 proto file에 정의한 `PersonService` service를 구현해보자.  
  이를 위해서는 아래 2개의 작업을 해야 한다.

  - Service 정의에 의해 생성된 인터페이스를 구현해야 한다.  
    즉 서비스가 실제로 할 일을 정의하는 것이다.

  - gRPC 서버를 실행해 클라이언트로부터 request를 받도록 하고, 올바른 서비스 구현체로 요청이 보내지도록 해야 한다.

### Implement `PersonService` service

- 이전에 proto file을 컴파일하며 생긴 파일 중 `person_grpc.pb.go`를 보면 아래처럼 `PersonServiceServer` 인터페이스가 만들어져 있다.

```go
type PersonServiceServer interface {
	// 사용자 정보 저장 요청, Unary RPC
	GetPersonInformation(context.Context, *PersonRequest) (*PersonResponse, error)
	// email이 모든 사용자 정보 요청, Server Streaming RPC
	ListPersons(*ListPersonRequest, PersonService_ListPersonsServer) error
	// 다수의 사용자 저장 요청, Client Streaming RPC
	SavePersons(PersonService_SavePersonsServer) error
	// 사용자 정보 실시간 요청, Bidirectional Streaming RPC
	AskAndGetPersons(PersonService_AskAndGetPersonsServer) error
	mustEmbedUnimplementedPersonServiceServer()
}
```

- 이제 위 인터페이스를 구현해야 한다.  
  그러기 위해 먼저 `PersonServiceServer` 인터페이스를 구현하는 `personServiceServer` 구조체를 만들어보자.  
  만들기 위해서는 컴파일된 proto 파일을 읽어야 하는데, 우선 아래처럼 `go.mod` 파일의 module을 수정해주자.  
  이렇게 수정한 이유는 코드가 있는 저장소의 전체 주소가 `https://github.com/Example-Collection/go-grpc-server`이기 때문이다.

```mod
module github.com/Example-Collection/go-grpc-server
```

- 다음으로 터미널에 아래 명령어를 입력해 코드를 가져온다.

```sh
go get github.com/Example-Collection/go-grpc-server/proto
```

- 그러면 `go.mod` 파일이 수정된 모습을 확인할 수 있다.

### Unary RPC

- 가장 먼저 서비스에 정의된 Unary RPC 메소드인 `GetPersonInformation`을 구현해보자.  
  Proto file을 컴파일하며 생긴 `PersonServiceServer` 말고, 코드 내에서 사용할 `personServiceServer` 구조체를 만들어보자.  
  이 구조체는 컴파일된 Proto 파일에 정의된 인터페이스를 구현하는 역할을 한다.

- 실제로 `person_grpc.pb.go`를 보면, `PersonServiceServer`는 하나의 구조체이며, 구현되지 않은 메소드들로 되어 있다.

```go
// UnimplementedPersonServiceServer must be embedded to have forward compatible implementations.
type UnimplementedPersonServiceServer struct {
}

func (UnimplementedPersonServiceServer) GetPersonInformation(context.Context, *PersonRequest) (*PersonResponse, error) {
	return nil, status.Errorf(codes.Unimplemented, "method GetPersonInformation not implemented")
}
func (UnimplementedPersonServiceServer) ListPersons(*ListPersonRequest, PersonService_ListPersonsServer) error {
	return status.Errorf(codes.Unimplemented, "method ListPersons not implemented")
}
func (UnimplementedPersonServiceServer) SavePersons(PersonService_SavePersonsServer) error {
	return status.Errorf(codes.Unimplemented, "method SavePersons not implemented")
}
func (UnimplementedPersonServiceServer) AskAndGetPersons(PersonService_AskAndGetPersonsServer) error {
	return status.Errorf(codes.Unimplemented, "method AskAndGetPersons not implemented")
}
```

- 이제 구조체를 만들어보자.

```go
type personServiceServer struct {
	pb.UnimplementedPersonServiceServer
	savedPersons []*pb.PersonRequest
}
```

- 위 구조체에서 `pb.UnimplementedPersonServiceServer`는 gRPC가 요청을 알맞은 메소드로 전달해주기 위해서 꼭 embed되어 있어야 한다.  
  savedPersons는 사용자들이 저장되는 DB를 흉내낸 것이다.

- 이제 `GetPersonInformation` 메소드를 구현해야 한다. 여기서는 `PersonRequest`를 받아 savedPersons에 저장하고, 저장된 사용자에  
  대한 `PersonResponse`를 반환하는 간단한 로직을 구현해보자.

```go
func personRequestToPersonResponse(req *pb.PersonRequest) *pb.PersonResponse {
	return &pb.PersonResponse{
		Email:   req.Email,
		Age:     req.Age,
		Name:    req.Name,
		Message: "Successfully saved!(name:" + req.Name + ")",
	}
}

func (s *personServiceServer) GetPersonInformation(_ context.Context, req *pb.PersonRequest) (*pb.PersonResponse, error) {
	log.Printf("PersonRequest(name: %v, age: %d, email: %v, password: %v) arrived.\n", req.Name, req.Age, req.Email, req.Password)
	s.savedPersons = append(s.savedPersons, req)
	return personRequestToPersonResponse(req), nil
}
```

- 마지막으로 gRPC Server를 실행시키는 메소드를 작성해보자.

```go
func newServer() *personServiceServer {
	return &personServiceServer{savedPersons: []*pb.PersonRequest{}}
}

func main() {
	// Listen at localhost:8081 using TCP
	lis, err := net.Listen("tcp", "localhost:8081")
	if err != nil {
		log.Fatal("Failed to listen on port 8081")
	}

	// Get gRPC Server informations such as credentials, Keep-Alive parameters.. and more
	var opts []grpc.ServerOption

	// Create a new gRPC server
	grpcServer := grpc.NewServer(opts...)

	// Register the personServiceServer to the gRPC server.
	pb.RegisterPersonServiceServer(grpcServer, newServer())
	_ = grpcServer.Serve(lis)
}
```

- `net.Listen()`은 TCP Protocol로 `localhost:8081`에서 `LISTEN`을 하겠다는 의미이다.  
  `grpc.ServerOption`은 gRPC Server에 대한 Credentials, Keep-Alive 파라미터 등의 정보가 포함되어 있다.  
  이 정보들을 가지고 `grpc.NewServer()`를 호출해 새로운 gRPC Server를 시작한다. 이때, 이 상태만으로 이 gRPC Server는  
  service가 등록되지 않고, 요청을 받지 않는다. 따라서 `pb.RegisterPersonServiceServer()`를 통해 만든 gRPC Server에  
  `personServiceServer`, 즉 서비스 메소드가 정의된 인터페이스를 구현한 구조체를 등록해야 한다.  
  마지막으로 `grpc.Serve()`를 호출해 gRPC Server가 지정된 포트(8081)에서 지정된 네트워크 프로토콜(TCP)로 실행되도록 한다.

- 이제 클라이언트에서 `localhost:8081`에 gRPC Connection을 수립하고 `GetPersonInformation()`을 호출하면,  
  서버에는 아래와 같이 콘솔에 메시지가 출력된다.

```
2022/01/06 14:07:20 PersonRequest(name: Sangwoo, age: 25, email: robbyra@gmail.com, password: sangwooPassword) arrived.
```

### Server Streaming RPC

- 이제 Server Streaming RPC를 구현해보자. service에 정의된 Server Streaming RPC 메소드는 `ListPersons()`이며,  
  이메일을 받아 해당 이메일과 일치하는 사용자 정보를 반환해준다.

- 우선 protoc로 컴파일한 이후의 시그니처는 아래와 같다.

```go
// 모든 사용자 정보 요청, Server Streaming RPC
ListPersons(*ListPersonRequest, PersonService_ListPersonsServer) error
```

- 바로 구현해보자. Email이 일치하는 사용자들을 찾아 1초 간격으로 Stream에 message를 전송하도록 해보았다.

```go
func (s *personServiceServer) ListPersons(req *pb.ListPersonRequest, stream pb.PersonService_ListPersonsServer) error {
	log.Printf("ListPersonRequest(email: %v) arrived.", req.Email)
	for _, person := range s.savedPersons {
		if person.Email == req.Email {
			time.Sleep(time.Second) // Optional
			if err := stream.Send(personRequestToPersonResponse(person)); err != nil {
				return err
			}
		}
	}
	return nil
}
```

- 코드에서 바로 알 수 있듯이 stream에 message를 보내려면 stream에 대해 `Send()`를 호출하면 된다.  
  클라이언트로 요청이 오면, 콘솔에 아래와 같이 출력된다.

```
2022/01/06 15:19:06 ListPersonRequest(email: robbyra@gmail.com) arrived.
```

- 참고로 이메일이 일치하는 사용자들을 설정해주기 위해 아래처럼 `newServer()`를 수정해 초기값을 지정해주었다.

<details><summary>초기값 지정 코드 보기</summary>

<p>

```go
func newServer() *personServiceServer {
	savedPersons := []*pb.PersonRequest{
		{
			Email:    "robbyra@gmail.com",
			Age:      25,
			Name:     "sangwooAged25",
			Password: "sangwooPassword",
		},
		{
			Email:    "robbyra@gmail.com",
			Age:      26,
			Name:     "sangwooAged26",
			Password: "sangwooPassword",
		},
		{
			Email:    "robbyra@gmail.com",
			Age:      27,
			Name:     "sangwooAged27",
			Password: "sangwooPassword",
		},
		{
			Email:    "notSangwoo@gmail.com",
			Age:      1,
			Name:     "notSangwoo",
			Password: "notSangwooPassword",
		},
	}
	return &personServiceServer{savedPersons: savedPersons}
}
```

</p></details>

### Client Streaming RPC

- 이번에는 클라이언트에서 일련의 message들을 stream에 담아 요청하는 Client Streaming RPC를 구현해보자.  
  Server Streaming RPC와 반대로, 이번에는 서버 측에서 stream에서 message를 뽑아 하나씩 처리해야 한다.

```go
func (s *personServiceServer) SavePersons(stream pb.PersonService_SavePersonsServer) error {
	for {
		// stream에서 request 추출 (에러 시 error가 nil이 아니다.)
		req, err := stream.Recv()

		// error가 io.EOF라는 뜻은 stream의 모든 message를 다 읽어 더 이상 message가 없다는 뜻이다.
		// 따라서 response를 보내준다.
		if err == io.EOF {
			log.Println("Read all messages in client stream.\nClosing stream after sending response.")
			return stream.SendAndClose(&pb.BasicResponse{Message: "All requests saved!"})
		}
		if err != nil {
			log.Fatalf("%v.SavePersons(_) = _, %v", s, err)
			return err
		}

		// message를 잘 읽은 경우
		s.savedPersons = append(s.savedPersons, req)
		log.Printf("Saved Person(name: %v, email: %v, age: %d).\n", req.Name, req.Email, req.Age)
	}
	return nil
}
```

- 이제 서버를 실행하면 클라이언트에서 1초 간격으로 stream에 `PersonRequest` message를 보내면 아래처럼 콘솔에  
  1초 간격으로 출력된다.

```
2022/01/09 15:06:51 Saved Person(name: name1, email: email1@test.com, age: 1).
2022/01/09 15:06:52 Saved Person(name: name2, email: email2@test.com, age: 2).
2022/01/09 15:06:53 Saved Person(name: name3, email: email3@test.com, age: 3).
2022/01/09 15:06:53 Read all messages in client stream.
Closing stream after sending response.
```

---

### Bidirectional Streaming RPC

- Bidirectional Streaming RPC에서는 서버, 클라이언트 모두 서로 독립적으로 message를 send, receive할 수 있는  
  stream을 사용한다.

- 사용자 정보 저장 요청(`PersonRequest`)가 오면 저장 후 저장된 사용자의 정보를 반환하는 `PersonResponse`를 반환하도록 해보자.

```go
func (s *personServiceServer) AskAndGetPersons(stream pb.PersonService_AskAndGetPersonsServer) error {
	for {

		// client의 stream에서 message를 읽어들인다.
		req, err := stream.Recv()

		// 더 이상 message가 없다면 종료한다.
		if err == io.EOF {
			log.Println("Read all messages from client stream. Closing server stream.")
			return nil
		}
		if err != nil {
			return err
		}

		// 읽은 message(pb.PersonRequest 타입)를 savedPersons에 저장하고,
		log.Printf("PersonRequest(email: %v, name: %v, age: %d) arrived.", req.Email, req.Name, req.Age)
		s.savedPersons = append(s.savedPersons, req)

		// 저장된 사용자의 정보를 반환한다.(pb.PersonResponse 타입)
		if err := stream.Send(personRequestToPersonResponse(req)); err != nil {
			return err
		}
	}
}
```

- 클라이언트에서 1초 간격으로 요청을 보내면, 아래처럼 콘솔에 출력되는 것을 확인할 수 있다.

```
2022/01/09 16:48:31 PersonRequest(email: email1@test.com, name: name1, age: 1) arrived.
2022/01/09 16:48:32 PersonRequest(email: email2@test.com, name: name2, age: 2) arrived.
2022/01/09 16:48:33 PersonRequest(email: email3@test.com, name: name3, age: 3) arrived.
2022/01/09 16:48:33 Read all messages from client stream. Closing server stream.
```

---
