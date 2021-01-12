Component와 props
======

<h2>무상태 컴포넌트(Stateless Component)</h2>

<table>
    <tr>   
        <td>그게 무엇인가?</td>
        <td>무상태 컴포넌트는 사용자에게 보여줄 컨텐츠를 렌더링하는 JS 함수이다. props는 컨텐츠 렌더링에 포함시킬 데이터를 한 컴포넌트에서
        다른 컴포넌트로 전달할 때 사용되는 수단이다.</td>
    </tr>
    <tr>   
        <td>왜 유용한가?</td>
        <td>컴포넌트는 JS, HTML, 그 외의 컴포넌트들을 조합해 리액트의 기능을 수행한다. props는 컴포넌트가 컨텐츠를 조정할 수 있게 한다.</td>
    </tr>
    <tr>   
        <td>어떻게 사용하는가?</td>
        <td>무상태 컴포넌트는 React Element를 반환하는 JS함수인데, 보통은 JSX 형식의 HTML을 사용해 정의한다.
        props의 경우에는 Element의 Property로 정의한다.</td>
    </tr>
    <tr>   
        <td>문제점 및 제약사항</td>
        <td>반드시 하나의 element를 항상 반환해야 하며, props에 관해서는 JS 표현식을 사용할 때 반드시 리터럴 값만을 지정해야한다는 점이 있다.</td>
    </tr>  
</table>

<hr/>

<h2>컴포넌트의 이해</h2>

```js
// src/App.js
export default function App() {
    return "Hello Sangwoo";
}
```
* 위 코드는 무상태 컴포넌트의 가장 단순한 형태를 보여주는 예시이다.
* 이 컴포넌트는 사용자에게 보여줄 컨텐츠를 반환하는, 즉 __rendering__ 하는 함수이다.
* App이 시작되면 index.js의 코드가 실행되는데, 그곳에 App 컴포넌트를 rendering하는   
  구문이 포함되어 있다.
* 그 결과 실행창에 지정한 "Hello Sangwoo"가 출력된다.
<hr/>

<h2>HTML Contents Rendering</h2>

* 컴포넌트가 문자열 값을 rendering하면, 이는 부모 element의 text 컨텐츠에 포함된다.
* 만약 HTML 컨텐츠를 반환한다면 더 유용한 컴포넌트가 될 수 있을 것이다.
```js
// src/App.js
import React from 'react';

export default function App() {
    return <h1 className="bg-primary text-white text-center p-2">
        Hello Sangwoo
    </h1>
}
```
* 위 코드는 빌드 과정에서 아래와 같이 변환된다.
```js
import React from 'react';

export default function App() {
    return React.createElement("h1", {className:"bg-primary text-white text-center p-2"},
        "Hello Sangwoo");
}
```

* 이 컴포넌트는 React.createElement 메소드의 결과, 즉 React가 DOM에 컨텐츠를 추가할 때   
  사용할 element를 반환한다.
* 만약 들여쓰기를 하고 싶다면 다음과 같이 하면 된다.
```js
export default function App() {
    return (
        <h1 className="bg-primary text-white text-center p-2">
            Hello Sangwoo
        </h1>
    )
}
```
* 주의 : __return문의 반환할 element는 소괄호로 묶어줘야 한다.__
* 위 처럼 function 키워드를 쓰는 대신 Arrow Function을 사용해도 된다.
* 또한 export 시 이름을 명명하는 것은 필수가 아닌 선택 사항이다.
<hr/>

<h2>다른 컴포넌트의 rendering</h2>

* React의 가장 중요한 기능 중 하나는 한 컴포넌트가 다른 컴포넌트들을 포함해 컨텐츠를   
  렌더링함으로써 복잡한 app을 만들 수 있다는 것이다.
* src/Message.js
```js
import React from 'react';

export function Message() {
    return <h4 className="bg-success text-white text-center p=2">
            This is a message
        </h4>
}
```
* src/App.js
```js
import React from 'react';
import {Message} from './Message';

export default function App() {
    return (
        <div>
            <h1 className="bg-primary text-white text-center p-2">
                Hello Sangwoo
            </h1>
            <Message/>
            <Message/>
        </div>
    )
}
```
* 위 코드에서는 새로운 import구문에서 Message element로 rendering할 Message 컴포넌트로의   
  의존성을 선언했다.
* App 컴포넌트가 rendering한 컨텐츠에는 Message Element가 포함됨으로써, Message 컴포넌트의   
  함수가 호출되고, Message element가 컨텐츠로 대체된다.
* React는 Message element를 만날 때 마다 Message 컴포넌트를 호출하고, 그 컨텐츠를   
  rendering한 결과로 Message element를 대체한다.
<hr/>

<h2>Props의 이해</h2>

* 각 컴포넌트들이 비슷한 컨텐츠를 rendering한다면 여러 자식 컴포넌트들의 컨텐츠를 포함하는   
  방법은 별로 유용하지 않을 것이다.
* React는 자식 컴포넌트가 자신의 컨텐츠를 rendering할 때 사용할 데이터를 부모로부터   
  받을 수 있는 __props(properties)__ 를 제공한다.

<h3>#1. 부모 컴포넌트에서 props 정의</h3>

* props는 컴포넌트에 적용된 Custom HTML Element에 프로퍼티를 추가하는 방식으로 정의한다.
```js
// src/App.js

//.. import

export default function App() {
    return (
        <div>
            <h1 className="bg-primary text-white text-center p-2">
                Hello Sangwoo
            </h1>
            <Message greeting="Hello" name="Kim"/>
            <Message greeting="Hola" name={"Park" + "inglot"}/>
        </div>
    )
}
```
* 위 코드에서는 각 Message 컴포넌트를 위해 greeting, name이라는 두가지 props를 추가했다.
* 대부분의 prop값은 리터럴 문자열로 표현된 정적인 값이지만, 두 번째 Message element의   
  name prop값은 두 문자열을 연결하는 표현식이다.
<hr/>
<h3>자식 컴포넌트에서 props 받기</h3>

* 자식 컴포넌트는 props를 __props__ 라는 이름의 파라미터로 받을 수 있다.
* props 객체에는 각 프로퍼티에 해당하는 prop들이 포함된다.
```js
// src/Message.js

//.. import

export function Message(props) {
    return <h4 className="bg-success text-white text-center p-2">
                {props.greeting}, {props.name}
           </h4>
}
```
* 자식 컴포넌트는 정적 값인지 표현값인지 알 필요 없이 그냥 JS의 객체처럼 props를 사용하면 된다.
<hr/>

<h2>JavaScript와 props의 조합</h2>

* 위 예시에서는 App 컴포넌트가 각 Message element에 전달한 prop값들의 결과로   
  각기 다른 컨텐츠가 생성됐다.
* 즉, 부모 컴포넌트가 동일한 기능을 각기 다른 방법으로 사용할 수 있다는 것이다.

<hr/>
<h3>#1. 컨텐츠의 조건부 rendering</h3>

* 컴포넌트는 JS의 조건문을 사용해 prop을 조사할 수 있으며, 그 값을 기초로 각기 다른 컨텐츠를   
  rendering할 수 있다.

<hr/>
<h3>#2. 배열 rendering</h3>

* JS Array객체의 메소드를 활용하여 요소들을 알맞게 rendering할 수 있다.

```js
// src/App.js
import React from 'react';
import {Summary} from "./Summary";

export default function App() {
    return (
        <div>
            <h1 className="bg-primary text-white text-center p-2">
                Hello Sangwoo
            </h1>
            <Summary names={["Bob", "Alice", "Jack"]} />
        </div>
    )
}
```
* Array.push 메소드의 사용 예제
```js
// src/Summary.js

import React from 'react';

function createInnerElements(names) {
    let arrayElems = [];
    for(let i = 0; i < names.length; i++) {
        arrayElems.push(
            <div>
                {`${names[i]} contains ${names[i].length} letters`}
            </div>
        )
    }
    return arrayElems;
}

export function Summary(props) {
    return <h4 className="bg-info text-white text-center p-2">
        {createInnerElements(props.names)}
        </h4>
}
```

* Array.map 메소드의 사용 예제
```js
// 기타 코드 모두 동일
function createInnerElements(names) {
    return names.map(name =>
        <div>
            {`${name} containes ${name.length} letters`}
        </div>
    )
}
```
<hr/>
<h3>#3. key prop의 추가</h3>

* React에서는 배열 안의 아이템을 효율적으로 다루기 위해 element에   
  __key__ prop을 추가해야 한다.
* __key prop의 값은 표현식이어야 하며, 그 값은 배열 내에서 유일해야 한다.__
* 위 코드에 key prop을 추가해보자.
```js
// src.Summary.js

// import..

export function Summary(props) {
    return (
        <h4 className="bg-info text-white text-center p-2">
            {props.names.map(name => 
                <div key={name} >
                    {`${name} contains ${name.length} letters`}
                </div>
            )
        }
        </h4>
    )
}
```
<hr/>
<h3>#4. 복수의 element rendering</h3>

* React 컴포넌트는 반드시 최상위 element를 하나만 반환해야 한다.   
  (물론 그 element가 다른 여러 자식 element를 포함할 수 있다.)
* 이러한 성질이 문제가 되는 경우가 있다.
* HTML 표준 명세에서는 element의 조합 방식을 규정하는데, 이게 하나의 element만   
  반환해야 하는 React와 충돌할 수 있는 부분이다.
* 다음 예시 코드를 보자.
```js
// src/App.js
import React from 'react';
import {Summary} from './Summary';

let names = ["Bob", "Alice", "Jack"];

export default function App() {
    return (
        <table className="table table-sm table-striped">
            <thead>
                <tr><th>#</th><th>Name</th><th>Letters</th></tr>
            </thead>
            <tbody>
                {names.map((name, index) =>
                    <tr key={name}>
                        <Summary index={index} name={name} />
                    </tr>
                )}
            </tbody>
        </table>
    )
}
```
* 위 코드를 보면 Summary 컴포넌트는 index와 name이라는 prop을 전달받는다.
```js
// src/Summary.js
import React from 'react';

export function Summary(props) {
    return  <td>{props.index + 1}</td>
            <td>{props.name}</td>
            <td>{props.name.length}</td>
}
```
* 위 코드를 실행하면, 아래와 같은 에러가 뜬다.
```text
Parsing Error : Adjacent JSX elements must be wrapped in an enclosing tag.
```
* 위 에러 메시지는, 컴포넌트가 rendering하는 컨텐츠가 단일한 최상위 element를   
  반환해야 한다는 React의 규칙에 맞지 않는다고 지적한다.   
  (HTML 표준 명세상의 문제는 전혀 없는 코드이다.)
* 위와 같은 문제를 해결하기 위해 최상위 element로 __React.Fragment__ 를 사용한다.
```js
// src/Summary.js
import React from 'react';

export function Summary(props) {
    return
        <React.Fragment>
            <td>{props.index + 1}</td>
            <td>{props.name}</td>
            <td>{props.name.length}</td>
        </React.Fragment>
}
```
* 참고 : React.Fragment 대신 빈 태그 (<>, </>)로 묶어도 된다.
<hr/>

<h3>#5. Rendering 하지 않기</h3>

* 경우에 따라 조건부로 컴포넌트를 반환해야 하는 경우가 있다.
* 특정 경우에 반환하지 않으려면 __null__ 을 반환하면 된다.
<hr/>

<h3>#6. props의 변경 시도</h3>

* __props는 읽기 전용으로 컴포넌트에 의해 변경될 수 없다.__
* React는 props 객체를 만들 때, 추후 변경이 시도되면 에러를 발생시킨다.
<hr/>

<h2>함수 props</h2>

* React는 위에서 본 Data Props 외에 Function Props를 지원한다.
* Function props는 부모 컴포넌트가 자식에게 제공하는 함수이며, 이 함수가 호출되면   
  호출에 의해 변경된 사항이 부모에게 통지된다.
* 이에 부모 컴포넌트는 data props를 변경함으로써 응답할 수 있으며, 이는   
  자식 컴포넌트가 사용자에게 보여줄 컨텐츠를 갱신할 수 있게 한다.
```js
// src/App.js
import React from 'react';
import {Summary} from './Summary';
import ReactDOM from 'react-dom';

let names = ["Bob", "Alice", "Jack"];

function reverseNames() {
    names.reverse();
    ReactDOM.render(<App/>, document.getElementById('root'));
}

export default function App() {
    return( 
        <table className="table table-sm table-striped">
            <thead>
                <tr><th>#</th><th>Name</th><th>Letters</th></tr>
            </thead>
            <tbody>
                {names.map((name, index) =>
                    <tr key={name}>
                        <Summary index={index} name={name} reverseCallback={reverseNames} />
                    </tr>
                )}
            </tbody>
        </table>            
    )
}
```

```js
// src/Summary.js
import React from 'react';

export function Summary(props) {
    return (
        <React.Fragment>
            <td>{props.index + 1}</td>
            <td>{props.name}</td>
            <td>{props.name.length}</td>
            <td>
                <button className="btn btn-primary btn-sm"
                    onClick={props.reverseCallback}>
                    Change
                </button>
            </td>
        </React.Fragment>
    )
}
```
* 위 코드에서는 버튼을 클릭하면 props의 reverseCallback 함수를 호출한다.
* ReactDOM.render 메소드
  * render는 컴포넌트의 컨텐츠를 브라우저의 DOM에 추가하는 메소드로,   
    Application을 시작시키는 index.js파일에서 사용된다.
<hr/>
<h3>#1. 함수 prop에 인자 사용</h3>

```js
// src/App.js

// 기타 코드 모두 동일

function promoteName(name) {
    names = [name, ...names.filter(val => val !== name)];
    ReactDOM.render(<App />, document.getElementById('root'));
}
// promoteName 메소드는 name을 배열의 가장 앞에 위치시킨다.

export default function App() {
    return( 
        <table className="table table-sm table-striped">
            <thead>
                <tr><th>#</th><th>Name</th><th>Letters</th></tr>
            </thead>
            <tbody>
                {names.map((name, index) =>
                    <tr key={name}>
                        <Summary index={index} name={name} 
                            reverseCallback={reverseNames} promoteCallback={promoteName}  />
                    </tr>
                )}
            </tbody>
        </table>            
    )
}
```

```js
// src/Summary.js
import React from 'react';

export function Summary(props) {
    return (
        <React.Fragment>
            <td>{props.index + 1}</td>
            <td>{props.name}</td>
            <td>{props.name.length}</td>
            <td>
                <button className="btn btn-primary btn-sm"
                    onClick={props.reverseCallback}>
                    Change
                </button>
                <button className="btn btn-info btn-sm m-1"
                    onClick={ () => props.promoteCallback(props.name)}>
            </td>
        </React.Fragment>
    )
}
```
* 주의 : __인자를 사용해 함수 prop을 호출할 때는 반드시 화살표 함수를 지정해야 한다.__
<hr/>

<h2>자식 컴포넌트에 props 전달</h2>

* React Application은 컴포넌트들의 조합으로 만들어지며, 그 사이에   
  부모 자식 관계들이 형성된다.
* 이러한 구조에선 한 컴포넌트가 부모로부터 data와 callback함수를 받고,   
  이를 다시 자식 컴포넌트에 전달하는 일들이 자주 생긴다.

```js
// src/CallbackButton.js
import React from 'react';

export function CallbackButton(props) {
    return (
        <button className={`btn btn-${props.theme} btn-sm m-1`}
            onClick={props.callback}>
            {props.text}
        </button>
    )
}
```
* 위 컴포넌트는 __text__ prop값을 사용한 text 컨텐츠를 갖는 버튼 element를   
  하나 rendering하며, 버튼이 클릭될 때 __callback__ prop을 통해 함수를 호출한다.   
  또한 __theme__ prop은 버튼 element를 위한 부트스트랩 CSS 스타일을 적용하기   
  위해 사용된다.

```js
// src/Summary.js
import React from 'react';
import {CallbackButton} from './CallbackButton';

export function Summary(props) {
    return (
        <React.Fragment>
            <td>{props.index + 1}</td>
            <td>{props.name}</td>
            <td>{props.name.length}</td>
            <td>
                <CallbackButton theme="primary" text="Reverse" 
                    callback={props.reverseCallback} />
                <CallbackButton theme="info" text="Promote"
                    callback={ () => props.promoteCallback(props.name)} />
            </td>
        </React.Fragment>
    )
}
```
* 위 코드는 가장 먼저 __최상위 부모인 App컴포넌트에서 name이 자식 컴포넌트인__   
  __Summary컴포넌트로 name, reverseCallback, promoteCallback을 전달했다.__   
  __Summary에서는 props로 위 3개에 접근했다. 그리고 또 Summary를 부모로 가지는__   
  __CallbackButton도 name을 props를 통해 접근할 수 있다.__
* 즉, CallbackButton은 name을 두 단계 위의 부모인 App컴포넌트로부터 받은 것이다.
<hr/>
<h3>props의 일괄 전달</h3>

* 부모 컴포넌트가 제공한 props의 이름과 자식 컴포넌트가 원하는 props의 이름이   
  동일한 경우에는 spread 연산자를 사용할 수 있다.
```js
// src/SimpleButton.js
import React from 'react';

export function SimpleButton(props) {
    return (
        <button onClick={props.callback} className={props.className}>
            {props.text}
        </button>
    )
}
```
* 위의 SimpleButton컴포넌트는 callback, className, text props를 요구한다.
* SimpleButton컴포넌트가 CallbackButton의 자식 컴포넌트가 된다면, 이 세개의   
  props는 부모가 제공한 props와 겹치게 된다.
* 이러한 경우에 spread 연산자를 사용해 props를 일괄 전달할 수 있다.
```js
// src/CallbackButton.js
import React from 'react';
import {SimpleButton} from './SimpleButton';

export function CallbackButton(props) {
    return (
        <SimpleButton {...props} className={`btn btn-${props.theme} btn-sm m-1`}>
    )
}
```
* 위의 {...props}는 부모 컴포넌트로부터 받은 모든 props를 SimpleButton에게 전달한다.
```js
// src/CallbackButton.js
import React from 'react';
import {SimpleButton} from './SimpleButton';

export function CallbackButton(props) {
    let {theme, ...childProps} = props;
    return (
        <SimpleButton {...childProps} className={`btn btn-${props.theme} btn-sm m-1`}>
    )
}
```
* 위 코드에서는 theme을 제외한 부모의 props를 모두 포함하는 childProps 배열 객체를 만들었다.
* 이 때 사용된 __... 연산자는 Rest Operator__ 이다.
* 지정된 인자가 아닌 나머지 모두를 포함하는 하나의 배열 객체를 __Rest Parameter__ 라 한다.
<hr/>

<h2>기본 prop값 제공</h2>

* App에서 사용하는 props의 수가 증가함에 따라 동일한 props를 반복 사용하는 경우가 발생한다.
* 매번 같은 값을 반복해서 사용하면, 기본값을 정의하고 필요에 의해 덮어쓸 수 있다.
```js
// src/CallbackButton.js
import React from 'react';
import {SimpleButton} from './SimpleButton';

export function CallbackButton(props) {
    let {theme, ...childProps} = props;
    return (
        <SimpleButton {...childProps} className={`btn btn-${props.theme} btn-sm m-1`}>
    )
}

CallbackButton.defaultProps = {
    text: "Default Text",
    theme:"Warning"
}
```
* 위에서는 새로 추가한 defaultPrps 프로퍼티에 부모 컴포넌트가 값을 제공하지 않을 때   
  사용할 기본값이 정의된 객체(defaultProps)를 할당했다.
```js
// src/Summary.js
import React from 'react';
import {CallbackButton} from './CallbackButton';

export function Summary(props) {
    return (
        <React.Fragment>
            <td>{props.index + 1}</td>
            <td>{props.name}</td>
            <td>{props.name.length}</td>
            <td>
                <CallbackButton callback={props.reverseCallback} />
                <CallbackButton theme="info" text="Promote"
                    callback={ () => props.promoteCallback(props.name)} />
            </td>
        </React.Fragment>
    )
}
```
* 위 코드의 첫 번째 CallbackButton컴포넌트는 theme과 text가 지정되어있지 않다.
* 이러한 경우에 CallbackButton의 defaultProps가 적용된다.
<hr/>

<h2>prop 값의 타입 검사</h2>

* props는 받고자 하는 값의 data type을 알 수 없으며, 받은 값을 사용할 수 없을 때   
  이를 부모 컴포넌트에 알릴 방법도 없다.
* 이를 해결하기 위해 React에서는 다음과 같이 props의 type선언을 지원한다.

```js
// src/SimpleButton.js
import React from 'react';
import PropTypes from 'prop-types';

export function SimpleButton(props) {
    return (
        <button onClick={props.callback} className={props.className}>
            {props.text}
        </button>
    )
}

SimpleButton.defaultProps = {
    disabled:false
}

SimpleButton.propTypes = {
    text:PropTypes.string,
    theme:PropTypes.string,
    callback:PropTypes.func,
    disabled:PropTypes.bool
}
```
* 위의 propTypes 프로퍼티에는 prop이름이 대응하는 프로퍼티 이름과 type을 지정한   
  객체를 할당했다. 타입 지정 시 사용한 __PropTypes__ 는 __prop-types__ 패키지로부터   
  가져으며, 유용한 값들은 다음과 같다.

<table>
    <tr>
        <td>array</td>
        <td>해당 prop이 배열이어야 한다.</td>
    </tr>
    <tr>
        <td>bool</td>
        <td>해당 prop이 boolean 타입이어야 한다.</td>
    </tr>
    <tr>
        <td>func</td>
        <td>해당 prop이 함수여야 한다.</td>
    </tr>
    <tr>
        <td>number</td>
        <td>해당 prop이 숫자 타입이어야 한다.</td>
    </tr>
    <tr>
        <td>object</td>
        <td>해당 prop이 객체여야 한다.</td>
    </tr>
    <tr>
        <td>string</td>
        <td>해당 prop이 문자열 타입이어야 한다.</td>
    </tr>
</table>

* PropTypes에는 둘 이상의 타입이나 특정 값들을 지정할 수 있는 다음의 메소드가 있다.
<table>
    <tr>
        <td>oneOfType</td>
        <td>PropTypes 값들의 배열, 즉 여러 타입을 지정할 수 있다.</td>
    </tr>
    <tr>
        <td>oneOf</td>
        <td>타입이 아닌 실제 값들의 배열을 지정할 수 있다.</td>
    </tr>
</table>

```js
// src/SimpleButton.js
SimpleButton.propTypes = {
    //...
    disabled: PropTypes.oneOfType([PropTypes.bool, PropTypes.string]);
    // disabled prop은 boolean이나 string 둘중 하나이다.
}
```