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

<h2>Classes</h2>

* JS의 Class 개념은 ES6에서 등장했다.

```js
// Defining Class
class Human {
    constructor(name, lastName) {
        this.name = name;
        this.lastName = lastName;
    }
}

// Using class
const sangwoo = new Human("sangwoo", "Ra");
console.log(sangwoo);  // Human {name: "sangwoo", lastName: "Ra"}
console.log(sangwoo.name);  // sangwoo
```

* Class는 다른 언어와 마찬가지로 확장 가능하다(상속).
```js
class Baby extends Human {
    cry() {
        console.log("WAAA");
    }
    sayName() {
        console.log(`My name is ${this.name}`)
    }
}

const baby = new Baby("Babyname", "Babylastname");
console.log(baby);  // Baby {name: "Babyname", lastName: "Babylastname"}
console.log(baby.cry());  // WAAA
console.log(baby.sayName());  // My name is Babyname.
```
<hr/>

<h2>Array map</h2>

* 배열에 대한 기본적인 것들
```js
const days = ["Mon", "Tue", "Wed"];
```

* 만약 이 배열의 모든 원소에 숫자를 추가하고 싶다면 아래와 같이 해보자.
```js
const numberDays = days.map(item => item + 1);
console.log(numberDays);  // ["Mon1", "Tue1", "Wed1"]
```

* `Array.map()` 메소드는 각 원소에 특정 작업을 수행하고, 작업이 수행된 원소들로 이루어진 새로운 배열을 반환한다.

* `map()` 메소드의 첫 번째 인자는 각 item이고, 두 번째 인자는 index 값이다.   
  인자를 하나만 전달할 수도 있다.
```js
const numberDays = days.map((item, index) => item = item + index);
console.log(numberDays);  // ["Mon0", "Tue1", "Wed2"]

// 위와 동일한 기능을 ` `로 구성해보자.
const numberDays = days.map((item, index) => `${item}${index}`);
```
<hr/>

<h2>Array filter</h2>

* `Array.filter()` 메소드는 지정한 조건을 만족하는 원소들로 이루어진 새로운 배열을 반환한다.

```js
const numbers = [1, 2, 3, 4, 5, 6];
const numbersFiltered = numbers.filter(number => number % 2 === 0);
console.log(numbersFiltered);  // [2, 4, 6]
```
<hr/>

<h2>Array forEach, push, etc</h2>

* `Array.forEach()`는 배열의 각 원소들에 대해 작업을 실행한다. (__새로운 배열 반환 X, 애초에 return할 것이 없다.__)
```js
let posts = ["Hi", "Hello", "Bye"];
posts.forEach(post => console.log(post));  // Hi Hello Bye
```

* `Array.push()`는 배열에 새로운 원소를 추가할 때 사용한다.
```js
let posts = ["hi"];
posts.push("hello");
console.log(posts);  // ["hi", "hello"]
```

* `Array.includes()`는 배열에 특정 원소가 있는지를 확인한다.
```js
let greetings = ["Hi", "Hello"];
if(greetings.includes("Hello")) {
    greetings.push("bye");
}
console.log(greetings);  // ["Hi", "Hello", "bye"]
```

* 그 외에도 다양한 `Array`의 메소드들이 있다.
<hr/>