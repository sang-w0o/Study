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

* 아래와 같이 `/movies`를 위한 API Path를 하나 추가해보자.
```ts
import { Controller, Get } from '@nestjs/common';

@Controller('movies')
export class MoviesController {
    @Get('/movies'): string {
        return 'This will return all movies';
    }
}
```

* 이제 `/movies`로 요청을 보내면 우리가 예상한대로 응답이 오지 않고,   
  Path를 찾을 수 없다는 404 응답 코드가 온다.   
  이 이유는 `@Controller('값')`가 적용된 컨트롤러 안에 있는 `@Get`, `@Post` 등의   
  Route들은 `값`에 포함되기 때문이다.

* 즉, 위의 예시를 봤을 때에 `/movies/movies`로 요청을 보내야 우리가 원하는   
  응답을 얻을 수 있다.
<hr/>