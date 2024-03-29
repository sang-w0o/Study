# 어노테이션과 리플렉션

- 코틀린에서 어노테이션을 사용하는 문법은 Java와 거의 같다.
- 코틀린에서는 Java보다 더 넓은 대상에 어노테이션을 적용할 수 있다. 그런 대상으로는 file, 식이 있다.
- 어노테이션 인자로 원시 타입 값, 문자열, enum, 클래스 참조, 다른 어노테이션의 인스턴스, 그리고 지금까지 말한 여러 유형의 값으로  
  이뤄진 배열을 사용할 수 있다.
- `@get:Rule`을 사용해 어노테이션의 사용 대상을 명시하면 한 코틀린 선언이 여러 가지 바이트코드 요소를 만들어내는 경우, 정확히 어떤  
  부분에 어노테이션을 적용할지 지정할 수 있다.
- 어노테이션 클래스를 정의할 때는 본문이 없고 주 생성자의 모든 파라미터를 val 프로퍼티로 표시한 코틀린 클래스를 사용한다.
- 메타어노테이션을 사용해 대상, 어노테이션 유지 방식 등 여러 어노테이션 특성을 지정할 수 있다.
- 리플렉션 API를 통해 실행 시점에 객체의 메소드와 프로퍼티를 열거하고 접근할 수 있다.  
  리플렉션 API에는 `KClass`(클래스), `KFunction`(함수) 등 여러 종류의 선언을 표현하는 인터페이스가 있다.
- 클래스를 컴파일 시점에 알고 있다면 `KClass` 인스턴스를 얻기 위해 `클래스명::class`를 사용한다.  
  하지만 실행 시점에 obj 변수에 담긴 객체로부터 `KClass` 인스턴스를 얻기 위해서는 `obj.javaClass.kotlin`을 사용한다.
- `KFunction`과 `KProperty` 인터페이스는 모두 `KCallable`을 확장한다.  
  `KCallable`은 제네릭 메소드인 `call()`을 제공한다.
- `KCallable.callBy()` 메소드를 사용하면 메소드를 호출하면서 default 파라미터 값을 사용할 수 있다.
- `KFunction0`, `KFunction1` 등의 인터페이스는 모두 파라미터 수가 다른 함수를 표현하며, `invoke()` 메소드를 통해 함수를 호출할 수 있다.
- `KProperty0`은 최상위 프로퍼티나 변수, `KProperty1`은 수신 객체가 있는 프로퍼티에 접근할 때 쓰는 인터페이스이다.  
  두 인터페이스 모두 `get()` 메소드를 사용해 프로퍼티 값을 가져올 수 있다.  
  `KMutableProperty0`와 `KMutableProperty1`은 각각 `KProperty0`와 `KProperty1`을 확장하며, `set()` 메소드를 통해 프로퍼티의  
  값을 변경할 수 있게 해준다.

---
