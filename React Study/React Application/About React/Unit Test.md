Unit Test
======

* Unit Test의 맥락 잡기

<table>
    <tr>
        <td>그게 무엇인가?</td>
        <td>React Component는 app의 다른 부분과의 상호 작용을 분리해서 조사할 수 있는, 유닛 테스트에 대한 특별한 지원을 필요로 한다.</td>
    </tr>
    <tr>
        <td>왜 유용한가?</td>
        <td>독립된 유닛 테스트는 app의 다른 부분으로부터 영향을 받지 않고, 컴포넌트의 기본 로직을 테스트할 수 있게 한다.</td>
    </tr>
    <tr>
        <td>문제점 또는 제약사항이 있는가?</td>
        <td>효과적인 유닛 테스트가 쉽지만은 않다. 유닛 테스트를 쉽게 작성하고 실행할 수 있는 지점을 얻기 위해 시간과 노력을 들여야 하며,
            테스트를 위한 대상을 올바르게 분리할 수 있어야 한다.</td>
    </tr>
</table터

* `create-react-app`은 프로젝트 생성 시, 기본적인 테스트 도구를 포함한다. 그러나 더 쉽게 사용할 수 있는   
  테스트 패키지가 있는데, 바로 enzyme이다.
* 패키지 설치 명령어 :    
  * `npm install --save-dev enzyme@3.8.0`
  * `npm install --save-dev enzyme-adapter-react-16@1.7.1`
* `enzyme` : 렌더링된 컨텐츠를 검사하고 그 props와 state를 확인함으로써 컴포넌트를 쉽게 테스트할 수 있게 한다.
* `enzyme-adapter-react-16` : enzyme을 사용하기 위한 React16을 위한 어댑터

* 아래의 `Result` 컴포넌트를 작성한다.
```js
// src/Result.js
import React, {Component} from 'react';

export const Result = (props) => {
    return <div className="bg-light text-dark border border-dark p-2">
        {props.result || 0}
    </div>
}
```
* 위 `Result`는 간단한 함수형 컴포넌트로써, result prop을 통해 받은 어떠한 계산 결과를 보여준다.

```js
// src/ValueInput.js
import React, {Component} from 'react';

export class ValueInput extends Component {

    constructor(props){
        super(props);
        this.state={
            fieldValue:0
        }
    }

    handleChange = (event) => {
        this.setState({
            fieldValue: event.target.value
        }, () => this.props.changeCallback(this.props.id, this.state.fieldValue));
    }

    render() {
        return(
            <div className="form-group p-2">
                <label>Value #{this.props.id}</label>
                <input className="form-control"
                    value={this.state.fieldValue}
                    onChange={this.handleChange} />
            </div>
        )
    }
}
```
* 위의 `ValueInput`은 input element를 rendering하고, 변경사항이 있을 때에 콜백함수를 호출하는   
  상태 유지 컴포넌트이다.
```js
// src/App.js
import React, {Component} from 'react';
import {ValueInput} from './ValueInput';
import {Result} from './Result';

export default class App extends Component {

  constructor(props){
    super(props);
    this.state={
      title: this.props.title || "Simple Addition",
      fieldValues:[],
      total : 0
    }
  }

  updateFieldValue = (id, value) => {
    this.setState(state => {
      state.fieldValues[id] = Number(value);
      return state;
    });
  }

  updateTotal = () => {
    this.setState(state => ({
      total: state.fieldValues.reduce((total, val) => total += val, 0)
    }));
  }

  render() {
    return(
      <div className="m-2">
        <h5 className="bg-primary text-white text-center p-2">
          {this.state.title}
        </h5>
        <Result result={this.state.total}/>
        <ValueInput id="1" changeCallback={this.updateFieldValue}/>
        <ValueInput id="2" changeCallback={this.updateFieldValue}/>
        <ValueInput id="3" changeCallback={this.updateFieldValue}/>
        <div className="text-center">
          <button className="btn btn-primary" onClick={this.updateTotal}>
            Total
          </button>
        </div>
      </div>
    )
  }  
}
```
<hr/>

<h2>간단한 Unit Test</h2>

* `create-react-app`으로 만든 프로젝트에는 Unit Test를 실행하고 그 결과를 보여주는 `Jest`라는 테스트 도구가 있다.   
  또한 프로젝트 설정 과정의 일환으로 `App.test.js` 파일이 생성되는데, 그 내용은 다음과 같다.
```js
import React from 'react';
import ReactDOM from 'react-dom';
import App from './App';

it('renders without crashing', () => {
  const div = document.createElement('div');
  ReactDOM.render(<App/>, div);
  ReactDOM.unmountComponentAtNode(div);
})
```
* 위는 기초적인 유닛 테스트이며, it 함수를 캡슐화 한다.   
  it함수의 첫 번째 인자는 __테스트에 대한 설명__ 이고, 두 번째 인자는 __어떤 작업을 수행하는 함수로서, 테스트 자체에__   
  __해당한다__. 여기서는 App 컴포넌트를 div element내에서 rendering한 후 unmound를 한다.
* Unit test를 수행하기 위한 명령어는 다음과 같다.   
  `npm run test`
* 테스트가 실행된 다음에 테스트 도구는 __감시 모드__ 에 들어가고, 이후에 파일이 변경되면 다시 test가 실행돼   
  그 결과를 보여주게 된다. Unit Test의 결과를 실패로 만들기 위해 다음 코드로 잠시 수정해보자.
```js
// src/App.js

//...

render() {
    throw new Error("SOMETHING WENT WRONG");
    //..
}
```
* 위 추가된 코드 때문에 `render` 메소드 호출 시, 에러가 던져질 것이며 이는 __unit test에서 감시하는 동작__ 이다.
<hr/>

<h2>얕은 rendering을 사용한 컴포넌트 테스트</h2>

* 얕은 rendering(Shallow Rendering)은 __컴포넌트를 자식으로부터 격리해 테스트__ 할 수 있게 한다.   
  이는 컨텐츠의 상호작용에 의한 영향 없이 컴포넌트의 기본 기능을 테스트할 수 있는 효과적인 기법이다.
* 얕은 rendering을 사용해 `App` 컴포넌트를 테스트해보기 위해 아래 코드를 작성하자.
```js
// src/appContent.test.js
import React from 'react';
import Adapter from 'enzyme-adapter-react-16';
import Enzyme, {shallow} from 'enzyme';
import App from './App';
import {ValueInput} from './ValueInput';

Enzyme.configure({adapter : new Adapter()});

it("Renders three ValueInputs", () => {
    const wrapper = shallow(<App />);
    const valCount = wrapper.find(ValueInput).length;
    expect(valCount).toBe(3);
});
```
* 위 코드를 자세하게 살펴보자.

```js
Enzyme.configure({ adapter:new Adapter()});
```
* `Enzyme.configure()` 메소드에는 설정 객체 하나가 전달되며, 거기엔 Adapter 패키지의 컨텐츠가 할당된   
  adapter 프로퍼티가 포함된다.

```js
it("Renders three ValueInputs", () => {
    //..
})
```
* 위 코드는 유닛 테스트를 정의하는 단계이다. `it()` 메소드는 사용하기 위해 따로 가져올 필요가 없는데, 그 이유는   
  `it()` 메소드는 Jest 패키지에 의해 전역으로 정의돼 있기 때문이다.
* `it()` 메소드의 첫 번째 인자로는 이 테스트가 하고자 하는 바의 의미 있는 설명이 들어가야 한다.   
  그 다음에는 Enzyme 패키지로부터 가져온 `shallow()` 메소드를 사용하는 구문이다.
```js
//...
const wrapper = shallow(<App/>);
```
* `shallow()` 메소드는 컴포넌트 element를 인자로 받는다. 컴포넌트는 인스턴스화된 다음, 생명주기에 들어서며,   
  그 컨텐츠가 rendering된다. __그러나 shallow rendering에선 자식 컴포넌트가 rendering에서 배제되며,__   
  __그 element들은 App 컴포넌트의 출력 결과에 그대로 남아있게 된다__. 즉, __App 컴포넌트의 props와__   
  __상태 데이터는 컨텐츠 rendering이 사용되나, 자식 컴포넌트는 처리하지 않는다__ 는 말이다.
* 결과물은 테스트를 위해 조사될 수 있는 wrapper 객체가 보여준다. Enzyme 패키지는 DOM으로부터 rendering된 컨텐츠를   
  조사할 때 사용할 수 있는, jQuery의 DOM Manipulation API를 모델로 한 메소드들을 제공한다.   
  그 중 많이 사용되는 메소드들은 다음과 같다.

<table>
    <tr>
        <td>find(selector)</td>
        <td>CSS Selector에 부합하는, 즉 Element 타입, 속성, 클래스가 모두 일치하는 모든 Element를 찾는다.</td>
    </tr>
    <tr>
        <td>findWhere(predicate)</td>
        <td>지정된 서술 함수(predicate)에 부합하는 모든 Element를 찾는다.</td>
    </tr>
    <tr>
        <td>first(selector)</td>
        <td>selector에 부합하는 첫 번째 Element를 반환한다. selector 생략 시에는 무조건 첫 번째 Element가 반환된다.</td>
    </tr>
    <tr>
        <td>children()</td>
        <td>현재 element의 자식을 포함하는 새 Wrapper 객체를 만든다.</td>
    </tr>
    <tr>
        <td>hasClass(class)</td>
        <td>현재 element가 지정된 클래스에 해당한다면 true를 반환한다.</td>
    </tr>
    <tr>
        <td>text()</td>
        <td>현재 element의 text 컨텐츠를 반환한다.</td>
    </tr>
    <tr>
        <td>html()</td>
        <td>얕은 rendering이 아닌, 전체 rendering을 한 컴포넌트 컨텐츠를 반환한다. 즉, 모든 자식 컴포넌트도 처리된다.</td>
    </tr>
    <tr>
        <td>debug()</td>
        <td>얕은 rendering을 한 컴포넌트 컨텐츠를 반환한다.</td>
    </tr>
</table>

* 위의 메소드들은 컴포넌트가 rendering한 컨텐츠를 탐색하고 조사하기 위해 사용된다.
```js
const valCount = wrapper.find(valueInput).length;
```
* 위 코드는 `find()` 메소드를 이용해 `App` 컴포넌트가 rendering하는 모든 `ValueInput` element를 찾고,   
  찾은 element의 개수를 length 프로퍼티로 읽어낸다.

```js
expect(valCount).toBe(3);
```
* 위 코드는 테스트의 마지막 작업으로, __예상 결과와 실제 결과를 비교하는 일__ 이다.   
  이는 Jest의 `expect()`라는 전역 메소드를 사용해 가능하다.
* 테스트 결과는 `expect()` 메소드에 전달되며, 그에 대해 `toBe()`라는 비교자(Matcher) 메소드가 호출된다.   
  Jset는 다양한 비교자 메소드들을 제공하는데, 유용한 메소드들은 다음과 같다.

<table>
    <tr>
        <td>toBe(value)</td>
        <td>결과가 지정한 value와 같은지 확인한다. 두 비교 대상이 객체일 필요는 없다.</td>
    </tr>
    <tr>
        <td>toEqual(object)</td>
        <td>결과가 지정한 값과 같은지 확인한다. 두 비교 대상이 동일한 객체 유형이어야 한다.</td>
    </tr>
    <tr>
        <td>toMatch(regexp)</td>
        <td>결과가 지정한 정규표현식에 부합하는지 확인한다.</td>
    </tr>
    <tr>
        <td>toBeDefined()</td>
        <td>결과가 정의돼 있는지 확인한다.</td>
    </tr>
    <tr>
        <td>toBeUndefined()</td>
        <td>결과가 아직 정의돼있지 않은지 확인한다.</td>
    </tr>
    <tr>
        <td>toBeNull()</td>
        <td>결과가 null인지 확인한다.</td>
    </tr>
    <tr>
        <td>toBeTruthy()</td>
        <td>결과가 참 계열(truthy)인지, 즉 true로 간주될 수 있는지 확인한다.</td>
    </tr>
    <tr>
        <td>toBeFalsy()</td>
        <td>결과가 거짓 계열(falsy)인지, 즉 false로 간주될 수 있는지 확인한다.</td>
    </tr>
    <tr>
        <td>toContain(substring)</td>
        <td>지정한 substring을 포함하고 있는지 확인한다.</td>
    </tr>
    <tr>
        <td>toBeLessThan(value)</td>
        <td>결과가 지정한 값보다 작은지 확인한다.</td>
    </tr>
    <tr>
        <td>toBeGreaterThan(value)</td>
        <td>결과가 지정한 값보다 큰지 확인한다.</td>
    </tr>
</table>

* Jest는 어떤 테스트가 실패했는지 추적해, 프로젝트의 모든 테스트 실행이 완료되면 그 결과를 보고한다.
<hr/>

<h2>전체 rendering을 사용한 컴포넌트 테스트</h2>

* p.662