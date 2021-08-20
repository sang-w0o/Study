JavaScript의 함수
======

<h2>JavaScript의 함수</h2>
<hr/>

* 익명 함수 생성 방법
  ```js
  var FUNC = function(){/* 함수 구현부 */};
  ```

* 선언적 함수 생성 방법
  ```js
  function FUNC(){
      // 함수 구현부
  };
  ```
  * 특징 : 함수의 선언보다 호출을 먼저 해도 된다.
  * 그 이유는 JavaScript는 함수의 정의부를 먼저 검색하고 읽어들이기 때문이다.
  * 하지만 익명함수는 runtime시에 인자로 값을 넣을 수 있으므로   
    익명함수는 반드시 선언을 먼저 하고 호출해야 한다.

* 가변 인자 함수
  * 가변 인자 함수는 인수 목록을 비워두어야 한다.
  * 가변 인자 함수는 호출 시 들어오는 인수를 __arguments__ 라는 객체에 저장한다.
    * JavaScript의 모든 함수는 내부에 인수를 저장하는 __arguments__ 변수가 있다.
  ```js
  // 함수의 선언
  function func(){
      for(var value in arguments){
          document.write(arguments[value] + '<br/>');
      }
  }

  // 호출
  func(10, 20, 30, 40, 50, 60);
  ```

  * JavaScript는 어떠한 함수의 인수로 함수를 받을 수도 있다.
  ```js
  // 2번째 인수가 함수인 out함수의 선언
  function out(a, func){
      return func(a);
  }

  // out함수의 호출
  out(10, function(a){ /*인수로 넣을 익명 함수의 구현부 */});

  // 2번째 인수가 함수인 func 함수의 선언
  function func(cnt, callFunc){
      callFunc(cnt);
  }

  // func함수의 호출
  func(100, function(cnt){
      alert(cnt);
  });
  ```

* JavaScript에서는 함수가 함수를 반환할 수 있다.
  ```js
  // 함수를 반환하는 func함수의 선언
  function func(){
      return function(){
          alert('Function return example.');
      }
  }

  // 사용 예제
  var f = func(); 
  // func함수는 함수를 반환하므로 f는 함수를 저장한 변수이다.
  f();  // func에서 반환된 함수의 호출

  func()();
  // func함수에서 반환한 함수를 변수에 저장하지 않고 바로 실행한다.

  ```
  * 익명함수를 선언과 동시에 실행하는 예제
    ```js
    // 인수가 없는 익명함수의 선언과 동시에 실행
    (function(){
        alert('ABCD');
    })();

    // 인수가 있는 익명함수의 선언과 동시에 실행
    (function(str){
        alert(str);
    })("HELLO");
    ```

* 콜백(Callback) 함수
  * JavaScript에는 함수도 하나의 자료형이므로 매개변수로 전달할 수 있다.
  * 이렇게 매개변수로 전달하는 함수를 __callback__ 함수라고 한다.
  * 즉, 위의 예시 코드들 중 인수로 함수를 전달하는 예시에서 이미 사용한 것이다.
  * 콜백 함수의 특징은 다음과 같다.
    * a()함수 내에서 b()함수를 실행한다고 하자.
    * 보통 프로그래밍 언어에서 함수 내에서 함수를 수행하면, a함수는   
      b함수의 수행이 끝날 때 까지 기다리지만, callback함수를 이용하면   
      a는 a함수대로, b는 b함수대로 수행하게 된다.
    * 즉, a함수가 b함수의 수행이 끝날 때 까지 기다리지 않는다.

* 클로저(Closer)
  * 함수에서 반환한 함수 또는 사라지지 않고 "살아남은" 지역 변수
  ```js
  // 함수를 반환하는 함수 test의 선언
  function test(name){
      var output = "Hello, " + name;
      return function(){
          alert(output);
      }
  }

  // test함수의 호출
  test('World');  // Hello, World 출력
  ```
  * 위 예시 코드를 보면, 변수 output은 test함수가 종료될 때,   
    즉, return문을 만날 때 사라져야 하지만, 함수를 반환하는 경우   
    해당 변수가 이후에도 사용될 가능성이 있으므로 JavaScript는   
    변수를 제거하지 않고 남겨둔다. 이를 __클로저__ 라 한다.

* JavaScript 내장 함수
  <table border="3">
    <tr>
        <td>setTimeout(function, millisecond)</td>
        <td>일정 시간 후 인수의 함수를 1번 실행한다.</td>
    </tr>  
    <tr>
        <td>setInterval(function, millisecond)</td>
        <td>일정 시간마다 함수를 반복해서 실행한다.</td>
    </tr> 
    <tr>
        <td>clearTimeout(id)</td>
        <td>일정 시간 후 함수를 1번 실행하는 작업을 중단한다.</td>
    </tr> 
    <tr>
        <td>clearInterval(id)</td>
        <td>일정 시간마다 함수를 반복실행하는 작업을 중단한다.</td>
    </tr> 
  <table>

  ```js
  var id = setTimeout(function(){
      alert('10초가 지남');
  } , 10000);

  // 위 함수의 첫번째 함수로 전달된 익명함수는 callback 함수이다.
  // 10초가 지난 후, 알림창이 뜬다.
  ```

  ```js
  // html파일에 id가 clock인 input 태그가 있다고 하자.
  var clokcId = null;
  function start(){
      clockId = setInterval(function(){
          let today = new Date();  //날짜 객체 생성
          let clockStr = today.getYear() + 1900 + '-';
          clockStr += today.getMonth() + 1 + '-';
          clockStr += today.getDate() + ' ';
          clockStr += today.getHours() + ':';
          clockStr += today.getMinutes() + ':';
          clockStr += today.getSeconds();

          //document영역에서 clock이 id인 태그를 찾아 value를 수정한다.
          document.getElementById('clock').value = clockStr;
      }, 1000);
  }

  // 위 함수에 의해 1초마다 출력되는 시간이 갱신된다.

  // start함수 내의 setInterval함수를 멈추는 stop함수
  function stop(){
      clearInterval(clockId);
  }
  ```

* JavaScript 내장 함수 - eval()
  <table border="3">
    <tr>
        <td>eval(string)</td>
        <td>string을 JavaScript 코드로 실행한다.</td>
    </tr>
  </table>

  ```js
  var willEval = '';
  willEval += 'var number = 10;';
  willEval += 'alert(number)';

  //eval함수 호출
  eval(willEval);

  // 10 출력
  ```

* JavaScript 내장 함수 - 숫자 변환 함수
  <table border="3">
    <tr>
        <td>parseInt(string)</td>
        <td>string을 정수로 바꾼다.</td>
    </tr>
    <tr>
        <td>parseFloat(string)</td>
        <td>string을 유리수로 바꾼다.</td>
    </tr>
  </table>

<hr/>
<h2>#3. ECMA6에 추가된 내용</h2>

* JavaScript의 실행 순서
  ```js
  alert('a');
  setTimeout(function(){
      alert('b');
  }, 0);
  alert('c');

  // 결과 : A --> C --> B 순서대로 출력
  ```
  * javaScript의 함수 중에는 웹 브라우저에 처리를 부탁하는 함수가 있다.
  * 대표적으로 타이머 함수와 웹 요청 관련 함수가 있으며, 이러한 함수들은   
    웹 브라우저가 처리하고, 처리가 완료되었음을 javaScript에 알려준다.
  * 이러한 함수들은 현재 실행중인 다른 코드가 끝나기 전에는 실행되지 않는다.

* 반복문과 콜백 함수
  ```js
  for(var i = 0; i < 3; i++) {
      setTimeout(function(){
          alert(i);
      }, 0);
  }

  // 실행 결과 : 3 3 3 출력
  ```
  * 위 함수의 결과가 1 2 3 이 아니라 3 3 3 인 것은 setTimeout함수를   
    호출하는 시점이 반복문이 모두 끝난 이후이기 때문이다.
  * 다음과 같이 자기 호출 함수와 클로저를 사용하여 해결할 수 있다.
  ```js
  for(var i = 0; i < 3; i++) {
      (function(closed_i){
          setTimeout(function(){
              alert(closed_i);
          }, 0);
      })(i);
  }

  // 또다른 해결 방법
  [0,1,2].forEach(function(i){
      setTimeout(function(){
          alert(i);
      }, 0);
  });
  ```

* 기본 매개변수 : 매개변수를 입력하지 않았을 때, 매개변수를 강제 초기화하는 것.
  * 즉, 매개변수가 undefined자료형이면, 값을 넣는 것이다.
  ```js

  function test(a,b,c){
      if(!b) {b =2}
      if(!c) {c = 3}

      alert(a + ":" + b + ":" + c);
  }

  test(1);  // 1:2:3 출력

  //ECMA6의 기본 매개변수
  function test(a, b=2, c=3){
      alert(a + ":" + b + ":" + c);
  }

  test(1);  // 1:2:3 출력
  ```
* 화살표 함수 - ECMAScript 6
  * 익명 함수는 다음과 같이 사용한다.
  ```js
  function () { }
  ```
  * 화살표 함수를 이용하면 다음과 같이 사용할 수 있다.
  ```js
  () => {}
  ```
  * 화살표 함수와 익명 함수는 내부적으로 사용하는 this키워드의 의미가 다르다.
    * 익명함수 : 함수 자체에 바인딩된 객체(window 또는 prototype 객체)
    * 화살표함수 : 전역 객체(웹 브라우저 환경에서는 window 객체)
  * 화살표 함수의 예시 코드
  ```js
  const multiply = (a, b) => a * b;

  alert(multiply(1,2));
  alert(multiply(3,4));
  ```

* 함수에서의 전개 연산자 - ECMAScript 6
  * 전개 연산자는 가변 매개변수 함수 함수를 만들 때 이용한다.
  * 기존에는 arguments 객체를 사용하여 가변인자를 참조했다.
  ```js
  // 기존 JavaScript의 가변 매개변수 함수
  function test(){
      alert(arguments[0]);
      alert(arguments[1]);
      alert(arguments[2]);
  }

  test(1, 2, 3);  // 1 2 3 출력

  // 전개 연산자를 사용한 가변 매개변수 함수
  function test(...numbers){
      alert(numbers[0]);
      alert(numbers[1]);
      alert(numbers[2]);
  }

  test(1, 2, 3);  // 1 2 3 출력
  ```
  * 전개 연산자는 __반드시 가장 뒤에 딱 하나__ 만 사용해야 한다.

* 함수 호출 시의 전개 연산자 사용
  * 모든 함수에는 apply() 메소드가 있다. 
  * apply메소드의 매개변수
    * 첫 번째 매개변수 : 함수 내부에서 활용할 this 객체
    * 두 번째 매개변수 : 매개변수 배열
  ```js
  // 기존 javaScript에서 함수의 매개변수에 배열을 전개해 넣는 방법
  function test(a, b, c, d){
      alert(a + ":" + b + ":" + c + ":" + d);
  }

  var array[1, 2, 3, 4];
  test.apply(null, array);

  // ECMAScript 6에서 배열을 전개해서 매개변수로 전달하기
  function test(a, b, c, d){
      alert('${a}:${b}:${c}:${d}');
  }

  // 사용 방법 (1)
  var array[1, 2, 3, 4];
  test(...array);

  // 사용 방법 (2)
  var array = [1, 2];
  // 앞에 다른 매개변수를 넣고 뒤를 배열로 채울 때
  test(333, 444, ...array);  // 333:444:1:2 출력
  // 배열을 병합하여 매개 변수로 전달하기
  test(...array, ...array);  // 1:2:1:2 출력
  ```
