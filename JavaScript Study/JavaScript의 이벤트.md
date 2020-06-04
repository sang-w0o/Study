이벤트
======

<h2>이벤트</h2>

<hr/>

* 이벤트란, 키보드나 마우스 등을 이용해 어떠한 상황이 발생하는 것을 의미한다.
* JavaScript에는 다음과 같은 이벤트가 있다.
  * 마우스 이벤트
  * 키보드 이벤트
  * HTML 프레임 이벤트
  * HTML 입력 양식 이벤트
  * 유저 인터페이스 이벤트
  * 구조 변화 이벤트
  * 터치 이벤트

```js
winidow.onload = function() {};
```
* 위 코드는 window객체의 onload속성에 함수 자료형을 할당했다.
* 즉, __이벤트를 연결__ 한 것이다.
* 이 때, load를 __Event Type__ 이라 하며, onload를 __이벤트 속성__ 이라 한다.
* 이벤트 속성에 할당한 함수를 __Event Listener__ 또는 __Event Handler__ 이라 한다.

* 이벤트 모델 : 문서 객체에 이벤트를 연결하는 방법
* 이벤트 모델은 DOM Level에 따라 두 가지로 분류된다.
  * DOM Level 0
    * 인라인 이벤트 모델
    * 기본 이벤트 모델
  * DOM Level 1
    * Microsoft IE 이벤트 모델
    * 표준 이벤트 모델

<hr/>

<h2>고전 이벤트 모델</h2>

<hr/>

* 고전 이벤트 모델 : JavaScript에서 문서 객체의 이벤트 속성으로 이벤트를 연결하는 방법
* 고전 이벤트 모델은 에빈트 하나에 이벤트 리스너를 한 개만 연결할 수 있다.
```HTML
<body>
    <h1 id="header"></h1>
</body>
```

```js
window.onload = function(){
    var header = document.getElementById('header');
    
    // 이벤트 연결
    header.onclick = function(){
        alert('click');
    };
};
```
<hr/>

<h2>이벤트 발생 객체와 이벤트 객체</h2>

<hr/>

* 이벤트 핸들러 안에서의 this 키워드
```HTML
<body>
    <h1 id="header">Click</h1>
</body>
```

```js
window.onload = function(){
    document.getElementById('header').onclick = function(){
        alert(this);
        // [object HTMLHeadingElement] 출력
    }
}
```

* 이벤트 핸들러 내의 this키워드는 이벤트가 발생한 객체를 의미한다.
```js
window.onload = function(){
    document.getElementById('header').onclick = function(){

        // this는 이벤트가 발생한 객체이므로 
        // 스타일을 지정하면 해당 객체의 스타일이 변경된다.
        this.style.color = 'orange';
        this.style.backgroundColor = 'red';
    }
}
```

<hr/>

<h2>이벤트 강제 실행</h2>

<hr/>

* 이벤트 속성도 속성이고, 함수 자료형을 넣으므로 메소드이다.
* 따라서 다음과 같이 메소드를 호출하는 것 처럼 이벤트 속성을 호출하면, 이벤트가 강제로 실행된다.

```js
header.onclick();  // header객체의 click이벤트를 강제로 발생시킨다.
```

<hr/>

<h2>인라인 이벤트 모델</h2>

<hr/>

* 인라인 이벤트 모델은 HTML페이지의 가장 기본적인 이벤트 연결 방법이다.

```html
<h1 onclick="alert('클릭됨')">Click</h1>
```

<hr/>

<h2>디폴트 이벤트 제거</h2>

* 일부 HTML태그는 디폴트 이벤트 리스너가 있다. 
* 예를들어 a태그는 클릭 시 다른 페이지로 이동하고, submit input type은 자동으로 입력 양식을 제출한다.

```html
<body>
    <form id="my-form">
        <input type="submit" value="제출" />
    </form>
</body>
```

```js

// submit에 대한 디폴트 이벤트 제거
window.onload = function(){
    document.getElementById('my-form').onsubmit = function(){
        return false;
    };
};
```

<hr/>

<h2>이벤트 전달</h2>

<hr/>

* 아래 코드를 먼저 보자
```html
<body>
    <div onclick = "alert('outer-div')">
        <div onclick = "alert('inner-div')">
            <h1 onclick = "alert('header')">
                <p onclick = "alert('paragraph')">Paragraph</p>
            </h1>
        </div>
    </div>
</body>
```

* 위 코드를 실행하면, 분명히 이벤트가 발생하는 순서가 있을 것이다.
* 이렇게 이벤트가 어떠한 순서로 발생하는지를 가리켜 __이벤트 전달__ 이라 한다.
* 일반적으로 JavaScript의 이벤트 전달 순서는 __이벤트 버블링 방식__ 을 따른다.
* __이벤트 버블링 방식__ 은 자식 노드에서 부모 노드 순으로 이벤트를 실행하는 것을 의미한다.
* 따라서 위 코드는 paragraph -> header -> inner-div -> outer-div의 순서로 출력된다.

<hr/>

<h2>표준 이벤트 모델</h2>

<hr/>

* 표준 이벤트 모델은 웹 표준 단체인 W3C에서 지정한 DOM Level2의 이벤트 모델이다.
* 표준 이벤트 모델은 이벤트 연결 시 다음 메소드를 이용한다.
<table>
    <tr><td>addEventListener(eventName, handler, useCapture)</td></tr>
    <tr><td>removeEventListener(eventName, handler)</td></tr>
</table>

* addEventListener의 매개변수인 useCapture은 미지정 시 false로 된다.
* capture는 사실상 사용하지 않는 기능이다.