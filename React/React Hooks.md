<h1>React Hooks</h1>

<h2>Context and State Management</h2>

* 리액트의 Component들은 State와 Props를 가지는데, 이들은 종종 서로 다른 컴포넌트에서 공유되곤 한다.
  * 예를 들면, 사용자 정보를 필요로하는 3개의 페이지가 있다고 하자. 각 페이지에 접근할 때 마다 사용자 정보를 위한   
    API요청을 보내는 것은 매우 비효율적이다.   
    첫 번째 방법으로는 위 3개의 페이지에 공통된 `User`라는 prop을 전달해주는 HOC를 만들어 활용하는 것이고,   
    두 번째 방법은 Redux와 같은 State Management Library를 활용하는 것이다.
  * 위 예제는 prop 전달의 단계가 1 이기 때문에 간단하다. 하지만 렌더링을 위해 부모 컴포넌트에서 자식 컴포넌트로   
    prop 전달을 3회 이상 하게되는 경우가 발생한다면, 이는 유지 보수 차원 등에서 절대 좋지 못하다.
<hr/>

<h2>useState</h2>

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

<h2>useEffect</h2>

* 공식 문서에 따르면, __Effect Hook를 사용하면 함수형 컴포넌트에서 `side effect`를 수행할 수 있다__ 고 한다.   
  여기서 side effect는 아래와 같은 것들을 의미한다.
  * API로부터 데이터 가져오기
  * 구독(Subscription) 설정하기
  * 수동으로 React 컴포넌트의 DOM 수정하기

* 클래스형 컴포넌트의 생명 주기 메소드들로 따졌을 때, `useEffect`는   
  `componentDidMount()`, `componentDidUpdate()`, `componentWillUnmount()`가 합쳐졌다고 볼 수 있다.

* 우선 비교를 위해 클래스형 컴포넌트를 보자.
```js
class Example extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      count: 0
    };
  }

  componentDidMount() {
    document.title = `You clicked ${this.state.count} times`;
  }
  componentDidUpdate() {
    document.title = `You clicked ${this.state.count} times`;
  }

  render() {
    return (
      <div>
        <p>You clicked {this.state.count} times</p>
        <button onClick={() => this.setState({ count: this.state.count + 1 })}>
          Click me
        </button>
      </div>
    );
  }
}
```

* 위 코드는 `componentDidMount()`에서 컴포넌트가 마운트 되는 시점에 문서의 제목을 바꾸고,   
  `componentDidUpdate()`에서 렌더링이 다시 될 때마다 문서의 제목을 갱신한다.   
  여기서 두 함수의 내용이 동일하다는 점을 주의깊게 보자.

* 위 코드를 함수형 컴포넌트를 사용하여 구현하면 아래와 같다.
```js
import React, { useState, useEffect } from 'react';

function Example() {
  const [count, setCount] = useState(0);

  useEffect(() => {
    document.title = `You clicked ${count} times`;
  });

  return (
    <div>
      <p>You clicked {count} times</p>
      <button onClick={() => setCount(count + 1)}>
        Click me
      </button>
    </div>
  );
}
```