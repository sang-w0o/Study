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

<h2>#3. JSX로 작성한 코드 예시 (조리법)</h2>

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

* React를 Production에 사용하려면 다음과 같이 고려할 사항이 꽤 많다.
  * JSX와 ES6 이상의 트랜스파일링 처리
  * 프로젝트의 의존관계 관리
  * 이미지와 CSS의 최적화

* 위와 같은 사항들을 해결해주는 도구들 중 하나가 바로 웹팩이다.
* 웹팩은 모듈 번들러(Module Bundler)로, 여러 파일들을 받아서 하나의 파일로 묶는다.
* 이 때, 모듈성(Modularity)와 네트워크 성능(Network Performance)의 이익을 볼 수 있다.
  * 모듈성 : 소스코드를 작업하기 쉽게 여러 부분 또는 모듈로 나눠서 다루는 것.
  * 의존 관계가 있는 파일을 묶은 번들을 브라우저가 한 번만 읽기에 네트워크 성능도 좋아진다.
    * 각 script태그는 HTTP요청을 만들며, 이러한 HTTP요청마다 약간의 대기시간이 발생한다.
    * 모든 의존 관계를 하나의 파일로 묶으면 한번의 HTTP요청으로 가져올 수 있으므로   
        그만큼 추가 대기 시간을 방지할 수 있는 것이다.

* 트랜스파일링 외에 웹팩이 처리할 수 있는 작업
  * 코드 분리 : 코드를 여러 덩어리러 나누어 필요 시 각각을 로딩할 수 있다.
    * 이를 Rollup 또는 Layer라고 부르기도 한다.
  * 코드 축소 : 공백, 줄바꿈, 긴 변수명 등을 없애 파일의 크기를 줄인다.
  * 특징 켜고 끄기 : 코드 기능 테스트 시 코드를 각각의 환경에 맞춰 보낸다.
  * HMR(Hot Module Replacement) : 소스 코드의 변경을 감지해서 바뀐 코드만 즉시 갱신한다.

* 웹팩 모듈 번들러를 사용할 때의 이점
  * 모듈성 : CommonJS의 모듈 패턴을 이용해 모듈을 외부로 export하고, 나중에 그 모듈을   
    필요한 곳에 import해서 사용할 수 있다.
  * 조합 : 모듈을 사용하면 application을 효율적으로 구축할 수 있고, 작고 단순하며   
    재사용하기 쉬운 React Component를 구축할 수 있다. Component가 작으면 이해하고,   
    테스트하거나 재사용하기 쉽다.
  * 속도 : 모든 application을 하나의 파일에 packaging하면 클라이언트에서 단 한번만   
    HTTP요청을 보내면 된다.
  * 일관성 : 웹팩이 JSX를 React로, ES6나 ES7를 일반 JS로 변환해주기 때문에 아직   
    비표준인 JS 문법이나 미래의 문법을 사용할 수 있다. 
<hr/>

<h3>#4 - (1) 웹팩 로더</h3>

* Loader : 빌드 과정에서 코드를 변환하는 방식을 처리하는 기능
  * 앱이 ES6, JS 등 브라우저가 이해할 수 없는 언어를 사용한다면 webpack.config.js에   
    필요한 로더를 지정해서 앱 코드를 브라우저가 이해할 수 있는 코드로 변환해야 한다.
  * 이전에 사용한 babel 또한 웹팩에서 제공하는 수많은 로더 중 하나이다.
<hr/>

<h3>#4 - (2) 웹팩 빌드를 사용한 위의 조리법 코드</h3>

* Component를 Module로 나누기
* 이전의 Recipe 컴포넌트 코드를 보자.
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
* 위 컴포넌트는 h1태그에 조리법 제목을 표시하고, 재료들의 ul을 만들고, 조리 절차의   
  각 단계를 p 요소로 만들어 표시하는 작업을 한다.
* Recipe를 더 작고 담당하는 기능 범위가 더 좁은 여러개의 함수형 컴포넌트로 분리하고,   
  그렇게 나온 컴포넌트들을 합성하는 방식으로 처리하면 더욱 함수적인 접근 방식이 된다.

* 먼저 조리 절차를 상태가 없는 함수형 컴포넌트로 따로 분리하고, 독립 파일에   
  모듈로 만들어서 어떠한 절차든 표시할 수 있는 기능으로 분리하자.
```js
const Instructions = ({ title, steps}) =>
    <section className="instructions">
        <h2>{title}</h2>
        {steps.map((step, i) => 
            <p key={i}>{step}</p>
        )}
    </section>

export default Instructions
```

* 다음으로는 재료(Ingredient)를 살펴보자. 기존 Recipe 컴포넌트에서는 재료명만 보여준다.   
  하지만 재료 데이터에는 분량(amount)와 단위(measurement) 정보도 있으므로 이들까지   
  표시해주는 컴포넌트를 만들어보자.
```js
const Ingredient = ({ amount, measurement, name }) => 
    <li>
        <span className="amount">{amount}</span>
        <span className="measurement">{measurement}</span>
        <span className="name">{name}</span>
    </li>

export default Ingredient
```

* 이제 재료들의 목록을 표시하는 IngredientList 컴포넌트를 Ingredient 컴포넌트를   
  활용하여 만들어보자.
```js
import Ingredient from './Ingredient'

const IngredientList = ({ list }) =>
    <ul className="ingredients">
        {list.map((ingredient, i) => 
            <Ingredient key={i}{...ingredient} />
        )}
    </ul>

export default IngredientList
```

* 위 파일을 이용하여 각 재료를 표시하기 위해서는 먼저 Ingredient 컴포넌트를   
  import 해야 한다. 이 컴포넌트에 전달되는 재료의 배열은 list라는 property   
  에 들어 있는 배열이다.
* IngredientList를 분석해보자.
```js
<Ingredient {...ingredient} />
```
* 위 코드에서는 스프레드 연산자를 사용한다. 위 코드는 아래와 같다.
```js
<Ingredient amount={ingredient.amount}
            measurement={ingredient.measurement}
            name={ingredient.name} />
```

* 이제 Recipe 컴포넌트를 리팩토링해보자.
