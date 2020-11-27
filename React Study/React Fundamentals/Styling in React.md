<h1>Styling in React</h1>

* React의 컴포넌트에 스타일을 정의하는 방법은 여러 가지가 있다.
  
1. css 파일 생성 후 import 해서 사용하기
  * 이 방법의 문제점은 컴포넌트와 CSS가 분리되어 있다는 점이다.   
    하나는 JS 파일, 하나는 CSS 파일로 분리되게 된다.

  * 아예 특정 컴포넌트를 위한 폴더를 만들고, 그 내에 컴포넌트 파일과   
    CSS 파일을 넣는 방법도 있다. (index.js도 추가 필요)

  * 이 방법의 또다른 문제점은 항상 className을 기억하고, 중복해야 하지 않아야 하며   
    항상 import를 해야 하기 때문이다.

2. 두 번째 방법은 css 파일이 Global(기본)이 아닌 Local이도록 하게 하는 것이다.
  * 이 방법은 `A.css`라는 파일을 `A.module.css`로 네이밍하는 것이다.   
  * 이렇게 한다면 import문도 아래와 같이 바꾸고, className을 JS Object처럼   
    사용할 수 있다.
  ```js
  import React from 'react';
  import styles from "./Header.module.css";

  export default () => (
      <header>
          <ul className={styles.nav}>
              <li>
                  <a href="/">Movies</a>
              </li>
              <li>
                  <a href="/tv">TVs</a>
              </li>
              <li>
                  <a href="/search">Search</a>
              </li>
          </ul>
      </header>
  )
  ```

  * 이렇게 하면, 실제 element는 굉장히 랜덤한 class를 갖게 된다.   
    나의 경우는 아래와 같은 class명을 갖게 되었다.
  ```html
  <ul class="Header_nav__3Xjbe"><li><a href="/">Movies</a></li><li><a href="/tv">TVs</a></li><li><a href="/search">Search</a></li></ul>
  ```

  * 이 방법의 이점은 navList라는 클래스명을 다른 파일에서도 반복해서 사용할 수   
    있게 되었기 때문이다. 즉, navList는 local 클래스명이 된 것이다.   
    하지만 여전히 className을 기억해야 한다는 문제점이 있다.

3. Styled Components
  * Styled-components 라이브러리를 활용하면, style이 내부적으로 정의된   
    컴포넌트를 생성할 수 있다.
  * 예시는 아래와 같다.
  ```js
  import React from 'react';
  import styled from 'styled-components';

  const List = styled.ul`
      display: flex;
      &:hover {
          background-color: blue;
      }
  `;

  export default () => (
      <header>
          <List>
              <ul>
                  <li>
                      <a href="/">Movies</a>
                  </li>
                  <li>
                      <a href="/tv">TVs</a>
                  </li>
                  <li>
                      <a href="/search">Search</a>
                  </li>
              </ul>
          </List>
        </header>
  )
  ```

  * 즉, `styled.요소`를 const 변수에 할당한 후, 그 변수로 스타일을 적용할   
    컴포넌트를 감싸주면 된다.

<h2>Styled Components</h2>

