# Go By Example

- [Go By Example](https://gobyexample.com/) 정리본

## Hello, World!

```go
package main

import "fmt"

func main() {
    fmt.Println("Hello, World!")
}
```

- `main.go` 파일이 있을 때, 아래 명령어를 통해 바로 실행할 수 있다.

```
go run main.go
```

- 바이너리 파일로 build하고, 실행하는 과정은 아래와 같다.

```
go build main.go
ls # main, main.go

./main # Hello, World!
```

<hr/>

## Values

- Go는 문자열, 정수형, 실수형, boolean 등 많은 값 타입을 가진다.

```go
package main

import "fmt"

func main() {
    fmt.Println("go" + "lang") // "golang"
    fmt.Println("1 + 2 = ", 1+2) // "1 + 2 = 3"
    fmt.Println("7.0 / 3.0 = ", 7.0/3.0) // "7.0 / 3.0 = 2.3333333333333335"
    fmt.Println(true && false) // "false"
}
```

---

## Variables

- Go에서는 변수를 명시적으로 선언해야 하고, 이런 정보들은 컴파일러가 사용한다.  
  (타입 체크, 함수 호출 등)

- 1개 이상의 변수는 `var` 키워드를 사용해 선언할 수 있다.

- 아래 처럼 여러 변수들을 한 번에 선언할 수도 있다.

```go
func main() {
    var n1, n2 int = 1, 2
    var n3, n4 = 3, 4
    fmt.Println(n1, n2) // "1 2"
    fmt.Println(n3, n4) // "3 4"
}
```

- n3, n4의 경우 Go는 변수가 초기화된 값으로 타입을 결정한다.

- 아래처럼 값이 할당되지 않고 선언만 된 변수들은 초기값의 _zero-valued_ 로 지정된다.  
  예를 들어 int형이라면 0, 문자열이라면 아무런 값도 없는 빈 문자열 `""`이 된다.

```go
func main() {
    var n1 int
    fmt.Println(n1) // "0"
    var s1 string
    fmt.Println(s1) // ""
}
```

- `:=` 표현식은 값을 선언하고 초기화하는 것을 의미한다.

```go
func main() {
    n1 := 1
    s1 := "string"
    fmt.Println(n1, s1) // "1 string"
}
```

---

## Constants

- Go는 문자, 문자열, boolean, 숫자형 값들에 대해 상수를 지원한다.  
  상수는 `const` 키워드를 사용해 선언한다.

```go
func main() {
    const s string = "constant"
    fmt.Println(s)

    const n = 50
    const d = 3e20 / n
    fmt.Println(d)

    fmt.Println(int64(d))
    fmt.Println(math.Sin(n))
}
```

- `const` 키워드는 `var` 키워드가 쓰이는 곳 어디서든 쓰일 수 있다.

---
