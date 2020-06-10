JSX를 사용하는 React
======

* JSX는 React.createElement 대신 사용할 수 있는 일종의 도구이다.
* JSX는 JS의 확장으로, HTML과 비슷한 구문을 사용해 React Element를   
  정의할 수 있게 해준다.

<h2>#1. JSX로 React Element 정의하기</h2>

* JSX에서는 태그를 사용해 Element의 타입을 지정한다.
  * Tag의 attribute는 property를 표현하며, 여는 태그와 닫는 태그 사이에   
    element의 자식을 넣는다.

* JSX Element에는 다른 JSX Element를 자식으로 추가할 수 있다.
* 예를 들어, ul 태그에 li 태그를 넣는 것을 생각할 수 있다.

* JSX는 component와도 잘 작동한다.
```js
// React Element
React.createElement(subjectList, {list:[...]});

// JSX
                   <subjectList list={[...]}>
```
* 배열을 component로 넘길 때는 중괄호 {}로 감싸야 한다.
* 이렇게 중괄호로 감싼 코드를 __JS 식__ 이라 한다.
* JSX에서 component에 property값으로 JS값을 넘기기 위해서는 JS 식을 이요해야 한다.
* component의 property 유형
  * JS식 : 배열, 객체, 함수 등 포함
  * 문자열

* JSX의 문법은 HTML과 비슷한데, 추가적으로 알아야할 몇 가지가 있다.

* 내포된 component
  * JSX에서는 다른 component의 자식으로 component를 추가할 수 있다.
```js
<SubjectList>
    <Subject />
    <Subject />
    <Subject />
  </SubjectList>
```

* className
  * JS에서는 class가 예약어이므로 class 대신 className을 사용한다.
  ```jsx
  <h1 className="food">Baked Salmon</h1>
  ```

* JS 식
  * JS 식을 중괄호로 감싸는 것은 곧 중괄호 내의 식을 평가해서 결과값을   
    돌려주어야함을 의미한다.
  * 예를 들어 element 내의 title값을 출력하고 싶으면, 다음과 같이 한다.
  
```js
<h1>{this.props.title}</h1>
```
  * 문자열이 아닌 다른 값도 JS식 안에 넣어야 한다.
```js
  <input type="checkbox" defaultChecked={false} />
```

* 평가
  * 중괄호 내에 들어간 JS 코드는 그 값을 평가받는다.
  * 이는 곧 여러 가지의 연산이 일어날 수 있음을 의미한다.
  * 또한, JS 식 내에 함수 호출 구문이 있으면, 해당 함수가 호출된다.
```js
<h1>{"Hello" + this.props.title}</h1>

<h1>{this.props.title.toLowerCase().replace}</h1>

function appendTitle({this.props.title}) {
    console.log(`${this.props.title} is great!`);
}
```

* 배열을 JSX로 매핑하기
  * JSX도 JS이므로 JS함수 내에서 JSX를 직접 사용할 수 있다.
```js
<ul>
    {this.props.subjects.map((ingredient, i) => 
        <li key={i}>{subject}</li>
    )}
</ul>
```
  * 위와 같은 JSX 코드는 깔끔하고 가독성이 뛰어나지만, 브라우저가 해석할 수 없다.
  * 따라서 이를 브라우저가 해석하게 하기 위해 __바벨(Babel)__ 이 필요하다.

<h2>#2. 바벨</h2>

* JS는 인터프리터 언어이기에, 브라우저가 코드의 text를 분석하므로   
  컴파일 과정이 불필요하다.
* 하지만 모든 브라우저가 ES6 또는 ES7 문법을 지원하지 않고, 어떤 브라우저는   
  JSX를 지원하지 않는 경우도 있다.
* 이러한 문제를 해결하기 위해 JSX를 브라우저가 해석할 수 있는 코드로 변환해주는   
  과정을 트랜스파일링(Transpiling)이라 하며, Babel이 이 작업을 수행해준다.
* Babel의 트랜스파일링이 필요한 파일은 다음 태그 내에 넣는다.
```HTML
  <script type="text/babel">
    <!-- 여기에 JSX 코드 기술-->
  </script>
```

<h2>#3. JSX로 작성한 코드 예시</h2>

* JSX로 작성한 코드는 React Element를 깔끔하게 기술할 수 있게 해주며, 가독성이 뛰어나다.
* 단점은 브라우저가 해석하지 못한다는 것이다. 따라서 JSX를 순수 React로 변환해야 한다.

```js
const data = [
    {
        "name":"Bacon Sanwich",
        "ingredients": [
            {"name":"bread","amount":"2","measurement":"slice"},
            {"name":"bacon","amount":"4","measurement":"piece"},
            {"name":"lettuce","amount":"3","measurement":"piece"}
        ],
        "steps":[
            "Prepare Bread.",
            "Cook Bacon.",
            "Put lettuce.",
            "Eat."
        ]
    }, 
    {
        "name":"Egg Sanwich",
        "ingredients": [
            {"name":"bread","amount":"4","measurement":"slice"},
            {"name":"egg","amount":"10","measurement":"piece"},
            {"name":"lettuce","amount":"3","measurement":"piece"}
        ],
        "steps":[
            "Prepare Bread.",
            "Cook Egg.",
            "Put lettuce.",
            "Eat."
        ]
    }
]
```
* 위의 data는 JavaScript 객체가 2개 포함된 배열이다.

```js
const Menu = (props) =>
    <article>
        <header>
            <h1>{props.title}</h1>
        </header>
        <div className="recipes">
        </div>
    <article>
```
* 위의 Menu는 조리법으로 이루어진 메뉴를 표현하는 상태가 없는 함수형 컴포넌트이다.
```js
const Recipe = ({name, ingredients, steps}) =>
    <section id={name.toLowerCase().replace(/ /g, "-")}>
        <h1>{name}</h1>
        <ul className="ingredients">
            {ingredients.map((ingredient, i) =>
                <li key={i}>{ingredient.name}</li>
            )}
        </ul>
        <section className="instructions">
            <h2>조리 절차</h2>
            {steps.map((step, i) => 
                <p key={i}>{step}</p>
            )}
        </section>
    </section>
```
* 위의 Recipe는 조리법 하나를 표현하는 상태가 없는 함수형 컴포넌트이다.

* 완성된 코드는 다음과 같다.
```js
const data = [
    {
        "name":"Bacon Sanwich",
        "ingredients": [
            {"name":"bread","amount":"2","measurement":"slice"},
            {"name":"bacon","amount":"4","measurement":"piece"},
            {"name":"lettuce","amount":"3","measurement":"piece"}
        ],
        "steps":[
            "Prepare Bread.",
            "Cook Bacon.",
            "Put lettuce.",
            "Eat."
        ]
    }, 
    {
        "name":"Egg Sanwich",
        "ingredients": [
            {"name":"bread","amount":"4","measurement":"slice"},
            {"name":"egg","amount":"10","measurement":"piece"},
            {"name":"lettuce","amount":"3","measurement":"piece"}
        ],
        "steps":[
            "Prepare Bread.",
            "Cook Egg.",
            "Put lettuce.",
            "Eat."
        ]
    }
];

const Menu = ({title, recipes}) => 
<article>
    <header>
        <h1>{title}</h1>
    </header>
    <div className="recipes">
    	{recipes.map((recipe, i) => 
    		<Recipe key={i} {...recipe} />
    	)}
    </div>
</article>


const Recipe = ({name, ingredients, steps}) =>
<section id={name.toLowerCase().replace(/ /g, "-")}>
    <h1>{name}</h1>
    <ul className="ingredients">
        {ingredients.map((ingredient, i) =>
            <li key={i}>{ingredient.name}</li>
        )}
    </ul>
    <section className="instructions">
        <h2>조리 절차</h2>
        {steps.map((step, i) => 
            <p key={i}>{step}</p>
        )}
    </section>
</section>

ReactDOM.render(<Menu recipes={data} title="Cooking!"/>,
		document.getElementById('react-container'))
```
<hr/>

<h2>#4. Web Pack</h2>

* 웹 팩에 대해서는 나중에 정리한다.