재조정과 생명주기
======

* React의 __재조정(Reconciliation)__ 은 react가 컴포넌트에 제공하는   
  전체 생명주기의 일부이다.

* 재조정과 생명주기의 맥락
<table>
    <tr>    
        <td>그게 무엇인가?</td>
        <td>재조정은 DOM의 변경을 최소화함으로써 컴포넌트가 만든 컨텐츠를 효과적으로 처리하는 과정이며, 상태 유지 컴포넌트에
        적용되는 생명주기의 일부이다.</td>
    </tr>
    <tr>    
        <td>왜 유용한가?</td>
        <td>방대한 컴포넌트 생명주기가 app개발을 위한 일관된 모델과 고급 프로젝트를 위한 유용한 기능을 제공함에 있어서
        재조정 과정은 app의 성능을 관리한다.</td>
    </tr>
    <tr>    
        <td>어떻게 사용하는가?</td>
        <td>재조정 과정은 자동으로 수행되므로 명시적인 처리를 할 필요가 없다. 모든 상태 유지 컴포넌트는 동일한 생명주기를 거친다.
        클래스 기반의 컴포넌트의 경우엔 특별한 메소드들을, 함수형 컴포넌트의 경우에는 effect hook을 구현함으로써 컴포넌트가
        생명주기에 적극적으로 참여할 수 있다.</td>
    </tr>
</table>

<hr/>

<h2>컨텐츠 rendering에 대한 이해</h2>

* rendering의 시작점은 __index.js__ 내에 있는 다음 구문이다.
```js
ReactDOM.render(<App/>, document.getElementById('root'));
```
* 위 메소드는 초기 rendering 과정을 시작한다. React는 ReactDOM.render 메소드의   
  첫 번째 인자로 지정된 __App__ 컴포넌트의 새 인스턴스를 만들고, 그 인스턴스의   
  __render__ 메소드를 호출한다. 이 과정은 App 컴포넌트의 자식 컴포넌트들까지   
  계속 이어진다.
<hr/>

<h2>갱신 과정의 이해</h2>

* App이 처음 구동되면 React는 모든 컴포넌트가 자신의 컨텐츠를 rendering하여   
  사용자에게 보이도록 명령한다. 일단 컨텐츠가 화면에 나타나면 app은   
  __재조정 상태(reconciled state)__ 가 되는데, 이는 사용자에게 보여주는   
  컨텐츠가 컴포넌트의 상태와 일관된다는 뜻이다.
* App이 재조정 상태에 있을 때에 React는 변경이 있을 때 까지 대기한다.   
  대부분의 app에서 변경이란, 사용자의 상호작용에서 비롯된다. 즉, 이벤트가   
  발생하고 __setState__ 메소드가 호출되는 결과를 낳는다.
* 하나의 이벤트는 여러 상태 데이터의 변경을 야기할 수 있으며, 모든 변경이   
  처리되면 react는 최신이 아닌(진부한) 각 컴포넌트와 자식 컴포넌트의   
  render 메소드를 호출한다.
* React는 app이 재조정 상태로 들어가기 전에 해야할 작업의 양을 최소화하기 위해   
  해당 변경에 의해 영향을 받는 컴포넌트만을 갱신한다.
<hr/>

<h2>재조정 과정의 이해</h2>

* 리액트는 진부하다고 표시된 __모든 컴포넌트__ 의 render 메소드를 호출하지만,   
  그 결과로 만들어진 컨텐츠를 항상 사용하는 것은 아니다.   
  DOM내의 HTML element변경은 값비싼 작업이므로, React는 컴포넌트가 반환한   
  컨텐츠를 이전 결과와 비교해 __브라우저가 최소한의 작업만 수행하도록__ 한다.

* React가 컨텐츠와 비교하는 대상은 효율적인 비교가 가능한 형태로 정의된 캐시,   
  이른바 __Virtual DOM__ 이다.
<hr/>

<h2>목록 재조정의 이해</h2>

* React는 데이터의 배열을 보여주는 element를 다룸에 있어 특별한 지원을 한다.   
  대부분의 목록 작업에 있어, 배열 내의 element들은 대개 그대로 남아있다.   
  React가 최소한의 작업으로 변경 사항을 반영할 수 있게 하려면, element에   
  __key__ prop을 부여하면 된다.
* React가 각 element를 식별할 수 있게 __key prop의 값은 배열 내에서 유일해야 한다__.
<hr/>

<h2>명시적 재조정</h2>

* 재조정 과정은 react가 setState 메소드를 통해 변경사항을 통지받아 어떤   
  데이터가 진부한지를 판별하는 작업이다.
* 그러나 외부 데이터가 도착하는 등 App외부에서 발생된 변경사항에 응답할 때,   
  항상 setState 메소드를 호출할 수 있는 것은 아니다.
* 이러한 상황을 위해 React는 __forceUpdate__ 메소드를 제공하는데, 이 메소드는   
  명시적으로 재조정을 실행해 모든 변경사항이 사용자가 보는 컨텐츠에 반영됨을 보장한다.
<hr/>

<h2>컴포넌트 생명주기</h2>

* 클래스 기반의 상태 유지 컴포넌트는 대개 생성자와 render 메소드를 구현한다
* 생성자의 역할은 주로 부모로부터 props를 받는 일과 상태 데이터를 관리하는 일이다.
* render메소드는 app이 시작될 때와 갱신될 때 컨텐츠를 생산하는 역할을 한다.

* 상태 유지 컴포넌트의 생명주기 메소드
<table>
    <tr>
        <td>constructor</td>
        <td>컴포넌트 클래스의 새 인스턴스가 생성될 때 호출된다.</td>
    </tr>
    <tr>    
        <td>render</td>
        <td>React가 컴포넌트의 컨텐츠를 요구할 때 호출된다.</td>
    </tr>
    <tr>    
        <td>componentDidMount</td>
        <td>컴포넌트의 초기 렌더링 작업이 완료된 후 호출된다.</td>
    </tr>
    <tr>    
        <td>componentDidUpdate</td>
        <td>컨텐츠 갱신을 위한 재조정 과정이 끝난 후 호출된다.</td>
    </tr>
    <tr>    
        <td>componentWillUnmount</td>
        <td>컴포넌트가 제거되기 전에 호출된다.</td>
    </tr>
    <tr>    
        <td>componentDidCatch</td>
        <td>에러를 다룰 때 사용된다.</td>
    </tr>
</table>

<hr/>

<h3>마운트 단계(Mounting phase)</h3>

* Mounting Phase : React가 처음으로 컴포넌트를 생성하고 컨텐츠를 rendering하는 단계
* 컴포넌트가 마운트 단계에 참여하기 위해 일반적으로 구현하는 세 개의 메소드
  * (1) 생성자
  * (2) render : 컴포넌트가 React에게 DOM에 추가될 컨텐츠를   
    제공하고자 할 때 호출된다.
  * (3) componentDidMount : 일반적으로 웹 서비스로부터 data를   
    받기 위한 비동식 요청, 즉 Ajax요청 수행
<hr/>

<h3>업데이트 단계(Update phase)</h3>

* Update Phase : React가 변경사항에 대해 응답해 재조정을 수행하는 과정
* 업데이트 단계의 순서
  * (1) render
  * (2) componentDidUpdate : 주로 React의 ref라는 기능을 사용해   
    DOM내의 HTML element를 직접 조작하는 일에 사용된다.

* Mounting phase를 통한 초기 rendering 이후의 어떤 render 메소드 호출이든,   
  __React가 재조정 과정을 완료하고 DOM을 갱신하려면 componentdidUpdate가 호출된다.__
<hr/>

<h3>언마운트 단계(Unmounting phase)</h3>

* 컴포넌트가 제거돼야 하는 시점인 __Unmounting Phase__ 에 왔을 때, React는   
  componentWillUnmount 메소드를 호출한다.
* 이 메소드는 자원 반환, 네트워크 연결 종료, 비동기 작업 중단 등을 할 수 있는   
  기회를 컴포넌트에게 제공한다.
* __React는 한번 unmount된 컴포넌트는 재사용하지 않는다__.
<hr/>

<h3>이펙트 훅(Effect Hook)</h3>

* 함수형 컴포넌트의 경우에는 생명주기 메소드를 구현할 수 없으며, 위의 방식대로   
  생명주기에 참여할 수 없다. 
* 이러한 상황을 위해 제공되는 __Effect Hook__ 이라는 기능이 있다.
```js
import React, {useState, useEffect} from 'react';
import {ActionButton} from './ActionButton';

export function HooksMessage(props) {
    const [showSpan, setShowSpan] = useState(false);

    useEffect(() => console.log('useEffect function invoked.'));

    const handleClick = (event) => {
        setShowSpan(!showSpan);
        props.callback(event);
    }

    const getMessageElement = () => {
        let div = <div id="messageDiv" className="h5 text-center p-2">
                    {props.message}
                  </div>

        return showSpan ? <span>{div}</span> : div; 
    }

    return(
        <div>
            <ActionButton theme="primary" {...props} callback={handleClick} />
                {getMessageElement()}
        </div>
    )
}
```
* 위 컴포넌트의 __useEffect__ 함수는 컴포넌트가 mount, update, unmount될 때   
  호출되는 함수를 등록하기 위해 사용된다.
* 클래스 기반 상태 유지 컴포넌트에서는 세 가지 단계에 따라 각각 다른 메소드가   
  호출됐지만, 위 컴포넌트는 세 가지 상황에서 모두 동일한 함수가 호출된다.
* useEffect에 전달된 함수는 컴포넌트가 unmount될 때, 즉 __componentWillUnmount__   
  메소드와 비슷한 역할을 하는 정리 함수를 반환할 수 있다.
```js
// 위 코드와 나머지 동일
export function HooksMessage(props) {

    useEffect(()=> {
        console.log("useEffect function invoked.");
        return () => console.log("useEffect cleanup.");
    })
}
```
<hr/>

<h2>고급 생명주기 메소드</h2>

* 고급 생명주기 메소드는 __클래스 기반의 컴포넌트__ 에서 사용할 수 있다.
<table>
    <tr>    
        <td>shouldComponentUpdate</td>
        <td>컴포넌트가 업데이트 돼야 하는지 알려준다.</td>
    </tr>
    <tr>    
        <td>getDerivedStateFromProps</td>
        <td>부모로부터 받은 props를 기준으로 상태 데이터를 갱신한다.</td>
    </tr>
    <tr>    
        <td>geSnapshotBeforeUpdate</td>
        <td>재조정 과정에서 DOM이 갱신되기 전에, 상태와 관련된 정보를 가져온다.</td>
    </tr>
</table>

<hr/>

<h3>불필요한 컴포넌트 업데이트의 방지</h3>

* React는 기본적으로 상태 데이터가 변경될 때마다 컴포넌트를 __진부함__ 으로   
  표히사고 그 컨텐츠를 rendering 한다.
* 컴포넌트는 __shouldComponentUpdate__ 메소드를 구현함으로써 위와 같은   
  React의 기본 동작 대신 새로운 동작을 정의할 수 있다. 이는 rendering이   
  필요하지 않은 상황에서도 render메소드를 호출하는 일을 방지함으로써   
  app의 성능을 향상시킨다.
* shouldComponentUpdate 메소드는 __Update phase__ 에서 호출되며,   
  컴포넌트의 새 컨텐츠를 rendering할지 판단한다.
* shouldComponentUpdate의 인자는 __새 props와 상태 객체__ 인데, 이는   
  기존 값들과 비교하기 위해 사용된다.   
  shouldComponentUpdate 메소드가 true를 반환하면 React는 update phase를   
  계속 진행하고, false를 반환하면 React는 update phase를 중단한다. 따라서   
  render와 componentDidUpdate는 호출되지 않는다.

```js
import React, {Component} from 'react';
export class Message extends Component {

    shouldComponentUpdate(newProps, newState) {
        let change = newProps.message !== this.props.message;
        if(change) {
            console.log("Update Allowed");
        } else {
            console.log("Update prevented.");
        }
        return change;
    }
}
```
<hr/>

<h3>props 값으로부터 상태 데이터 갱신</h3>

* getDerivedStateFromProps 메소드는 단계에 따라 다음과 같이 호출된다.
  * Mounting phase : __render 메소드 호출 전에 호출된다.__
  * Update phase : __shouldComponentUpdate 메소드 전에 호출된다.__

* getDerivedStateFromProps 메소드는 컨텐츠가 rendering되기 전에 prop값을   
  사용해 상태 데이터를 갱신할 수 있는 기회를 제공하며, 특히 prop값이   
  변경되면 그 동작에 영향을 받는 컴포넌트를 위해 고안됐다.

* getDerivedStateFromProps는 __static method__ 로서, 인스턴스 메소드나   
  프로퍼티에서 __this 키워드를 이용해 접근할 수 없다__.   
  그 대신 이 메소드는 __props와 state__ 를 인자로 받는다.
  * props 객체 : 부모 컴포넌트로부터 받은 prop값들이 들어간다.
  * state 객체 : 현재의 상태 데이터가 들어간다.
  * 반환값: props 데이터를 기준으로 갱신된 새로운 State 객체 
  ```js
  static getDerivedStateFromProps(props, state) {
    if(props.value !== state.lastValue) {
      return {
        lastValue:props.value,
        direction:state.lastValue > props.value ? "down" : "up"
      }
    }
    return state;
  }
  ```