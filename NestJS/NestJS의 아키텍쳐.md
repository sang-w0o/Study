<h1>NestJS의 아키텍쳐</h1>

* NestJS 프로젝트를 생성하면 `src/main.ts`가 있다.
```ts
import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  await app.listen(3000);
}
bootstrap();
```

* NestJS는 `main.ts` 파일을 가진다. 파일명이 꼭 main 이어야만 한다.

* 실행 후 `localhost:3000`을 가면 Hello World!가 보이는데, 이는 어디서 오는걸까?

* `AppModule`로 가보면 아래의 코드가 있다.
```ts
import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';

@Module({
  imports: [],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
```

* 위의 `@Module({ .. })`는 `Decorator`라 하는데, Decorator는 클래스에   
  함수 기능을 추가하도록 해준다.

* export문에는 빈 객체가 들어있지만, 실제 모든 정보는 `@Module` 내에 들어 있다.

* `@Module`내의 controllers로 지정된 `AppController`를 보자.
```ts
import { Controller, Get } from '@nestjs/common';
import { AppService } from './app.service';

@Controller()
export class AppController {
  constructor(private readonly appService: AppService) {}

  @Get()
  getHello(): string {
    return this.appService.getHello();
  }
}
```

* 위 코드는 `@Get`이라는 Decorator와 `appService.getHello()`를 반환하는   
  `getHello()` 함수가 있다.

* 그럼 다음으로 `AppService` 코드를 보자.
```ts
import { Injectable } from '@nestjs/common';

@Injectable()
export class AppService {
  getHello(): string {
    return 'Hello World!';
  }
}
```
<hr/>

<h2>Controllers</h2>

* NestJS 애플리케이션은 `main.ts`로부터 시작한다.   
  위에서 봤듯이 한 개의 모듈(AppModule)로부터 애플리케이션(app)을 실행한 후   
  3000번 포트를 열어 실행시킨다.

* AppModule은 모든 것들의 Root Module이다.

* `main.ts`의 `@Module` 데코레이터를 다시 살펴보자.
```ts
@Module({
  imports: [],
  controllers: [AppController],
  providers: [AppService],
})
```

* Controller가 하는 일은 기본적으로 Request Path를 읽어온 후   
  알맞은 서비스 함수를 실행시키는 일이다. Spring MVC의 Controller와 같은 역할을 담당한다.   
  Node.js 애플리케이션의 Router와도 동일하다.

* 다시 `app.controller.ts`를 보면, `@Get()` 데코레이터가 있다.
```ts
@Controller()
export class AppController {
  constructor(private readonly appService: AppService) {}

  @Get()
  getHello(): string {
    return this.appService.getHello();
  }
}
```

* `@Get()` 데코레이터는 Express의 get 라우터와 같은 역할을 한다.   
  아래와 같이 라우터에 대한 메소드를 추가할 수 있다.
```ts
@Controller()
export class AppController {
  constructor(private readonly appService: AppService) {}

  @Get()
  getHello(): string {
    return this.appService.getHello();
  }

  @Get('/test')
  test(): string {
    return 'TEST';
  }
}
```

* 이제 NestJS는 `/test`로 요청이 오면 TEST를 반환하게 된다.

* Decorator는 꾸며주는 함수 또는 클래스와 붙어있어야 한다.   
  쉽게 말해 Java의 Annotation과 같이 사용해야 한다. 아래처럼 하면 안된다.
```ts
@Controller()
export class AppController {
  constructor(private readonly appService: AppService) {}

  @Get('/test')


  test(): string {
    return 'TEST';
  }
}
```
<hr/>