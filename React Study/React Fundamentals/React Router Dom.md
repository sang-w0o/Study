<h1>react-router-dom에 대한 기본 정리</h1>

* 아래와 같이 `Router.js`라는 컴포넌트가 있다고 하자.
```js
import React from 'react';
import {BrowserRouter as Router, Route, Redirect} from 'react-router-dom';
import Home from '../Routes/Home';
import Search from '../Routes/Search';
import TV from '../Routes/TV';

export default () => (
    <Router>
        <>
        <Route path="/" exact component={Home} />
        <Route path="/tv" component={TV} />
        <Route path="/tv/popular" render={() => <h1>Popular TV!</h1>}/>
        <Route path="/search" exact component={Search} />
        <Redirect from="*" to="/" />
        </>
    </Router>
)
```

* 위에서 `<>`와 `</>`가 존재하는 이유는 `render()`는 기본적으로 단 하나의 컴포넌트만   
  반환할 수 있기 때문이다. `<>`와 `</>` 사이에 여러 컴포넌트를 넣어주면, 여러 개의 컴포넌트를   
  렌더링할 수 있다.

* `<Route>` 컴포넌트의 path 속성은 URL을 의미하며, 위의 경우 "/"로 가면, `Home` 컴포넌트를 렌더링한다.   
  exact 속성은 정확히 일치해야함을 의미하며, component는 렌더링할 컴포넌트를 지정한다.   
  또한 render 속성에 콜백 함수를 두어, 여러 개의 컴포넌트를 렌더링하게 할 수도 있다.

* `<Redirect>` 컴포넌트는 from과 to의 속성을 가지는데, 위의 경우 만약 "/", "/tv", "tv/popular", "/search"가 아닌   
  다른 URI로 가려 한다면, "/"로 redirect 시키라는 의미를 가진다.

* exact 속성은 중요하다. "/tv"와 "/tv/popular"의 경우, "/tv"에 대한 `Route` 컴포넌트가 exact 속성이 없다면,   
  "/tv/popular"에 "/tv"가 렌더링 하는 `TV` 컴포넌트도 함께 렌더링 하게 된다.

* 위 코드에서의 문제점은 모든 URI (`Redirect`의 from 속성이 "*" 이다.)를 "/"로 redirect 시킨다는 점이다.   
  이를 해결하기 위한, 즉 특정 경우에만 redirect 시키려고 한다면 `Switch`를 사용한다.

