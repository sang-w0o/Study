# 명명 패턴보다 어노테이션을 사용하라

- 전통적으로 도구나 프레임워크가 특별히 다뤄야 할 프로그램 요소에는 딱 구분되는 명명 패턴을 적용해왔다.  
  예를 들어 JUnit은 JUnit3까지 테스트 메소드의 이름을 모두 `test`로 시작하게끔 했다.  
  이는 효과적인 방법이지만 단점도 많다.

  - 우선 오타가 나면 안된다.
  - 그리고 올바른 프로그램 요소에서만 사용되리라 보증할 방법이 없다는 것이다.  
    예를 들어 메소드가 아닌 클래스명을 `TestSafetyMechanisms`로 지어  
    JUnit에게 줬다 해보자. 개발자는 이 클래스에 정의된 테스트 메소드들을 수행해주길 기대하겠지만  
    실제로는 아무런 테스트가 수행되지 않는다.
  - 또한 프로그램 요소를 매개변수로 전달할 마땅한 방법이 없다. 특정 예외를 던져야만 성공하는  
    테스트가 있다 해보자. 기대하는 예외 타입을 테스트에 매개변수로 전달해야 하는 상황이다.  
    예외의 이름을 테스트 메소드명에 덧붙이는 방법도 있지만, 보기에도 나쁘고 깨지기도 쉽다.  
    컴파일러는 메소드명에 덧붙인 문자열이 예외를 가리키는지 알 도리가 없다. 테스트를 실행하기 전에는  
    그런 이름의 클래스가 존재하는지 혹은 예외가 맞는지조차 알 수 없다.

- 어노테이션은 이 모든 문제를 해결해주는 멋진 개념으로, JUnit도 JUnit4부터 전면 도입했다.  
  이번 아이템에서는 어노테이션의 동작 방식을 보기 위해 직접 작은 테스트 프레임워크를 만들어보자.  
  `@Test`라는 어노테이션을 정의해보자. 자동으로 수행되는 간단한 테스트용 어노테이션으로, 예외가  
  발생하면 해당 테스트를 실패 처리한다.

```java
/**
 * 테스트 메소드임을 선언하는 어노테이션
 * 매개변수 없는 정적 메소드 전용이다.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {
}
```

- 보다시피 `@Test` 어노테이션 타입 선언 자체에도 두 가지의 다른 어노테이션이 달려 있다.  
  바로 `@Retention`과 `@Target`이다. 이처럼 어노테이션 선언에 다는 어노테이션을 **Meta-Annotation**이라 한다.  
  `@Retention(RetentionPolicy.RUNTIME)` 메타어노테이션은 `@Test`가 런타임에도 유지되어야  
  한다는 표시다. 만약 이 메타어노테이션을 생략하면 테스트 도구는 `@Test`를 인식할 수 없다.  
  한편, `@Target(ElementType.METHOD)` 메타어노테이션은 `@Test`가 반드시 메소드 선언에만 사용돼야 한다고  
  알려준다. 따라서 클래스 선언, 필드 선언 등 다른 프로그램 요소에는 달 수 없다.

- 앞 코드의 메소드 주석에는 "매개변수 없는 정적 메소드 전용이다." 라고 쓰여 있다.  
  이 제약을 컴파일러가 강제할 수 있으면 좋겠지만, 그렇게 하려면 직접 적절한 어노테이션 처리기를 구현해야 한다.  
  관련 방법은 `javax.annotation.processing` API를 참고하자. 적절한 어노테이션 처리기 없이 인스턴스  
  메소드나 매개변수가 있는 메소드에 달면 어떻게 될까? 컴파일은 잘 되겠지만, 테스트 도구를 실행할 때 문제가 된다.

- 아래 코드는 `@Test`를 실제로 적용한 모습이다. 이와 같은 어노테이션을 _"아무 매개변수 없이 단순히 대상이 marking한다."_ 는  
  뜻에서 **Marker Annotation**이라 한다. 이 어노테이션을 사용하면 프로그래머가 `@Test`에 오타를 내거나 메소드 선언  
  외의 프로그램 요소에 달면 오류를 내준다.

```java
public class Sample {
    @Test public static void m1() { } // should success
    public static void m2() { }
    @Test public static void m3() {
	throw new RuntimeException("Fail"); // should fail
    }
    public static void m4() { }
    @Test public void m5() { } // Wrong usage: Not static method
    public static void m6() { }
    @Test public static void m7() {
	throw new RuntimeException("Fail"); // should fail
    }
    public static void m8() { }
}
```

- `Sample` 클래스에는 정적 메소드가 7개이며, 그중 4개에 `@Test`를 달았다.  
  `m3()`와 `m7()` 메소드는 예외를 던지고, `m1()`과 `m5()`는 그렇지 않다. 그리고 `m5()`는 인스턴스 메소드이기에  
  `@Test`를 잘못 사용한 경우다. 요약하면 총 4개의 테스트 메소드 중 1개는 성공, 2개는 실패, 1개는 잘못 사용됐다.  
  그리고 `@Test`를 붙이지 않은 나머지 4개의 메소드는 테스트 도구가 무시할 것이다.

- `@Test` 어노테이션이 `Sample` 클래스의 의미에 직접적인 영향을 주지는 않는다. 그저 이 어노테이션에 관심 있는  
  프로그램에게 추가 정보를 제공할 뿐이다. 더 넓게 이야기하면, 대상 코드의 의미는 그대로 둔 채 그 어노테이션에  
  관심 있는 도구에서 특별히 처리를 할 기회를 준다. 아래의 `RunTests`가 바로 그런 도구의 예시이다.

```java
public class RunTests {
    public static void main(String[] args) throws Exception {
        int tests = 0;
        int passed = 0;
        Class<?> testClass = Class.forName(args[0]);
        for(Method method : testClass.getDeclaredMethods()) {
            if(method.isAnnotationPresent(Test.class)) {
                tests++;
                try {
                    method.invoke(null);
                    passed++;
                } catch(InvocationTargetException wrappedException) {
                    Throwable exception = wrappedException.getCause();
                    System.out.println(method + " fail: " + exception);
                } catch(Exception exception) {
                    System.out.println("Wrong used @Test: " + method);
                }
            }
        }
        System.out.printf("Success: %d, Fail: %d", passed, tests - passed);
    }
}
```

- 이 테스트 러너는 명령줄로부터 완전 정규화된 클래스명을 받아, 그 클래스에서 `@Test` 어노테이션이 달린  
  메소드들을 차례로 호출한다. `isAnnotationPresent()`가 실행할 메소드를 찾아주는 메소드다. 테스트 메소드가  
  예외를 던지면 Reflection 메커니즘이 `InvocationTargetException`으로 감싸서 다시 던진다.  
  그래서 이 프로그램은 `InvocationTargetException`을 잡아 원래 예외에 담긴 실패 정보를 추출해(`getCause()`)  
  출력한다.

- `InvocationTargetException` 외의 예외가 발생했다면 `@Test`를 잘못 사용했다는 뜻이다. 아마도 인스턴스 메소드,  
  매개변수가 있는 메소드, 호출할 수 없는 메소드 등에 달았을 것이다. 위 코드의 두 번째 catch 블록은 이처럼 잘못  
  사용해 발생한 예외를 잡아 적절한 오류 메시지를 출력한다.

- 이제 특정 예외를 던져야만 성공하는 테스트를 지원하도록 해보자. 이를 위해선 새로운 어노테이션 타입이 필요하다.

```java
/**
 * 명시한 예외를 throw 해야지만 성공하는 테스트 메소드용 어노테이션
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionTest {
    Class<? extends Throwable> value();
}
```

- 이 어노테이션의 매개변수 타입은 `Class<? extends Throwable>`이다. 여기서의 와일드카드 타입은  
  많은 의미를 담고 있다. _"`Throwable`을 확장한 클래스의 `Class` 객체"_ 라는 뜻이며, 따라서 모든  
  예외와 오류 타입을 수용한다. 이는 한정적 타입 토큰의 또 하나의 활용 사례다. 그리고 아래는 이 어노테이션을  
  실제로 사용하는 모습이다. class 리터럴은 어노테이션의 매개변수의 값으로 사용됐다.

```java
public class Sample2 {
    @ExceptionTest(ArithmeticException.class)
    public static void m1() { // Should success
        int i = 0;
        i = i / 1;
    }

    @ExceptionTest(ArithmeticException.class)
    public static void m2() { // Should fail since other exception occurs
        int[] a = new int[0];
        int i = a[1];
    }

    @ExceptionTest(ArithmeticException.class)
    public static void m3() { } // Should fail since no exception occurs
}
```

- 이제 이 어노테이션을 다룰 수 있도록 테스트 도구를 수정해보자.

```java
public class RunTests {
    public static void main(String[] args) throws Exception {
        int tests = 0;
        int passed = 0;
        Class<?> testClass = Class.forName(args[0]);
        for(Method method : testClass.getDeclaredMethods()) {
            if(method.isAnnotationPresent(Test.class)) {
                tests++;
                try {
                    method.invoke(null);
                    passed++;
                } catch(InvocationTargetException wrappedException) {
                    Throwable exception = wrappedException.getCause();
                    System.out.println(method + " fail: " + exception);
                } catch(Exception exception) {
                    System.out.println("Wrong used @Test: " + method);
                }
            }
            if(method.isAnnotationPresent(ExceptionTest.class)) {
                tests++;
                try {
                   method.invoke(null);
                    System.out.printf("Test %s fail: Exception not thrown%n", method);
                } catch(InvocationTargetException wrappedException) {
                    Throwable exception = wrappedException.getCause();
                    Class<? extends Throwable> excType = method.getAnnotation(ExceptionTest.class).value();
                    if(excType.isInstance(exception)) {
                        passed++;
                    } else {
                        System.out.printf("Test %s failed: Expected exception %s, Actual exception %s%n", method, excType.getTypeName(), exception);
                    }
                } catch(Exception exception) {
                    System.out.println("Wrong used @ExceptionTest: " + method);
                }
            }
        }
        System.out.printf("Success: %d, Fail: %d", passed, tests - passed);
    }
}
```

- `@Test`를 위한 if 블록과 유사한 형태로 추가되었다. 한 가지 차이라면, 새로운 코드 블록은  
  어노테이션 매개변수의 값을 추출하여 테스트 메소드가 올바른 예외를 던지는지 확인하는 부분이 있다는 점이다.  
  형변환 코드가 없으니 `ClassCastException`은 걱정하지 않아도 된다. 따라서 테스트 프로그램이 문제 없이  
  컴파일되면 어노테이션 매개변수가 가리키는 예외가 올바른 타입이라는 뜻이다. 단, 해당 예외의 클래스 파일이  
  컴파일타임에는 존재했으나 런타임에는 존재하지 않을 수는 있다. 이런 경우라면 테스트 러너가 `TypeNotPresentException`을  
  던질 것이다.

- 이 예외 테스트 예시에서 한걸음 더 들어가, 예외를 여러 개 명시하고 그중 하나가 발생하면 성공하게 만들 수도 있다.  
  어노테이션 메커니즘에는 이런 쓰임에 아주 유용한 기능이 기본으로 들어가 있다. `@ExceptionTest`의 매개변수  
  타입을 `Class` 객체의 배열로 수정해보자.

```java
/**
 * 명시한 예외를 throw 해야지만 성공하는 테스트 메소드용 어노테이션
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionTest {
    Class<? extends Throwable>[] value();
}
```

- 배열 매개변수를 받는 어노테이션용 문법은 아주 유연하다. 단일 원소 배열에 최적화했지만, 앞서의  
  `@ExceptionTest`들도 모두 수정없이 수용한다. 원소가 여럿인 배열을 지정할 때는 아래와 같이  
  원소들을 중괄호로 감싸고 쉼표로 구분해주기만 하면 된다.

```java
public class Sample2 {
    //..

    @ExceptionTest({IndexOutOfBoundsException.class, NullPointerException.class})
    public static void doublyBad() {
        List<String> list = new ArrayList<>();
        list.addAll(5, null);
    }
}
```

- 이제 새로운 매개변수를 받을 수 있는 `@ExceptionTest`를 지원하도록 테스트 러너를 수정해보자.

```java
public class RunTests {
    public static void main(String[] args) throws Exception {
        int tests = 0;
        int passed = 0;
        Class<?> testClass = Class.forName(args[0]);
        for(Method method : testClass.getDeclaredMethods()) {
            //..
            if(method.isAnnotationPresent(ExceptionTest.class)) {
                tests++;
                try {
                   method.invoke(null);
                    System.out.printf("Test %s fail: Exception not thrown%n", method);
                } catch(InvocationTargetException wrappedException) {
                    Throwable exception = wrappedException.getCause();
                    int oldPassed = passed;
                    Class<? extends Throwable>[] excTypes = method.getAnnotation(ExceptionTest.class).value();
                    for(Class<? extends Throwable> excType: excTypes) {
                        if(excType.isInstance(exception)) {
                            passed++;
                            break;
                        }
                    }
                    if(passed == oldPassed) {
                        System.out.printf("Test %s failed: %s %n", method, exception);
                    }
                } catch(Exception exception) {
                    System.out.println("Wrong used @ExceptionTest: " + method);
                }
            }
        }
        System.out.printf("Success: %d, Fail: %d", passed, tests - passed);
    }
}

```

- Java8에서는 여러 개의 값을 받는 어노테이션을 다른 방식으로도 만들 수 있다. 배열 매개변수를 사용하는 대신  
  어노테이션에 `@Repeatable` 메타어노테이션을 다는 방식이다. `@Repeatable`을 단 어노테이션은 하나의  
  프로그램 요소에 여러번 달 수 있다. 단, 주의할 점이 있다. 첫째로 `@Repeatable`을 단 어노테이션을  
  반환하는 _컨테이너 어노테이션_ 을 하나 더 정의하고, `@Repeatable`에 이 컨테이너 어노테이션의  
  class 객체를 매개변수로 전달해야 한다. 두 번째로, 컨테이너 어노테이션은 내부 어노테이션 타입의 배열을  
  반환하는 `value()` 메소드를 정의해야 한다. 마지막으로 컨테이너 어노테이션 타입에는 적절한 보존 정책(`@Retention`)과  
  적용 대상(`@Target`)을 명시해야 한다. 그렇지 않으면 컴파일되지 않을 것이다.

```java
// @Repeatable 메타어노테이션 적용
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ExceptionTestContainer.class)
public @interface ExceptionTest {
    Class<? extends Throwable> value();
}

// 컨테이너 어노테이션
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionTestContainer {
    ExceptionTest[] value();
}

// 수정된 테스트 코드
@ExceptionTest(IndexOutOfBoundsException.class)
@ExceptionTest(NullPointerException.class)
public static void doublyBad() {
    List<String> list = new ArrayList<>();
    list.addAll(5, null);
}
```

- 반복 가능 어노테이션은 처리할 때도 주의를 해야 한다. 반복 가능 어노테이션을 여러 개 달면  
  하나만 달았을 때와 구분하기 위해 해당 _컨테이너_ 어노테이션 타입이 적용된다. `getAnnotationsbyType()`  
  메소드는 이 둘을 구분하지 않아서 반복 가능 어노테이션과 그 컨테이너 어노테이션을 모두 가져오지만,  
  `isAnnotationPresent()` 메소드는 둘을 명확히 구분한다. 따라서 반복 가능 어노테이션을 여러 번 적용한  
  다음 `isAnnotationPresent()`로 반복 어노테이션이 달렸는지를 검사한다면 _그렇지 않다._ 고 한다.  
  (컨테이너 어노테이션이 달렸다고 하기 때문.) 그 결과 어노테이션을 여러번 단 메소드들을 모두 무시하고 지나친다.  
  같은 이유로, `isAnnotationPresent()`로 컨테이너 어노테이션이 달렸는지 검사한다면 반복 가능 어노테이션을  
  한 번만 단 메소드를 무시하고 지나친다. 그래서 달려 있는 개수와 상관없이 모두 검사하려면 둘을 따로따로  
  확인하는 수밖에 없다. 아래는 `RunTests` 프로그램이 `@ExceptionTest`의 반복 가능 버전을 사용하도록  
  수정한 모습이다.

```java
public class RunTests {
    public static void main(String[] args) throws Exception {
        int tests = 0;
        int passed = 0;
        Class<?> testClass = Class.forName(args[0]);
        for(Method method : testClass.getDeclaredMethods()) {
            //..
            if(method.isAnnotationPresent(ExceptionTest.class) || method.isAnnotationPresent(ExceptionTestContainer.class)) {
                tests++;
                try {
                   method.invoke(null);
                   System.out.printf("Test %s fail: Exception not thrown%n", method);
                } catch(InvocationTargetException wrappedException) {
                    Throwable exception = wrappedException.getCause();
                    int oldPassed = passed;
                    ExceptionTest[] exceptionTests = method.getAnnotationsByType(ExceptionTest.class);
                    for(ExceptionTest exceptionTest : exceptionTests) {
                        if(exceptionTest.value().isInstance(exception)) {
                            passed++;
                            break;
                        }
                    }
                    if(passed == oldPassed) {
                        System.out.printf("Test %s failed: %s %n", method, exception);
                    }
                } catch(Exception exception) {
                    System.out.println("Wrong used @ExceptionTest: " + method);
                }
            }
        }
        System.out.printf("Success: %d, Fail: %d", passed, tests - passed);
    }
}
```

- 반복 가능 어노테이션을 사용해 하나의 프로그램 요소에 같은 어노테이션을 여러번 달 때의 코드 가독성을 높여보았다.  
  이 방식으로 코드의 가독성을 개선할 수 있다면 이 방식을 사용하도록 하자. 하지만 어노테이션을 선언하고  
  이를 처리하는 부분에서는 코드량이 늘어나며, 특히 처리 코드가 복잡해져 오류가 날 가능성이 커짐을 명시하자.

- 이번 아이템에서 만든 테스트 프레임워크는 아주 간단하지만 어노테이션이 명명 패턴보다 낫다는 점은 확실히 보여준다.  
  테스트는 어노테이션으로 할 수 있는 일 중 극히 일부일 뿐이다.  
  **어노테이션으로 할 수 있는 일을 명명 패턴으로 처리할 이유는 없다.**

- 도구 제작자를 제외하고는, 일반 프로그래머가 어노테이션 타입을 직접 정의할 일은 거의 없다.  
  하지만 **Java 프로그래머라면 예외 없이 Java가 제공하는 어노테이션 타입들을 사용해야 한다.**  
  IDE나 정적 분석 도구가 제공하는 어노테이션을 사용하면, 해당 도구가 제공하는 진단 정보의 품질을  
  높여줄 것이다. 단, 이러한 어노테이션들은 표준이 아니니 도구를 바꾸거나 표준이 만들어지면 수정 작업이 필요할 것이다.

<hr/>
