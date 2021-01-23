<h1>TypeScript란?</h1>

* `TypeScript`는 `JavaScript`를 상속받는 언어이다.

<h2>TypeScript가 없을 때의 단점</h2>

* 아래의 JS로 작성된 함수를 보자.
```js
const plus = (a, b) => a + b;
```

* 간단히 위 함수는 두 인자를 더한 값을 반환한다고 이해할 수 있다.   
  사용자가 아래와 같이 함수를 호출하는 것을 예상했을 것이다.
```js
let result = plus(1, 3);
// 4
```

* 하지만 JS는 타입이 없는(?) 언어이기에 아래와 같은 경우에도 작동한다.
```js
let result = plus('abc', 3);
// abc3
```

* 이렇게 JS는 개발자가 의도한 대로 함수 또는 변수들이 동작하지 않을 수 있다.
<hr/>

<h2>TS가 위의 코드를 해결해주는 방법</h2>

* TS는 타입을 지정함으로써 JS 보다 훨씬 타입에 대해 안전하게 코드를 작성하게 해준다.   
  위의 코드를 TS로 작성해보자.
```ts
const plus = (a:number, b:number):number => a + b;
let result = plus(1, 3); // 4
let result2 = plus('abc', 3);
```

* 위 경우, result2는 코드가 작성됨과 동시에 컴파일 에러를 일으킨다.   
  즉, JS에 비해 절대 타입으로 인한 의도치 않은 결과가 도출되는 것을 원천적으로 막을 수 있다.
<hr/>

<h2>TS 기초</h2>

* 당연하게 함수 뿐만 아니라 변수에도 타입을 지정할 수 있다.
```ts
let variable:string = 'abc';

variable = 'def';  // OK

variable = 3;  // Compile Error
```

* 함수의 타입은 아래와 같이 지정해준다.
```ts
const print = (name:string, age:number):string => {
    return `My name is ${name}(${age})`;
}
```
<hr/>

<h2>TypeScript Interface</h2>

* TS의 Interface란 JSONObject에 대한 정의를 하는 부분이다.
```js
const person = {
    name: 'Sangwoo',
    age: 24,
    isStudent: true
}

const hello = (person) => {
    console.log(`I am ${person.name}, ${person.name} years old.`);
    if(person.isStudent) {
        console.log('I am a student.');
    }
}
```

* 위 예시를 TS의 인터페이스로 작성해보자.   
  만약 위 코드를 그대로 TS에서 사용하면 `hello`가 인자로 받는   
  person의 타입을 모르기 때문에 에러가 난다.
```ts
const person = {
    name: 'Sangwoo',
    age: 24,
    isStudent: true
}

interface IPerson {
    name: string,
    age: number,
    isStudent: boolean
}

const hello = (person: IPerson) => {
    console.log(`I am ${person.name}, ${person.name} years old.`);
    if(person.isStudent) {
        console.log('I am a student.');
    }
}
```

* 이제 컴파일러는 `hello()`에 들어오는 person이 `IPerson` 인터페이스의 멤버 변수들을 가지고,   
  각 변수들의 타입에 대해 검사할 수 있게 되었다.

* 만약 `IPerson`의 age 변수가 필수 값이 아니라면, 아래와 같이 지정해주면 된다.
```ts
interface IPerson {
    name: string,
    age?: number,
    isStudent: boolean
}
```

* 위 코드에서는 타입 정의 시 `?`를 앞에 붙여줬는데, 이는 age가 필수 값이 아님을 표현한다.   
  만약 이 값이 없다면 age는 `undefined`가 된다.
<hr/>