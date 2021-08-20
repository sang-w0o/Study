JavaScript의 기본 자료형과 연산자
======

* 이 문서는 다른 프로그래밍 언어와 동일한 연산자, 문법 등은 다루지 않고   
  JavaScript 특유의 내용을 다룹니다.
<hr/>
<h2>기본 자료형과 연산자</h2>
<hr/>

* JavaScript에는 크게 number, string, bool 자료형이 존재한다.

* 자료형의 검사
  * number, string, bool등의 자료형을 검사할 때는 __typedef__ 연산자를 사용한다.
```js
typeof(128);  // --> number
```

* 변수의 선언
* 전역 변수 : __var__ 키워드를 이용하여 선언한다.  
  * __var__ 로 선언된 변수는 재선언이 가능하다.
* 지역 변수 : __let__ 키워드를 이용하여 선언한다.
  * __let__ 으로 선언된 변수는 재선언이 불가하다.
* 상수의 선언
  * 상수는 __const__ 키워드를 이용하여 선언한다.
  * 상수는 반드시 선언과 동시에 초기화를 해야하며,   
    재선언 및 값 변경이 불가하다.
* JavaScript는 함수를 변수로 선언할 수도 있다.
  (예시 코드)
```js
var number = 3;
var stringVar = "String";
var booleanVar = true;
var functionVar = function(){};
var objectVar = {};

alert(number * 2);
// alert는 브라우저 상에 알림창을 띄우는 함수이다.
```

* undefined 자료형
  * 선언하지 않은 변수 또는 초기화되지 않은 변수를 참조할 때,   
    해당 자료형은 undefined 자료형이 된다.

* javaScript 특유의 연산자 (일치 연산자)
  <table border="3">
    <tr>
        <td>===</td>
        <td>양쪽 변의 자료형과 값이 일치하면 true반환</td>
    </tr>
    <tr>
        <td>!==</td>
        <td>양쪽 변의 자료형과 값이 다르면 true 반환</td>
    </tr>
  </table>

* 배열 선언 방법
  * (1) 선언과 동시에 초기화
  ```js
  var ary = [10, 20, 30, 40];
  var ary2 = Array(10, 20, 30, 40);
  ```
  * (2) 가변 배열 선언 (선언과 초기화를 따로 한다.)
  ```js
  var ary = Array();
  ary[0] = 10; ary[1] = 20; ary[2] = 30;
  ```

* 배열에서 자주 쓰이는 속성과 메소드
  * length : 배열의 길이값 반환
  * push() : 배열에 원소를 추가하는 메소드
  * for-in 반복문
  ```js
  var ary = [1,2,3,4,5];
  for(var value in ary){
      // 반복할 문장
  }
  ```
