React에서 자주 사용되는 JavaScript의 기술
=====

<h2>JavaScript Module</h2>

* JavaScript 모듈의 생성과 사용
  * 예시 코드 - sum.js
   ```js
   // sum.js
   export default function(values) {
       return values.reduce((total, val) => total + val, 0);
   }
   ``` 
  * 위 코드는 Array의 reduce 메소드를 사용해 값을 더하고 반환한다.
  * 여기서 중요한건 함수가 자신만의 파일에 정의되었다는 사실이다.

  * __export__ : 모듈의 기능을 모듈 밖에서 사용할 수 있도록 명시하는 키워드
    * JS 파일의 컨텐츠는 기본적으로 private이다. 따라서 app의 외부에서   
        사용될 수 있게 하려면 export키워드를 사용해 명시적으로 공유해야한다.
  * __default__ : 모듈에 기능이 단 하나만 있을 때 사용하는 키워드
  * __import__ : 모듈을 사용하기 위한 키워드
    * 사용법 : __import__ 식별자 __from__ "moduleRoute";
    * from 뒤에 오는 모듈 경로는 주로 상대경로를 이용하며,   
    * 확장자인 .js는 생략해도 된다.
    * 경로의 첫 부분에 .을 생략하면 import구문은 node_modules 폴더 내의   
        모듈로의 의존성을 선언한다.
      * node_modules폴더는 프로젝트를 처음 만들 때 설치되는 패키지들의 위치이다.
      * 이 위치는 독자적인 소프트웨어인 third-party 패키지가 제공하는 기능에   
        접근할 때 사용된다. react 패키지도 이에 속한다.

  * 예시 코드(2)
    ```js
    import React, {Component} from "react";
    ```
  * 위 import 구문은 마침표로 시작하지 않으므로 node_modules 폴더를 찾고,   
    거기서 react모듈로의 의존성을 갖게 된다.
  * __react 모듈은 React Application의 핵심 기능을 제공하는 패키지이다.__

  * 만약 모듈에서 특정 기능을 가져올 경우, 가져올 기능은 { }로 감싸주어야 한다.

  * export시, default 키워드를 사용하지 않았다면 import 시 { }로 가져와야 한다.

  * import 시, 다른 경로에서 같은 명명 기능을 가져올 경우 다음과 같이 as 키워드를 사용한다.
  ```js
  import {addValues} from './sum';
  import {addValues as newAddValues} from './newSum';

  // newSum에서 가져온 addValues 기능은
  // 이 코드에서는 newAddValues로 사용해야 한다.
  ```

  * 경로에서 가져올 모든 기능을 가져올 때는 *(asterisk)를 사용한다.
  ```js
  import * as ops from './operations'
  ops.divide();  // operations의 divide 기능 사용
  ops.add();  // operations의 add 기능 사용

  ```
<hr/>

<h2>JavaScript Promise</h2>

* Promise : 미래의 어느 시점에서 종료될 백그라운드 작업으로,   
  주로 HTTP요청을 통한 데이터 요청 시 사용된다.   
  그 이유는 이는 비동기식으로 수행되며, 웹 서버로부터 응답을   
  받은 후에 결과를 만들어야 하기 때문이다.

* 비동기 작업의 문제점
  * 간단한 예제 코드를 보자.
  ```js
  // async.js
  import {sumvalues} from './sum';
  
  export function asyncAdd(values) {
      setTimeout(() => {
          let total = sumValues(values);
          console.log(`Async total : ${total}`);
          return total;
      }, 500);
  }
  ``` 

  ```js
  // example.js
  import {asyncAdd} from './async';

  let values = [10, 20, 30, 40, 50];

  let total = asyncAdd(values);

  console.log(`Main total : ${total}`);
  ```

  * 위 example.js의 실행 결과는 다음과 같다.
  ```js
  // Main Total : undefined
  // Async Total : 150
  ```
  * Main Total이 undefined가 된 이유는, asyncAdd에서 0.5초를 기다린 후   
    함수를 수행하는데, JS는 비동기이므로 asyncAdd가 결과값을 반환하기 전에   
    console.log를 실행했기 때문이다.
  * 위와 같은 문제를 해결하기 위해 promise를 사용하게 된다.
  * promise를 사용하여 __비동기 작업이 완료되기를 기다렸다가 그 결과를__   
    __사용할 수 있도록__ 해보자.
  ```js
  //async.js
  
  import {sumValues} from './sum';

  export function asyncAdd(values) {
      return new Promise(callback=>
        setTimeout(()=> {
            let sum = sumValues(values);
            console.log(`Async total : ${sum}`);
            callback(total);
        }, 500));
  }
  ```
  * 위 코드를 분석해보자.
  * (1) new 키워드로 Promise객체 생성
    * Promise는 관찰할 함수를 파라미터로 받는다.
    * 관찰 대상인 함수에는 콜백 함수가 포함되는데, 이 함수는   
        비동기 작업이 완료되면 호출되며, 그 결과를 인자로 받는다.
    * 이와 같은 콜백 함수의 호출을 __약속 이행(Resolving the Promise)__ 라 한다.
  * (2) then 메소드 사용
  ```js
  // example.js
  import {asyncAdd} from './async';

  let values = [10, 20, 30, 40, 50];

  asyncAdd(values).then(total => console.log(`Main total : ${total}`));
  ```
  * then 메소드는 콜백이 실행되면 호출될 함수를 인자로 받는다.
  * 즉, 콜백에 전달된 결과는 then함수에 제공된다.
  * 이는 비동기작업이 완료되기 전에는 total의 값을 콘솔에 출력하지 않겠다는 것이고,   
    비동기 작업이 완료된 후에는 다음과 같은 결과를 볼 수 있다.
  ```js
  // Async total : 150
  // Main total : 150
  ```
<hr/>

<h2>비동기 작업을 다루는 더 쉬운 방법</h2>

* JS는 Promise를 직접 사용하지 않고도 비동기작업을 다룰 수 있게 한다.
* 이를 위해 제공하는 두 키워드는 __async, await__ 이다.
* __async, await는 app의 작동 방식에 전혀 영향을 주지 않는다.__   
  여전히 비동기작업은 수행되며, 작업이 완료되기 전까지 결과는 나오지 않는다.   
  이 두 키워드는 오직 비동기작업을 편하게 다룰 수 있게 하는 것이 목적이므로   
  then메소드를 사용하면 안된다.

* async, await를 사용한 example.js
```js
import {asyncAdd} from './async';

let values = [10, 20, 30, 40, 50];

async function doTask() {
    let total = await asyncAdd(values);
    console.log(`Main total : ${total}`);
}

doTask();

// 결과
// Async total : 150
// Main total : 150
```