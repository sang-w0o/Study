<h1>React에서 TS 사용하기</h1>

<h2>프로젝트 생성 방법</h2>

* React 프로젝트의 생성 방법은 JS로 할 때와 마찬가지로 `CRA(Create-React-App)`을 사용한다.   
  CRA는 이미 TS를 사용할 수 있는 패키지들을 포함하는데, 프로젝트 생성 시 `--typescript` 옵션을 주면   
  해당 프로젝트에 TS를 사용할 것임을 명시할 수 있다.
```
npx create-react-app APP_NAME --template typescript
```

* 이렇게 프젝트를 생성하면 기본으로 제공되는 파일들이 모두 TS로 작성되어 있는 것을 확인할 수 있다.
<hr/>

<h2>tsconfig.json</h2>

* `tsconfig.json`에는 TS 사용에 대한 규칙들이 지정되어 있다.   
  아래는 기본적으로 제공되는 `tsconfig.json`의 내용이다.
```json
{
  "compilerOptions": {
    "target": "es5",
    "lib": [
      "dom",
      "dom.iterable",
      "esnext"
    ],
    "allowJs": true,
    "skipLibCheck": true,
    "esModuleInterop": true,
    "allowSyntheticDefaultImports": true,
    "strict": true,
    "forceConsistentCasingInFileNames": true,
    "noFallthroughCasesInSwitch": true,
    "module": "esnext",
    "moduleResolution": "node",
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx"
  },
  "include": [
    "src"
  ]
}
```

* 예를들어, 위의 "allowSyntheticDefaultImports"를 false로 바꾸면, import문을   
  아래와 같이 바꿔주어야 한다.
```ts
// 기존 import문
import React from 'react';

// 바뀐 import문
import * as React from 'react';
```

* 또한, 아래의 함수를 보자.
```ts
const plus = (a, b) => a + b;
```

* 위 함수는 타입을 지정하지 않았기에 명시적으로 `any` 타입을 갖게 되어   
  컴파일 에러를 발생시킨다. 에러는 아래와 같다.
```
TypeScript error: Parameter 'a' implicitly has an 'any' type. TS7006
```

* 위 에러 로그에서 알 수 있듯이, 위의 오류는 `TS7006` 규칙에 따르기 때문에 발생한 것이다.   
  이와 같은 규칙을 재정의하면 `tsconfig.json`을 수정하면 된다.
```json
{
    //....
    "noImplicitAny": false
}
```

* 위의 `noImplicitAny`는 위에서 발생한 `TS7006` 규칙을 무시하게 해준다.
<hr/>

<h2>package.json</h2>

* `package.json`을 살펴보면 dependency에 `@types`로 시작하는 패키지들이 설치된 것을 볼 수 있다.   
  TS는 JS에 기반을 두기에, 이 라이브러리들은 JS로 작성된 React가 TS에서 작동할 수 있게   
  적절한 타입들을 선언해준 라이브러리다.

* 예를 들어 `styled-components` 패키지를 추가하고 import하면 이 패키지는 JS로 작성되었기에   
  TS가 인식하기로는 타입이 정의되어 있지 않은 `any` 타입이 된다.

* 이를 해결하기 위해서는 `@types/styled-components` 패키지를 추가해주면 된다.   
  이렇게 JS로 작성된 라이브러리를 TS 프로젝트에서 사용하기 위해서는 해당 라이브러리를   
  TS 컴파일러가 인식할 수 있도록 해주는 또다른 라이브러리를 추가해줘야 한다.

* 만약 사용하려는 라이브러리에 대해 TS 타입을 제공하는 라이브러리가 없다면   
  `tsconfig.json`에 `"noImplicitAny": true`를 지정해줘야 한다.  
<hr/>

<h2>TS와 React State</h2>

* 간단한 클래스형 컴포넌트를 보자.
```ts
import React, { Component } from 'react';

class App extends Component {
  state = {
    counter: 0
  }

  add = () => {
    this.setState(prev => {
      return {
        counter: prev.counter++
      }
    })
  }
  render() {
    const {counter} = this.state;
    return (
      <div>{counter}</div>
    )
  }
}

export default App;
```

* 위 코드를 실행하면 에러 메시지가 나오는데, 아래와 같다.
```
Property 'counter' does not exist on type 'Readonly<{}>
```

* 이는 우리가 TS 컴파일러가 인식하도록 state를 정의하지 않았기 때문이다.   
  이를 해결하기 위해서는 state를 지정해줘야 한다.

* `Component` 부분을 보면, 아래와 같다.
```js
class Component<P = {}, S = {}, SS = any>
```

* `P`는 props, `S`는 state를 가리킨다.   
  따라서 이 부분에 state를 지정해줘야 한다.
```ts
import React, { Component } from 'react';

interface IState {
  counter: number
}

class App extends Component<{}, IState> {
  state = {
    counter: 0
  }

  add = ():void => {
    this.setState(prev => {
      return {
        counter: prev.counter + 1
      }
    })
  }
  render() {
    const {counter} = this.state;
    return (
      <div>
        {counter}
        <button onClick={this.add}>Add!</button>
      </div>
    )
  }
}

export default App;
```

* 이 후에 counter에 숫자가 아닌 타입을 지정하려고 한다면   
  TS 컴파일러가 에러를 띄우게 된다.
<hr/>

<h2>TS와 React Props</h2>

* 위의 `App.tsx`의 counter를 props로 받는 `Number`라는 컴포넌트가 있다고 해보자. 코드는 아래와 같다.
```ts
import React from 'react';
import styled from 'styled-components';

const Container = styled.span``;

interface IProps {
    count: number
}

const Number: React.FC<IProps> = ({count}) => (
    <Container>{count}</Container>
);

export default Number;
```

* `Number` 컴포넌트는 함수형 컴포넌트이며, 이 컴포넌트의 TS 타입은 아래 두 개 중 하나이다.
  * `React.FC<>`
  * `React.FunctionComponent<>`

* 클래스형 컴포넌트와 마찬가지로 `<>` 내에 Props에 대한 인터페이스를   
  생성한 후 지정해주었다.
<hr/>

<h2>TS와 React Events</h2>

* 아래와 같은 두 개의 컴포넌트가 있다고 하자.
```ts
import React from 'react';

export const Input: React.FC = () => (
    <input type="text" placeholder="Name" />
);

export const Form: React.FC = ({children}) => (
    <form>{children}</form>
)
```

* 그리고 위 컴포넌트를 사용하는 곳은 아래와 같다.
```ts
class App extends Component<{}, IState> {
  state = {
    counter: 0
  }

  add = ():void => {
    this.setState(prev => {
      return {
        counter: prev.counter + 1
      }
    })
  }
  render() {
    const {counter} = this.state;
    return (
      <div>
        <Form>
          <Input />
        </Form>
      <Number count={counter}/>
      </div>
    )
  }
}
```

* `<Form>` 컴포넌트 내에 `<Input>` 컴포넌트가 있음을 알 수 있다.   
  이 상태로 `Form` 컴포넌트의 children을 보면, `React.ReactNode`라는   
  타입이 지정되어 있는 것을 알 수 있다.

* 이는 `Form`이 `React.FC` 타입이라고 지정해주었고, `React.FC`는   
  기본적으로 children이라는 prop을 가지기 때문이다.   
  위 경우 `Form`의 children은 `Input`이 된다.

* 이제 `Input`의 값을 처리해보자. 우선 `<input />` 태그는 value와   
  `onChange()` 함수를 prop으로 가져야 한다.
```ts
export const Input: React.FC = ({value, handleChange}) => (
    <input type="text" 
        placeholder="Name" 
        value={value} 
        onChange={handleChange} 
    />
);
```

* 위에서 Props를 받아온 것과 마찬가지로 TS 컴파일러는 value와   
  handleChange에 대해 모르기 때문에 아래와 같이 interface를 지정해주자.
```ts
interface IInputProps {
    value: string;
    handleChange: () => void;
}

export const Input: React.FC<IInputProps> = ({value, handleChange}) => (
    <input type="text" 
        placeholder="Name" 
        value={value} 
        onChange={handleChange} 
    />
);
```

* `IInputProps.handleChange()`는 반환하는 값이 없는 함수라고 타입을 지정했다.

* 이제 `Input` 컴포넌트를 호출하는 부분에 value와 handleChange를 전달하지   
  않으면 TS 컴파일러는 오류를 띄운다.

* `handleChange()` 함수를 아래와 같이 작성하고, `Input`에 전달해보자.
```ts
handleChange = (event) => console.log(event.target);

//...
<Input type="text" placeholder="Name" value={value} onChange={this.handleChange}>
```

* 이렇게 했을 때, `Input` 컴포넌트에서 오류가 발생한다.   
  props의 인터페이스로 전달해준 `handleChange()`는 인자로 받는 값이 없는   
  void형 함수인데, event를 인자로 전달했기 때문이다.

* 따라서 `IInputProps`와 `handleChange()`를 각각 수정해주자.
```ts
// Input 컴포넌트가 정의된 부분
interface IInputProps {
    value: string;
    onChange: (event: React.SyntheticEvent<HTMLInputElement>) => void;
}

// Input 컴포넌트를 사용하는 부분
handleChange = (event: React.SyntheticEvent<HTMLInputElement>) => {

}
```

* 이제 `Form` 컴포넌트를 보자. `Button`이 클릭 되었을 때 기본적으로   
  수행되는 `submit` 이벤트를 방지하도록 해보자.
```ts
// Form 컴포넌트를 사용하는 부분
onFormSubmit = (event: React.FORMEvent) => {
    event.preventDefault();
    // TODO
}

// Form 컴포넌트가 정의된 부분
interface IFormProps {
    onFormSubmit: (event: React.FormEvent) => void;
}

export const Form: React.FC<IFormProps> = ({ children, onFormSubmit }) => (
    <form onSubmit={onFormSubmit}>{children}</form>
)
```
<hr />