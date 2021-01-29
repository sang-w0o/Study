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