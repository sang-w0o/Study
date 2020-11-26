<h1>Fundamentals for ReactJS Development</h1>

<h2>Arrow Functions</h2>

* Arrow Function : 함수를 만드는 새로운 방법

```js
// Arrow Function 도입 이전
function helloName(name) {
    return "Hello " + name;
}

const sangwooo = helloName("sangwoo");

console.log(sangwoo);  // Hello sangwoo


// Arrow Function 도입 후
const helloName = (name) => "Hello " + name;

console.log(sangwoo);  // Hello sangwoo
```

* `{ }`가 없다면 return이라는 예약어를 명시적으로 작성하지 않아도 된다.

* 함수의 인자에는 기본값을 지정할 수 있다.  

```js
// Arrow Function 도입 이전
function helloName(name = "Sangwoo") {
    return "Hello " + name;
}

console.log(helloName());  // Hello Sangwoo

// Arrow Function 도입 후
const helloName = (name = "Sangwoo") => "Hello " + name;
```

* Arrow Function은 익명 함수를 만드는데에도 적합하다.   
  `<button>` 요소가 하나 있다고 가정해보자.

```js
const button = document.querySelector("button");

const handleClick = (event) => console.log(event);

button.addEventListener("click", handleClick);

// 익명 함수로 넣기(Arrow Function 도입 전)

button.addEventListener("click", function(event) {
    console.log(event);
});

// 익명 함수로 넣기(Arrow Function 도입 후)
button.addEventListener("click", event => console.log(event));
```

* Arrow function 사용 시 인자가 하나라면 `( )` 처리를 할 필요가 없다.

<hr/>

<h2>Template Literals</h2>

```js
const helloName = (name = "Human") => "Hello " + name;
```

* 위 코드는 반환값을 `+` 기호로 이어줘야 한다. 대신 아래와 같이 할 수 있다.
```js
const helloName = (name = "Human") => `Hello ${name}`;
```

<hr/>

<h2>Object Destructuring</h2>

