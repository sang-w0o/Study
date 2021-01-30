<h1>REST API 개발하기</h1>

* 프로젝트 생성시 NestJS의 CLI를 설치했기에 다양한 명령들을 사용할 수 있다.

![](2021-01-30-13-56-16.png)

* 예를 들어 새로운 `Controller`를 생성한다고 해보자.
```sh
# 단축어 없이
nest generate controller

# 단축어 사용
nest g co
```

* 위 명령어를 입력하면 컨트롤러의 이름을 물어보는데, movies라고 했다고 해보자.   
  그러면 `src/movies` 폴더가 생성되며. `app.module.ts`에는 자동으로 `@Module`   
  Decorator에 `MoviesController`가 import되어있는 것을 볼 수 있다.

```ts
// movies.controller.ts
import { Controller } from '@nestjs/common';

@Controller('movies')
export class MoviesController {}
```
<hr/>

<h2>Route 설정하기</h2>

