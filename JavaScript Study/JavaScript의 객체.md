JavaScript의 기본적인 내용(3)

<h2>객체</h2>

* JavaScript의 객체는 중괄호 { }로 묶어서 선언한다.
* 또한 객체의 멤버는 배열식으로 참조할 수 있으며, 객체식으로 참조할 수 있다.
  ```js
  var Person {name:'thisisname', age:23}

  // 객체식으로 참조
  alert(Person.name);  // thisisname 출력

  // 배열식으로 참조
  alert(Person['age']);  // 23 출력
  ```
* 단, 식별자가 문자가 아닌 키로 사용할 경우는 무조건 배열식으로 참조해야 한다.

<hr/>

* 객체 내의 값은 __property__ 이라 하며, 함수 자료형의 속성은 __method__ 라 한다.
* 객체에 메소드 멤버 넣기
  ```js
  var Person = { name:'thisisname',
                 age : 23,
                 info : function(){
                     alert(this.name + this.age);
                    }
                 }
  };

  // Person객체의 메소드 호출
  Person.info();
  ```

<hr/>

* __in__ 키워드 : 객체 내에 해당 속성 또는 메소드가 존재하는지 확인한다.
  ```js
  var student = {
      name:'sname',
      age:'23',
      score:100
  };

  // in 키워드 사용 예시
  var output1 = '';
  output += ('name' in student) + '\n';
  // true출력

  var output2 = '';
  output2 += ('ID' in student) + '\n';
  // false 출력
  
  ```

<hr/>

* __with__ 키워드 : 객체의 접근을 간편하게 해준다.
  ```js
  var Student = {};

  // with 키워드 사용 전

  Student.name = 'sname';
  Student.major = 'software';
  Student.grade = 3;

  // with 키워드 사용 시
  with(student) {
      name = 'sname';
      major = 'software';
      grade = 3
  }
  ```

  <hr/>

* __delete__ : 객체의 속성 삭제
  ```js
  // 위 코드의 Student 객체에서 major 속성 삭제하기
  delete Student.major;
  ```

<hr/>

* 옵션 객체의 초기화
  * 옵션 객체 : 함수의 매개변수로 전달하는 객체
  ```js
   // Masonry 라이브러리의 예시
   var masonry = new Masonry('.grid', {
       columnWidth : 200,
       itemSelector : '.gried-item'
   });
  ```
  * 옵션 객체는 말 그대로 옵션으로, 입력해도 되고 입력하지 않아도 된다.
  * 따라서 기본 매개변수처럼 값을 입력하지 않을 시 초기화해주는 작업이 필요하다.
  ```js
  function test(options){
      // 옵션 객체의 초기화
      options.valueA = options.valueA || 10;
      options.valueB = options.valueB || 20;
      options.valueC = options.valueC || 30;

      alert(options.valueA + ":" + options.valueB + ":" + options.valueC);
  }

  //test 호출
  test ({
      valueA : 11,
      valueB : 33
  });

  // 결과 : 11:20:33 출력
  ```
<hr/>

* 참조 복사와 값 복사
  ```js
  // 값 복사(깊은 복사)의 예시
  var originalValue = 10;
  var newValue = originalValue;

  originalValue = 20;

  alert(originalValue);  // 20 출력
  alert(newValue);  // 10 출력

  // 참조 복사(얕은 복사)의 예시
  var originalArray = [1,2,3];
  var newArray = originalArray;

  originalArray[0] = 99;

  alert(originalArray);  // 99,2,3 출력
  alert(newArray);  // 99,2,3 출력
  ```

  * JavaScript는 기본 자료형(Number, String, Bool)을 복사할 때 값을 완전히 복사한다.
  * 하지만 객체(배열 포함)를 복사할 때는 복사본과 원본이 객체가 가지는 메모리를 같이 가리키게 된다.
  * 객체의 깊은 복사 방법
  ```js
  var ary = [10, 20, 30, 40];
  
  // 방법 (1)
  var ary1 = Array();
  for(var i = 0; i < ary.length; i++) {
      ary1.push(ary[i]);
  }

  // 방법 (2)
  var ary2 = [...ary];
  ``` 

* 전개 연산자를 사용한 배열 테크닉 - ECMAScript 6
  * ECMAScript 5까지의 배열 복제 방법
  ```js
  var originalArray = [1, 2, 3, 4, 5];

  // 복제
  var newArray = new Array();
  for(var i = 0; i < originalArray.length; i++){
      newArray.push(originalArray[i]);
  }
  ```

  * 전개 연산자를 사용한 배열 복제
  ```js
  const originalArray = [1, 2, 3, 4, 5];
  const newArray = [...originalArray];
  ```

  * 전개 연산자를 이용한 배열의 병합
    * 전개 연산자를 두번 이용하면 배열을 병합할 수 있다.
  ```js
  // 예시 1
  const arrA = [1, 2, 3, 4, 5];
  const arrB = [11, 12, 13, 14, 15];

  const newArr = [...arrA, ...arrB];
  alert(newArr);  // 1,2,3,4,5,11,12,13,14,15 출력

  const newArr2 = [111, 222, 333, ...arrA];
  alert(newArr2);  // 111,222,333,1,2,3,4,5 출력
  ```