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