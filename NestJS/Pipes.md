<h1>NestJS의 Pipes</h1>

* Pipe는 `@Injectable()` 어노테이션이 적용된 클래스이며,   
  Pipe는 무조건 `PipeTransform` 인터페이스를 구현해야 한다.

![](2021-01-30-14-40-52.png)

* Pipe는 보통 아래 두 경우에 사용된다.
  * `Transformation` : Input Data를 사용자가 원하는 타입으로 변환하고자 할 때   
    ex) string을 number로 변환
  * `Validation` : Input Data를 검증하여 올바르지 않으면 예외를 발생시킨다.

* 위의 두 경우 모두, Pipe는 `Controller Route Handler`로 전달되는 인자들에 대해   
  특정 작업을 실행한다. Nest는 pipe를 __해당 컨트롤러의 메소드가 실행되기 전에__   
  pipe를 작동시키며, pipe는 특정 작업을 수행한 후 컨트롤러의 메소드로 제어를 넘긴다.

* Nest는 기본적으로 Pipe들을 제공하며, 개발자는 본인이 원하는 Pipe를 만들어   
  사용할 수도 있다.

* 참고로 Pipe는 `Exceptions zone`에서 실행된다. 이는 곧 만약 Pipe가 작업을   
  수행하는 도중 예외를 발생시키면(Exception은 Exceptions Layer가 담당한다.)   
  해당 컨트롤러의 메소드는 실행되지 않는 다는 것을 의미한다.   
  이는 데이터를 검증하는데에 있어 매우 당연한 일이다.
<hr/>

<h2>Built-in Pipes</h2>

* NestJS는 아래 6개의 기본 내장 Pipe들을 제공한다.
  * `ValidationPipe`
  * `ParseIntPipe`
  * `ParseBoolPipe`
  * `ParseArrayPipe`
  * `ParseUUIDPipe`
  * `DefaultValuePipe`
  
* 이들은 모두 `@nestjs/common` 패키지로부터 export된다.

<h3>Binding Pipes</h3>

* Pipe를 사용하기 위해서 개발자는 각 Pipe의 인스턴스를 올바른 컨텍스트에   
  바인딩 해야 한다. 예를 들어, Path Variable로 들어오는 id 값을 string이 아닌   
  number로 받고 싶다면 개발자는 해당 변환 작업(string => number)이 컨트롤러의   
  서비스 메소드가 실행되기 전에 이루어짐을 보장해야 한다. 따라서 Pipe를   
  메소드 파라미터 레벨에 바인딩 해야 한다.
```ts
@Get(':id')
async findOne(@Param('id', ParseIntPipe) id: number) {
    return this.exService.findOne(id);
}
```

* 위 코드는 `findOne()`에서 사용되는 id가 number로 취급될 것과 만약 number가 아니라면   
  Route handler가 실행되기 전에 예외가 발생할 것임을 보장한다.

* 예를 들어 위 Path를 아래와 같이 요청했다고 해보자.
```
GET localhost:3000/ABC
```

* 그렇다면 아래와 같은 응답이 온다.
```json
{
  "statusCode": 400,
  "message": "Validation failed (numeric string is expected)",
  "error": "Bad Request"
}
```

* 위 예시 코드에는 `Pipe` 클래스의 인스턴스가 아닌 `ParseIntPipe` 클래스 자체를 전달했다.   
  이는 `ParseIntPipe`의 인스턴스화와 의존성 주입을 프레임워크가 담당하게 한다.

* 개발자는 원하는 응답을 줄 수도 있다. 예를 들어 위의 경우, 숫자가 아닌 값이   
  id의 자리에 오면 `400(BAD_REQUEST)`가 응답되었지만, `406(NOT_ACCEPTABLE)`로   
  응답이 오게하려면 아래와 같이 하면 된다.
```ts
@Get(':id')
async findOne(@Param('id', new ParseIntPipe({ errorHttpStatusCode: HttpStatus.NOT_ACCEPTABLE}))id: number) {
    return this.exService.findOne(id);
} 
```

* 이제 id에 숫자가 아닌 값을 넣으면 아래의 응답이 온다.
```json
{
    "statusCode": 406,
    "message": "Validation failed (numeric string is expected)",
    "error": "Not Acceptable"
}
```

* `Parse**Pipe`들은 사용 구조가 모두 비슷하다. Query Parameter에 대한 것도 마찬가지이다.
```ts
@Get('/info')
  getInfoById(
    @Query(
      'id',
      new ParseIntPipe({ errorHttpStatusCode: HttpStatus.NOT_ACCEPTABLE }),
    )
    id: number,
  ): string {
    return `INFO ID : ${id}`;
  }
```

* 위 코드에 대한 요청URL은 아래와 같다.
```
GET localhost:3000/info?id=123
```
<hr/>

<h2>Custom Pipes</h2>

* 사용자의 회원 등록을 처리하는 POST API가 있다고 해보자.   
  사용자의 데이터를 가지는 DTO는 아래와 같다.
```ts
// src/dtos/create-user.dto.ts

export class UserCreateDto {
  name: string;
  email: string;
  password: string;
  phoneNumber: string;
}
```

* 그리고 해당 요청을 처리하는 컨트롤러와 서비스는 아래와 같다.   
  먼저 서비스이다.
```ts
// src/user/user.service.ts

import { UserCreateDto } from 'src/dtos/create-user.dto';

@Injectable()
export class UserService {
  saveUser(dto: UserCreateDto): string {
    return `Saving user.. name:${dto.name}, email:${dto.name}\n
                phoneNumber:${dto.phoneNumber} password:${dto.password}`;
  }
}
```

* 다음으로는 컨트롤러 코드이다.
```ts
//src/user/user.controller.ts
import { UserService } from './user.service';

@Controller('user')
export class UserController {
  constructor(private readonly userService: UserService) {}
  @Post()
  saveUser(@Body() dto: UserCreateDto): string {
    return this.userService.saveUser(dto);
  }
}
```

* 이제 위 Path로 요청을 보내면 정상적으로 작동한다.   
  하지만 만약 Request Body에서 name, email, password, phoneNumber 중   
  하나를 빼먹으면 Response Status는 `201(CREATED)`이지만 빼먹은 데이터의 값은   
  undefined가 된다.

* 위와 같이 Request Body가 잘못된 경우를 대비하기 위해 Custom Pipe가 필요하다.

* 우선 `Dto` 단에서 모든 정보가 필요함을 명시하기 위해 해당 기능을 제공하는 패키지를 설치하자.
```
yarn add class-validator
```

* 이제 constraint를 적용한 DTO 클래스는 아래와 같다.
```ts
import { IsString } from 'class-validator';

export class UserCreateDto {
  @IsString()
  name: string;

  @IsEmail()
  email: string;

  @IsString()
  password: string;

  @IsString()
  phoneNumber: string;
}
```

* 참고 : `class-validator` 링크: <a href="https://github.com/typestack/class-validator#usage">링크</a>

* 이 상태로 올바르지 않은 Request Body로 요청을 보내도 응답 결과는 달라지지 않는다.   
  추가적인 작업이 필요한데, 바로 Pipe를 만드는 것이다. 만들 때에는 `class-validator`   
  패키지를 만든 사람들이 함께 만든 `class-transformer` 패키지를 사용한다.
```
yarn add class-transformer
```

* 이제 `UserCreateDto`를 검증하기 위한 Pipe를 생성해보자.
```ts
// src/pipes/create-user.validation.pipe.ts

import { ArgumentMetadata, BadRequestException, Injectable, PipeTransform } from '@nestjs/common';
import { plainToClass } from 'class-transformer';
import { validate } from 'class-validator';

@Injectable()
export class UserInfoValidationPipe implements PipeTransform<any> {
  async transform(value: any, { metatype }: ArgumentMetadata) {
    if (!metatype || !this.toValidate(metatype)) {
      return value;
    }
    const object = plainToClass(metatype, value);
    const errors = await validate(object);
    if (errors.length > 0) {
      throw new BadRequestException('VALIDATION FAILED');
    }
    return value;
  }

  private toValidate(metatype: Function): boolean {
    const types: Function[] = [String, Boolean, Number, Array, Object];
    return !types.includes(metatype);
  }
}
```

* 먼저 `UserInfoValidationPipe#transform()`을 먼저 보자.   
  모든 Pipe는 `PipeTransform` 인터페이스의 구현체여야만 하며, `PipeTransform#transform()`을   
  구현해야 한다. `ransform()`은 value, metadata를 인자로 전달받는다.

* value는 해당 Pipe가 처리할 매개 변수들을 의미하며, metadata는 처리할 매개변수들의 메타데이터이다.   
  아래는 `ArgumentMetaData`의 코드이다.
```ts
export interface ArgumentMetadata {
    type: 'body' | 'query' | 'param' | 'custom';
    metatype?: Type<unknown>;
    data?: string;
}
```

  * `type` : 매개변수가 `@Body()`, `@Query()`, `@Param()` 등으로 읽혀온 것인지에 대한 정보
  * `metatype` : 매개변수의 메타 타입 정보를 가진다.
  * `data` : Decorator로 전달된 인자 값을 읽어온다.ex) `@Body('user')`


* 다시 `UserInfoValidationPipe`의 코드를 보자.   
  `UserInfoValidationPipe#transform()`는 async 처리가 되어 있다.   
  이는 Nest가 비동기, 동기적 pipe를 모두 제공하기에 사용 가능하기 때문이다.

* 다음으로는 `UserInfoValidationPipe`의 내부에서만 사용되는 `toValidate()` 함수를 보자.   
  