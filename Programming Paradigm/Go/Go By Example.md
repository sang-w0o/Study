# Go By Example

- [Go By Example](https://gobyexample.com/) 정리본

<details>
<summary>Hello, World!</summary>

<p>

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

---

</p>
</details>

<details>
<summary>Values</summary>

<p>

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

</p>
</details>

<details>
<summary>Variables</summary>

<p>

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

</p>
</details>

<details>
<summary>Constants</summary>

<p>

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

</p>
</details>

<details>
<summary>For</summary>

<p>

- `for`는 Go에서 유일한 반복문이다.

```go
func main() {
	i := 1
	for i <= 3 {
		fmt.Print(i, " ")
		i = i + 1
	}

	// "1 2 3"

	for j := 7; j <= 9; j++ {
		fmt.Println(j, " ")
	}

	// "7 8 9"

	for {
		fmt.Println("LOOP")
		break
	}

	// "LOOP"

	for n := 0; n <= 5; n++ {
		if n%2 == 0 {
			continue
		}
		fmt.Print(n, " ")
	}

	// "1 3 5"
}
```

---

</p>
</details>

<details>
<summary>If, Else</summary>

<p>

- Go에서는 다른 언어와 if, else 구문이 동일하고, else문 없이 if문만 있을 수도 있다.  
  단, if else block에서의 `{ }`는 필수적으로 필요하다.

```go
func main() {
	if 7%2 == 0 {
		println("7 is even")
	} else {
		println("7 is odd")
	}

	if num := 9; num < 0 {
		println(num, "is negative")
	} else if num < 10 {
		println(num, "has 1 digit")
	} else {
		println(num, "has multiple digits")
	}
}

// "7 is odd"
// "9 has 1 digit"
```

---

</p>
</details>

<details>
<summary>Switch</summary>

<p>

- `switch`문 또한 다른 언어와 비슷하다.

```go
func main() {
	i := 2
	switch i {
	case 1:
		println("one")
	case 2:
		println("two")
	default:
		println("wrong")
	}
}
// "two"
```

- `case` 절에서는 `,`를 구분자로 해서 여러 조건을 하나의 case 절에 대해 적용할 수 있다.

```go
func main() {
	switch time.Now().Weekday() {
	case time.Saturday, time.Sunday:
		println("Weekend!")
	default:
		println("Weekday :(")
	}
}
// "Weekend!"
```

- switch 문에는 조건이 들어가지 않을 수도 있는데, 이는 if/else 문을 나타내는 또다른 방법 중 하나다.

```go
func main() {
	now := time.Now()
	switch {
	case now.Hour() < 12:
		println("Before noon.")
	default:
		println("After noon.")
	}

	// 위 switch-case는 아래의 if-else와 동일
	if now.Hour() < 12 {
		println("Before noon.")
	} else {
		println("After noon.")
	}
}
```

---

</p>
</details>

<details><summary>Arrays</summary>

<p>

- 아래 코드는 5개의 원소를 가지는 int형 배열을 선언한다. 변수 선언 시와 마찬가지로  
  선언된 배열 a는 모두 int의 기본값인 0을 5개 갖고 있다.

```go
func main() {
	var a [5]int
	fmt.Println(a)
}

// "[0 0 0 0 0]"
```

- 배열의 값은 `array[index]` 구문으로 설정할 수 있고, 가져올 때도 `array[index]` 구문을 사용한다.

```go
a[0] = 1
a[1] = 2
fmt.Println(a[0], a[1]) // "1 2"
```

- 아래 구문을 통해 배열을 선언함과 동시에 초기화할 수도 있다.

```go
func main() {
	a := [5]int{1, 2, 3, 4, 5}
	fmt.Println(a)
}

// "[1 2 3 4 5]"
```

- 내장 함수인 `len()`을 사용해 배열의 길이를 알아낼 수 있다.

```go
fmt.Println(len(a)) // "5"
```

- `Array` 타입은 1차원이지만, 이들을 조합해 n차원의 배열을 만들어낼 수 있다.

```go
func main() {
	var twoDimensionalArray [2][3]int
	for i := 0; i < 2; i++ {
		for j := 0; j < 3; j++ {
			twoDimensionalArray[i][j] = i + j
		}
	}
	fmt.Println(twoDimensionalArray)
}

// "[[0 1 2] [1 2 3]]"
```

---

</p></details>
