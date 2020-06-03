JavaScript의 기본적인 내용(5)
======

<h2>기본 내장 객체</h2>

* JavaScript 기본 자료형과 객체의 차이
  ```js
  var primitiveNumber = 100;
  var objectNumber = new Number(100);
  ```
  * 기본 자료형에도 객체와 마찬가지로 속성과 메소드가 있다.
  * 하지만 기본 자료형에는 속성과 메소드를 추가할 수 없다.
  * 굳이 추가하고 싶다면, prototype에 메소드를 추가하면 된다.
  ```js
  Number.prototype.method = function(){
      return 'Method on prototype.';
  }
  ```

<hr/>

<h3>Object 객체</h3>

* JavaScript의 __최상위 객체__
* Object 객체의 메소드
  
<table>
   <tr>
        <td>constructor()</td>
        <td>객체의 생성자 함수</td>
    </tr>
     <tr>
        <td>hasOwnProperty(name)</td>
        <td>객체가 name 속성이 있는지의 유무 반환</td>
    </tr>
     <tr>
        <td>isPrototypeof(object)</td>
        <td>객체가 object의 prototype인지의 유무 반환</td>
    </tr>
     <tr>
        <td>propertyIsEnumerable(name)</td>
        <td>반복문으로 열거할 수 있는지의 유무 반환</td>
    </tr>
     <tr>
        <td>toLocaleString()</td>
        <td>객체를 호스트 환경에 맞는 언어의 문자열로 변환</td>
    </tr>
     <tr>
        <td>toString()</td>
        <td>객체를 문자열로 변환</td>
    </tr>
     <tr>
        <td>valueOf()</td>
        <td>객체의 값을 나타낸다.</td>
    </tr>
  </table>

* toString메소드는 객체를 문자열로 나타낼 때 자동으로 호출되므로, 주로 오버라이딩하여 사용한다.

<hr/>

* 자료형 구분
  * typeof 연산자를 이용한 구분의 문제점
  ```js
  var numberFromLiteral = 10;
  var numberFromObject = new Number(10);

  var output = '';
  output += '1. ' + typeof(numberFromLiteral) + '\n';
  output += '2. ' + typeof(numberFromObject) + '\n';

  alert(output);
  // 1. number 2. object 출력
  ```
    * 위의 두 변수는 모두 숫자이지만, 생성자 함수로 만든 숫자는 객체이므로 경고창을 출력하지 않는다. 
    * 이러한 두 대상을 같은 자료형으로 취급하고 싶을 때는 constructor() 메소드를 이용해야 한다.
  * constructor() 메소드를 이용한 자료형 비교
  ```js
  var numberFromLiteral = 10;
  var numberFromObject = new Number(10);

  if(numberFromLiteral.constructor == Number) {
      alert('number FromLiteral은 숫자이다.');
  }

  if(numberFromObject.constructor == Number){
      alert('numberFromObject는 숫자이다.');
  }

  // 출력 결과 두 개의 if문 모두 출력된다.
  ```
<hr/>

* 모든 객체에 공통적인 메소드 추가하기
  * Object객체는 모든 JavaScript객체의 최상위 객체이다.
  * 이를 이용하여 Object의 Prototype에 메소드를 추가하면 해당 메소드는   
    모든 JavaScript 객체가 이용할 수 있게 된다.
  ```js
  Object.prototype.test = function(){
      alert(this);
  }

  var number = 10;
  number.test();  // 10 출력
  ```
<hr/>

<h3>Number 객체</h3>

* 숫자를 표현할 때 사용되는 객체이다.
* Number 객체가 가지는 메소드
  
<table>
    <tr>
        <td>toExponential()</td>
        <td>숫자를 지수 표시로 나타낸 문자열 반환</td>
    </tr>
     <tr>
        <td>toFixed()</td>
        <td>숫자를 고정 소수점 표시로 나타낸 문자열 반환</td>
    </tr>
     <tr>
        <td>toPrecision()</td>
        <td>숫자를 길이에 따라 지수 또는 고정 소수점 표시로 나타낸 문자열 반환</td>
    </tr>
</table>

  ```js
  var number = 123.456789;

  var output = '';
  output += number.toFixed(1) + '\n';
  output += number.toFixed(4);
  alert(output);  // 123.4  123.4568 출력

  // 직접 호출하는 예시
  var fixedNumber = (123.4567).toFixed(2);
  alert(fixedNumber);  // 123.46 출력
  ```
* Number객체의 생성자 함수의 속성
<table>
    <tr>
        <td>MAX_VALUE</td>
        <td>JavaScript의 숫자가 나타낼 수 있는 최대 숫자</td>
    </tr>
    <tr>
        <td>MIN_VALUE</td>
        <td>JavaScript의 숫자가 나타낼 수 있는 최소 숫자</td>
    </tr>
    <tr>
        <td>NaN</td>
        <td>JavaScript의 숫자로 나타낼 수 없는 숫자</td>
    </tr>
    <tr>
        <td>POSITIVE_INFINITY</td>
        <td>양의 무한대 숫자</td>
    </tr>
    <tr>
        <td>NEGATIVE_INFINITY</td>
        <td>음의 무한대 숫자</td>
    </tr>
</table>
  
<hr/>

<h3>String 객체</h3>

* JavaScript에서 가장 많이 사용하는 내장 객체
* String 객체의 속성
<table>
    <tr>
        <td>length</td>
        <td>문자열의 길이값 반환</td>
    </tr>
</table>

* String 객체의 메소드
  * String객체의 메소드들은 자기 자신을 변화시키지 않고 새로운 문자열을 반환함을 주의한다.
  * 즉, 리턴값으로 받지 않으면 기본 문자열은 변화하지 않는다.
<table>
    <tr>
        <td>charAt(index)</td>
        <td>index에 위치한 문자 반환</td>
    </tr>
    <tr>
        <td>charCodeAt(index)</td>
        <td>index에 위치한 문자의 unicode값 반환</td>
    </tr>
    <tr>
        <td>concat(args)</td>
        <td>매개변수로 입력한 문자열을 이어붙여 반환</td>
    </tr>
    <tr>
        <td>indexOf(str)</td>
        <td>str과 일치하는 문자열이 있는 첫번째 위치 반환</td>
    </tr>
    <tr>
        <td>lastIndexOf(str)</td>
        <td>str과 일치하는 문자열이 있는 마지막 위치 반환</td>
    </tr>
    <tr>
        <td>matching(regExp)</td>
        <td>문자열 내에 정규표현식이 있는지의 유무 반환</td>
    </tr>
    <tr>
        <td>replace(regExp, replacement)</td>
        <td>문자열 내에 정규표현식과 맞는 곳을 replacement문자열로 변경 후 반환</td>
    </tr>
    <tr>
        <td>search(regExp)</td>
        <td>문자열 내에 정규표현식과 일치하는 문자열의 위치 반환</td>
    </tr>
    <tr>
        <td>slice(start, end)</td>
        <td>문자열의 start부터 end까지의 문자열을 반환</td>
    </tr>
    <tr>
        <td>split(separator, limit)</td>
        <td>seperator로 문자열을 잘라서 반환한다.</td>
    </tr>
    <tr>
        <td>substr(start, count)</td>
        <td>문자열의 start번째 부터 count개 만큼의 문자를 문자열로 반환</td>
    </tr>
    <tr>
        <td>substring(start, end)</td>
        <td>start부터 end까지 문자열을 잘라서 반환</td>
    </tr>
    <tr>
        <td>toLowerCase()</td>
        <td>문자열을 모두 소문자로 변환 후 반환</td>
    </tr>
    <tr>
        <td>toUpperCase()</td>
        <td>문자열을 모두 대문자로 변환 후 반환</td>
    </tr>
</table>

<hr/>

<h3>Array 객체</h3>

* Array 객체의 생성자
<table>
    <tr>
        <td>Array()</td>
        <td>빈 배열 생성</td>
    </tr>
    <tr>
        <td>Array(number)</td>
        <td>매개변수 크기의 배열 생성</td>
    </tr>
    <tr>
        <td>Array(el1, el2,...)</td>
        <td>매개변수를 배열로 만든다.</td>
    </tr>
</table>

* Array객체의 속성
<table>
    <tr>
        <td>length</td>
        <td>배열 요소의 개수 반환</td>
    </tr>
</table>

* Array 객체의 메소드
<table>
    <tr>
        <td>concat(arr)</td>
        <td>매개변수로 입력한 배열의 요소를 기존 배열에 붙여 새로운 배열 반환</td>
    </tr>
    <tr>
        <td>join()</td>
        <td>배열 내의 모든 요소를 문자열로 만들어 반환</td>
    </tr>
    <tr>
        <td>pop()</td>
        <td>배열의 마지막 요소 제거(자기자신 수정)</td>
    </tr>
     <tr>
        <td>push(element)</td>
        <td>배열의 마지막에 새로운 요소 삽입(자기자신 수정)</td>
    </tr>
     <tr>
        <td>reverse()</td>
        <td>배열의 원소 순서를 뒤집는다.(자기자신 수정)</td>
    </tr>
     <tr>
        <td>slice(startIndex, endIndex)</td>
        <td>배열의 startIndex부터 endIndex까지의 원소로 이루어진 배열 반환</td>
    </tr>
     <tr>
        <td>sort()</td>
        <td>배열의 원소들을 정렬(자기자신 수정)</td>
    </tr>
     <tr>
        <td>splice(index, count)</td>
        <td>배열의 index번 째 원소를 포함한 count개의 원소를 제거하고 삭제한 원소 반환</td>
    </tr>
</table>

* 배열을 정렬하는 sort() 메소드의 정렬 방법 지정하기
  * sort() 메소드는 기본적으로 매개 변수 2개를 받아야 한다.
  ```js
  array.sort(function(left, right)){
      
      // 오름차순 정렬
      return left - right;

      // 내림차순 정렬
      return right - left;
  }

  // 예시 코드
  var arry[10, 40, 20, 30];
  array.sort(function(left, right){
      return left - right;
  });

  alert(array);  // 10,20,30,40 출력
  ```

* 자주 사용하는 prototype함수 예시 - remove
  ```js
  Array.prototype.remove = function(index){
      this.splice(index, 1);
  }

  // 사용 예시
  var array = [52, 273, 103, 32, 274, 129];
  for(var i = array.length; i >= 0; i--){
      if(array[i] > 100)
        array.remove(i);
  }

  alert(array);  // 52,32 출력

  // 위 for문을 0번 index부터 돌면 원소가 삭제된 후 
  // index도 1개씩 줄기 때문에 제대로 동작하지 않는다.
  ```

<hr/>

<h3>Date 객체</h3>

* Date 객체는 날짜와 시간을 표시하는 객체이다.

* Date 객체의 생성자와 생성 방법
  ```js
  // 생성 방법 (1)
  var date = new Date();  //date는 객체가 생성된 시점의 날짜와 시각을 갖는다.

  // 생성 방법 (2)
  var date = new Date('June 9, 2020');

  // 생성 방법 (3)
  //var date = new Date(year, month-1, day, hour, minute, second);
  var date = new Date(2020, 5, 2, 20, 27, 11);  // 2020년 6월 2일 20시 27분 11초
  ```

* Date 객체의 메소드는 정말 다양하다.
  * 연, 월, 일, 시, 분, 초를 가져올 수 있는 getter 메소드
  * 연, 월, 일, 시, 분, 초를 가져올 수 있는 setter 메소드
  * 각종 format에 맞춘 문자열을 반환하는 toString 메소드
  ```js
  // 날짜 간격 구하는 예시
  var now = new Date();
  var before = new Date('July 14, 2018');

  // 날짜 간격 구하기
  var interval = now.getTime() - before.getTime();
  interval = Math.floor(interval / (1000 * 60 * 60 * 24));

  alert(interval + "일");
  ```

<hr/>

<h3>Math 객체</h3>

* Math 객체는 수학관련 연산을 제공한다.
  * JavaScript의 기본 내장 객체 중 유일하게 생성자 함수가 없다.

* Math 객체의 메소드 (매개변수를 종류에 관계없이 숫자로 변환하여 처리한다.)
<table>
    <tr>
        <td>abs(x)</td>
        <td>x의 절대값 반환</td>
    </tr>
    <tr>
        <td>ceil(x)</td>
        <td>x보다 크거나 같은 가장 작은 정수 반환</td>
    </tr>
    <tr>
        <td>floor(x)</td>
        <td>x보다 작거나 같은 가장 큰 정수 반환</td>
    </tr>
    <tr>
        <td>max(x,y,z...n)</td>
        <td>매개변수 중 가장 큰 값 반환</td>
    </tr>
    <tr>
        <td>min(x,y,z...n</td>
        <td>매개변수 중 가장 작은 값 반환</td>
    </tr>
    <tr>
        <td>pow(x,y)</td>
        <td>x의 y승 값 반환</td>
    </tr>
    <tr>
        <td>random()</td>
        <td>0과 1사이의 난수 반환</td>
    </tr>
    <tr>
        <td>round(x)</td>
        <td>x의 값을 반올림한 정수 반환</td>
    </tr>
    <tr>
        <td>sqrt(x)</td>
        <td>x의 제곱근 반환</td>
    </tr>
</table>

<hr/>

<h3>ECMAScript5 Array 객체</h3>

* Array생성자 함수에 추가된 메소드
<table>
    <tr>
        <td>Array.isArray()</td>
        <td>배열인지의 유무 반환</td>
    </tr>
</table>

* 탐색 메소드
<table>
    <tr>    
        <td>indexOf(element)</td>
        <td>특정 원소를 앞쪽부터 검색</td>
    </tr>
    <tr>
        <td>lastIndexOf(element)</td>
        <td>특정 원소를 뒤쪽부터 검색</td>
    </tr>
</table>

* 반복 메소드
<table>
    <tr>    
        <td>forEach()</td>
        <td>배열 각각의 요소를 사용해 특정 함수를 for-in반복문처럼 수행</td>
    </tr>
    <tr>
        <td>map()</td>
        <td>기존의 배열에 특정 규칙을 적용해 새로운 배열 생성</td>
    </tr>
</table>

  * forEach메소드에는 매개변수로 element, index, array를 넣는다.
  ```js
  var array = [1,2,3,4,5,6,7,8,9,10];

  var sum = 0;
  var output = '';
  array.forEach(function(element, index, array) {
      sum += element;
      output += index + ':' + element + '-->' + sum + '\n';
  });
  alert(output);
  ```

  * map메소드는 배열의 각 요소를 변경해 새로운 배열을 반환한다.
  ```js
  var array = [1,2,3,4,5,6,7,8,9,10];

  var output = array.map(function(element){
      return element * element;
  });

  alert(output);  // 1,4,9,16,25,36,49,64,81,100 출력
  ```

* 조건 메소드 (filter, every, some)
  * 조건 메소드는 forEach의 인수로 들어가는 함수와 같이 element, index, array를 인수로 사용한다.
<table>
    <tr>    
        <td>filter()</td>
        <td>특정 조건을 만족하는 원소를 추출해 새로운 배열 생성</td>
    </tr>
    <tr>
        <td>every()</td>
        <td>배열의 요소가 특정 조건을 모두 만족하는지의 유무 반환</td>
    </tr>
    <tr>
        <td>some()</td>
        <td>배열의 요소가 특정 조건을 적어도 하나 만족하는지의 유무 반환</td>
    </tr>
</table>

  ```js
  //filter() 메소드 사용 예시
  var array = [1,2,3,4,5,6,7,8,9,10];
  array = array.filter(function(element, index, array){
      return element <= 5;
  })
  alert(array);  // 1,2,3,4,5 출력
  ```

  * every()메소드 : 모든 요소가 조건을 만족해야만 true 반환
  * some() 메소드 : 배열의 모든 요소 중 적어도 1개가 조건을 만족하면 true 반환
  ```js
  var array = [1,2,3,4,5,6,7,8,9,10];
  function lessThanFive(element, index, array){
      return element < 5; 
  }
  
  function greaterThanZero(element, index, array){
      return element > 0;
  }

  var output1 = array.every(lessThanFive);
  alert(output1);  // false 출력
  
  var output2 = array.every(greaterThanZero);
  alert(output2);  // true 출력

  var output3 = array.some(lessThanFive);
  alert(output3);  // true 출력
  
  var output4 = array.some(greaterThanZero);
  alert(output4);  // true 출력
  ```

* 연산 메소드
<table>
    <tr>
        <td>reduce()</td>
        <td>배열의 요소가 하나가 될 때 까지 좌측부터 우측으로 원소를 2개씩 묶는다.</td>
    </tr>
    <tr>
        <td>reduceRight()</td>
        <td>배열의 요소가 하나가 될 때 까지 우측부터 좌측으로 원소를 2개씩 묶는다.</td>
    </tr>
</table>

  ```js
  var array = [1,2,3,4,5];
  
  var result = array.reduce(function(previousValue, currentValue){
      return previousValue + currentValue;
  });

  alert(result);  // 15 출력
  ```

<hr/>

<h3>ECMAScript 5 String 객체에 추가된 메소드</h3>

<table>
    <tr>
        <td>trim()</td>
        <td>문자열 양쪽 끝의 공백 제거</td>
    </tr>
</table>

<hr/>

<h3>JSON 객체</h3>

* JSON 객체 : Javascript객체의 형태를 갖는 문자열

<table>
    <tr>    
        <td>JSON.stringify()</td>
        <td>JavaScript객체를 JSON문자열로 반환</td>
    </tr>
    <tr>    
        <td>JSON.parse()</td>
        <td>JSON문자열을 Javascript객체로 반환</td>
    </tr>
</table>

```js
var object = {
    name:'thisisname',
    region:'seoul'
};

alert(JSON.stringify(object));
// {"name":"thisisname","region":"seoul"}
```

* toJSON() 메소드
  * JSON.stringify()의 매개변수로 들어간 객체에 toJSON() 메소드가 없다면   
    객체 전체를 JSON으로 변환한다.
  * 반면 toJSON() 메소드가 있다면 toJSON()에서 반환한 객체를 JSON으로 변환한다.
```js
var object = {
    name:'object',
    prop:'object',
    toJSON:function(){
        return {
            custom:'custom';
        };
    }
};

alert(JSON.stringify(object));
// {"custom":"custom"} 출력
```

<hr/>

<h3>화살표 함수를 이용한 Array 객체의 메소드 활용 - ECMAScript 6</h3>

```js
var students = [
    {name:'s1', kor:90, math:91, eng:78, science:89},
    {name:'s2', kor:100, math:79, eng:81, science:94}
];

// science 점수가 90점 이상인 학생을 필터링한다.
var filteredA = students.filter(function(item){
    return item.science > 90;
});

alert(filteredA);

// 평균 점수가 90점 이상인 학생을 필터링한다.
var filteredB = students.filter(function(item){
    return ((item.kor + item.math + item.eng + item.science) / 4) > 90;
});

alert(filteredB);
```

* 위 코드를 화살표 함수로 구현하면 다음과 같다.
```js
var filteredA = students.filter((item) => item.science > 90);
alert(filteredA);

var filteredB = students.filter((i) => ((i.kor + i.math + i.eng + i.science) / 4 > 90);
alert(filteredB);
```
