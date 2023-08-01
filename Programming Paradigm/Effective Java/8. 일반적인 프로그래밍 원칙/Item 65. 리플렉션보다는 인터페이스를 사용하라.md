# 리플렉션보다는 인터페이스를 사용하라

- 리플렉션 기능(`java.lang.reflect`)을 이용하면 프로그램에서 임의의 클래스에 접근할 수 있다.  
  `Class` 객체가 주어지면 그 클래스의 생성자, 메소드, 필드에 해당하는 `Constructor`,  
  `Method`, `Field` 인스턴스를 가져올 수 있고, 이어서 이 인스턴스들로는 그 클래스의 멤버명,  
  필드 타입, 메소드 시그니처 등을 가져올 수 있다.

- 나아가 `Constructor`, `Method`, `Field` 인스턴스를 이용해 각각에 연결된 실제 생성자,  
  메소드, 필드를 조작할 수도 있다. 이 인스턴스들을 통해 해당 클래스의 인스턴스를 생성하거나, 메소드를  
  호출하거나, 필드에 접근할 수 있다는 뜻이다. 예를 들어 `Method#invoke()`는 어떤 클래스의  
  어떤 객체가 가진 어떠한 메소드라도 호출할 수 있게 해준다.(물론 일반적인 보안 제약사항은 준수해야 한다.)  
  리플렉션을 이용하면 컴파일 당시에 존재하지 않던 클래스도 이용할 수 있는데, 물론 단점이 있다.

  - **컴파일타임 타입 검사가 주는 이점을 하나도 누릴 수 없다.** 예외 검사도 마찬가지다.  
    프로그램이 리플렉션 기능을 써서 존재하지 않는, 혹은 접근할 수 없는 메소드를 호출하려 시도하면  
    런타임 오류가 발생할 수 있다.

  - **리플렉션을 이용하면 코드가 지저분하고 장황해진다.** 지루한 일이고, 읽기도 어렵다.

  - **성능이 떨어진다.** 리플렉션을 통한 메소드 호출은 일반 메소드 호출보다 훨씬 느리다.

- 코드 분석 도구나 의존관계 주입 프레임워크처럼 리플렉션을 써야 하는 복잡한 애플리케이션이 몇 가지 있다.  
  하지만 이런 도구들마저 리플렉션 사용을 점차 줄이고 있다. 단점이 명백하기 때문이다. 애플리케이션에  
  리플렉션이 필요한지 확신할 수 없다면 아마도 필요 없을 가능성이 클 것이다.

- **리플렉션은 아주 제한된 형태로만 사용해야 그 단점을 피하고 이점만 취할 수 있다.** 컴파일타임에  
  이용할 수 없는 클래스를 사용해야만 하는 프로그램은 비록 컴파일타임이라도 적절한 인터페이스나 상위 클래스를  
  이용할 수는 있을 것이다. 다행이 이런 경우라면 **리플렉션은 인스턴스 생성에만 쓰고, 이렇게 만든 인스턴스는**  
  **인터페이스나 상위 클래스로 참조해 사용하자.**

- 예를 들어 아래 프로그램은 `Set<String>` 인터페이스의 인스턴스를 생성하는데, 정확한 클래스는  
  명령줄의 첫 번째 인수로 확정한다. 그리고 생성한 집합(`Set`)에 두 번째 이후의 인수들을 추가한 다음  
  화면에 출력한다. 첫 번째 인수와 상관없이 이후의 인수들에서 중복은 제거한 후 출력한다. 반면, 이 인수들이  
  출력되는 순서는 첫 번째 인수로 지정한 클래스가 무엇이냐에 따라 달라진다. `HashSet`을 지정하면  
  무작위 순서가 될 것이고, `TreeSet`을 지정하면 알파벳 순서로 출력될 것이다.

```java
public static void main(String[] args) {
  // 클래스명을 Class로 반환
  Class<? extends Set<String>> cl = null;
  try {
    // 비검사 형변환
    cl = (Class<? extends Set<String>>) Class.forName(args[0]);
  } catch(ClassNotFoundException e) {
    fatalError("Class not found");
  }

  // 생성자 획득
  Constructor<? extends Set<String>> cons = null;
  try {
	cons = cl.getDeclaredConstructor();
  } catch(NoSuchMethodException e) {
	fatalError("No default constructor found");
  }

  // Set 인스턴스 생성
  Set<String> s = null;
  try {
    s = cons.newInstance();
  } catch(IllegalAccessException e) {
    fatalError("Unable to access constructor");
  } catch(InstantiationException e) {
    fatalError("Cannot instantiate class");
  } catch(InvocationTargetException e) {
    fatalError("Constructor threw error: " + e.getCause());
  } catch(ClassCastException e) {
    fatalError("Class does not implement Set");
  }

  // 생성한 집합 사용
  s.addAll(Arrays.asList(args).subList(1, args.length));
  System.out.println(s);
}

private static void fatalError(String msg) {
  System.err.println(msg);
  System.exit(1);
}
```

- 위 예시는 리플렉션의 단점 두 개를 보여준다. 첫 번째, 런타임에 총 6개나 되는 예외를 던질 수 있다.  
  이 모두는 인스턴스를 리플렉션 없이 생성했다면 컴파일타임에 잡아낼 수 있었을 예외들이다.  
  두 번째, 클래스 이름만으로 인스턴스를 생성해내기 위해 무려 25줄이나 되는 코드를 작성했다.  
  리플렉션이 아니라면 생성자 호출 한 줄로 끝났을 일이다.

> 참고로 리플렉션 예외 각각을 잡는 대신 모든 리플렉션 예외의 상위 클래스인  
> `ReflectiveOperationException`을 잡도록 해서 코드 길이를 줄일 수도 있다.

- 두 단점 모두 객체를 생성하는 부분에만 국한된다. 객체가 일단 만들어지면 그 후의  
  코드는 여타 `Set` 인스턴스를 사용할 때와 똑같다. 그래서 실제 프로그램에서는 이런 제약에 영향받는  
  코드는 일부에 지나지 않는다.

- 이 프로그램을 컴파일하면 비검사 형변환 경고가 뜬다. 하지만 `Class<? extends Set<String>>`으로의  
  형변환은 심지어 명시한 클래스가 `Set`을 구현하지 않았더라도 성공할 것이라, 실제 문제로 이어지지는 않는다.  
  단, 그 클래스의 인스턴스를 생성하려 할 때 `ClassCastException`을 던지게 된다. 이 경고를 숨기기  
  위해서는 `@SuppressWarnings("unchecked")`를 사용하면 된다.

- 드물긴 하지만, 리플렉션은 런타임에 존재하지 않을 수도 있는 다른 클래스, 메소드, 필드와의 의존성을 관리할 때  
  적합하다. 이 기법은 버전이 여러 개 존재하는 외부 패키지를 다룰 때 유용하다. 가동할 수 있는 최소한의 환경,  
  즉 주로 가장 오래된 버전만을 지원하도록 컴파일 한 후, 이후 버전의 클래스와 메소드 등은 리플렉션으로  
  접근하는 방식이다. 이렇게 하려면 접근하려는 새로운 클래스나 메소드가 런타임에 존재하지 않을 수도 있다는  
  사실을 반드시 감안해야 한다. 즉, 같은 목적을 이룰 수 있는 대체 수단을 이용하거나 기능을 줄여 동작하는 등의  
  적절한 조치를 취해야 한다.

---

## 핵심 정리

- 리플렉션은 복잡한 특수 시스템을 개발할 때 필요한 강력한 기능이지만, 단점도 많다.  
  컴파일타임에는 알 수 없는 클래스를 사용하는 프로그램을 작성한다면 리플렉션을 사용해야 할 것이다.  
  단, 되도록이면 객체 생성에만 사용하고, 생성한 객체를 이용할 때는 적절한 인터페이스나 컴파일타임에  
  알 수 있는 상위 클래스로 형변환해 사용해야 한다.

---
