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

* 회원 정보를 업데이트 하는 `UserUpdateDto` 클래스를 만들어보자.   
  항상 모든 정보를 업데이트 해야하진 않기에 각 필드는 필수 속성이 아닐 것이다.   
  즉, 들어온 필드에 대해서만 업데이트를 실시한다는 것이다.
```ts
import { IsEmail, IsString } from 'class-validator';

export class UserUpdateDto {
  @IsString()
  readonly name?: string;

  @IsEmail()
  readonly email?: string;

  @IsString()
  readonly password?: string;

  @IsString()
  readonly phoneNumber?: string;
}
```

* DTO의 필드마다 `?:` 를 주어 해당 타입이 undefined 또는 null일 수 있다고 명시할 수도 있지만,   
  이러한 기능을 NestJS에서 제공해준다.   
  해당 기능을 사용하려면 `@nestjs/mapped-types` 패키지를 설치한다.
```
yarn add @nestjs/mapped-types
```

* `@nestjs/mapped-types`는 타입을 개발자가 원하는대로 변환하고 사용하는 작업을 도와주는 패키지이다.

* 사용법은 아래와 같다.
```ts
import { IsEmail, IsString } from 'class-validator';

export class UserUpdateDto extends PartialType {
  @IsString()
  readonly name?: string;

  @IsEmail()
  readonly email?: string;

  @IsString()
  readonly password?: string;

  @IsString()
  readonly phoneNumber?: string;
}
```

* `PartialType`은 Base Type이 필요한데, 이 경우 `UserUpdateDto`는 `UserCreateDto`의   
  필드 중 일부만을 가져도 되므로 Base Type으로 `UserCreateDto`를 지정해준다.
```ts
import { PartialType } from '@nestjs/mapped-types';
import { UserCreateDto } from './create-user.dto';

export class UserUpdateDto extends PartialType(UserCreateDto) {}
```
<hr/>

<h2>Module과 DI</h2>

* 프로젝트 최상단에 있는 `app.module.ts`를 보자.
```ts
import { Module } from '@nestjs/common';
import { UserController } from './user/user.controller';
import { UserService } from './user/user.service';

@Module({
  imports: [],
  controllers: [UserController],
  providers: [UserService],
})
export class AppModule {}
```

* 기존에 `UserController`와 `UserService`를 만든 방법은 NestJS CLI를 사용해서 만들었다.   
  이 상태에서 주문(Order) API와 권한(Auth) API를 만든다면 `app.module.ts`는 아래와 같아질 것이다. 
```ts
@Module({
  imports: [],
  controllers: [UserController, OrderController, AuthController],
  providers: [UserService, OrderService, AuthService],
})
export class AppModule {}
```

* 이렇게 Controller, Service가 하나씩 늘어날 때마다 `app.module.ts`에 명시해주는 방법은   
  바람직하지 않다. Controller, Service가 몇개가 될지 모르기 때문이다.

* 이런 문제를 해소하기 위해 `NestJS`는 `app.module.ts`에 `AppController`와   
  `AppService`만을 가지는 컨벤션을 강조한다.

* NestJS의 애플리케이션은 여러 개의 모듈로 구성된다.

* 모듈 또한 Nest CLI로 생성 가능하다.
```
nest g mo
```

* 이름을 user로 지정하면, `app.module.ts`가 아래와 같이 바뀐다.
```ts
import { Module } from '@nestjs/common';
import { UserController } from './user/user.controller';
import { UserService } from './user/user.service';
import { UserModule } from './user/user.module';

@Module({
  imports: [UserModule],
  controllers: [UserController],
  providers: [UserService],
})
export class AppModule {}
```

* 차이점이라면 `@Module` Decorator의 imports에 `UserModule`이 추가되었다는 것이다.   
  `UserModule`은 아래와 같다.
```ts
// src/user/user.module.ts

import { Module } from '@nestjs/common';

@Module({})
export class UserModule {}
```

* 이제 `app.module.ts`에서 `UserController`와 `UserService`를 제거하자.
```ts
import { Module } from '@nestjs/common';
import { UserModule } from './user/user.module';

@Module({
  imports: [UserModule],
  controllers: [],
  providers: [],
})
export class AppModule {}
```

* `AppModule`이 `UserModule`을 import하고 있음을 파악하자.

* 이 상태에서 `/user`로 요청을 보내면 아래와 같은 응답이 온다.
```json
{
    "statusCode": 404,
    "message": "Cannot PUT /user",
    "error": "Not Found"
}
```

* 이는 `UserController`와 `UserService`가 어떠한 모듈에도 포함되어 있지 않기 때문이다.   
  따라서 `UserModule`를 아래와 같이 수정해주자.
```ts
import { Module } from '@nestjs/common';
import { UserController } from './user.controller';
import { UserService } from './user.service';

@Module({
  controllers: [UserController],
  providers: [UserService],
})
export class UserModule {}
```

* 이제 `/user`로의 요청은 정상적으로 수행된다.

<h3>DI(Dependency Injection)</h3>

* `UserController`의 코드를 보자.
```ts
// src/users/user.controller.ts

import { Body, Controller, Post, Put } from '@nestjs/common';
import { UserCreateDto } from 'src/dtos/create-user.dto';
import { UserUpdateDto } from 'src/dtos/update-user.dto';
import { UserInfoValidationPipe } from 'src/pipes/create-user.validation.pipe';
import { UserService } from './user.service';

@Controller('user')
export class UserController {
  constructor(private readonly userService: UserService) {}
  @Post()
  saveUser(
    @Body(new UserInfoValidationPipe()) dto: UserCreateDto,
  ): UserCreateDto {
    return this.userService.saveUser(dto);
  }

  @Put()
  updateUser(@Body() dto: UserUpdateDto): UserUpdateDto {
    return this.userService.updateUser(dto);
  }
}
```

* 위 코드에서 `saveUser()`내에서 `this.userService.saveUser(dto)`가 작동하는 이유는   
  생성자 부분에 userService 프로퍼티가 `UserService` 타입임을 명시해주었기 때문이다.

* 즉, `UserController`는 `UserService`로의 의존성(Dependency)를 가지는 것이다.   
  그렇다면 의존성 주입은 누가 하는 것일까?   
  단순히 `UserController`의 생성자에 `UserService` 타입을 지정해준다고 의존성이 주입되지는   
  않는다. 실제로 의존성을 주입하는 곳은 `UserModule`이다.
```ts
import { Module } from '@nestjs/common';
import { UserController } from './user.controller';
import { UserService } from './user.service';

@Module({
  controllers: [UserController],
  providers: [UserService],
})
export class UserModule {}
```

* 만약 위 코드에서 `@Module` Decorator의 providers에서 `UserService`를 제거한다면   
  아래와 같은 오류 메시지가 출력된다.
```
Nest can't resolve dependencies of the UserController (?). 
Please make sure that the argument UserService at index [0] is available in the UserModule context.

Potential solutions:
- If UserService is a provider, is it part of the current UserModule?
- If UserService is exported from a separate @Module, is that module imported within UserModule?
  @Module({
    imports: [ /* the Module containing UserService */ ]
  })
 +1ms
Error: Nest can't resolve dependencies of the UserController (?). Please make sure that the argument UserService at index [0] is available in the UserModule context.
```

* 즉, 의존성 주입은 `UserModule`에서 이루어지고 있다는 것이다.

* 실제로 `UserService` 코드를 보자.
```ts
import { Injectable } from '@nestjs/common';
import { UserCreateDto } from 'src/dtos/create-user.dto';
import { UserUpdateDto } from 'src/dtos/update-user.dto';

@Injectable()
export class UserService {
  saveUser(dto: UserCreateDto): UserCreateDto {
    return dto;
  }
  updateUser(dto: UserUpdateDto): UserUpdateDto {
    return dto;
  }
}
```

* 여기서 `@Injectable()` Decorator에 눈여겨 보자. 이 Decorator는 아래의 기능을 수행한다.
```
Decorator that marks a class as a provider. 
Providers can be injected into other classes via constructor parameter injection using Nest's built-in Dependency Injection (DI) system.
```

* 여기서 궁금했던 점은, `@Injectable()` decorator를 명시하지 않아도 `UserService`의   
  `UserController`로의 의존성 주입이 원활하게 동작한다는 점이었다.   
  그렇다면 `@Injectable()`을 굳이 왜 명시해줘야 하는 걸까?

  * `@Injectable()` Decorator의 기능은 NestJS에게 이 Decorator가 적용된 클래스가   
    다른 클래스로부터 의존성 주입을 받을 경우가 있다는 것을 알리는 것이다.   
    즉, 이 Decorator는 외부로부터 주입받는 의존성이 없을 때에는 생략이 가능하다.   
    위의 `UserService`는 다른 클래스로부터 의존성 주입을 받지 않기에 생략이 가능하다.
<hr/>

<h2>그 외의 간략한 기능</h2o>

* NestJS는 Express.js 상에서 동작하는 프레임워크이기 때문에 Express로 API를 개발하던 것처럼   
  컨트롤러에서 `Request`, `Response` 객체가 필요하다면 사용할 수 있다.   
  사용법은 아래와 같다.
```ts
@Get()
getAll(@Req() requestObject, @Res responseObject): any {
    // Call service.
}
```

* 하지만 NestJS에서 `Request`, `Response`에 대한 직접적인 접근은 좋은 방법이 아니다.   
  이는 NestJS가 Express 와 Fastify 모두를 지원하기 때문이다. Express와 Fastify는   
  각각 `Request`, `Response` 객체에 접근하는 방법이 다르기 때문에 위 방법은 권장되지 않는다.
<hr/>