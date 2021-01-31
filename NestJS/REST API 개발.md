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

<h2>Path Variable값 읽기</h2>

* 위위의 `MoviesController`에 아래와 같은 Route가 있다고 해보자.
```ts
import { Controller, Get } from '@nestjs/common';

@Controller('movies')
export class MoviesController {
    @Get('/:id')
    getMovieById(): string {
        // TODO : Return movie by id
    }
}
```

* 위의 `getMovieById`에서 URL Path로 들어온 `id` Path Variable값을   
  읽는 방법은 다음과 같다. 

```ts
import { Controller, Get } from '@nestjs/common';

@Controller('movies')
export class MoviesController {
    @Get('/:id')
    getMovieById(@Param('id') id: number ): string {
        return `This is info about a movie.(ID : ${id})`;
    }
}
```

* ~~Spring하고 너무너무너무 비슷하다...~~

* 위 코드는 문제점이 있는데, `@Param`으로 가져온 값은 기본적으로 string이 된다.   
  따라서 위와 같이 명시적으로 `id: number`를 지정해줘도, `typeof id`를 아래에서   
  살펴보면 string이 나오게 된다. 그렇다면 `/:id`의 id를 number로 string이 아닌   
  number로 가져오려면 어떻게 해야할까?

* 해답은 `ParseIntPipe`를 사용하는 것이다.
```ts
import { Controller, Get, Param, ParseIntPipe } from '@nestjs/common';

@Controller('movies')
export class MoviesController {

  @Get('/:id')
  getMovieById(@Param('id', ParseIntPipe) id: number): string {
    return `This is info about a movie. (ID : ${id}), ${typeof id}`;
  }
}
```

* 위와 같이 `@Param`의 두 번째 인자로 `ParseIntPipe`를 지정하면 id 파라미터를   
  number로 가져오며, `typeof id`도 number로 뜬다.   
  또한 `id: string`으로 가져와도 id는 number이 되며, id값에 숫자가 아닌   
  다른 값이 들어가면 `400(BAD_REQUEST)`가 반환된다.

* `POST`, `DELETE`, `PUT` 등의 HTTP Method도 아래와 같이   
  각각에 맞는 데코레이터를 적용해 주면 된다.
```ts
@Controller('movies')
export class MoviesController {
  @Get('/movies')
  getAll(): string {
    return 'This will return all movieS';
  }

  @Get('/:id')
  getMovieById(@Param('id', ParseIntPipe) id: string): string {
    return `This is info about a movie. (ID : ${id}), ${typeof id}`;
  }

  @Post()
  createMovie(): string {
    return 'This will create a movie.';
  }

  @Delete('/:id')
  deleteMovie(@Param('id', ParseIntPipe) id: number): string {
    return `This will remove a movie.(ID : ${id}`;
  }

  @Put('/:id')
  updateMovie(@Param('id', ParseIntPipe) id: number): string {
    return `This will update a movie.(ID : ${id})`;
  }

  @Patch('/:id')
  updateMovieInfo(@Param('id', ParseIntPipe) id: number): string {
    return `This will update some info about movie.(ID : ${id})`;
  }
}
```

* Pipe에 대한 더 많은 정보는 <a href="https://github.com/sangwoo-98/Study/blob/master/NestJS/Pipes%20%EB%B0%8F%20Request%20Body%EC%9D%98%20%20%EA%B2%80%EC%A6%9D.md">링크</a>에 정리해 놓았다.
<hr/>

<h2>DTO와 DTO의 검증</h2>

* DTO에는 다양한 제약을 걸 수 있는데, 우선 `readonly` 부터 알아보자.   
  `readonly`는 읽을 수만 있다는 속성을 주는 예약어로, 아래와 같이 사용한다.
```ts
import { IsEmail, IsString } from 'class-validator';

export class UserCreateDto {

  readonly name: string;

  readonly email: string;

  readonly password: string;

  readonly phoneNumber: string;
}
```