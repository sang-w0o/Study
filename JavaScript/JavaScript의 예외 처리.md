JavaScript의 예외 처리
======

<h2>기본 예외 처리</h2>

<hr/>

* If문 등의 조건문으로 예외를 검사하여 처리하는 것.

<hr/>

<h2>고급 예외 처리</h2>

<hr/>

* try-catch-finally 구문을 이용하여 예외를 처리하는 것.
```js
try{   
    willExcept.byeBye();
} catch(exception) {

}
```
* 이 예외 처리 구문은 JavaScript에 유일한 것이 아니기에 자세한 설명은 생략한다.
* try catch문을 이용한 이벤트 연결
```HTML
<body>
<h1 id="header">Click</h1>
</body>
```

```js
function registerEventListener(node, event, listener){
    try{
        // 최신 버전의 웹 브라우저
        node.addEventListener(event, listener, false);
    } catch(exception) {
        // 구 버전의 웹 브라우저
        node.attachEvent('on' + event, listener);
    }
}

window.onload = function(){
    var header = document.getElementById('header');
    registerEventListener(header, 'click', function(){
        alert('Click');
    });
};
```

<hr/>

<h2>예외 객체</h2>

<hr/>

* 예외 객체란, try-catch구문 이용 시 catch구문에 입력하는 식별자를 의미한다.
* 일반적으로는 e 또는 exception 식별자를 이용한다.

* 모든 브라우저가 갖는 예외 객체의 속성
<table>
    <tr>   
        <td>message</td>
        <td>예외 메시지</td>
    </tr>
    <tr>   
        <td>description</td>
        <td>예외 설명</td>
    </tr>
    <tr>   
        <td>name</td>
        <td>예외 이름</td>
    </tr>
</table>

* 예외 객체의 속성을 알 수 있는 코드
```js
try {
    // 할당 최대 크기보다 더 큰 배열을 할당받았다.
    var array = new Array(99999999999999999999999);
} catch(exception) {
    var output = '';
    for(var i in exception) {
        output += i + ":" + exception[i] + '\n';
    }
    alert(output);
}
```

<hr/>

<h2>예외 강제 발생</h2>

<hr/>

* 예외를 강제로 발생시킬 때는 __throw__ 키워드를 이용한다.

```js
// 0으로 나누는 것은 불가능하므로 이에 대한 예외처리를 해줘야 한다.
function divide(a, b) {
    if(b == 0){
        throw "DivideByZeroException";
    } else {
        return a / b;
    }
}

try {
    divide(10, 0);
} catch(exception) {
    alert('CATCH!');
}
```

* finally 구문은 타 언어와 마찬가지의 기능을 하므로 생략한다.

<hr/>