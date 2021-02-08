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
