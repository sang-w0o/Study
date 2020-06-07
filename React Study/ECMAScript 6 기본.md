ECMAScript 6 기본
======

<h2>ES6의 변수 선언</h2>

* const : 상수 선언
  * const로 선언된 상수는 값을 변경할 수 없다.
* let : 지역 변수 선언
  * let으로 선언된 지역 변수는 var처럼 전역 scope를 가지지 않는다.
  ```js
  var topic = 'javaScript';

  if(topic) {
      let topic = 'React';
      console.log('let topic is : ' + topic);
      // let topic is : React 출력
  }

  console.log('var topic is : ' + topic);
  // var topic is : javaScript 출력
  ```

<hr/>

<h2>템플릿 문자열(Template String)</h2>

<hr/>

  ```js
  // 기존 JS 문자열 연결 방식
  console.log(lastName + ", " + firstName + " " + middleName);

  // Template을 사용한 문자열 연결 방식
  console.log(`${lastName}, ${firstName} ${middleName}`);
  ```

  * ${ }내에는 값을 만들어내는 JS식이라면 어떤 것이든 들어갈 수 있다.
  * 템플릿 문자열은 빈 칸 뿐만아니라 탭, 개행등을 유지한다.

<hr/>

<h2>디폴트 파라미터</h2>

<hr>

* ES6에는 c++ 등의 언어에서 지원되는 디폴트 파라미터가 명세에 추가되었다.

```js
function logActivity(name='defaultName', activity='typing'){
    console.log(`${name}은 ${activity}를 즐긴다.`);
}

logActivity();  // defaultName은 typing을 즐긴다 출력
```
* 이 때, 디폴트 파라미터에는 객체를 포함한 어떠한 타입도 올 수 있다.

<hr/>

<h2>화살표 함수(Arrow Function)</h2>

<hr/>

* 화살표 함수를 사용하면, __function__ 키워드 없이도 함수를 만들 수 있으며, __return__ 키워드 없이도 값을 반환할 수 있다.

```js
// 전통적인 함수
var returnNum = function(num) {
    return `input number is ${num}`;
}

// 화살표 함수 적용
var returnNum = num => `input number is ${num}`;
```

* 함수가 파라미터를 단 하나만 받을 때는 괄호가 생략 가능하지만,   
  두 개 이상의 파라미터를 받을 때는 괄호가 필요하다.

```js
// 전통적인 함수
var add = function(a, b){
    return `result is ${a+b}`;
}

console.log(add(1,2));

// 화살표 함수 적용
var add = (a,b) => `result is ${a+b}`;

console.log(add(1,2));
```

* 화살표 함수는 this로 새로 바인딩하지 않는다.
```js
var printWord = {
    words: ["A", "B", "C", "D", "E"];
    print : function(delay = 1000) {
        setTimeout(function(){
            console.log(this.words.join(","))
        }, delay);
    };
};

printWord.print(); 
// Cannot read property 'join' of undefined. 출력
```
* 위 오류는 this.words의 join 메소드를 호출할 때 발생한다.
* 그 이유는 this가 window객체이기 때문에 words가 undefined가 되기 때문이다.
* 위 코드 대신 화살표 함수를 이용하면 this영역이 제대로 유지된다.
```js
var printWord = {
    words : ["A", "B", "C", "D", "E"];
    print : function(delay = 1000) {
        setTimeout(() => {
            console.log(this.words.join(", "));
        }, delay);
    };
};

printWord.print();  // A, B, C, D, E 출력
```

<hr/>

<h2>ES6 트랜스파일링(Transpiling)</h2>

<hr/>

* 모든 웹 브라우저가 ES6를 제공하지는 않는다.
* 따라서 브라우저에서 ES6 코드를 실행하기 전에 ES5로 컴파일을 하면, ES6의 완벽한 동작을 보장할 수 있다.
* 이러한 변환을 __트랜스파일링__ 이라 한다.
* 트랜스파일링은 코드를 binary로 변환하는 것이 아니라, 한 버전의 JS 코드를 더 많은 브라우저가 이해할 수 있는   
  다른 버전의 JS구문으로 변환하는 것이다.
* 대표적인 트랜스파일링 도구로는 바벨(Babel)이 있다.
```js
// Transpiling 이전의 ES6 코드
const add = (x=5, y=10) => console.log(x+y);

// 트랜스파일러로 변환 후의 출력
"use strict";

var add = function add() {
    var x = arguments.length <= 0 || arguments[0] === undefined ? 5 : arguments[0];
    var y = arguments.length <= 1 || arguments[1] === undefined ? 10 : arguments[1];

    return console.log(x+y);
}
```
* 트랜스파일러는 use strict 선언을 맨 위에 추가해서 코드가 엄격한 모드에서 실행되도록 만든다.
* 위 예시 함수에서 x, y 파라미터의 디폴트값은 arguments객체로 처리된다.
* 인라인 바벨 트랜스파일러의 사용 방법
```HTML
<div id="output"></div>
<!-- 바벨 로딩 -->
<script src="https://unpkg.con/babel-standalone@6/babel.min.js"></script>
<!-- 변환할 코드를 script 태그 안에 넣기 --> 
<script type="text/babel">
const getMessage = () => "Hello, world";
document.getElementById('output').innerHTML = getMessage();
</script>
<!-- 파일에 있는 소스코드를 트랜스파일링 하기 -->
<script src="script.js" type="text/babel"></script>
```
* 위와 같이 script태그에 babel.js를 포함시키고, 변환하고 싶은 코드의 script태그에   
  type="text/babel" 속성을 지정하면 된다.

<hr/>

<h2>ES6의 객체와 배열</h2>

<hr/>

<h3>(1) 구조 분해를 사용한 대입</h3>

<hr/>

* 구조 분해를 이용하면 객체 안에 있는 필드 값을 원하는 변수에 대입할 수 있다.

```js
// sandwich 객체의 bread, meat 필드값을 가져온다.
var sandwich = {
    bread : 'dutch',
    meat : 'salmon',
    cheese : 'swiss',
    toppings : ['lettuce', 'tomato', 'mustard'];
};

var {bread, meat} = sandwich;
bread = "garlic";
meat = "turkey";

console.log(bread);  // dutch 출력
console.log(meat);  // turkey 출력

console.log(sandwich.bread, sandwich.meat);
// dutch salmon 출력
```

* 객체를 분해하여 함수의 인자로 넘길 수도 있다.
```js
var getInfo = Person => {
    console.log(`${Person.name}은 서울에 산다.`);
}

var Person = {
    name:'thisisname';
}

getInfo(Person);
```
* 위 코드에서는 객체의 필드에 접근하기 위해 (.)을 사용했지만, 이번에는 구조 분해를 통해 접근해보자.

```js
var getInfo = ({name}) => {
    console.log(`${name}은 서울에 산다.`);
}

getInfo(Person);
```
* 위 코드는 구조 분해로 name을 가져옴으로써 객체의 필드 중 name만들 사용한다는 사실을 선언한다.

* 배열을 구조 분해해서 원소의 값을 변수에 대입할 수 있다.
```js
// 첫 번째 원소를 변수에 대입.
var [alphabets] = ["A", "B", "C"];

console.log(alphabets);  // A 출력
```

* 불필요한 값을 콤마를 이용해 생략하는 __List Matching__ 을 사용해보자.
* 무시하고 싶은 원소 위치에 콤마를 넣으면, 리스트 매칭이 된다.
```js
var [,,alphabets] = ["A", "B", "C"];
console.log(alphabets); // C 출력
```

<hr/>

<h3>(2) 객체 리터럴 개선(Object Literal Enhancement)</h3>

<hr/>

* 객체 리터럴 개선은 구조 분해의 반대로, 구조를 다시 만들어내는 과정 또는 내용을 한데 묶는 과정이라 할 수 있다.

```js
// 객체 리터럴 개선으로 객체에 필드를 추가하는 예시
var name = "thisisname";
var age = 23;

var Person = {name, age};

console.log(Person);  // {name:"thisisname", age:23} 출력

// 객체 리터럴 개선으로 객체에 메소드를 추가하는 예시
var name = "thisisname";
var age = 23;
var print = function(){
    console.log(`name is ${this.name} and age is ${this.age}`);
    // 객체의 필드에 접근하기 위해 this 키워드 사용
}

var Person = {name, age, print};

Person.print();  // name is thisisname and age is 23 출력
```

* 객체 선언 문법을 예전과 ES6를 비교해보자.
```js
// 기존 방식
var skier = {
    name:name,
    sound:sound,
    powderYell : function(){
        var yell = this.sound.toUpperCase();
        console.log(`${yell} ${yell} ${yell}!!`);
    },
    speed : function(mph) {
        this.speed = mph;
        console.log('speed(mph)', mph);
    }
}

// 새로운 방식
const skier = {
    name,
    sound,
    powderYell() {
        let yell = this.sound.toUpperCase();
        console.log(`${yell} ${yell} ${yell}!!`);
    },
    speed(mph) {
        this.speed = mph;
        console.log('speed(mph)', mph);
    }
}
```
* 위와 같이 객체 리터럴 개선을 통해 현재 영역에서 볼 수 있는 변수들을 객체의 필드에 대입할 수 있으며,   
  function키워드를 사용하지 않아도 되게 되었다.

<hr/>

<h2>스프레드 연산자(Spread Operator)</h2>

<hr/>

* 스프레드 연산자는 (...)로 이루어진 연산자로, 다양한 역할을 할 수 있다.

* (1) 스프레드 연산자를 적용하여 두 배열의 모든 원소가 들어간 새로운 배열 생성
```js
var arr1 = ["A", "B", "C", "D"];
var arr2 = ["E", "F", "G"];
var arr3 = [...arr1, ...arr2];

console.log(arr3.join(","));
// A,B,C,D,E,F,G 출력
```

* (2) 배열의 원본 보호
* Array.reverse() 메소드는 배열의 원본을 수정한다. 이럴 경우, 원본을 보호하기 위해 스프레드 연산자를 사용할 수 있다.
```js
var arr1 = ["A", "B", "C", "D"];

var arr2 = [...arr1].reverse();

console.log(arr2.join(","));  // D,C,B,A 출력
console.log(arr1.join(","));  // A,B,C,D 출력
// 즉, 원본 arr1은 보호되었다.
```

* (3) 배열의 나머지 원소를 얻는 경우
```js
var arr1 = ["A", "B", "C", "D"];
var [first, ...rest] = arr1;

console.log(rest.join(",")); // B,C,D 출력
```

* (4) 함수의 인자를 배열로 모으는 경우
```js
function directions(...args) {
    var [start, ...remaining] = args;
    var [finish, ...stops] = remaining.reverse();

    console.log(`${args.length} 도시를 출발한다.`);
    console.log(`${start}에서 출발한다.`);
    console.log(`목적지는 ${finish}이다.`);
    console.log(`중간에 ${stops.length}군데를 들린다.`);
}

directions("SEOUL", "SUWON", "DAEJEON", "DAEGU", "BUSAN");
```
<hr/>

<h2>프러미스(Promise)</h2>

<hr/>

* Promise는 비동기적인 동작을 잘 다루기 위해, 더 쉽게 처리하기 위한 방법이다.
* 비동기 요청을 보내면 성공하거나 오류를 반환하는데, 요청의 성공 또는 실패에도 다양한 유형이 존재한다.
* 프로미스를 사용하면 이러한 여러 가지의 성공, 실패를 편리하게 단순한 성공이나 실패로 환원할 수 있다.

```js
// randomuser.me API로부터 데이터를 가져오는 비동기 Promise 만들기
// 이 API에는 가짜 member에 대한 주소,이름,전화번호,집주소 등의 정보가 있다.

// 새로운 promise를 반환하는 getFakeMembers 함수

const getFakeMembers = count => new Promise((resolves, rejects) => {
    const api = `https://api.randomuser.me/?nat=US&results=${count}`;
    const request = new XMLHttpRequest();
    request.open('GET', api);
    request. onload = () => 
        (request.status === 200) ?
        resolves(JSON.parse(request.response).results) :
        reject(Error(request.statusText));
    request.onerror = (err) => rejects(err);
    request.send();
})

// 가져오고 싶은 멤버수를 getFakeMembers함수의 인자로 전달해 호출
getFakeMembers(5).then(
    members => console.log(members),
    err => console.error(
        new Error("Request failed at randomuser.me"))
)
```
* then 함수는 getFakeMembers()의 호출이 성공한 경우에 수행된다.
* 이는 then함수를 promise뒤에 chaining시킨 것이다.
<hr/>

<h2>클래스(Class)</h2>

<hr/>

* 기존 JS에서의 객체 선언과 ES6에서의 객체 선언 및 상속
```js
// 기존 JS에서의 객체 선언 및 상속
function Rectangle(width, height) {
    var width = width;
    var height = height;

    this.getWidth() = function() {return width};
    this.getHeight() = function() {return height};
}

Rectangle.prototype.getArea = function(){
    return this.getWidth() * this.getHeight();
}

// Rectangle을 상속하는 Square
function Square(length) {
    this.base = Rectangle;
    this.base(length, length);
}

Square.prototype = Rectangle.prototype;
Square.prototype.constructor = Rectangle;

// ES6 에서의 객체 선언 및 상속
class Rectangle {
    constructor(width, height) {
        this.width = width;
        this.height = height;
    }
    getArea() {
        return this.width * this.height;
    }
}

// Rectangle을 상속하는 Square
class Square extends Rectangle {
    constructor(length) {
        super(length, length);
    }
}

const square = new Square(10);
console.log(square.getArea()); // 100 출력
```
<hr/>
