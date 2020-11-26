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

* Structuring

```js
const human = {
    name: "Sangwoo",
    lastName: "Ra",
    nationality: "KR"
}
```

* 위 human 객체의 값에 일일히 접근하기 보다는 아래와 같이 객체 비구조화를 사용하자.
```js
// 객체 비구조화 전
const name = human.name;
const lastName = human.lastName;

// 객체 비구조화 후
const { name, lastName} = human;
console.log(name);  // Sangwoo
console.log(lastName);  // Ra
```

* 만약 특정 객체의 멤버 변수의 값을 사용하고 싶지만, 멤버 변수명은 바꾸고 싶다면 아래와 같다.
```js
// 객체 비구조화 전
const name = human.name;
const lastName = human.lastName;
const nationality1 = human.nationality;

// 객체 비구조화 후
const {name, lastName, nationality: nationality1} = human;
console.log(nationality1);  // KR
```

* 아래 객체를 보자.
```js
const human: {
    name: "Sangwoo",
    lastName: "Ra",
    nationality: "KR",
    favFood: {
        breakfast: "FOOD_A",
        lunch: "FOOD_B",
        dinner: "FOOD_C"
    }
}
```

* 위 객체에서 breakfast, lunch, dinner에 접근하려면 아래와 같이 할 수 있다.
```js
const {favFood: {breakfast, lunch, dinner}} = human;
```
<hr/>

<h2>Spread Operator</h2>

```js
const days = ["Mon", "Tue", "Wed"];
const otherDays = ["Thu", "Fri", "Sat"];
```

* Spread Operator는 배열의 원소들을 unpack 한다.
* 위 두 배열의 원소들을 합치고 싶을 때 스프레드 연산자를 활용할 수 있다.
```js
const realDays = [...days, ...otherDays, "Sun"];
console.log(realDays);  // ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"]
```

* Spread Operator는 객체에 대해서도 동일하게 동작한다.
```js
const ob1 = {
    first: "hi",
    second: "hello"
};

const ob2 = {
    third: "I am",
    fourth: "sangwoo"
};

const objectUnited = {...ob1, ...ob2};
console.log(objectUnited);  // Object {first: "hi", second: "hello", third: "I am", fourth: "sangwoo"}
```
<hr/>

