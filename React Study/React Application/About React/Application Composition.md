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

* p.531