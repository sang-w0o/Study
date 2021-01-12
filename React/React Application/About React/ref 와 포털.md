ref와 포털
======

* 보통의 상황에서 컴포넌트는 DOM 내의 element와 직접 상호작용할 수 없다. 그 대신   
  props와 이벤트 핸들러를 통해 상호작용을 하며, 이는 컨텐츠에 대한 지식 없이도   
  컴포넌트들의 협업을 가능하게 한다.
* 그러나 때로는 컴포넌트가 DOM 내의 element와 상호작용을 해야하는 경우도 있다.   
  React는 이를 위해 두 가지 기능을 제공하는데, 다음과 같다.
  * __ref(reference)__ : 컴포넌트가 rendering한 HTML Element가 DOM에 추가된 후에도   
    접근을 가능하게 한다.
  * __portal__ : Application contents의 외부에서 HTML Element에 접근할 수 있게 한다.
  
<table>
    <tr>
        <td>그게 무엇인가?</td>
        <td>ref는 컴포넌트에 의해 이미 rendering된 DOM 내의 element로의 참조이다. Portal은 app 컨텐츠의 외부에서 컨텐츠를 rendering할 수 있게 한다.</td>
    </tr>
    <tr>
        <td>왜 유용한가?</td>
        <td>element에 focus를 주는 일과 같이 DOM에 직접 접근하지 않으면 쉽게 처리할 수 없는 HTML Element의 일부 특징들이 있다. 또한 다른 framework나 library에 접근하는 경우에도 유용하다.</td>
    </tr>
    <tr>
        <td>어떻게 사용하는가?</td>
        <td>ref는 특별한 속성인 ref를 사용해 만들 수 있으며, React.createRef 메소드나 callback함수를 통해 만들 수도 있다.</td>
    </tr>
    <tr>
        <td>문제점이나 제약사항이 있는가?</td>
        <td>ref와 portal은 남용되기 쉽다. 이들은 컴포넌트의 독립성을 해칠 수 있으며, React가 이미 제공하는 기능을 중복해 구현하게 할 가능성이 높다.</td>
    </tr>
</table>

* Note : jQuery@3.3.1을 사용한다.

```js
// src/Editor.js
import React, {Component} from 'react';

export class Editor extends Component {

    constructor(props) {
        super(props);
        this.state={
            name:"",
            category:"",
            price:""
        }
    }

    handleChange = (event) => {
        event.persist();
        this.setState(state=> state[event.target.name] = event.target.value);
    }

    handleAdd = () => {
        this.props.callback(this.state);
        this.setState({name:"", category:"", price:""});
    }

    render() {
        return(
            <React.Fragment>
                <div className="form-group p-2">
                    <label>Name</label>
                    <input className="form-control" name="name"
                        value={this.state.name} onChange={this.handleChange}
                        autoFocus={true} />
                </div>
                <div className="form-group p-2">
                    <label>Category</label>
                    <input className="form-control" name="category"
                        value={this.state.category} onChange={this.handleChange}/>
                </div>
                <div className="form-group p-2">
                    <label>Price</label>
                    <input className="form-control" name="price"
                        value={this.state.price} onChange={this.handleChange}/>
                </div>
                <div className="text-center">
                    <button className="btn btn-primary" onClick={this.handleAdd}>
                        Add
                    </button>
                </div>
            </React.Fragment>
        )
    }
}
```
* Editor 컴포넌트는 일련의 input element를 rendering 한다. 각 element의 값은   
  상태 데이터 프로퍼티를 사용해 설정되며, onChange이벤트는 handleChange메소드에 의해 처리되고,   
  button element의 onClick 이벤트는 handleAdd메소드에 의해 처리된다.

```js
// src/ProductTable.js

import React, {Component} from 'react';

export class ProductTable extends Component {

    render() {
        return(
            <table className="table table-sm table-striped">
                <thead><tr><th>Name</th><th>Category</th><th>Price</th></tr></thead>
                <tbody>
                    {
                        this.props.products.map(p =>
                            <tr key={p.name}>
                                <td>{p.name}</td>
                                <td>{p.category}</td>
                                <td>${Number(p.price).toFixed(2)}</td>
                            </tr>
                        )
                    }
                </tbody>
            </table>
        )
    }
}
```
* ProductTable 컴포넌트는 products prop으로 받는 각 객체를 위한 tr를 포함하는   
  table 하나를 rendering 한다.

```js
// src/App.js

import React, {Component} from 'react';
import {Editor} from './Editor';
import {ProductTable} from './ProductTable';

export default class App extends Component {

  constructor(props) {
    super(props);
    this.state={
      products:[]
    }
  }

  addProduct = (product) => {
    if(this.state.products.indexOf(product.name) === -1) {
      this.setState({products: [...this.state.products, product]});
    }
  }

  render() {
    return(
      <div>
        <Editor callback={this.addProduct}/>
        <h6 className="bg-secondary text-white m-2 p-2">Products</h6>
        <div className="m-2">
          {
            this.state.products.length === 0 ? <div className="text-center">No Products</div> : 
              <ProductTable products={this.state.products}/>
          }
        </div>
      </div>
    )
  }
}
```
<hr/>

<h2>ref의 생성</h2>

* ref는 컴포넌트가 특정 HTML Element의 기능을 사용하기 위해 DOM에 접근하고자할 때 사용된다.
* props를 통해 사용할 수 없는 HTML 기능들이 존재하는데, 그 중 하나는 element가 focus를   
  얻었는지의 여부를 확인하는 일이다. 컨텐츠가 __처음 rendering될 때 focus를 주고 싶다면__   
  __autoFocus속성을 사용__ 하면 되지만, 사용자가 버튼을 클릭하면 focus가 버튼으로 이동할 것이다.   
  이는 input element를 클릭하거나 Tab키를 이용해 input element에 다시 focus를 주지 않는 한, 새로운   
  아이템을 만들기 위한 타이핑을 즉시 시작할 수 없다는 뜻이다.

* ref를 사용하면 Add 버튼을 클릭해 이벤트가 발생했을 때, DOM에 접근해 input element에 대한   
  focus 메소드를 호출할 수 있다.
```js
// src/Editor.js

// 기타 코드 동일

export class Editor extends Component {

    constructor(props) {
        super(props);
        this.state={
            name:"",
            category:"",
            price:""
        }
        this.nameRef = React.createRef();
    }

    handleAdd = () => {
        this.props.callback(this.state);
        this.setState({name:"", category:"", price:""}, () => this.nameRef.current.focus());
    }

    render() {
        return(
            <React.Fragment>
                <div className="form-group p-2">
                    <label>Name</label>
                    <input className="form-control" name="name"
                        value={this.state.name} onChange={this.handleChange}
                        autoFocus={true} ref={this.nameRef} />
                </div>
                <div className="form-group p-2">
                    <label>Category</label>
                    <input className="form-control" name="category"
                        value={this.state.category} onChange={this.handleChange}/>
                </div>
                <div className="form-group p-2">
                    <label>Price</label>
                    <input className="form-control" name="price"
                        value={this.state.price} onChange={this.handleChange}/>
                </div>
                <div className="text-center">
                    <button className="btn btn-primary" onClick={this.handleAdd}>
                        Add
                    </button>
                </div>
            </React.Fragment>
        )
    }
}
```
* 위에서는 ref를 __React.createRef 메소드__ 를 이용해 만들었으며, 이를 constructor 내에서   
  함으로써 그 결과를 컴포넌트 전체에서 사용할 수 있게 했다.
* __createRef 메소드가 반환한 ref 객체는 current 프로퍼티를 정의__ 하는데, 이 프로퍼티는   
  __DOM 내의 Element를 대변하는 HTMLElement라는 객체를 반환__ 한다.
<hr/>

<h2>비제어 form 컴포넌트</h2>

* 위 코드들은 제어 컴포넌트로 form을 다루고 있다. 이는 React가 각 form element의 컨텐츠에   
  대한 책임을 지며 값 저장은 상태 데이터 프로퍼티로, 값 변경은 이벤트 핸들러로 처리한다.
* Form element에는 이미 값 저장과 변경 처리를 할 수 있는 능력이 있지만, 제어 컴포넌트에서는   
  사용되지 않는다. 하지만 비제어 컴포넌트로서 form을 다루는 기법이 있다.   
  이 경우 form element에 대한 접근에 ref가 사용되며, 브라우저가 element의 값 관리와 변경   
  처리의 책임을 진다.
```js
// src/Editor.js
import React, {Component} from 'react';

export class Editor extends Component {

    constructor(props) {
        super(props);
        this.nameRef = React.createRef();
        this.categoryRef = React.createRef();
        this.priceRef = React.createRef();
    }

    handleAdd = () => {
        this.props.callback({
            name:this.nameRef.current.value,
            category:this.categoryRef.current.value,
            price:this.priceRef.current.value
        });

        this.nameRef.current.value = "";
        this.categoryRef.current.value = "";
        this.priceRef.current.value="";
        this.nameRef.current.focus();
    }

    render() {
        return(
            <React.Fragment>
                <div className="form-group p-2">
                    <label>Name</label>
                    <input className="form-control" name="name"
                        autoFocus={true} ref={this.nameRef}/>
                </div>
                <div className="form-group p-2">
                    <label>Category</label>
                    <input className="form-control" name="category"
                        ref={this.categoryRef}/>
                </div>
                <div className="form-group p-2">
                    <label>Price</label>
                    <input className="form-control" name="price"
                        ref={this.priceRef}/>
                </div>
                <div className="text-center">
                    <button className="btn btn-primary" onClick={this.handleAdd}>
                        Add
                    </button>
                </div>
            </React.Fragment>
        )
    }
}
```
* 위 컴포넌트에서 사용자가 Add 버튼을 클릭하면 handleAdd 메소드에서는 각 input element의   
  ref를 사용해 value 프로퍼티를 읽는다.
<hr/>

<h3>callback함수를 사용한 ref 생성</h3>

```js
// src/Editor.js

import React, {Component} from 'react';

export class Editor extends Component {

    constructor(props) {
        super(props);
        this.formElements = {
            name:{},
            category:{},
            price:{}
        }
    }

    setElement = (element) => {
        if(element !== null) {
            this.formElements[element.name].element = element;
        }
    }
    handleAdd = () => {
        let data = {};
        Object.values(this.formElements).forEach(v=>{
            data[v.element.name] = v.element.value;
            v.element.value = "";
            });
        this.props.callback(data);
        this.formElements.name.element.focus();
    }

    render() {
        return(
            <React.Fragment>
                <div className="form-group p-2">
                    <label>Name</label>
                    <input className="form-control" name="name"
                        autoFocus={true} ref={this.setElement}/>
                </div>
                <div className="form-group p-2">
                    <label>Category</label>
                    <input className="form-control" name="category"
                        ref={this.setElement}/>
                </div>
                <div className="form-group p-2">
                    <label>Price</label>
                    <input className="form-control" name="price"
                        ref={this.setElement}/>
                </div>
                <div className="text-center">
                    <button className="btn btn-primary" onClick={this.handleAdd}>
                        Add
                    </button>
                </div>
            </React.Fragment>
        )
    }
}
```
* 각 input element의 ref 프로퍼티 값으로 컨텐츠가 rendering될 때 호출되는   
  메소드가 지정됐다. 이 메소드는 ref객체의 current 프로퍼티가 아닌, HTMLElement   
  객체를 직접 받는다.
<hr/>

<h2>ref와 생명주기</h2>

* ref는 React가 컴포넌트의 render 메소드를 호출하기 전까지 값을 할당받지 못한다.
* createRef 메소드를 사용하는 경우라면 컴포넌트가 자신의 컨텐츠를 rendering하기 전까지   
  current 프로퍼티는 값을 할당받지 못한다. 마찬가지로 callback ref 역시 컴포넌트의   
  rendering 전까지 자신의 메소드를 호출하지 못한다.

* ref의 할당은 컴포넌트 생명주기에 있어서 후반에 일어난다. ref는 DOM Element에 접근하게   
  해주지만, DOM Element는 rendering 단계 이전에는 생성되지 않는다.   
  즉 React는 render메소드가 호출되기 전까지는 ref가 참조할 element를 만들지 않는다.   
  따라서 ref와 연결된 element는 오직 __componentDidMount 와 componentDidUpdate__ 메소드   
  에서 접근 가능하다. 이 생명주기 메소드들은 rendering이 완료되고 DOM Element가 생성되거나   
  갱신된 후에 사용되기 때문이다.

* ref를 사용하는 결과 중 하나는, React가 DOM의 Element를 교체하는 경우 컴포넌트가 context를   
  보존하기 위해 상태에 의존할 수 밖에 없다는 것이다.

* __getSnapshotBeforeUpdate 생명주기 메소드__ : 상태 유지 컴포넌트에 존재하는 생명주기 메소드로,   
  __update phase__ 에서 __render__ 메소드 다음에, __componentDidUpdate__ 메소드 이전에 호출된다.   
  이 메소드는 Snapshot 객체를 만든다.

* Snapshot객체는 getSnapshotBeforeUpdate 메소드에 의해 만들어지며, DOM 갱신 이전의 현재 컨텐츠를   
  조사해 만들어진 것이다. DOM 갱신이 완료되면 componentDidUpdate 메소드가 Snapshot객체를 받으며 호출된다.   
  따라서 컴포넌트는 현재 DOM에 존재하는 element를 다룰 수 있게 된다.

```js
// src/Editor.js

// 기타 코드 생략

getSnapshotBeforeUpdate(props, state) {
    return Object.values(this.formElements).map(item => {
        return {name:[item.name], value:item.element.value}
    })
}

componentDidUpdate(oldProps, oldState, snapshot) {
    snapshot.forEach(item => {
        let element = this.formElements[item.name].element;
        if(element.value !== item.value) {
            element.value = item.value;
        }
    })
}
```
* getSnapshotBeforeUpdate 메소드는 DOM 갱신 전의 props와 상태 객체를 받고, DOM 갱신 후에   
  componentDidUpdate 메소드에 전달될 객체를 반환한다.   
  React는 snapshot객체에 대한 특별한 형식을 강조하지 않으며, getSnapshotBeforeUpdate메소드는   
  쓸모만 있다면 어떠한 형식의 데이터라도 반환할 수 있다.
* 일단 갱신 작업이 끝나면 React는 componentDidUpdate 메소드를 호출하는데, 이때 이전 props와   
  이전 상태 데이터를 포함한 snapshot을 인자로 전달한다.
<hr/>

<h2>자식 컴포넌트의 컨텐츠에 접근</h2>

* ref는 React가 특별히 취급하는 prop이다. 이는 자식 컴포넌트가 rendering하는 DOM Element에   
  대한 ref를 사용할 때 주의가 필요하다는 뜻이다. 가장 쉬운 접근법은 ref객체나 callback함수를   
  ref가 아닌 다른 이름을 사용하는 것이다. 그러면 React는 그 ref를 여느 prop과 마찬가지로   
  전달할 것이다.

```js
// src/FormField.js
import React, {Component} from 'react';

export class FormField extends Component {

    constructor(props){
        super(props);
        this.state = {
            fieldValue=""
        }
    }

    handleChange = (event) => {
        this.setState({fieldValue : event.target.value})
    }

    render() {
        return (
            <div className="form-group">
                <label>{this.props.label}</label>
                <input className="form-control" value={this.state.fieldValue}
                    onChange={this.handleChange} ref={this.props.fieldRef} />
            </div>
        )
    }
}
```
* 이 컴포넌트는 제어 가능한 input element를 rendering하며, fieldRef라는 prop을   
  사용해 부모로부터 받은 ref를 element에 연계한다.
```js
// src/App.js
import React, {Component} from 'react';
import {FormField} from './FormField';

export default class App extends Component {

  constructor(props) {
    super(props);
    this.fieldRef = React.createRef();
  }

  handleClick = () => {
    this.fieldRef.current.focus();
  }

  render() {
    return(
      <div className="m-2">
        <FormField label="Name" fieldRef={this.fieldRef}/>
        <div className="text-center m-2">
          <button className="btn btn-primary" onClick={this.handleClick}>
            Focus!
          </button>
        </div>
      </div>
    )
  }
}
```
* App 컴포넌트는 ref를 만들어 fieldRef prop을 사용해 FormField 컴포넌트에 전달한다.
<hr/>

<h3>ref forwarding</h3>

* ref forwarding은 자식에게 ref를 전달하는 또 다른 방법인데, 일반적인 prop 대신   
  ref를 사용할 수 있게 하는 방법이다.
```js
// src/FormField.js
import React, {Component} from 'react';

export const ForwardFormField = React.forwardRef((props, ref) =>
    <FormField {...props} fieldRef={ref}/>
)

export class FormField extends Component {

    constructor(props){
        super(props);
        this.state = {
            fieldValue:""
        }
    }

    handleChange = (event) => {
        this.setState({fieldValue : event.target.value})
    }

    render() {
        return (
            <div className="form-group">
                <label>{this.props.label}</label>
                <input className="form-control" value={this.state.fieldValue}
                    onChange={this.handleChange} ref={this.props.fieldRef} />
            </div>
        )
    }
}
```
* __React.forwardRef__ 메소드는 __props와 ref값을 받아 컨텐츠를 rendering하는__   
  __함수에 전달__ 된다. 위에서는 받은 ref값을 fieldRef에 넘겼는데, fieldRef는   
  FormField 컴포넌트가 사용할 prop의 이름이다.
```js
// src/App.js
import React, {Component} from 'react';
import {ForwardFormField} from './FormField';

export default class App extends Component {

  constructor(props) {
    super(props);
    this.fieldRef = React.createRef();
  }

  handleClick = () => {
    this.fieldRef.current.focus();
  }

  render() {
    return(
      <div className="m-2">
        <ForwardFormField label="Name" ref={this.fieldRef}/>
        <div className="text-center m-2">
          <button className="btn btn-primary" onClick={this.handleClick}>
            Focus!
          </button>
        </div>
      </div>
    )
  }
}
```
<hr/>

<h2>포털</h2>

* 포털은 컴포넌트가 부모 컨텐츠의 일부로서가 아닌, 특정 DOM Element 내에서 자신의   
  컨텐츠를 rendering할 수 있게 한다. 이는 통상적인 React의 컴포넌트 모델을 벗어나는   
  일이다. 따라서 포털은 사용자에게 보여줄 대화상자나 모달 경고창을 만드는 경우, 또는   
  React를 다른 framework나 library가 만든 컨텐츠에 통합하는 경우 등 제한된 상황에서만   
  유용한 기능이다.

```js
// src/PortalWrapper.js

import React, {Component} from 'react';
import ReactDOM from 'react-dom';

export class PortalWrapper extends Component {

    constructor(props) {
        super(props);
        this.portalElement = document.getElementById("portal");
    }

    render() {
        return ReactDOM.createPortal(
            <div className="border p-3">{this.props.children}</div>, this.portalElement
        );
    }
}
```
* PortalWrapper 컴포넌트는 props.children 프로퍼티를 사용해 컨테이너를 생성하며,   
  rendering할 컨텐츠와 대상 DOM Element를 인자로 하는 __ReactDOM.createPortal__ 메소드를   
  사용해 HTML파일의 대상 element를 찾는다.

```js
// src/App.js

import React, {Component} from 'react';
import {ForwardFormField} from './FormField';
import {PortalWrapper} from './PortalWrapper';

export default class App extends Component {

  constructor(props) {
    super(props);
    this.fieldRef = React.createRef();
    this.portalFieldRef = React.createRef();
  }

  focusLocal = () => {
    this.fieldRef.current.focus();
  }

  focusPortal = () => {
    this.portalFieldRef.current.focus();
  }

  render() {
    return(
      <div>
        <PortalWrapper>
          <ForwardFormField label="Name" ref={this.portalFieldRef}/>
        </PortalWrapper>
        <div className="text-center m-2">
          <button className="btn btn-primary m-1"
            onClick={this.focusLocal}>
              Focus Local
          </button>
          <button className="btn btn-primary m-1"
            onClick={this.focusPortal}>
              Focus Portal
          </button>
        </div>
      </div>
    )
  }
}
```