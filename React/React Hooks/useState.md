<h1>useState</h1>

* 우선 클래스형 컴포넌트에서 state를 사용하는 간단한 예시는 아래와 같다.
```js
import React from 'react';

class Example extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      count: 0
    }
  }

  render() {
    return (
      <div>
        <p>Clicked times : {this.state.count}</p>
        <button onClick={() => this.setState({ count: this.state.count + 1 })}>
          Click!
        </button>
      </div>
    )
  }
}
```

* 위에서는 count라는 state의 변수를 `this.state = { }`로 선언했다.   
  이에 반해 함수형 컴포넌트는 클래스가 없으므로 상속 관계도 없으며, 당연히 `this`도 없다.   
  위 예시를 함수형 컴포넌트로 바꾸면 아래와 같다.
```js
import React, { useState } from 'react';

const Example = () => {
  const [count, setCount] = useState(0);

  return (
    <div>
      <p>Clicked times : {count}</p>
      <button onClick={() => setCount(count + 1)}>
        Click!
      </button>
    </div>
  )
}
```

* 위 코드에서는 state에 count라는 변수를 `useState()`를 사용하여 정의했다.   
  두 번째 인자인 `setCount`는 함수로, count 변수를 바꿀 때에 사용되며,   
  `useState()`에 들어간 인자인 0은 count를 0으로 초기화시킨다.

* 코드에서 보다시피 count는 const로 선언되어 있어 직접적인 변경이 불가하며,   
  오로지 `setCount()`를 통해서만 변경할 수 있다.

* `useState()`로 변수를 초기화할 때에는 number, string 뿐만 아니라 배열, object도 가능하다.
```js
const [object, setObject] = useState({
  userInfo: {
    id: 1,
    name: 'sangwoo',
    role: 'user'
  }
})
```

* 참고로 `useState`의 두 번째 인자인 함수를 통해 첫 번째 인자인 변수의 값을 변경할 때   
  아래와 같이 두 가지 방법이 가능하다.
```ts
interface ApiResult {
  name: string;
  warehouseId: number;
  lastModifiedAt: string;
  status: string;
}

const Example: React.FC = () => {
  const [results, setResults] = useState<Array<ApiResult>>([]);

  // 비즈니스 로직 중
  setResults((prevResults) => [...prevResults, newResult]);
}
```
<hr/>