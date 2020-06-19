Event
======

<h2>이벤트의 이해</h2>

* 이벤트는 사용자가 버튼을 클릭하거나 텍스트를 입력하는 등의 중요한 변화를 알리기   
  위해 HTML Element가 발생시킨다.
* React에서 이벤트를 다루는 일은 DOM API를 사용하는 경우와 비슷하지만,   
  몇 가지 중요한 차이점이 있다.

```js
// src/App.js
import React, {Component} from 'react';

export default class App extends Component {

  constructor(props) {
    super(props);
    this.state = {
      message:"Ready"
    }
  }

  render() {
    return (
      <div className="m-2">
        <div className="h4 bg-primary text-white text-center p-2">
          {this.state.message}
        </div>
        <div className="text-center">
          <button className="btn btn-primary"
            onClick={()=>this.setState({message:"Clicked!"})}>
              Click Me
          </button>
        </div>
      </div>
    )
  }
}
```
* 위 코드는 버튼에 대해 click 이벤트 핸들러 함수를 추가한 것이다.
* 이벤트는 그에 대응하는 DOM API 프로퍼티를 Camel Casing으로 표현한   
  이름의 프로퍼티를 사용해 처리할 수 있다.
* 즉, DOM API의 __onclick__ 프로퍼티는 React Application에서   
  __onClick__ 으로 표기된다.
<hr/>

<h3>이벤트 처리 메소드 사용</h3>

* 상태 유지 컴포넌트에는 메소드를 정의할 수 있으며, 이를 사용해   
  이벤트에 응답할 수 있다.
* 이렇게 하면 동일한 이벤트에 대해 동일한 방식으로 처리하는 여러   
  element들이 있는 상황에서 코드의 중복을 방지할 수 있다.
* App의 상태를 변경하지 않거나 다른 컴포넌트의 기능에 접근하지 않는   
  간단한 메소드의 경우에는 다음과 같이 정의할 수 있다.
```js
// src/App.js

// 기타 코드 모두 동일

export default class App extends Component {
    //..

    handleEvent() {
        console.log('handleEvent method invoked.');
    }

    render() {
        return (
            //...
            <button className="btn btn-primary">
                onClick={this.handleEvent}>
                Click Me
            </button>
        )
    }
}
```
* 위 코드에서는 __{this.handleEvent()}__ 와 다르게 ()를 표기하지 않았다.
* 만약 위와 같이 뒤에 ()를 사용한다면, 이는 __컴포넌트 객체가 생성될 때__   
  __handleEvent 메소드가 호출되는 결과__ 를 낳는다.
<hr/>

<h3>이벤트 핸들러 내에서의 컴포넌트 기능 접근</h3>

* 이벤트 핸들러 내에서 컴포넌트의 기능에 접근하려면 추가 작업이 필요하다.
* __this__ 키워드의 값은 JS 클래스 메소드가 호출될 때 기본적으로 호출되는   
  것이 아니다. 즉, 이벤트 핸들러 내에서 컴포넌트의 메소드나 프로퍼티에   
  접근할 수 있는 방법이 없다는 뜻이다.
```js
// src/App.js

// 기타 코드 모두 동일

// 예시 1
handleEvent() {
    this.setState({message:"Clicked!"});
}

// 예시 2
handleEvent = function() {
    this.setState({message:"Clicked!"});
}
```
* 위 두 예시는 모두 다음 예외가 발생한다.
```text
TypeError : Cannot read property 'setState' of undefined.
```
* 이를 해결하기 위해선 JS 클래스 필드 문법을 사용하여 이벤트 핸들러를 표현해야 한다.
```js
handleEvent = () => {
    this.setState({message:"Clicked!"});
}
```
<hr/>

<h3>이벤트 객체의 이해</h3>

* React는 이벤트가 발생하면 그 이벤트를 기술하는 __SyntheticEvent__ 라는 객체를 제공한다.
* SyntheticEvent는 DOM API가 제공하는 Event객체의 wrapper로서, 각기 다른 브라우저에서도   
  일관되게 이벤트를 기술하기 위한 코드가 추가된 객체이다.
* SyntheticEvent 객체에 포함된 기본적인 프로퍼티와 메소드는 다음과 같다.

<table>
    <tr>
        <td>nativeEvent</td>
        <td>이 프로퍼티는 DOM API가 제공하는 Event 객체를 반환한다.</td>
    </tr>
    <tr>
        <td>target</td>
        <td>이 프로퍼티는 이벤트의 출처인 element를 나타내는 객체를 반환한다.</td>
    </tr>
    <tr>
        <td>timeStamp</td>
        <td>이 프로퍼티는 이벤트가 발생된 시각을 나타내는 timestamp를 반환한다.</td>
    </tr>
    <tr>
        <td>type</td>
        <td>이 프로퍼티는 이벤트 타입을 나타내는 문자열을 반환한다.</td>
    </tr>
    <tr>
        <td>isTrusted</td>
        <td>이 프로퍼티는 이벤트가 브라우저에 의해 촉발됐을 땐 true를, 코드 내에서 생성된 이벤트라면 false를 반환한다.</td>
    </tr>
    <tr>
        <td>preventDefault()</td>
        <td>이 메소드는 이벤트의 기본 동작을 취소한다.</td>
    </tr>
    <tr>
        <td>defaultPrevented</td>
        <td>이 프로퍼티는 해당 이벤트 객체에 대헤 preventDefault() 메소드가 호출됐는지의 유무를 반환한다.</td>
    </tr>
    <tr>
        <td>persist()</td>
        <td>이 메소드는 이벤트 객체의 재사용을 위해 사용된다.</td>
    </tr>
</table>

<hr/>

<h3>이벤트 재사용의 함정</h3>

* React는 일단 이벤트 하나를 처리하면 SyntheticEvent객체를 재사용하며,   
  모든 프로퍼티를 null로 초기화한다.
* 위의 경우를 극복하기 위해 __persist()__ 메소드를 사용하는 것이다.
<hr/>

<h3>이벤트 핸들러 호출에 커스텀 인자 사용</h3>

* React가 기본적으로 제공하는 SyntheticEvent 객체 대신, 커스텀 인자를   
  사용해 이벤트 핸들러를 호출하는 방법이 더 유용한 경우가 있다.
```js
// src/App.js

import React, {Component} from 'react';

export default class App extends Component {

  constructor(props) {
    super(props);
    this.state = {
      message:"Ready",
      counter:0,
      theme:"secondary"
    }
  }

  handleEvent = (event) => {
    event.persist();
    this.setState({
      counter: this.state.counter + 1,
      theme : event.target.innerText === "Normal" ? "primary" : "danger"
    }, () => this.setState({message:`${event.type}: ${this.state.counter}`}))
  }

  render() {
    return (
      <div className="m-2">
        <div className={`h4 bg-${this.state.theme} text-white text-center p-2`}>
          {this.state.message}
        </div>
        <div className="text-center">
          <button className="btn btn-primary"
            onClick={this.handleEvent}>
              Normal
          </button>
          <button className="btn btn-danger m-1"
            onClick = {this.handleEvent}>
            Danger
          </button>
        </div>
      </div>
    )
  }
}
```
* 위 코드의 문제점은 rendering되는 컨텐츠의 상당 부분을   
  이벤트 핸들러가 알아야 한다는 점이다.   
  위 코드의 경우 handleEvent는 이벤트의 근원지를 파악하기   
  위해 innerText 프로퍼티값을 알앙 하며 theme이라는 상태   
  데이터 프로퍼티의 값을 결정해야 한다.
* 이와 같은 방식은 컴포넌트가 rendering하는 컨텐츠가 변경되거나   
  동일한 결과를 만드는 여러 상호작용이 존재할 때 관리의 어려움을   
  야기한다.
```js
// src/App.js

// 기타 코드 동일
handleEvent = (event, newTheme) => {
    event.persist();
    this.setState({
      counter: this.state.counter + 1,
      theme : newTheme
    }, () => this.setState({message:`${event.type}: ${this.state.counter}`}))
  }

  render() {
    return (
      <div className="m-2">
        <div className={`h4 bg-${this.state.theme} text-white text-center p-2`}>
          {this.state.message}
        </div>
        <div className="text-center">
          <button className="btn btn-primary"
            onClick={(e) => this.handleEvent(e, "primary")}>
              Normal
          </button>
          <button className="btn btn-danger m-1"
            onClick={(e) => this.handleEvent(e, "danger")}>
            Danger
          </button>
        </div>
      </div>
    )
  }
```
* 위와 같이 코드를 수정하면, handleEvent 메소드가 theme 프로퍼티를   
  변경하기 위해 더 이상 이벤트를 발생시킨 element를 조사하지 않아도 된다.
<hr/>

<h3>기본 동작 취소</h3>

* 일부 이벤트들은 브라우저가 기본으로 수행하는 동작이 적용된다.   
  ex) checkbox 클릭 시 브라우저의 기본 동작은 checkbox의   
  상태를 toggle 하는 것이다.
* __preventDefault__ 메소드는 이벤트 객체를 이러한 기본   
  동작으로부터 보호할 수 있다.
```js
// src/App.js

// 기타 코드 동일

toggleCheckBox = (event) => {
    if(this.state.counter === 0) {
      event.preventDefault();
    }
  }

  render() {
    return (
      <div className="m-2">
        <div className="form-check">
          <input className='form-check-input' type="checkbox"
            onClick={this.toggleCheckBox}/>
          <label>This is a checkbox.</label>
        </div>
        <div className={`h4 bg-${this.state.theme} text-white text-center p-2`}>
          {this.state.message}
        </div>
        <div className="text-center">
          <button className="btn btn-primary"
            onClick={(e) => this.handleEvent(e, "primary")}>
              Normal
          </button>
          <button className="btn btn-danger m-1"
            onClick={(e) => this.handleEvent(e, "danger")}>
            Danger
          </button>
        </div>
      </div>
    )
  }
}
```
* 위 코드는 counter값이 0일 때 checkbox가 자동으로 toggle되는 것을   
  event.preventDefault()로 막은 것이다.
<hr/>

<h2>이벤트 전파</h2>

* 이벤트에는 __생명 주기__ 가 있다. 이로 인해 element의 조상이 후손이 일으킨   
  이벤트를 받을 수 있으며, 이벤트가 element에 도착하기 전에 가로채는   
  일도 가능하다.

* 이벤트 전파와 관련된 SyntheticEvent의 프로퍼티와 메소드
<table>
  <tr>
    <td>eventPhase</td>
    <td>이 프로퍼티는 현재의 이벤트 전파 단계를 반환한다. 그러나 react가 이벤트를 다루는 방식으로 인해 이 프로퍼티는 유용하지 않다.</td>
  </tr>
  <tr>
    <td>bubbles</td>
    <td>이 프로퍼티는 이벤트가 bubble단계에 진입할 상황이면 true를 반환한다.</td>
  </tr>
  <tr>
    <td>currentTarget</td>
    <td>이 프로퍼티는 이벤트 핸들러가 이벤트 처리를 할 대상 element를 나타내는 객체를 반환한다.</td>
  </tr>
  <tr>
    <td>stopPropagation()</td>
    <td>이 메소드는 이벤트 전파를 중단시킬 때 호출된다.</td>
  </tr>
  <tr>
    <td>isPropagationStopped()</td>
    <td>이 메소드는 이벤트에 대해 stopPropagation()이 호출됐다면 true를 반환한다.</td>
  </tr>
</table>

<hr/>

<h3>Target 단계와 Bubble 단계</h3>

* 처음 촉발된 이벤트는 먼저 __Target Phase__ 에 진입한다. 이는 이벤트의   
  원천인 element에 이벤트 핸들러가 적용되는 단계이다.
* 이벤트 핸들러의 실행이 완료된 다음에 이벤트는 __Bubble Phase__ 에 들어간다.   
  이는 이벤트가 조상 element를 거슬러 올라가면서 해당 유형의 이벤트에 적용되는   
  모든 이벤트 핸들러가 호출되는 단계이다.

* Bubble Phase는 컴포넌트가 rendering한 컨텐츠를 넘어 확장돼, HTML element   
  계층도 전체에 전파된다.

* SyntheticEvent 객체의 target, currentTarget 프로퍼티 차이점 간단 정리
  * target : 이벤트를 발생시킨 element 반환
  * currentTarget : 이벤트 핸들러를 호출한 element 반환
<hr/>

<h3>Capture 단계</h3>

* __Capture phase__ 는 Target phase보다 먼저 element가 이벤트를   
  처리할 수 있는 기회를 제공한다.
* 즉, 브라우저는 Bubble phase와는 반대로 body element에서 시작해   
  이벤트를 element 계층도로 따라 내려보내며 처리할 수 있게 한다.

```js
// src/ThemeButton.js

import React, {Component} from 'react';

export class ThemeButton extends Component {

    handleClick = (event) => {
        console.log(`ThemeButton : Type : ${event.type}` + 
        ` Target : ${event.target.tagName}` + 
        ` CurrentTarget : ${event.currentTarget.tagName}`);

        this.props.callback(this.props.theme);
    }

    render() {
        return <span className="m-1" onClick={this.handleClick}
            onClickCapture={this.handleClick}>
            <button className={`btn btn-${this.props.theme}`}
                onClick={this.handleClick}>
                    Select {this.props.theme} Theme
                </button>
        </span>
    }
}
```
* 위 코드에서는 onClick에 대응하는 캡처 프로퍼티가 __onClickCapture__   
  임을 알 수 있다. 그리고, 이를 
* 위 코드를 실행하고, ThemeButton 객체를 클릭하면 다음과 같이   
  콘솔에 출력된다.
```text
ThemeButton: Type: click Target: BUTTON CurrentTarget: SPAN
ThemeButton: Type: click Target: BUTTON CurrentTarget: BUTTON
ThemeButton: Type: click Target: BUTTON CurrentTarget: SPAN
App: Type: click Target: BUTTON CurrentTarget: DIV
App: Type: click Target: BUTTON CurrentTarget: DIV
```