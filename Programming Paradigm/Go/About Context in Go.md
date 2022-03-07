# About context in Go

> 💡 최근 Go를 사용해 gRPC Server를 만들던 도중, context를 사용하지 않았다는 피드백을 받아서 context란 무엇인지, 왜 필요한지에 대해 정리해 보았습니다.

[Context란](https://www.notion.so/About-context-in-Go-4ca1180e65df4da8a2cec8cb0339e3bb)

[Context 인터페이스](https://www.notion.so/About-context-in-Go-4ca1180e65df4da8a2cec8cb0339e3bb)

[Context가 필요한 이유](https://www.notion.so/About-context-in-Go-4ca1180e65df4da8a2cec8cb0339e3bb)

## Context란

[Go의 공식 문서](https://pkg.go.dev/context#pkg-overview)를 직역하면, Go에서 말하는 context란 아래와 같습니다.

> Context type은 deadline, cancellation signals, 그리고 그 외의 request에 한정된 범위의 값들을 API 경계, 프로세스 사이에서 가지며, `context` 패키지에 들어 있다.

- Context (맥락) 라는 단어의 표현 그대로, Go에서 이야기하는 context는 **프로세스, API 간에 전달되는 맥락**이라고 이해했습니다.

- 따라서 하나의 시스템적인 동작을 수행하기 위해 일련의 함수 호출을 해야 하는 경우, 이 맥락을 유지하기 위해 context를 같이 넘기게 됩니다.  
  예를 들어, 특정 HTTP Request를 처리하는 handler의 경우에는 아래처럼 context를 가져올 수 있습니다.

```go
func handleRequest(w http.ResponseWriter, req *http.Request) {
	context := req.Context()
	// 요청 처리
}
```

- 동일한 Stub을 공유해 클라이언트에서 컴파일된 proto 파일에 정의된 함수를 호출하면 자동으로 서버 측에서 구현한 함수로 요청이  
  전달되는 gRPC 의 경우에도, 컴파일된 함수는 아래처럼 `context.Context` 를 파라미터로 가집니다.

```go
func (UnimplementedServer) handleRequest(context.Context, *someRequest) (*PsomeResponse, error) {
		return nil, status.Errorf(codes.Unimplemented, "method handleRequest not implemented")
}
```

컴파일된 gRPC 함수의 시그니처에 자동으로 `context.Context` 가 파라미터로 들어있는 이유는 **해당 요청을 시작하는 부분**이기 때문입니다. 비즈니스 로직으로 일련의 과정을 처리할 때, 이 context를 전달하면 다양한 상황을 처리할 수 있습니다. 자세한 내용은 아래에 있어요!

---

## Context 인터페이스

우선 context 패키지 내의 Context 인터페이스는 아래와 같이 4개의 메소드를 가집니다.

```go
package context

type Context interface {
	Deadline() (deadline time.Time, ok bool)
	Done() <-chan struct{}
	Err() error
	Value(key interface{}) interface{}
}
```

위의 함수들이 각각 수행하는 행동을 하나씩 파악해보겠습니다.

### `Deadline() (deadline time.Time, ok bool)`

- `Deadline()` 함수는 이 context가 **작업을 끝내야 할 최대 시간을 반환**합니다.
  일상에서도 _“과제에 deadline이 있다”_ 라고 표현하는 것과 동일한 의미를 가지죠.

- 하나의 예시 상황을 볼게요. 클라이언트에서 아래처럼 요청 시작 직후부터 3초의 deadline을 지정했어요.

```go
func callSomeRequest(req *pb.SomeRequest) {
	ctx, cancel := context.WithDeadline(context.Background(), time.Now().Add(time.Second * 3))
	defer cancel()
	response, err := client.someRequest(ctx, req)
	if err != nil {
		fmt.Println(err)
	}
}
```

- 그리고 서버에서는 작업이 5초가 소요된다고 해볼게요.

```go
func (s *someServer) someRequest(ctx context.Context, req *pb.SomeRequest) (*pb.SomeResponse, error) {
	time.Sleep(time.Second * 10)
	return ...
}
```

- 위 상태로 클라이언트가 요청을 하면, 아래처럼 `deadline exceeded` error가 발생합니다.  
  클라이언트에서 정한 작업의 최대 소요 시간 이상을 서버에서 사용했기 때문이죠.

```go
rpc error: code = DeadlineExceeded desc = context deadline exceeded
```

### `Done() <-chan struct{}`

- `Done()` 은 해당 context의 작업이 취소되어야 하는 경우에 닫힌 **channel을 반환**합니다.
  단, 만약 절대 취소될 수 없는 context에 대해 호출된 경우에는 `nil` 을 반환할 수도 있습니다.

- 여기서 절대 취소될 수 없는 context란, 아래 메소드들 중 하나를 통해 생성된 context를 말합니다.

  - `context.Background()`
  - `context.TODO()`

- 특정 context 상에서 수행되는 함수들은 `Done()` 이 반환하는 channel을 cancellation signal로 받아 작업을 중단하도록 해야 합니다.
- 하나의 간단한 예시를 보자면, 이번에는 클라이언트에서 보낸 요청에 대해 서버가 처리하는 도중, 클라이언트가 요청을 취소하는 예시를 볼게요.
- 아래는 서버측 코드 입니다.

```go
func (s *someServer) someRequest(ctx context.Context, req *pb.SomeRequest) (*pb.SomeResponse, error) {
	time.Sleep(5 * time.Second)
	fmt.Println("5초 걸리는 작업 완료")
}
```

- 위 코드는 실행되면 요청을 받은 순간으로부터 5초 후에 _“5초 걸리는 작업 완료”_ 가 출력됩니다.  
  문제는 **클라이언트가 도중 요청을 취소하더라도(ex. 요청 1초 후) 서버는 작업을 그대로 한다**는 것입니다.

- 이번에는 `context.Done()` 을 사용해 코드를 바꿔보겠습니다.

```go
func (s *someServer) someRequest(ctx context.Context, req *pb.SomeRequest) (*pb.SomeResponse, error) {
	select {
	case <-time.After(time.Second * 5):
		fmt.Println("5초 걸리는 작업 완료")
	case <-ctx.Done():
		fmt.Println("요청 취소")
	}
}
```

- 이 경우, 클라이언트가 5초가 되기 전에 요청을 취소하면 바로 _“요청 취소”_ 가 출력되며, 서버는 해당 요청을 처리하기 위한 작업을 종료합니다.

### `Err() error`

- `Err()` 는 `Done()` 이 반환한 channel이 close 되지 않았다면 `nil` 을, close 되었다면 왜 close 되었는지에 대한 이유를 담은 `error` 를 반환합니다. 바로 위에서 본 코드의 case 문을 아래처럼 수정해보죠.

```go
func (s *someServer) someRequest(ctx context.Context, req *pb.SomeRequest) (*pb.SomeResponse, error) {
	select {
	case <-time.After(time.Second * 5):
		fmt.Println("5초 걸리는 작업 완료")
	case <-ctx.Done():
		fmt.Println(ctx.Err())
	}
}
```

- 이 상태로 위애서 본 예시 상황처럼 5초가 되기 전에 클라이언트가 요청을 취소하면, _“context canceled”_ 라고 콘솔에 출력됩니다.
- 참고로 context가 cancel되면 `Canceled` 를, deadline을 초과하면 `DeadlineExceeded` 를 반환합니다.

```go
var Canceled = errors.New("context canceled")
var DeadlineExceeded = deadlineExceededError{}

type deadlineExceededError struct{}

func (deadlineExceededError) Error() string { return "context deadline exceeded" }
func (deadlineExceededError) Timeout() bool { return true }
func (deadlineExceededError) Temporary() bool { return true }
```

### `Value(key interface{}) interface{}`

- `Value()` 는 해당 context에 있는 값(value)를 특정 key로 가져오는 함수 입니다.  
  `context.WithValue()` 로 값을 설정할 수 있는데요. 이 함수들은 주로 **동작을 조절하는 것이 아닌, 알리는 데에 사용**됩니다.
- 예를 들어, logger를 생각해본다면 request ID, 요청자의 IP 주소 등 logging에 필요한 정보들을 context에 넣어 전체 flow를 기록할 수 있습니다.
- 단, 만약 context 내의 value에 따라 프로그램(함수)의 동작이 달라진다면, 해당 API를 문서화하고, 사용자들이 사용하기가 매우 어려워 집니다.
- 사실 `context.Value()` 의 목적을 logging(tracing) 정도로만 예상하고 있었는데, [이 블로그](https://medium.com/@cep21/how-to-correctly-use-context-context-in-go-1-7-8f2c0fafdf39)에서는 강력하게  
  프로그램의 동작을 조절하는 목적으로 `context.Value()` 를 사용하지 말라고 합니다. 또한 API 개발자만 사용할 것을 권장합니다.

  - 저 또한 이 의견에 공감하는데요. context에 값을 넣어 넘기고, 그 값을 사용하면 어떤 함수가 어떤 함수를 호출하는지를 항상 파악해야 하고,  
    함수 시그니처만 보고서 어떤 값을 context에 넣어서 호출해야 하는지 등 동작을 예측할 수 없게끔 할 것 같기 때문입니다.

---

## Context가 필요한 이유

- Context가 가장 유용하게 쓰이는 부분은 위에서 본 `Done()` 에 대한 예시에서 확인할 수 있습니다.  
  예를 들어 아래의 세 가지 단계로 요청을 처리하는 서버가 있다고 생각해볼게요.

  1. Request 수신
  2. 데이터 가공
  3. DB 작업
  4. Response 전송

- 만약 context가 없다면 클라이언트에서 request를 전송 하자마자 취소하더라도, 2~4번 과정이 수행되게 됩니다.  
  이를 좀 더 일반화하면 **클라이언트에서 모종의 이유로 request를 cancel하게 되더라도, 서버에서는 해당 request를 처리하기 위해 필요한 모든 작업을 하게 됩니다.**

- 이때, _cancel 가능한 context_ 가 있다면 클라이언트에서 request를 cancel하면 서버도 해당 request를 처리하기 위한 작업을 즉각 중단할 수 있습니다.

- 이러한 context의 특성을 이용하면 goroutine을 활용해 병렬적으로 수행되는 작업들이 있더라도, 그 중 하나의 goroutine이 cancel되면  
  나머지 goroutine들도 모두 cancel할 수 있습니다. 예를 들어 goroutine에 의해 한 layer에서 다른 layer로 context를 전달하면,  
  상위 layer에서 작업을 종료하면 하위 layer들의 작업도 함께 중단할 수 있습니다.

- _cancel 가능한 context_ 의 구현된 부분을 살짝 보겠습니다.

### `context.WithCancel()`

```go
type CancelFunc func()

func WithCancel(parent Context) (ctx Context, cancel CancelFunc) {
	if parent == nil {
		panic("cannot create context from nil parent")
	}
    c := newCancelCtx(parent)
    propagateCancel(parent, &c)
    return &c, func() { c.cancel(true, Canceled) }
}
```

- `WithCancel()` 은 파라미터에서도 알 수 있듯이 부모 context를 받는데요.  
  이 부모 context를 복사하고, 복사한 부모 context에 새로운 Done channel을 넣어 반환합니다.  
  반환된 context의 Done channel은 아래의 2가지 중 하나의 상황이 발생했을 때 close 됩니다.

  - 반환된 cancel(`CancelFunc` 타입) 함수가 호출된 경우
  - 부모 context의 Done channel이 close된 경우

- 이 함수의 구현 내용을 살짝 살펴보겠습니다.  
  우선 처음 있는 if문은 parent context가 `nil`인지를 검사합니다.  
  따라서 처음 `WithCancel()` 으로 cancel 가능한 context를 만들려면, `context.Background()` 를 넘겨야 합니다.

- 다음으로 `newCancelCtx()` 는 아래처럼 구현되어 있습니다.

```go
func newCancelCtx(parent Context) cancelCtx {
	return cancelCtx{Context: parent}
}

type cancelCtx struct {
	Context

	mu       sync.Mutex
	done     atomic.Value
	children map[canceler]struct{}
	err      error
}
```

- cancelCtx는 canceler 인터페이스를 구현하는데요.  
  canceler 인터페이스와 cancelCtx가 이를 구현하는 과정은 아래와 같습니다.

```go
type canceler interface {
	cancel(removeFromParent bool, err error)
	Done() <-chan struct{}
}

func (c *cancelCtx) cancel(removeFromParent bool, err error) {
	if err == nil {
		panic("context: internal error: missing cancel error")
	}
	c.mu.Lock()

	// c.err가 nil이 아니라는 것은 이미 cancel 되었다는 것이므로 return.
	if c.err != nil {
		c.mu.Unlock()
		return
	}

	// 전달된 Canceled error를 저장
	c.err = err

	// done을 가져오고
	d, _ := c.done.Load().(chan struct{})

	// nil 이라면 channel을 저장
	if d == nil {
		c.done.Store(make(chan struct{})
	// nil이 아니라면 기존에 channel이 있으므로 close
	} else {
		close(d)
	}

	// 연결된 자식 context(sub-context)들을 순회하며 cancel 호출
	for child := range c.children {
		child.cancel(false, err)
	}

	// 모든 sub-context를 cancel한 후 nil 처리
	// 즉, c(자신, sub-context들의 부모)와 sub-context의 관계를 끊는다.
	c.children = nil
	c.mu.Unlock()

	// 본인과 부모 context의 관계도 끊는다.
	if removeFromParent {
		removeChild(c.Context, c)
	}
}
```

## `context.WithDeadline()`

```go
func WithDeadline(parent Context, d time.Time) (Context, CancelFunc)
```

- `WithDeadline()` 도 `WithCancel()` 과 마찬가지로 cancel 가능한 context를 반환하지만, deadline(두 번째 인자 d) 도 함께 받습니다.  
  이 함수가 반환하는 context는 아래의 두 가지 상황 중 하나의 상황이 발생했을 때 cancel 됩니다.
  - deadline이 지난 경우
  - 반환된 cancel(`CancelFunc` 타입) 함수가 호출된 경우

---

## `context.WithTimeout()`

```go
func WithTimeout(parent Context, timeout time.Duration) (Context, CancelFunc) {
		return WithDeadline(parent, time.Now().Add(timeout))
}
```

- `WithDeadline()` 이 `time.Time` 타입으로 context의 deadline을 지정해줬다면, `WithTimeout()` 은 `time.Duration` 을  
  전달해주면 **해당 함수가 호출되는 시점 + timeout 을 deadline으로 갖는 context**를 반환해준다.

---

# 참고 자료

- [https://devjin-blog.com/golang-context/](https://devjin-blog.com/golang-context/)
- [https://go.dev/blog/context](https://go.dev/blog/context)
