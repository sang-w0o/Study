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