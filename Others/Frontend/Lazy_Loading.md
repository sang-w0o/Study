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

# React.Lazy

- `React.Lazy` 메소드는 React Application을 컴포넌트 레벨에서 dynamic import로 코드를 잘게 쪼갤 수 있게 해준다.  
  (Code-Splitting)

```js
import React, { lazy } from "react";

const AvatarComponent = lazy(() => import("./AvatarComponent"));

const DetailsComponent = () => (
  <div>
    <AvatarComponent />
  </div>
);
```

- 규모가 큰 React Application은 정말 많은 컴포넌트들과 메소드, 그리고 제3의 라이브러리들을 포함한다.  
  위에서 말했듯이, 만약 특정 작업을 수행해주지 않으면 페이지가 로딩될 때 부터 위의 수많은 데이터들이  
  한 번에 모두 로딩되게 된다.

- `React.Lazy` 함수는 React가 제공하는 메소드로, 애플리케이션 내의 컴포넌트들을 각자 다른 JavaScript Chunk로  
  쉽게 분리할 수 있게 해준다. 이렇게 분리를 하면, 로딩 중인 상태를 `Suspense` 컴포넌트로 처리할 수 있다.

<hr/>

# Suspense

<h2>Suspense와 React.Lazy의 작동</h2>

- `React.Lazy`를 사용하면 말 그대로 필요 시에만 로딩하는 `Lazy Loading`을 사용하기에, 컴포넌트가 로딩될 때 까지  
  지연 시간이 발생할 수 있다. 이럴 경우에, 사용자에게 로딩 중임을 표시하는게 매우 중요하다.  
  이를 `React.Lazy`와 `Suspense`로 해결하면 매우 간단하게 해결할 수 있다.

```js
import React, { lazy, Suspense } from "react";

const AvatarComponent = lazy(() => import("./AvatarComponent"));

const renderLoader = () => <p>Loading</p>;

const DetailsComponent = () => (
  <Suspense fallback={renderLoader()}>
    <AvatarComponent />
  </Suspense>
);
```

- `Suspense` 컴포넌트는 만약 컴포넌트가 로딩 중이라면 보여줄 컴포넌트를 fallback 속성으로 받는다.

<hr/>

<h2>Suspense로 여러 개의 컴포넌트 로딩 처리</h2>

- `Suspsense`의 또다른 기능은 여러 개의 컴포넌트가 모두 로딩될 때 까지에 대한 처리도 가능하다는 것이다.

```js
import React, { lazy, Suspense } from "react";

const AvatarComponent = lazy(() => import("./AvatarComponent"));
const InfoComponent = lazy(() => import("./InfoComponent"));
const MoreInfoComponent = lazy(() => import("./MoreInfoComponent"));

const renderLoader = () => <p>Loading</p>;

const DetailsComponent = () => (
  <Suspense fallback={renderLoader()}>
    <AvatarComponent />
    <InfoComponent />
    <MoreInfoComponent />
  </Suspense>
);
```

- 위 코드에서, `Suspense` 컴포넌트는 `AvatarComponent`와 `InfoComponent`, `MoreInfoComponent`가  
  모두 로딩될 때 까지 `<p>Loading</p>`를 띄워준다.

- 이는 하나의 로딩 상태에서 여러 개의 컴포넌트를 띄워주고 싶은 경우에 매우 유용하게 사용된다.  
  `Suspense` 내의 컴포넌트들이 모두 로딩이 완료되면, 사용자는 로딩된 컴포넌트들을 한번에 보게 된다.

<hr/>

<h2>로딩 실패 처리해주기</h2>

- `Suspense`는 컴포넌트가 로딩 될 때 네트워크 통신이 필요할 때, 이에 대한 로딩 처리를 해줄 수 있다.  
  하지만 만약 모종의 이유로 네트워크 요청이 실패하면 어떻게 될까?  
  사용자가 오프라인이거나, 웹 서버가 다운되었거나 등의 다양한 경우가 있을 것이다.

- React는 이렇게 로딩할 때 생기는 예외 상황에 대한 처리 방법을 제공하는데, 바로 에러 범주(Error Boundary)를  
  활용하도록 한다. 공식 문서에 따르면, 모든 React 컴포넌트는 Error Boundary로서의 역할을 할 수 있다.

- Lazy Loading에 대한 예외 상황을 발견하고 처리하기 위해서는 `Suspense` 컴포넌트를 Error Boundary로서  
  작동하는 컴포넌트로 감싸주면 된다. Error Boundary 컴포넌트의 `render()` 메소드에서 만약 에러가 없다면  
  children을 반환해주면 되고, 에러가 있다면 개발자가 직접 작성한 화면을 렌더링 해주도록 할 수 있다.

```js
import React, { lazy, Suspense } from "react";

const AvatarComponent = lazy(() => import("./AvatarComponent"));
const InfoComponent = lazy(() => import("./InfoComponent"));
const MoreInfoComponent = lazy(() => import("./MoreInfoComponent"));

const renderLoader = () => <p>Loading</p>;

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true };
  }

  render() {
    if (this.state.hasError) {
      return <p>Loading failed! Please reload.</p>;
    }

    return this.props.children;
  }
}

const DetailsComponent = () => (
  <ErrorBoundary>
    <Suspense fallback={renderLoader()}>
      <AvatarComponent />
      <InfoComponent />
      <MoreInfoComponent />
    </Suspense>
  </ErrorBoundary>
);
```
