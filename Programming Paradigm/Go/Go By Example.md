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

<details><summary>Slices</summary>

<p>

- `Slice`는 Go의 주요 데이터 타입 중 하나로, 배열보다 연속적인 작업에 대해 더 많은 기능을 제공한다.

- 배열과 다르게 `Slice`는 크기가 선언된 원소 개수가 아닌, 가진 원소의 실제 크기로 결정된다.  
  0이 아닌 길이의 `Slice`를 만들기 위해선 내장 함수인 `make()`를 사용하면 된다.  
  아래 예시에서는 3개의 문자열을 가진 `Slice`를 만들었다.  
  배열과 마찬가지로 `arr[index]` 형식으로 값을 가져오거나 설정할 수 있다.

```go
func main() {
	s := make([]string, 3)
	fmt.Println("emp:", s) // emp: [  ]
	s[0] = "a"; s[1] = "b"; s[2] = "c"
	fmt.Println("set:", s) // set: [a b c]
	fmt.Println("get:", s[2]) // get: c

}
```

- 배열에는 없는 기능으로, 내장 함수인 `append()`를 사용해 `Slice`에 값을 추가할 수 있다.

```go
func main() {
	s := make([]string, 1)
	s[0] = "a"
	fmt.Println(s) // [a]
	s = append(s, "b")
	s = append(s, "c", "d", "e")
	fmt.Println(s) // [a b c d e]
}
```

- `Slice`는 `copy()`를 사용해 복사할 수 있다.

```go
func main() {
	original := make([]string, 3)
	original[0] = "a";
	original[1] = "b";
	original[2] = "c"
	fmt.Println(original) // [a b c]

	copied := make([]string, len(original))
	copy(copied, original)
	fmt.Println(copied) // [a b c]

	copied[0] = "x";
	copied[1] = "y";
	copied[2] = "z"
	fmt.Println(original) // [a b c]
	fmt.Println(copied) // [x y z]
}
```

- `Slice`는 slice 연산자도 제공하는데, `slice[low:high]` 형식으로 사용할 수 있다.  
  예를 들어, 아래 코드는 `s[2], s[3], s[4]`를 담는 `Slice`를 반환한다.

```go
func main() {
	s := make([]string, 6)
	s[0] = "a"
	s[1] = "b"
	s[2] = "c"
	s[3] = "d"
	s[4] = "e"
	s[5] = "f"
	slice := s[2:5]
	fmt.Println(slice) // [c d e]
	slice2 := s[:3]
	fmt.Println(slice2) // [a b c]
	slice3 := s[1:]
	fmt.Println(slice3) // [b c d e f]
}
```

- 배열과 마찬가지로 `Slice`도 다차원 데이터를 담을 수 있다.  
  다만 길이가 정해진 배열과 달리, 원소의 개수에 따라 길이가 달라지는 `Slice`의 특성 상  
  내부 데이터(`Slice`)의 길이는 달라질 수 있다.

```go
func main() {
	twoDimensionalSlice := make([][]int, 3)
	for i := 0; i < 3; i++ {
		innerLen := i + 1
		twoDimensionalSlice[i] = make([]int, innerLen)
		for j := 0; j < innerLen; j++ {
			twoDimensionalSlice[i][j] = i + j
		}
	}
	fmt.Println(twoDimensionalSlice) // [[0] [1 2] [2 3 4]]
}
```

---

</p></details>

<details><summary>Maps</summary>

<p>

- `Map`은 다른 언어들에서 _hashes_, _dict_ 라고 불리는 자료형과 비슷한 데이터 타입이다.

- 빈 `Map`을 만들기 위해서는 아래처럼 `make(map[key-type] value-type)` 구문을 사용한다.  
  값을 설정하기 위해서는 `name[key] = val` 형식을 사용하면 된다.  
  값을 가져올 때도 `name[key]` 형식을 사용한다.

```go
func main() {

	m := make(map[string]int)
	m["key1"] = 1
	m["key2"] = 2
	fmt.Println(m) // map[key1:1 key2:2]
}
```

- `len()` 내장함수를 `Map`에 대해 사용하면, key-value 쌍의 개수를 반환한다.

- `delete()` 내장함수를 사용하면 `Map`의 key-value pair를 제거한다.

```go
func main() {

	m := make(map[string]int)
	m["key1"] = 1
	m["key2"] = 2
	delete(m, "key1")
	fmt.Println(m) // map[key2:2]
}
```

- `name[key]` 형식으로 `Map`에서 value를 가져올 때는 해당 key가 존재하는지를  
  알려주는 2번째 반환값도 있다.

```go
func main() {

	m := make(map[string]int)
	m["key1"] = 1
	m["key2"] = 2
	value1, isPresent1 := m["key1"]
	fmt.Println(value1) // 1
	fmt.Println(isPresent1) // true

	value3, isPresent3 := m["key3"]
	fmt.Println(value3) // 0
	fmt.Println(isPresent3) // false
}
```

- 마지막으로 아래처럼 `Map`을 선언함과 동시에 key-value pair를 지정해 초기화할 수 있다.

```go
func main() {

	m := map[string]int{"key1": 1, "key2": 2}
	fmt.Println(m) // map[key1:1 key2:2]
}
```

---

</p></details>

<details><summary>Range</summary>

- `range`는 여러 종류의 자료구조를 순회(iterate)할 때 사용한다.

- 아래는 `range`를 사용해 `Slice`에 있는 숫자들을 합치는 예시이다.

```go
func main() {
	nums := []int{1, 2, 3}
	sum := 0
	for _, num := range nums {
		sum += num
	}
	println(sum) // 6
}
```

- 배열, `Slice`에 대한 `range`는 index, value를 함께 반환하며 순회한다.  
  위 예시에서는 index의 자리에 `_`를 사용했다.

> 관례적으로 사용하지 않는 변수가 있다면, `_`로 네이밍한다.

- `Map`에 대한 `range`는 key, value pair를 순회한다.

```go
func main() {
	m := map[string]int{"key1": 1, "key2": 2}
	for key, value := range m {
		fmt.Println(key, " -> ", value)
	}
	// key1 -> 1
	// key2 -> 2

	// key만 순회
	for k := range m {
		print(k, " ") // key1 key2
	}
}
```

- 마지막으로 문자열에 대한 `range`는 각 문자의 unicode를 순회한다.

```go
func main() {
	str := "abcdefg"
	for index, value := range str {
		fmt.Println(index, value)
	}
	/*
		0 97
		1 98
		2 99
		3 100
		4 101
		5 102
		6 103
	*/
}
```

---

<p>

</p></details>

<details><summary>Functions</summary>

<p>

- 함수는 `func` 키워드를 사용해 선언한다.

- 우선 아래는 2개의 int를 매개변수로 하고 int를 반환하는 함수 `plus()`이다.  
  매개변수마다 타입을 지정해줘도 되고, 같은 타입이 여러 개 있다면 한 번만 써줘도 된다.

```go
func plus(a int, b int) int {
	return a + b
}

func plusAll(a, b, c int) int {
	return a + b + c
}

func plusDifferentTypes(a, b int, c, d string) string {
	println(a + b)
	return c + d
}

func main() {
	fmt.Println(plusDifferentTypes(1, 2, "c", "d"))
}
```

---

</p></details>

<details><summary>Multiple Return Values</summary>

<p>

- Go에서는 하나의 함수, 표현식이 여러 개의 반환 값을 가질 수 있다.  
  예를 들어, 결과와 error를 모두 한 번에 반환하도록 할 수 있다.

- 아래의 `vals()` 함수는 2개의 int를 반환한다.

```go
func vals() (int, int) {
	return 3, 7
}

func main() {
	fmt.Println(vals())
}
```

- 2개 이상의 값이 반환될 때 하나만 사용하고 싶다면, 사용하지 않을 반환값에는 관례적으로 `_`로 네이밍한다.

```go
func main() {
	_, seven := vals()
	println(seven) // 7
}
```

---

</p></details>

<details><summary>Variadic Functions</summary>

<p>

- Go에서의 가변인자를 선언하는 방법은 Java와 동일하게 `...` 을 사용한다.

```go
func sum(nums ...int) int {
	result := 0
	for _, num := range nums {
		result += num
	}
	return result
}

func main() {
	fmt.Println(sum(1, 2, 3, 4))
}
```

- 가변인자를 매개변수로 받는 함수에 배열이나 `Slice`를 전달하려면 `...` 연산자를 붙힌다.

```go
func main() {
	numbers := []int{1, 2, 3, 4, 5}
	fmt.Println(sum(numbers...))

	slices := make([]int, 6)
	for i := 0; i < 6; i++ {
		slices[i] = i
	}
	fmt.Println(sum(slices...))
}
```

---

</p></details>

<details><summary>Closures</summary>

<p>

- Go는 익명 함수를 지원하며, 이를 사용해 `Closure`를 활용할 수 있다.  
  익명 함수는 이름 그대로 특정 작업을 하지만, 이름을 붙이고 싶지 않을 때 활용할 수 있다.  
  아래의 `intSeq()` 함수를 보자.

```go
func intSeq() func() int {
	i := 0
	return func() int {
		i++
		return i
	}
}

func main() {
	nextInt := intSeq()
	fmt.Println(nextInt()) // 1
	fmt.Println(nextInt()) // 2
	fmt.Println(nextInt()) // 3

	newInt := intSeq()
	fmt.Println(newInt()) // 1
	fmt.Println(newInt()) // 2
}
```

- 우선 `nextInt`는 `intSeq()`의 반환 결과, 즉 i가 0인 함수를 갖고 있다.  
  이후 `nextInt`를 호출할 때마다 i를 1씩 증가시키고 반환하는 익명 함수가 실행되기에  
  값이 1, 2, 3으로 출력되는 것이다.

- 반면 `newInt`는 또 다시 `intSeq()`의 반환 결과, 즉 i가 0인 함수를 갖고 있기에 1부터 다시 시작한다.

---

</p></details>

<details><summary>Recursion</summary>

<p>

- 당연히 Go에서도 재귀 함수를 사용할 수 있다. 아래의 `fact()`를 보자.  
  이 함수는 `fact(0)`가 호출될 때까지 계속 재귀적으로 호출된다.

```go
func fact(n int) int {
	if n == 0 {
		return 1
	}
	return n * fact(n-1)
}

func main() {
	fmt.Println(fact(4)) // 24
}
```

- 재귀 함수로 Closure 개념을 사용할 수도 있다.  
  하지만 재귀 함수의 목적으로 Closure를 사용할 때는 항상 `var`로 명시적으로 선언되어야 한다.  
  아래 코드에서 `fib()`를 명시적으로 선언하지 않으면, 함수를 선언함과 동시에 반환하기에 말이 안된다.  
  실제로도 `fib is undefined` 에러가 난다.

```go
func main() {
	var fib func(n int) int

	fib = func(n int) int {
		if n < 2 {
			return n
		}
		return fib(n-1) + fib(n-2)
	}

	fmt.Println(fib(7))
}
```

---

</p></details>

<details><summary>Pointers</summary>

<p>

- Go는 포인터를 지원한다.

- 아래의 `zeroval()`과 `zeroptr()` 함수를 통해 포인터와 값의 차이를 보자.  
  `zeroval()`은 int형 _값_ 을 받아 그 값을 0으로 바꾸기만 한다.  
  반면 `zeroptr()`는 `*int` 형 파라미터를 갖고 있으며, 이는 곧 int형 포인터를 의미한다.  
  이 함수 내의 `*iptr`는 `int*`형을 한 차원 내려 그 포인터 주소가 가리키는 값을 가져온다.  
  또한 `zeroptr()`에 전달할 때 `&i`는 i 변수의 포인터 주소를 전달하는 구문이다.

```go
func zeroval(ival int) {
	ival = 0
}

func zeroptr(iptr *int) {
	*iptr = 0
}

func main() {
	i := 1
	fmt.Println("Initial: ", i) // Initial:  1

	zeroval(1)
	fmt.Println("zeroval(): ", i) // zeroval():  1

	zeroptr(&i)
	fmt.Println("zeroptr(): ", i) // zeroptr():  0

	fmt.Println("pointer: ", &i) // pointer:  0x140000160c8
}
```

---

</p></details>

<details><summary>Structs</summary>

<p>

- Go에서는 `struct` 키워드를 사용해 특정 타입들로 이뤄진 구조체를 만들어 사용할 수 있다.

```go
type person struct {
	name string
	age  int
}

func newPerson(name string) *person {
	p := person{name: name, age: 25}
	return &p
}

func newPerson2(name string) person {
	p := person{name: name}
	p.age = 30
	return p
}

func main() {
	sangwoo := newPerson("sangwoo")
	fmt.Println(*sangwoo) // {sangwoo 25}
	sangwoo.name = "new Sangwoo"
	fmt.Println(*sangwoo) // {new Sangwoo 25}

	sangwoo2 := newPerson2("sangwoo2")
	fmt.Println(sangwoo2) // {sangwoo2 30}
	sangwoo2.name = "new Sangwoo2"
	fmt.Println(sangwoo2)
}
```

- 위처럼 관례적으로 구조체를 생성할 때는 해당 역할을 하는 함수를 만들어 호출한다.

- 구조체의 필드 접근 시에는 `.`를 사용할 수 있으며, 구조체 포인터의 필드를 접근할 때도 마찬가지로 `.`를  
  사용해 접근한다. (자동으로 dereference 된다.)

- 구조체를 만들 때는 `구조체명{필드명: 값}` 식으로 초기화를 할 수 있으며, 초기화하지 않으면 기본값이 할당된다.

- 구조체는 _가변(mutable)_ 이다.

---

</p></details>

<details><summary>Methods</summary>

<p>

- Go에서는 구조체에 대해 메소드를 정의할 수 있다.  
  아래의 `area()`는 rect 구조체에 대해 정의한 메소드이다.  
  `perim()`과 `area()`에서 알 수 있듯이 구조체에 대한 메소드는 구조체 포인터에 대해서도 할 수 있으며,  
  구조체 값에 대해서도 할 수 있다.

```go
type rect struct {
	width, height int
}

func (r *rect) area() int {
	return r.width * r.height
}

func (r rect) perim() int {
	return 2*r.width + 2*r.height
}

func main() {
	r := rect{width: 10, height: 5}
	fmt.Println("area: ", r.area()) // area: 50
	fmt.Println("perim: ", r.perim()) // perim: 30

	rp := &r
	fmt.Println("area: ", rp.area()) // area: 50
	fmt.Println("perim: ", rp.perim()) // perim: 30
}
```

- r는 구조체 값, rp는 구조체 포인터를 담고 있지만 둘 다 메소드를 같은 형태로 호출했다.  
  이는 Go가 메소드 호출에 대해 값과 포인터를 자동으로 변환해주기 때문이다.

---

</p></details>

<details><summary>Interfaces</summary>

<p>

- Go에서의 인터페이스는 메소드 시그니처의 집합체 이다.

- 아래는 `area()`, `perim()`을 가지는 geometry라는 인터페이스가 정의된 모습이다.

```go
type geometry interface {
	area() float64
	perim() float64
}
```

- 아래처럼 Go에서 인터페이스를 구현하기 위해서는 해당 인터페이스가 가진 메소드를 모두 구현하기만 하면 된다.  
  rect, circle 구조체 모두 geometry 인터페이스가 가지는 `area()`, `perim()`을 구현했기에  
  자동으로 geometry의 구현체로 취급되며, 그렇기에 `measure()`의 인자로 전달될 수 있는 것이다.

```go
type rect struct {
	width, height float64
}

func (r rect) area() float64 {
	return r.width * r.height
}

func (r rect) perim() float64 {
	return 2*r.width + 2*r.height
}

type circle struct {
	radius float64
}

func (c circle) area() float64 {
	return math.Pi * c.radius * c.radius
}

func (c circle) perim() float64 {
	return 2 * math.Pi * c.radius
}

func measure(g geometry) {
	fmt.Println(g)
	fmt.Println(g.area())
	fmt.Println(g.perim())
}

func main() {
	r := rect{width: 3, height: 4}
	c := circle{radius: 5}

	measure(r)
	measure(c)
}
```

---

</p></details>

<details><summary>Embedding</summary>

<p>

- Go에서는 구조체(struct)와 인터페이스(interface)들의 embedding을 지원한다.  
  이를 사용해 수많은 조합을 만들어 사용할 수 있다.

```go
type base struct {
	num int
}

func (b base) describe() string {
	return fmt.Sprintf("base with num=%v", b.num)
}

type container struct {
	base
	str string
}
```

- 위 코드에서 container 구조체는 base 구조체를 갖고 있다.  
  이렇게 embedding은 마치 이름 없는 필드와 같이 보인다.

```go
func main() {

	co := container{
		base: base{num: 1},
		str:  "Some name",
	}

	fmt.Println(co.describe()) // base with num=1
	fmt.Println(co.base.describe()) // base with num=1
	fmt.Println(co.base.num) // 1
	fmt.Println(co.num) // 1

	type describer interface {
		describe() string
	}

	var d describer = co
	fmt.Println(d.describe()) // base with num=1
}
```

- 재밌는 것은 co의 num에 접근하려면 `co.base.num`이 맞지만, `co.num`으로도 접근이 가능하다는 것이다.  
  마찬가지로 `describe()`는 base에 있는 메소드이지만 `co.describe()`도 가능하다.

- 또한 describer라는 인터페이스를 만들었는데, 이 인터페이스의 메소드 시그니처를 동일하게 base 구조체가  
  구현한다. 이때 container가 base를 embed하고 있기에 container가 describer를 구현하는 것으로 취급된다.

---

</p></details>

<details><summary>Errors</summary>

<p>

- Java와 같이 정상적인 결과를 반환하거나, 예외를 던지는 패러다임과는 달리 Go에서는 예외를 던지지 않고,  
  결과와 함께 반환한다.

- 이러한 Go의 패러다임은 예외를 특별하게 처리하지 않고, 정상적인 결과를 처리할 때와 동일하게 코드를 작성하개 해준다.

- 관례적으로 에러들은 `error` 타입을 가지며, 함수의 마지막 반환 값이다.

- `errors.New`는 주어진 에러 메시지를 가진 새로운 `error`를 만들어낸다.

```go
func f1(arg int) (int, error) {
	if arg == 42 {
		return -1, errors.New("cannot work with 42.")
	}
	return arg + 3, nil
}
```

- 위 `f1()` 함수는 인자로 42가 주어지면 에러를 반환한다.
- 에러 반환값이 nil이라면 에러가 발생하지 않았음을 의미한다.

- 아래처럼 `Error()` 메소드를 구현한 구조체를 만들어서 Custom error를 사용할 수 있다.

```go
type argError struct {
	arg     int
	problem string
}

func (e *argError) Error() string {
	return fmt.Sprintf("%d - %s", e.arg, e.problem)
}

func f2(arg int) (int, error) {
	if arg == 42 {
		return -1, &argError{arg, "Cannot work with 42."}
	}
	return arg + 3, nil
}
```

- 아래 코드를 보자. `range`를 사용해 7~42를 순회하며 각각 `f1()`과 `f2()`를 호출한다.  
  아래 코드에서 쓰인 if절의 모습은 Go에서는 매우 흔한 모습이다.

```go
func main() {
	for _, i := range []int{7, 42} {
		if r, e := f1(i); e != nil {
			fmt.Println("f1() failed:", e)
		} else {
			fmt.Println("f1() worked:", r)
		}
	}
	for _, i := range []int{7, 42} {
		if r, e := f2(i); e != nil {
			fmt.Println("f2() failed:", e)
		} else {
			fmt.Println("f2() worked:", r)
		}
	}
}
```

- 마지막으로 Custom error를 만들어 발생시키고, 사용할 때에는 형변환을 명시적으로 해줘야 한다.

```go
func main() {
	_, e := f2(42)
	if ae, ok := e.(*argError); ok {
		fmt.Println(ae.arg) // 42
		fmt.Println(ae.problem) // Cannot work with 42.
	}
}
```

- `e.(*argError)`는 형변환을 하는 모습이며 ae는 형변환된 결과, ok는 형변환이 가능한지를 담는 boolean 변수이다.

---

</p></details>
