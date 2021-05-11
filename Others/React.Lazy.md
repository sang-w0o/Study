# React.Lazy로 로딩 시간 단축시키기

- 웹 페이지의 성능 중 많은 부분을 차지하는 로딩 속도는 성능 자체의 의미로도 충분히 중요하지만,  
  웹 페이지의 로딩 속도가 늦어질 수록 그에 기하급수적으로 비례해서 고객의 이탈률이 높아지기도 한다.

- 따라서 이번에는 `React.Lazy()`를 사용해서 로딩 속도를 단축시키는 것을 알아보려 한다.

- 웹 페이지는 단순히 HTML로만 이루어진 것이 아니며, JS와 같은 스크립트 파일, CSS와 같은 스타일 시트 등을 불러와서  
  기능 및 속성을 덭붙여 작동되도록 되어 있다. 이때, 만약 JavaScript 파일의 용량이 매우 크다면, 이 파일을 모두  
  로딩시킨 후 페이지가 렌더링되기 때문에 당연히 그에 비례하여 로딩 속도도 느려질 것이다.

- 따라서 모든 JS 파일을 사용자가 처음 페이지에 들어선 순간 받아오는 것이 아니라, 번들(Bundle)을 조각들로 쪼개어  
  **필요한 시점에만 불러오도록** 해야 한다.

- 우선 JS Bundle을 쪼개어, 사용자가 페이지를 로딩할 때 첫 번째 Route에만 필요한 코드를 반환하도록 하자.  
  Webpack, Parcel, Rollup 등의 유명한 모듈 번들러(Module Bundler)들은 큰 번들을 `Dynamic Import`를  
  사용하여 잘게 쪼개는 기능을 제공한다.

- 예를 들어, 아래와 같이 form이 submit되면 동작하는 `someFunction()`이 있다고 하자.

```js
import moduleA from "library";

form.addEventListener("submit", (e) => {
  e.preventDefault();
  someFunction();
});

const someFunction = () => {
  // uses moduleA
};
```

- 위 코드에서, `someFunction`은 특정 라이브러리에서 import된 함수이다.  
  만약 이 함수가 다른 곳에서 더 사용되지 않는다면, 아래와 같이 필요한 경우에만 import해서 사용할 수 있게 할 수 있다.

```js
form.addEventListener("submit", (e) => {
  e.preventDefault();
  import("library.moduleA")
    .then((module) => module.default) // using the default export
    .then(someFunction())
    .catch(handleError());
});

const someFunction = () => {
  // uses moduleA
};
```

- 위 코드를 보면, 이제 최상위에서 특정 라이브러리부터 moduleA를 import하지 않는다.  
  이렇게 필요한 경우에만 로딩하는 것을 `Lazy Loading`이라 한다.
