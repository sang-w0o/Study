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

* `useEffect`는 __컴포넌트가 렌더링 된 이후에 특정 작업들을 수행할 수 있게__ 해준다.
* `useEffect`를 컴포넌트 내에 두는 이유는 해당 컴포넌트의 모든 state와 props에 접근하게 하기 위함이다.
* `useEffect`는 렌더링 이후애 매번 수행된다. 즉 렌더링이 될 때마다 `useEffect`가 한 번씩 호출된다고 보면 된다.

* 또한 `useEffect`는 첫 번째 인자로는 수행할 함수 또는 행동, 두 번째 인자로는   
  첫 번째 인자로 들어온 함수가 수행될 때 의존하는 변수들을 배열로 넘겨준다.
```js
import React, { useState, useEffect } from "react";

const App = () => {
  const [person, setPerson] = useState({
    name: "sangwoo",
    age: 24
  });

  useEffect(() => {
    setPerson({age: 999});
  }, []);

  return (
    <div>
      {person.name} is {person.age} years old.
    </div>
  );
};

export default App;
```

* 위 코드를 수행하면 아래와 같은 경고문이 출력된다.
```
React Hook useEffect has a missing dependency:'person'
Either include it or remove the dependency array.(react-hooks/exhaustive-deps)
```

* 위 경고문이 출력되는 이유는 `useEffect`의 첫 번째 인자에서 person을 참조하기 때문이다.   
  이렇게 외부 변수를 참조할 때에는 dependency array에 추가해줘야 한다.
```js
useEffect(() => {
  setPerson({ age: 999});
}, [person]);
```

* 이렇게 하면 person은 `useState`로 age를 24로 초기화했지만,   
  `useEffect`내에서 person의 age를 999로 바꿨기 때문에 화면에는 999가 보인다.