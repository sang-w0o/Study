# JavaScript의 throw문

- JavaScript에서 try-catch문을 사용하여 예외를 처리할 수 있다.  
  보통 아래의 구문을 사용한다.

```js
try {
  foo(); // 예외가 발생할 가능성이 있는 함수
} catch (e) {
  bar(e); // 예외를 처리해주는 함수
}
```

<h3>'throw' of exception caught locally</h3>

= 아래 코드는 express application에서 route에 대한 요청을 처리해주는 부분이다.  
 이 api는 userId를 request parameter로 받는데, 이 값이 숫자이어야 하기에  
 만약 숫자가 아닌 값일 경우, 적절한 예외 처리를 해주려 한다.

```js
router
  .route(userByIdRoute)
  .delete(async (req: Request, res: Response, next: NextFunction) => {
    try {
      const userId = parseInt(req.params.userId);
      if (isNaN(userId)) {
        throw new BadRequestError("userId는 정수 값이어야 합니다.");
      }
      const dto = await userService.getUserInfo();
      return res.status(StatusCodes.OK_200).json(dto);
    } catch (e) {
      next(e);
    }
  });
```

- 위 코드는 아무런 문제가 없지만, 특정 IDE(WebStorm..)에서 확인하면, 경고가 뜬다.  
  경고는 `throw` 키워드에 뜨는데, 내용은 아래와 같다.

> 'throw' of exception caught locally
> Inspection info: Reports throw statements whose exceptions are always caught by containing try statements.  
> Using throw statements as a "goto" to change the local flow of control is likely to be confusing.

- 즉, 진단 결과 해당 try문 내에서 `BadRequestError`가 던져지면 항상 뒤에 따르는 catch문에서 잡힌다는 것이다.  
  이는 곧 catch문으로 제어가 넘어가는 것이 goto문과 같은 역할을 하여 혼란을 야기할 수 있다는 뜻이다.
  위 코드에서 `throw new BadRequestError()`가 던져지면, 뒤따르는 catch문에서 우리는 `next(e)`를 호출할 것을 바로 알 수 있다.

- 결론적으로, 위 코드의 `if(isNaN(userId))` 부분을 아래와 같이 바꿔주면 경고는 사라진다.

```js
if (isNaN(lecture_id)) {
  next(new BadRequestError("lecture_id는 정수 값이어야 합니다."));
}
```

- 그런데 이 경고 자체에 대해서 개발자들 사이에서 의견 충돌이 있는 것 같다.
  상황에 따라 다르지만, 애초에 예외를 발생시킨 이유가 개발자가 예외로 판단한 상황일 것이기에  
  명시적으로 `try-catch`문으로 처리해주는 것이 좋을 것 같다는 의견도 있다.

- 개인적으로는 `try-catch`를 사용하는 것이 더 명시적으로 표현하기에는 좋다고 생각한다.
