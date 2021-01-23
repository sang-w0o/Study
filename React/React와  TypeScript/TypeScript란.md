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
const plus:number = (a:number, b:number) => a + b;
let result = plus(1, 3); // 4
let result2 = plus('abc', 3);
```

* 위 경우, result2는 코드가 작성됨과 동시에 컴파일 에러를 일으킨다.   
  즉, JS에 비해 절대 타입으로 인한 의도치 않은 결과가 도출되는 것을 원천적으로 막을 수 있다.