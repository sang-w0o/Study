# AWS Lambda 구축하기

<h2>Basics</h2>

- AWS의 Lambda는 대표적인 Serverless 서비스이다.  
  _Serverless_ 하다는 것이 어색할 수 있지만, 말 그대로 받아들이면 된다.

- Serverless의 특성에 맞게 Lambda는 아래와 같이 **Trigger**를 발생시켰을 때 수행된다.  
  그 **Trigger**로는 스케쥴러(특정 시간에 실행)가 될 수도 있고, AWS의 API Gateway와  
  연결하여 하나의 API를 호출하는 것이 trigger가 될 수도 있다.

- 이 예시에서는 스케쥴러가 아닌, API Gateway를 사용하여 Lambda를 초 간단하게 사용해보자.

- 참고로 Serverless 답게 Lambda의 비용 청구 방식도 매우 특이한데,  
  Lambda는 기본적으로 아래의 컨셉을 가지고 있다.

> 특정 trigger에 의해 함수가 실행된다.

- 위 컨셉에 맞게 비용 청구 방식은 _함수가 수행된 시간_ 이다.

<hr/>

<h2>Starting Project</h2>

- Lambda는 실행시킬 함수에 대하여 정말 많은 언어를 지원하는데, 이번 예제에서는  
  boiler plate를 제공해주는 serverless 프레임워크를 사용해보자.  
  언어로는 TypeScript를 선택했다.

```
npm i -g serverless
```

- 위 명령으로 전역젹으로 serverless를 설치한 후, serverless가 제공하는  
  명령어를 통해 AWS Lambda를 위한 typescript로 된 boilerplate를 만들어보자.

```
serless create --template aws-nodejs-typescript --path ./Lambda_Example
```

- 위 명령어는 aws-nodejs-typescript의 boilerplate 코드를 Lambda_Example 폴더에  
  생성해준다.

- 생성해준 폴더를 보면 상당히 많은 라이브러리들이 들어가 있는데, 필요에 맞게 삭제해도 문제 없다.  
  아래는 최소한의 설정만을 담은 `package.json` 이다.

```json
{
  "name": "lambda-example",
  "version": "1.0.0",
  "description": "Serverless aws-nodejs-typescript template",
  "license": "MIT",
  "scripts": {
    "test": "echo \"Error: no test specified\" && exit 1"
  },
  "engines": {
    "node": ">=14.15.0"
  },
  "dependencies": {
    "aws-lambda": "^1.0.6"
  },
  "devDependencies": {
    "@types/aws-lambda": "^8.10.78",
    "serverless": "^2.23.0",
    "typescript": "^4.1.3",
    "serverless-plugin-typescript": "^1.1.9"
  }
}
```

- `aws-lambda`: aws lambda의 함수 형식을 작성하기 위한 라이브러리
- `@types/aws-lambda`: aws-lambda의 typescript 사용을 위한 라이브러리
- `serverless`: 로컬 환경에서 serverless 배포를 테스트하기 위한 라이브러리
- `typescript`: typescript 사용을 위한 라이브러리
- `serverless-plugin-typescript`: typescript를 serverless와 함께 사용하기 위한 라이브러리

<hr/>

<h2>Writing example code</h2>

- 이제 Lambda가 trigger되면 실제로 수행될 함수를 작성해보자.  
  `src/` 하위에 `helloWorld`라는 폴더를 만든 후, `handler.ts`를 만든다.

```ts
// handler.ts
import { Handler } from "aws-lambda";

interface Response {
  message: string;
}

const helloLambdaWorld: Handler = async () => {
  const response: Response = {
    message: "Hello, Lambda new!",
  };

  return {
    statusCode: 200,
    body: JSON.stringify(response),
  };
};

export { helloLambdaWorld };
```

- `helloLambdaWorld()` 함수가 바로 Lambda가 trigger되었을 때 실행될 함수이다.  
  이 함수를 보면 `Handler` 타입을 가지는데, aws lambda가 이 함수를 직접적으로 호출하여  
  수행할 것임을 알려준다.

- 반환하는 형식을 보면 statusCode, body가 포함된 객체를 반환하는데 이는  
  API Gateway가 반환하는 형식을 준수하여 작성한 객체이다.  
  만약 준수하지 않는다면 코드에는 문제가 없어도 아래 에러가 나면서 500이 온다.

```
Malformed Lambda proxy response
```

<hr/>
