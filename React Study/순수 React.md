순수 React
======

<h2>#1. 페이지 설정</h2>

* 리액트를 브라우저이서 다루려면 React와 ReactDOM 라이브러리를 불러와야 한다.
  * React : View를 만들기 위한 라이브러리
  * ReactDOM : UI를 실제로 브라우저에 렌더링할 때 사용되는 라이브러리
  * 여기서는 react16.js와 reactDOM16.js를 사용한다.
  * 또한 HTML에서 script태그를 body 태그 내에 불러오자.(?)
  ```HTML
  <!DOCTYPE html>
  <html>
  <head>
  <meta charset="UTF-8">
  <title>React Test</title>
  </head>

  <body>
    <div id="react-container"></div>
    <script src="./scripts/react16.js"></script>
    <script src="./scripts/reactDOM16.js"></script>
    <script src="./scripts/script.js"></script>
  </body>
  </html>
  ```
<hr/>

<h2>#2. 가상 DOM</h2>

* HTML은 간단히 말해 브라우저가 Data Object Model을 구성하기 위해 따라야 하는 절차이다.
* HTML문서를 이루는 Element는 브라우저가 HTML문서를 읽어들이면 DOM Element가 되고,   
  이 DOM이 화면에 사용자 인터페이스를 표시한다.

* 전통적으로 웹사이트는 독립적인 HTML 페이지들로 만들어졌다. 따라서 사용자가 페이지 사이를   
  이동하면, 브라우저는 매번 다른 HTML문서를 요청해서 로딩했다.
* 하지만 Ajax가 생기면서 단일 페이지 웹(SPA, Single Page Application)이 생겼다. 브라우저가   
  Ajax를 통해 작은 데이터를 요청해서 가져올 수 있게됨에 따라 이제는 전체 웹 애플리케이션이   
  한 페이지로 실행되면서 JS에 의존해 사용자 인터페이스를 갱신할 수 있게된 것이다.
* SPA에서 처음에 브라우저는 HTML문서를 하나 적재한다. 사용자는 사이트를 이동하지만, 실제로는   
  같은 페이지 내에 계속 머문다.

* DOM API는 브라우저의 DOM을 변경하기 위해 JS가 사용할 수 있는 객체들의 모음이다.
* 화면에 표시된 DOM Element를 JS로 갱신하거나 변경하는 것은 상대적으로 쉽지만, 새로운 Element를   
  추가하는 것은 시간이 꽤나 오래 걸린다. 이는 곧 웹 개발자가 UI의 변경 방법을 세밀하고 정확하게   
  신경 써야 Application의 성능을 향상시킬 수 있음을 의미한다.

* 위의 문제를 해결하기 위해 사용하는 것이 바로 React이다.
* React는 브라우저 DOM을 갱신하기 위해 만들어진 라이브러리로, React가 모든 처리를 대신 해주기에   
  웹 개발자는 더 이상 SPA를 더 효율적으로 만들기 위해 복잡한 내용을 신경 쓸 필요가 없게 됐다.

* DOM API는 React 라이브러리가 직접 조작하기에, 개발자는 대신 __가상 DOM__ 을 다루거나 React가   
  UI를 생성하고 브라우저와 상호작용하기 위해 사용하는 몇 가지 명령을 다룬다.

* 가상 DOM은 React Element로 이루어진다.
* React Element는 개념상 HTML Element와 비슷하지만, 실제로는 JavaScript 객체이다.
<hr/>

<h2>#3. React Element</h2>

* 브라우저 DOM은 DOM Element로 이루어지는것과 같이, React DOM은 React Element로 이루어진다.
* React Element는 그에 대응하는 DOM Element가 어떻게 생겨야 하는지를 기술한다.   
  즉, React Element는 브라우저 DOM 을 만드는 방법을 알려주는 명령이다.

* React.createElement() : React Element를 만드는 함수
```js
React.createElement("h1", null, "Baked Salmon");
``` 
* 첫 번째 인자 : 만들고자 하는 Element의 타입
* 두 번째 인자 : Element의 Property(속성)
* 세 번째 인자 : 출력될 내용 (여는 태그와 닫는 태그 사이에 들어갈 문자열)
* 위 JS코드는 렌더링 과정에서 아래의 실제 DOM Element로 변환된다.
```HTML
<h1>Baked Salmon</h1>
```
* DOM Element의 속성을 React Element의 속성으로 표현할 수 있다.
```js
React.createElement("h1", {id:"recipe-0", 'data-type':"title"}, "Baked Salmon");
```
* 위 코드는 렌더링 후 아래의 코드가 된다.
```HTML
<h1 data-reactroot id="recipe-0" data-type="title">Baked Salmon</h1>
```
* data-reactroot : React Component의 Root Element를 식별해주는 속성
  * 개발자가 만든 React Component의 Root Element에 항상 나타난다.
  * 이는 렌더링 시 Element사이의 계층 관계에 따라 갱신 대상을 추적하기 위해서 이다.

* 위 코드의 createElement가 실제로 만들어낸 Element는 다음과 같다.
```js
{
    $$typeof : Symbol(React.element),
    "type":"h1",
    "key":null,
    "ref":null,
    "props":{"children":"Baked Salmon"},
    "_owner":null,
    "_store":{}
}
```
* 위는 실제 React Element의 구조를 표현한 것이다.
  * type : 만드려는 HTML이나 SVG Element의 타입 지정
  * props : DOM Element를 만들기 위해 필요한 data나 자식 element 표현
  * children : Text형태로 표시할 다른 내부 Element
<hr/>

<h2>#4. ReactDOM</h2>

* ReactDOM에는 React Element를 브라우저에 rendering하기 위한 모든 도구가 들어있다.
* 즉, 가상DOM에서 HTML을 생성하는데 필요한 모든 도구가 이 라이브러리에 있는 것이다.

* React Element와 그 자식 element를 함께 렌더링하기 위해 ReactDOM.render메소드를 사용한다.
  * 첫 번째 인자 : 렌더링할 React Element
  * 두 번째 인자 : 렌더링이 일어날 대상 DOM 노드
```js
var dish = React.createElement("h1", null, "Baked Salmon");

ReactDOM.render(dish, document.getElementById('react-container'));
```
* 위와 같이 개발자는 Element를 만들고, 그 Element를 DOM으로 렌더링만 하면 된다.
<hr/>

<h2>#5. 자식</h2>

* ReactDOM에서는 항상 1개의 element만 DOM으로 렌더링할 수 있다.
* React는 렌더링 대상 Element에 data-reactroot라는 꼬리표를 단다.
* 모든 다른 React Element는 이 root element 아래에 내포된다.

* React는 props.children을 사용해 자식 element를 렌더링한다.
* __컴포넌트 트리__ : 텍스트가 아닌 다른 React Element를 자식으로 렌더링할 수 있고,   
  그렇게 하면 Element의 tree가 생기게 된다. 이것이 컴포넌트 트리이다.
* 트리에는 root component가 1개 존재하고, root아래로 많은 가지가 자란다.

```HTML
<ul class="subjects">
    <li>math</li>
    <li>english</li>
    <li>korean</li>
    <li>history</li>
</ul>
```
* 위 코드에서는 ul요소가 root element이며, 그 하위에는 4개의 자식 element가 있다.
* 이를 React.createElement로 나타내보자.
```js
React.createElement(
    "ul",
    null,
    React.createElement("li", null, "math"),
    React.createElement("li", null, "english"),
    React.createElement("li", null, "korean"),
    React.createElement("li", null, "history")
)
```
* 위 createElement에서는 4개의 자식 element를 생성했다.
* React는 이러한 자식 element의 배열을 만들고, props.children의 값을   
  그 배열에 설정한다.
* 위 코드의 결과로 생기는 React Element를 보자.
```js
{
    "type":"ul",
    "props": {
        "children": [
            {"type":"li", "props": {"children":"math"} ... },
            {"type":"li", "props": {"children":"english"} ... },
            {"type":"li", "props": {"children":"korean"} ... },
            {"type":"li", "props": {"children":"history"} ... },
        ]
        ...
    }
}
```
* React에서 class속성을 지정하려면 __className__ property를 사용해야 한다.   
  (class가 JS 예약어이기 때문이다.)
<hr/>

<h2>#6. Data로 Element 만들기</h2>

* React사용의 가장 큰 장점은 UI Element와 Data를 분리할 수 있다는 것이다.
* React도 JS이기에 React Component Tree를 더 편하게 구성하기 위해 JS의 로직을 사용할 수 있다.
```js
React.createElement(
    "ul", {"className":"subjects"},
    React.createElement("li", null, "math"),
    React.createElement("li", null, "english"),
    React.createElement("li", null, "korean"),
    React.createElement("li", null, "history")
)
```
* 위 코드의 과목명을 JS 배열로 간단하게 표현해보자.
```js
var items = [
    "math",
    "english",
    "korean",
    "history"
]
```
* 이제 Array.map 함수를 사용해 간편하게 가상 DOM을 구성하자.
```js
React.createElement(
    "ul",
    {"className":"subjects"},
    items.map(subject=>
        React.createElement("li", null, subject))
)
```
<hr/>

<h2>#7. React Component</h2>

* 모든 UI는 여러 부분으로 나누어져있고, React에서는 이러한 부분들을 Component라 한다.
* Component를 사용하면 서로 다른 데이터 집합에 같은 DOM 구조를 사용할 수 있다.
* 컴포넌트는 코드의 재사용성을 향상시키기 위해 필요한 매우 중요한 개념이다.
* Component는 2가지 방법으로 만들 수 있다.
  * (1) ES6 클래스 이용
  * (2) 상태가 없는 함수형 컴포넌트 사용
<hr/>

<h3>#7 - (2) React.Component</h3>

* React Component를 새로 만들 때에는 React.Component를 추상 클래스로 사용할 수 있다.
* ES6 구문으로 이 추상클래스를 상속하면 Custom Component를 만들 수 있다.
```js
var items = [
    "math",
    "english",
    "korean",
    "history"
];

class SubjectList extends React.Component {
	
	constructor(item){
		super(item);
		this.item = item;
	}
	
	renderListItem(subject, i) {
		return React.createElement("li", {key:i}, subject);
	}
	
	render(){
		return React.createElement("ul", {className:"subjects"},
				this.item.map(this.renderListItem)
				);
	}
}

var subjectList = new SubjectList(items);

ReactDOM.render(subjectList.render(), document.getElementById('react-container'))
```
<hr/>

<h3>#7 - (2) 상태가 없는 함수형 컴포넌트</h3>

* 상태가 없는 함수형 컴포넌트는 객체가 아니라 함수이다. 따라서 this가 없다.
* 이는 간단한 순수 함수이므로 application에서는 가능하면 함수형 컴포넌트를 사용하는 것이 좋다.
* 함수형 컴포넌트를 사용하면 application architecture를 단순하게 유지할 수 있다는 장점이 있다.
* 또한 성능도 좋아진다. 
* 위의 renderListItem과 render 메소드를 1개의 함수로 묶어보자.
```js
const SubjectList = props =>
    React.createElement("ul", {className:"subjects"},
    props.items.map((subject, i) => 
        React.createElement("li", {key:i}, subject)
    )
)
```
* 위 코드를 완성하면 다음과 같다.
```js
var items = [
    "math",
    "english",
    "korean",
    "history"
];

const SubjectList = props => 
	React.createElement("ul", {className:"subjects"},
			props.map((subject, i) =>
				React.createElement("li", {key:i}, subject)
		)
)

ReactDOM.render(SubjectList(items), document.getElementById('react-container'));
```
<hr/>

<h2>#8. DOM Rendering</h2>

* Data를 Component에 Property로 넘길 수 있으므로 UI를 만들 때 사용하는   
  로직과 데이터를 분리할 수 있다.
* 이렇게 분리된 데이터는 DOM보다 훨씬 쉽게 다룰 수 있다는 장점이 있다.
* 만약 모든 data를 한 JS객체에 저장했다고 하자. 이 객체를 변경할 때 마다 그 객체를   
  component에 property로 전달하고 UI를 렌더링해야 한다.
* 기존에 존재하는 ul, li 태그를 수정하는 경우를 생각해보자.   
  전체 DOM을 없애고 재구축하는 대신, __ReactDOM.render 메소드는 현재의 DOM을 남겨둔 채__   
  __가능한 한 DOM을 적게 변경하면서__  새로운 UI를 만들어낸다.
* Element를 DOM에 삽입하는 것은 DOM API 연산 중 가장 비싼 연산이다.   
  따라서 ReactDOM.render를 사용하면 이러한 연산을 빠르게 수행할 수 있다.
<hr/>