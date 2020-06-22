Application 컴포지션
======

* Application Composition의 맥락 잡기
<table>
    <tr>
        <td>그게 무엇인가?</td>
        <td>복잡한 기능을 만들기 위한 컴포넌트의 조합을 말한다.</td>
    </tr>
    <tr>
        <td>왜 유용한가?</td>
        <td>컴포지션은 작고 간단한 컴포넌트들을 작성해 그들을 조합하기 전에 개별적으로 테스트하게 함으로써 app개발을 쉽게 해준다.</td>
    </tr>
    <tr>
        <td>어떻게 사용하는가?</td>
        <td>여러 패턴이 있지만, 기본은 여러 컴포넌트들을 결합하는 갓이다.</td>
    </tr>
<table>

<hr/>

<h2>기본 컴포넌트 관계</h2>

* React 개발에서, 부모 컴포넌트는 데이터 props로 자식 컴포넌트를 설정하고,   
  함수 props를 통해 알림을 받아 상태 데이터를 변경하는 것은 비일비재하다.
* 이러한 패턴은 간단한 상황에서는 이해하기 쉬우나, 좀 더 복잡한 상황에서는 사용법이   
  덜 분명하며, 코드와 데이터의 중복 없이 상태 데이터, props, callback함수를 위치시키고   
  분배하는 방법을 알기에는 너무 어렵다.
<hr/>

<h2>children prop 사용하기</h2>

* React는 부모가 제공한 컨텐츠를 보여줘야 하지만 그 컨텐츠가 뭔지 미리 알 수 없는   
  경우에 사용할 수 있는 __children__ 이라는 특별한 prop을 제공한다.
* 이는 app전반에 걸쳐 재사용할 수 있는 기능을 컨테이너 내에 표준화함으로써   
  코드의 중복을 줄일 수 있는 유용한 방법이다.
```js
// src/ThemeSelector.js
import React, {Component} from 'react';

export class ThemeSelector extends Component {
    render() {
        return (
            <div className="bg-dark p-2">
                <div className="bg-info p-2">
                    {this.props.children}
                </div>
            </div>
        )
    }
}
```
* 위 컨테이너 컴포넌트는 __children__ prop값을 갖는 표현식을 포함하는 2개의   
  div 요소를 rendering한다.
```js
// src/App.js
import React, {Component} from 'react';
import {ActionButton} from './ActionButton';
import {Message} from './Message';
import {ThemeSelector} from './ThemeSelector';

export default class App extends Component {
  constructor(props){
    super(props);
    this.state = {
      counter:0
    }
  }

  incrementCounter = () => {
    this.setState({counter:this.state.counter + 1})
  }

  render() {
    return (
      <div className="m-2 text-center">
        <ThemeSelector>
          <Message theme="primary" message={`Counter: ${this.state.counter}`}/>
          <ActionButton theme="secondary" text="Increment" callback={this.incrementCounter}/>
        </ThemeSelector>
      </div>
    )
  }
}
```
* App 컴포넌트는 ThemeSelector 컨테이너 컴포넌트의 처음과 끝 사이에 Message와   
  ActionButton 컴포넌트들 컨텐츠로 넣었다. React는 App컴포넌트가 rendering하는   
  컨텐츠를 처리할 때, ThemeSelector 태그 사이의 컨텐츠르 __props.children__ 프로퍼티에 할당한다.

<hr/>

<h3>children prop 다루기</h3>

* __children__ prop은 컴포넌트가 자식에게 서비스를 제공할 수 있을 때 유용하지만,   
  자식이 제공하는 것에 대해 알지 못하는 경우엔 사용하기 어렵다. 이를 극복하기 위해   
  react는 컨테이너가 자식을 다룰 때 사용할 수 있는 다음의 메소드를 제공한다.

* 컨테이너 메소드
<table>
    <tr>
        <td>React.Children.map</td>
        <td>각 자식에 대해 함수롤 호출하고, 그 결과들을 배열로 반환한다.</td>
    </tr>
    <tr>
        <td>React.Children.forEach</td>
        <td>각 자식에 대해 함수롤 호출하지만 배열을 반환하지는 않는다.</td>
    </tr>
    <tr>
        <td>React.Children.count</td>
        <td>자식의 개수를 반환한다.</td>
    </tr>
    <tr>
        <td>React.Children.only</td>
        <td>단 하나의 자식이 아닐 경우 오류를 발생시킨다.</td>
    </tr>
    <tr>
        <td>React.Children.toArray</td>
        <td>자식의 배열을 반환하는데, element를 재정렬하거나 부분 제거할 때 유용하다.</td>
    </tr>
    <tr>
        <td>React.cloneElement</td>
        <td>자식 element를 복제하며, 새로운 props의 추가도 가능하다.</td>
    </tr>
</table>

<hr/>

<h3>컨테이너에 props 추가</h3>

* 컴포넌트는 부모로부터 받은 컨텐츠를 직접 조작할 수 없다. 따라서 children prop을   
  통해 받은 컨텐츠에 데이터나 함수를 추가하려면, __React.Children.map__ 메소드와 __React.cloneElement__   
  메소드를 함께 사용해 자식 컴포넌트를 복제하고 추가 props를 할당해야 한다.
```js
// src/ThemeSelector.js
import React, {Component} from 'react';

export class ThemeSelector extends Component {

    constructor(props) {
        super(props);
        this.state= {
            theme:"primary"
        }

        this.themes = ["primary", "secondary", "success", "warning", "dark"];
    }

    setTheme = (event) => {
        this.setState({theme:event.target.value})
    }

    render() {

        let modChildren = React.Children.map(this.props.children, 
            (c => React.cloneElement(c, {theme:this.state.theme})));
        
        return (
            <div className="bg-dark p-2">
                <div className="form-group text-left">
                    <label className="text-white">Theme:</label>
                    <select className="form-control" value={this.state.theme}
                        onChange={this.setTheme}>
                        {this.themes.map(theme => <option key={theme} value={theme}>{theme}</option>)}
                    </select>
                </div>

                <div className="bg-info p-2">
                    {modChildren}
                </div>
            </div>
        )
    }
}
```
* props는 읽기 전용이기 때문에 단순히 __React.Children.forEach__ 메소드를 사용해   
  자식 컴포넌트들을 열거하고, 그 컴포넌트들의 props객체에 새 프로퍼티를 할당하는 일은   
  불가하다. 따라서 그 대신 __React.Children.map__ 메소드를 사용해 자식들을 열거하고   
  __React.cloneElement__ 메소드로 추가 prop과 함께 각 자식들을 복제했다.
```js
let modChildren = React.Children.map(this.props.children,
(c => React.cloneElement(c, {theme:this.state.theme})));
```
* 위 코드에서 cloneElement는 자식 컴포넌트와 props 객체를 받는데, 이 props는 자식   
  컴포넌트의 기존 props와 병합된다.
<hr/>

<h3>컴포넌트의 재정렬과 부분 제거</h3>

```js
// src/ThemeSelector.js

import React, {Component} from 'react';

export class ThemeSelector extends Component {

    constructor(props) {
        super(props);
        this.state= {
            theme:"primary",
            reverseChildren:false
        }

        this.themes = ["primary", "secondary", "success", "warning", "dark"];
    }

    setTheme = (event) => {
        this.setState({theme:event.target.value})
    }

    toggleReverse = () => {
        this.setState({reverseChildren : !this.state.reverseChildren})
    }

    render() {

        let modChildren = React.Children.map(this.props.children, 
            (c => React.cloneElement(c, {theme:this.state.theme})));

        if(this.state.reverseChildren) {
            modChildren.reverse();
        }
        
        return (
            <div className="bg-dark p-2">
                <button className="btn btn-primary" onClick={this.toggleReverse}>
                    Reverse
                </button>
                <div className="form-group text-left">
                    <label className="text-white">Theme:</label>
                    <select className="form-control" value={this.state.theme}
                        onChange={this.setTheme}>
                        {this.themes.map(theme => <option key={theme} value={theme}>{theme}</option>)}
                    </select>
                </div>

                <div className="bg-info p-2">
                    {modChildren}
                </div>
            </div>
        )
    }
}
```
* ThemeSelector 컴포넌트의 button은 자식들의 순서를 뒤집는데, 이는 map 메소드가   
  반환한 배열에 대해 reverse 메소드를 호출하는 방법으로 구현했다.
<hr/>

<h2>특성화 컴포넌트</h2>

* 어떤 컴포넌트는 다른 평범한 컴포넌트가 제공하는 기능의 특성화된 버전을 제공할 수 있다.   
  일부 프레임워크의 경우, 클래스 상속 같은 기능을 사용해 특성화를 하지만, React는   
  평범한 컴포넌트를 renering하면서 props를 사용해 관리하는 __특성화 컴포넌트(Specialized Component)__   
  를 사용한다.

```js
// src/GeneralList.js
import React, {Component} from 'react';

export class GeneralList extends Component {
    render() {
        return (
            <div className={`bg-${this.props.theme} text-white p-2`}>
                {this.props.list.map((item, index) =>
                    <div key={item}>{index + 1}: {item}</div>
                )}
            </div>
        )
    }
}
```
* 위 컴포넌트는 __list__ 라는 이름의 prop을 받고, 그에 대해 map 메소드를 사용하여   
  일련의 div element를 rendering 한다.

```js
// src/SortedList.js

import React, {Component} from 'react';
import {GeneralList} from './GeneralList';
import {ActionButton} from './ActionButton';

export class SortedList extends Component {
    constructor(props) {
        super(props);
        this.state={
            sort:false
        }
    }

    getList(){
        return this.state.sort ? [...this.props.list].sort() : this.props.list;
    }

    toggleSort = () => {
        this.setState({sort : !this.state.sort});
    }

    render() {
        return(
            <div>
                <GeneralList list={this.getList()} theme="info"/>
                <div className="text-center m-2">
                    <ActionButton theme="primary" text="Sort" callback={this.toggleSort}/>
                </div>
            </div>
        )
    }
}
```
* SortedList는 자신의 결과의 일부로서 GeneralList를 rendering 한다. 또한 list prop을   
  사용해 사용자가 목록의 정렬 여부를 선택할 수 있게 함으로써 데이터를 보여주는 방식을   
  제어한다.
<hr/>

<h2>고차 컴포넌트(HOC, High-Order Component)</h2>

* 고차 컴포넌트는 특성화 컴포넌트의 대안으로서, 컴포넌트에 공통 코드는 필요하지만   
  연관 컨텐츠의 rendering은 필요 없는 경우에 유용하다.
* HOC는 __횡단 관심사(Cross-cutting Concern)__ 의 구현에 자주 사용되는데,   
  횡단 관심사란 app전반을 가로지르는 공통의 작업을 의미한다.
* 횡단 관심사의 개념이 없다면 동일한 코드가 여러 곳에서 구현될 여지가 많을 것이다.
* 가장 흔히 볼 수 있는 횡단 관심사의 예는 보안 기능, logging, 데이터 검색 등이다.
* HOC는 하나의 컴포넌트를 받아 __추가 기능을 입힌 새로운 하나의 컴포넌트를 반환__ 하는 함수다.
```js
// src/ProFeature.js
import React, {Component} from 'react';

export function ProFeature(FeatureComponent) {
    return function(props) {
        if(props.pro){
            let {pro, ...childProps} = props;
            return <FeatureComponent {...childProps}/>
        } else {
            return (
                <h5 className="bg-warning text-white text-center">
                    This is a Pro Feature.
                </h5>
            )
        }
    }
}
```
* 위 코드의 HOC는 ProFeature라는 함수인데, 이는 pro라는 이름의 prop이 true일 때만   
  사용자에게 보여야 할 컴포넌트를 하나 받는다. 즉, 일종의 단순한 권한 관리 기능이다.   
  컴포넌트를 보여주기 위해 이 함수는 인자로 받은 컴포넌트를 사용해 pro를 제외한   
  모든 props를 전달한다.
* 만약 pro prop값이 false라면 ProFeature는 경고 메시지를 보여주는 헤더 element를 반환한다. 
* HOC를 사용하려면 다음과 같이 해당 HOC함수를 호출해 새로운 컴포넌트를 만들면 된다.
```js
const ProList = ProFeature(SortedList);
```
* HOC는 함수이므로 동작을 설정하기 위해 인자들을 더 추가할 수 있다.
<hr/>

<h3>상태 유지 HOC</h3>

* HOC도 상태 유지 컴포넌트가 될 수 있으며, 그렇게 함으로써 app에 좀 더 복잡한   
  기능을 추가할 수 있다.
```js
// src/ProController.js

import React, {Component} from 'react';
import {ProFeature} from './ProFeature';

export function ProController(FeatureComponent) {

    const ProtectedFeature = ProFeature(FeatureComponent);

    return class extends Component {
        constructor(props){
            super(props);
            this.state = {
                proMode:false
            }
        }

        toggleProMode = () => {
            this.setState({
                proMode = !this.state.proMode
            })
        }

        render() {
            return (
                <div className="container-fluid">
                    <div className="row">
                        <div className="col-12 text-center p-2">
                            <input type="checkbox" className="form-check-input"
                                value={this.state.proMode} onChange={this.toggleProMode}/>
                            <label className="form-check-label">Pro Mode</label>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col-12">
                            <ProtectedFeature {...this.props}
                                pro={this.state.proMode}/>
                        </div>
                    </div>
                </div>
            )
        }
    }
}
```
* 위 HOC 함수는 클래스 기반의 상태 유지 컴포넌트를 반환한다.
* 이 컴포넌트는 체크박스를 보여주며, wrapping된 컴포넌트의 가시성을   
  제어하기 위해 ProFeature HOC를 사용한다.
<hr/>

<h3>HOC의 조합</h3>

* HOC의 유용함 중 하나는 wrapping된 컴포넌트 클래스를 생성하는 함수 호출만   
  변경함으로써 서로 조합할 수 있단느 점이다.

<hr/>

<h2>Render Prop</h2>

* Rendering Prop은 rendering돼야 할 컨텐츠를 컴포넌트에 제공하는 함수prop이며,   
  이는 한 컴포넌트가 다른 컴포넌트를 wrapping하는 또 하나의 방법이다.

* 이 전의 ProFeature 컴포넌트가 rendering prop을 사용하도록 변경해보자.
```js
// src/ProFeature.js
import React, {Component} from 'react';

export function ProFeature(props) {
    if(props.pro) {
        return props.render();
    } else {
        return(
            <h5 className="bg-warning text-white text-center">
                This is a Pro Feature.
            </h5>
        )
    }
}
```
* Rendering prop을 사용하는 컴포넌트도 일반적인 방법으로 정의하면 된다.   
  단지 다른점은 부모가 제공한 컨텐츠를 보여주기 위해 __render라는 이름의__   
  __함수prop을 호출하는 점__ 이다.
* 부모 컴포넌트는 자식 컴포넌트를 적용할 때 rendering prop을 위한 함수를 제공해야 한다.
<hr/>

<h3>인자가 있는 rendering prop</h3>

* Rendering prop은 보통의 JS함수이며, 따라서 인자도 받을 수 있다.
* 인자를 사용하면 rendering prop을 호출하는 컴포넌트가 자신을 wrapping하는   
  컨텐츠에 props를 전달할 수 있다.
```js
// src/ProFeature.js

import React, {Component} from 'react';

export function ProFeature(props) {
    if(props.pro) {
        return props.render("PRO FEATURE");
    } else {
        return(
            <h5 className="bg-warning text-white text-center">
                This is a Pro Feature.
            </h5>
        )
    }
}
```

```js
// src/App.js

// 기타 코드 동일

export default class App extends Component{
    //...

    render() {
    return (
        <div className="container-fluid">
          <div className="row">
            <div className="col-12 text-center p-2">
              <div className="form-check">
                 <input type="checkbox" className="form-check-input"
                   value={this.state.proMode} onChange={this.toggleProMode}/>
                 <label className="form-check-label">Pro Mode</label>
              </div>
            </div>
          </div>
          <div className="row">
            <div className="col-6">
             <ProFeature pro={this.state.proMode}
              render={(text)=>
               <React.Fragment>
                 <h4 className="text-right">{text}</h4>
                 <SortedList list={this.state.names}/>
               </React.Fragment>}
               />
            </div>
          </div>
        </div>
    )   
  }

}
```

<hr/>

<h2>전역 데이터를 위한 컨텍스트</h2>

* Application Composition과 관계없이 props를 관리하는 app의 복잡도가  
  증가함에 따라 어려워진다. 컴포넌트의 계층도가 커짐에 따라 상태 data는   
  app내에서 점점 더 끌어 올려지며, 그 결과 모든 컴포넌트가 자신이 직접   
  사용하지도 않는 props를 후손을 위해 전달하게 된다.
* 이의 해결을 위해 React는 __Context__ 기능을 제공한다.

* Context는 상태 데이터가 정의된 곳으로부터 필요로하는 곳까지 중간 컴포넌트들을   
  거치지 않고 전달되게 해준다.
```js
// src/ActionButton.js
import React, {Component} from 'react';

export class ActionButton extends Component {

    render() {
        return (
            <button className={this.getClasses(this.props.proMode)}
                disabled={!this.props.proMode}
                onClick={this.props.callback}>
                    {this.props.text}
            </button>
        )
    }


    getClasses(proMode) {
        let col = proMode ? this.props.theme : "danger";
        return `btn btn-${col} m-2`;
    }
}
```
* ActionButton이 의존하는 proMode 프로퍼티는 App 컴포넌트의 상태 일부분으로  
  사용될 것이다. App컴포넌트는 또한 proMode 값을 변경할 때 사용할 checkbox도   
  정의할 것이다.
* 컴포넌트 사슬의 결과, 부모로부터 받은 proMode 프로퍼티를 자식에게 전달하게 된다.
* 이는 곧 props가 `App 컴포넌트` 에서 `SortedList 컴포넌트` 로, 그리고 다시   
  `SortedList 컴포넌트` 에서 `ActionButton 컴포넌트` 로 전달된다는 뜻이다.

* 위와 같은 구조를 __prop drilling 또는 prop threading__ 이라 하는데, 이는 곧 데이터 값을   
  필요로하는 곳으로 컴포넌트 계층도를 통해 prop이 전달되는 것을 의미한다.
* 하지만 prop drilling은 위에서 얘기한대로, 계층도가 복잡해지면 구현이 복잡해진다.
* 이를 해결하기 위해 등장한 것이 __Context__ 이다.
<hr/>

<h3>컨텍스트 정의</h3>

* Context는 Application의 어느 곳에서든 정의할 수 있다.
```js
// src/ProModeContext.js
import React from 'react';

export const ProModeContext = React.createContext({
    proMode:false
})
```
* 위와 같이 새 context를 만들 때엔 __React.createContext__ 메소드를 사용하며,   
  컨텍스트의 기본값을 지정하기 위한 데이터 객체를 넣을 수 있다.
* __이 객체의 값은 컨텍스트가 사용되는 곳에서 바뀔 수 있다__.
<hr/>

<h3>컨텍스트 소비자</h3>

* 컨텍스트 소비자(Context consumer) : 데이터 값이 필요한 곳에서 context를 소비하는 것
```js
// src/ActionButton.js

import React, {Component} from 'react';
import {ProModeContext} from './ProModeContext';

export class ActionButton extends Component {

    render() {
        return (
            <ProModeContext.Consumer>
                {contextData =>
                    <button className={this.getClasses(this.props.proMode)}
                        disabled={!this.props.proMode}
                        onClick={this.props.callback}>
                        {this.props.text}
                    </button>
                }
            </ProModeContext.Consumer>
        )
    }

    getClasses(proMode) {
        let col = proMode ? this.props.theme : "danger";
        return `btn btn-${col} m-2`;
    }
}
```
* Context를 소비하는 방법은 rendering prop을 정의할 때와 비슷한데,   
  context를 필요로 하는 HTML element를 추가하면 된다.
* 우선 __context이름에 해당하는 HTML Element를(여기서는 ProModeContext)를__,   
  __그 다음엔 마침표를, 마지막엔 Consumer를 적는다.__
* 위 HTML Element의 시작과 끝 태그 사이엔 __컨텍스트 객체를 받고, 그와 함께 컨텐츠를__   
  __rendering하는 함수를 넣는다__.
```js
return <ProModeContext.Consumer>
    // Context가 소비되는 부분
    </ProModeContext.Consumer>
```
<hr/>

<h3>컨텍스트 제공자</h3>

* Context Provider : 컨텍스트에 상태 데이터를 결부시킨다.
```js
import React, {Component} from 'react';
import {GeneralList} from './GeneralList';
import {SortedList} from './SortedList';
import {ProFeature} from './ProFeature';
import { ProModeContext } from './ProModeContext';

export default class App extends Component {
  constructor(props){
    super(props);
    this.state = {
      names:["Zoe", "Bob", "Alice", "Doom", "Jack"],
      cities:["London", "New York", "Paris", "Seoul", "Boston"],
      proContextData:{
        proMode:false
      }
    }
  }

  toggleProMode = () => {
    this.setState(state => state.proContextData.promo != state.proContextData.proMode);
  }

  render() {
    return (
     <div className="container-fluid">
       <div className="row">
         <div className="col-12 text-center p-2">
           <div className="form-check">
              <input type="checkbox" className="form-check-input"
                value={this.state.proContextData.proMode} onChange={this.toggleProMode}/>
              <label className="form-check-label">Pro Mode</label>
           </div>
         </div>
       </div>
       <div className="row">
         <div className="col-6">
          <ProModeContext.Provider value={this.state.proContextData}>
            <SortedList list={this.state.names}/>
          </ProModeContext.Provider>
       </div>
     </div>
    </div>
    )
  }
}
```
* Context 소비자에게 App컴포넌트의 모든 상태 데이터를 노출하지 않기 위해   
  proMode 프로퍼티를 갖는 proContextData 상태 객체를 만들었다.
* Context를 적용하려면 또 다른 Custom HTML Element를 사용해야 하는데, 먼저   
  __컨텍스트 이름(위에선 ProModeContext)를, 그 다음엔 마침표를, 마지막으로__   
  __Provider 를 적는다.__
* 위 코드의 구조는 다음과 같다.
  * ProModeContext.Providor의 시작과 끝 태그 사이에 정의된 컴포넌트는   
    ProModeContext.Consumer element를 이용해 상태 데이터에 접근할 수 있다.
  * 즉, App컴포넌트의 proMode 상태 데이터 프로퍼티를 ActionButton컴포넌트가   
    SortedList 컴포넌트를 거치지 않고도 직접 사용할 수 있다는 뜻이다.
<hr/>

<h3>컨텍스트 데이터 변경</h3>

* 컨텍스트 내의 데이터는 읽기 전용이지만, __함수 prop을 컨텍스트 객체에 포함시켜__   
  __상태 데이터를 갱신할 수 있다__.
```js
// src/ProModeContext.js
import React from 'react';

export const ProModeContext = React.createContext({
    proMode:false,
    toggleProMode: () => {}
})
```
* toggleProMode는 context 공급업체가 value 프로퍼티를 사용하지 않고 컨텐츠를  
  적용할 때 사용될 수 있는 임시 역할의 함수이다.
* toggleProMode는 빈 함수이며, 오직 소비자로부터 기본 데이터 객체를 받았을   
  경우의 에러를 방지한다.
```js
// src/ProModeToggle.js
import React, {Component} from 'react';
import {ProModeContext} from './ProModeContext';

export class ProModeToggle extends Component {
    render() {
        return(
            <ProModeContext.Consumer>
                {contextData => (
                    <div className="form-check">
                        <input type="checkbox" className="form-check-input"
                            value={contextData.proMode}
                            onChange={contextData.toggleProMode}/>
                        <label className="form-check-label">
                            {this.props.label}
                        </label>
                    </div>
                )}
            </ProModeContext.Consumer>
        )
    }
}
```
* 위 컴포넌트는 컨텍스트 소비자로서, proMode 프로퍼티를 사용해 체크박스의   
  값을 설정하고, 그 값이 바뀌면 toggleProMode함수를 호출한다.
```js
// src/App.js
import React, {Component} from 'react';
import {SortedList} from './SortedList';
import { ProModeContext } from './ProModeContext';
import {ProModeToggle} from './ProModeToggle';

export default class App extends Component {
  constructor(props){
    super(props);
    this.state = {
      names:["Zoe", "Bob", "Alice", "Doom", "Jack"],
      cities:["London", "New York", "Paris", "Seoul", "Boston"],
      proContextData:{
        proMode:false,
        toggleProMode:this.toggleProMode
      },
      superProContextData:{
        proMode:false,
        toggleProMode:this.toggleSuperMode
      }
    }
  }

  toggleProMode = () => {
    this.setState(state => state.proContextData.proMode = !state.proContextData.proMode);
  }

  toggleSuperMode = () => {
    this.setState(state => state.superProContextData.proMode = !state.superProContextData.proMode)
  }

  render() {
    return (
     <div className="container-fluid">
       <div className="row">
         <div className="col-6 text-center p-2">
           <ProModeContext.Provider value={this.state.proContextData}>
             <ProModeToggle label="PRO MODE"/>
          </ProModeContext.Provider>
         </div>
         <div className="col-6 text-center p-2">
           <ProModeContext.Provider value={this.state.superProContextData}>
             <ProModeToggle label="SUPER PRO MODE"/>
           </ProModeContext.Provider>
         </div>
       </div>
       <div className="row">
         <div className="col-6">
           <ProModeContext.Provider value={this.state.proContextData}>
             <SortedList list={this.state.names}/>
           </ProModeContext.Provider>
         </div>
         <div className="col-6">
           <ProModeContext.Provider value={this.state.superProContextData}>
             <SortedList list={this.state.cities}/>
           </ProModeContext.Provider>
         </div>
       </div>
     </div>
    )
  }
}
```
<hr/>

<h3>컨텍스트 API 사용</h3>
