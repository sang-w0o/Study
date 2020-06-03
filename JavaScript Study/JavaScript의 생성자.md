JavaScript의 생성자
======

<h2>생성자</h2>

* 생성자 함수 : 객체를 생성할 때 사용하는 함수
  ```js
  // 객체의 선언
  function Person(name, sex, email){
      this.name = name;
      this.sex = sex;
      this.email = email;

      // 내부에 메소드 선언
      this.toString = function(){
          return this.name + ":" + this.sex + ":" + this.email;
      }
  }

  // 객체의 생성 및 멤버 메소드 호출
  var p = new Person('name', 'male', 'ex@naver.com');
  p.toString();

  // 이 때 위의 p를 객체 또는 인스턴스라고 부른다.
  ```
<hr/>

* 객체의 타입은 Java에도 있는 instanceof 연산자를 통해 검사할 수 있다.

<hr/>

* 프로토타입(Prototype)
  * Prototype : 생성자 함수로 생성된 객체가 공통으로 가지는 공간
  * Prototype에는 주로 객체가 가진 메소드들을 정의한다.
  * 위의 Person 생성자 함수의 toString메소드를 Prototype으로 생성하면 다음과 같다.
  ```js
  Person.prototype.toString = function(){
      return this.name + ":" + this.sex + ":" + this.email;
  }
  ```

<hr/>

* 캡슐화
  * 생성자 함수 내의 멤버 필드에 대한 외부의 접근을 막기 위한 것.
  * 생성자 함수에서 this가 아닌 var를 이용하여 멤버 필드를 선언한다.
  ```js
  function Regtangle(width, height){
      var width = width;
      var height = height;
  }
  ```
  * 위와 같이 var로 선언된 멤버 필드는 this로 접근할 수 없게 된다.
  * 따라서 var로 선언된 멤버 필드에 접근하기 위해 JavaScript에도   
    getter, setter 메소드들을 두게 된다.

<hr/>

* 상속 : 기존의 생성자 함수나 객체를 기반으로 새로운 생성자 함수나 객체를 만드는 행위
  ```js
  function Rectangle(width, height){
      var width = width;
      var height = height;

      this.getWidth = function() {return width;}
      this.getHeight = function() {return height;}
  }
  
  Rectangle.prototype.getArea = function(){
      return this.getWidth() * this.getHeight();
  }

  // Rectangle 생성자 함수를 상속하는 Square 생성자 함수
  function Square(length){

      // Rectangle 객체의 속성을 Square 객체에 추가
      this.base = Rectangle;
      this.base(length, length);
  }

  // Rectangle객체의 prototype이 가진 속성 또는 메소드를
  // Square객체의 prototype에 복사
  Square.prototype = Rectangle.prototype;
  Square.prototype.constructor = Square;

  // 객체 생성
  var square = new Square(5);
  alert(square instanceof Rectangle);  // true 출력
  ```
  * 위 코드에서 Square객체는 Regtangle 객체를 상속받았으므로,   
    Regtangle 객체가 하는 모든 일을 수행할 수 있다.

<hr/>

* 클래스 - ECMAScript 6
  * 클래스의 선언과 생성자 함수 선언 및 메소드 선언
  ```js
  // 클래스의 선언 및 객체 생성 예시
  class Rectangle{
      constructor(width, height){
          this.width = width;
          this.height = height;
      }
  }

  const rectangle = new Rectangle(100, 200);

  // 메소드의 선언 - prototype내가 아닌, class블록 내부에 선언한다.
  class Rectangle{
      constructor(width, height){
          this.width = width;
          this.height = height;
      }

      getArea(){
          return this.width * this.height;
      }
  }

  const rectangle = new Rectangle(100, 200);
  alert(rectangle.getArea());
  ```

  * getter, setter의 사용법
  ```js
  class Rectangle {
      constructor(width, height){
          this._width = width;
          this._height = height;
      }

      // width의 getter, setter
      get width(){
          return this._width;
      }

      set width(input){
          this._width = input;
      }

      // height의 getter, setter
      get height(){
          return this._height;
      }

      set height(input){
          this._height = input;
      }

      getArea(){
          return this._width * this._height;
      }
  }

  const rect = new Rectangle(100, 200);
  rect.width = 200;

  alert(rect.width);  // 200 출력
  alert(rect.getArea());  // 40000 출력
  ```
  * ECMAScript 6의 클래스는 __변수를 숨길 수 없다.__ 
  * 관례적으로는 외부의 접근을 불허하는 변수에 대해서는 앞에 _ 를 붙인다.
  * getter, setter는 메소드 선언 시 앞에 get, set를 붙여 선언한다.
  * 하지만 getter, setter의 사용은 method chaining등이 어렵다는 등의 이유로 사용이 비권장된다.

<hr/>

* 상속 : extends 키워드 이용
  ```js
  // Rectangle 클래스는 위 코드와 동일하다.

  // Rectangle 클래스를 상속받는 Square 클래스
  class Square extends Rectangle{
      constructor(length){
          // 부모인 Rectangle의 생성자 호출
          super(length, length);
      }

      // 정사각형이므로 width 또는 height를 변경하면
      // 둘 다 변경되도록 다시 선언한다.
      set width(input){
          this._width = input;
          this._height = input;
      }

      set height(input){
          this._width = input;
          this._height = input;
      }
  }

  // 객체 생성하기
  const square = new Square(100);
  alert(square.getArea());  // 10000 출력
  ```
