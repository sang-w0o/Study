JavaScript를 활용한 함수형 프로그래밍
======

<h2>함수형 프로그래밍이란</h2>

* JavaScript는 변수에 함수를 대입할 수 있고, 함수를 다른 함수의 인자로 넘길 수 있으며, 함수에서 함수를 반환할 수 있다.
* 즉, JavaScript는 함수를 일반 변수들과 마찬가지로 취급할 수 있다.

* 변수가 함수를 담는 경우
```js
var log = function(message) {
    console.log(message);
}

// 위와 같은 기능
const log = (message) => console.log(message);

```
* 함수를 객체에 넣는 경우
```js
const obj = {
    message:"함수를 객체에 넣는다.",
    log(message) {
        console.log(message);
    }
}

obj.log(message);
```
* 함수를 배열에 넣는 경우
```js
const messages = [
    "첫 번째 원소",
    message => console.log(message),
    "두 번째 원소",
    message => console.log(message)
];

messages[1](messages[0]);  // 첫 번째 원소 출력
messages[3](messages[2]);  // 두 번째 원소 출력
```
* 함수를 다른 함수의 인자에 넣는 경우
```js
const insideFn = logger => logger("인수로 들어온 함수");
insideFn(message => console.log(message));
// 인수로 들어온 함수 출력
```
* 함수가 함수를 반환하는 경우
```js
var createScream = function(logger) {
    return function(message) {
        logger(message.toUpperCase() + "!!!");
    }
}

const scream = createScream(message => console.log(message));

scream('함수가 함수를 반환');  // 함수가 함수를 반환!!! 출력
scream('function returns function');  // FUNCTION RETURNS FUNCTION!!! 출력

// 위 createScream을 화살표 함수로 만들어 보자.
const createScream = logger => message => logger(message.toUpperCase() + "!!!");
```

<hr/>

<h2>명령형 프로그래밍과 선언적 프로그래밍의 비교</h2>

* 선언적 프로그래밍(Declarative Programming)
  * 필요한 것을 달성하는 과정의 기술 보다 필요한 것이 어떤 것인지 기술하는데   
    중점을 두고, application의 구조를 세워나가는 프로그래밍 스타일
* 명령형 프로그래밍(Imperative Programming)
  * 원하는 결과를 달성해가는 과정에만 관심을 두는 프로그래밍 스타일 

* 코드 예시
```js
// 공백을 - 로 바꾸는 예시

// (1) 명령형 프로그래밍
var string = "This is midday show with Cheryl Waters";
var urlFriendly = "";

for(var i  = 0; i < string.length; i++) {
    if(string[i] == ' ') {
        urlFriendly += "-";
    }
    else {
        urlFriendly += string[i];
    }
}

console.log(urlFriendly);

// (2) 선언적 프로그래밍
const string = "This is midday show with Cheryl Waters";
const urlFriendly = string.replace(/ /g, "-");

console.log(urlFriendly);
```
* 위와 같이 선언적 프로그래밍은 코드의 추론이 더 쉽다.
* Application의 추론이 쉬우면, 그 규모를 확장하는 것도 더 쉽기 마련이다.

* DOM을 만드는 과정에서의 명령형 프로그래밍과 선언적 프로그래밍의 비교
```js
// 명령형 접근 방식
var target = document.getElementById('target');
var wrapper = document.createElement('div');
var headLine = document.createElement('h1');

wrapper.id = "welcome";
headLine.innerText = "Hello World";

wrapper.appendChild(headLine);
target.appendChild(wrapper);

// React Component를 이용해 DOM을 선언적으로 구성하는 방식
const {render} = ReactDOM;

const Welcome = () => (
    <div id="welcome">
        <h1>Hello World</h1>
    </div>
)

render(
    <Welcome />,
    document.getElementById('target');
)
```
<hr/>

<h2>함수형 프로그래밍의 개념</h2>

<hr/>

<h3>(1) 불변성(Immutability)</h3>

* Application의 불변성이 작동하는 방식
  * 원본 데이터를 변경하는 대신 복사본을 만들고, 복사본의 일부를 변경한다.
  * 즉, 원본 대신 변경한 복사본을 사용하여 필요한 작업을 진행한다.
```js
let color_lawn = {
    title : '잔디',
    color : '#00FF00',
    rating : 0
}

// 넘겨받은 color 객체의 rating을 변경하는 rateColor함수
function rateColor(color, rating) {
    color.rating = rating;
    return color;
}

console.log(rateColor(color_lawn, 5).rating);  // 5 출력
console.log(color_lawn.rating);  // 5 출력
```
* 위 코드에서는 rateColor의 인자로 넘겨준 color_lawn의 원본 rating값이 변경되었다.
* 그 이유는 JS에서 함수의 인자는 실제 데이터에 대한 참조이기 때문이다.
```js
var rateColor = function(color, rating) {
    return Object.assign({}, color, {rating:rating});
}

console.log(rateColor(color_lawn, 5).rating);  // 5 출력
console.log(color_lawn.rating);  // 0 출력
```
* 위 코드에서의 assign은 Object객체의 메소드인데, 첫 번째 인수에는 빈 객체를 지정한다.
* 그 후 두번째 인수 객체를 첫 번째의 빈 객체에 복사하고, 복사본에 있는   
  rating 필드값을 rating 파라미터의 값으로 변경한다.
* 따라서 원본은 수정되지 않게 된다.
* 위와 마찬가지로 배열에 대해서도 원본 배열을 수정하는 메소드 보다는   
  새로운 배열을 반환해주는 메소드를 사용하는 것이 불변성을 지키는데 좋다.

<hr/>

<h3>(2) 순수 함수(Pure Function)</h3>

* 순수 함수 : 매개변수에 의해서만 반환값이 결정되는 함수
* 즉, 순수 함수는 1개 이상의 인자를 받고, 인자값이 같으면 항상 같은 값 또는 함수를 반환한다.
* 순수 함수는 인자를 변경 불가능한 데이터로 취급한다.

* DOM을 변경하는 순수하지 않는 함수의 예시
```js
function Header(text) {
    let h1 = document.createElement('h1');
    h1.innerText = text;
    document.body.appendChild(h1);
}
```
* 위 Header함수는 반환값이 없으며, DOM을 변경하는 부수 효과를 발생시키기 때문에 순수 함수가 아니다.
* React에서는 UI를 순수 함수로 표현한다. Header를 순수 함수로 변경해보자.
```js
const Header = (props) => <h1>{props.title}</h1>
```
* 위 Header함수는 DOM을 수정하는 부수 효과를 발생시키지 않으며 Element를 반환한다.

* 마지막으로 순수 함수 생성의 3가지 규칙을 보자.
  * 순수 함수는 파라미터를 최소 1개 이상 받아야 한다.
  * 순수 함수는 값이나 다른 함수를 반환해야 한다.
  * 순수 함수는 인자나 함수 밖에 있는 다른 변수를 변경하거나   
    입출력을 수행해서는 안된다.

<hr/>

<h3>(3) 데이터 변환</h3>

* (1) Array.join 메소드
  * 배열의 모든 원소를 인자로 받아 구분자로 연결한 배열을 반환한다.
  * 즉, 원본 배열은 그대로 남는다.
* (2) Array.filter 메소드
  * filter메소드는 booolean값(true, false)만을 인수로 받는다. 
  * 이 메소드 또한 특정 조건에 따라 filtering된 새로운 배열을 반환다므로   
    원본 배열은 그대로 남는다.
* (3) Array.map 메소드
  * map메소드는 변환 함수를 인자로 받는다.
  * 이 메소드는 변환 함수를 배열의 모든 원소에 적용하여 반환받은 값들로   
    새로운 배열을 만들어 반환한다.
* (4) Object.keys 메소드
  * keys메소드는 어떤 객체의 키로 이루어진 배열을 반환한다.
```js
const schools = {
    "Yorktown":10,
    "Washington&Lee":2,
    "Wakefield":5
}

const schoolArray = Object.keys(schools).map(key =>
    ({
        name:key,
        wins:schools[key]
    })
)

console.log(schoolArray);
// [ {name:'Yorktown', wins:10}, 
//   {name:'Washington&Lee', wins:2},
//   {name:'Wakefield', wins:5} ]
```
* (5) Array.reduce, Array.reduceRight 메소드
```js
const ages = [21, 18, 42, 20, 64, 63, 34];

const maxAge = ages.reduce((max, age) => {
    console.log(`${age} > ${max} = ${age > max}`);
    if(age > max)
        return age;
    else
        return max;
}, 0);

console.log('maxAge', maxAge);
// 21 > 0 = true
// 18 > 21 = false
// 42 > 21 = true
// 40 > 42 = false
// 64 > 42 = true
// 63 > 64 = false
// 34 > 64 = false
// maxAge = 64
```
* 위 코드에서는 ages배열을 하나의 값으로 축약(Reduce) 했다.
* reduce메소드는 변환함수와 초기값을 인수로 받는다.
* 변환함수는 객체의 모든 원소에 한 번씩 호출된다.
* reduce메소드는 첫 번째 원소부터 마지막 원소까지 원소를 2개씩 묶는 반면,   
  reduceRight메소드는 마지막 원소부터 첫 원소까지 원소를 반대로 2개씩 묶는다.

* reduce메소드를 사용해서 배열을 객체로 변환하는 예시
```js
const colors = [
    {
        id:'extremeRed',
        title:'과격한 빨강',
        rating:3
    },
    {
        id:'bigBlue',
        title:'큰 파랑',
        rating:2
    },
    {
        id:'banana',
        title:'바나나',
        rating:1
    }
];

const hashColors = colors.reduce((hash, {id, title, rating}) => {
    hash[id] = {title, rating};
    return hash;
}, {})

console.log(hashColors);

// {'extremeRed' : {title:'과격한 빨강', rating: 3}, 
//  'bitBlue' : {title: '큰 파랑', rating: 2}, 
//  'banana' : {title:'바나나', rating:1} }
```
* 위 코드에서 reduce함수에 두 번째로 전달한 인자는 빈 객체이다.
* 위에서는 배열의 각 원소에 있는 id값을 키 값으로 사용한다.
* 이렇게 배열을 한 값으로 축약할 수도 있다.

* reduce메소드를 사용해 배열을 전혀 다른 배열로 만드는 예시
```js
const colors = ['red', 'red', 'green', 'blue', 'green'];

const distinctColors = colors.reduce((distinct, color) =>
    (distinct.indexOf(color) !== -1) ? distinct : [...distinct, color],
    []
)

console.log(distinctColors);
// ["red", "green", "blue"]
```
* 위에서는 reduce로 같은 값이 여러 개 들어있는 배열을 서로 다른 값이 1번씩만 들어가게 했다.
* 이 때 두번째로 들어간 인자는 빈 배열이다.

<hr/>

<h3>(4) 고차 함수(HOF, High Order Function)</h3>

* 고차 함수는 다른 함수를 조작할 수 있는 함수다.
* 즉, 다른 함수를 인자로 받거나 함수를 반환할 수 있다.
```js
const invokeIf = (condition, fnTrue, fnFalse) =>
    (condition) ? fnTrue() : fnFalse();

const showWelcome = () => console.log("Welcome");
const showUnauthorized = () => console.log("unauthorized");

invokeIf(true, showWelcome, showUnauthorized);  // Welcome 출력
invokeIf(false, showWelcome, showUnauthorized);  // unauthorized 출력
```
* 위와 같이 다른 함수를 반환하는 고차 함수는 비동기적인 실행 맥락을 처리할 때 유용하다.

* 커링(Currying)은 고차 함수 사용법과 관련된 함수형 프로그래밍 기법이다.
* 이는 어떤 연산을 수행할 때 필요한 값 중 일부를 저장하고 나중에 나머지값을 전달받는 기법이다.
* 이를 위해 다른 함수를 반환하는 함수를 사용하며, 이를 커링된 함수라고 한다.

<hr/>

<h3>(5) 재귀(Recursion)</h3>

* 재귀는 자기 자신을 호출하는 함수를 만드는 기법이다.
```js
const countdown = (value, fn) => {
    fn(value);
    return (value > 0) ? countdown(value -1, fn) : value;
}

countdown(10, value => console.log(value));

//10 9 8 7 6 5 4 3 2 1 0 출력
```

<hr/>

<h3>(6) 합성(Composition)</h3>

* 함수형 프로그래밍은 로직을 구체적인 작업을 담당하는 여러 작은 순수 함수로 나눈다.
* 그 과정에서 언젠가는 모든 작은 함수를 하나로 합쳐야할 필요가 있는데, 이를 합성이라 한다.
* 대표적인 합성 기법으로는 chaining 기법이 있다.
<hr/>
