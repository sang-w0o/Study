<h1>MiddleWare 적용하기</h1>

- `Middleware`는 함수의 하나로, 각 Route의 Request Handler들이 요청을 핸들링 하기 전에  
  호출되는 것이다. 이 Middleware는 `Request(요청)` 객체와 `Response(응답)`객체에  
  접근을 할 수 있으며, 이 Middleware내에 있는 `next()` 함수에도 접근할 수 있다.

![picture 1](../images/9482f67f52fb1e2f57a9e20aadf1a0a9cd13c2b84af7d0d78a32227e4a975995.png)

- NestJS에서도 개발자가 직접 작성한 Middleware를 적용할 수 있는데, 해당 Middleware는  
  함수 또는 `@Injectable()` 데코레이터가 적용된 클래스여야 한다.  
  만약 클래스를 사용한다면 `NestMiddleware` 인터페이스의 구현체여야 한다.

<h2>클래스형 Middleware 사용하기</h2>

```ts
@Injectable()
export class LoggerMiddleware implements NestMiddleware {
  use(req: Request, res: Response, next: NextFunction) {
    console.log("This is logged by LoggerMiddleware.");
    next();
  }
}
```

- 이제 이렇게 작성한 Middleware를 특정 컨트롤러 자체에, 또는 특정 Route에 맞게 적용할 수 있다.  
  만약 `UserModule` 전체에 적용하고 싶다면 아래와 같이 하면 된다.

```ts
// app.module.ts
@Module({
  imports: [UserModule],
})
export class AppModule implements NestModule {
  configure(consumer: MiddlewareConsumer) {
    consumer.apply(LoggerMiddleware).forRoutes("user");
  }
}
```

- 특정 Route와 HTTP Method에 대해서만 적용하고 싶다면 아래와 같이 하면 된다.

```ts
// app.module.ts

@Module({
  imports: [UserModule],
})
export class AppModule implements NestModule {
  configure(consumer: MiddlewareConsumer) {
    consumer
      .apply(LoggerMiddleware)
      .forRoutes({ path: "user", method: RequestMethod.POST });
  }
}
```

- 위 코드들에서 `configure()` 내에서 사용하는 `MiddlewareConsumer` 클래스는 헬퍼 클래스로  
  Middleware를 관리하기 위한 다양한 메소드들을 제공한다.  
  예를 들어 `forRoutes()` 메소드는 단일 string을 받거나, 여러 개의 string들,  
  `RouteInfo` 객체, 또는 컨트롤러 클래스, 또는 여러 개의 컨트롤러 클래스들을 인자로 전달할 수 있다.

- 만약 해당 middleware를 적용하고 싶지 않은 Route가 있다면 `MiddlewareConsumer#exclude()`를  
  사용하면 된다.

```ts
consumer
  .apply(LoggerMiddleware)
  .exclude(
    { path: "user", method: RequestMethod.GET },
    { path: "user/:id", method: RequestMethod.GET }
  )
  .forRoutes(UserController);
```

<hr/>
