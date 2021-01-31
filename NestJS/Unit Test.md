<h1>Unit testing in NestJS</h1>

* Nest CLI로 프로젝트를 생성하면 `package.json`에 기본적으로 테스트를 위한   
  스크립트가 포함되어 있다.
```json
// package.json

{
  // 기타 정보
  "scripts": {
    // 기타 Scripts
    "test": "jest",
    "test:watch": "jest --watch",
    "test:cov": "jest --coverage",
    "test:debug": "node --inspect-brk -r tsconfig-paths/register -r ts-node/register node_modules/.bin/jest --runInBand",
    "test:e2e": "jest --config ./test/jest-e2e.json"
  },
  "dependencies": {
    // 의존 패키지들
  },
  "devDependencies": {
    // 기타 의존 패키지들
    "jest": "^26.6.3",
    "supertest": "^6.0.0",
    "ts-jest": "^26.4.3",
  },
  "jest": {
    "moduleFileExtensions": [
      "js",
      "json",
      "ts"
    ],
    "rootDir": "src",
    "testRegex": ".*\\.spec\\.ts$",
    "transform": {
      "^.+\\.(t|j)s$": "ts-jest"
    },
    "collectCoverageFrom": [
      "**/*.(t|j)s"
    ],
    "coverageDirectory": "../coverage",
    "testEnvironment": "node"
  }
}
```

* 테스트 관련 스크립트에는 `test`, `test:watch`, `test:cov`, `test:debug`, `test:e2e`가 있다.

* 우선 `jest`는 Javascript로 작성된 애플리케이션에 대한 테스팅을 지원하는 npm 패키지이다.

* Nest CLI로 생성한 Controller와 Service는 아래와 같은 spec 파일들이 있다.
  * `user.controller.spec.ts`
  * `user.service.spec.ts`

* `*.spec.ts`는 테스팅을 위한 스펙 문서이다.   
  예를 들어 `user.service.spec.ts`는 `UserService`를 테스트하기 위한 스펙 문서이다.

* 기본적으로 NestJS에서는 jest가 테스트를 위해 `*.spec.ts` 파일들을 찾아보도록 설정되어 있다.   
  `UserService`를 테스트하기 위한 spec 파일을 작성해보자.
```ts
import { Test, TestingModule } from '@nestjs/testing';
import { UserService } from './user.service';

describe('UserService', () => {
  let service: UserService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [UserService],
    }).compile();

    service = module.get<UserService>(UserService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
```

* `beforeEach()` 함수는 각 테스트 케이스가 실행되기 전에 수행되는 함수이다.   
  `JUnit`의 `@Before`, `@BeforeEach`와 동일하다고 생각하면 된다.

* `describe()`는 하나의 큰 테스트 케이스이다.   
  첫 번째 인자로 Test명을 전달하고, 두 번째 인자로 익명함수를 넣는데,   
  이 익명함수 내에는 여러 개의 테스트 함수들이 들어갈 수 있다.

* `it()` 함수는 단언을 시작하는 함수로, 첫 번째 인자는 테스트 메소드명을 string으로   
  지정하며, 두 번째 인자로는 단언을 수행할 함수를 지정한다.

* 아래는 간단한 예시이다.
```ts
it('should be 5', () => {
    expect(5).toEqual(5);
  });
```

* 테스트를 수행하면 아래와 같은 결과가 나온다.
```
 PASS  src/user/user.service.spec.ts (5.784 s)
  UserService
    √ should be 5 (5 ms)
```

* 아래와 같이 잘못된 단언을 가진 테스트 코드를 보자.
```ts
it('should be 5', () => {
    expect(5).toEqual(4);
  });
```

* 결과는 아래와 같다.
```
 FAIL  src/user/user.service.spec.ts (6.22 s)
  UserService
    × should be 5 (7 ms)

  ● UserService › should be 5

    expect(received).toEqual(expected) // deep equality

    Expected: 4
    Received: 5

      18 | 
      19 |   it('should be 5', () => {
    > 20 |     expect(5).toEqual(4);
         |               ^
      21 |   });
      22 | });
      23 | 

      at Object.<anonymous> (user/user.service.spec.ts:20:15)
```

* 로그를 보면 예상한 값(Expected)은 4인데, 실제 값(Received)는 5여서 테스트가 실패한다.   
  즉 `expect()` 내에는 실제 값(테스트가 통과해야 하는 값)을 지정하고,   
  `toEqual()` 내에는 `expect()`가 반환하는 값과 일치해야하는 값을 지정해야함을 알 수 있다.
<hr/>