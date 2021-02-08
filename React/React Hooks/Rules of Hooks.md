<h1>React Hook의 규칙</h1>

* 기본적으로 React Hook은 __React 함수 내에서만 호출해야 한다.__   
  호출 가능한 부분은 아래와 같다.
  * React 함수 컴포넌트에서 호출
  * Custom Hook에서의 호출

* 아래 코드를 보자.
```js
import React, { useState, useEffect } from "react";

const App = () => {
  const [person, setPerson] = useState({
    name: "sangwoo",
    age: 24
  });

  useEffect(() => {
    setPerson({age: 999});
  }, [person]);

  return (
    <div>
      {person.name} is {person.age} years old.
    </div>
  );
};

export default App;
```

* 위 코드처럼 당연히 한 컴포넌트 내에서 State나 Effect Hook을 여러 개 사용할 수 있다.   
  그렇다면 React는 내부적으로 어떤 state가 어떤 `useState()`에 의해 관리되는지 파악할 수 있을까?   
  정답은 __Hook들이 호출되는 순서를 기준으로 결정한다__ 는 것이다.   
  아래 코드를 보자.
```js
const Person = () => {
    const [name, setName] = useState('sangwoo');
    
    const setNameToStorage = () => {
        localStorage.setItem('name', name);
    };
    
    useEffect(() => {
        setNameToStorage();
    }, [setNameToStorage]);

    const [firstName, setFirstName] = useState('Ra');

    const updateTitle = () => {
        document.title = firstName + ' ' + name;
    }

    useEffect(() => {
        updateTitle();
    }, [updateTitle]);
}
```

* React는 Hook이 호출되는 순서에 의존한다고 했다.   
  모든 렌더링에서 Hook의 호출 순서는 같기 때문에 위 코드의 순서는 아래와 같다.
```js
// 렌더링 시작 시
useState('sangwoo');  // `sangwoo`라는 값을 name state변수를 선언한다.
useEffect(setNameToStorage);  // 로컬 스토리지에 name을 저장하는 effect를 추가한다.
useState('Ra');  // 'Ra'라는 값을 firstName state변수를 선언한다.
useEfect(updateTitle);  // document의 title을 업데이트하기 위한 effect를 추가한다.

// 두 번째 렌더링 시작 시
useState('sangwoo');  // name state 변수를 읽는다.(인자로 들어간 'sangwoo'는 무시된다.)
useEffect(setNameToStorage);  // 로컬 스토리지에 저장하기 위한 effect가 대체된다.
useState('Ra');  // firstName state 변수를 읽는다.(마찬가지로 인자로 들어간 'Ra'는 무시된다.)
useEffect(updateTitle);  // document의 title을 업데이트하기 위한 effect가 대체된다.
```

* Hook들의 호출 순서가 렌더링 간에 동일하다면 React는 지역적인 state를 각 Hook에 연동시킬 수 있다.   
  하지만 만약에 아래처럼 Hook이 조건문 내에서 특정 조건에만 호출된다면 어떻게 될까?
```js
//...
if(name !== '' && firstName !== '') {
    useEffect(() => {
        updateTitle();
    }, [updateTitle]);
}
```

* 첫 번째 렌더링 시에 위 코드의 조건문은 당연히 만족하므로 조건문 내의   
  `useEffect` Hook은 동작한다. 하지만 만약 name과 firstName state가 사용자에 의해   
  변경될 수 있는 값이라면 저 조건문을 만족하지 않는 경우가 생길 수도 있다.   
  만약 사용자에 의해 name과 firstName이 값이 없는 string이 된다면 조건문을 만족하지 않아   
  조건문 내의 `useEffect` Hook은 건너뛰게 될 것이다.   
  이 경우 hook이 호출되는 순서와 과정은 아래와 같다.
```js
// 첫 번째 렌더링이 아니라는 것을 잊지 말자.
useState('sangwoo');  // name state 변수를 읽는다.('sangwoo'라는 인자의 값은 무시된다.)
// useEffect(setNameToStorage);  // 이 hook은 호출되지 않는다.
useState('Ra');  // firstName state 변수를 읽는데 실패한다.
useEffect(updateTitle);  // document의 title을 업데이트하기 위한 effect가 대체되는 데 실패했다.
```

* 위 예시에서는 두 번째 hook인 `useEffect(setNameToStorage)`가 firstName이라는 state 변수를 접근하지   
  않기에 문제가 될 수 없다고 생각할 수 있지만, 만약 두 번째 hook에서 name state에 접근하게 된다면   
  이후에 문제가 생길 수 있을 것이다.

* 또한 React는 첫 번째 렌더링 때 처럼 컴포넌트 내에서 두 번째 hook의 호출이 `setNameToStorage` effect와   
  일치할 것이라 예상했지만 그렇지 않게 되었기 때문에, 그 시점부터 건너뛴 hook 다음에 호출되는 hook들의 순서가   
  하나씩 밀리면서 버그를 발생시키게 된다.

* 이러한 경우를 방지하기 위해 React는 조건적으로 hook을 호출하는 것을 금지한다.   
  이것이 __컴포넌트의 최상위(The top of level)에서 Hook이 호출되어야만 하는 이유__ 이다.   
  만약 조건적으로 hook을 호출해야 한다면, __조건문 자체를 hook 내부에 넣어서__ 구현 가능하다.
```js
const updateTitle = () => {
    if(name !== '' && firstName !== '') {
        document.title = firstName + ' ' + name;
    }
};

useEffect(() => {
    updateTitle();
}, [updateTitle])
```
<hr/>