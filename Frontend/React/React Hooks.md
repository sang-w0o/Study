<h1>React Hooks</h1>

* 기존에 책들이나 강의를 보면 컴포넌트 작성 시 `Class Component`를 사용하는 경우가 많은데,   
  기존 코드를 `Functional Component`로 변환하면서 왜 `Class Component`가 Deprecated 수준으로   
  안쓰이는지, `Functional Component`가 왜 좋은지를 몸소 알게 되었다.

* `Class Component`와 달리 `Functional Component`는 생명 주기 함수를 제공하지 않는다.   
  예를 들어 `componentShouldUpdate()`, `componentDidMount()`, `componentWillUnmount()` 등의   
  함수들이 제공되지 않는 것이다. 또한 state를 `Class Component`처럼 사용할 수 없다.

* 위의 문제들을 간단히 해결해주는 것이 React Hook인데, 하나씩 정리해보겠다.   
  ~~진짜 동일한 작업을 하더라도 코드가 말도 안되게 줄어든다.~~

<h2>useState()</h2>

* 다음에하자
