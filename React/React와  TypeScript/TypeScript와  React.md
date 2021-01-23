<h1>React에서 TS 사용하기</h1>

<h2>프로젝트 생성 방법</h2>

* React 프로젝트의 생성 방법은 JS로 할 때와 마찬가지로 `CRA(Create-React-App)`을 사용한다.   
  CRA는 이미 TS를 사용할 수 있는 패키지들을 포함하는데, 프로젝트 생성 시 `--typescript` 옵션을 주면   
  해당 프로젝트에 TS를 사용할 것임을 명시할 수 있다.
```
npx create-react-app APP_NAME --template typescript
```

* 이렇게 프젝트를 생성하면 기본으로 제공되는 파일들이 모두 TS로 작성되어 있는 것을 확인할 수 있다.
<hr/>

<h2>tsconfig.json</h2>

* `tsconfig.json`에는 TS 사용에 대한 규칙들이 지정되어 있다.   
  아래는 기본적으로 제공되는 `tsconfig.json`의 내용이다.
```json
{
  "compilerOptions": {
    "target": "es5",
    "lib": [
      "dom",
      "dom.iterable",
      "esnext"
    ],
    "allowJs": true,
    "skipLibCheck": true,
    "esModuleInterop": true,
    "allowSyntheticDefaultImports": true,
    "strict": true,
    "forceConsistentCasingInFileNames": true,
    "noFallthroughCasesInSwitch": true,
    "module": "esnext",
    "moduleResolution": "node",
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx"
  },
  "include": [
    "src"
  ]
}
```

* 예를들어, 위의 "allowSyntheticDefaultImports"를 false로 바꾸면, import문을   
  아래와 같이 바꿔주어야 한다.
```ts
// 기존 import문
import React from 'react';

// 바뀐 import문
import * as React from 'react';
```

* 또한, 아래의 함수를 보자.
```ts
const plus = (a, b) => a + b;
```

* 위 함수는 타입을 지정하지 않았기에 명시적으로 `any` 타입을 갖게 되어   
  컴파일 에러를 발생시킨다. 에러는 아래와 같다.
```
TypeScript error: Parameter 'a' implicitly has an 'any' type. TS7006
```

* 위 에러 로그에서 알 수 있듯이, 위의 오류는 `TS7006` 규칙에 따르기 때문에 발생한 것이다.   
  이와 같은 규칙을 재정의하면 `tsconfig.json`을 수정하면 된다.
```json
{
    //....
    "noImplicitAny": false
}
```

* 위의 `noImplicitAny`는 위에서 발생한 `TS7006` 규칙을 무시하게 해준다.
<hr/>

<h2>package.json</h2>

* `package.json`을 살펴보면 dependency에 `@types`로 시작하는 패키지들이 설치된 것을 볼 수 있다.   
  TS는 JS에 기반을 두기에, 이 라이브러리들은 JS로 작성된 React가 TS에서 작동할 수 있게   
  적절한 타입들을 선언해준 라이브러리다.

* 예를 들어 `styled-components` 패키지를 추가하고 import하면 이 패키지는 JS로 작성되었기에   
  TS가 인식하기로는 타입이 정의되어 있지 않은 `any` 타입이 된다.

* 이를 해결하기 위해서는 `@types/styled-components` 패키지를 추가해주면 된다.   
  이렇게 JS로 작성된 라이브러리를 TS 프로젝트에서 사용하기 위해서는 해당 라이브러리를   
  TS 컴파일러가 인식할 수 있도록 해주는 또다른 라이브러리를 추가해줘야 한다.

* 만약 사용하려는 라이브러리에 대해 TS 타입을 제공하는 라이브러리가 없다면   
  `tsconfig.json`에 `"noImplicitAny": true`를 지정해줘야 한다.  
<hr/>